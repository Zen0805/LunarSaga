package com.github.zen05.lunarsaga.input;

public interface ControllerState {

    void keyDown(Command command);

    default void keyUp(Command command) {

    }

}
