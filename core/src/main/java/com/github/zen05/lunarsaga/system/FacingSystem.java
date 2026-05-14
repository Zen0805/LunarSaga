package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.zen05.lunarsaga.component.Controller;
import com.github.zen05.lunarsaga.component.Facing;
import com.github.zen05.lunarsaga.component.Move;
import com.github.zen05.lunarsaga.component.Transform;

public class FacingSystem extends IteratingSystem {

    public FacingSystem() {
        super(Family.all(Facing.class, Move.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Facing facing = Facing.MAPPER.get(entity);
        Controller controller = Controller.MAPPER.get(entity);

        if (controller != null && controller.isAiming()) {
            Transform transform = Transform.MAPPER.get(entity);
            if (transform != null) {
                Vector2 diff = new Vector2(controller.getAimTarget()).sub(transform.getPosition());
                
                // Luôn cập nhật flipX theo hướng ngắm chuột (dù chuột đang ở trên hay dưới)
                if (diff.x > 0) {
                    facing.setFlipX(false);
                } else if (diff.x < 0) {
                    facing.setFlipX(true);
                }

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
        if (move == null) return;

        Vector2 moveDirection = move.getDirection();
        if (moveDirection.isZero()) {
            return;
        }

        if (moveDirection.x > 0f) {
            facing.setFlipX(false);
        } else if (moveDirection.x < 0f) {
            facing.setFlipX(true);
        }

        if (moveDirection.y > 0f){
            facing.setDirection(Facing.FacingDirection.UP);
        } else if (moveDirection.y < 0f){
            facing.setDirection(Facing.FacingDirection.DOWN);
        } else if (moveDirection.x > 0f) {
            facing.setDirection(Facing.FacingDirection.RIGHT);
        } else {
            // Khi đi sang trái, ta dùng hình ảnh của RIGHT nhưng lật ngang (flipX)
            facing.setDirection(Facing.FacingDirection.RIGHT);
        }
    }
}
