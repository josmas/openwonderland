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
package org.jdesktop.wonderland.modules.affordances.client;


import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenu;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.modules.affordances.client.event.AffordanceRemoveEvent;

/**
 * Client-size plugin for the cell manipulator affordances.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AffordancesClientPlugin implements ClientPlugin {
    /* The single instance of the Affordance HUD Panel */
    private JFrame affordanceHUDFrame = null;
    private AffordanceHUDPanel affordanceHUDPanel = null;

    public void initialize(ServerSessionManager loginInfo) {
        ContextMenu contextMenu = ContextMenu.getContextMenu();
        contextMenu.addContextMenuItem("Edit", new EditContextListener());
    }

    /**
     * Creates the affordance HUD frame
     */
    private void createHUDFrame() {
        affordanceHUDFrame = new JFrame();
        affordanceHUDFrame.getContentPane().setLayout(new GridLayout(1, 1));
        affordanceHUDFrame.getContentPane().add(affordanceHUDPanel = new AffordanceHUDPanel(affordanceHUDFrame));
        affordanceHUDFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        affordanceHUDFrame.addWindowListener(new FrameCloseListener());
        affordanceHUDFrame.setTitle("Edit Cell");
        affordanceHUDFrame.pack();
    }

    /**
     * Handles when the affordance frame is closed
     */
    class FrameCloseListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            // Tell all of the affordances to remove themselves by posting
            // an event to the input system as such.
            InputManager.inputManager().postEvent(new AffordanceRemoveEvent());
        }
    }

    /**
     * Handles when the "Edit" context menu item has been selected
     */
    class EditContextListener implements ContextMenuListener {

        public void entityContextPerformed(ContextMenuEvent event) {
            // Fetch the Entity associated with the event. If there is none,
            // then ignore the event quietly.
            if (event.getEntityList() == null || event.getEntityList().size() == 0) {
                Logger logger = Logger.getLogger(AffordancesClientPlugin.class.getName());
                logger.warning("Unable to find Entity in context menu event, ignoring context event");
                return;
            }
            Entity entity = event.getEntityList().get(0);

            // Fetch the cell associated with the entity. If null, then ignore
            // quietly.
            Cell cell = SceneManager.getCellForEntity(entity);
            if (cell == null) {
                return;
            }
            
            // Display the affordance HUD Panel
            if (affordanceHUDFrame == null) {
                createHUDFrame();
            }
            affordanceHUDFrame.setVisible(true);
            affordanceHUDPanel.updateGUI();
        }
    }
}
