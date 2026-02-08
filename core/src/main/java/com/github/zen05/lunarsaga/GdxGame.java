package com.github.zen05.lunarsaga;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.screen.LoadingScreen;

import java.util.HashMap;
import java.util.Map;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GdxGame extends Game {

    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f;
    public static final float UNIT_SCALE = 1f / 16f;

    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AssetService assetService;
    private GLProfiler glProfiler;
    private FPSLogger fpsLogger;
    private InputMultiplexer inputMultiplexer;

    private final Map<Class<? extends Screen>, Screen> screenCache = new HashMap<>();

    @Override
    public void create() {

        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        this.inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);

        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, this.camera);
        this.assetService = new AssetService(new InternalFileHandleResolver());

        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();

        this.fpsLogger = new FPSLogger();

        addScreen(new LoadingScreen(this, assetService));
        setScreen(LoadingScreen.class);

    }

    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true);
        super.resize(width, height);

    }

    public void addScreen(Screen screen) {

        screenCache.put(screen.getClass(), screen);

    }

    public void removeScreen(Screen screen) {

        screenCache.remove(screen.getClass());

    }

    public void setScreen(Class<? extends Screen> screenClass) {

        Screen screen =  screenCache.get(screenClass);
        if(screen == null){
            throw new GdxRuntimeException("No screen with class " + screenClass + " found in the screen cache");
        }
        super.setScreen(screen);

    }

    @Override
    public void render() {

        glProfiler.reset();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();

        Gdx.graphics.setTitle("LunarSaga - Draw Calls: " + glProfiler.getDrawCalls());
        fpsLogger.log();

    }

    @Override
    public void dispose() {

        screenCache.values().forEach(Screen::dispose);
        screenCache.clear();

        this.batch.dispose();
        this.assetService.debugDiagnostics();
        this.assetService.dispose();
    }

    public Batch getBatch() {

        return this.batch;

    }

    public OrthographicCamera getCamera() {

        return this.camera;

    }

    public Viewport getViewport() {

        return this.viewport;

    }

    public AssetService getAssetService() {

        return this.assetService;

    }

    public void setInputProcessor(InputProcessor... processors) {

        inputMultiplexer.clear();
        if (inputMultiplexer == null) return;

        for (InputProcessor processor : processors) {

            inputMultiplexer.addProcessor(processor);

        }

    }

}
