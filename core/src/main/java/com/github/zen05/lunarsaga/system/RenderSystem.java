package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.zen05.lunarsaga.GdxGame;
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

    public RenderSystem(Batch batch, Viewport viewport, OrthographicCamera camera) {

        super(

            Family.all(Transform.class, Graphic.class).get(),
            Comparator.comparing(Transform.MAPPER::get)
        );

        this.batch = batch;
        this.viewport = viewport;
        this.camera = camera;
        this.mapRenderer = new OrthogonalTiledMapRenderer(null, GdxGame.UNIT_SCALE, this.batch);
        this.forceGroundLayers = new ArrayList<>();
        this.backGroundLayers = new ArrayList<>();

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
        this.batch.setColor(graphic.getColor());
        this.batch.draw(
            graphic.getRegion(),
            position.x - (1f - scaling.x) * size.x * 0.5f,
            position.y - (1f - scaling.y) * size.y * 0.5f,
            size.x * 0.5f, size.y * 0.5f,
            size.x, size.y,
            scaling.x, scaling.y,
            transform.getRotationDegrees()
        );

    }

    public void setMap(TiledMap tiledMap) {

        this.mapRenderer.setMap(tiledMap);

        this.forceGroundLayers.clear();
        this.backGroundLayers.clear();
        List<MapLayer> currentLayer = backGroundLayers;
        for (MapLayer layer : tiledMap.getLayers()) {

            if ("objects".equals(layer.getName())){
                currentLayer = forceGroundLayers;
                continue;
            }

            if (layer.getClass().equals(MapLayer.class)){

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
