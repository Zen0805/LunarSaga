package com.github.zen05.lunarsaga.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.github.zen05.lunarsaga.component.Controller;

public class GameControllerState implements ControllerState {

    private final ImmutableArray<Entity> controllerEntities;

    public GameControllerState(Engine engine) {
        this.controllerEntities = engine.getEntitiesFor(Family.all(Controller.class).get());
    }

    @Override
    public void keyDown(Command command) {

        for (Entity entity : controllerEntities) {
            Controller.MAPPER.get(entity).getPressedCommands().add(command);
        }

    }

    @Override
    public void keyUp(Command command) {

        for (Entity entity : controllerEntities) {
            Controller.MAPPER.get(entity).getReleasedCommands().add(command);
        }

    }

    @Override
    public boolean touchDown(float worldX, float worldY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            for (Entity entity : controllerEntities) {
                Controller controller = Controller.MAPPER.get(entity);
                controller.setAiming(true);
                controller.getAimTarget().set(worldX, worldY);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(float worldX, float worldY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            for (Entity entity : controllerEntities) {
                Controller controller = Controller.MAPPER.get(entity);
                // Chỉ bắn nếu đang ở trạng thái ngắm
                if (controller.isAiming()) {
                    controller.setAiming(false);
                    controller.getAimTarget().set(worldX, worldY);
                    // Lấy vị trí tâm nhân vật làm điểm spawn mũi tên
                    com.github.zen05.lunarsaga.component.Transform transform =
                            com.github.zen05.lunarsaga.component.Transform.MAPPER.get(entity);
                    if (transform != null) {
                        controller.getShootOrigin().set(
                                transform.getPosition().x + transform.getSize().x * 0.5f,
                                transform.getPosition().y + transform.getSize().y * 0.5f
                        );
                        controller.setPendingShoot(true);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(float worldX, float worldY, int pointer) {
        for (Entity entity : controllerEntities) {
            Controller.MAPPER.get(entity).getAimTarget().set(worldX, worldY);
        }
        return true;
    }

    @Override
    public boolean mouseMoved(float worldX, float worldY) {
        for (Entity entity : controllerEntities) {
            Controller.MAPPER.get(entity).getAimTarget().set(worldX, worldY);
        }
        return true;
    }

}
