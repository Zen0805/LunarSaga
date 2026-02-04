package com.github.zen05.lunarsaga.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.component.Graphic;
import com.github.zen05.lunarsaga.component.Transform;

public class TiledAshleyConfigurator {

    private final Engine engine;
    private final AssetService assetService;

    public TiledAshleyConfigurator(Engine engine, AssetService assetService) {

        this.engine = engine;
        this.assetService = assetService;

    }

    public void onLoadObject(TiledMapTileMapObject tileMapObject){

        Entity entity = this.engine.createEntity();
        TiledMapTile tile = tileMapObject.getTile();
        TextureRegion textureRegion =  getTextureRegion(tile);
        int z = tile.getProperties().get("z", 1, Integer.class);

        entity.add(new Graphic(textureRegion, Color.WHITE.cpy()));
        addEntityTransform(
            tileMapObject.getX(), tileMapObject.getY(), z,
            textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
            tileMapObject.getScaleX(), tileMapObject.getScaleY(),
            entity
        );

        this.engine.addEntity(entity);

    }

    private void addEntityTransform(
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

        entity.add(new Transform(position, z, size, scaling, 0f));

    }

    private TextureRegion getTextureRegion(TiledMapTile tile){

        return tile.getTextureRegion();

    }

}
