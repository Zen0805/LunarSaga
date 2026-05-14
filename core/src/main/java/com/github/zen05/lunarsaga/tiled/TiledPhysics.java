package com.github.zen05.lunarsaga.tiled;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.zen05.lunarsaga.GdxGame;

/**
 * Chuyển đổi các đối tượng hình học của Tiled (Rectangle, Ellipse, Polygon...)
 * thành FixtureDef của Box2D để gắn vào một Body.
 *
 * Tham khảo từ: mystictutorial / tiled/TiledPhysics.java
 */
public final class TiledPhysics {

    private TiledPhysics() {}

    public static FixtureDef fixtureDefOf(MapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        if (mapObject instanceof RectangleMapObject rectMapObj) {
            return rectangleFixtureDef(rectMapObj, scaling, relativeTo);
        } else if (mapObject instanceof EllipseMapObject ellipseMapObj) {
            return ellipseFixtureDef(ellipseMapObj, scaling, relativeTo);
        } else if (mapObject instanceof PolygonMapObject polygonMapObj) {
            Polygon polygon = polygonMapObj.getPolygon();
            float offsetX = polygon.getX() * GdxGame.UNIT_SCALE;
            float offsetY = polygon.getY() * GdxGame.UNIT_SCALE;
            return polyFixtureDef(polygonMapObj, polygon.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else if (mapObject instanceof PolylineMapObject polylineMapObj) {
            Polyline polyline = polylineMapObj.getPolyline();
            float offsetX = polyline.getX() * GdxGame.UNIT_SCALE;
            float offsetY = polyline.getY() * GdxGame.UNIT_SCALE;
            return polyFixtureDef(polylineMapObj, polyline.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else {
            throw new GdxRuntimeException("Unsupported MapObject for physics: " + mapObject.getClass().getSimpleName());
        }
    }

    private static FixtureDef rectangleFixtureDef(RectangleMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        Rectangle r = mapObject.getRectangle();
        float boxX = r.x * GdxGame.UNIT_SCALE * scaling.x - relativeTo.x;
        float boxY = r.y * GdxGame.UNIT_SCALE * scaling.y - relativeTo.y;
        float boxW = r.width  * GdxGame.UNIT_SCALE * scaling.x * 0.5f;
        float boxH = r.height * GdxGame.UNIT_SCALE * scaling.y * 0.5f;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxW, boxH, new Vector2(boxX + boxW, boxY + boxH), 0f);
        return fixtureDefOf(mapObject, shape);
    }

    private static FixtureDef ellipseFixtureDef(EllipseMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        Ellipse e = mapObject.getEllipse();
        float ex = e.x * GdxGame.UNIT_SCALE * scaling.x - relativeTo.x;
        float ey = e.y * GdxGame.UNIT_SCALE * scaling.y - relativeTo.y;
        float ew = e.width  * GdxGame.UNIT_SCALE * scaling.x * 0.5f;
        float eh = e.height * GdxGame.UNIT_SCALE * scaling.y * 0.5f;

        if (MathUtils.isEqual(ew, eh, 0.1f)) {
            CircleShape shape = new CircleShape();
            shape.setPosition(new Vector2(ex + ew, ey + eh));
            shape.setRadius(ew);
            return fixtureDefOf(mapObject, shape);
        }

        final int N = 8;
        float step = MathUtils.PI2 / N;
        Vector2[] verts = new Vector2[N];
        for (int i = 0; i < N; i++) {
            float angle = i * step;
            verts[i] = new Vector2(ex + ew + ew * MathUtils.cos(angle), ey + eh + eh * MathUtils.sin(angle));
        }
        PolygonShape shape = new PolygonShape();
        shape.set(verts);
        return fixtureDefOf(mapObject, shape);
    }

    private static FixtureDef polyFixtureDef(MapObject mapObject, float[] rawVerts,
                                              float offsetX, float offsetY,
                                              Vector2 scaling, Vector2 relativeTo) {
        offsetX = offsetX * scaling.x - relativeTo.x;
        offsetY = offsetY * scaling.y - relativeTo.y;
        float[] verts = new float[rawVerts.length];
        for (int i = 0; i < rawVerts.length; i += 2) {
            verts[i]     = offsetX + rawVerts[i]     * GdxGame.UNIT_SCALE * scaling.x;
            verts[i + 1] = offsetY + rawVerts[i + 1] * GdxGame.UNIT_SCALE * scaling.y;
        }
        ChainShape shape = new ChainShape();
        if (mapObject instanceof PolygonMapObject) {
            shape.createLoop(verts);
        } else {
            shape.createChain(verts);
        }
        return fixtureDefOf(mapObject, shape);
    }

    private static FixtureDef fixtureDefOf(MapObject mapObject, Shape shape) {
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.friction    = mapObject.getProperties().get("friction",    0f,    Float.class);
        fd.restitution = mapObject.getProperties().get("restitution", 0f,    Float.class);
        fd.density     = mapObject.getProperties().get("density",     0f,    Float.class);
        fd.isSensor    = mapObject.getProperties().get("sensor",      false, Boolean.class);
        return fd;
    }
}
