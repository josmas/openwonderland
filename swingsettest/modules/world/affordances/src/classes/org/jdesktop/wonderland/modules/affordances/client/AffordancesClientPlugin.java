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


import java.awt.event.KeyEvent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenu;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.modules.affordances.client.jme.Affordance;
import org.jdesktop.wonderland.modules.affordances.client.jme.RotateAffordance;
import org.jdesktop.wonderland.modules.affordances.client.jme.TranslateAffordance;

/**
 * Client-size plugin for the cell manipulator affordances.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AffordancesClientPlugin implements ClientPlugin {
    
    public void initialize(ServerSessionManager loginInfo) {
        ContextMenu contextMenu = ContextMenu.getContextMenu();
        contextMenu.addContextMenuItem("Move", new MoveContextListener());
        contextMenu.addContextMenuItem("Rotate", new RotateContextListener());
    }

    /**
     * Inner class to handle when the "Move" item has been selected from the
     * context menu
     */
    class MoveContextListener implements ContextMenuListener {

        public void entityContextPerformed(ContextMenuEvent event) {
            // Fetch the Entity associated with the event. If there is none,
            // then ignore the event quietly.
            if (event.getEntityList() == null && event.getEntityList().size() == 0) {
                return;
            }
            Entity entity = event.getEntityList().get(0);

            // Fetch the cell associated with the entity. If null, then ignore
            // quietly.
            Cell cell = SceneManager.getCellForEntity(entity);
            if (cell == null) {
                return;
            }

            // Otherwise, attach a translate affordance to the cell. If we can
            // then also listen for the Esc key to dismiss the affordance.
            TranslateAffordance affordance = TranslateAffordance.addToCell(cell);
            if (affordance != null) {
                InputManager.inputManager().addGlobalEventListener(new KeyListener(affordance));
            }
        }
    }

    /**
     * Inner class to handle when the "Rotate" item has been selected from the
     * context menu
     */
    class RotateContextListener implements ContextMenuListener {

        public void entityContextPerformed(ContextMenuEvent event) {
            // Fetch the Entity associated with the event. If there is none,
            // then ignore the event quietly.
            Entity entity = event.getEntityList().get(0);
            if (entity == null) {
                return;
            }

            // Fetch the cell associated with the entity. If null, then ignore
            // quietly.
            Cell cell = SceneManager.getCellForEntity(entity);
            if (cell == null) {
                return;
            }

            // Otherwise, attach a translate affordance to the cell. if we
            // can then also listen for the Esc key to dismiss the affordance
            RotateAffordance affordance = RotateAffordance.addToCell(cell);
            if (affordance != null) {
                InputManager.inputManager().addGlobalEventListener(new KeyListener(affordance));
            }
        }
    }

    /**
     * Listens for key presses to display the affordance
     */
    class KeyListener extends EventClassListener {
        private Affordance affordance;

        public KeyListener(Affordance affordance) {
            this.affordance = affordance;
        }

        @Override
        public void commitEvent(Event event) {
            if (event instanceof KeyEvent3D) {
                KeyEvent3D ke = (KeyEvent3D)event;
                if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    affordance.remove();
                    InputManager.inputManager().removeGlobalEventListener(this);
                }
            }
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { KeyEvent3D.class };
        }
    }
}
