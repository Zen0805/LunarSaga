package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.github.zen05.lunarsaga.ai.EnemyState;

public class EnemyFsm implements Component {

    public static final ComponentMapper<EnemyFsm> MAPPER = ComponentMapper.getFor(EnemyFsm.class);

    private final DefaultStateMachine<Entity, EnemyState> stateMachine;

    public EnemyFsm(DefaultStateMachine<Entity, EnemyState> stateMachine) {
        this.stateMachine = stateMachine;
    }

    public DefaultStateMachine<Entity, EnemyState> getStateMachine() {
        return stateMachine;
    }
}
