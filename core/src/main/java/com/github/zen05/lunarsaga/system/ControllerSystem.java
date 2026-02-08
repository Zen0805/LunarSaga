package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.zen05.lunarsaga.component.Controller;
import com.github.zen05.lunarsaga.component.Move;
import com.github.zen05.lunarsaga.input.Command;

public class ControllerSystem extends IteratingSystem {

    public ControllerSystem() {

        super(Family.all(Controller.class).get());

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() && controller.getReleasedCommands().isEmpty()) {
            return;
        }

    for (Command command : controller.getPressedCommands()){

        switch (command){

            case UP -> moveEntity(entity, 0f, 1f);
            case DOWN -> moveEntity(entity, 0f, -1f);
            case LEFT -> moveEntity(entity, -1f, 0f);
            case RIGHT -> moveEntity(entity, 1f, 0f);

        }

    }
    controller.getPressedCommands().clear();

    for (Command command : controller.getReleasedCommands()){

        switch (command){

            case UP -> moveEntity(entity, 0f, -1f);
            case DOWN -> moveEntity(entity, 0f, 1f);
            case LEFT -> moveEntity(entity, 1f, 0f);
            case RIGHT -> moveEntity(entity, -1f, 0f);

        }

    }
    controller.getReleasedCommands().clear();

    }


    private void moveEntity(Entity entity, float directionX, float directionY) {

        Move move = Move.MAPPER.get(entity);
        if (move == null) return;

        move.getDirection().x += directionX;
        move.getDirection().y += directionY;

    }
}
