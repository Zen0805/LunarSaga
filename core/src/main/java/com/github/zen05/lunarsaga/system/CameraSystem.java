package com.github.zen05.lunarsaga.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.zen05.lunarsaga.GdxGame;
import com.github.zen05.lunarsaga.component.CameraFollow;
import com.github.zen05.lunarsaga.component.Transform;

public class CameraSystem extends IteratingSystem {
    private final Camera camera;
    private final float smoothingFactor;
    private final Vector2 targetPosition;
    private float mapW;
    private float mapH;

    public CameraSystem(Camera camera) {
        super(Family.all(CameraFollow.class, Transform.class).get());
        this.camera = camera;
        this.smoothingFactor = 4f; // Số càng nhỏ, camera trượt (lerp) càng chậm/mượt
        this.targetPosition = new Vector2();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);
        // Lấy tâm của nhân vật
        Vector2 entityCenter = new Vector2(
                transform.getPosition().x + transform.getSize().x * 0.5f,
                transform.getPosition().y + transform.getSize().y * 0.5f
        );
        calcTargetPosition(entityCenter);

        // Smooth Lerp
        float progress = smoothingFactor * deltaTime;
        float smoothedX = MathUtils.lerp(camera.position.x, this.targetPosition.x, progress);
        float smoothedY = MathUtils.lerp(camera.position.y, this.targetPosition.y, progress);
        camera.position.set(smoothedX, smoothedY, camera.position.z);
    }

    /**
     * Tính toán vị trí mục tiêu của Camera sao cho không vượt qua biên giới (Map Boundaries) của bản đồ.
     */
    private void calcTargetPosition(Vector2 entityCenter) {
        float targetX = entityCenter.x;
        float camHalfW = camera.viewportWidth * 0.5f;
        
        // Chặn biên trái và phải
        if (mapW > camera.viewportWidth) {
            float min = camHalfW;
            float max = mapW - camHalfW;
            targetX = MathUtils.clamp(targetX, min, max);
        } else {
            // Nếu map nhỏ hơn màn hình, căn giữa camera
            targetX = mapW * 0.5f;
        }

        float targetY = entityCenter.y;
        float camHalfH = camera.viewportHeight * 0.5f;
        
        // Chặn biên dưới và trên
        if (mapH > camera.viewportHeight) {
            float min = camHalfH;
            float max = mapH - camHalfH;
            targetY = MathUtils.clamp(targetY, min, max);
        } else {
            // Nếu map nhỏ hơn màn hình, căn giữa camera
            targetY = mapH * 0.5f;
        }

        this.targetPosition.set(targetX, targetY);
    }

    /**
     * Lấy kích thước của bản đồ hiện tại để làm giới hạn cho Camera
     */
    public void setMap(TiledMap tiledMap) {
        int width = tiledMap.getProperties().get("width", 0, Integer.class);
        int tileW = tiledMap.getProperties().get("tilewidth", 0, Integer.class);
        int height = tiledMap.getProperties().get("height", 0, Integer.class);
        int tileH = tiledMap.getProperties().get("tileheight", 0, Integer.class);
        
        mapW = width * tileW * GdxGame.UNIT_SCALE;
        mapH = height * tileH * GdxGame.UNIT_SCALE;

        // Ép camera di chuyển ngay lập tức tới vị trí nhân vật (bỏ qua hiệu ứng mượt) 
        // để khi chuyển map không bị quay cuồng màn hình
        Entity camEntity = getEntities().first();
        if (camEntity == null) {
            return;
        }
        
        Transform transform = Transform.MAPPER.get(camEntity);
        Vector2 entityCenter = new Vector2(
                transform.getPosition().x + transform.getSize().x * 0.5f,
                transform.getPosition().y + transform.getSize().y * 0.5f
        );
        calcTargetPosition(entityCenter);
        camera.position.set(this.targetPosition.x, this.targetPosition.y, camera.position.z);
    }
}
