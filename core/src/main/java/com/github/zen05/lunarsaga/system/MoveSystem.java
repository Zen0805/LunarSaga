package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.zen05.lunarsaga.component.Move;
import com.github.zen05.lunarsaga.component.Transform;

public class MoveSystem extends IteratingSystem {
    private final Vector2 normalizeDirection = new Vector2();

    public MoveSystem() {

        super(Family.all(Move.class, Transform.class).get());

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Move move = Move.MAPPER.get(entity);
        if (move.isRooted() || move.getDirection().isZero()){
            return;
        }

        normalizeDirection.set(move.getDirection()).nor();
        Transform transform = Transform.MAPPER.get(entity);
        Vector2 position = transform.getPosition();
        position.set(
            position.x + move.getMaxSpeed() * move.getDirection().x * deltaTime,
            position.y + move.getMaxSpeed() * move.getDirection().y * deltaTime
        );


    }
}
