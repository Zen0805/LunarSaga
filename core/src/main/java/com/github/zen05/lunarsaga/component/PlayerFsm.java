package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.github.zen05.lunarsaga.ai.PlayerState;

/**
 * Component Máy trạng thái (Gameplay Logic) dành riêng cho Player.
 * Quản lý các trạng thái như NORMAL, DEAD...
 */
public class PlayerFsm implements Component {

    public static final ComponentMapper<PlayerFsm> MAPPER = ComponentMapper.getFor(PlayerFsm.class);

    private final DefaultStateMachine<Entity, PlayerState> stateMachine;

    public PlayerFsm(Entity owner) {
        // Bắt đầu luôn ở trạng thái NORMAL
        this.stateMachine = new DefaultStateMachine<>(owner, PlayerState.NORMAL);
    }

    public DefaultStateMachine<Entity, PlayerState> getStateMachine() {
        return stateMachine;
    }
}
