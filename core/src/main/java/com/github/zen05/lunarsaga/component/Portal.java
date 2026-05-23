package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.zen05.lunarsaga.asset.MapAsset;

public class Portal implements Component {
    public static final ComponentMapper<Portal> MAPPER = ComponentMapper.getFor(Portal.class);

    private final MapAsset toMap;
    private final float toX;
    private final float toY;

    public Portal(MapAsset toMap, float toX, float toY) {
        this.toMap = toMap;
        this.toX = toX;
        this.toY = toY;
    }

    public MapAsset getToMap() {
        return toMap;
    }

    public float getToX() {
        return toX;
    }

    public float getToY() {
        return toY;
    }
}
