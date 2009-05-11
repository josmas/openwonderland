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
package org.jdesktop.wonderland.modules.xremwin.client.registry;

import java.util.List;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.xremwin.common.registry.XAppRegistryItem;

/**
 * Client-size plugin for registering items in the Cell Registry that come from
 * the configured list of X Apps.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class XAppRegistryPlugin extends BaseClientPlugin {

    /**
     * @inheritDoc()
     */
    @Override
    protected void activate() {
        CellRegistry registry = CellRegistry.getCellRegistry();

        // Fetch the list of X Apps registered on the system and register them
        // with the Cell Registry
        List<XAppRegistryItem> userItems =
                XAppRegistryItemUtils.getUserXAppRegistryItemList();
        for (XAppRegistryItem item : userItems) {
            XAppCellFactory factory = new XAppCellFactory(item.getAppName(),
                    item.getCommand());
            registry.registerCellFactory(factory);
        }
    }

    /**
     * @inheritDoc()
     */
    @Override
    protected void deactivate() {
        // Don't do anything here. It is assumed the Cell Regsitry will take
        // care of unregistering Cell Factories when the primary server
        // changes.
    }
}
