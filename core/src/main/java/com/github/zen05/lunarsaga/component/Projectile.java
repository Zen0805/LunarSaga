package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

/**
 * Dữ liệu đạn (mũi tên).
 * Chuyển sang cơ chế Parabol (ném xiên) mượt mà hoàn hảo:
 * Dựa vào thời gian bay (totalTime), tự động tính lực nảy Z ban đầu (dz) 
 * sao cho mũi tên vẽ thành 1 đường cong đều từ lúc bắn đến lúc cắm đất.
 */
public class Projectile implements Component {

    public static final ComponentMapper<Projectile> MAPPER = ComponentMapper.getFor(Projectile.class);

    private final Vector2 direction;
    private int    damage;

    // ── Trục Z Ảo ────────────────────────────────────────────────────────────
    private float z;   // Cao độ hiện tại
    private float dz;  // Vận tốc trục Z
    
    // Đánh dấu đã bắn trúng mục tiêu (biến mất ngay)
    private boolean hitTarget = false;

    /**
     * @param totalTime  Tổng thời gian bay mong muốn (càng lâu bay càng xa)
     * @param direction  Hướng bay (đã normalize)
     * @param damage     Sát thương gây ra khi trúng
     * @param initialZ   Cao độ ban đầu (bằng tay Player cầm cung)
     */
    public Projectile(float totalTime, Vector2 direction, int damage, float initialZ) {
        this.direction = new Vector2(direction);
        this.damage    = damage;
        this.z         = initialZ;

        // Phép màu Vật lý: Tính lực nảy Z ban đầu để chạm đất ĐÚNG lúc totalTime kết thúc.
        // Phương trình: 0 = z0 + v0*t - 0.5*g*t^2 
        // => v0 = (0.5*g*t^2 - z0) / t = 0.5*g*t - z0/t
        this.dz = (0.5f * com.github.zen05.lunarsaga.ai.PlayerDef.ARROW_GRAVITY * totalTime) - (initialZ / totalTime);
    }

    /** 
     * Trả về true nếu mũi tên chạm đất (z<=0) hoặc trúng mục tiêu. 
     */
    public boolean updateLogic(float delta) {
        if (hitTarget) return true; // Xóa ngay nếu trúng

        // Euler Integration tạo đường cong Parabol cực mượt
        dz -= com.github.zen05.lunarsaga.ai.PlayerDef.ARROW_GRAVITY * delta;
        z  += dz * delta;

        if (z <= 0f) {
            z = 0f;
            return true; // Chạm đất → báo xóa
        }
        return false;
    }

    /** Khi trúng mục tiêu → biến mất ngay. */
    public void kill() {
        this.hitTarget = true;
    }

    public Vector2 getDirection() { return direction; }
    public int     getDamage()    { return damage; }
    public float   getZ()         { return z; }
}
