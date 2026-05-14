package com.github.zen05.lunarsaga.input;

public interface ControllerState {

    void keyDown(Command command);

    default void keyUp(Command command) {

    }

    default boolean touchDown(float worldX, float worldY, int pointer, int button) { return false; }
    default boolean touchUp(float worldX, float worldY, int pointer, int button) { return false; }
    default boolean touchDragged(float worldX, float worldY, int pointer) { return false; }
    default boolean mouseMoved(float worldX, float worldY) { return false; }

}
