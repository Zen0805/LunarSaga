package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Đánh dấu Entity có thể bị tấn công và quản lý iFrames (khung hình bất tử tạm thời).
 * Tham chiếu: self.invulnerable + self.invulnerableTimer trong Legend of Lua.
 *
 * Khi bị đánh trúng:
 * 1. iFrameTimer = iFrameDuration (bắt đầu bất tử)
 * 2. knockbackTimer chạy đồng thời (kích thước nhỏ hơn): block velocity
 * 3. Mỗi frame: DamageSystem tick timer xuống
 * 4. RenderSystem mờ dần alpha theo nhịp nhấp nháy
 * 5. Khi timer = 0: entity có thể bị đánh lại
 */
public class Damageable implements Component {

    public static final ComponentMapper<Damageable> MAPPER = ComponentMapper.getFor(Damageable.class);

    /** Tổng thời gian bất tử sau khi bị đánh (giây). */
    private final float iFrameDuration;
    /** Thời gian còn lại (đếm ngược → 0). */
    private float iFrameTimer = 0f;

    /** Knockback window: trong thời gian này PhysicSystem bỏ qua velocity để body trượt tự do. */
    private float knockbackTimer = 0f;
    public static final float KNOCKBACK_DURATION = 0.2f; // 0.2s đầu sau khi trúng đòn

    /** Lực Knockback được áp dụng qua Box2D Impulse. */
    public static final float KNOCKBACK_IMPULSE = 3.5f;

    public Damageable(float iFrameDuration) {
        this.iFrameDuration = iFrameDuration;
    }

    // ─── iFrames ──────────────────────────────────────────────────────────

    /** Kích hoạt iFrames và knockback window. Gọi ngay khi trúng đòn. */
    public void triggerInvulnerability() {
        this.iFrameTimer    = iFrameDuration;
        this.knockbackTimer = KNOCKBACK_DURATION;
    }

    /** Gọi mỗi frame từ DamageSystem để đếm ngược. */
    public void tickInvulnerability(float delta) {
        if (iFrameTimer > 0f)
            iFrameTimer = Math.max(0f, iFrameTimer - delta);
        if (knockbackTimer > 0f)
            knockbackTimer = Math.max(0f, knockbackTimer - delta);
    }

    /** True trong suốt thời gian iFrames → DamageSystem bỏ qua va chạm. */
    public boolean isInvulnerable() {
        return iFrameTimer > 0f;
    }

    /**
     * True trong 0.2s đầu sau khi trúng đòn.
     * PhysicSystem dùng cái này: nếu true → bỏ qua setLinearVelocity để
     * body trượt tự do theo lực Impulse, tạo cảm giác "văng ra sau rồi phanh lại".
     */
    public boolean isKnockbackActive() {
        return knockbackTimer > 0f;
    }

    /**
     * Trả về giá trị alpha để RenderSystem vẽ hiệu ứng mờ dần nhấp nháy.
     * - Khi không trong iFrames: alpha = 1.0 (bình thường)
     * - Khi trong iFrames: alpha dao động giữa 0.25 và 0.9 theo sin
     *   tạo hiệu ứng "mờ dần rồi nhấp nháy" tự nhiên.
     */
    public float getFlashAlpha() {
        if (iFrameTimer <= 0f) return 1.0f;
        // dùng sin để dao động mượt: sin(t * pi / 0.1) → chu kỳ 0.2s
        // map từ [-1, 1] → [0.25, 0.9]
        float sineVal = (float) Math.sin(iFrameTimer * Math.PI / 0.1f);
        return 0.25f + (sineVal + 1f) * 0.5f * (0.9f - 0.25f);
    }

    public float getIFrameTimer() {
        return iFrameTimer;
    }

    public float getIFrameDuration() {
        return iFrameDuration;
    }
}
