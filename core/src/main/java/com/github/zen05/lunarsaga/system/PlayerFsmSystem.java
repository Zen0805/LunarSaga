package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.ai.PlayerState;
import com.github.zen05.lunarsaga.component.PlayerFsm;
import com.github.zen05.lunarsaga.screen.GameOverScreen;

/**
 * System xử lý FSM Logic của Player.
 * Tác dụng: Cập nhật trạng thái logic, đặc biệt xử lý thời gian đếm ngược
 * khi chết trước khi gọi màn hình Game Over.
 */
public class PlayerFsmSystem extends IteratingSystem {

    private static final float DEATH_DELAY = 1.5f; // Đợi 1.5 giây sau khi hết máu

    private final GdxGame game;
    private float deadTimer = 0f;

    public PlayerFsmSystem(GdxGame game) {
        super(Family.all(PlayerFsm.class).get());
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerFsm playerFsm = PlayerFsm.MAPPER.get(entity);

        // Tick state (gọi hàm update của state hiện tại)
        playerFsm.getStateMachine().update();

        // Xử lý đếm ngược nếu đang trong trạng thái DEAD
        if (playerFsm.getStateMachine().isInState(PlayerState.DEAD)) {
            deadTimer += deltaTime;

            if (deadTimer >= DEATH_DELAY) {
                // Hết thời gian chờ → Chuyển Game Over
                Gdx.app.log("PlayerFsmSystem", "Death delay completed. Transitioning to Game Over.");
                game.setScreen(GameOverScreen.class);
                
                // Tránh gọi setScreen liên tục trong các frame tiếp theo
                deadTimer = -9999f; 
            }
        } else {
            // Đảm bảo reset timer nếu sống lại
            deadTimer = 0f;
        }
    }
}
