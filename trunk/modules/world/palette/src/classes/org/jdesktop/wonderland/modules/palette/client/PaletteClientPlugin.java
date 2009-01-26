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
package org.jdesktop.wonderland.modules.palette.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenu;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;

/**
 * Client-size plugin for the cell palette.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PaletteClientPlugin implements ClientPlugin {
    /* The single instance of the cell palette dialog */
    private WeakReference<CellPalette> cellPaletteFrameRef = null;

    public void initialize(ServerSessionManager loginInfo) {
        // Add the Palette menu and the Cell submenu and dialog that lets users
        // create new cells.
        JMenu paletteMenu = new JMenu("Palettes");
        JMenuItem item = new JMenuItem("Cell Palette");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CellPalette cellPaletteFrame;
                if (cellPaletteFrameRef == null || cellPaletteFrameRef.get() == null) {
                    cellPaletteFrame = new CellPalette();
                    cellPaletteFrameRef = new WeakReference(cellPaletteFrame);
                }
                else {
                    cellPaletteFrame = cellPaletteFrameRef.get();
                }
                
                if (cellPaletteFrame.isVisible() == false) {
                    cellPaletteFrame.setVisible(true);
                }
            }
        });
        paletteMenu.add(item);
        JmeClientMain.getFrame().addToToolMenu(paletteMenu);

        // For the Cell Edit frame, we need to register some standard cell
        // components that exist in the system. Although these components are
        // likely defined in core, we register them here in one place. Module-
        // specific components are registered in the module themselves.
//        CellRegistry registry = CellRegistry.getCellRegistry();
//        registry.registerCellComponentFactory(new CellComponentFactory() {
//            public ConfigPanel getConfigPanel() {
//                return new MovableComponentConfigPanel();
//            }
//
//            public String getDisplayName() {
//                return "Moveable";
//            }
//
//            public String getDescription() {
//                return "Origin, Rotation, and Scaling";
//            }
//
//            public Class getCellComponentSetupClass() {
//                return MovableCellComponentSetup.class;
//            }
//        });

        // Add the "Properties" item to the context menu. Display a CellEditFrame
        // when selected.
        ContextMenu contextMenu = ContextMenu.getContextMenu();
        contextMenu.addContextMenuItem("Properties", new ContextMenuListener() {
            public void entityContextPerformed(ContextMenuEvent event) {
                // Fetch the cell from the context event, using the entity
                // given in the event
                Entity entity = event.getEntityList().get(0);
                if (entity == null) {
                    return;
                }
                Cell cell = SceneManager.getCellForEntity(entity);
                if (cell == null) {
                    return;
                }

                // Create a new cell edit frame passing in the Cell and make
                // it visible
                CellEditFrame frame = new CellEditFrame(cell);
                frame.setVisible(true);
            }
        });
    }
}
