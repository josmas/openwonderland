/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/

package com.jme.app.mtgame.entity;

import java.util.HashMap;
import com.jme.renderer.Camera;
import com.jme.scene.Spatial;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;

/**
* This Entity Component encapsulates camera control data and calculations.
* The position is inherited from the entity.
 * 
* @author Doug Twilleager
*/
public class CameraComponent extends EntityComponent {
    /**
     * A flag that indicates that this is the primary camera
     */
    private boolean primary = false;
    
    /**
     * The viewport width and height
     */
    private int width = -1;
    private int height = -1;
    
    /**
     * The field of view
     */
    private float fieldOfView = 45.0f;
    
    /**
     * The aspect ratio
     */
    private float aspectRatio = 1.0f;
   
    /**
     * The near and far clip planes
     */
    private float nearClip = 1.0f;
    private float farClip = 1000.0f;
    
    /**
     * The jME Camera object
     * Note: This is created by the renderer
     */
    private Camera camera = null;
    
    /**
     * The scene graph which contains the CameraNode
     */
    private Node cameraSceneGraph = null;
    
    /**
     * A reference to the CameraNode
     */
    private CameraNode cameraNode = null;
    
    /**
     * The constructor
     */
    public CameraComponent(int viewportWidth, int viewportHeight, float fov,
            float aspect, float near, float far, boolean primary) {
        width = viewportWidth;
        height = viewportHeight;
        fieldOfView = fov;
        aspectRatio = aspect;
        nearClip = near;
        farClip = far;
        this.primary = primary;
    }
    
    /**
     * Parse the known attributes.
     */
    public void parseAttributes(HashMap attributes) {
       
    }
    
    /**
     * Set the viewport width and height
     */
    public void setViewport(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Get the vieport width
     */
    public int getViewportWidth() {
        return (width);
    }
    
    /**
     * Get the Viewport height
     */
    public int getViewportHeight() {
        return (height);
    }
    
    /**
     * Set the Field of View
     */
    public void setFieldOfView(float fov) {
        fieldOfView = fov;
    }
    
    /**
     * Get the field of view
     */
    public float getFieldOfView() {
        return (fieldOfView);
    }
    
    /**
     * Set the aspect ratio
     */
    public void setAspectRatio(float ratio) {
        aspectRatio = ratio;
    }
    
    /**
     * Get the aspect ratio
     */
    public float getAspectRatio() {
        return (aspectRatio);
    }
    
    /**
     * Set the near and far clip distances
     */
    public void setClipDistances(float near, float far) {
        nearClip = near;
        farClip = far;
    }
    
    /**
     * Get the near clip distance
     */
    public float getNearClipDistance() {
        return (nearClip);
    }
    
    /**
     * Get the far clip distance
     */
    public float getFarClipDistance() {
        return (farClip);
    }
    
    /**
     * Set the camera scene graph
     */
    public void setCameraSceneGraph(Node sg) {
        cameraSceneGraph = sg;
    }
    
    /**
     * Get the camera scene graph
     */
    public Node getCameraSceneGraph() {
        return (cameraSceneGraph);
    }
    
    /**
     * Set the CameraNode reference
     */
    public void setCameraNode(CameraNode cn) {
        cameraNode = cn;
    }
    
    /**
     * Get the CameraNode
     */
    public CameraNode getCameraNode() {
        return (cameraNode);
    }
    
    /**
     * Set the primary camera flag
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
    
    /**
     * Get the primary flag
     */
    public boolean isPrimary() {
        return (primary);
    }
    
    /**
     * Set the jME Camera
     * Note: This is used by the renderer
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
    
    /**
     * Get the jME Camera
     */
    public Camera getCamera() {
        return (camera);
    }
}