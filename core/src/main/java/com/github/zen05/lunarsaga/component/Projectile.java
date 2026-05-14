package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class Projectile implements Component {

    public static final ComponentMapper<Projectile> MAPPER = ComponentMapper.getFor(Projectile.class);

    private float lifetime;
    private final Vector2 direction;

    public Projectile(float lifetime, Vector2 direction) {
        this.lifetime = lifetime;
        this.direction = new Vector2(direction);
    }

    public float getLifetime() {
        return lifetime;
    }

    public void decLifetime(float delta) {
        this.lifetime -= delta;
    }

    public boolean isDead() {
        return lifetime <= 0f;
    }

    public Vector2 getDirection() {
        return direction;
    }

}
