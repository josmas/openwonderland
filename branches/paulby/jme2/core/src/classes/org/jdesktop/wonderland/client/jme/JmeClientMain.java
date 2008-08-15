/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.jme;

import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.state.ZBufferState;
import com.jme.light.PointLight;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.shape.Teapot;
import com.jme.scene.shape.Box;
import com.jme.bounding.BoundingBox;
import com.jme.math.*;
import org.jdesktop.mtgame.AWTEventListenerComponent;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FPSCameraProcessor;
import org.jdesktop.mtgame.NodeListener;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RotationProcessor;
import org.jdesktop.mtgame.SceneComponent;
import org.jdesktop.mtgame.Space;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 */
public class JmeClientMain {
    
    /**
     * The CameraNode
     */
    private CameraNode cameraNode = null;
        
    /**
     * The desired frame rate
     */
    private int desiredFrameRate = 30;
    
    /**
     * The width and height of our 3D window
     */
    private int width = 800;
    private int height = 600;
    private float aspect = 800.0f/600.0f;
    
    private static WorldManager worldManager;
    
    public JmeClientMain(String[] args) {
        final ClientManager clientManager = new ClientManager();
        worldManager = new WorldManager("Wonderland");
        
        worldManager.addNodeListener(new NodeListener() {

            public void nodeMoved(Node arg0) {
                clientManager.nodeMoved(arg0);
            }
            
        });
        
        processArgs(args);
        worldManager.setDesiredFrameRate(desiredFrameRate);
        
        createUI(worldManager);  
//        createTestSpaces(worldManager);
//        createTestEntities(worldManager);
        createCameraEntity(worldManager);        
    }
    
    public static WorldManager getWorldManager() {
        return worldManager;
    }
    
    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = new CameraComponent(width, height, 45.0f, aspect, 1.0f, 1000.0f, true);
        cc.setCameraSceneGraph(cameraSG);
        cc.setCameraNode(cameraNode);
        camera.addComponent(CameraComponent.class, cc);

        // Create the input listener and process for the camera
        AWTEventListenerComponent eventListener = new AWTEventListenerComponent();
        FPSCameraProcessor eventProcessor = new FPSCameraProcessor(eventListener, cameraNode, wm, camera);
        eventProcessor.setRunInRenderer(true);
        camera.addComponent(ProcessorComponent.class, eventProcessor);
        wm.addAWTKeyListener(eventProcessor);
        wm.addAWTMouseListener(eventProcessor);    
        wm.addEntity(camera);         
    }
    
    
    private void createTestEntities(WorldManager wm) {
        ColorRGBA color = new ColorRGBA();

        ZBufferState buf = (ZBufferState) wm.createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);
        LightState lightState = (LightState) wm.createRendererState(RenderState.RS_LIGHT);
        lightState.setEnabled(true);
        lightState.attach(light);

        // The center teapot
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        createTeapotEntity("Center ", 0.0f, 0.0f, 0.0f, buf, lightState, color, wm);

        color.r = 0.0f; color.g = 1.0f; color.b = 0.0f; color.a = 1.0f;
        createTeapotEntity("North ", 0.0f, 0.0f, 100.0f, buf, lightState, color, wm);
        
        color.r = 1.0f; color.g = 0.0f; color.b = 0.0f; color.a = 1.0f;
        createTeapotEntity("South ", 0.0f, 0.0f, -100.0f, buf, lightState, color, wm);
        
        color.r = 1.0f; color.g = 1.0f; color.b = 0.0f; color.a = 1.0f;
        createTeapotEntity("East ", 100.0f, 0.0f, 0.0f, buf, lightState, color, wm);
        
        color.r = 0.0f; color.g = 1.0f; color.b = 1.0f; color.a = 1.0f;
        createTeapotEntity("West ", -100.0f, 0.0f, 0.0f, buf, lightState, color, wm);        
    }
    
    public void createTeapotEntity(String name, float xoff, float yoff, float zoff, 
            ZBufferState buf, LightState ls, ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;
        
        // The center teapot
        Node node = new Node();
        Teapot teapot = new Teapot();
        teapot.resetData();
        node.attachChild(teapot);

        matState = (MaterialState) wm.createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setRenderState(ls);
        node.setLocalTranslation(xoff, yoff, zoff);

        Entity te = new Entity(name + "Teapot");
        SceneComponent sc = new SceneComponent();
        sc.setSceneRoot(node);
        te.addComponent(SceneComponent.class, sc);
        
        RotationProcessor rp = new RotationProcessor(name + "Teapot Rotator", wm, 
                node, (float) (6.0f * Math.PI / 180.0f));
        //rp.setRunInRenderer(true);
        te.addComponent(ProcessorComponent.class, rp);
        wm.addEntity(te);        
    }

    private void createTestSpaces(WorldManager wm) {
        ColorRGBA color = new ColorRGBA();
        Vector3f center = new Vector3f();

        ZBufferState buf = (ZBufferState) wm.createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);

        /** Attach the light to a lightState and the lightState to rootNode. */
        LightState lightState = (LightState) wm.createRendererState(RenderState.RS_LIGHT);
        lightState.setEnabled(true);
        lightState.setTwoSidedLighting(true);
        lightState.attach(light);

        // First create the geometry
        center.x = 0.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        createSpace("Center ", center, buf, lightState, color, wm);
        
        center.x = 0.0f; center.y = 25.0f; center.z = 98.0f;
        color.r = 0.0f; color.g = 1.0f; color.b = 0.0f; color.a = 1.0f;
        createSpace("North ", center, buf, lightState, color, wm);

        center.x = 0.0f; center.y = 25.0f; center.z = -98.0f;
        color.r = 1.0f; color.g = 0.0f; color.b = 0.0f; color.a = 1.0f;
        createSpace("South ", center, buf, lightState, color, wm);

        center.x = 98.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 1.0f; color.g = 1.0f; color.b = 0.0f; color.a = 1.0f;
        createSpace("East ", center, buf, lightState, color, wm);

        center.x = -98.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 0.0f; color.g = 1.0f; color.b = 1.0f; color.a = 1.0f;
        createSpace("West ", center, buf, lightState, color, wm);
    }
    
    public void createSpace(String name, Vector3f center, ZBufferState buf, LightState ls,
            ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;

        Box cube = null;
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        
        // Create the root for the space
        Node node = new Node();
        
        // Now the walls
        Box box = new Box(name + "Box", center, 50.0f, 50.0f, 50.0f);
        node.attachChild(box);
       
        // Now some rotating cubes - all confined within the space (not entities)
        createCube(center, -25.0f, 15.0f,  25.0f, pcc, node, wm);
        createCube(center,  25.0f, 15.0f,  25.0f, pcc, node, wm);
        createCube(center,  25.0f, 15.0f, -25.0f, pcc, node, wm);
        createCube(center, -25.0f, 15.0f, -25.0f, pcc, node, wm);
     
        // Add bounds and state for the whole space
        BoundingBox bbox = new BoundingBox(center, 50.0f, 50.0f, 50.0f);
        node.setModelBound(bbox);
        node.setRenderState(buf);
        node.setRenderState(ls);
        matState = (MaterialState) wm.createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);
        
        // Create a scene component for it
        SceneComponent sc = new SceneComponent();
        sc.setSceneRoot(node);
        
        // Finally, create the space and add it.
        Space s = new Space(name + "Space", sc, bbox);
        s.addComponent(ProcessorCollectionComponent.class, pcc);
        wm.addSpace(s);        
    }
    
    private void createCube(Vector3f center, float xoff, float yoff, float zoff, 
            ProcessorCollectionComponent pcc, Node parent, WorldManager wm) {
        Vector3f cubeCenter = new Vector3f();
        Vector3f c = new Vector3f();
        
        cubeCenter.x = center.x + xoff;
        cubeCenter.y = center.y + yoff;
        cubeCenter.z = center.z + zoff;
        Box cube = new Box("Space Cube", c, 5.0f, 5.0f, 5.0f);
        Node cubeNode = new Node();
        cubeNode.setLocalTranslation(cubeCenter);
        cubeNode.attachChild(cube);  
        parent.attachChild(cubeNode);
        
        RotationProcessor rp = new RotationProcessor("Cube Rotator", wm, cubeNode, 
                (float) (6.0f * Math.PI / 180.0f));
        //rp.setRunInRenderer(true);
        pcc.addProcessor(rp);
    }
    
    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");        
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);
        
        return (cameraSG);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JmeClientMain worldTest = new JmeClientMain(args);
        
    }
    
    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
                i++;
            }
        }
    }
    
    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm) {             
        MainFrame frame = new MainFrame(wm, width, height);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        frame.setVisible(true);
    }
    
}
