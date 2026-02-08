package com.github.zen05.lunarsaga.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.asset.AtlasAsset;

public class LoadingScreen extends ScreenAdapter {

    private final GdxGame game;
    private final AssetService assetService;

    public LoadingScreen(GdxGame game, AssetService assetService) {

        this.game = game;
        this.assetService = assetService;

    }

    @Override
    public void show() {

        for (AtlasAsset atlas : AtlasAsset.values()) {
            assetService.queue(atlas);
        }

    }

    @Override
    public void render(float delta) {
        if (this.assetService.update()) {

            Gdx.app.debug("Loading Screen", "Finished asset loading");
            createScreens();
            this.game.removeScreen(this);
            this.dispose();
            this.game.setScreen(GameScreen.class);

        }
    }

    private void createScreens() {
        this.game.addScreen(new GameScreen(this.game));
    }
}
