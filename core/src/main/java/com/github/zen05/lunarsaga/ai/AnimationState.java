package com.github.zen05.lunarsaga.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.github.zen05.lunarsaga.component.Animation2D;
import com.github.zen05.lunarsaga.component.Animation2D.AnimationType;
import com.github.zen05.lunarsaga.component.Fsm;
import com.github.zen05.lunarsaga.component.Move;


public enum AnimationState implements State<Entity>{

    IDLE {
        @Override
        public void enter(Entity entity) {

            Animation2D.MAPPER.get(entity).setType(AnimationType.IDLE);

        }

        @Override
        public void update(Entity entity) {

            Move move = Move.MAPPER.get(entity);

            if (move != null && !move.isRooted() && !move.getDirection().isZero()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(WALK);
                return;
            }

        }

        @Override
        public void exit(Entity entity) {

        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    WALK {
        @Override
        public void enter(Entity entity) {

            Animation2D.MAPPER.get(entity).setType(AnimationType.WALK);

        }

        @Override
        public void update(Entity entity) {

            Move move = Move.MAPPER.get(entity);

            if (move == null || move.isRooted() || move.getDirection().isZero()) {

                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);

            }

        }

        @Override
        public void exit(Entity entity) {

        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    }

}
