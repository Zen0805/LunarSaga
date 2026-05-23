package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.zen05.lunarsaga.component.EnemyFsm;

/**
 * Hệ thống tick FSM (Máy trạng thái) cho Kẻ địch mỗi frame.
 * Tương tự FsmSystem nhưng dùng EnemyFsm thay vì Fsm,
 * để tách biệt logic AI Enemy khỏi AnimationState của Player.
 */
public class EnemyFsmSystem extends IteratingSystem {

    public EnemyFsmSystem() {
        super(Family.all(EnemyFsm.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        EnemyFsm.MAPPER.get(entity).getStateMachine().update();
    }
}
