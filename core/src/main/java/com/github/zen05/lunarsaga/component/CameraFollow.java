package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class CameraFollow implements Component {
    public static final ComponentMapper<CameraFollow> MAPPER = ComponentMapper.getFor(CameraFollow.class);
}
