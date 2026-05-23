package com.github.zen05.lunarsaga.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.github.zen05.lunarsaga.component.Animation2D;
import com.github.zen05.lunarsaga.component.Animation2D.AnimationType;
import com.github.zen05.lunarsaga.component.Fsm;
import com.github.zen05.lunarsaga.component.Move;

/**
 * FSM (Máy trạng thái hữu hạn) cho Kẻ địch theo phong cách Legend of Lua.
 * Mỗi state chịu trách nhiệm chuyển đổi Animation và set direction cho Move component.
 *
 * Luồng: IDLE → WANDER → CHASE (và ngược lại)
 */
public enum EnemyState implements State<Entity> {

    IDLE {
        @Override
        public void enter(Entity entity) {
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim != null) anim.setType(AnimationType.IDLE);

            // Dừng lại khi vào trạng thái IDLE
            Move move = Move.MAPPER.get(entity);
            if (move != null) move.getDirection().setZero();
        }

        @Override
        public void update(Entity entity) {
            // Logic IDLE đơn giản: chỉ đứng yên.
            // EnemyAISystem sẽ kiểm tra khoảng cách → tự chuyển sang CHASE nếu Player đến gần.
        }

        @Override
        public void exit(Entity entity) {}

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) { return false; }
    },

    WANDER {
        @Override
        public void enter(Entity entity) {
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim != null) anim.setType(AnimationType.WALK);
        }

        @Override
        public void update(Entity entity) {
            // Logic WANDER: di chuyển ngẫu nhiên.
            // EnemyAISystem sẽ đặt direction ngẫu nhiên và kiểm tra khoảng cách → CHASE.
        }

        @Override
        public void exit(Entity entity) {}

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) { return false; }
    },

    CHASE {
        @Override
        public void enter(Entity entity) {
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim != null) anim.setType(AnimationType.WALK);
        }

        @Override
        public void update(Entity entity) {
            // Logic CHASE: hướng về phía Player.
            // EnemyAISystem sẽ tính toán vector hướng và đặt vào Move.direction.
        }

        @Override
        public void exit(Entity entity) {
            // Dừng lại khi thoát CHASE
            Move move = Move.MAPPER.get(entity);
            if (move != null) move.getDirection().setZero();
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) { return false; }
    }
}
