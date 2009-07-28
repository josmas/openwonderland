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

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.help.HelpSystem;
import org.jdesktop.wonderland.common.LogControl;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.dnd.DragAndDropManager;
import org.jdesktop.wonderland.client.jme.utils.GUIUtils;

/**
 * The Main JFrame for the wonderland jme client
 * 
 * @author  paulby
 */
public class IMIDemoFrame extends JFrame implements MainFrame {
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(IMIDemoFrame.class.getName());
    /** i18n support **/
    private static final ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/bundle", Locale.getDefault());
    /** Menu stuffs **/
    private JMenuItem logoutMI;
    private JMenuItem exitMI;
    private ButtonGroup cameraButtonGroup = new ButtonGroup();
    private JRadioButtonMenuItem firstPersonRB;
    private JRadioButtonMenuItem thirdPersonRB;
    private JRadioButtonMenuItem frontPersonRB;
    private final Map<JMenuItem, Integer> menuWeights = new HashMap<JMenuItem, Integer>();
    private JMenu frameRateMenu;
    private int desiredFrameRate = 30;
    private FrameRateListener frameRateListener = null;
    private JMenuItem fpsMI;
    private Chart chart;
    private HUDComponent fpsComponent;

    private WorldManager wm;

    static {
        new LogControl(MainFrameImpl.class, "/org/jdesktop/wonderland/client/jme/resources/logging.properties");
    }
    // variables for the location field
    private String serverURL;
    private ServerURLListener serverListener;

    /** Creates new form MainFrame */
    public IMIDemoFrame(WorldManager wm, int width, int height) {
        this.wm = wm;
       

        GUIUtils.initLookAndFeel();
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        initComponents();
        initMenus();

        setTitle(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/bundle").getString("Wonderland"));
        centerPanel.setMinimumSize(new Dimension(width, height));
        centerPanel.setPreferredSize(new Dimension(width, height));

        // Register the main panel with the drag-and-drop manager
        DragAndDropManager.getDragAndDropManager().setDropTarget(centerPanel);

        pack();
    }

    private void initMenus() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // File menu
                // Log out
//                logoutMI = new JMenuItem(bundle.getString("Log out"));
//                logoutMI.addActionListener(new ActionListener() {
//
//                    public void actionPerformed(ActionEvent evt) {
//                        logoutMIActionPerformed(evt);
//                    }
//                });
//                addToFileMenu(logoutMI, 2);

                // Exit
                exitMI = new JMenuItem(bundle.getString("Exit"));
                exitMI.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        exitMIActionPerformed(evt);
                    }
                });
                fileMenu.add(exitMI);
//                addToFileMenu(exitMI, 3);

                // View menu
                firstPersonRB = new JRadioButtonMenuItem(bundle.getString("First Person Camera"));
                firstPersonRB.setToolTipText("");
                firstPersonRB.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cameraChangedActionPerformed(evt);
                    }
                });
                addToViewMenu(firstPersonRB, 0);
                cameraButtonGroup.add(firstPersonRB);

                thirdPersonRB = new JRadioButtonMenuItem(bundle.getString("Third Person Camera"));
                thirdPersonRB.setSelected(true);
                thirdPersonRB.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cameraChangedActionPerformed(evt);
                    }
                });
                addToViewMenu(thirdPersonRB, 1);
                cameraButtonGroup.add(thirdPersonRB);

                frontPersonRB = new JRadioButtonMenuItem(bundle.getString("Front Camera"));
                frontPersonRB.setToolTipText("A camera looking at the front of the avatar (for testing only)");
                frontPersonRB.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cameraChangedActionPerformed(evt);
                    }
                });
                addToViewMenu(frontPersonRB, 2);
                cameraButtonGroup.add(frontPersonRB);

                // Frame Rate menu
                frameRateMenu = new JMenu(bundle.getString("Max Frame Rate"));

                JMenuItem fps15 = new JCheckBoxMenuItem(bundle.getString("15 fps"));
                JMenuItem fps30 = new JCheckBoxMenuItem(bundle.getString("30 fps (default)"));
                JMenuItem fps60 = new JCheckBoxMenuItem(bundle.getString("60 fps"));
                JMenuItem fps120 = new JCheckBoxMenuItem(bundle.getString("120 fps"));
                JMenuItem fps200 = new JCheckBoxMenuItem(bundle.getString("200 fps"));

                frameRateMenu.add(fps15);
                frameRateMenu.add(fps30);
                frameRateMenu.add(fps60);
                frameRateMenu.add(fps120);
                frameRateMenu.add(fps200);

//                addToViewMenu(frameRateMenu, 5);

                fps15.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        frameRateActionPerformed(evt);
                    }
                });
                fps30.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        frameRateActionPerformed(evt);
                    }
                });
                fps60.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        frameRateActionPerformed(evt);
                    }
                });
                fps120.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        frameRateActionPerformed(evt);
                    }
                });
                fps200.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        frameRateActionPerformed(evt);
                    }
                });

                // frame rate meter
                fpsMI = new JCheckBoxMenuItem(bundle.getString("FPS_Meter"));
                // Demo hack!
                fpsMI.setActionCommand("IMI_HACK_FILTER");
                fpsMI.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if ((fpsComponent == null) || !fpsComponent.isVisible()) {
                            showFPSMeter(true);
                        } else {
                            showFPSMeter(false);
                        }
                    }
                });

                addToWindowMenu(fpsMI, -1);

                // Help menu
                HelpSystem helpSystem = new HelpSystem();
                JMenu helpMenu = helpSystem.getHelpJMenu();
//                mainMenuBar.add(helpMenu);
            }
        });
    }

    private void exitMIActionPerformed(ActionEvent evt) {
        System.exit(0);
    }

    private void cameraChangedActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == firstPersonRB) {
            ClientContextJME.getViewManager().setCameraController(new FirstPersonCameraProcessor());
        } else if (evt.getSource() == thirdPersonRB) {
            ClientContextJME.getViewManager().setCameraController(new ThirdPersonCameraProcessor());
        } else if (evt.getSource() == frontPersonRB) {
            ClientContextJME.getViewManager().setCameraController(new FrontHackPersonCameraProcessor());
        }

    }

    private void frameRateActionPerformed(java.awt.event.ActionEvent evt) {
        JMenuItem mi = (JMenuItem) evt.getSource();
        String[] fpsString = mi.getText().split(" ");
        int fps = Integer.valueOf(fpsString[0]);
        logger.info("maximum fps: " + fps);
        setDesiredFrameRate(fps);
    }

    public void setDesiredFrameRate(int desiredFrameRate) {
        this.desiredFrameRate = desiredFrameRate;

        for (int i = 0; i < frameRateMenu.getItemCount(); i++) {
            JMenuItem item = frameRateMenu.getItem(i);
            String[] fpsString = item.getText().split(" ");
            int fps = Integer.valueOf(fpsString[0]);
            if (fps == desiredFrameRate) {
                item.setSelected(true);
            } else {
                item.setSelected(false);
            }
        }
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);

        removeFrameRateListener(frameRateListener);
        frameRateListener = addFrameRateListener(desiredFrameRate);

        if (chart != null) {
            chart.setMaxValue(desiredFrameRate);
        }
    }

    /**
     * Return the JME frame
     * @return the frame
     */
    public JFrame getFrame() {
        return this;
    }

    /**
     * Returns the canvas of the frame.
     */
    public Canvas getCanvas() {
        return ViewManager.getViewManager().getCanvas();
    }

    /**
     * Returns the panel of the frame in which the 3D canvas resides.
     */
    public JPanel getCanvas3DPanel() {
        return centerPanel;
    }

    public void addToAvatarsMenu(JMenuItem menuItem) {
        addToMenu(avatarsMenu, menuItem, -1);
    }

    public void removeFromAvatarsMenu(JMenuItem menuItem) {
        removeFromMenu(avatarsMenu, menuItem);
    }
    /**
     * {@inheritDoc}
     */
    public void addToMenu(JMenu menu, JMenuItem menuItem, int weight) {
        if (weight < 0) {
            weight = Integer.MAX_VALUE;
        }

        final int weightFinal = weight;
        final JMenu menuFinal = menu;
        final JMenuItem menuItemFinal = menuItem;

        logger.fine(menuFinal.getText() + " menu: inserting [" + menuItemFinal.getText() +
                "] with weight: " + weight);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // find the index of the first menu item with a higher weight or
                // the same weight and later in the alphabet
                int index = 0;
                for (index = 0; index < menuFinal.getItemCount(); index++) {
                    JMenuItem curItem = menuFinal.getItem(index);
                    int curWeight = 0;
                    if (menuWeights.get(curItem) != null)
                        curWeight = menuWeights.get(curItem);
                    if (curWeight > weightFinal) {
                        break;
                    } else if (curWeight == weightFinal) {
                        if (curItem.getName() == null) {
                            break;
                        }

                        if (menuItemFinal.getName() != null &&
                                menuItemFinal.getName().compareTo(curItem.getName()) > 0) {
                            break;
                        }
                    }
                }

                // add the item at the right place
                menuFinal.insert(menuItemFinal, index);

                // remember the menu's weight
                menuWeights.put(menuItemFinal, weightFinal);
            }
        });
    }

    /**
     * Remove the given menu item from a menu
     * @param menu the menu to remove from
     * @param item the item to remove
     */
    public void removeFromMenu(JMenu menu, JMenuItem item) {
//        menu.remove(item);
//        menuWeights.remove(item);
    }

    /**
     * {@inheritDoc}
     */
    public void addToFileMenu(JMenuItem menuItem) {
//        addToMenu(fileMenu, menuItem, -1);
    }

    /**
     * {@inheritDoc}
     */
    public void addToFileMenu(JMenuItem menuItem, int index) {
//        addToMenu(fileMenu, menuItem, index);
    }

    /**
     * {@inheritDoc}
     */
    public void removeFromFileMenu(JMenuItem menuItem) {
//        removeFromMenu(fileMenu, menuItem);
    }

    /**
     * {@inheritDoc}
     */
    public void addToEditMenu(JMenuItem menuItem) {

    }

    /**
     * {@inheritDoc}
     */
    public void addToEditMenu(JMenuItem menuItem, int index) {

    }

    /**
     * {@inheritDoc}
     */
    public void removeFromEditMenu(JMenuItem menuItem) {

    }

    /**
     * {@inheritDoc}
     */
    public void addToViewMenu(JMenuItem menuItem) {
        addToMenu(viewMenu, menuItem, -1);
    }

    /**
     * {@inheritDoc}
     */
    public void addToViewMenu(JMenuItem menuItem, int index) {
        if (!menuItem.getText().toLowerCase().contains("properties"))
            addToMenu(viewMenu, menuItem, index);
    }

    /**
     *
     * @param menuItem
     */
    public void addToViewMenuCameraGroup(JRadioButtonMenuItem menuItem) {

    }

    /**
     * {@inheritDoc}
     */
    public void removeFromViewMenu(JMenuItem menuItem) {
        removeFromMenu(viewMenu, menuItem);
    }

    /**
     * {@inheritDoc}
     */
    public void addToInsertMenu(JMenuItem menuItem) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void addToInsertMenu(JMenuItem menuItem, int index) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void removeFromInsertMenu(JMenuItem menuItem) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void addToToolsMenu(JMenuItem menuItem) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void addToToolsMenu(JMenuItem menuItem, int index) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void removeFromToolsMenu(JMenuItem menuItem) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void addToPlacemarksMenu(JMenuItem menuItem) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void addToPlacemarksMenu(JMenuItem menuItem, int index) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void removeFromPlacemarksMenu(JMenuItem menuItem) {
     
    }

    /**
     * {@inheritDoc}
     */
    public void addToWindowMenu(JMenuItem menuItem) {
        addToWindowMenu(menuItem, -1);
    }

    /**
     * {@inheritDoc}
     */
    public void addToWindowMenu(JMenuItem menuItem, int index) {
        if (menuItem.getActionCommand() == "IMI_HACK_FILTER")
            addToMenu(windowMenu, menuItem, index);
    }

    /**
     * {@inheritDoc}
     */
    public void removeFromWindowMenu(JMenuItem menuItem) {
     
    }

    /**
     * Set the server URL in the location field
     * @param serverURL the server URL to set
     */
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
     
    }

    public void connected(boolean connected) {
        //showFPSMeter(connected);
    }

    /**
     * Add a camera menu item to the end of the View menu.
     *
     * @param cameraMenuItem
     */
    public void addToCameraChoices(JRadioButtonMenuItem cameraMenuItem) {
        addToCameraChoices(cameraMenuItem, -1);
    }

    /**
     * Add a camera menu item to the View menu at the specified index, where -1 adds
     * to the end of the menu
     *
     * @param cameraMenuItem
     */
    public void addToCameraChoices(JRadioButtonMenuItem cameraMenuItem, int index) {
        final JRadioButtonMenuItem itemFinal = cameraMenuItem;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                viewMenu.add(itemFinal);
                cameraButtonGroup.add(itemFinal);
                if (itemFinal.isSelected())
                    cameraButtonGroup.setSelected(itemFinal.getModel(), true);
            }
        });
    }

    /**
     * Removes the specified camera choice
     * @param menuItem
     */
    public void removeFromCameraChoices(JRadioButtonMenuItem menuItem) {
        final JRadioButtonMenuItem menuFinal = menuItem;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                cameraButtonGroup.remove(menuFinal);
                viewMenu.remove(menuFinal);
             }});
    }

    public void showFPSMeter(boolean visible) {
        if (visible) {
            if (chart == null) {
                // display FPS meter
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

                // create fps Swing control
                chart = new Chart("fps:");
                chart.setSampleSize(200);
                chart.setMaxValue(30);
                chart.setPreferredSize(new Dimension(200, 34));

                // create HUD control panel
                fpsComponent = mainHUD.createComponent(chart);
                fpsComponent.setDecoratable(false);
                fpsComponent.setPreferredLocation(Layout.SOUTHEAST);

                // add HUD control panel to HUD
                mainHUD.addComponent(fpsComponent);

                removeFrameRateListener(frameRateListener);
                frameRateListener = addFrameRateListener(desiredFrameRate);
            }
        } else {
            removeFrameRateListener(frameRateListener);
        }
        fpsComponent.setVisible(visible);
        fpsMI.setSelected(visible);
    }

    public FrameRateListener addFrameRateListener(int frameRate) {
        FrameRateListener listener = new FrameRateListener() {

            public void currentFramerate(float fps) {
                if (chart != null) {
                    chart.setValue(fps);
                }
            }
        };
        ClientContextJME.getWorldManager().getRenderManager().setFrameRateListener(listener, frameRate);

        return listener;
    }

    public void removeFrameRateListener(FrameRateListener listener) {
        if (listener != null) {
            ClientContextJME.getWorldManager().getRenderManager().setFrameRateListener(null, desiredFrameRate);
            frameRateListener = null;
        }
    }

    public void addServerURLListener(ServerURLListener listener) {
        serverListener = listener;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        serverPanel = new javax.swing.JPanel();
        centerPanel = new javax.swing.JPanel();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        viewMenu = new javax.swing.JMenu();
        windowMenu = new javax.swing.JMenu();
        avatarsMenu = new javax.swing.JMenu();

        jLabel1.setText("jLabel1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        serverPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        serverPanel.setPreferredSize(new java.awt.Dimension(692, 35));

        org.jdesktop.layout.GroupLayout serverPanelLayout = new org.jdesktop.layout.GroupLayout(serverPanel);
        serverPanel.setLayout(serverPanelLayout);
        serverPanelLayout.setHorizontalGroup(
            serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 682, Short.MAX_VALUE)
        );
        serverPanelLayout.setVerticalGroup(
            serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 25, Short.MAX_VALUE)
        );

        getContentPane().add(serverPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setText(bundle.getString("File")); // NOI18N
        mainMenuBar.add(fileMenu);

        viewMenu.setText("View");
        mainMenuBar.add(viewMenu);

        windowMenu.setText("Window");
        mainMenuBar.add(windowMenu);

        avatarsMenu.setText("Change Avatar");
        avatarsMenu.setToolTipText("Select an Avatar");
        mainMenuBar.add(avatarsMenu);

        setJMenuBar(mainMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu avatarsMenu;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPanel serverPanel;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables
}
