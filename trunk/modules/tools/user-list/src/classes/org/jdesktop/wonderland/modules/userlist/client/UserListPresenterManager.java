/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.userlist.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.userlist.client.presenters.WonderlandUserListPresenter;
import org.jdesktop.wonderland.modules.userlist.client.views.WonderlandUserListView;

/**
 *
 * @author JagWire
 */
public enum UserListPresenterManager implements HUDEventListener {
    INSTANCE;
    
    private final Map<String, WonderlandUserListPresenter> presenters = 
            new HashMap<String, WonderlandUserListPresenter>();
    
    private WonderlandUserListPresenter defaultPresenter;
    private HUDComponent hudComponent;
    private String activePresenter = "default";
    private static final String DEFAULT = "default";
    private ImageIcon userListIcon = null;
    private JMenuItem userListMenuItem = null;
    private boolean initialized = false;
    
    private final List<DefaultUserListListener> listeners = 
            new CopyOnWriteArrayList<DefaultUserListListener>();
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/userlist/client/resources/Bundle");
    public static final String TABBED_PANEL_PROP = 
            "AudioManagerClient.Tabbed.Panel";
    private static final Logger logger = Logger.getLogger(UserListPresenterManager.class.getName());
    
    
    public void intialize() {
        if (!isInitialized()) {
            logger.fine("INITIALIZING PRESENTER MANAGER");
                    
            initializeMenuItem();
            
            userListIcon = new ImageIcon(getClass().getResource(
                    "/org/jdesktop/wonderland/modules/userlist/client/"
                    + "resources/GenericUsers32x32.png"));


            HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
            WonderlandUserListView view =
                    new WonderlandUserListView(new UserListCellRenderer());

            if (Boolean.parseBoolean(System.getProperty(TABBED_PANEL_PROP))) {
                HUDTabbedPanel tabbedPanel = HUDTabbedPanel.getInstance();
                hudComponent = main.createComponent(tabbedPanel);
                tabbedPanel.addTab("Users", view);
                tabbedPanel.getTabbedPanel().setSelectedIndex(0);
                tabbedPanel.setHUDComponent(hudComponent);
            } else {
                hudComponent = main.createComponent(view);
            }

            hudComponent.setDecoratable(true);
            hudComponent.setPreferredLocation(CompassLayout.Layout.NORTHWEST);
            hudComponent.setIcon(userListIcon);
            hudComponent.setName("Users (0)");
            hudComponent.addEventListener(this);

            main.addComponent(hudComponent);
            defaultPresenter = new WonderlandUserListPresenter(view,
                    hudComponent);

            presenters.put("default", defaultPresenter);

            synchronized (this) {
                initialized = true;
            }
            notifyListeners();
        }
    }
    
    public void cleanup() {
        removeMenuItem();
        hideActivePresenter();
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        if (hudComponent != null) {
            mainHUD.removeComponent(hudComponent);
            hudComponent = null;
        }
        presenters.clear();
    }
    
    public void HUDObjectChanged(HUDEvent event) {
       HUDEvent.HUDEventType hudEventType = event.getEventType();
        if (hudEventType == HUDEvent.HUDEventType.MINIMIZED
                        || hudEventType == HUDEvent.HUDEventType.MAXIMIZED
                        || hudEventType == HUDEvent.HUDEventType.CLOSED) {
                final boolean isSelected = hudEventType == HUDEvent.HUDEventType.MAXIMIZED;
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                                userListMenuItem.setSelected(isSelected);
                        }
                });
        }
    }
    
    public WonderlandUserListPresenter getDefaultPresenter() {
        return presenters.get(DEFAULT);
    }
    
    public void addPresenter(String name, WonderlandUserListPresenter presenter) {
        if(name == null || name.equals(DEFAULT)) {
            logger.warning("CANNOT OVERWRITE DEFAULT PRESENTER.");
            return;
        }
        
        synchronized(presenters) {
            presenters.put(name, presenter);                
        }        
    }
    
    public void removePresenter(String name) {
        if(name == null || name.equals(DEFAULT)) {
            logger.warning("CANNOT REMOVE DEFAULT PRESENTER!");
            return;
        }
        
        synchronized(presenters) {
            presenters.remove(name);
        }
    }
    
    public void setActivePresenter(String name) {
        synchronized(presenters) {
            if(name == null || !presenters.containsKey(name)) {
                logger.warning("CANNOT ACTIVATE KEY THAT DOES NOT EXIST!");
                return;
            }
            
            hideActivePresenter();
            activePresenter = name;
            
            final WonderlandUserListPresenter ulp = presenters.get(name);
            
            SwingUtilities.invokeLater(new Runnable() { 
                public void run() {
                   ulp.setVisible(true);
                }
            });
            
            
        }
    }
    
    public void hideActivePresenter() {

        synchronized (presenters) {
            if (activePresenter == null || !presenters.containsKey(activePresenter)) {
                logger.warning("TRIED TO HIDE NONEXISTANT ACTIVE PRESENTER: "
                        + activePresenter);
                return;
            }

            final WonderlandUserListPresenter ulp = presenters.get(activePresenter);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    ulp.setVisible(false);
                }
            });
        }
    }
    
    public void showActivePresenter() {
        synchronized(presenters) {
            if(activePresenter == null || !presenters.containsKey(activePresenter)) {
                logger.warning("ACTIVE PRESENTER DOES NOT EXIST: "+activePresenter);
                return;
            }
            
            WonderlandUserListPresenter presenter = presenters.get(activePresenter);
            presenter.setVisible(true);
            presenter.updateUserList();
        }
    }
        
    private void initializeMenuItem() {
        userListMenuItem = new JCheckBoxMenuItem();
        userListMenuItem.setSelected(true);
        userListMenuItem.setText("Users");
        userListMenuItem.setEnabled(false);

        userListMenuItem.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent event) {
                handleMenuItemPress(event);
            }
        });
        userListMenuItem.setEnabled(true);
        
        JmeClientMain.getFrame().addToWindowMenu(userListMenuItem, 5);
                        
    }
    
    private void removeMenuItem() {
        if (userListMenuItem != null) {
            JmeClientMain.getFrame().removeFromWindowMenu(userListMenuItem);
        }
    }
    
    private synchronized boolean isInitialized() {
        return initialized;
    }
    
    private void handleMenuItemPress(ActionEvent event) {
        if(userListMenuItem.isSelected()) {
            showActivePresenter();
        } else {
            hideActivePresenter();
        }
    }
    
    public void addUserListListener(DefaultUserListListener l) {
        listeners.add(l);

        //if our listener has registered after we've been initialized, 
        //go ahead and notify it of activation.
        if(isInitialized()) {
            l.listActivated();
        }
    }
    
    public void removeUserListListener(DefaultUserListListener l) {
        listeners.remove(l);
    }
    
    public void notifyListeners() {
        for(DefaultUserListListener l: listeners) {
            l.listActivated();
        }
    }  
}
