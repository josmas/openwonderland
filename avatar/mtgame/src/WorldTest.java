/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

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
import com.jme.app.mtgame.entity.*;

import imi.animations.morphanimation.MorphAnimation;
import imi.animations.morphanimation.MorphAnimationInstance;
import imi.animations.morphanimation.MorphAnimationProcessor;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JLabel;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class WorldTest {
    /**
     * The constructor
     */
    
    /**
     * The CameraNode
     */
    private CameraNode cameraNode = null;
    
    /**
     * The space model
     */
    private Space testSpaces = null;
    
    /**
     * The desired frame rate
     */
    private int desiredFrameRate = 60;
    
    /**
     * The width and height of our 3D window
     */
    private int width = 800;
    private int height = 600;
    private float aspect = 800.0f/600.0f;
    
    public WorldTest(String[] args) {
        WorldManager wm = new WorldManager("TestWorld", null);
        
        processArgs(args);
        wm.setDesiredFrameRate(desiredFrameRate);
        
        createUI(wm);  
        createTestSpaces(wm);
        createTestEntities(wm);
        createCameraEntity(wm);  
        createAnimationEntity(wm);  //  Lou Hayt - morph animation test
    }
    
    private void createAnimationEntity(WorldManager wm) {
         
        String name = "animation entity test";
        float xoff  = 0.0f;
        float yoff  = 0.0f;
        float zoff  = 0.0f;
        MaterialState matState  = null;
        ColorRGBA color = new ColorRGBA();
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;

        ZBufferState buf = (ZBufferState) wm.createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.CF_LEQUAL);

        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);
        LightState ls = (LightState) wm.createRendererState(RenderState.RS_LIGHT);
        ls.setEnabled(true);
        ls.attach(light);

        
        // The animation
        MorphAnimation morphAnimation = new MorphAnimation("test animation");
        MorphAnimationInstance anim = new MorphAnimationInstance(morphAnimation);
        
        Node node = new Node();
        node.attachChild(anim);

        matState = (MaterialState) wm.createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setRenderState(ls);
        node.setLocalTranslation(xoff, yoff, zoff);

        Entity te = new Entity(name + "", null);
        SceneComponent sc = new SceneComponent();
        sc.setSceneRoot(node);
        te.addComponent(SceneComponent.class, sc);
        
        // Animation processor
        MorphAnimationProcessor ap = new MorphAnimationProcessor(name + "Animation Processor", wm, anim);
        te.addComponent(ProcessorComponent.class, ap);
        wm.addEntity(te);        
    }
    
    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera", null);
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
        buf.setFunction(ZBufferState.CF_LEQUAL);

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

        Entity te = new Entity(name + "Teapot", null);
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
        buf.setFunction(ZBufferState.CF_LEQUAL);

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
        Space s = new Space(name + "Space", null, sc, bbox);
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
        WorldTest worldTest = new WorldTest(args);
        
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
        SwingFrame frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        frame.setVisible(true);
    }
    
    class SwingFrame extends JFrame implements FrameRateListener {

        JPanel contentPane;
        JPanel mainPanel = new JPanel();
        Canvas canvas = null;
        JLabel fpsLabel = new JLabel("FPS: ");

        // Construct the frame
        public SwingFrame(WorldManager wm) {
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO: Real cleanup
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());
            mainPanel.setLayout(new GridBagLayout());
            setTitle("DUCK!");

            // make the canvas:
            canvas = wm.createCanvas(width, height);
            canvas.setVisible(true);
            wm.setFrameRateListener(this, 100);

            contentPane.add(mainPanel, BorderLayout.NORTH);
            mainPanel.add(fpsLabel,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0,
                    5), 0, 0));

            canvas.setBounds(0, 0, width, height);
            contentPane.add(canvas, BorderLayout.CENTER);

            pack();
        }
        
        /**
         * Listen for frame rate updates
         */
        public void currentFramerate(float framerate) {
            fpsLabel.setText("FPS: " + framerate);
        }
    }

}
