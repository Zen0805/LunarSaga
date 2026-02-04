package com.github.zen05.lunarsaga;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.asset.MapAsset;
import com.github.zen05.lunarsaga.system.RenderSystem;
import com.github.zen05.lunarsaga.tiled.TiledAshleyConfigurator;
import com.github.zen05.lunarsaga.tiled.TiledService;

import java.util.function.Consumer;

public class GameScreen extends ScreenAdapter {

    private final GdxGame game;
    private final Batch batch;
    private final AssetService assetService;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final Engine engine;
    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;

    public GameScreen(GdxGame game){

        this.game = game;
        this.batch = game.getBatch();
        this.assetService = game.getAssetService();
        this.viewport = game.getViewport();
        this.camera = game.getCamera();
        this.tiledService = new TiledService(this.assetService);
        this.engine = new Engine();
        this.tiledAshleyConfigurator= new TiledAshleyConfigurator(this.engine, this.assetService);

        this.engine.addSystem(new RenderSystem(this.batch, this.viewport, this.camera));

    }

    @Override
    public void show() {

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        this.tiledService.setMapChangeConsumer(renderConsumer);
        this.tiledService.setLoadObjectConsumer(this.tiledAshleyConfigurator::onLoadObject);

        TiledMap tiledMap = this.tiledService.loadMap(MapAsset.MAIN);
        this.tiledService.setMap(tiledMap);

    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
    }

    @Override
    public void render(float delta) {

        delta = Math.min(delta, 1 / 60f);
        this.engine.update(delta);

    }

    @Override
    public void dispose() {

        for(EntitySystem system : this.engine.getSystems()){

            if(system instanceof Disposable){

                Disposable disposableSystem = (Disposable) system;

                disposableSystem.dispose();

            }

        }

    }

}
