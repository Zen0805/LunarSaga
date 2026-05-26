package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.zen05.lunarsaga.input.Command;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Controller implements Component {

    public static final ComponentMapper<Controller> MAPPER = ComponentMapper.getFor(Controller.class);

    private final List<Command> pressedCommands;
    private final List<Command> releasedCommands;
    private final Vector2 aimTarget;
    private boolean isAiming;
    private boolean pendingShoot;
    private final Vector2 shootOrigin;

    /** Thời gian giữ chuột (giây). Tăng dần khi isAiming=true, reset về 0 sau khi bắn. */
    private float chargeTime;

    public Controller() {
        this.pressedCommands = new ArrayList<>();
        this.releasedCommands = new ArrayList<>();
        this.aimTarget = new Vector2();
        this.isAiming = false;
        this.pendingShoot = false;
        this.shootOrigin = new Vector2();
        this.chargeTime = 0f;
    }

    public List<Command> getPressedCommands() {
        return pressedCommands;
    }

    public List<Command> getReleasedCommands() {
        return releasedCommands;
    }

    public Vector2 getAimTarget() {
        return aimTarget;
    }

    public boolean isAiming() {
        return isAiming;
    }

    public void setAiming(boolean aiming) {
        isAiming = aiming;
    }

    public boolean isPendingShoot() {
        return pendingShoot;
    }

    public void setPendingShoot(boolean pendingShoot) {
        this.pendingShoot = pendingShoot;
    }

    public Vector2 getShootOrigin() {
        return shootOrigin;
    }

    public float getChargeTime() {
        return chargeTime;
    }

    public void addChargeTime(float delta) {
        this.chargeTime += delta;
    }

    public void resetChargeTime() {
        this.chargeTime = 0f;
    }
}
