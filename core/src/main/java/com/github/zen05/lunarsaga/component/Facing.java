package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Facing implements Component {

    public static final ComponentMapper<Facing> MAPPER = ComponentMapper.getFor(Facing.class);

    private FacingDirection direction;
    private boolean flipX;

    public Facing(FacingDirection direction) {
        this.direction = direction;
        this.flipX = false;
    }

    public FacingDirection getDirection() {
        return direction;
    }

    public void setDirection(FacingDirection direction) {
        this.direction = direction;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public enum FacingDirection {

        UP, UP_LEFT, UP_RIGHT, DOWN, DOWN_LEFT, DOWN_RIGHT, LEFT, RIGHT;

        private final String atlasKey;

        FacingDirection() {
            this.atlasKey = name().toLowerCase();
        }

        public String getAtlasKey() {
            return atlasKey;
        }

    }

}
