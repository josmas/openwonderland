/**
 * Project Looking Glass
 *
 * $RCSfile: Main.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.160 $
 * $Date: 2007/12/06 21:09:06 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import java.util.Iterator;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.Timer;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import org.jdesktop.j3d.utils.loaders.x3d.X3DLoader;
import org.jdesktop.lg3d.appkit.Lg3dConnector;
import org.jdesktop.lg3d.media.jmf.audio.joal.JOAL;
import org.jdesktop.lg3d.media.jmf.audio.joal.JOALOptionsFrame;
import org.jdesktop.lg3d.wg.event.LgEvent;
import org.jdesktop.lg3d.wg.event.LgEventConnector;
import org.jdesktop.lg3d.wg.event.LgEventListener;
import org.jdesktop.lg3d.wg.event.LgEventSource;
import org.jdesktop.lg3d.wonderland.appshare.SharedApp2DCellConfig;
import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.config.client.AvatarClientConfig;
import org.jdesktop.lg3d.wonderland.config.client.WonderlandClientConfig;
import org.jdesktop.lg3d.wonderland.config.common.WonderlandConfig;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.scenemanager.AvatarControlBehavior;
import org.jdesktop.lg3d.wonderland.scenemanager.HelpJFrame;
import org.jdesktop.lg3d.wonderland.scenemanager.InputConfigJFrame;
import org.jdesktop.lg3d.wonderland.scenemanager.MapJFrame;
import org.jdesktop.lg3d.wonderland.scenemanager.PerformanceJFrame;
import org.jdesktop.lg3d.wonderland.scenemanager.events.ChatMessageEvent;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelControllerTest;
import org.jdesktop.lg3d.wonderland.darkstar.client.SipStarter;
import org.jdesktop.lg3d.wonderland.scenemanager.avatar.AvatarConfigurator;
import org.jdesktop.lg3d.wonderland.scenemanager.jinput.JInput;
import org.jdesktop.lg3d.wonderland.appshare.AppConfigX11;
import org.jdesktop.lg3d.wonderland.appshare.AppConfigX11Example;
import org.jdesktop.lg3d.wonderland.appshare.AppConfigX11Examples;
import org.jdesktop.lg3d.wonderland.config.client.WonderlandClientConfigGUI;
import org.jdesktop.lg3d.wonderland.config.common.WonderlandConfigGUI;
import org.jdesktop.lg3d.wonderland.scenemanager.AvatarCamera;
import org.jdesktop.lg3d.wonderland.appshare.MasterLauncher;
import org.jdesktop.lg3d.wonderland.appshare.AppMonitor;
import org.jdesktop.lg3d.wonderland.appshare.AppReporter;
import org.jdesktop.lg3d.wonderland.scenemanager.WonderlandUniverseFactory;
import com.sun.j3d.loaders.Scene;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import org.jdesktop.lg3d.displayserver.EventProcessor;
import org.jdesktop.lg3d.displayserver.EventProcessor;
import org.jdesktop.lg3d.wonderland.darkstar.client.SipListener;
import org.jdesktop.lg3d.wonderland.darkstar.socket.ClientSocket;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUDFactory;
import org.jdesktop.lg3d.wonderland.appshare.FrameSyncImage;
import org.jdesktop.lg3d.wonderland.appshare.remwin.Client;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.ServerMasterClientReadyMessage.AppRecord;
import org.jdesktop.lg3d.wonderland.scenemanager.HelpTopic;
import org.jdesktop.lg3d.wonderland.scenemanager.avatar.AvatarNameTagEvent;

/**
 *
 * @author  paulby
 */
public class Main extends javax.swing.JFrame {
    
    private static Logger logger = Logger.getLogger("wonderland.main");
    
    private Color chatNameColor = new Color(0,0,204);
    private Color chatTextColor = new Color(0,0,0);
    
    private boolean fullScreen = WonderlandClientConfig.getDefault().isFullScreen();
    private int screenWidth = WonderlandClientConfig.getDefault().getScreenWidth();
    private int screenHeight = WonderlandClientConfig.getDefault().getScreenHeight();
    private String workingDir;
    
    private boolean localXAppsSupported = clientPlatformIsUnix();
    
    private WeakReference<PerformanceJFrame> assetManagerFrame=null;
    
    private ChannelControllerTest controllerTest;
    
    private List<AppConfigX11Example> serverApps = new ArrayList<AppConfigX11Example>(0);
    
    private void populateAudioQualityMenu() {
        ButtonGroup audioQualityButtons = new ButtonGroup();

        for (SipStarter.AudioQuality quality: SipStarter.AudioQuality.values()) {

            final SipStarter.AudioQuality fq = quality;
            JRadioButtonMenuItem mitem = new JRadioButtonMenuItem(new AbstractAction(quality.toString()) {
                public void actionPerformed(ActionEvent arg0) {
                    setAudioQuality(fq);
                }
            });

            audioQualityMenu.add(mitem);
            audioQualityButtons.add(mitem);

            if (WonderlandClientConfig.getDefault().getAudioQuality() == quality) {
                mitem.setSelected(true);
            }
        }
    }

    private void setAudioQuality(SipStarter.AudioQuality quality) {
        try {
            ChannelController.getController().disconnectSoftphone();
            SipStarter.getInstance().setAudioQuality(quality);
            ChannelController.getController().connectSoftphone();
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error setting audio quality", ioe);
        }
    }

    private void populateSharedAppsMenu (Iterator<AppConfigX11Example> appIterator) {
        
        if (!localXAppsSupported) {
            localAppsMenu.setEnabled(false);
        } 
        
        while (appIterator.hasNext()) {
            AppConfigX11Example appConfig = appIterator.next();

            javax.swing.JMenuItem mitem = appConfig.getMenuItem();

            switch(appConfig.getAppMenu()) {
            case CONFERENCE_ROOM :
                 conferenceRoomMenu.add(mitem);
                 if (!conferenceRoomMenu.isEnabled())
                     conferenceRoomMenu.setEnabled(true);
                 break;
            case DEMO_FLOOR :
                demoFloorMenu.add(mitem);
                 if (!demoFloorMenu.isEnabled())
                     demoFloorMenu.setEnabled(true);
                break;
            case LOCAL :
                localAppsMenu.add(mitem);
                 if (!localAppsMenu.isEnabled())
                     localAppsMenu.setEnabled(true);
                break;
            case TEAM_ROOM :
                teamRoomMenu.add(mitem);
                 if (!teamRoomMenu.isEnabled())
                     teamRoomMenu.setEnabled(true);
                break;
            }
        }
        
    }

    private void populateHelpMenu() {
        List<HelpTopic> helpTopics = HelpTopic.getHelpTopics();
        for (final HelpTopic topic : helpTopics) {
            Action action = new AbstractAction(topic.getName()) {
                public void actionPerformed(ActionEvent arg0) {
                    new HelpJFrame(topic.getName(), topic.getFile()).setVisible(true);
                }
            };
            
            JMenuItem topicMenuItem = new JMenuItem(action);
            helpMenu.add(topicMenuItem);
        }
    }
    
    private void chatHistoryAppend(String str) {
        chatHistory.append(str);
        chatHistory.setCaretPosition(chatHistory.getText().length());
    }
    
    public void toggleMenuBar() {
        if (getJMenuBar() == null) {
            setJMenuBar(mainMenuBar);
        } else {
            setJMenuBar(null);
            
        }
        validate();
        
        logger.info("Toggling main menubar: " + ((getJMenuBar() == null) ? "INVISIBLE" : "VISIBLE"));
    }
    
    private static boolean clientPlatformIsUnix () {
	String osName = System.getProperty("os.name");
	return "Linux".equals(osName) || "SunOS".equals(osName);
    }

    public Main() {
        System.setProperty("lg.wonderland", "true");  // Required so lg3d setup is correct
        
        if (screenWidth <= 0) {
            screenWidth =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
        }
        
        if (screenHeight <= 0) {
            screenHeight =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
        }

        // Check if we are in webstart and setup appropriately.
        Webstart.setup(this, localXAppsSupported);
        
        if (fullScreen)
            setUndecorated(true);
        
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        initComponents();
        
        mapViewCheckBoxMenuItem.setVisible(false);
        
        SipStarter.getInstance().addSipListener(new SipListener() {
            public void sipNotification(boolean b) {
                updateSelected(softphoneCheckBoxMenuItem, b);
	    }
            public void softphoneMuted(boolean m) {}
            public void softphoneConnected(boolean c) {}
            public void softphoneExited() {
                updateSelected(softphoneCheckBoxMenuItem, false);
	    }
        });
        
        chatPanelCheckBoxMenuItem.setSelected(
                WonderlandClientConfig.getDefault().isChatPanelVisible());
        
        controllerTest = new ChannelControllerTest(chatHistory);
        
        // Dont display the chat panel if set
        if (!chatPanelCheckBoxMenuItem.getState())
            remove(southPanel);
        
        if (fullScreen) {
            // To run fullscreen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setSize(screenSize);
        } else
            setSize(screenWidth, screenHeight);
        
        validate();
        
	populateSharedAppsMenu(AppConfigX11Examples.iterator());
        populateAudioQualityMenu();
	populateDebugSharedAppsMenu(debugSharedAppsMenu);
        populateHelpMenu();
        pruneMenus();

        if (!JOAL.isAvailable())
            joalConfigMenuItem.setEnabled(false);
        
        if (!JInput.isAvailable())
            toggleJInputMenuItem.setEnabled(false);
        
        if (!WonderlandConfig.getDefault().isVoiceBridgeEnabled())
            softphoneCheckBoxMenuItem.setEnabled(false);
        
        robotMenuItem.setState(WonderlandClientConfig.getDefault().isRobot());
        AvatarControlBehavior.getAvatarControlBehavior().setRobotEnabled(robotMenuItem.getState());
        
        if (localXAppsSupported) {
            // Install the Demo Center and Right Firefox profiles into ~/.mozilla
            firefoxSetup();
        } else {
            localAppsMenu.setEnabled(false);
            rdpClientMenuItem.setEnabled(false);
        }
        SharedApp2DCellConfig.getDefault();
                
        System.setProperty("lg.configclass", "org.jdesktop.lg3d.wonderland.scenemanager.WonderlandConfigControl");
        System.setProperty("lg.platformConfigClass", "org.jdesktop.lg3d.wonderland.scenemanager.WonderlandPlatformConfig");
        
        Lg3dConnector connector = Lg3dConnector.getConnector(new StartupListener());
        
        centerPanel.add("Center", connector);
        
    }
    
    /**
     *  Traverse the menus and disable (exp) and (dev) items as necessary
     */
    private void pruneMenus() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boolean devEnabled = System.getProperty("wonderland.developerfeatures", "false").equalsIgnoreCase("true");
                boolean expEnabled = System.getProperty("wonderland.experimentalfeatures", "false").equalsIgnoreCase("true");

                if (!devEnabled) {
                    devToolsMenu.setVisible(false);
                }
                
                pruneMenu(mainMenuBar.getSubElements(), devEnabled, expEnabled);
            }           
        });
    }
    
    /**
     *  Traverse the menu and disable (exp) and (dev) items as necessary
     */
    private void pruneMenu(MenuElement[] elements, boolean devEnabled, boolean expEnabled) {
        // TODO Not I18N friendly
        
        for(MenuElement element : elements) {
            if (element instanceof JMenuItem) {
                JMenuItem item = (JMenuItem)element;
                if (!devEnabled && item.getText().endsWith("(dev)")) {
                     item.setEnabled(false);
                } else if (!expEnabled && item.getText().endsWith("(exp)")) {
                    item.setEnabled(false);
                }
            }
            if (element.getSubElements()!=null) {
                pruneMenu(element.getSubElements(), devEnabled, expEnabled);
                if (element.getSubElements().length==0) {
                    if (element instanceof JPopupMenu) { 
                        // This JMenu has no children
                        ((JPopupMenu)element).setEnabled(false);
                    } else if (element instanceof JMenu) {
                        // This JMenu has no children
                        ((JMenu)element).setEnabled(false);
                    }
                }
            }
        }
    }
    
    public void updateComponents() {
        updateWindowMenuItems();
    }
    
    private void updateSelected(JCheckBoxMenuItem item, boolean selected) {
        if (item.isSelected() != selected) {
            item.setSelected(selected);
        }
    }
    
    public void updateWindowMenuItems() {
        boolean selected;
        
        if (softphoneCheckBoxMenuItem.isEnabled()) {
            updateSelected(softphoneCheckBoxMenuItem,
                    SipStarter.getInstance().softphoneIsVisible());
        }
        
        updateSelected(mapViewCheckBoxMenuItem, MapJFrame.getMap().isVisible());
        
        if ((assetManagerFrame != null) && (assetManagerFrame.get() !=null))
            updateSelected(performancePanelCheckBoxMenuItem, assetManagerFrame.get().isVisible());
        
        updateSelected(userListCheckBoxMenuItem,
                UserListJFrame.getUserListJFrame().isVisible());
    }
    
    private void openJFrameWithinScreen(JFrame f, int x, int y, boolean visible) {
        Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (x < 0 || x > screenSize.width) {
            x = 0;
        }
        if (y < 0 || y > screenSize.height) {
            y = 0;
        }
        f.setLocation(x,y);
        f.setVisible(visible);
        // TODO: Set focus back to Main window and add focus traversal strategy
        // to default focus into Wonderland window
    }
    
    /** Called after login succeeds, to enable the user interface */
    public void enableMainWindow() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                openPerformanceJFrame(
                        WonderlandClientConfig.getDefault().isPerformancePanelVisible());
                
                
                
                JFrame f = UserListJFrame.getUserListJFrame();
                int x = WonderlandClientConfig.getDefault().getUserListX();
                int y = WonderlandClientConfig.getDefault().getUserListY();
                openJFrameWithinScreen(f, x, y, WonderlandClientConfig.getDefault().isUserListVisible());
                // AppLauncher.startApp(org.jdesktop.lg3d.apps.jmfplayer.JMFPlayer.class);
            }
        });
        
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                MapJFrame f = MapJFrame.getMap();
//                f.createFrame();
//                int x = WonderlandClientConfig.getDefault().getMapViewX();
//                int y = WonderlandClientConfig.getDefault().getMapViewY();
//                openJFrameWithinScreen(f, x, y, WonderlandClientConfig.getDefault().isMapViewVisible());
//            }
//        });
        
        updateComponents();
    }
    
    class StartupListener implements Lg3dConnector.Lg3dStartupListener {
        public void showPanel() {
            setVisible(true);
        }
        
        public void startupComplete() {
            LgEventConnector.getLgEventConnector().addListener(LgEventSource.ALL_SOURCES, new ChatMessageListener());
            
            if (System.getProperty("wonderland.benchmark")!=null) {
                ActionListener taskPerformer = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        // Transition from startup to normal behavior scheduling
                        AvatarControlBehavior.getAvatarControlBehavior().scheduleUpdate();
                    }
                };
                new Timer(20*1000, taskPerformer).start();
                
                AvatarClientConfig.getDefault().setUsername(System.getProperty("wonderland.benchmark"));
            }
            
            if (WonderlandClientConfig.getDefault().getLogin()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        JFrame f = LoginDialog.getLoginDialog();
                    
                        // center
                        int x = (screenWidth / 2) - (f.getSize().width / 2);
                        int y = (screenHeight / 2) - (f.getSize().height / 2);
                        f.setLocation(x, y);
                    
                        if (System.getProperty("wonderland.benchmark")!=null) {
                            ((LoginDialog)f).doLogin();
                        } else {
                            f.setVisible(
                                    WonderlandClientConfig.getDefault().getLogin());
                        }
                    }
                });
            }
            
            // if we didn't show the login dialog, enable everything here
            if (!WonderlandClientConfig.getDefault().getLogin()) {
                enableMainWindow();
            }
            
            // Auto start all demo apps
//            if (localXAppsSupported &&
//                System.getProperty("wonderland.autostart.x.apps","false").equalsIgnoreCase("true")) {
//                (new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException ex) {
//                            ex.printStackTrace();
//                        }
//                        startApp("Terminal");
//                        // startDemoApps();
//                    }
//                }).start();
//            }
        }
    }
    
    
    /**
     * Parse the string into a position and lookDirection. If there are no syntax errors
     * in the string returns true, else returns false
     *
     * String format ( <float>, <float>, <float> ) (<float>, <float>, <float>)
     * first tuple is position, second is lookDirection
     */
    private void parseGotoLocation(String locationStr, Point3f pos, Vector3f lookDirection) throws GotoParserException {
        StreamTokenizer tok = new StreamTokenizer(new StringReader(locationStr));
        
        parseTuple3f(tok, pos);
        parseTuple3f(tok, lookDirection);
    }
    
    private void parseTuple3f(StreamTokenizer tok, Tuple3f t3f) throws GotoParserException {
        parserExpectToken(tok, '(');
        t3f.x = parserExpectFloat(tok);
        parserExpectToken(tok, ',');
        t3f.y = parserExpectFloat(tok);
        parserExpectToken(tok, ',');
        t3f.z = parserExpectFloat(tok);
        parserExpectToken(tok, ')');
    }
    
    private void parserExpectToken(StreamTokenizer tok, char token) throws GotoParserException {
        try {
            int type = tok.nextToken();
            if (tok.ttype!=token)
                throw new GotoParserException("Expecting (, got "+tok.sval);
        } catch(Exception ex) {
            throw new GotoParserException("Expected number, got exception "+ex.getMessage());
        }
    }
    
    private float parserExpectFloat(StreamTokenizer tok) throws GotoParserException {
        try {
            int type = tok.nextToken();
            if (tok.ttype!=StreamTokenizer.TT_NUMBER)
                throw new GotoParserException("Expected number, got "+tok.sval);
            float ret = (float)tok.nval;
            return ret;
        } catch(Exception ex) {
            throw new GotoParserException("Expected number, got exception "+ex.getMessage());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gotoLocationDialog = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        gotoTF = new javax.swing.JTextField();
        cameraButtonGroup = new javax.swing.ButtonGroup();
        fpsButtonGroup = new javax.swing.ButtonGroup();
        centerPanel = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        southPanel = new javax.swing.JPanel();
        chatEntry = new javax.swing.JTextField();
        jScrollPane = new javax.swing.JScrollPane();
        chatHistory = new javax.swing.JTextArea();
        dividerPanel = new javax.swing.JPanel();
        jSeparator = new javax.swing.JSeparator();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        cameraModeMenu = new javax.swing.JMenu();
        cameraModeFirstPersonMI = new javax.swing.JRadioButtonMenuItem();
        cameraModeThirdPersonMI = new javax.swing.JRadioButtonMenuItem();
        cameraModeOrbitMI = new javax.swing.JRadioButtonMenuItem();
        showHUDCheckBox = new javax.swing.JCheckBoxMenuItem();
        viewPropertiesMenuItem = new javax.swing.JMenuItem();
        maxFrameRateMenu = new javax.swing.JMenu();
        fps120MI = new javax.swing.JRadioButtonMenuItem();
        fps30MI = new javax.swing.JRadioButtonMenuItem();
        fps20MI = new javax.swing.JRadioButtonMenuItem();
        fps15MI = new javax.swing.JRadioButtonMenuItem();
        placemarksMenu = new javax.swing.JMenu();
        gotoTeamRoomMI = new javax.swing.JMenuItem();
        gotoConferenceRoomMI = new javax.swing.JMenuItem();
        gotoStartingLocationMI = new javax.swing.JMenuItem();
        gotoLocationMI = new javax.swing.JMenuItem();
        sharedAppsMenu = new javax.swing.JMenu();
        localAppsMenu = new javax.swing.JMenu();
        runXAppMenuItem = new javax.swing.JMenuItem();
        teamRoomMenu = new javax.swing.JMenu();
        conferenceRoomMenu = new javax.swing.JMenu();
        demoFloorMenu = new javax.swing.JMenu();
        runServerXAppMenuItem = new javax.swing.JMenuItem();
        rdpClientMenuItem = new javax.swing.JMenuItem();
        releaseAllAppControlMI = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        avatarModelConfigMenuItem1 = new javax.swing.JMenuItem();
        muteMenuItem = new javax.swing.JCheckBoxMenuItem();
        audioQualityMenu = new javax.swing.JMenu();
        audioMenu = new javax.swing.JMenu();
        testAudioMenu = new javax.swing.JMenuItem();
        connectMenu = new javax.swing.JMenuItem();
        audioProblemMenuItem = new javax.swing.JMenuItem();
        importModel = new javax.swing.JMenuItem();
        worldEditorMI = new javax.swing.JMenuItem();
        transferMenu = new javax.swing.JMenu();
        fileTransferSmcMenuItem = new javax.swing.JMenuItem();
        fileTransferMenuItem = new javax.swing.JMenuItem();
        devToolsMenu = new javax.swing.JMenu();
        advancedConfigureMenu = new javax.swing.JMenu();
        avatarPropertiesConfigMenuItem = new javax.swing.JMenuItem();
        inputConfigMenuItem = new javax.swing.JMenuItem();
        joalConfigMenuItem = new javax.swing.JMenuItem();
        sharedAppsConfigMenuItem = new javax.swing.JMenuItem();
        wonderlandConfigMenuItem = new javax.swing.JMenuItem();
        toggleJInputMenuItem = new javax.swing.JCheckBoxMenuItem();
        systemGCMenuItem = new javax.swing.JMenuItem();
        robotMenuItem = new javax.swing.JCheckBoxMenuItem();
        showCurrentLocationMI = new javax.swing.JMenuItem();
        debugSharedAppsMenu = new javax.swing.JMenu();
        nameTagsEnabledMI = new javax.swing.JCheckBoxMenuItem();
        windowMenu = new javax.swing.JMenu();
        chatPanelCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        mapViewCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        performancePanelCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        softphoneCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        userListCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();

        gotoLocationDialog.setTitle("Goto Location WRL");

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        jPanel2.add(applyButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel2.add(cancelButton);

        gotoLocationDialog.getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jLabel1.setText("Goto WRL");
        jPanel1.add(jLabel1);

        gotoTF.setColumns(15);
        gotoTF.setText("(50,0,50) (0,0,-1)");
        gotoTF.setToolTipText("Goto World Resource Location (WRL)");
        jPanel1.add(gotoTF);

        gotoLocationDialog.getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Project Wonderland");

        centerPanel.setFocusable(false);
        centerPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        centerPanel.setOpaque(false);
        centerPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        centerPanel.setRequestFocusEnabled(false);
        centerPanel.setLayout(new java.awt.BorderLayout());
        centerPanel.add(jProgressBar1, java.awt.BorderLayout.CENTER);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        southPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Chat"));
        southPanel.setFocusable(false);
        southPanel.setRequestFocusEnabled(false);
        southPanel.setVerifyInputWhenFocusTarget(false);
        southPanel.setLayout(new java.awt.BorderLayout());

        chatEntry.setToolTipText("IM Text entry");
        chatEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chatEntryActionPerformed(evt);
            }
        });
        southPanel.add(chatEntry, java.awt.BorderLayout.SOUTH);

        chatHistory.setColumns(20);
        chatHistory.setEditable(false);
        chatHistory.setLineWrap(true);
        chatHistory.setRows(5);
        chatHistory.setToolTipText("IM Window");
        jScrollPane.setViewportView(chatHistory);

        southPanel.add(jScrollPane, java.awt.BorderLayout.CENTER);

        dividerPanel.setLayout(new java.awt.BorderLayout());
        dividerPanel.add(jSeparator, java.awt.BorderLayout.CENTER);

        southPanel.add(dividerPanel, java.awt.BorderLayout.NORTH);

        getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

        mainMenuBar.setFocusable(false);
        mainMenuBar.setRequestFocusEnabled(false);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        mainMenuBar.add(fileMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");

        cameraModeMenu.setMnemonic('C');
        cameraModeMenu.setText("Camera Mode");

        cameraButtonGroup.add(cameraModeFirstPersonMI);
        cameraModeFirstPersonMI.setMnemonic('F');
        cameraModeFirstPersonMI.setText("FIrst Person");
        cameraModeFirstPersonMI.setToolTipText("1st Person, No Avatar ");
        cameraModeFirstPersonMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraModeChangeActionPerformed(evt);
            }
        });
        cameraModeMenu.add(cameraModeFirstPersonMI);

        cameraButtonGroup.add(cameraModeThirdPersonMI);
        cameraModeThirdPersonMI.setMnemonic('T');
        cameraModeThirdPersonMI.setSelected(true);
        cameraModeThirdPersonMI.setText("Third Person");
        cameraModeThirdPersonMI.setToolTipText("3rd Person, Avatar Visible");
        cameraModeThirdPersonMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraModeChangeActionPerformed(evt);
            }
        });
        cameraModeMenu.add(cameraModeThirdPersonMI);

        cameraButtonGroup.add(cameraModeOrbitMI);
        cameraModeOrbitMI.setMnemonic('O');
        cameraModeOrbitMI.setText("Orbit");
        cameraModeOrbitMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraModeChangeActionPerformed(evt);
            }
        });
        cameraModeMenu.add(cameraModeOrbitMI);

        viewMenu.add(cameraModeMenu);

        showHUDCheckBox.setMnemonic('H');
        showHUDCheckBox.setSelected(true);
        showHUDCheckBox.setText("Show HUD");
        showHUDCheckBox.setToolTipText("Whether to show the Heads Up Display");
        showHUDCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHUDCheckBoxActionPerformed(evt);
            }
        });
        viewMenu.add(showHUDCheckBox);

        viewPropertiesMenuItem.setMnemonic('P');
        viewPropertiesMenuItem.setText("Properties...");
        viewPropertiesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewPropertiesMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(viewPropertiesMenuItem);

        maxFrameRateMenu.setMnemonic('M');
        maxFrameRateMenu.setText("Max Frame Rate");

        fpsButtonGroup.add(fps120MI);
        fps120MI.setText("120 fps (dev)");
        fps120MI.setToolTipText("For Testing !");
        fps120MI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFpsMIActionPerformed(evt);
            }
        });
        maxFrameRateMenu.add(fps120MI);

        fpsButtonGroup.add(fps30MI);
        fps30MI.setSelected(true);
        fps30MI.setText("30 fps");
        fps30MI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFpsMIActionPerformed(evt);
            }
        });
        maxFrameRateMenu.add(fps30MI);

        fpsButtonGroup.add(fps20MI);
        fps20MI.setText("20 fps");
        fps20MI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFpsMIActionPerformed(evt);
            }
        });
        maxFrameRateMenu.add(fps20MI);

        fpsButtonGroup.add(fps15MI);
        fps15MI.setText("15 fps");
        fps15MI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFpsMIActionPerformed(evt);
            }
        });
        maxFrameRateMenu.add(fps15MI);

        viewMenu.add(maxFrameRateMenu);

        mainMenuBar.add(viewMenu);

        placemarksMenu.setMnemonic('P');
        placemarksMenu.setText("Placemarks");

        gotoTeamRoomMI.setMnemonic('T');
        gotoTeamRoomMI.setText("MPK20 Team Room");
        gotoTeamRoomMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoTeamRoomMIActionPerformed(evt);
            }
        });
        placemarksMenu.add(gotoTeamRoomMI);

        gotoConferenceRoomMI.setMnemonic('C');
        gotoConferenceRoomMI.setText("MPK20 Conference Room");
        gotoConferenceRoomMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoConferenceRoomMIActionPerformed(evt);
            }
        });
        placemarksMenu.add(gotoConferenceRoomMI);

        gotoStartingLocationMI.setMnemonic('S');
        gotoStartingLocationMI.setText("Starting Location");
        gotoStartingLocationMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoStartingLocationMIActionPerformed(evt);
            }
        });
        placemarksMenu.add(gotoStartingLocationMI);

        gotoLocationMI.setMnemonic('L');
        gotoLocationMI.setText("Location...");
        gotoLocationMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoLocationMIActionPerformed(evt);
            }
        });
        placemarksMenu.add(gotoLocationMI);

        mainMenuBar.add(placemarksMenu);

        sharedAppsMenu.setMnemonic('S');
        sharedAppsMenu.setText("Shared Apps");
        sharedAppsMenu.setToolTipText("Shared X11 Applications");
        sharedAppsMenu.setActionCommand("shared-apps");

        localAppsMenu.setMnemonic('L');
        localAppsMenu.setText("Run Local Apps");
        localAppsMenu.setToolTipText("Run application in front of user");

        runXAppMenuItem.setMnemonic('X');
        runXAppMenuItem.setText("Run X application...");
        runXAppMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runXAppMenuItemActionPerformed(evt);
            }
        });
        localAppsMenu.add(runXAppMenuItem);

        sharedAppsMenu.add(localAppsMenu);

        teamRoomMenu.setMnemonic('T');
        teamRoomMenu.setText("Team Room");
        teamRoomMenu.setToolTipText("Run application in team room");
        sharedAppsMenu.add(teamRoomMenu);

        conferenceRoomMenu.setMnemonic('C');
        conferenceRoomMenu.setText("Conference Room");
        conferenceRoomMenu.setToolTipText("Run application in Conference room");
        sharedAppsMenu.add(conferenceRoomMenu);

        demoFloorMenu.setMnemonic('D');
        demoFloorMenu.setText("Demo Floor");
        demoFloorMenu.setToolTipText("Run application on demo floor");
        sharedAppsMenu.add(demoFloorMenu);

        runServerXAppMenuItem.setText("Run X app on server... (exp)");
        runServerXAppMenuItem.setEnabled(false);
        runServerXAppMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runServerXAppMenuItemActionPerformed(evt);
            }
        });
        sharedAppsMenu.add(runServerXAppMenuItem);

        rdpClientMenuItem.setMnemonic('P');
        rdpClientMenuItem.setText("rdesktop RDP Client (exp)");
        rdpClientMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdpClientMenuItemActionPerformed(evt);
            }
        });
        sharedAppsMenu.add(rdpClientMenuItem);

        releaseAllAppControlMI.setMnemonic('R');
        releaseAllAppControlMI.setText("Release All App Control");
        releaseAllAppControlMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                releaseAllAppControlMIActionPerformed(evt);
            }
        });
        sharedAppsMenu.add(releaseAllAppControlMI);

        mainMenuBar.add(sharedAppsMenu);

        toolsMenu.setMnemonic('T');
        toolsMenu.setText("Tools");

        avatarModelConfigMenuItem1.setMnemonic('C');
        avatarModelConfigMenuItem1.setText("Configure Avatar...");
        avatarModelConfigMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avatarModelConfigMenuItem1ActionPerformed(evt);
            }
        });
        toolsMenu.add(avatarModelConfigMenuItem1);

        muteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_MASK));
        muteMenuItem.setMnemonic('M');
        muteMenuItem.setText("Mute");
        muteMenuItem.setToolTipText("Toggle mute state");
        muteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                muteMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(muteMenuItem);

        audioQualityMenu.setMnemonic('Q');
        audioQualityMenu.setText("Audio Quality");
        toolsMenu.add(audioQualityMenu);

        audioMenu.setMnemonic('A');
        audioMenu.setText("Audio");

        testAudioMenu.setText("Test Audio");
        testAudioMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testAudioMenuActionPerformed(evt);
            }
        });
        audioMenu.add(testAudioMenu);

        connectMenu.setMnemonic('S');
        connectMenu.setText("Reconnect Softphone");
        connectMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectMenuActionPerformed(evt);
            }
        });
        audioMenu.add(connectMenu);

        audioProblemMenuItem.setText("Log Audio Problem");
        audioProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                audioProblemMenuItemActionPerformed(evt);
            }
        });
        audioMenu.add(audioProblemMenuItem);

        toolsMenu.add(audioMenu);

        importModel.setMnemonic('I');
        importModel.setText("Import Model (dev)");
        importModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importModelActionPerformed(evt);
            }
        });
        toolsMenu.add(importModel);

        worldEditorMI.setMnemonic('E');
        worldEditorMI.setText("World Editor (dev)");
        worldEditorMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                worldEditorMIActionPerformed(evt);
            }
        });
        toolsMenu.add(worldEditorMI);

        transferMenu.setMnemonic('T');
        transferMenu.setText("Transfer (exp)");

        fileTransferSmcMenuItem.setMnemonic('S');
        fileTransferSmcMenuItem.setText("File to Server");
        fileTransferSmcMenuItem.setActionCommand("Application server");
        fileTransferSmcMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTransferSmcMenuItemActionPerformed(evt);
            }
        });
        transferMenu.add(fileTransferSmcMenuItem);

        fileTransferMenuItem.setMnemonic('U');
        fileTransferMenuItem.setText("File to User");
        fileTransferMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTransferMenuItemActionPerformed(evt);
            }
        });
        transferMenu.add(fileTransferMenuItem);

        toolsMenu.add(transferMenu);

        mainMenuBar.add(toolsMenu);

        devToolsMenu.setMnemonic('D');
        devToolsMenu.setText("Dev Tools");

        advancedConfigureMenu.setMnemonic('C');
        advancedConfigureMenu.setText("Advanced Configuration (dev)");

        avatarPropertiesConfigMenuItem.setMnemonic('P');
        avatarPropertiesConfigMenuItem.setText("Avatar Properties...");
        avatarPropertiesConfigMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avatarPropertiesConfigMenuItemActionPerformed(evt);
            }
        });
        advancedConfigureMenu.add(avatarPropertiesConfigMenuItem);

        inputConfigMenuItem.setMnemonic('I');
        inputConfigMenuItem.setText("Input Mapping...");
        inputConfigMenuItem.setActionCommand("Config Input");
        inputConfigMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputConfigMenuItemActionPerformed(evt);
            }
        });
        advancedConfigureMenu.add(inputConfigMenuItem);
        inputConfigMenuItem.getAccessibleContext().setAccessibleName("Input Config");

        joalConfigMenuItem.setMnemonic('J');
        joalConfigMenuItem.setText("JOAL (OpenAL)");
        joalConfigMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joalConfigMenuItemActionPerformed(evt);
            }
        });
        advancedConfigureMenu.add(joalConfigMenuItem);

        sharedAppsConfigMenuItem.setMnemonic('S');
        sharedAppsConfigMenuItem.setText("Shared Applications");
        sharedAppsConfigMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sharedAppsConfigMenuItemActionPerformed(evt);
            }
        });
        advancedConfigureMenu.add(sharedAppsConfigMenuItem);

        wonderlandConfigMenuItem.setMnemonic('C');
        wonderlandConfigMenuItem.setText("Wonderland Client...");
        wonderlandConfigMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wonderlandConfigMenuItemActionPerformed(evt);
            }
        });
        advancedConfigureMenu.add(wonderlandConfigMenuItem);

        devToolsMenu.add(advancedConfigureMenu);

        toggleJInputMenuItem.setMnemonic('J');
        toggleJInputMenuItem.setText("JInput support (dev)");
        toggleJInputMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleJInputMenuItemActionPerformed(evt);
            }
        });
        devToolsMenu.add(toggleJInputMenuItem);

        systemGCMenuItem.setMnemonic('G');
        systemGCMenuItem.setText("Garbage Collect (dev)");
        systemGCMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemGCMenuItemActionPerformed(evt);
            }
        });
        devToolsMenu.add(systemGCMenuItem);

        robotMenuItem.setMnemonic('R');
        robotMenuItem.setText("Robot (dev)");
        robotMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                robotMenuItemActionPerformed(evt);
            }
        });
        devToolsMenu.add(robotMenuItem);

        showCurrentLocationMI.setMnemonic('P');
        showCurrentLocationMI.setText("Print Current Location (dev)");
        showCurrentLocationMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCurrentLocationMIActionPerformed(evt);
            }
        });
        devToolsMenu.add(showCurrentLocationMI);

        debugSharedAppsMenu.setMnemonic('S');
        debugSharedAppsMenu.setText("Debug Shared Apps (dev)");
        devToolsMenu.add(debugSharedAppsMenu);

        nameTagsEnabledMI.setMnemonic('N');
        nameTagsEnabledMI.setSelected(true);
        nameTagsEnabledMI.setText("Name Tags Enabled (dev)");
        nameTagsEnabledMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTagsEnabledMIActionPerformed(evt);
            }
        });
        devToolsMenu.add(nameTagsEnabledMI);

        mainMenuBar.add(devToolsMenu);

        windowMenu.setMnemonic('W');
        windowMenu.setText("Window");

        chatPanelCheckBoxMenuItem.setMnemonic('C');
        chatPanelCheckBoxMenuItem.setText("Chat");
        chatPanelCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chatPanelCheckBoxMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(chatPanelCheckBoxMenuItem);

        mapViewCheckBoxMenuItem.setMnemonic('M');
        mapViewCheckBoxMenuItem.setText("Map View");
        mapViewCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapViewCheckBoxMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(mapViewCheckBoxMenuItem);

        performancePanelCheckBoxMenuItem.setMnemonic('P');
        performancePanelCheckBoxMenuItem.setText("Status Panel");
        performancePanelCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performancePanelCheckBoxMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(performancePanelCheckBoxMenuItem);

        softphoneCheckBoxMenuItem.setMnemonic('S');
        softphoneCheckBoxMenuItem.setText("Softphone");
        softphoneCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                softphoneCheckBoxMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(softphoneCheckBoxMenuItem);

        userListCheckBoxMenuItem.setMnemonic('U');
        userListCheckBoxMenuItem.setText("Users");
        userListCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userListCheckBoxMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(userListCheckBoxMenuItem);

        mainMenuBar.add(windowMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");
        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void testAudioMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testAudioMenuActionPerformed
    
    SipStarter.getInstance().runLineTest();
}//GEN-LAST:event_testAudioMenuActionPerformed
    
    private void importModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importModelActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                //Create a file chooser
                final JFileChooser jloadFileDialog = new JFileChooser();
                if ( workingDir != null )
                    jloadFileDialog.setCurrentDirectory( new File( workingDir ) );
                int returnVal = jloadFileDialog.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = jloadFileDialog.getSelectedFile();
                    logger.warning("Opening: " + file.getName() + ".\n");
                    if ( returnVal == JFileChooser.APPROVE_OPTION ) {
                        File theFile = jloadFileDialog.getSelectedFile();
                        if(theFile == null) {
                            JOptionPane.showMessageDialog(null, "File Not Found", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            if(theFile.isDirectory()) {
                                JOptionPane.showMessageDialog(null, "Can not load a directoy", "Error", JOptionPane.ERROR_MESSAGE);
                            } else {
                                String filename = jloadFileDialog.getCurrentDirectory().getPath() + System.getProperty( "file.separator" ) + jloadFileDialog.getSelectedFile().getName();
                                if ( filename == "" ) {
                                    JOptionPane.showMessageDialog(null, "Empty file name", "Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    workingDir = new String( jloadFileDialog.getCurrentDirectory().getPath() );
                                    
                                    if ( filename.endsWith( ".x3d" ) || filename.endsWith( ".X3D" ) ) {
                                        X3DLoader x3dLoader = new X3DLoader();
                                        Scene scene = x3dLoader.load(filename);
                                        WonderlandUniverseFactory.getUniverse().addBranchGraph(scene.getSceneGroup());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    logger.warning("Open command cancelled by user.\n");
                }
            }
        });
    }//GEN-LAST:event_importModelActionPerformed
    
    private ServerAppLaunchDialog serverAppLaunchDialog;
        
    private void avatarPropertiesConfigMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avatarPropertiesConfigMenuItemActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                WonderlandConfigGUI gui = WonderlandClientConfigGUI.getDefault();
                
                gui.setSelected(AvatarClientConfig.getUserDefault());
                gui.setVisible(true);
            }
        });
    }//GEN-LAST:event_avatarPropertiesConfigMenuItemActionPerformed
    
    private void wonderlandConfigMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wonderlandConfigMenuItemActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                WonderlandConfigGUI gui = WonderlandClientConfigGUI.getDefault();
                
                gui.setSelected(WonderlandClientConfig.getUserDefault());
                gui.setVisible(true);
            }
        });
    }//GEN-LAST:event_wonderlandConfigMenuItemActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        gotoLocationDialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        Point3f pos = new Point3f();
        Vector3f lookDirection = new Vector3f();
        try {
            parseGotoLocation(gotoTF.getText(), pos, lookDirection);
            
            gotoLocationDialog.setVisible(false);
            AvatarControlBehavior.getAvatarControlBehavior().orientAvatar(pos, lookDirection, true);
        } catch(GotoParserException ex) {
            JOptionPane.showMessageDialog(gotoLocationDialog, "Error in Goto WRL "+ex.getMessage(), "Error in WRL", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_applyButtonActionPerformed
    
    private void gotoLocationMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoLocationMIActionPerformed
        gotoLocationDialog.pack();
        gotoLocationDialog.setVisible(true);
    }//GEN-LAST:event_gotoLocationMIActionPerformed
    
    private void showCurrentLocationMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCurrentLocationMIActionPerformed
        
        Point3f pos = new Point3f();
        Vector3f v3f = new Vector3f();
        
        AvatarControlBehavior.getAvatarControlBehavior().getPosition(pos);
        AvatarControlBehavior.getAvatarControlBehavior().getLookDirection(v3f);
        
        JOptionPane.showMessageDialog(this, "Avatar Current Location : "+pos+"\n  lookDirection "+v3f,"Location", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_showCurrentLocationMIActionPerformed
    
    private void gotoConferenceRoomMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoConferenceRoomMIActionPerformed
        Point3f pos = new Point3f(53.827713f, 1.1920929E-7f, 88.33005f);
        AvatarControlBehavior.getAvatarControlBehavior().orientAvatar(pos, new Vector3f(0f,0f,1f), true);
        
    }//GEN-LAST:event_gotoConferenceRoomMIActionPerformed
    
    private void gotoTeamRoomMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoTeamRoomMIActionPerformed
        Point3f pos = new Point3f(51.64006f, 0f, 13.052788f);
        AvatarControlBehavior.getAvatarControlBehavior().orientAvatar(pos, new Vector3f(0f,0f,-1f), true);
    }//GEN-LAST:event_gotoTeamRoomMIActionPerformed
    
    private void robotMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_robotMenuItemActionPerformed
        
        AvatarControlBehavior.getAvatarControlBehavior().setRobotEnabled(robotMenuItem.getState());
        
    }//GEN-LAST:event_robotMenuItemActionPerformed
        
    private void sharedAppsConfigMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sharedAppsConfigMenuItemActionPerformed
        JFrame f = SharedApp2DCellConfig.getDefault();
        f.setLocation(screenWidth,0);
        f.setVisible(true);
    }//GEN-LAST:event_sharedAppsConfigMenuItemActionPerformed
    
    private void userListCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userListCheckBoxMenuItemActionPerformed
        UserListJFrame.getUserListJFrame().setVisible(userListCheckBoxMenuItem.isSelected());
    }//GEN-LAST:event_userListCheckBoxMenuItemActionPerformed
    
    private void performancePanelCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performancePanelCheckBoxMenuItemActionPerformed
        openPerformanceJFrame(performancePanelCheckBoxMenuItem.isSelected());
    }//GEN-LAST:event_performancePanelCheckBoxMenuItemActionPerformed
    
    private void softphoneCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_softphoneCheckBoxMenuItemActionPerformed
        if (softphoneCheckBoxMenuItem.isSelected()) {
            SipStarter.getInstance().showSoftphone();
        } else {
            SipStarter.getInstance().hideSoftphone();
        }
    }//GEN-LAST:event_softphoneCheckBoxMenuItemActionPerformed
        
    private void runXAppMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runXAppMenuItemActionPerformed
        String s = (String)JOptionPane.showInputDialog(this,
                "Please enter the full pathname (and arguments) for the X Application\n" +
                "to be run inside Project Wonderland :\n",
                "Run X Application",
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
        
        if ((s != null) && (s.length() > 0)) {
            AppConfigX11 appConfig = new AppConfigX11(s);
            appConfig.setInitInBestView(true);
            AppLauncher.startXApp(appConfig);
        }
    }//GEN-LAST:event_runXAppMenuItemActionPerformed
    
    private void mapViewCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapViewCheckBoxMenuItemActionPerformed
        MapJFrame.getMap().setVisible(mapViewCheckBoxMenuItem.getState());
    }//GEN-LAST:event_mapViewCheckBoxMenuItemActionPerformed
    
    private void systemGCMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemGCMenuItemActionPerformed
        Runtime r = Runtime.getRuntime();
        
        long bt = r.totalMemory()/1024, bf = r.freeMemory()/1024, bm = r.maxMemory()/1024;
        r.gc();
        long at = r.totalMemory()/1024, af = r.freeMemory()/1024, am = r.maxMemory()/1024;
        
        logger.warning("Garbage collecting:\n"
                + "   Before: Total = " + bt + "K, Free = " + bf + "K, Max = " + bm + "K\n"
                + "   After : Total = " + at + "K, Free = " + af + "K, Max = " + am + "K");
    }//GEN-LAST:event_systemGCMenuItemActionPerformed
    
    private void inputConfigMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputConfigMenuItemActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InputConfigJFrame().setVisible(true);
            }
        });
    }//GEN-LAST:event_inputConfigMenuItemActionPerformed
    
    private void chatPanelCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chatPanelCheckBoxMenuItemActionPerformed
        if (chatPanelCheckBoxMenuItem.isSelected()) {
            if (!southPanel.isShowing()) {
                add(southPanel, BorderLayout.SOUTH);
                validate();
            }
        } else {
            remove(southPanel);
            validate();
        }
        
    }//GEN-LAST:event_chatPanelCheckBoxMenuItemActionPerformed
    
    private void joalConfigMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joalConfigMenuItemActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JOALOptionsFrame.getJOALOptionsFrame().setVisible(true);
            }
        });
    }//GEN-LAST:event_joalConfigMenuItemActionPerformed
    
    private void connectMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectMenuActionPerformed
        
        ChannelController controller = ChannelController.getController();
        
        try {
            controller.reconnectSoftphone();
        } catch (IOException e) {
            logger.warning("Unable to reconnect softphone:  " + e.getMessage());
        }
}//GEN-LAST:event_connectMenuActionPerformed
    
    private void audioProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_audioProblemMenuItemActionPerformed
        String s = audioProblemMenuItem.getText();
        
        SipStarter sipStarter = SipStarter.getInstance();
	sipStarter.sendCommandToSoftphone("stack");
    }//GEN-LAST:event_audioProblemMenuItemActionPerformed

    private void toggleJInputMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleJInputMenuItemActionPerformed
// TODO add your handling code here:
        if (toggleJInputMenuItem.isSelected()) {
        }
        logger.warning("Not yet implemented.");
    }//GEN-LAST:event_toggleJInputMenuItemActionPerformed
            
    private void gotoStartingLocationMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoStartingLocationMIActionPerformed
        AvatarControlBehavior.getAvatarControlBehavior().orientAvatar(
                AvatarControlBehavior.DEFAULT_POSITION,
                AvatarControlBehavior.DEFAULT_LOOK_DIRECTION,
    	        true);
        
}//GEN-LAST:event_gotoStartingLocationMIActionPerformed
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    private void openPerformanceJFrame(boolean visible) {
        if (assetManagerFrame==null || assetManagerFrame.get()==null)
            assetManagerFrame = new WeakReference<PerformanceJFrame>(new PerformanceJFrame());
        
        int x = WonderlandClientConfig.getDefault().getPerformancePanelX();
        int y = WonderlandClientConfig.getDefault().getPerformancePanelY();
        openJFrameWithinScreen(assetManagerFrame.get(), x, y, visible);
    }
    
    private void chatEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chatEntryActionPerformed
        String msg = chatEntry.getText();
        
        chatEntry.setText("");
        chatHistoryAppend(msg+"\n");
        
        if (!controllerTest.process(msg)) {
            ChannelController controller = ChannelController.getController();
            controller.getLocalUser().getAvatarCell().getAvatar().sendChatMessage(msg);
        }
        
    }//GEN-LAST:event_chatEntryActionPerformed
    
    private void muteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_muteMenuItemActionPerformed
        ChannelController.getController().getLocalUser().toggleMute();
    }//GEN-LAST:event_muteMenuItemActionPerformed

    private void fileTransferMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTransferMenuItemActionPerformed
        ChannelController.getController().testFileTransfer();
    }//GEN-LAST:event_fileTransferMenuItemActionPerformed

private void fileTransferSmcMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTransferSmcMenuItemActionPerformed
    ChannelController.getController().testFileTransferSmc();
}//GEN-LAST:event_fileTransferSmcMenuItemActionPerformed

private void worldEditorMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_worldEditorMIActionPerformed
    WorldEditor.getWorldEditor().setVisible(true);
}//GEN-LAST:event_worldEditorMIActionPerformed

private void showHUDCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showHUDCheckBoxActionPerformed
    if(logger.isLoggable(Level.WARNING)) logger.log(Level.WARNING, "HUD toggled with {0}", new Object[]{evt}); // TOOD: Re-level
    HUDFactory.getHUD().setShowingHUD(showHUDCheckBox.isSelected());
}//GEN-LAST:event_showHUDCheckBoxActionPerformed

private void cameraModeChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraModeChangeActionPerformed
// TODO add your handling code here:
    if (evt.getSource()==cameraModeFirstPersonMI) {
        AvatarControlBehavior.getAvatarControlBehavior().setCameraMode(AvatarCamera.Mode.COCKPIT);
    } else if (evt.getSource()==cameraModeThirdPersonMI) {
        AvatarControlBehavior.getAvatarControlBehavior().setCameraMode(AvatarCamera.Mode.ATTACHED);
    } else if (evt.getSource()==cameraModeOrbitMI) {
        AvatarControlBehavior.getAvatarControlBehavior().setCameraMode(AvatarCamera.Mode.ORBIT);
    }
}//GEN-LAST:event_cameraModeChangeActionPerformed

private void viewPropertiesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewPropertiesMenuItemActionPerformed
    new ViewControls().setVisible(true);
}//GEN-LAST:event_viewPropertiesMenuItemActionPerformed

private void avatarModelConfigMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avatarModelConfigMenuItem1ActionPerformed
    JFrame f = AvatarConfigurator.getDefault();
    f.setLocation(screenWidth,0);
    f.setVisible(true);
}//GEN-LAST:event_avatarModelConfigMenuItem1ActionPerformed

private void setFpsMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setFpsMIActionPerformed
    int fps=30;

    if (evt.getSource()==fps30MI) {
        fps = 30;
    } else if (evt.getSource()==fps20MI) {
        fps = 20;
    } else if (evt.getSource()==fps15MI) {
        fps = 15;
    } else if (evt.getSource()==fps120MI) {
        fps = 120;
    } else {
        logger.severe("Unrecognised FPS setting");
    }

    WonderlandUniverseFactory.setMaxFps(fps);
}//GEN-LAST:event_setFpsMIActionPerformed

private void runServerXAppMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runServerXAppMenuItemActionPerformed
    if (serverAppLaunchDialog == null) {
        serverAppLaunchDialog = new ServerAppLaunchDialog(this, false, 
                                                          serverApps);
    }
    serverAppLaunchDialog.setVisible(true);
}//GEN-LAST:event_runServerXAppMenuItemActionPerformed

private void rdpClientMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdpClientMenuItemActionPerformed
    String rdpCmd = System.getProperty("wonderland.rdp.command", null);
    
    if ((rdpCmd == null) || ("".equals(rdpCmd.trim()))) {
        String rdpHost = System.getProperty("wonderland.rdp.hostname", "");
        String rdpUser = System.getProperty("wonderland.rdp.user", null);
        
        rdpCmd = "/usr/bin/rdesktop";
        if (rdpUser != null)
            rdpCmd = rdpCmd + " -u " + rdpUser;
        
        rdpCmd = rdpCmd + " " + rdpHost;
        
        String s = (String)JOptionPane.showInputDialog(this,
                "Please enter the full commandline for the Windows RDP client\n" +
                "to be run inside Project Wonderland :\n\n" +
                "Usage: rdesktop [options] server[:port]\n" +
                "   -u: user name\n" +
                "   -d: domain\n" +
                "   -p: password (- to prompt)\n" +
                "   -n: client hostname\n" +
                "   -g: desktop geometry (WxH)\n" +
                "   -f: full-screen mode\n" +
                "   -z: enable rdp compression\n\n",
                "Run RDP client",
                JOptionPane.QUESTION_MESSAGE,
                null, null, rdpCmd);
    }
    
    if ((rdpCmd != null) && (rdpCmd.length() > 0)) {
        AppConfigX11 appConfig = new AppConfigX11(rdpCmd);
        appConfig.setInitInBestView(true);
        AppLauncher.startXApp(appConfig);
    }
}//GEN-LAST:event_rdpClientMenuItemActionPerformed

private void releaseAllAppControlMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_releaseAllAppControlMIActionPerformed
    AppGroup.releaseControlAll();
}//GEN-LAST:event_releaseAllAppControlMIActionPerformed

private void nameTagsEnabledMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTagsEnabledMIActionPerformed
    EventProcessor.processor().postEvent(new AvatarNameTagEvent(nameTagsEnabledMI.getState()), null);
}//GEN-LAST:event_nameTagsEnabledMIActionPerformed
    
    public void setMuteMenuItemState(boolean muted) {
        String labelText = muted ? "(un)Mute" : "Mute";
        muteMenuItem.setText(labelText);
        muteMenuItem.setSelected(muted);
    }
    
    /**
     * Create the debug shared apps menu
     * 
     * TODO Put this in the .form file
     */
    private void populateDebugSharedAppsMenu (javax.swing.JMenu debugSharedAppsMenu) {

	/*****************************************************************
	 ** Usage Note: These must be enabled BEFORE the app is started **
	 ****************************************************************/

	/*
	** Populate Stats submenu 
	*/

	javax.swing.JMenu statsMenu = new javax.swing.JMenu();
        statsMenu.setMnemonic('S');
        statsMenu.setText("Stats");
        statsMenu.setToolTipText("Whether to enable shared app statistics");
	debugSharedAppsMenu.add(statsMenu);

        final javax.swing.JCheckBoxMenuItem statsCommCheckBox = new javax.swing.JCheckBoxMenuItem();
        statsCommCheckBox.setMnemonic('C');
        statsCommCheckBox.setSelected(false);
        statsCommCheckBox.setText("Master/Slave Comm");
        statsCommCheckBox.setToolTipText("Whether to enable master/slave communication statistics");
        statsCommCheckBox.addActionListener(new java.awt.event.ActionListener() {
	    private boolean enabled = false;
            public void actionPerformed(java.awt.event.ActionEvent evt) {
		boolean enable = statsCommCheckBox.isSelected();
		if (enable != enabled) {
  		    ClientSocket.toggleStatsEnable();
		}
            }
        });
        statsMenu.add(statsCommCheckBox);

        final javax.swing.JCheckBoxMenuItem statsDrawCheckBox = new javax.swing.JCheckBoxMenuItem();
        statsDrawCheckBox.setMnemonic('D');
        statsDrawCheckBox.setSelected(false);
        statsDrawCheckBox.setText("Drawing");
        statsDrawCheckBox.setToolTipText("Whether to enable drawing statistics");
        statsDrawCheckBox.addActionListener(new java.awt.event.ActionListener() {
	    private boolean enabled = false;
            public void actionPerformed(java.awt.event.ActionEvent evt) {
		boolean enable = statsDrawCheckBox.isSelected();
		if (enable != enabled) {
  		    FrameSyncImage.toggleStatsEnable();
		}
            }
        });
        statsMenu.add(statsDrawCheckBox);

        final javax.swing.JCheckBoxMenuItem statsXremwinCheckBox = new javax.swing.JCheckBoxMenuItem();
        statsXremwinCheckBox.setMnemonic('X');
        statsXremwinCheckBox.setSelected(false);
        statsXremwinCheckBox.setText("Xremwin");
        statsXremwinCheckBox.setToolTipText("Whether to enable xremwin statistics");
        statsXremwinCheckBox.addActionListener(new java.awt.event.ActionListener() {
	    private boolean enabled = false;
            public void actionPerformed(java.awt.event.ActionEvent evt) {
		boolean enable = statsXremwinCheckBox.isSelected();
		if (enable != enabled) {
  		    Client.toggleXremwinStatsEnable();
		}
            }
        });
        statsMenu.add(statsXremwinCheckBox);

	/*
	** Populate rest of shared apps debug menu
	*/

	javax.swing.JMenuItem mitem;

	mitem = new javax.swing.JMenuItem("Toggle Xremwin Verbosity");
	debugSharedAppsMenu.add(mitem);
	mitem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		Client.toggleVerbosity();
	    }
	});

	mitem = new javax.swing.JMenuItem("Toggle Collapse");
	debugSharedAppsMenu.add(mitem);
	mitem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		ClientSocket.toggleCollapseEnable();
	    }
	});

	mitem = new javax.swing.JMenuItem("Toggle Slow Slaves");
	debugSharedAppsMenu.add(mitem);
	mitem.addActionListener(new java.awt.event.ActionListener() {
	    private static final int SLAVE_DELAY_FAST = 0;
	    private static final int SLAVE_DELAY_SLOW = 600;
    	    private boolean slow = false;		    
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
	        slow = !slow;
		ClientSocket.setSlaveWriteDelay(
		    slow ? SLAVE_DELAY_SLOW : SLAVE_DELAY_FAST);
	    }
	});

	/* TODO
	mitem = new javax.swing.JMenuItem("Toggle Catch Up");
	debugSharedAppsMenu.add(mitem);
	mitem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
	        ClientSocket.toggleCatchUpEnable();
	    }
        });
	*/
    }

    private static Main mainFrame = null;
    
    public static Main getMain() {
        return mainFrame;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        LocalControl control = LocalControl.getInstance();
        
        // first check if we should go to a particular place
        // TODO real command line processing
        if (args.length == 2 && args[0].equals("-goto")) {
            // parse the arguments
            String coords[] = args[1].split(":");
            if (coords.length != 3) {
                System.err.println("Usage: Wonderland -goto x:y:z");
                System.exit(-1);
            }
            
            float x = Float.parseFloat(coords[0]);
            float y = Float.parseFloat(coords[1]);
            float z = Float.parseFloat(coords[2]);
            
            // if there is an existing instance, just have it go to the
            // right place
            if (control.moveTo(x, y, z)) {
                // we are all done -- just exit at this point
                System.exit(0);
            } else {
                // TODO set initial location to given URL
                System.setProperty("lg.initialPosition", args[1]);
            }
        }
        
        // if we are the first instance, start listening for updates
        if (control.isFirstInstance()) {
            control.startListener();
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                (mainFrame = new Main()).setVisible(true);
            }
        });
    }
    
    private static void firefoxSetup() {
        String webstartProp = System.getProperty(MasterLauncher.XREMWIN_WEBSTART_DIR_PROP);
        
        String execPrefix;
        String ffsetupArg;
        if (webstartProp == null) {
            execPrefix = System.getProperty("wonderland.scripts.dir");
            ffsetupArg = "./data/Wonderland/test/appshare";
        } else {
            execPrefix = webstartProp;
            ffsetupArg = webstartProp;
        }
        
        
        Process proc = MasterLauncher.launchExternal(null, new String[] {
            execPrefix + File.separatorChar + "ffsetup",
            ffsetupArg
        });
        AppMonitor appMonitor = new AppMonitor(proc, new ffsetupReporter());
        // No need to wait for the process to terminate
    }
    
    private static class ffsetupReporter implements AppReporter {
        
        public String getName() {
            return "ffsetup";
        }
        
        public void output(String str, boolean app) {
            //System.err.println("Output for " + getName() + ": " + str);
        }
        
        public void exitValue(int exitValue, boolean app) {
            if (exitValue != 0) {
                System.err.print("Process " + getName() + "exitted with error status ");
                System.err.println(exitValue);
            }
        }
    }
    
    class ChatMessageListener implements LgEventListener {
        public void processEvent(LgEvent evt) {
            ChatMessageEvent chatMsg = (ChatMessageEvent)evt;
            chatHistory.setForeground(chatNameColor);
            chatHistoryAppend(chatMsg.getUserName()+": ");
            chatHistory.setForeground(chatTextColor);
            chatHistoryAppend(chatMsg.getMessage()+"\n");
        }
        
        public Class[] getTargetEventClasses() {
            return new Class[] { ChatMessageEvent.class };
        }
        
    }
    
    class GotoParserException extends Exception {
        public GotoParserException(String msg) {
            super(msg);
        }
    }
    
    /**
     * Called when the client receives the AlsSupported message which indicates
     * that the server supports the App Launch Service (ALS) and can launch
     * X apps on the server.
     * @param whether or not apps are supported
     * @param appNames the names of the supported apps, or an empty list if
     * apps aren't supported
     */
    public void alsSupported (final boolean supported, 
                              final List<AppRecord> apps) 
    {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (System.getProperty("wonderland.experimentalfeatures", "false").equalsIgnoreCase("true"))
                    runServerXAppMenuItem.setEnabled(supported);
                
                if (supported) {
                    // update the list of apps
                    serverApps = new ArrayList<AppConfigX11Example>(apps.size());
                    for (AppRecord ar : apps) {
                        String name = ar.getAppName();
                        AppConfigX11Example.AppMenu menu =
                                AppConfigX11Example.AppMenu.valueOf(ar.getMenuName());

                        AppConfigX11Example app = new AppConfigX11Example(name, menu);
                        app.setServerLaunch(true);
                        serverApps.add(app);
                    }

                    populateSharedAppsMenu(serverApps.iterator());
                } else {
                    for(AppConfigX11Example app : serverApps) {
                        app.getMenuItem().getParent().remove(app.getMenuItem());
                    }
                    serverApps.clear();
                }
                // if there is a server app launch dialog open, update its
                // list of apps
                if (serverAppLaunchDialog != null) {
                    serverAppLaunchDialog.setApps(serverApps);
                }
                pruneMenus();  // TODO for some reason the empty menus still have a child component after items have been removed
            }
         });

    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu advancedConfigureMenu;
    private javax.swing.JButton applyButton;
    private javax.swing.JMenu audioMenu;
    private javax.swing.JMenuItem audioProblemMenuItem;
    private javax.swing.JMenu audioQualityMenu;
    private javax.swing.JMenuItem avatarModelConfigMenuItem1;
    private javax.swing.JMenuItem avatarPropertiesConfigMenuItem;
    private javax.swing.ButtonGroup cameraButtonGroup;
    private javax.swing.JRadioButtonMenuItem cameraModeFirstPersonMI;
    private javax.swing.JMenu cameraModeMenu;
    private javax.swing.JRadioButtonMenuItem cameraModeOrbitMI;
    private javax.swing.JRadioButtonMenuItem cameraModeThirdPersonMI;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JTextField chatEntry;
    private javax.swing.JTextArea chatHistory;
    private javax.swing.JCheckBoxMenuItem chatPanelCheckBoxMenuItem;
    private javax.swing.JMenu conferenceRoomMenu;
    private javax.swing.JMenuItem connectMenu;
    private javax.swing.JMenu debugSharedAppsMenu;
    private javax.swing.JMenu demoFloorMenu;
    private javax.swing.JMenu devToolsMenu;
    private javax.swing.JPanel dividerPanel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem fileTransferMenuItem;
    private javax.swing.JMenuItem fileTransferSmcMenuItem;
    private javax.swing.JRadioButtonMenuItem fps120MI;
    private javax.swing.JRadioButtonMenuItem fps15MI;
    private javax.swing.JRadioButtonMenuItem fps20MI;
    private javax.swing.JRadioButtonMenuItem fps30MI;
    private javax.swing.ButtonGroup fpsButtonGroup;
    private javax.swing.JMenuItem gotoConferenceRoomMI;
    private javax.swing.JDialog gotoLocationDialog;
    private javax.swing.JMenuItem gotoLocationMI;
    private javax.swing.JMenuItem gotoStartingLocationMI;
    private javax.swing.JTextField gotoTF;
    private javax.swing.JMenuItem gotoTeamRoomMI;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem importModel;
    private javax.swing.JMenuItem inputConfigMenuItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JSeparator jSeparator;
    private javax.swing.JMenuItem joalConfigMenuItem;
    private javax.swing.JMenu localAppsMenu;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JCheckBoxMenuItem mapViewCheckBoxMenuItem;
    private javax.swing.JMenu maxFrameRateMenu;
    private javax.swing.JCheckBoxMenuItem muteMenuItem;
    private javax.swing.JCheckBoxMenuItem nameTagsEnabledMI;
    private javax.swing.JCheckBoxMenuItem performancePanelCheckBoxMenuItem;
    private javax.swing.JMenu placemarksMenu;
    private javax.swing.JMenuItem rdpClientMenuItem;
    private javax.swing.JMenuItem releaseAllAppControlMI;
    private javax.swing.JCheckBoxMenuItem robotMenuItem;
    private javax.swing.JMenuItem runServerXAppMenuItem;
    private javax.swing.JMenuItem runXAppMenuItem;
    private javax.swing.JMenuItem sharedAppsConfigMenuItem;
    private javax.swing.JMenu sharedAppsMenu;
    private javax.swing.JMenuItem showCurrentLocationMI;
    private javax.swing.JCheckBoxMenuItem showHUDCheckBox;
    private javax.swing.JCheckBoxMenuItem softphoneCheckBoxMenuItem;
    private javax.swing.JPanel southPanel;
    private javax.swing.JMenuItem systemGCMenuItem;
    private javax.swing.JMenu teamRoomMenu;
    private javax.swing.JMenuItem testAudioMenu;
    private javax.swing.JCheckBoxMenuItem toggleJInputMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu transferMenu;
    private javax.swing.JCheckBoxMenuItem userListCheckBoxMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem viewPropertiesMenuItem;
    private javax.swing.JMenu windowMenu;
    private javax.swing.JMenuItem wonderlandConfigMenuItem;
    private javax.swing.JMenuItem worldEditorMI;
    // End of variables declaration//GEN-END:variables
}
