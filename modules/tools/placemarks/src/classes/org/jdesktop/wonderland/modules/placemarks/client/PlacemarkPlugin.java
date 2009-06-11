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
package org.jdesktop.wonderland.modules.placemarks.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.placemarks.client.PlacemarkClientConfigConnection.PlacemarkConfigListener;
import org.jdesktop.wonderland.modules.placemarks.client.PlacemarkRegistry.PlacemarkListener;
import org.jdesktop.wonderland.modules.placemarks.client.PlacemarkRegistry.PlacemarkType;
import org.jdesktop.wonderland.modules.placemarks.common.Placemark;
import org.jdesktop.wonderland.modules.placemarks.common.PlacemarkList;

/**
 * Client-size plugin for registering items in the Cell Registry that come from
 * the configured list of X Apps.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class PlacemarkPlugin extends BaseClientPlugin
        implements SessionLifecycleListener, SessionStatusListener {

    private static Logger logger = Logger.getLogger(PlacemarkPlugin.class.getName());
    private WeakReference<EditPlacemarksJFrame> managePlacemarksFrameRef = null;
    private JMenuItem manageMI = null;
    private JMenuItem addMI = null;
    private PlacemarkListener listener = null;
    private PlacemarkClientConfigConnection placemarksConfigConnection = null;
    private PlacemarkConfigListener configListener = null;
    
    // A Map of Placemarks to their menu items for system-wide and user placemarks
    private Map<Placemark, JMenuItem> systemPlacemarkMenuItems = new HashMap();
    private Map<Placemark, JMenuItem> userPlacemarkMenuItems = new HashMap();

    /**
     * @inheritDoc()
     */
    @Override
    public void initialize(final ServerSessionManager sessionManager) {
        // We will listen for changes to the list of registered Placemarks and
        // edit the main menu as a result.
        listener = new PlacemarkMenuListener();

        // Create a new base connection to use for Placemark Config updates and
        // registry for notifications in changes to the current session.
        placemarksConfigConnection = new PlacemarkClientConfigConnection();
        sessionManager.addLifecycleListener(this);

        // Create the "Manage Placemarks..." menu item. The menu will be added
        // when our server intiializes.
        manageMI = new JMenuItem("Manage Placemarks...");
        manageMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EditPlacemarksJFrame managePlacemarksFrame;
                if (managePlacemarksFrameRef == null || managePlacemarksFrameRef.get() == null) {
                    JFrame frame = JmeClientMain.getFrame().getFrame();
                    managePlacemarksFrame = new EditPlacemarksJFrame();
                    managePlacemarksFrame.setLocationRelativeTo(frame);
                    managePlacemarksFrame.setSize(500, 300);
                    managePlacemarksFrameRef = new WeakReference(managePlacemarksFrame);
                }
                else {
                    managePlacemarksFrame = managePlacemarksFrameRef.get();
                }

                if (managePlacemarksFrame.isVisible() == false) {
                    managePlacemarksFrame.setSize(400, 300);
                    managePlacemarksFrame.setVisible(true);
                }
            }
        });

        // Create the "Add Placemark..." menu item. This menu will be added when
        // our server initializes.
        addMI = new JMenuItem("Add Placemark...");
        addMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = JmeClientMain.getFrame().getFrame();

                // Form a placemark with the current position about the avatar's
                // location.
                Placemark placemark = getCurrentPlacemark(sessionManager);

                // Fetch the list of known USER Placemark names
                PlacemarkRegistry registry = PlacemarkRegistry.getPlacemarkRegistry();
                Set<Placemark> placemarkSet = registry.getAllPlacemarks(PlacemarkType.USER);

                // Display a dialog with the values in the Placemark. And if we wish
                // to update the values, then re-add the placemark. (Re-adding the
                // placemark should have the effect of updating its values.
                AddEditPlacemarkJDialog dialog =
                        new AddEditPlacemarkJDialog(frame, true, placemark, placemarkSet);
                dialog.setTitle("Add Placemark");
                dialog.setLocationRelativeTo(frame);
                dialog.pack();
                dialog.setVisible(true);

                if (dialog.getReturnStatus() == AddEditPlacemarkJDialog.RET_OK) {
                    // Create a new placemark with the new information.
                    String name = dialog.getPlacemarkName();
                    String url = dialog.getServerURL();
                    float x = dialog.getLocationX();
                    float y = dialog.getLocationY();
                    float z = dialog.getLocationZ();
                    float angle = dialog.getLookAtAngle();
                    Placemark newPlacemark = new Placemark(name, url, x, y, z, angle);

                    try {
                        PlacemarkUtils.addUserPlacemark(newPlacemark);
                    } catch (Exception excp) {
                        logger.log(Level.WARNING, "Unable to add " + name + " to " +
                                " user's placemarks", excp);
                        return;
                    }

                    // Tell the client-side registry of placemarks that a new one has
                    // been added
                    registry.registerPlacemark(newPlacemark, PlacemarkType.USER);
                }
            }
        });

        super.initialize(sessionManager);
    }

    /**
     * @inheritDoc()
     */
    @Override
    protected void activate() {

        // Listen for changes to the list of Placemarks. This needs to come
        // first before we actually add the Placemarks from WebDav
        PlacemarkRegistry registry = PlacemarkRegistry.getPlacemarkRegistry();
        registry.addPlacemarkRegistryListener(listener);

        // Fetch the list of system-wide placemarks and add them to the Placemark
        // main menu item.
        PlacemarkList systemList = PlacemarkUtils.getSystemPlacemarkList();
        for (Placemark placemark : systemList.getPlacemarksAsList()) {
            registry.registerPlacemark(placemark, PlacemarkType.SYSTEM);
        }

        // Fetch the list of system-wide placemarks and add them to the Placemark
        // main menu item.
        PlacemarkList userList = PlacemarkUtils.getUserPlacemarkList();
        for (Placemark placemark : userList.getPlacemarksAsList()) {
            registry.registerPlacemark(placemark, PlacemarkType.USER);
        }

        // Add the "Manage Placemarks..." and "Add Placemark..." to the main frame
        JmeClientMain.getFrame().addToPlacemarksMenu(manageMI, -1);
        JmeClientMain.getFrame().addToPlacemarksMenu(addMI, -1);
    }

    /**
     * @inheritDoc()
     */
    @Override
    protected void deactivate() {
        // Remove the "Manage Placemarks..." from the main frame
        JmeClientMain.getFrame().removeFromPlacemarksMenu(manageMI);
    }

        /**
     * @inheritDoc()
     */
    public void sessionCreated(WonderlandSession session) {
        // Do nothing.
    }

    /**
     * @inheritDoc()
     */
    public void primarySession(WonderlandSession session) {
        session.addSessionStatusListener(this);
        if (session.getStatus() == WonderlandSession.Status.CONNECTED) {
            connectClient(session);
        }
    }

    /**
     * @inheritDoc()
     */
    public void sessionStatusChanged(WonderlandSession session, Status status) {
        switch (status) {
            case CONNECTED:
                connectClient(session);
                return;

            case DISCONNECTED:
                disconnectClient();
                return;
        }
    }

    /**
     * Connect the client.
     */
    private void connectClient(WonderlandSession session) {
        try {
            configListener = new ClientPlacemarkConfigListener();
            placemarksConfigConnection.addPlacemarkConfigListener(configListener);
            placemarksConfigConnection.connect(session);
        } catch (ConnectionFailureException e) {
            logger.log(Level.WARNING, "Connect client error", e);
        }
    }

    /**
     * Disconnect the client
     */
    private void disconnectClient() {
        placemarksConfigConnection.disconnect();
        placemarksConfigConnection.removePlacemarkConfigListener(configListener);
        configListener = null;
    }

    /**
     * Returns a Placemark that represents the current position of the avatar.
     */
    private Placemark getCurrentPlacemark(ServerSessionManager sessionManager) {
        // Fetch the current translation of the avatar and the (x, y, z) of its
        // position
        ViewManager manager = ViewManager.getViewManager();
        ViewCell viewCell = manager.getPrimaryViewCell();
        Vector3f location = viewCell.getWorldTransform().getTranslation(null);
        float x = location.x;
        float y = location.y;
        float z = location.z;

        // Find out what the URL to the server is
        String url = sessionManager.getServerURL();

        // Compute the current look angle, as degrees from the +x axis, ignoring
        // any look in the y-axis.
        Vector3f look = manager.getCameraLookDirection(null);
        float lookAngle = (float) Math.atan(look.z / look.x);
        lookAngle = (float)Math.toDegrees(lookAngle);

        return new Placemark("", url, x, y, z, lookAngle);
    }

    /**
     * Listens for when Placemarks are added or removed.
     */
    private class ClientPlacemarkConfigListener implements PlacemarkConfigListener {

        public void placemarkAdded(Placemark placemark) {
            PlacemarkRegistry registry = PlacemarkRegistry.getPlacemarkRegistry();
            registry.registerPlacemark(placemark, PlacemarkType.SYSTEM);
        }

        public void placemarkRemoved(Placemark placemark) {
            PlacemarkRegistry registry = PlacemarkRegistry.getPlacemarkRegistry();
            registry.unregisterPlacemark(placemark, PlacemarkType.SYSTEM);
        }
    }

    /**
     * Action listener for Placemark menu items.
     */
    private class PlacemarkActionListener implements ActionListener {
        private Placemark placemark = null;

        /** Constructor, takes the placemark associated with the menu item */
        public PlacemarkActionListener(Placemark placemark) {
            this.placemark = placemark;
        }

        public void actionPerformed(ActionEvent e) {
            String url = placemark.getUrl();
            float x = placemark.getX();
            float y = placemark.getY();
            float z = placemark.getZ();
            float angle = placemark.getAngle();
            Vector3f location = new Vector3f(x, y, z);
            Quaternion look = new Quaternion(0.0f, 1.0f, 0.0f, angle);
//            try {
//                ClientContextJME.getClientMain().gotoLocation(url, location, look);
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }

            JFrame frame = JmeClientMain.getFrame().getFrame();
            JOptionPane.showMessageDialog(frame,
                    "Placemarks are not yet functional, sorry!");
        }
    }

    /**
     * Listens for changes to the list of Placemarks and updates the menu
     * system accordingly.
     */
    private class PlacemarkMenuListener implements PlacemarkListener {
        public void placemarkAdded(Placemark placemark, PlacemarkType type) {
            // First try to find any existing placemark with the same name. If
            // there is one, then remove the JMenuItem from the menu. Note that
            // we can use the given Placemark as a key in the Map, because the
            // Placemark.equals() methods only evaluates the placemark name.
            placemarkRemoved(placemark, type);

            // Now add the new JMenuItem to either the system or user position
            // in the Placemarks menu
            JMenuItem menuItem = new JMenuItem(placemark.getName());
            PlacemarkActionListener listner = new PlacemarkActionListener(placemark);
            menuItem.addActionListener(listner);
            if (type == PlacemarkType.USER) {
                userPlacemarkMenuItems.put(placemark, menuItem);
                JmeClientMain.getFrame().addToPlacemarksMenu(menuItem, 2);
            }
            else {
                systemPlacemarkMenuItems.put(placemark, menuItem);
                JmeClientMain.getFrame().addToPlacemarksMenu(menuItem, 1);
            }
        }

        public void placemarkRemoved(Placemark placemark, PlacemarkType type) {
            // Try to find the existing placemark and remove it from the menu
            Map<Placemark, JMenuItem> map = (type == PlacemarkType.USER) ?
                userPlacemarkMenuItems : systemPlacemarkMenuItems;

            JMenuItem menuItem = map.get(placemark);
            if (menuItem != null) {
                map.remove(placemark);
                JmeClientMain.getFrame().removeFromPlacemarksMenu(menuItem);
            }
        }
    }
}
