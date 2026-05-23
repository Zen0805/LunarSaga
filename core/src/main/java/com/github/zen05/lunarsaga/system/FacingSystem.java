package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.zen05.lunarsaga.component.Controller;
import com.github.zen05.lunarsaga.component.Facing;
import com.github.zen05.lunarsaga.component.Move;
import com.github.zen05.lunarsaga.component.Transform;

import com.github.zen05.lunarsaga.component.Enemy;

public class FacingSystem extends IteratingSystem {

    public FacingSystem() {
        super(Family.all(Facing.class, Move.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Facing facing = Facing.MAPPER.get(entity);
        Controller controller = Controller.MAPPER.get(entity);

        // ─── PLAYER: dùng 4 hướng đầy đủ + ngắm chuột ─────────────────────
        if (controller != null) {
            if (controller.isAiming()) {
                Transform transform = Transform.MAPPER.get(entity);
                if (transform != null) {
                    Vector2 diff = new Vector2(controller.getAimTarget()).sub(transform.getPosition());

                    if (diff.x > 0) facing.setFlipX(false);
                    else if (diff.x < 0) facing.setFlipX(true);

                    if (Math.abs(diff.y) > Math.abs(diff.x)) {
                        if (diff.y > 0) facing.setDirection(Facing.FacingDirection.UP);
                        else facing.setDirection(Facing.FacingDirection.DOWN);
                    } else {
                        facing.setDirection(Facing.FacingDirection.RIGHT);
                    }
                    return;
                }
            }

            Move move = Move.MAPPER.get(entity);
            if (move == null || move.getDirection().isZero()) return;

            Vector2 dir = move.getDirection();
            if (dir.x > 0f) facing.setFlipX(false);
            else if (dir.x < 0f) facing.setFlipX(true);

            if (dir.y > 0f) facing.setDirection(Facing.FacingDirection.UP);
            else if (dir.y < 0f) facing.setDirection(Facing.FacingDirection.DOWN);
            else if (dir.x > 0f) facing.setDirection(Facing.FacingDirection.RIGHT);
            else facing.setDirection(Facing.FacingDirection.RIGHT); // trái → flip RIGHT
            return;
        }

        // ─── ENEMY (và các entity khác): chỉ trái/phải ──────────────────────
        // Nếu di chuyển theo trục Y (lên/xuống), GIỮ NGUYÊN hướng mặt hiện tại.
        // Logic này giống Legend of Lua: quái không "nhìn lên/xuống", chỉ trái/phải.
        Move move = Move.MAPPER.get(entity);
        if (move == null || move.getDirection().isZero()) return;

        Vector2 dir = move.getDirection();

        // Chỉ cập nhật flip khi có thành phần X
        if (dir.x > 0f) {
            facing.setFlipX(false);
            facing.setDirection(Facing.FacingDirection.RIGHT);
        } else if (dir.x < 0f) {
            facing.setFlipX(true);
            facing.setDirection(Facing.FacingDirection.RIGHT); // flip ảnh RIGHT → thành LEFT
        }
        // Nếu chỉ có Y (lên/xuống): không làm gì → giữ nguyên hướng mặt cũ
    }
}

