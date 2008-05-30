/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import com.jme.app.mtgame.entity.*;
import com.jmex.awt.SimpleCanvasImpl;
import com.jmex.awt.JMECanvas;
import com.jme.system.lwjgl.LWJGLDisplaySystem;
import com.jmex.awt.lwjgl.LWJGLCanvas;
import org.lwjgl.opengl.AWTGLCanvas;

import java.util.ArrayList;
import java.awt.Canvas;
import java.awt.event.*;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.Spatial;
import com.jme.intersection.CollisionResults;
import com.jme.system.*;
import com.jme.renderer.*;
import com.jme.input.*;
import com.jme.scene.state.*;
import com.jmex.awt.input.AWTMouseInput;

/**
 * This is the main rendering thread for a screen.  All jME calls must be 
 * made from this thread
 * 
 * @author Doug Twilleager
 */
public class Renderer extends Thread {
    /**
     * The RenderManager for this renderer
     */
    private RenderManager renderManager = null;
    
    /**
     * The EntityProcessController
     */
    private EntityProcessController entityProcessController = null;
    
    /**
     * The list of processors to run on the frame
     */
    private ArrayList processorsTriggered = new ArrayList();
    
    /**
     * A flag indicating, that some entity has been added or removed
     */
    private boolean entityChanged = false;
    
    /**
     * A lock to acquire when checking/changing entities
     */
    private Object entityLock = new Object();
    
    /**
     * The list of scene objects that need their state updated
     */
    private ArrayList sceneUpdateList = new ArrayList();

    /**
     * The list of scene objects that need their state updated
     */
    private ArrayList updateList = new ArrayList();
        
    /**
     * The list of camera objects that need their state updated
     */
    private ArrayList cameraUpdateList = new ArrayList();
    
    /**
     * The cached list of known cameras
     */
    private ArrayList renderCameras = new ArrayList();
    
    /**
     * The array list of camera's
     */
    private ArrayList cameras = new ArrayList();
    
    /**
     * A boolean indicating that the camera list has changed
     */
    private boolean camerasChanged = false;
    
    /**
     * The current list of scenes
     */
    private ArrayList renderScenes = new ArrayList();
    
    /**
     * The array list of scene's
     */
    private ArrayList scenes = new ArrayList();
    
    /**
     * A boolean indicating that the scene list has changed
     */
    private boolean scenesChanged = false;
    
    /**
     * The list of HUD elements
     */
    private HudComponent[] renderHuds = null;
    
    /**
     * The array list of hud's
     */
    private ArrayList huds = new ArrayList();
    
    /**
     * A boolean indicating that the hud list has changed
     */
    private boolean hudsChanged = false;
    
    /**
     * The list of Avatars
     */
    private AvatarComponent[] renderAvatars = null;
    
    /**
     * The array list of avatar's
     */
    private ArrayList avatars = new ArrayList();
    
    /**
     * A boolean indicating that the avatar list has changed
     */
    private boolean avatarsChanged = false;
    
    /**
     * The list of Spaces
     */
    private ArrayList collisionSpaces = new ArrayList();
    
    /**
     * The array list of avatar's
     */
    private ArrayList spaces = new ArrayList();
    
    /**
     * A boolean indicating that the avatar list has changed
     */
    private boolean spacesChanged = false;
    
    /**
     * The screen number for this renderer
     */
    private int screenNumber = -1;
    
    /**
     * This flag tells the renderer when it is done.
     */
    private boolean done = false;
    
    /**
     * The commit process list - to be processed as we can at the
     * end of the render loop.
     */
    private ProcessorComponent[] commitList = null;
    
    /**
     * The committer currently being processed.
     */
    private int currentCommit = 0;
    
    /**
     * The desired framerate, in frames per second.
     */
    private int desiredFrameRate = 60;
    
    /**
     * The frame time needed to achieve the desired frames per second.
     */
    private long desiredFrameTime = -1;
    
    /**
     * A callback for someone interested in the framerate
     */
    private FrameRateListener frameRateListener = null;
    
    /**
     * The Frequency - in number of frames - to update the listener
     */
    private int frameRateListenerFrequency = 0;
    
    /**
     * A countdown variable for the listener
     */
    private int listenerCountdown = 0;
    private long listenerStarttime = 0;
    
    /**
     * The Display System for jME
     */
    private DisplaySystem displaySystem = null;
       
    /**
     * The Display System for jME
     */
    private LWJGLDisplaySystem lwjglDisplay = null;
    
    /**
     * The jME Renderer object
     */
    private com.jme.renderer.Renderer jmeRenderer = null;
    
    /**
     * The current jME KeyInput object
     */
    private KeyInput keyInput = null;
    
    /**
     * A boolean that controls whether or not we track key input
     */
    private boolean trackKeyInput = false;
        
    /**
     * The current jME MouseInput object
     */
    private MouseInput mouseInput = null;
    
    /**
     * A boolean that controls whether or not we track key input
     */
    private boolean trackMouseInput = false;
    
    /**
     * The current rendering canvas
     */
    private AWTGLCanvas currentCanvas = null;
    
    /**
     * The current canvas in AWT form
     */
    private Canvas canvas = null;
    
    /**
     * A boolean that indicates that the canvas is ready
     */
    private boolean canvasReady = false;
            
    /**
     * The constructor
     */
    public Renderer(RenderManager rm, int screenNum) {
        renderManager = rm;
        screenNumber = screenNum;
        desiredFrameTime = 1000000000/desiredFrameRate;
    }
    
    /**
     * Get the renderer started.  This is called from the render manager to get
     * the thread started.
     */
    public synchronized void initialize() {
        this.start();
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    } 

    public synchronized Canvas createCanvas(int width, int height) {
        // Create the canvas and it's notification object
        canvas = displaySystem.createCanvas(width, height);

        MyImplementor impl = new MyImplementor(this, width, height);
        JMECanvas jmeCanvas = ((JMECanvas) canvas);
        jmeCanvas.setImplementor(impl);
         
        currentCanvas = (AWTGLCanvas)canvas;
        displaySystem.setCurrentCanvas(jmeCanvas);
        return (canvas);
    }
    
    /**
     * Set the entityProcessController
     */
    public void setEntityProcessController(EntityProcessController epc) {
        entityProcessController = epc;
    }
    
    /**
     * This is internal initialization done once.
     */
    private synchronized void initRenderer() {
        //Create the base jME objects
        try {
            displaySystem = DisplaySystem.getDisplaySystem("LWJGL");
            lwjglDisplay = (LWJGLDisplaySystem) displaySystem;
        } catch (JmeException e) {
            System.out.println(e);
        }

        // Let the caller know to proceed
        notify();
        
        // Now wait for a canvas...
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        
        // We should be set to go now...
        jmeRenderer = displaySystem.getRenderer();
        System.out.println("jmeRenderer: " + jmeRenderer);
        canvasReady = true;
    }
    
    public synchronized void canvasIsReady() {
        // Just notify the renderer
        notify();
    }
    
    public void waitUntilReady() {
        while (!canvasReady) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
    
    /**
     * Get an object from the jME Renderer
     */
    public RenderState createRendererState(int type) {
        return (jmeRenderer.createState(type));
    }
    
    /**
     * The render loop
     */
    public void run() {
        long processTime = -1;
        long frameStartTime = -1;
        long renderTime = -1;
        long totalTime = -1;
        long threadStartTime = System.nanoTime();
        
        initRenderer();
        while (!done) {
            // Snapshot the current time
            frameStartTime = System.nanoTime();
            
            /**
             * Grab any new entities
             */
            synchronized (collisionSpaces) {
                checkForEntityChanges();
            }
            
            if (renderScenes.size() > 0 || renderCameras.size() > 0) {
                //lwjglDisplay.setCurrentCanvas((JMECanvas)currentCanvas);
                //lwjglDisplay.switchContext(currentCanvas);
                try {
                    currentCanvas.makeCurrent();
                } catch (org.lwjgl.LWJGLException e) {
                    System.out.println(e);
                }

                /* 
                 * This block of code handles calling entity processes which are
                 * locked to the renderer - like the current camera.
                 */
                runProcessorsTriggered();
                
                /* 
                 * This block handles any state updates needed to any of the graphs
                 */
                synchronized (collisionSpaces) {
                    processCameraUpdates(0.0f);
                    processSceneUpdates(0.0f);
                    processUpdates(0.0f);
                }

                // First, clear the buffers
                //jmeRenderer.setBackgroundColor(new ColorRGBA(1.0f, 0.0f, 0.0f, 0.0f));
                jmeRenderer.clearBuffers();

                /*
                 * This block does the actual rendering of the frame
                 */
                for (int i = 0; i < renderScenes.size(); i++) {
                    SceneComponent scene = (SceneComponent) renderScenes.get(i);
                    Node sceneRoot = scene.getSceneRoot();

                    //System.out.println("Rendering: " + sceneRoot);
                    jmeRenderer.draw(sceneRoot);
                }

                // Now, swap the buffer
                //jmeRenderer.displayBackBuffer();
                try {
                    currentCanvas.swapBuffers();
                } catch (org.lwjgl.LWJGLException e) {
                    System.out.println(e);
                }
            }
            
            /*
             * Now we track some times, and process the commit lists
             */
            
            // Snapshot the time it took to render
            renderTime = System.nanoTime() - frameStartTime;
            //Calculate the amount of time left to process commits
            processTime = desiredFrameTime - renderTime;
            
            // Process the commit list
            if (processTime > 0) {
                synchronized (collisionSpaces) {
                    processCommitList(processTime);
                }
            } else {
                //System.out.println("NEED TO ADAPT TO NEGATIVE PROCESS TIME");
            }
             
            // Let the processes know that we want to do a frame tick
            if (screenNumber == 0) {
                renderManager.triggerNewFrame();
            }

            
            // Decide if we need to sleep
            totalTime = System.nanoTime() - frameStartTime;
            if (totalTime < desiredFrameTime) {
                // Sleep to hit the frame rate
                try {
                    int sleeptime = (int)(desiredFrameTime - totalTime);
                    int numMillis = sleeptime/1000000;
                    int numNanos = sleeptime - (numMillis*1000000);
                    //System.out.println("Sleeping for " + numMillis + ", " + numNanos);
                    Thread.sleep(numMillis, numNanos);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }

            if (frameRateListener != null) {
                listenerCountdown--;
                if (listenerCountdown == 0) {
                    long currentTime = System.nanoTime();
                    long elapsedTime = currentTime - listenerStarttime;
                    float flTime = elapsedTime/1000000000.0f;
                    float framerate = ((float)frameRateListenerFrequency)/flTime;
                    frameRateListener.currentFramerate(framerate);

                    listenerCountdown = frameRateListenerFrequency;
                    listenerStarttime = currentTime;
                }
            }
        }
    }
    
    /**
     * Process the scene updates
     */
    private void processUpdates(float referenceTime) {

        if (updateList.size() != 0) {
            for (int i = 0; i < updateList.size(); i++) {
                Spatial s = (Spatial) updateList.get(i);

                s.updateGeometricState(referenceTime, true);
                s.updateRenderState();
            }
            updateList.clear();
        }
    }
    
    /**
     * Process the scene updates
     */
    private void processSceneUpdates(float referenceTime) {

        if (sceneUpdateList.size() != 0) {
            for (int i = 0; i < sceneUpdateList.size(); i++) {
                SceneComponent scene = (SceneComponent) sceneUpdateList.get(i);
                Node sceneRoot = scene.getSceneRoot();

                sceneRoot.updateGeometricState(referenceTime, true);
                sceneRoot.updateRenderState();
            }
            sceneUpdateList.clear();
        }
    }

    /**
     * Process the camera updates
     */
    private void processCameraUpdates(float referenceTime) {
        CameraComponent cameraComponent = null;
        Node cameraSceneGraph = null;
        Camera camera = null;
        CameraNode cameraNode = null;

        if (cameraUpdateList.size() != 0) {
            for (int i = 0; i < cameraUpdateList.size(); i++) {
                cameraComponent = (CameraComponent) cameraUpdateList.get(i);
                cameraSceneGraph = cameraComponent.getCameraSceneGraph();
                
                
                camera = cameraComponent.getCamera();
                cameraNode = cameraComponent.getCameraNode();
                if (camera == null) {
                    int width = cameraComponent.getViewportWidth();
                    int height = cameraComponent.getViewportHeight();
                    float fov = cameraComponent.getFieldOfView();
                    float near = cameraComponent.getNearClipDistance();
                    float far = cameraComponent.getFarClipDistance();
                    float aspect = (float)width/(float)height;
                    
                    camera = jmeRenderer.createCamera(width, height);
                    camera.setFrustumPerspective(fov, aspect, near, far);
                    cameraComponent.setCamera(camera);
                    cameraNode.setCamera(camera);
                }
                
                if (cameraComponent.isPrimary()) {
                    camera.update();
                    jmeRenderer.setCamera(camera);
                }

                cameraSceneGraph.updateGeometricState(referenceTime, true);
            }
            cameraUpdateList.clear();
        }
    }
    
    /**
     * Process as many committers as we can, given the amount of process time
     */
    private synchronized void processCommitList(long processTime) {
        long currentTime = System.nanoTime();
        long elapsedTime = 0;
        long nextCurrentTime = 0;
        ProcessorComponent pc = null;
        
        if (commitList == null) {
            //System.out.println("Renderer: No Commits");
            return;
        }
        
        // Note: We won't stop in the middle of a chain
        while (elapsedTime < processTime && currentCommit != commitList.length) {
            pc = commitList[currentCommit++];
            pc.commit(pc.getCurrentTriggerConditions());
            pc.clearTriggerConditions();

            // Process the chain
            pc = pc.getNextInChain();
            while (pc != null) {
                pc.commit(pc.getCurrentTriggerConditions());
                pc.clearTriggerConditions();
                pc = pc.getNextInChain();
            }

            
            nextCurrentTime = System.nanoTime();
            elapsedTime += (nextCurrentTime - currentTime);
            currentTime = nextCurrentTime;
        }
        
        // If we are done, notify the process controller
        if (currentCommit == commitList.length) {
            currentCommit = 0;
            commitList = null;
            notify();
        }
    }
    
    /**
     * Run the processes component commit list
     * For now, we'll just run them on screen 0
     */
    public synchronized void runCommitList(ProcessorComponent[] runList) {
        commitList = runList;
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        
    }
        
    /**
     * Add a processor which has triggerd to the Renderer Processor List
     */
    public void addTriggeredProcessor(ProcessorComponent pc) {
        synchronized (processorsTriggered) {
            if (!processorsTriggered.contains(pc)) {
                processorsTriggered.add(pc);
            }
        }
    }
    
    /** 
     * Run the processors that have triggered
     */
    private void runProcessorsTriggered() {
        ProcessorComponent pc = null;
        ProcessorComponent[] procs = new ProcessorComponent[0];
        
        synchronized (processorsTriggered) {
            // Snapshot the list of those to run.
            procs = (ProcessorComponent[]) processorsTriggered.toArray(procs);
            processorsTriggered.clear();
        }
        
        for (int i = 0; i < procs.length; i++) {
            pc = procs[i];
            pc.compute(pc.getCurrentTriggerConditions());
            pc.commit(pc.getCurrentTriggerConditions());
            pc.clearTriggerConditions();
            entityProcessController.armProcessorComponent(pc, pc.getArmingConditions());
        }
    }
    
    public void addEntity(Entity e) {
        EntityComponent ec = null;

        synchronized (entityLock) {
            entityChanged = true;
            
            // Lot's of things can have a camera
            if ((ec = e.getComponent(CameraComponent.class)) != null) {
                cameras.add(ec);
                camerasChanged = true;
            }

            // An entity is one of the following - for now.
            if ((ec = e.getComponent(SceneComponent.class)) != null) {
                scenes.add(ec);
                scenesChanged = true;
            } else if ((ec = e.getComponent(AvatarComponent.class)) != null) {
                avatars.add(ec);
                avatarsChanged = true;
            } else if ((ec = e.getComponent(HudComponent.class)) != null) {
                huds.add(ec);
                hudsChanged = true;
            }
            
            // Add it to our list of spaces
            if (e instanceof Space) {
                spaces.add(e);
                spacesChanged = true;
            }
        }
    }
    
    /**
     * Add a node to be updated
     */
    public void addToUpdateList(Spatial spatial) {
        updateList.add(spatial);
    }
    
    /**
     * Get the jme renderer
     */
    public com.jme.renderer.Renderer getJMERenderer() {
        return (jmeRenderer);
    }
    
    /**
     * Check for changes in any entities
     */
    private void checkForEntityChanges() {
        synchronized (entityLock) {
            if (entityChanged) {
                if (scenesChanged) {
                    processScenesChanged();
                    scenesChanged = false;
                }
                if (camerasChanged) {
                    processCamerasChanged();
                    camerasChanged = false;
                }
                if (hudsChanged) {
                    processHudsChanged();
                    hudsChanged = false;
                }
                if (avatarsChanged) {
                    processAvatarsChanged();
                    avatarsChanged = false;
                }
                if (spacesChanged) {
                    processSpacesChanged();
                    spacesChanged = false;
                }
                entityChanged = false;
            }
        }
    }
    
    /**
     * Check for camera changes
     */
    private void processCamerasChanged() {
        int len = 0;
        CameraComponent camera = null;
        
        // Minimize the number of additions to the cameraUpdateList
        // First, let's look for removals
        len = renderCameras.size();
        for (int i=0; i<len;) {
            camera = (CameraComponent) renderCameras.get(i);
            if (cameras.contains(camera)) {
                // move on to the next
                i++;
            } else {
                // remove the scene, this will shift things down
                renderCameras.remove(camera);
                len--;
            }
        }
        
        // Now let's look for additions
        for (int i=0; i<cameras.size(); i++) {
            camera = (CameraComponent) cameras.get(i);
            if (!renderCameras.contains(camera)) {
                renderCameras.add(camera);
                cameraUpdateList.add(camera);
            }
        }
        
        // Just a sanity check
        if (cameras.size() != renderCameras.size()) {
            System.out.println("Error, Camera sizes differ");
        }
               
    }
    
    /**
     * Check for scene changes
     */
    private void processScenesChanged() {
        int len = 0;
        SceneComponent scene = null;
        
        // Minimize the numner of additions to the updateList
        // First, let's look for removals
        len = renderScenes.size();
        for (int i=0; i<len;) {
            scene = (SceneComponent) renderScenes.get(i);
            if (scenes.contains(scene)) {
                // move on to the next
                i++;
            } else {
                // remove the scene, this will shift things down
                renderScenes.remove(scene);
                len--;
            }
        }
        
        // Now let's look for additions
        for (int i=0; i<scenes.size(); i++) {
            scene = (SceneComponent) scenes.get(i);
            if (!renderScenes.contains(scene)) {
                renderScenes.add(scene);
                sceneUpdateList.add(scene);
            }
        }
        
        // Just a sanity check
        if (scenes.size() != renderScenes.size()) {
            System.out.println("Error, Scene sizes differ");
        }
       
    }
      
    /**
     * Check for scene changes
     */
    private void processSpacesChanged() {
        int len = 0;
        Entity space = null;
        
        // Minimize the numner of additions to the updateList
        // First, let's look for removals
        len = collisionSpaces.size();
        for (int i=0; i<len;) {
            space = (Space) collisionSpaces.get(i);
            if (spaces.contains(space)) {
                // move on to the next
                i++;
            } else {
                // remove the scene, this will shift things down
                collisionSpaces.remove(space);
                len--;
            }
        }
        
        // Now let's look for additions
        for (int i=0; i<spaces.size(); i++) {
            space = (Space) spaces.get(i);
            if (!collisionSpaces.contains(space)) {
                collisionSpaces.add(space);
            }
        }
        
        // Just a sanity check
        if (spaces.size() != collisionSpaces.size()) {
            System.out.println("Error, Spaces sizes differ");
        }
       
    }
    
    /**
     * Set desired frame rate
     */
    public void setDesiredFrameRate(int fps) {
        desiredFrameRate = fps;
        desiredFrameTime = 1000000000/desiredFrameRate;
    }
              
    /**
     * Set a listener for frame rate updates
     */
    public void setFrameRateListener(FrameRateListener l, int frequency) {
        listenerCountdown = frequency;
        listenerStarttime = System.nanoTime();
        frameRateListenerFrequency = frequency;
        frameRateListener = l;
    }
    
    /**
     * Turn on key input tracking
     */
    public void trackKeyInput(Object listener) {
        canvas.addKeyListener((KeyListener)listener);
    }
    
    /**
     * turn off key input tracking
     */
    public void untrackKeyInput(Object listener) {
        canvas.removeKeyListener((KeyListener)listener);
    }
        
    /**
     * turn on mouse input tracking
     */
    public void trackMouseInput(Object listener) {
        canvas.addMouseListener((MouseListener) listener);
        canvas.addMouseMotionListener((MouseMotionListener) listener);
        canvas.addMouseWheelListener((MouseWheelListener) listener);
    }
        
    /**
     * turn off mouse input tracking
     */
    public void untrackMouseInput(Object listener) {
        canvas.removeMouseListener((MouseListener) listener);
        canvas.removeMouseMotionListener((MouseMotionListener) listener);
        canvas.removeMouseWheelListener((MouseWheelListener) listener);
    }
    
    /**
     * Check for avatar changes
     */
    private void processAvatarsChanged() {
        
    }
    
    /**
     * Check for hud changes
     */
    private void processHudsChanged() {
        
    }
    
    class MyImplementor extends SimpleCanvasImpl {
        /**
         * The Renderer to notify
         */
        private Renderer renderer = null;

        public MyImplementor(Renderer renderer, int width, int height) {
            super(width, height);
            this.renderer = renderer;
        }

        public void simpleSetup() {
            //renderer.canvasIsReady();
            System.out.println("In simple setup: ");
        }

        public void simpleUpdate() {
            renderer.canvasIsReady();
            System.out.println("In simple update");
        }
    }
    
    public void findCollisions(Spatial sp, CollisionResults cr) {
        SceneComponent sc = null;
        Space space = null;
        
        // Right now, just iterate over the spaces
        synchronized (collisionSpaces) {
            for (int i=0; i<collisionSpaces.size(); i++) {
                space = (Space)collisionSpaces.get(i);
                sc = (SceneComponent)space.getComponent(SceneComponent.class);
                //System.out.println("Space Bounds: " + sc.getSceneRoot().getWorldBound());
                //System.out.println("Spatial Bounds: " + sp.getWorldBound());
                sc.getSceneRoot().findCollisions(sp, cr);
            }
        }
    }

}
