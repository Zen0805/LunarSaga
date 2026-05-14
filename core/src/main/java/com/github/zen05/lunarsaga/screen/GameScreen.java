package com.github.zen05.lunarsaga.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.asset.MapAsset;
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

    public GameScreen(GdxGame game) {
        this.game = game;

        // Không có trọng lực (top-down game)
        this.physicWorld = new World(new Vector2(0f, 0f), true);

        this.tiledService = new TiledService(game.getAssetService(), this.physicWorld);
        this.engine = new Engine();
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine, game.getAssetService(),
                this.physicWorld);
        this.keyboardController = new KeyboardController(GameControllerState.class, engine, game.getViewport());

        this.engine.addSystem(new ControllerSystem());
        this.engine.addSystem(new PhysicSystem(this.physicWorld)); // Thay thế MoveSystem
        this.engine.addSystem(new FsmSystem());
        this.engine.addSystem(new FacingSystem());
        this.engine.addSystem(new AnimationSystem(game.getAssetService()));
        this.engine.addSystem(new ProjectileSystem(game.getAssetService(), this.physicWorld));
        this.engine.addSystem(
                new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera(), game.getAssetService()));
    }

    @Override
    public void show() {
        game.setInputProcessor(keyboardController);
        keyboardController.setActiveState(GameControllerState.class);

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        this.tiledService.setMapChangeConsumer(renderConsumer);
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
        delta = Math.min(delta, 1 / 30f);
        this.engine.update(delta);
    }

    @Override
    public void dispose() {
        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable) {
                ((Disposable) system).dispose();
            }
        }
        this.physicWorld.dispose();
    }

}
