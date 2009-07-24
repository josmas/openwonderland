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
package org.jdesktop.wonderland.modules.clientmonitor.client;

import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.clientmonitor.client.cell.ClientMonitorComponent;

/**
 *
 * @author paulby
 */
public class ClientMonitorPlugin implements ClientPlugin {

    public void initialize(ServerSessionManager loginInfo) {
        ViewManager.getViewManager().addViewManagerListener(new ViewManagerListener() {

            public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
                if (oldViewCell!=null) {
                    oldViewCell.removeComponent(ClientMonitorComponent.class);
                }

                newViewCell.addComponent(new ClientMonitorComponent(newViewCell));
            }

        });
    }

}
