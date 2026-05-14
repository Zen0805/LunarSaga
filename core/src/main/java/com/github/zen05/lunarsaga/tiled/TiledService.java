package com.github.zen05.lunarsaga.tiled;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.asset.MapAsset;

import java.util.function.Consumer;

public class TiledService {

    private final AssetService assetService;
    private final World physicWorld;
    private TiledMap currentMap;

    private Consumer<TiledMap> mapChangeConsumer;
    private Consumer<TiledMapTileMapObject> loadObjectConsumer;

    public TiledService(AssetService assetService, World physicWorld) {
        this.assetService = assetService;
        this.physicWorld = physicWorld;
        this.mapChangeConsumer = null;
        this.loadObjectConsumer = null;
        this.currentMap = null;
    }

    public TiledMap loadMap(MapAsset mapAsset) {
        TiledMap tiledMap = this.assetService.load(mapAsset);
        tiledMap.getProperties().put("mapAsset", mapAsset);
        return tiledMap;
    }

    public void setMap(TiledMap map) {
        if (this.currentMap != null) {
            this.assetService.unload(this.currentMap.getProperties().get("mapAsset", MapAsset.class));
            // Xóa tất cả static body thuộc "environment" (tường, cây cối, biên map)
            Array<Body> bodies = new Array<>();
            physicWorld.getBodies(bodies);
            for (Body body : bodies) {
                if ("environment".equals(body.getUserData())) {
                    physicWorld.destroyBody(body);
                }
            }
        }

        this.currentMap = map;
        loadMapContent(map);

        if (this.mapChangeConsumer != null) {
            this.mapChangeConsumer.accept(map);
        }
    }

    private void loadMapContent(TiledMap tiledMap) {
        for (MapLayer layer : tiledMap.getLayers()) {
            if ("objects".equals(layer.getName())) {
                loadObjectLayer(layer);
            } else if ("collision".equals(layer.getName())) {
                // Tạo Static Body từ các hình vẽ va chạm trong Tiled
                loadCollisionLayer(layer);
            }
        }
        spawnMapBoundary(tiledMap);
    }

    /**
     * Đọc layer "collision" trong Tiled và tạo các Static Body Box2D tương ứng.
     * Mỗi đối tượng trong layer (Rectangle, Polygon...) trở thành "bức tường vô hình".
     * Tham khảo: mystictutorial / TiledService.java#loadTileLayer() + TiledPhysics.java
     */
    private void loadCollisionLayer(MapLayer collisionLayer) {
        Vector2 scaling = new Vector2(1f, 1f);

        for (MapObject mapObject : collisionLayer.getObjects()) {
            // Bỏ qua nếu người dùng vô tình để lọt Tile Object (cái cây) vào layer collision
            if (mapObject instanceof TiledMapTileMapObject) {
                com.badlogic.gdx.Gdx.app.log("TiledService", "Cảnh báo: Đã bỏ qua 1 TiledMapTileMapObject trong layer collision (Layer này chỉ nên chứa Rectangle/Polygon).");
                continue;
            }

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.fixedRotation = true;
            // Static body đặt tại gốc tọa độ (0,0) — tọa độ của fixture sẽ chứa vị trí thật
            bodyDef.position.setZero();

            Body body = physicWorld.createBody(bodyDef);
            body.setUserData("environment");

            FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(mapObject, scaling, Vector2.Zero);
            body.createFixture(fixtureDef);
            fixtureDef.shape.dispose();
        }
    }

    /**
     * Tạo 4 bức tường vô hình bao quanh biên của map để ngăn nhân vật đi ra ngoài.
     * Tham khảo: mystictutorial / TiledService.java#spawnMapBoundary()
     */
    private void spawnMapBoundary(TiledMap tiledMap) {
        int width   = tiledMap.getProperties().get("width",     0, Integer.class);
        int tileW   = tiledMap.getProperties().get("tilewidth", 0, Integer.class);
        int height  = tiledMap.getProperties().get("height",    0, Integer.class);
        int tileH   = tiledMap.getProperties().get("tileheight",0, Integer.class);
        float mapW  = width  * tileW * GdxGame.UNIT_SCALE;
        float mapH  = height * tileH * GdxGame.UNIT_SCALE;
        float halfW = mapW * 0.5f;
        float halfH = mapH * 0.5f;
        float thick = 0.5f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.setZero();
        Body boundary = physicWorld.createBody(bodyDef);
        boundary.setUserData("environment");

        PolygonShape shape = new PolygonShape();

        // Cạnh trái
        shape.setAsBox(thick, halfH, new Vector2(-thick, halfH), 0f);
        boundary.createFixture(shape, 0f);
        // Cạnh phải
        shape.setAsBox(thick, halfH, new Vector2(mapW + thick, halfH), 0f);
        boundary.createFixture(shape, 0f);
        // Cạnh dưới
        shape.setAsBox(halfW, thick, new Vector2(halfW, -thick), 0f);
        boundary.createFixture(shape, 0f);
        // Cạnh trên
        shape.setAsBox(halfW, thick, new Vector2(halfW, mapH + thick), 0f);
        boundary.createFixture(shape, 0f);

        shape.dispose();
    }

    private void loadObjectLayer(MapLayer objectLayer) {
        if (loadObjectConsumer == null) return;

        for (MapObject mapObject : objectLayer.getObjects()) {
            if (mapObject instanceof TiledMapTileMapObject tileMapObject) {
                loadObjectConsumer.accept(tileMapObject);
            } else {
                throw new GdxRuntimeException("Unsupported Object: " + mapObject.getClass().getSimpleName());
            }
        }
    }

    public void setMapChangeConsumer(Consumer<TiledMap> mapChangeConsumer) {
        this.mapChangeConsumer = mapChangeConsumer;
    }

    public void setLoadObjectConsumer(Consumer<TiledMapTileMapObject> loadObjectConsumer) {
        this.loadObjectConsumer = loadObjectConsumer;
    }

}
