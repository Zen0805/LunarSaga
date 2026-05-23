package com.github.zen05.lunarsaga.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.asset.MapAsset;
import com.github.zen05.lunarsaga.component.Controller;
import com.github.zen05.lunarsaga.component.Physic;
import com.github.zen05.lunarsaga.component.Player;
import com.github.zen05.lunarsaga.component.Portal;
import com.github.zen05.lunarsaga.component.Transform;
import com.github.zen05.lunarsaga.input.GameControllerState;
import com.github.zen05.lunarsaga.input.KeyboardController;
import com.github.zen05.lunarsaga.system.*;
import com.github.zen05.lunarsaga.tiled.TiledAshleyConfigurator;
import com.github.zen05.lunarsaga.tiled.TiledService;

import java.util.function.Consumer;

public class GameScreen extends ScreenAdapter {

    private final Engine engine;
    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;
    private final KeyboardController keyboardController;
    private final GdxGame game;
    private final World physicWorld;
    private final com.badlogic.gdx.physics.box2d.Box2DDebugRenderer debugRenderer;

    private Portal pendingPortal = null;

    public GameScreen(GdxGame game) {
        this.game = game;

        // Không có trọng lực (top-down game)
        this.physicWorld = new World(new Vector2(0f, 0f), true);
        this.debugRenderer = new com.badlogic.gdx.physics.box2d.Box2DDebugRenderer();

        this.tiledService = new TiledService(game.getAssetService(), this.physicWorld);
        this.engine = new Engine();
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine, game.getAssetService(),
                this.physicWorld);
        this.keyboardController = new KeyboardController(GameControllerState.class, engine, game.getViewport());

        this.engine.addSystem(new ControllerSystem());
        this.engine.addSystem(new PhysicSystem(this.physicWorld));
        
        this.physicWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object dataA = contact.getFixtureA().getBody().getUserData();
                Object dataB = contact.getFixtureB().getBody().getUserData();

                if (dataA instanceof Entity entityA && dataB instanceof Entity entityB) {
                    checkPortalCollision(entityA, entityB);
                    checkPortalCollision(entityB, entityA);
                }
            }

            private void checkPortalCollision(Entity player, Entity portalEntity) {
                if (Controller.MAPPER.has(player) && Portal.MAPPER.has(portalEntity)) {
                    pendingPortal = Portal.MAPPER.get(portalEntity);
                }
            }

            @Override
            public void endContact(Contact contact) {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });

        this.engine.addSystem(new FsmSystem());
        this.engine.addSystem(new EnemyAISystem());   // Tính toán AI (timer, random hướng)
        this.engine.addSystem(new EnemyFsmSystem());  // Tick FSM sau khi AI đã cập nhật

        this.engine.addSystem(new FacingSystem());
        this.engine.addSystem(new AnimationSystem(game.getAssetService()));
        this.engine.addSystem(new ProjectileSystem(game.getAssetService(), this.physicWorld));
        this.engine.addSystem(new CameraSystem(game.getCamera())); // Thêm hệ thống Camera
        this.engine.addSystem(
                new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera(), game.getAssetService()));
    }

    @Override
    public void show() {
        game.setInputProcessor(keyboardController);
        keyboardController.setActiveState(GameControllerState.class);

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = this.engine.getSystem(CameraSystem.class)::setMap;
        this.tiledService.setMapChangeConsumer(map -> {
            renderConsumer.accept(map);
            cameraConsumer.accept(map);
        });
        this.tiledService.setLoadObjectConsumer(this.tiledAshleyConfigurator::onLoadObject);

        TiledMap tiledMap = this.tiledService.loadMap(MapAsset.MAIN);
        this.tiledService.setMap(tiledMap);
    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
    }

    @Override
    public void render(float delta) {
        if (pendingPortal != null) {
            changeMap(pendingPortal);
            pendingPortal = null;
        }

        delta = Math.min(delta, 1 / 30f);
        this.engine.update(delta);
        this.debugRenderer.render(this.physicWorld, game.getCamera().combined);
    }

    private void changeMap(Portal portal) {
        // Bước 1: Tìm Player và lưu lại reference trước khi xóa entity
        Entity player = engine.getEntitiesFor(Family.all(Player.class).get()).first();

        // Bước 2: Xóa tất cả entity không phải Player khỏi Engine.
        // Phải copy ra array riêng vì không thể xóa trong khi đang lặp.
        // PhysicSystem sẽ tự xử lý destroy Box2D body của entity khi nó bị xóa (entityRemoved listener).
        Array<Entity> toRemove = new Array<>();
        ImmutableArray<Entity> allEntities = engine.getEntitiesFor(Family.all().get());
        for (Entity entity : allEntities) {
            if (!Player.MAPPER.has(entity)) {
                toRemove.add(entity);
            }
        }
        for (Entity entity : toRemove) {
            engine.removeEntity(entity);
        }

        // Bước 3: Di chuyển Player đến tọa độ portal chỉ định (bằng tile units)
        // Trước khi setMap() để camera snap đúng vị trí ngay từ đầu
        if (player != null) {
            // Lấy map mới để biết kích thước tile
            TiledMap newMap = tiledService.loadMap(portal.getToMap());
            int tileW = newMap.getProperties().get("tilewidth", 16, Integer.class);
            int tileH = newMap.getProperties().get("tileheight", 16, Integer.class);
            float targetX = portal.getToX() * tileW * GdxGame.UNIT_SCALE;
            float targetY = portal.getToY() * tileH * GdxGame.UNIT_SCALE;

            Physic physic = Physic.MAPPER.get(player);
            Transform transform = Transform.MAPPER.get(player);
            physic.getBody().setTransform(targetX, targetY, 0);
            physic.getPrevPosition().set(targetX, targetY);
            transform.getPosition().set(targetX, targetY);

            // Bước 4: Load map mới (sẽ không spawn Player vì đã có Player.class trong Engine)
            tiledService.setMap(newMap);
        }
    }

    @Override
    public void dispose() {
        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable) {
                ((Disposable) system).dispose();
            }
        }
        this.physicWorld.dispose();
        this.debugRenderer.dispose();
    }

}
