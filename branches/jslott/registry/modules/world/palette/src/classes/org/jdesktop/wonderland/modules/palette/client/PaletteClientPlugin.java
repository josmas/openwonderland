/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.palette.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author jordanslott
 */
public class PaletteClientPlugin implements ClientPlugin {

    public void initialize(ServerSessionManager loginInfo) {
        JMenuItem item = new JMenuItem("Cell Palette");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CellPalette().setVisible(true);
            }
        });
        JmeClientMain.getFrame().addToToolMenu(item);
    }

}
