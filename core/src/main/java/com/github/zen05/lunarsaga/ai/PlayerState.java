package com.github.zen05.lunarsaga.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.github.zen05.lunarsaga.component.Physic;

/**
 * Gameplay Logic State cho Player.
 * Quản lý luật chơi thay vì chỉ hoạt ảnh.
 */
public enum PlayerState implements State<Entity> {

    /** Trạng thái bình thường: Cho phép di chuyển và bắn cung. */
    NORMAL {
        @Override
        public void enter(Entity entity) {
        }

        @Override
        public void update(Entity entity) {
        }

        @Override
        public void exit(Entity entity) {
        }
    },

    /**
     * Trạng thái chết: Khóa phím, dừng di chuyển Box2D.
     * Logic đếm ngược chuyển màn hình được xử lý bên PlayerFsmSystem.
     */
    DEAD {
        @Override
        public void enter(Entity entity) {
            Physic physic = Physic.MAPPER.get(entity);
            if (physic != null && physic.getBody() != null) {
                // Khóa cứng nhân vật ngay lập tức
                physic.getBody().setLinearVelocity(0, 0);
                // Tắt hoàn toàn Box2D body để không nhận va chạm (quái không đẩy, tên không
                // trúng)
                physic.getBody().setActive(false);
            }
        }

        @Override
        public void update(Entity entity) {
            // Đảm bảo không bị ngoại lực đẩy đi khi đã chết
            Physic physic = Physic.MAPPER.get(entity);
            if (physic != null && physic.getBody() != null) {
                physic.getBody().setLinearVelocity(0, 0);
            }
        }

        @Override
        public void exit(Entity entity) {
        }
    };

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }
}
