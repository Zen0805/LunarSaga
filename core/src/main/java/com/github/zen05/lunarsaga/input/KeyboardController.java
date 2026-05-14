package com.github.zen05.lunarsaga.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;
import java.util.Map;


public class KeyboardController extends InputAdapter{

    private static final Map<Integer, Command> KEY_MAPPING = Map.ofEntries(
        Map.entry(Input.Keys.W, Command.UP),
        Map.entry(Input.Keys.S, Command.DOWN),
        Map.entry(Input.Keys.A, Command.LEFT),
        Map.entry(Input.Keys.D, Command.RIGHT),
        Map.entry(Input.Keys.SPACE, Command.SELECT),
        Map.entry(Input.Keys.ESCAPE, Command.CANCEL)
    );

    private final boolean[] commandState;
    private final Map<Class<? extends ControllerState>, ControllerState> stateCache;
    private ControllerState activeState;
    private final Viewport viewport;
    private final Vector2 tempVec = new Vector2();

    public KeyboardController(Class<? extends ControllerState> initialState, Engine engine, Viewport viewport) {

        this.stateCache = new HashMap<>();
        this.activeState = null;
        this.commandState = new boolean[Command.values().length];
        this.viewport = viewport;

        this.stateCache.put(IdleControllerState.class, new IdleControllerState());
        this.stateCache.put(GameControllerState.class, new GameControllerState(engine));
        setActiveState(initialState);

    }

    public void setActiveState(Class<? extends ControllerState> stateClass) {

        ControllerState controllerState = stateCache.get(stateClass);
        if (controllerState == null) {
            throw new GdxRuntimeException("No state with class: " + stateClass + " found in the state cache");
        }

        for (Command command : Command.values()) {
            if (this.activeState != null && this.commandState[command.ordinal()]) {
                this.activeState.keyUp(command);
            }
            this.commandState[command.ordinal()] = false;
        }

        this.activeState = controllerState;

    }


    @Override
    public boolean keyDown(int keycode) {
        Command command = KEY_MAPPING.get(keycode);
        if (command == null) return false;

        this.commandState[command.ordinal()] = true;
        this.activeState.keyDown(command);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {

        Command command = KEY_MAPPING.get(keycode);
        if (command == null) return false;
        if (!this.commandState[command.ordinal()]) return false;

        this.commandState[command.ordinal()] = false;
        this.activeState.keyUp(command);
        return true;
    }

    private void unproject(int screenX, int screenY) {
        tempVec.set(screenX, screenY);
        viewport.unproject(tempVec);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        unproject(screenX, screenY);
        return this.activeState.touchDown(tempVec.x, tempVec.y, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        unproject(screenX, screenY);
        return this.activeState.touchUp(tempVec.x, tempVec.y, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        unproject(screenX, screenY);
        return this.activeState.touchDragged(tempVec.x, tempVec.y, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        unproject(screenX, screenY);
        return this.activeState.mouseMoved(tempVec.x, tempVec.y);
    }

}
