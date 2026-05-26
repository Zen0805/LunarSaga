package com.github.zen05.lunarsaga.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.github.zen05.lunarsaga.GdxGame;

/**
 * Màn hình Game Over hiển thị khi Player chết.
 * Dùng BitmapFont built-in của libGDX (không cần asset font riêng).
 */
public class GameOverScreen extends ScreenAdapter {

    private static final float FADE_DURATION = 1.0f;

    private final GdxGame game;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch  hudBatch;
    private final OrthographicCamera camera;

    // Font built-in của libGDX — không cần asset, có sẵn luôn
    private final BitmapFont   titleFont;
    private final BitmapFont   hintFont;
    private final GlyphLayout  layout;

    private float fadeTimer   = 0f;
    private boolean readyForInput = false;

    public GameOverScreen(GdxGame game) {
        this.game          = game;
        this.shapeRenderer = new ShapeRenderer();
        this.hudBatch      = new SpriteBatch();
        this.camera        = new OrthographicCamera();
        this.layout        = new GlyphLayout();

        // Font tiêu đề "GAME OVER" — scale lên 4x
        this.titleFont = new BitmapFont(); // Arial 15pt built-in
        this.titleFont.getData().setScale(4f);
        this.titleFont.setColor(Color.WHITE);

        // Font gợi ý bên dưới — scale 1.5x
        this.hintFont = new BitmapFont();
        this.hintFont.getData().setScale(1.5f);
        this.hintFont.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
    }

    @Override
    public void show() {
        fadeTimer     = 0f;
        readyForInput = false;
        Gdx.app.log("GameOverScreen", "Game Over! Nhấn phím bất kỳ để chơi lại.");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fadeTimer = Math.min(fadeTimer + delta, FADE_DURATION);
        float alpha = fadeTimer / FADE_DURATION; // 0.0 → 1.0

        if (!readyForInput && alpha >= 1f) {
            readyForInput = true;
        }

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera.setToOrtho(false, w, h);
        camera.update();

        // ── Vẽ nền đỏ tối mờ dần ───────────────────────────────────────────
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.35f, 0f, 0f, alpha * 0.92f);
        shapeRenderer.rect(0, 0, w, h);
        shapeRenderer.end();

        // ── Vẽ chữ bằng BitmapFont + SpriteBatch ────────────────────────────
        // Cần enable blending để font có alpha mờ dần theo fade
        hudBatch.enableBlending();
        hudBatch.setProjectionMatrix(camera.combined);
        hudBatch.begin();

        // Chữ "GAME OVER" — căn giữa màn hình, hơi cao hơn giữa
        titleFont.setColor(1f, 0.2f, 0.2f, alpha);
        layout.setText(titleFont, "GAME OVER");
        titleFont.draw(hudBatch, layout,
                (w - layout.width) / 2f,
                h * 0.60f + layout.height);

        // Chữ gợi ý — nhấp nháy sau khi fade xong
        if (readyForInput) {
            float blinkAlpha = (MathUtils.sin(fadeTimer * 4f) + 1f) * 0.5f * alpha;
            hintFont.setColor(0.9f, 0.9f, 0.9f, blinkAlpha);
            layout.setText(hintFont, "Press any key to retry");
            hintFont.draw(hudBatch, layout,
                    (w - layout.width) / 2f,
                    h * 0.38f);
        }

        hudBatch.end();

        // ── Nhận phím sau khi sẵn sàng ──────────────────────────────────────
        if (readyForInput && Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            restartGame();
        }
        if (readyForInput && Gdx.input.justTouched()) {
            restartGame();
        }
    }

    private void restartGame() {
        game.removeScreen(GameScreen.class);
        GameScreen freshGame = new GameScreen(game);
        game.addScreen(freshGame);
        game.setScreen(GameScreen.class);
        Gdx.app.log("GameOverScreen", "Restart!");
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudBatch.dispose();
        titleFont.dispose();
        hintFont.dispose();
    }
}
