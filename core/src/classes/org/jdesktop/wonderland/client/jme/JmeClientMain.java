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
