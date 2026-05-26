package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.zen05.lunarsaga.component.Controller;
import com.github.zen05.lunarsaga.component.Health;

/**
 * Vẽ HUD (Heads-Up Display) lên màn hình bằng ShapeRenderer.
 * Hiển thị thanh máu (HP Bar) của Player ở góc trên bên trái.
 *
 * Vẽ RIÊNG bằng Camera HUD (không phụ thuộc camera game) để
 * thanh máu luôn cố định góc màn hình dù Camera đang di chuyển theo Player.
 */
public class HudSystem extends EntitySystem implements Disposable {

    // ─── Thông số UI (pixel màn hình) ────────────────────────────────────────
    private static final float BAR_X        = 12f;   // Cách mép trái
    private static final float BAR_Y_OFFSET = 12f;   // Cách mép trên (tính từ trên xuống)
    private static final float BAR_WIDTH    = 80f;   // Chiều rộng thanh max
    private static final float BAR_HEIGHT   = 10f;   // Chiều cao thanh
    private static final float BAR_PADDING  = 2f;    // Viền

    private static final Color COLOR_BG     = new Color(0.15f, 0.15f, 0.15f, 0.8f); // Nền tối
    private static final Color COLOR_HP     = new Color(0.85f, 0.18f, 0.18f, 1f);   // Đỏ máu
    private static final Color COLOR_BORDER = new Color(0.8f,  0.8f,  0.8f,  1f);   // Viền trắng

    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera hudCamera; // Camera cố định (không theo Player)
    private final Viewport viewport;

    private ImmutableArray<Entity> playerEntities;

    public HudSystem(Viewport viewport) {
        super(Integer.MAX_VALUE); // Ưu tiên cao nhất — vẽ sau tất cả hệ thống khác
        this.viewport       = viewport;
        this.shapeRenderer  = new ShapeRenderer();
        this.hudCamera      = new OrthographicCamera();
    }

    @Override
    public void addedToEngine(Engine engine) {
        playerEntities = engine.getEntitiesFor(
                Family.all(Controller.class, Health.class).get());
    }

    @Override
    public void update(float deltaTime) {
        if (playerEntities == null || playerEntities.size() == 0) return;

        Entity player = playerEntities.first();
        Health health = Health.MAPPER.get(player);
        if (health == null) return;

        // Cập nhật HUD Camera theo kích thước màn hình thực (pixel)
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        float screenH = Gdx.graphics.getHeight();
        float barX    = BAR_X;
        float barY    = screenH - BAR_Y_OFFSET - BAR_HEIGHT; // Góc trên trái

        float hpRatio    = (float) health.getCurrentHp() / health.getMaxHp();
        float filledWidth = BAR_WIDTH * hpRatio;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 1. Nền tối của thanh
        shapeRenderer.setColor(COLOR_BG);
        shapeRenderer.rect(barX - BAR_PADDING, barY - BAR_PADDING,
                BAR_WIDTH + BAR_PADDING * 2, BAR_HEIGHT + BAR_PADDING * 2);

        // 2. Phần đỏ theo tỉ lệ HP
        if (filledWidth > 0) {
            shapeRenderer.setColor(lerpHpColor(hpRatio));
            shapeRenderer.rect(barX, barY, filledWidth, BAR_HEIGHT);
        }

        shapeRenderer.end();

        // 3. Viền trắng (vẽ riêng với ShapeType.Line)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COLOR_BORDER);
        shapeRenderer.rect(barX - BAR_PADDING, barY - BAR_PADDING,
                BAR_WIDTH + BAR_PADDING * 2, BAR_HEIGHT + BAR_PADDING * 2);
        shapeRenderer.end();
    }

    /**
     * Màu thanh máu thay đổi theo HP:
     * HP đầy → Đỏ tươi | HP thấp (< 30%) → Vàng cam cảnh báo
     */
    private Color lerpHpColor(float ratio) {
        if (ratio > 0.5f) return COLOR_HP;
        // Dưới 50%: trộn dần từ đỏ → cam → vàng
        Color warn = new Color(1f, 0.65f, 0f, 1f); // Cam vàng
        return new Color(COLOR_HP).lerp(warn, 1f - (ratio * 2f));
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
