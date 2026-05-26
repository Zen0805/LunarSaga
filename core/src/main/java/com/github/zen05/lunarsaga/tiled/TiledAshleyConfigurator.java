package com.github.zen05.lunarsaga.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
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
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.ai.EnemyDef;
import com.github.zen05.lunarsaga.ai.EnemyState;

import com.github.zen05.lunarsaga.asset.AssetService;
import com.github.zen05.lunarsaga.asset.AtlasAsset;
import com.github.zen05.lunarsaga.asset.MapAsset;
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

    public void onLoadObject(com.badlogic.gdx.maps.MapObject mapObject) {
        if (mapObject instanceof TiledMapTileMapObject tileMapObject) {
            onLoadTileObject(tileMapObject);
        } else {
            String type = mapObject.getProperties().get("type", String.class);
            if ("portal".equals(type)) {
                createPortal(mapObject);
            } else if ("enemy".equals(type)) {
                createEnemy(mapObject);
            }
        }
    }

    private void createPortal(com.badlogic.gdx.maps.MapObject mapObject) {
        Entity entity = this.engine.createEntity();

        String toMapStr = mapObject.getProperties().get("toMap", String.class);
        MapAsset toMap = MapAsset.valueOf(toMapStr);
        int toX = mapObject.getProperties().get("toX", 0, Integer.class);
        int toY = mapObject.getProperties().get("toY", 0, Integer.class);

        entity.add(new Portal(toMap, toX, toY));

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.setZero();

        Body body = physicWorld.createBody(bodyDef);
        body.setUserData(entity);

        FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(mapObject, new Vector2(1f, 1f), Vector2.Zero);
        fixtureDef.isSensor = true; // Là cổng thì phải đi xuyên qua được

        body.createFixture(fixtureDef);
        fixtureDef.shape.dispose();

        entity.add(new Physic(body));
        this.engine.addEntity(entity);
    }

    /**
     * Sinh ra Entity kẻ địch từ một Point object trong Tiled.
     * Đọc các thuộc tính: type, enemyId, ai (trạng thái ban đầu).
     * Entity sẽ có: Enemy (tag), Transform, Physic (Dynamic Body hình tròn),
     * Move, Animation2D, Facing, Fsm (EnemyState).
     */
    private void createEnemy(com.badlogic.gdx.maps.MapObject mapObject) {
        String enemyId = mapObject.getProperties().get("enemyId", "bat", String.class);
        String aiStr   = mapObject.getProperties().get("ai", "idle", String.class);
        float  x       = mapObject.getProperties().get("x", 0f, Float.class);
        float  y       = mapObject.getProperties().get("y", 0f, Float.class);

        // ── Bước 1: Tra từ điển EnemyDef để lấy thông số mặc định ──────────────
        // Mô phỏng entity_defs.lua của Legend of Lua: mọi thông số quái đều tập trung
        // tại 1 nơi duy nhất (EnemyDef.java), không hardcode rải rác khắp nơi.
        EnemyDef def = EnemyDef.fromId(enemyId);

        // ── Bước 2: Tiled Override (ghi đè từng trường nếu bạn muốn tùy chỉnh) ─
        // Ví dụ: con dơi bảo vệ rương → đặt thêm Custom Property "leashRange=2" trên Tiled
        // để nó không bao giờ bay đi xa. Các con không có property đó dùng giá trị EnemyDef.
        float spriteW      = mapObject.getProperties().get("spriteW",      (float) def.spriteW,  Float.class);
        float spriteH      = mapObject.getProperties().get("spriteH",      (float) def.spriteH,  Float.class);
        float bodyRadius   = mapObject.getProperties().get("bodyRadius",   def.bodyRadius,        Float.class);
        float speed        = mapObject.getProperties().get("speed",        def.speed,             Float.class);
        float wanderRadius = mapObject.getProperties().get("wanderRadius", def.wanderRadius,      Float.class);
        float aggroRange   = mapObject.getProperties().get("aggroRange",   def.aggroRange,        Float.class);
        float deAggroRange = mapObject.getProperties().get("deAggroRange", def.deAggroRange,      Float.class);
        float leashRange   = mapObject.getProperties().get("leashRange",   def.leashRange,        Float.class);

        // ── Bước 3: Xây dựng Entity ─────────────────────────────────────────────
        Entity entity = this.engine.createEntity();

        // --- Transform ---
        Transform transform = addEntityTransform(x, y, 1, (int)spriteW, (int)spriteH, 1f, 1f, entity);

        // --- Box2D Dynamic Body (hình tròn, phù hợp với quái bay/bò) ---
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(transform.getPosition());
        bodyDef.fixedRotation = true;

        Body body = physicWorld.createBody(bodyDef);
        body.setUserData(entity);

        CircleShape circle = new CircleShape();
        circle.setRadius(bodyRadius);
        circle.setPosition(new Vector2(
                transform.getSize().x * 0.5f,
                transform.getSize().y * 0.5f
        ));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        body.createFixture(fixtureDef);
        circle.dispose();

        entity.add(new Physic(body));

        // --- Components: dùng thông số từ EnemyDef (đã override bởi Tiled nếu có) ---
        Enemy enemyComponent = new Enemy(enemyId, wanderRadius, aggroRange, deAggroRange, leashRange);
        enemyComponent.setSpawnPoint(transform.getPosition().x, transform.getPosition().y);
        entity.add(enemyComponent);
        entity.add(new Move(speed));
        entity.add(new Facing(FacingDirection.DOWN));

        // --- Máu & iFrames: đọc từ EnemyDef, có thể override bằng Tiled property "hp" ---
        int hp = mapObject.getProperties().get("hp", def.hp, Integer.class);
        entity.add(new Health(hp));
        entity.add(new Damageable(0.6f)); // 0.6 giây bất tử sau khi trúng đòn

        // Animation: atlasKey = "enemies/bat" → AnimationSystem tìm "enemies/bat/idle_down",
        // "enemies/bat/walk_down", v.v. Nếu kẻ địch chỉ có animation chung (không phân hướng),
        // AnimationSystem sẽ fallback sang key "{atlasKey}/{type}" (sau khi ta cập nhật nó).
        String atlasKey = "enemies/" + enemyId; // "enemies/bat"
        entity.add(new Animation2D(
                AtlasAsset.OBJECTS,
                atlasKey,
                AnimationType.IDLE,
                com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP,
                0.15f
        ));

        // Graphic: dùng frame 0 của key "enemies/bat/bat" trong atlas
        TextureAtlas atlas = this.assetService.get(AtlasAsset.OBJECTS);
        com.badlogic.gdx.graphics.g2d.TextureRegion firstFrame = atlas.findRegion(atlasKey + "/" + enemyId, 0);
        if (firstFrame == null) firstFrame = atlas.findRegion(atlasKey + "/" + enemyId);
        if (firstFrame == null && !atlas.getRegions().isEmpty()) firstFrame = atlas.getRegions().first();
        entity.add(new Graphic(
                firstFrame,
                com.badlogic.gdx.graphics.Color.WHITE.cpy()
        ));

        // --- FSM: Dùng EnemyState (AI), KHÔNG phải AnimationState (Player) ---
        com.badlogic.gdx.ai.fsm.DefaultStateMachine<Entity, EnemyState> stateMachine =
                new com.badlogic.gdx.ai.fsm.DefaultStateMachine<>(entity, EnemyState.IDLE);
        // Nếu Tiled cấu hình ai=wander thì bắt đầu ở trạng thái đi lung tung
        if ("wander".equalsIgnoreCase(aiStr)) {
            stateMachine.changeState(EnemyState.WANDER);
        }
        entity.add(new EnemyFsm(stateMachine));

        this.engine.addEntity(entity);
        com.badlogic.gdx.Gdx.app.log("TiledAshleyConfigurator",
                "Đã sinh ra [" + enemyId + "] tại (" + x + ", " + y + ") | AI: " + aiStr
                + " | aggro=" + aggroRange + " leash=" + leashRange + " speed=" + speed);
    }

    private void onLoadTileObject(TiledMapTileMapObject tileMapObject) {
        // Trường phái Lai: Nếu object này là Player (controller=true) và Player đã tồn tại → bỏ qua
        boolean isController = tileMapObject.getProperties().get("controller", false, Boolean.class);
        if (isController && playerAlreadyExists()) {
            com.badlogic.gdx.Gdx.app.log("TiledAshleyConfigurator",
                    "Đã có Player trong Engine. Bỏ qua hình nộm Player trên Tiled.");
            return;
        }

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
        addEntityPhysic(tile, tile.getObjects(), transform, entity);
        addEntityController(tileMapObject, entity);
        addEntityMove(tile, entity);
        addEntityAnimation(tile, entity);
        entity.add(new Facing(FacingDirection.DOWN));
        entity.add(new Fsm(entity));

        // Chỉ Player (controller=true) mới có máu và có thể bị tấn công
        // Các tile object khác (cây, vật trang trí, NPC tĩnh...) KHÔNG gắn Health/Damageable
        if (isController) {
            entity.add(new Player());  // Tag để phân biệt với tile object khác
            entity.add(new Health(com.github.zen05.lunarsaga.ai.PlayerDef.MAX_HP));
            entity.add(new Damageable(com.github.zen05.lunarsaga.ai.PlayerDef.IFRAME_DURATION));
            entity.add(new com.github.zen05.lunarsaga.component.PlayerFsm(entity));
        }

        this.engine.addEntity(entity);
    }

    /**
     * Kiểm tra xem Player đã tồn tại trong Engine chưa.
     * Dùng để tránh spawn thêm Player khi load map mới (Trường phái Lai).
     */
    private boolean playerAlreadyExists() {
        return engine.getEntitiesFor(Family.all(Player.class).get()).size() > 0;
    }

    /**
     * Tạo Box2D Dynamic Body cho entity từ hình học va chạm trong Tile.
     * Nếu tile không có collision object nào, không gắn Physic component.
     * Tham khảo: mystictutorial / TiledAshleyConfigurator.java#addEntityPhysic()
     */
    private void addEntityPhysic(TiledMapTile tile, MapObjects mapObjects, Transform transform, Entity entity) {
        String bodyTypeStr = tile.getProperties().get("bodyType", "DynamicBody", String.class);
        BodyDef.BodyType bodyType = BodyDef.BodyType.valueOf(bodyTypeStr);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
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
        entity.add(new CameraFollow()); // Nhân vật nào được điều khiển thì camera sẽ bám theo nhân vật đó
        entity.add(new Player());       // Gắn tag Player để nhận diện khi chuyển map
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
