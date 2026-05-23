package com.github.zen05.lunarsaga.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class Enemy implements Component {

    public static final ComponentMapper<Enemy> MAPPER = ComponentMapper.getFor(Enemy.class);

    private final String enemyId;

    // ─── Điểm neo (Spawn Point) ───────────────────────────────────────────────
    private final Vector2 spawnPoint = new Vector2();

    // ─── Bán kính AI (world units, mặc định chuẩn cho Bat) ───────────────────
    private final float wanderRadius; // 3f — phạm vi đi dạo quanh tổ
    private final float aggroRange; // 4f — tầm phát hiện Player
    private final float deAggroRange; // 6f — tầm mất dấu (luôn > aggroRange)
    private final float leashRange; // 8f — dây xích tối đa khi đang đuổi

    // ─── Timer nội bộ ─────────────────────────────────────────────────────────
    private float timer = 0f;
    private float targetTime = 0f;

    public Enemy(String enemyId,
            float wanderRadius, float aggroRange,
            float deAggroRange, float leashRange) {
        this.enemyId = enemyId;
        this.wanderRadius = wanderRadius;
        this.aggroRange = aggroRange;
        this.deAggroRange = deAggroRange;
        this.leashRange = leashRange;
    }

    // ─── Getters cơ bản ───────────────────────────────────────────────────────

    public String getEnemyId() {
        return enemyId;
    }

    public Vector2 getSpawnPoint() {
        return spawnPoint;
    }

    /** Gọi một lần duy nhất ngay sau khi entity được khởi tạo. */
    public void setSpawnPoint(float x, float y) {
        spawnPoint.set(x, y);
    }

    public float getWanderRadius() {
        return wanderRadius;
    }

    public float getAggroRange() {
        return aggroRange;
    }

    public float getDeAggroRange() {
        return deAggroRange;
    }

    public float getLeashRange() {
        return leashRange;
    }

    // ─── Timer ────────────────────────────────────────────────────────────────

    public void addTimer(float delta) {
        this.timer += delta;
    }

    public float getTimer() {
        return timer;
    }

    public float getTargetTime() {
        return targetTime;
    }

    /** Đặt lại timer về 0 và set targetTime mới. Gọi khi chuyển sang state mới. */
    public void resetTimer(float targetTime) {
        this.timer = 0f;
        this.targetTime = targetTime;
    }
}
