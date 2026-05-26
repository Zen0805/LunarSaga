package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.ai.PlayerState;
import com.github.zen05.lunarsaga.component.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý vòng đời iFrames sau khi bị đánh:
 *   - Tick đếm ngược Damageable.iFrameTimer mỗi frame.
 *   - Áp dụng hiệu ứng mờ dần alpha (sin wave) vào Graphic.color.
 *   - Khi HP = 0: Enemy → xóa entity; Player → chuyển FSM sang DEAD.
 */
public class DamageSystem extends IteratingSystem {

    private final GdxGame game;
    private final List<Entity> toRemove = new ArrayList<>();

    public DamageSystem(GdxGame game) {
        super(Family.all(Health.class, Damageable.class, Graphic.class).get());
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Damageable damageable = Damageable.MAPPER.get(entity);
        Health     health     = Health.MAPPER.get(entity);
        Graphic    graphic    = Graphic.MAPPER.get(entity);

        // ── 1. Tick iFrames + knockbackTimer ──────────────────────────────────
        damageable.tickInvulnerability(deltaTime);

        // ── 2. Hiệu ứng mờ dần alpha (sin wave) khi đang trong iFrames ────────
        graphic.getColor().a = damageable.getFlashAlpha();

        // ── 3. Kiểm tra chết ─────────────────────────────────────────────────
        if (health.isDead()) {
            boolean isPlayer = Controller.MAPPER.has(entity);
            if (isPlayer) {
                // Đổi trạng thái sang DEAD để PlayerFsmSystem lo đếm ngược
                PlayerFsm fsm = PlayerFsm.MAPPER.get(entity);
                if (fsm != null && !fsm.getStateMachine().isInState(PlayerState.DEAD)) {
                    Gdx.app.log("Combat", "Player đã chết! Khóa phím và đếm ngược...");
                    fsm.getStateMachine().changeState(PlayerState.DEAD);
                }
            } else {
                toRemove.add(entity);
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        Engine engine = getEngine();
        for (Entity entity : toRemove) {
            engine.removeEntity(entity);
        }
        toRemove.clear();
    }
}
