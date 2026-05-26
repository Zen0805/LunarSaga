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
import com.github.zen05.lunarsaga.ai.PlayerDef;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.asset.AtlasAsset;
import com.github.zen05.lunarsaga.component.*;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.List;

public class ProjectileSystem extends IteratingSystem {

    private static final float ARROW_LIFETIME = 4f; // Tối đa 4 giây trước khi biến mất

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
        
        // Cập nhật logic bay (z thay đổi theo parabol, trả về true nếu z <= 0)
        boolean shouldDie = projectile.updateLogic(deltaTime);
        if (shouldDie) {
            deadEntities.add(entity);
            return;
        }

        // Di chuyển ngang luôn đều đặn từ đầu đến cuối
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
                    float chargeTime = controller.getChargeTime();
                    spawnArrow(
                            controller.getShootOrigin().x,
                            controller.getShootOrigin().y,
                            controller.getAimTarget(),
                            chargeTime
                    );
                    controller.setPendingShoot(false);
                    controller.resetChargeTime(); // Reset sau khi bắn
                }
            }
        }

        super.update(deltaTime);

        Engine engine = getEngine();
        for (Entity dead : deadEntities) {
            engine.removeEntity(dead);
        }
        deadEntities.clear();
    }

    /**
     * Tạo một entity mũi tên. 
     * Tốc độ, sát thương và THỜI GIAN BAY (tầm xa) được tính dựa vào chargeTime.
     */
    public void spawnArrow(float spawnX, float spawnY, Vector2 aimTarget, float chargeTime) {
        if (arrowRegion == null) return;

        // ── 1. Tính tỉ lệ tụ lực (0.0 → 1.0) ────────────────────────────────
        float chargeRatio = MathUtils.clamp(chargeTime / PlayerDef.CHARGE_MAX_TIME, 0f, 1f);

        // ── 2. Tốc độ, sát thương, tầm xa nội suy theo tỉ lệ charge ─────────
        float speed    = MathUtils.lerp(PlayerDef.ARROW_SPEED_MIN, PlayerDef.ARROW_SPEED_MAX, chargeRatio);
        int   damage   = Math.round(MathUtils.lerp(PlayerDef.ARROW_DAMAGE_MIN, PlayerDef.ARROW_DAMAGE_MAX, chargeRatio));
        
        // Thời gian bay (lifetime). Kết hợp với speed sẽ ra tầm xa của mũi tên.
        float lifetime = MathUtils.lerp(PlayerDef.ARROW_LIFETIME_MIN, PlayerDef.ARROW_LIFETIME_MAX, chargeRatio); 

        // ── 3. Hướng bay ─────────────────────────────────────────────────────
        Vector2 direction = new Vector2(aimTarget.x - spawnX, aimTarget.y - spawnY).nor();
        float angleRad = MathUtils.atan2(direction.y, direction.x);
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        float arrowW = arrowRegion.getRegionWidth()  * GdxGame.UNIT_SCALE;
        float arrowH = arrowRegion.getRegionHeight() * GdxGame.UNIT_SCALE;

        // ── 4. Tạo Transform & Components ────────────────────────────────────
        Transform transform = new Transform(
                new Vector2(spawnX - arrowW * 0.5f, spawnY - arrowH * 0.5f),
                1,
                new Vector2(arrowW, arrowH),
                new Vector2(1f, 1f),
                angleDeg
        );

        Move move = new Move(speed);
        move.getDirection().set(direction);

        Projectile projectile = new Projectile(
                lifetime, direction, damage, PlayerDef.ARROW_INITIAL_Z
        );
        Graphic graphic = new Graphic(arrowRegion, Color.WHITE.cpy());

        // ── 5. Body Box2D ─────────────────────────────────────────────────────
        com.badlogic.gdx.physics.box2d.BodyDef bodyDef = new com.badlogic.gdx.physics.box2d.BodyDef();
        bodyDef.type = com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(transform.getPosition());
        bodyDef.fixedRotation = true;

        com.badlogic.gdx.physics.box2d.Body body = physicWorld.createBody(bodyDef);

        com.badlogic.gdx.physics.box2d.PolygonShape shape = new com.badlogic.gdx.physics.box2d.PolygonShape();
        float halfW = arrowW * 0.5f;
        float halfH = arrowH * 0.5f;
        // Xoay hitbox (PolygonShape) khớp với góc của mũi tên thay vì nằm ngang cố định
        shape.setAsBox(halfW, halfH, new Vector2(halfW, halfH), angleRad);

        com.badlogic.gdx.physics.box2d.FixtureDef fixtureDef = new com.badlogic.gdx.physics.box2d.FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef).setUserData("arrow");
        shape.dispose();

        Entity arrow = getEngine().createEntity();
        body.setUserData(arrow);

        arrow.add(transform);
        arrow.add(move);
        arrow.add(projectile);
        arrow.add(graphic);
        arrow.add(new Physic(body));

        getEngine().addEntity(arrow);
    }

}
