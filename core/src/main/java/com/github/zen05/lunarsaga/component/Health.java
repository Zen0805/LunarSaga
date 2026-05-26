package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Lưu trữ máu của Entity (Enemy hoặc Player).
 * Tham chiếu: entity.health trong Legend of Lua.
 */
public class Health implements Component {

    public static final ComponentMapper<Health> MAPPER = ComponentMapper.getFor(Health.class);

    private int maxHp;
    private int currentHp;

    public Health(int maxHp) {
        this.maxHp     = maxHp;
        this.currentHp = maxHp;
    }

    public int getMaxHp()     { return maxHp; }
    public int getCurrentHp() { return currentHp; }

    public boolean isDead()   { return currentHp <= 0; }

    /** Trừ máu. Trả về lượng máu thực sự đã bị trừ. */
    public int takeDamage(int amount) {
        int actual = Math.min(amount, currentHp);
        currentHp -= actual;
        return actual;
    }

    /** Khôi phục máu (dùng cho item hồi phục sau này). */
    public void heal(int amount) {
        currentHp = Math.min(currentHp + amount, maxHp);
    }
}
