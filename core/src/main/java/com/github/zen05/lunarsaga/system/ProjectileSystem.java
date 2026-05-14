package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.asset.AtlasAsset;
import com.github.zen05.lunarsaga.component.*;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.List;

public class ProjectileSystem extends IteratingSystem {

    private static final float ARROW_SPEED = 7f;
    private static final float ARROW_LIFETIME = 3f;

    private final TextureRegion arrowRegion;
    private final List<Entity> deadEntities = new ArrayList<>();
    private ImmutableArray<Entity> controllerEntities;
    private final World physicWorld;

    public ProjectileSystem(AssetService assetService, World physicWorld) {
        super(Family.all(Projectile.class, Transform.class, Move.class).get());

        this.physicWorld = physicWorld;
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        this.arrowRegion = atlas.findRegion("weapons/arrow");
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        controllerEntities = engine.getEntitiesFor(Family.all(Controller.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Projectile projectile = Projectile.MAPPER.get(entity);
        projectile.decLifetime(deltaTime);

        if (projectile.isDead()) {
            deadEntities.add(entity);
            return;
        }

        // Di chuyển mũi tên theo hướng cố định của nó
        Move move = Move.MAPPER.get(entity);
        move.getDirection().set(projectile.getDirection());
    }

    @Override
    public void update(float deltaTime) {
        // Poll pendingShoot từ các entity có Controller
        if (controllerEntities != null) {
            for (Entity entity : controllerEntities) {
                Controller controller = Controller.MAPPER.get(entity);
                if (controller.isPendingShoot()) {
                    spawnArrow(
                            controller.getShootOrigin().x,
                            controller.getShootOrigin().y,
                            controller.getAimTarget()
                    );
                    controller.setPendingShoot(false);
                }
            }
        }

        super.update(deltaTime);

        // Xoá các entity đã hết hạn ngoài vòng lặp
        Engine engine = getEngine();
        for (Entity dead : deadEntities) {
            engine.removeEntity(dead);
        }
        deadEntities.clear();
    }

    /**
     * Tạo một entity mũi tên tại vị trí spawnX/spawnY bay về hướng aimTarget.
     * Tham khảo spawnArrow() trong legend-of-lua-main/src/items/arrow.lua:
     *   direction = toMouseVector(x, y)
     *   arrow.speed = 230
     *   arrow.rot = math.atan2(direction.y, direction.x)
     */
    public void spawnArrow(float spawnX, float spawnY, Vector2 aimTarget) {
        if (arrowRegion == null) return;

        // Hướng từ điểm spawn đến chuột
        Vector2 direction = new Vector2(aimTarget.x - spawnX, aimTarget.y - spawnY).nor();

        // Góc xoay của mũi tên (giống math.atan2 trong Lua)
        float angleRad = MathUtils.atan2(direction.y, direction.x);
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        float arrowW = arrowRegion.getRegionWidth() * GdxGame.UNIT_SCALE;
        float arrowH = arrowRegion.getRegionHeight() * GdxGame.UNIT_SCALE;

        // Tạo Transform tại vị trí spawn
        Transform transform = new Transform(
                new Vector2(spawnX - arrowW * 0.5f, spawnY - arrowH * 0.5f),
                1,
                new Vector2(arrowW, arrowH),
                new Vector2(1f, 1f),
                angleDeg
        );

        Move move = new Move(ARROW_SPEED);
        move.getDirection().set(direction);

        Projectile projectile = new Projectile(ARROW_LIFETIME, direction);
        Graphic graphic = new Graphic(arrowRegion, Color.WHITE.cpy());

        // Tạo Body vật lý cho mũi tên
        com.badlogic.gdx.physics.box2d.BodyDef bodyDef = new com.badlogic.gdx.physics.box2d.BodyDef();
        bodyDef.type = com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(transform.getPosition());
        bodyDef.fixedRotation = true;

        com.badlogic.gdx.physics.box2d.Body body = physicWorld.createBody(bodyDef);
        
        com.badlogic.gdx.physics.box2d.PolygonShape shape = new com.badlogic.gdx.physics.box2d.PolygonShape();
        float halfW = arrowW * 0.5f;
        float halfH = arrowH * 0.5f;
        shape.setAsBox(halfW, halfH, new Vector2(halfW, halfH), 0f);

        com.badlogic.gdx.physics.box2d.FixtureDef fixtureDef = new com.badlogic.gdx.physics.box2d.FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // Là Sensor để không đẩy Player ra, chỉ dùng phát hiện va chạm
        body.createFixture(fixtureDef).setUserData("arrow");
        shape.dispose();

        Entity arrow = getEngine().createEntity();
        body.setUserData(arrow);

        arrow.add(transform);
        arrow.add(move);
        arrow.add(projectile);
        arrow.add(graphic);
        arrow.add(new Physic(body)); // Gắn Physic để PhysicSystem xử lý di chuyển

        getEngine().addEntity(arrow);
    }

}
