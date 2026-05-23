package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.github.zen05.lunarsaga.component.Move;
import com.github.zen05.lunarsaga.component.Physic;
import com.github.zen05.lunarsaga.component.Transform;

/**
 * Giai đoạn 1 của vòng lặp vật lý:
 *  - Áp vận tốc từ Move vào Box2D body (PhysicMoveSystem role cộng gộp luôn)
 *  - Bước thời gian cố định (fixed timestep) với nội suy vị trí (interpolation)
 *  - Đồng bộ vị trí Box2D body → Transform để RenderSystem dùng
 *  - Khi Entity có Physic bị xóa khỏi Engine, body tương ứng cũng bị xóa khỏi World.
 *
 * Tham khảo: mystictutorial / system/PhysicSystem.java + system/PhysicMoveSystem.java
 */
public class PhysicSystem extends IteratingSystem implements EntityListener {

    private static final float FIXED_STEP = 1f / 60f;
    private final World world;
    private float accumulator = 0f;

    public PhysicSystem(World world) {
        super(Family.all(Physic.class, Transform.class).get());
        this.world = world;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Thay vì nghe getFamily() (đòi hỏi cả Physic và Transform),
        // ta chỉ cần nghe những Entity có Physic để dọn dẹp Box2D Body khi chúng bị xóa.
        // Điều này sửa lỗi Portal (không có Transform) không bị dọn dẹp Body khi chuyển map.
        engine.addEntityListener(Family.all(Physic.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(this);
    }

    // ─── EntityListener ───────────────────────────────────────────────────────

    @Override
    public void entityAdded(Entity entity) {}

    @Override
    public void entityRemoved(Entity entity) {
        // Khi một entity bị xóa, giải phóng body Box2D tương ứng
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null) {
            world.destroyBody(physic.getBody());
        }
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Bước 1: Áp vận tốc từ Move component vào body (thay thế MoveSystem).
     * Bước 2: Chạy vật lý với timestep cố định.
     * Bước 3: Nội suy vị trí và ghi vào Transform.
     */
    @Override
    public void update(float deltaTime) {
        // Bước 1: cập nhật vận tốc cho tất cả body từ Move component
        applyVelocities();

        // Bước 2: fixed timestep physics step
        accumulator += deltaTime;
        while (accumulator >= FIXED_STEP) {
            // Lưu vị trí trước bước vật lý để nội suy
            for (Entity entity : getEntities()) {
                Physic physic = Physic.MAPPER.get(entity);
                physic.getPrevPosition().set(physic.getBody().getPosition());
            }
            world.step(FIXED_STEP, 6, 2);
            accumulator -= FIXED_STEP;
        }
        world.clearForces();

        // Bước 3: nội suy vị trí và cập nhật Transform
        float alpha = accumulator / FIXED_STEP;
        for (Entity entity : getEntities()) {
            interpolateEntity(entity, alpha);
        }
    }

    private void applyVelocities() {
        for (Entity entity : getEntities()) {
            Move move = Move.MAPPER.get(entity);
            Physic physic = Physic.MAPPER.get(entity);
            if (move == null || physic == null) continue;

            Body body = physic.getBody();
            if (move.isRooted() || move.getDirection().isZero()) {
                body.setLinearVelocity(0f, 0f);
            } else {
                float speed = move.getMaxSpeed();
                
                // Chuẩn hóa vector để đi chéo không bị nhanh hơn
                float len = move.getDirection().len();
                float nx = move.getDirection().x / len;
                float ny = move.getDirection().y / len;

                body.setLinearVelocity(
                        nx * speed,
                        ny * speed
                );
            }
        }
    }

    private void interpolateEntity(Entity entity, float alpha) {
        Physic physic = Physic.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        transform.getPosition().set(
                MathUtils.lerp(physic.getPrevPosition().x, physic.getBody().getPosition().x, alpha),
                MathUtils.lerp(physic.getPrevPosition().y, physic.getBody().getPosition().y, alpha)
        );
    }

    // processEntity không dùng — tất cả xử lý trong update()
    @Override
    protected void processEntity(Entity entity, float deltaTime) {}

}
