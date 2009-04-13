/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.appbase.client.view;

import com.jme.image.Texture2D;
import com.jme.math.Matrix4f;
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.Point;
import com.jme.scene.state.TextureState;
import java.awt.Dimension;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.input.InputManager3D;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import com.jme.image.Texture;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * If you want to customize the geometry of a displayer, implement
 * this interface and pass an instance to displayer.setGeometryNode.
 */
@ExperimentalAPI
public abstract class GeometryNode extends Node {

    private static final Logger logger = Logger.getLogger(GeometryNode.class.getName());

    /** The displayer for which the geometry object was created. */
    private View2D view;

    /** The texture to be displayed. */
    private Texture2D texture;

    /** Used by calcPositionInPixelCoordinates. */
    private Point lastPosition = null;

    /**
     * Create a new instance of ViewGeometryObject. Attach your geometry as
     * children of this Node.
     * @param displayer The displayer the geometry is to display.
     */
    public GeometryNode (View2D  view) {
        super("GeometryNode for " + view.getName());
        this.view = view;
    }

    /**
     * Clean up resources. Any resources you allocate for the
     * object should be detached in here. Must be called in a render loop safe way.
     *
     * NOTE: It is normal for one ViewObjectGeometry to get stuck in
     * the Java heap (because it is reference by Wonderland picker last
     * picked node). So make sure there aren't large objects, such as a
     * texture, attached to it.
     */
    public void cleanup() {
        view = null;
    }

    /**
     * Specify the size of the geometry node. In your subclass method you should make the 
     * corresponding change to your geometry. This must be called in a render loop safe way.
     * (i.e. from inside a render updater or commit method).
     */
    public abstract void setSize (float width, float height);

    /**
     * Specify the texture to be displayed. In your subclass method you should make your geometry display
     * this texture. Must be called in a render loop safe way.
     */
    public void setTexture(Texture2D texture) {
        this.texture = texture;
    }

    /**
     * Specify the texture coordinates to be used. You should use widthRatio and heightRatio to update
     * the texture coordinates of your geometry. Must be called in a render loop safe way.
     * @param widthRatio The ratio of the displayer width to the rounded up size of the texture width.
     * @param heightRatio The ratio of the displayer height to the rounded up size of the texture height.
     */
    public abstract void setTexCoords(float widthRatio, float heightRatio);

    /**
     * Specifies the transform of the geometry node. 
     * Must be called in a render loop safe way.
     */
    public void setTransform (CellTransform transform) {
        setLocalRotation(transform.getRotation(null));
        setLocalTranslation(transform.getTranslation(null));
    }

    /**
     * Returns the actual (rounded up) size of the texture.
     */
    public Dimension getTextureSize () {
        if (texture != null) {
            return new Dimension(texture.getImage().getWidth(),
                                 texture.getImage().getHeight());
        } else {
            return null;
        }
    }

    /**
     * Transform the given 3D point in world coordinates into the corresponding point
     * in the texel space of the geometry. The given point must be in the plane of the window.
     * <br><br>
     * Note: works when called with both a vector or a point.
     * @param point The point to transform.
     * @param clamp If true return the last position if the argument point is null or the resulting
     * position is outside of the geometry's rectangle. Otherwise, return null if these conditions hold.
     * @return the 2D position of the pixel space the window's image.
     */
    public Point calcPositionInPixelCoordinates(Vector3f point, boolean clamp) {
        if (point == null) {
            if (clamp) {
                return lastPosition;
            } else {
                lastPosition = null;
                return null;
            }
        }
        logger.fine("point = " + point);

        // First calculate the actual coordinates of the corners of the view in world coords.
        float width = view.getDisplayerLocalWidth();
        float height = view.getDisplayerLocalHeight();
        Vector3f topLeftLocal = new Vector3f(-width / 2f, height / 2f, 0f);
        Vector3f topRightLocal = new Vector3f(width / 2f, height / 2f, 0f);
        Vector3f bottomLeftLocal = new Vector3f(-width / 2f, -height / 2f, 0f);
        Vector3f bottomRightLocal = new Vector3f(width / 2f, -height / 2f, 0f);
        logger.fine("topLeftLocal = " + topLeftLocal);
        logger.fine("topRightLocal = " + topRightLocal);
        logger.fine("bottomLeftLocal = " + bottomLeftLocal);
        logger.fine("bottomRightLocal = " + bottomRightLocal);
        Matrix4f local2World = getLocalToWorldMatrix(null); // TODO: prealloc
        Vector3f topLeft = local2World.mult(topLeftLocal, new Vector3f()); // TODO:prealloc
        Vector3f topRight = local2World.mult(topRightLocal, new Vector3f()); // TODO:prealloc
        Vector3f bottomLeft = local2World.mult(bottomLeftLocal, new Vector3f()); // TODO:prealloc
        Vector3f bottomRight = local2World.mult(bottomRightLocal, new Vector3f()); // TODO:prealloc
        logger.fine("topLeft = " + topLeft);
        logger.fine("topRight = " + topRight);
        logger.fine("bottomLeft = " + bottomLeft);
        logger.fine("bottomRight = " + bottomRight);

        // Now calculate the x and y coords relative to the view

        float widthWorld = topRight.x - topLeft.x;
        float heightWorld = topLeft.y - bottomLeft.y;
        logger.fine("widthWorld = " + widthWorld);
        logger.fine("heightWorld = " + heightWorld);

        // TODO: doc: point must be on window
        float x = point.x - topLeft.x;
        float y = (topLeft.y - point.y);
        logger.fine("x = " + x);
        logger.fine("y = " + y);

        x /= widthWorld;
        y /= heightWorld;
        logger.fine("x pct = " + x);
        logger.fine("y pct = " + y);

        // Assumes window is never scaled
        if (clamp) {
            if (x < 0 || x >= 1 || y < 0 || y >= 1) {
                logger.fine("Outside!");
                return lastPosition;
            }
        }

        Window2D window = view.getWindow();
        int winWidth = window.getWidth();
        int winHeight = window.getHeight();
        logger.fine("winWidth = " + winWidth);
        logger.fine("winHeight = " + winHeight);

        logger.fine("Final xy " + (int) (x * winWidth) + ", " + (int) (y * winHeight));
        lastPosition = new Point((int) (x * winWidth), (int) (y * winHeight));
        return lastPosition;
    }

    /**
     * Returns the texture displayed by this geometry.
     */
    public abstract Texture getTexture();

    /**
     * Returns the texture state.
     */
    public abstract TextureState getTextureState();

    /** 
     * Set the ortho Z order (used only when the geometry's render component is in ortho mode).
     * Must be called in a render loop safe way.
     */
    public abstract void setOrthoZOrder (int zOrder);

    /** Returns the Z order. */
    public abstract int getOrthoZOrder();

    /**
     * Given a point in the pixel space of the Wonderland canvas calculates
     * the texel coordinates of the point on the geometry where a
     * ray starting from the current eye position intersects the geometry.
     *
     * Note on subclassing:
     *
     * If the geometry is nonplanar it is recommended that the subclass
     * implement this method by performing a pick. If this pick misses the
     * subclass will need to decide how to handle the miss. One possible way to
     * handle this is to assume that there is a planar "halo" surrounding the
     * the window with which the ray can be intersected.
     */
    public Point calcIntersectionPixelOfEyeRay(int x, int y) {

        // Calculate the ray
        Ray rayWorld = InputManager3D.getInputManager().pickRayWorld(x, y);

        // Calculate an arbitrary point on the plane (in this case, the top left corner)
        float width = texture.getImage().getWidth();
        float height = texture.getImage().getHeight();
        Vector3f topLeftLocal = new Vector3f(-width / 2f, height / 2f, 0f);
        Matrix4f local2World = getLocalToWorldMatrix(null);
        Vector3f topLeftWorld = local2World.mult(topLeftLocal, new Vector3f());

        // Calculate the plane normal
        Vector3f planeNormalWorld = getPlaneNormalWorld();

        // Now find the intersection of the ray with the plane
        Vector3f intPointWorld = calcPlanarIntersection(rayWorld, topLeftWorld, planeNormalWorld);
        if (intPointWorld == null) {
            return null;
        }

        // TODO: opt: we can optimize the following by reusing some of the intermediate
        // results from the previous steps
        Point pt = calcPositionInPixelCoordinates(intPointWorld, false);
        return pt;
    }

    /**
     * Returns the plane normal of the window in world coordinates.
     */
    protected Vector3f getPlaneNormalWorld() {
        // Find two vectors on the plane and take the cross product and then normalize

        float width = texture.getImage().getWidth();
        float height = texture.getImage().getHeight();
        Vector3f topLeftLocal = new Vector3f(-width / 2f, height / 2f, 0f);
        Vector3f topRightLocal = new Vector3f(width / 2f, height / 2f, 0f);
        Vector3f bottomLeftLocal = new Vector3f(-width / 2f, -height / 2f, 0f);
        Vector3f bottomRightLocal = new Vector3f(width / 2f, -height / 2f, 0f);
        logger.fine("topLeftLocal = " + topLeftLocal);
        logger.fine("topRightLocal = " + topRightLocal);
        logger.fine("bottomLeftLocal = " + bottomLeftLocal);
        logger.fine("bottomRightLocal = " + bottomRightLocal);
        Matrix4f local2World = getLocalToWorldMatrix(null); // TODO: prealloc
        Vector3f topLeftWorld = local2World.mult(topLeftLocal, new Vector3f()); // TODO:prealloc
        Vector3f topRightWorld = local2World.mult(topRightLocal, new Vector3f()); // TODO:prealloc
        Vector3f bottomLeftWorld = local2World.mult(bottomLeftLocal, new Vector3f()); // TODO:prealloc
        Vector3f bottomRightWorld = local2World.mult(bottomRightLocal, new Vector3f()); // TODO:prealloc

        Vector3f leftVec = bottomLeftWorld.subtract(topLeftWorld);
        Vector3f topVec = topRightWorld.subtract(topLeftWorld);
        return leftVec.cross(topVec).normalize();
    }

    /**
     * Calculates the point in world coordinates where the given ray
     * intersects the "world plane" of this geometry. Returns null if the ray doesn't intersect the plane.
     * <br><br>
     * All inputs are in world coordinates.     
     * <br><br>
     * @param ray The ray.
     * @param planePoint A point on the plane.
     * @param planeNormal The plane normal vector.
     */
    protected Vector3f calcPlanarIntersection(Ray ray, Vector3f planePoint, Vector3f planeNormal) {

        // Ray Equation is X = P + t * V
        // Plane Equation is (X - P0) dot N = 0
        //
        // where
        //     X is a point on the ray 
        //     P = Starting point for ray (ray.getOrigin())
        //     t = distance along ray to intersection point
        //     V = Direction vector of Ray (ray.getDirection())
        //     P0 = known point on plane (planePoint)
        //     N  = Normal for plane (planeNormal)
        //
        // Combine equations to calculate t:
        //
        // t = [ (P0 - P) dot N ] / (V dot N)
        //
        // Then substitute t into the Ray Equation to get the intersection point.
        //
        // Source: Various: Lars Bishop book, Geometry Toolbox, Doug T.

        Vector3f pointDiffVec = new Vector3f(planePoint);
        pointDiffVec.subtract(ray.getOrigin());
        float numerator = planeNormal.dot(pointDiffVec);
        float denominator = planeNormal.dot(ray.getDirection());
        if (denominator == 0f) {
            // No intersection
            return null;
        }
        float t = numerator / denominator;

        // Now plug t into the Ray Equation is X = P + t * V
        Vector3f x = ray.getDirection().mult(t).add(ray.getOrigin());
        return x;
    }
    /**/

    /*
    // TODO
    // @param N The plane normal in world coords.

    protected Vector3f calcPlanarIntersection(Ray ray, Vector3f planePoint, Vector3f N) {

        // Uses the following formulae for Ray/Plane intersection:
        //
        // Ray Equation is P = P0 + t(V)
        // Plane Equation is P.N + d = 0
        //
        // Where,
        // P = Intersection Point
        // P0 = Starting Point for Ray
        // V = Direction of Ray
        // t = distance along ray to intersection point
        // N = Normal for plane
        // d = from definition of plane
        // 
        // By substitution:
        // 
        // (P0 + t(V)).N + d = 0;
        // P0.N + tV.N + d = 0;
        // tV.N = -(P0.N + d);
        // t = -(P0.N + d)/V.N;
        // 
        // Once you have t, simply substitute in the ray equation for P.
        // 
        // Source: Doug Twilleager. I'm not sure where he got it from.

        Vector3f V = ray.getDirection();
        Vector3f P0 = ray.getOrigin();

        // First calculate the denominator: V dot N
        float denominator = V.dot(N);
        if (denominator == 0f) {
            // No intersection
            return null;
        }

        // t = -(P0.N +d)
        // Once you have t, simply substitute in the ray equation for P.


        // Now calculate the numerator: -(P0.N + d)
        float dotTmp = P0.dot(N);
>>>> left off here
        Vector3f numerator = planePoint.subtract(ray.getOrigin(), new Vector3f());
        float numerator = planeNormal.dot(vecTmp);

        // Now calculate t
        float t = numerator / denominator;

        // Now plug t into the ray equation P = P0 + t * rayDirection
        Vector3f p = ray.getDirection().mult(t).add(ray.getOrigin());
        return p;
    }
    */

}

