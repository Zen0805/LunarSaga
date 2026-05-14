package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.asset.AtlasAsset;
import com.github.zen05.lunarsaga.component.Controller;
import com.github.zen05.lunarsaga.component.Facing;
import com.github.zen05.lunarsaga.component.Graphic;
import com.github.zen05.lunarsaga.component.Transform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RenderSystem extends SortedIteratingSystem implements Disposable {

    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Batch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final List<MapLayer> forceGroundLayers;
    private final List<MapLayer> backGroundLayers;
    private final TextureRegion bowRegion;

    // Offset của cây cung tính từ tâm nhân vật (đơn vị thế giới)
    private static final float BOW_OFFSET = 0.25f;

    public RenderSystem(Batch batch, Viewport viewport, OrthographicCamera camera, AssetService assetService) {

        super(

                Family.all(Transform.class, Graphic.class).get(),
                Comparator.comparing(Transform.MAPPER::get));

        this.batch = batch;
        this.viewport = viewport;
        this.camera = camera;
        this.mapRenderer = new OrthogonalTiledMapRenderer(null, GdxGame.UNIT_SCALE, this.batch);
        this.forceGroundLayers = new ArrayList<>();
        this.backGroundLayers = new ArrayList<>();

        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        this.bowRegion = atlas.findRegion("weapons/bow");

    }

    @Override
    public void update(float deltaTime) {

        AnimatedTiledMapTile.updateAnimationBaseTime();
        this.viewport.apply();

        batch.begin();
        this.batch.setColor(Color.WHITE);
        this.mapRenderer.setView(this.camera);
        backGroundLayers.forEach(mapRenderer::renderMapLayer);

        forceSort();
        super.update(deltaTime);

        this.batch.setColor(Color.WHITE);
        forceGroundLayers.forEach(mapRenderer::renderMapLayer);

        batch.end();

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        Transform transform = Transform.MAPPER.get(entity);
        Graphic graphic = Graphic.MAPPER.get(entity);

        if (graphic.getRegion() == null) {
            return;
        }

        Vector2 position = transform.getPosition();
        Vector2 scaling = transform.getScaling();
        Vector2 size = transform.getSize();
        
        float renderScaleX = scaling.x;
        Facing facing = Facing.MAPPER.get(entity);
        if (facing != null && facing.isFlipX()) {
            renderScaleX = -scaling.x;
        }

        this.batch.setColor(graphic.getColor());
        this.batch.draw(
                graphic.getRegion(),
                position.x - (1f - scaling.x) * size.x * 0.5f,
                position.y - (1f - scaling.y) * size.y * 0.5f,
                size.x * 0.5f, size.y * 0.5f,
                size.x, size.y,
                renderScaleX, scaling.y,
                transform.getRotationDegrees());

        Controller controller = Controller.MAPPER.get(entity);
        if (controller != null && controller.isAiming() && bowRegion != null) {
            drawBow(position, size, controller.getAimTarget());
        }

    }

    private void drawBow(Vector2 entityPos, Vector2 entitySize, Vector2 aimTarget) {
        float cx = entityPos.x + entitySize.x * 0.5f;
        float cy = entityPos.y + entitySize.y * 0.5f;

        // Vector hướng từ nhân vật đến chuột
        float dx = aimTarget.x - cx;
        float dy = aimTarget.y - cy;
        float angleRad = MathUtils.atan2(dy, dx);
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        // Vị trí đặt cây cung: offset về phía chuột
        float bowX = cx + MathUtils.cos(angleRad) * BOW_OFFSET;
        float bowY = cy + MathUtils.sin(angleRad) * BOW_OFFSET;

        // Kích thước cây cung theo đơn vị thế giới
        float bowW = bowRegion.getRegionWidth() * GdxGame.UNIT_SCALE;
        float bowH = bowRegion.getRegionHeight() * GdxGame.UNIT_SCALE;

        this.batch.setColor(Color.WHITE);
        this.batch.draw(
                bowRegion,
                bowX - bowW * 0.5f, // x (góc trái dưới, sau khi trừ origin)
                bowY - bowH * 0.5f, // y
                bowW * 0.5f, // originX
                bowH * 0.5f, // originY
                bowW, bowH, // width, height
                1f, 1f, // scaleX, scaleY
                angleDeg // rotation
        );
    }

    public void setMap(TiledMap tiledMap) {

        this.mapRenderer.setMap(tiledMap);

        this.forceGroundLayers.clear();
        this.backGroundLayers.clear();
        List<MapLayer> currentLayer = backGroundLayers;
        for (MapLayer layer : tiledMap.getLayers()) {

            if ("objects".equals(layer.getName())) {
                currentLayer = forceGroundLayers;
                continue;
            }

            if (layer.getClass().equals(MapLayer.class)) {

                continue;

            }

            currentLayer.add(layer);

        }

    }

    @Override
    public void dispose() {

        this.mapRenderer.dispose();

    }

}
