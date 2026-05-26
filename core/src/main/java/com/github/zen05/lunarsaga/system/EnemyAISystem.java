package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.zen05.lunarsaga.ai.EnemyState;
import com.github.zen05.lunarsaga.component.Enemy;
import com.github.zen05.lunarsaga.component.EnemyFsm;
import com.github.zen05.lunarsaga.component.Move;
import com.github.zen05.lunarsaga.component.Physic;
import com.github.zen05.lunarsaga.component.Player;
import com.github.zen05.lunarsaga.component.Transform;

/**
 * Hệ thống AI kẻ địch theo phong cách Legend of Lua — dùng FSM + tính khoảng cách thuần túy.
 *
 * Bước 3.2: Vòng lặp IDLE ↔ WANDER (đứng yên → bay lung tung → lại đứng yên).
 * Bước 3.3: Thêm CHASE với hệ thống 3 bán kính:
 *   - AggroRange    : khoảng cách Player → kích hoạt CHASE.
 *   - De-AggroRange : khoảng cách Player → bỏ CHASE về IDLE.
 *   - LeashRange    : khoảng cách Spawn → bỏ CHASE về IDLE (dây xích).
 *
 * Vào WANDER: nếu quái quá xa Spawn (> wanderRadius), hướng di chuyển = về Spawn.
 */
public class EnemyAISystem extends IteratingSystem {

    // ─── Hằng số thời gian ────────────────────────────────────────────────────
    private static final float IDLE_MIN   = 1.5f;
    private static final float IDLE_MAX   = 3.0f;
    private static final float WANDER_MIN = 1.0f;
    private static final float WANDER_MAX = 2.5f;

    // Tạm dùng để tìm Player trong Engine (không giữ hard reference Entity)
    private final Family playerFamily = Family.all(Player.class, Transform.class).get();

    public EnemyAISystem() {
        super(Family.all(Enemy.class, EnemyFsm.class, Move.class, Physic.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        EnemyFsm enemyFsm = EnemyFsm.MAPPER.get(entity);
        Enemy enemy        = Enemy.MAPPER.get(entity);
        EnemyState state   = enemyFsm.getStateMachine().getCurrentState();

        enemy.addTimer(deltaTime);
        enemy.tickAlert(deltaTime); // Đếm ngược alert mỗi frame

        // Lấy vị trí hiện tại của quái
        Vector2 enemyPos = Physic.MAPPER.get(entity).getBody().getPosition();

        // Lấy vị trí Player (nếu có)
        Vector2 playerPos = getPlayerPosition(getEngine());

        switch (state) {
            case IDLE   -> handleIdle(entity, enemy, enemyFsm, enemyPos, playerPos);
            case WANDER -> handleWander(entity, enemy, enemyFsm, enemyPos, playerPos);
            case CHASE  -> handleChase(entity, enemy, enemyFsm, enemyPos, playerPos);
        }
    }

    // ─── IDLE ─────────────────────────────────────────────────────────────────

    private void handleIdle(Entity entity, Enemy enemy, EnemyFsm enemyFsm,
                            Vector2 enemyPos, Vector2 playerPos) {
        // Chỉ cho phép CHASE nếu enemy đang trong vùng an toàn quanh Spawn.
        // Nếu enemy vừa lết về từ leash, không để nó lập tức lao ra đuổi lại.
        boolean canAggro = dst(enemyPos, enemy.getSpawnPoint()) <= enemy.getWanderRadius() + 1f;

        if (canAggro && playerPos != null && dst(enemyPos, playerPos) <= enemy.getAggroRange()) {
            startChase(entity, enemy, enemyFsm);
            return;
        }
        // Hết giờ đứng im → WANDER (hàm startWander tự quyết định hướng bay về nhà hay random)
        if (enemy.getTimer() >= enemy.getTargetTime()) {
            startWander(entity, enemy, enemyFsm, enemyPos);
        }
    }

    // ─── WANDER ───────────────────────────────────────────────────────────────

    private void handleWander(Entity entity, Enemy enemy, EnemyFsm enemyFsm,
                              Vector2 enemyPos, Vector2 playerPos) {
        // Tương tự IDLE: chỉ aggro nếu đang trong vùng an toàn.
        boolean canAggro = dst(enemyPos, enemy.getSpawnPoint()) <= enemy.getWanderRadius() + 1f;

        if (canAggro && playerPos != null && dst(enemyPos, playerPos) <= enemy.getAggroRange()) {
            startChase(entity, enemy, enemyFsm);
            return;
        }
        // Hết giờ bay → về IDLE
        if (enemy.getTimer() >= enemy.getTargetTime()) {
            startIdle(entity, enemy, enemyFsm);
        }
    }

    // ─── CHASE ────────────────────────────────────────────────────────────────

    private void handleChase(Entity entity, Enemy enemy, EnemyFsm enemyFsm,
                             Vector2 enemyPos, Vector2 playerPos) {
        // Kiểm tra LeashRange: quái bị dụ ra quá xa Spawn → về WANDER, bay về nhà.
        // Dùng WANDER (không phải IDLE) để hàm startWander tự động ép hướng về Spawn.
        // Nếu dùng IDLE, frame tiếp theo player vẫn trong aggroRange → lập tức CHASE lại
        // → loop mỗi frame → IDLE.enter() liên tục → animation reset → đóng băng.
        if (dst(enemyPos, enemy.getSpawnPoint()) > enemy.getLeashRange()) {
            startWander(entity, enemy, enemyFsm, enemyPos);
            return;
        }

        // Kiểm tra De-AggroRange: Player chạy thoát quá xa → mất dấu, về IDLE
        if (playerPos == null || dst(enemyPos, playerPos) > enemy.getDeAggroRange()) {
            startIdle(entity, enemy, enemyFsm);
            return;
        }

        // Đang trong tầm: cập nhật hướng di chuyển về phía Player mỗi frame
        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.getDirection()
                .set(playerPos.x - enemyPos.x, playerPos.y - enemyPos.y)
                .nor();
        }
    }


    // ─── Chuyển state ─────────────────────────────────────────────────────────

    private void startIdle(Entity entity, Enemy enemy, EnemyFsm enemyFsm) {
        enemy.resetTimer(MathUtils.random(IDLE_MIN, IDLE_MAX));
        Move move = Move.MAPPER.get(entity);
        if (move != null) move.getDirection().setZero();
        enemyFsm.getStateMachine().changeState(EnemyState.IDLE);
    }

    private void startWander(Entity entity, Enemy enemy, EnemyFsm enemyFsm, Vector2 enemyPos) {
        enemy.resetTimer(MathUtils.random(WANDER_MIN, WANDER_MAX));

        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            float distFromSpawn = dst(enemyPos, enemy.getSpawnPoint());

            if (distFromSpawn > enemy.getWanderRadius()) {
                // "Dây xích WANDER": quá xa tổ → ép hướng về Spawn
                move.getDirection()
                    .set(enemy.getSpawnPoint().x - enemyPos.x,
                         enemy.getSpawnPoint().y - enemyPos.y)
                    .nor();
            } else {
                // Trong phạm vi → random góc bay tự do 360°
                float angle = MathUtils.random(0f, MathUtils.PI2);
                move.getDirection().set(MathUtils.cos(angle), MathUtils.sin(angle));
            }
        }

        enemyFsm.getStateMachine().changeState(EnemyState.WANDER);
    }

    private void startChase(Entity entity, Enemy enemy, EnemyFsm enemyFsm) {
        // Kích hoạt "!" trên đầu quái: hiện 0.8 giây rồi tự tắt
        enemy.startAlert();
        // Reset timer (không dùng cho CHASE, nhưng giữ sạch cho state tiếp theo)
        enemy.resetTimer(0f);
        enemyFsm.getStateMachine().changeState(EnemyState.CHASE);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Khoảng cách Euclidean giữa 2 điểm. */
    private float dst(Vector2 a, Vector2 b) {
        return a.dst(b);
    }

    /** Tìm vị trí Player hiện tại trong Engine. Trả về null nếu không có Player. */
    private Vector2 getPlayerPosition(Engine engine) {
        if (engine == null) return null;
        var players = engine.getEntitiesFor(playerFamily);
        if (players.size() == 0) return null;
        return Physic.MAPPER.get(players.first()).getBody().getPosition();
    }
}
