package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Tag/Marker component để nhận diện đây là nhân vật chính (Player).
 * Dùng để lọc khi chuyển map — tất cả entity KHÔNG có component này
 * sẽ bị xóa. Player được sinh ra duy nhất 1 lần và tồn tại xuyên suốt game.
 */
public class Player implements Component {
    public static final ComponentMapper<Player> MAPPER = ComponentMapper.getFor(Player.class);
}
