package com.github.zen05.lunarsaga.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.asset.AtlasAsset;
import com.github.zen05.lunarsaga.component.*;
import com.github.zen05.lunarsaga.component.Animation2D.AnimationType;
import com.github.zen05.lunarsaga.component.Facing.FacingDirection;

public class TiledAshleyConfigurator {

    private final Engine engine;
    private final AssetService assetService;
    private final World physicWorld;

    public TiledAshleyConfigurator(Engine engine, AssetService assetService, World physicWorld) {
        this.engine = engine;
        this.assetService = assetService;
        this.physicWorld = physicWorld;
    }

    public void onLoadObject(TiledMapTileMapObject tileMapObject) {
        Entity entity = this.engine.createEntity();
        TiledMapTile tile = tileMapObject.getTile();
        TextureRegion textureRegion = getTextureRegion(tile);
        int z = tile.getProperties().get("z", 1, Integer.class);

        entity.add(new Graphic(textureRegion, Color.WHITE.cpy()));
        Transform transform = addEntityTransform(
                tileMapObject.getX(), tileMapObject.getY(), z,
                textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
                tileMapObject.getScaleX(), tileMapObject.getScaleY(),
                entity
        );
        addEntityPhysic(tile.getObjects(), transform, entity);
        addEntityController(tileMapObject, entity);
        addEntityMove(tile, entity);
        addEntityAnimation(tile, entity);
        entity.add(new Facing(FacingDirection.DOWN));
        entity.add(new Fsm(entity));

        this.engine.addEntity(entity);
    }

    /**
     * Tạo Box2D Dynamic Body cho entity từ hình học va chạm trong Tile.
     * Nếu tile không có collision object nào, không gắn Physic component.
     * Tham khảo: mystictutorial / TiledAshleyConfigurator.java#addEntityPhysic()
     */
    private void addEntityPhysic(MapObjects mapObjects, Transform transform, Entity entity) {
        BodyDef bodyDef = new BodyDef();
        // Nếu là Player hoặc có Move component thì thường là DynamicBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(transform.getPosition());
        bodyDef.fixedRotation = true;

        Body body = physicWorld.createBody(bodyDef);
        body.setUserData(entity);

        Vector2 scaling = transform.getScaling();

        if (mapObjects.getCount() > 0) {
            // Nếu trong file TSX có vẽ hình va chạm, lấy hình đó
            for (var mapObject : mapObjects) {
                FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(mapObject, scaling, Vector2.Zero);
                body.createFixture(fixtureDef).setUserData(mapObject.getName());
                fixtureDef.shape.dispose();
            }
        } else {
            // Nếu không vẽ gì trong TSX, tạo một khối vuông mặc định vừa bằng kích thước nhân vật
            com.badlogic.gdx.physics.box2d.PolygonShape defaultShape = new com.badlogic.gdx.physics.box2d.PolygonShape();
            float halfW = transform.getSize().x * 0.5f;
            float halfH = transform.getSize().y * 0.5f;
            // Box2D setAsBox lấy "nửa chiều rộng, nửa chiều cao" và tâm (center) của hình
            defaultShape.setAsBox(halfW, halfH, new Vector2(halfW, halfH), 0f);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = defaultShape;
            body.createFixture(fixtureDef);
            defaultShape.dispose();
        }

        entity.add(new Physic(body));
    }

    private void addEntityAnimation(TiledMapTile tile, Entity entity) {
        String animationStr = tile.getProperties().get("animation", "", String.class);
        if (animationStr.isBlank()) return;

        AnimationType animationType = AnimationType.valueOf(animationStr);
        String atlasAssetStr = tile.getProperties().get("atlasAsset", "OBJECTS", String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        float speed = tile.getProperties().get("animationSpeed", 0f, Float.class);
        entity.add(new Animation2D(atlasAsset, atlasKey, animationType, PlayMode.LOOP, speed));
    }

    private void addEntityMove(TiledMapTile tile, Entity entity) {
        float speed = tile.getProperties().get("speed", 0f, Float.class);
        if (speed == 0f) return;
        entity.add(new Move(speed));
    }

    private void addEntityController(TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean controller = tileMapObject.getProperties().get("controller", false, Boolean.class);
        if (!controller) return;
        entity.add(new Controller());
    }

    private Transform addEntityTransform(
            float x, float y, int z,
            int w, int h,
            float scaleX, float scaleY,
            Entity entity
    ) {
        Vector2 position = new Vector2(x, y);
        Vector2 size = new Vector2(w, h);
        Vector2 scaling = new Vector2(scaleX, scaleY);

        position.scl(GdxGame.UNIT_SCALE);
        size.scl(GdxGame.UNIT_SCALE);

        Transform transform = new Transform(position, z, size, scaling, 0f);
        entity.add(transform);
        return transform;
    }

    private TextureRegion getTextureRegion(TiledMapTile tile) {
        String atlasAssetStr = tile.getProperties().get("atlasAsset", AtlasAsset.OBJECTS.name(), String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        TextureAtlas textureAtlas = this.assetService.get(atlasAsset);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        TextureRegion region = textureAtlas.findRegion(atlasKey + "/" + atlasKey);
        if (region != null) {
            return region;
        }
        return tile.getTextureRegion();
    }

}
