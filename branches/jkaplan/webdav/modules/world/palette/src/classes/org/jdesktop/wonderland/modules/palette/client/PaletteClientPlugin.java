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
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 * Client-size plugin for the cell palette.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PaletteClientPlugin implements ClientPlugin {
    /* The single instances of the cell palette dialog */
    private WeakReference<CellPalette> cellPaletteFrameRef = null;
    
    public void initialize(ServerSessionManager loginInfo) {
        // Add the Palette menu and the Cell submenu and dialog that lets users
        // create new cells.
        JMenu paletteMenu = new JMenu("Palettes");
        JMenuItem item = new JMenuItem("Cell Palette");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CellPalette cellPaletteFrame;
                if (cellPaletteFrameRef==null || cellPaletteFrameRef.get()==null) {
                    cellPaletteFrame = new CellPalette();
                    cellPaletteFrameRef = new WeakReference(cellPaletteFrame);
                } else
                    cellPaletteFrame = cellPaletteFrameRef.get();
                
                if (cellPaletteFrame.isVisible() == false) {
                    cellPaletteFrame.setVisible(true);
                }
            }
        });
        paletteMenu.add(item);
        JmeClientMain.getFrame().addToToolMenu(paletteMenu);
    }
}
