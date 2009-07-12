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
package org.jdesktop.wonderland.client.jme;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.scenario.animation.Clip;
import com.sun.scenario.animation.Interpolators;
import com.sun.scenario.animation.TimingTarget;
import java.awt.event.ActionEvent;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
import org.jdesktop.wonderland.client.jme.login.JmeLoginUI;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.JBulletPhysicsSystem;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.PhysicsSystem;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.common.ThreadManager;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.MainFrame.ServerURLListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.LoginManager;

/**
 *
 */
public class JmeClientMain {

    private static final Logger logger =
            Logger.getLogger(JmeClientMain.class.getName());
    public static final String SERVER_URL_PROP = "sgs.server";
    /** The frame of the Wonderland client window. */
    private static MainFrame frame;

    // standard properties
    private static final String PROPS_URL_PROP = "run.properties.file";
    private static final String CONFIG_DIR_PROP = "wonderland.client.config.dir";
    private static final String DESIRED_FPS_PROP = "wonderland.client.fps";
    private static final String WINDOW_SIZE_PROP = "wonderland.client.windowSize";

    // default values
    private static final String SERVER_URL_DEFAULT = "http://localhost:8080";
    private static final String DESIRED_FPS_DEFAULT = "30";
    private static final String WINDOW_SIZE_DEFAULT = "800x600";
    private int desiredFrameRate = Integer.parseInt(DESIRED_FPS_DEFAULT);
    /**
     * The width and height of our 3D window
     */
    private int width = 800;
    private int height = 600;
    // the current Wonderland login and session
    private JmeLoginUI login;
    private JmeClientSession curSession;

    // keep tack of whether we are currently logging out
    private boolean loggingOut = false;

    public JmeClientMain(String[] args) {
        // process command line arguments
        processArgs(args);

        // load properties in a properties file
        URL propsURL = getPropsURL();
        loadProperties(propsURL);

        // set up the context
        ClientContextJME.setClientMain(this);

        String windowSize = System.getProperty(WINDOW_SIZE_PROP, WINDOW_SIZE_DEFAULT);
        try {
            if (windowSize.equalsIgnoreCase("fullscreen")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] gs = ge.getScreenDevices();
                if (gs.length > 1) {
                    logger.warning("Fullscreen using size of first screen");
                }
                GraphicsConfiguration gc = gs[0].getDefaultConfiguration();
                Rectangle size = gc.getBounds();
                width = size.width;
                height = size.height; // -50 hack for current swing decorations
            } else {
                String sizeWidth = windowSize.substring(0, windowSize.indexOf('x'));
                String sizeHeight = windowSize.substring(windowSize.indexOf('x') + 1);
                width = Integer.parseInt(sizeWidth);
                height = Integer.parseInt(sizeHeight);
            }
        } catch (Exception e) {
            logger.warning(WINDOW_SIZE_PROP + " error, should be of the form 640x480 (or fullscreen), instead of the current " + windowSize);
        }

        // make sure the server URL is set
        String serverURL = System.getProperty(SERVER_URL_PROP);
        if (serverURL == null) {
            serverURL = SERVER_URL_DEFAULT;
            System.setProperty(SERVER_URL_PROP, serverURL);
        }

        // HUGE HACK ! Force scenario to initialize so menus work correctly
        // (work around for scenario bug)
        Clip clip2 = Clip.create(1000, new TimingTarget() {

            public void timingEvent(float arg0, long arg1) {
            }

            public void begin() {
            }

            public void end() {
            }

            public void pause() {
            }

            public void resume() {
            }
        });
        clip2.setInterpolator(Interpolators.getEasingInstance(0.4f, 0.4f));
        clip2.start();
        // End HUGE HACK.


        WorldManager worldManager = ClientContextJME.getWorldManager();

        String requestedFPS = System.getProperty(DESIRED_FPS_PROP, DESIRED_FPS_DEFAULT);
        if (requestedFPS != null) {
            try {
                desiredFrameRate = Integer.parseInt(requestedFPS);
            } catch (NumberFormatException e) {
                // No action required, the default has already been set.
                logger.warning(DESIRED_FPS_PROP + " property format error for '" + requestedFPS + "', using default");
            }
        }
        worldManager.getRenderManager().setDesiredFrameRate(desiredFrameRate);

        createUI(worldManager);

        // Register our loginUI for login requests
        login = new JmeLoginUI(frame);
        LoginManager.setLoginUI(login);

        // add a listener that will be notified when the user selects a new
        // server
        frame.addServerURLListener(new ServerURLListener() {

            public void serverURLChanged(final String serverURL) {
                // run in a new thread so we don't block the AWT thread
                new Thread(ThreadManager.getThreadGroup(), new Runnable() {

                    public void run() {
                        try {
                            loadServer(serverURL);
                        } catch (IOException ioe) {
                            logger.log(Level.WARNING, "Error connecting to " +
                                    serverURL, ioe);
                        }
                    }
                }).start();
            }

            public void logout() {
                new Thread(ThreadManager.getThreadGroup(), new Runnable() {

                    public void run() {
                        JmeClientMain.this.logout();
                    }
                }).start();
            }
        });

        JMenuItem physicsMI = new JCheckBoxMenuItem("Physics Enabled");
        physicsMI.setEnabled(false);
        physicsMI.setSelected(false);
        physicsMI.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PhysicsSystem phySystem = ClientContextJME.getPhysicsSystem(curSession.getSessionManager(), "Default");
                if (phySystem instanceof JBulletPhysicsSystem) {
                    ((JBulletPhysicsSystem) phySystem).setStarted(((JCheckBoxMenuItem) e.getSource()).isSelected());
                } else {
                    logger.severe("Unsupported physics system " + phySystem);
                }
            }
        });
        frame.addToEditMenu(physicsMI, 3);

        // connect to the default server
        try {
            loadServer(serverURL);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error connecting to default server " +
                    serverURL, ioe);
        }

    }

    /**
     * Move the client to the given location
     * @param serverURL the url of the server to go to, or null to stay
     * on the current server
     * @param location the location to go to, or null to go to the default
     * location on the given server
     * @param look the direction to look in, or null to look in the default
     * direction
     * @throws IOException if there is an error going to the new location
     */
    public void gotoLocation(String serverURL, Vector3f translation,
            Quaternion look)
            throws IOException {
        if (serverURL == null) {
            // get the server from the current session
            if (curSession == null) {
                throw new IllegalStateException("No server");
            }

            serverURL = curSession.getSessionManager().getServerURL();
        }

        // see if we need to change servers
        if (curSession != null &&
                serverURL.equals(curSession.getSessionManager().getServerURL())) {
            curSession.getLocalAvatar().localMoveRequest(translation, look);
        } else {
            loadServer(serverURL, translation, look);
        }
    }

    protected void loadServer(String serverURL) throws IOException {
        loadServer(serverURL, null, null);
    }

    protected void loadServer(String serverURL, Vector3f translation,
            Quaternion look)
            throws IOException {
        logger.info("[JmeClientMain] loadServer " + serverURL);

        logout();

        // get the login manager for the given server
        ServerSessionManager lm = LoginManager.getSessionManager(serverURL);

        // Register default collision and physics systems for this session
//        JBulletDynamicCollisionSystem collisionSystem = (JBulletDynamicCollisionSystem)
//                ClientContextJME.getWorldManager().getCollisionManager().loadCollisionSystem(JBulletDynamicCollisionSystem.class);
//        JBulletPhysicsSystem physicsSystem = (JBulletPhysicsSystem)
//                ClientContextJME.getWorldManager().getPhysicsManager().loadPhysicsSystem(JBulletPhysicsSystem.class, collisionSystem);
        JMECollisionSystem collisionSystem = (JMECollisionSystem) ClientContextJME.getWorldManager().getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        ClientContextJME.addCollisionSystem(lm, "Default", collisionSystem);
//        ClientContextJME.addPhysicsSystem(lm, "Default", physicsSystem);

        // set the initial position, which will bne sent with the initial
        // connection properties of the cell cache connection
        login.setInitialPosition(translation, look);

        // create a new session
        try {
            curSession = lm.createSession(login);
        } catch (LoginFailureException lfe) {
            IOException ioe = new IOException("Error connecting to " +
                    serverURL);
            ioe.initCause(lfe);
            throw ioe;
        }

        // make sure we logged in successfully
        if (curSession == null) {
            logger.log(Level.WARNING, "Unable to connect to session");
            return;
        }

        frame.connected(true);

        // Listen for session disconnected and remove session physics and collision systems
        curSession.addSessionStatusListener(new SessionStatusListener() {

            public void sessionStatusChanged(WonderlandSession session, Status status) {
                if (status == Status.DISCONNECTED) {
                    ClientContextJME.removeAllPhysicsSystems(session.getSessionManager());
                    ClientContextJME.removeAllCollisionSystems(session.getSessionManager());

                    // update the UI for logout
                    boolean inLogout;
                    synchronized (JmeClientMain.this) {
                        inLogout = loggingOut;
                    }

                    if (!inLogout) {
                        // if we didn't initiate the logout through the
                        // logout() method, then this is an unexpected
                        // logout.  Clean up by calling the logout() method,
                        // then attempt to reconnect
                        // reconnect dialog
                        final ServerSessionManager mgr = curSession.getSessionManager();

                        logger.warning("[JmeClientMain] unexpected logout!");

                        logout();

                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                ReconnectFrame rf = new ReconnectFrame(JmeClientMain.this, mgr);
                                rf.pack();
                                rf.setLocationRelativeTo(JmeClientMain.getFrame().getFrame());
                                rf.setVisible(true);
                            }
                        });

                    } else {
                        synchronized (JmeClientMain.this) {
                            loggingOut = false;
                        }
                    }
                }
            }
        });

        // set the primary login manager and session
        LoginManager.setPrimary(lm);
        lm.setPrimarySession(curSession);
        frame.setServerURL(serverURL);
    }

    protected void logout() {
        logger.warning("[JMEClientMain] log out");

        // disconnect from the current session
        if (curSession != null) {
            if (curSession.getStatus() == Status.CONNECTED) {
                synchronized (this) {
                    loggingOut = true;
                }
            }

            curSession.getCellCache().unloadAll();

            curSession.logout();
            curSession = null;
            frame.connected(false);

            // notify listeners that there is no longer a primary server
            LoginManager.setPrimary(null);
        }
    }

    protected URL getPropsURL() {
        String propURLStr = System.getProperty(PROPS_URL_PROP);
        try {
            URL propsURL;

            if (propURLStr == null) {
                String configDir = System.getProperty(CONFIG_DIR_PROP);
                if (configDir == null) {
                    File userDir = new File(System.getProperty("user.dir"));
                    configDir = userDir.toURI().toURL().toString();
                }

                // use the default
                URL configDirURL = new URL(configDir);
                propsURL = new URL(configDirURL, "run-client.properties");
            } else {
                propsURL = new URL(propURLStr);
            }

            return propsURL;
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to load properties", ioe);
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (Webstart.isWebstart()) {
            Webstart.webstartSetup();
        }

        JmeClientMain worldTest = new JmeClientMain(args);

    }

    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i + 1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
                i++;
            }

            if (args[i].equals("-p")) {
                System.setProperty(PROPS_URL_PROP, "file:" + args[i + 1]);
                i++;
            }
        }
    }

    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm) {

        frame = new MainFrameImpl(wm, width, height);
        // center the frame
        frame.getFrame().setLocationRelativeTo(null);

        // show frame
        frame.getFrame().setVisible(true);

        JPanel canvas3D = frame.getCanvas3DPanel();
        ViewManager.initialize(canvas3D.getWidth(), canvas3D.getHeight()); // Initialize an onscreen view

        // This call will block until the render buffer is ready, for it to become
        // ready the canvas3D must be visible
        ViewManager.getViewManager().attachViewCanvas(canvas3D);

        // Initialize the input manager.
        // Note: this also creates the view manager.
        // TODO: low bug: we would like to initialize the input manager BEFORE frame.setVisible.
        // But if we create the camera before frame.setVisible the client window never appears.
        CameraComponent cameraComp = ViewManager.getViewManager().getCameraComponent();
        InputManager inputManager = ClientContext.getInputManager();
        inputManager.initialize(frame.getCanvas(), cameraComp);

        // Default Policy: Enable global key and mouse focus everywhere
        // Note: the app base will impose its own (different) policy later
        inputManager.addKeyMouseFocus(inputManager.getGlobalFocusEntity());

        /* Note: Example of global key and mouse event listener
        InputManager3D.getInputManager().addGlobalEventListener(
        new EventClassFocusListener () {
        private final Logger logger = Logger.getLogger("My Logger");
        public Class[] eventClassesToConsume () {
        return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
        }
        public void commitEvent (Event event) {
        // NOTE: to test, change the two logger.fine calls below to logger.warning
        if (event instanceof KeyEvent3D) {
        if (((KeyEvent3D)event).isPressed()) {
        logger.fine("Global listener: received key event, event = " + event );
        }
        } else {
        logger.fine("Global listener: received mouse event, event = " + event);
        MouseEvent3D mouseEvent = (MouseEvent3D) event;
        System.err.println("Event pickDetails = " + mouseEvent.getPickDetails());
        System.err.println("Event entity = " + mouseEvent.getEntity());
        }
        }
        });
         */
        frame.setDesiredFrameRate(desiredFrameRate);
    }

    /**
     * Returns the frame of the Wonderland client window.
     */
    public static MainFrame getFrame() {
        return frame;
    }

    /**
     * Set the main frame
     * @param frame the new main frame
     */
    public static void setFrame(MainFrame frame) {
        JmeClientMain.frame = frame;
    }

    /**
     * Load system properties and properties from the named file
     */
    protected void loadProperties(URL propsURL) {
        // load the given file
        if (propsURL != null) {
            try {
                System.getProperties().load(propsURL.openStream());
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Error reading properties from " +
                        propsURL, ioe);
            }
        }
    }
}
