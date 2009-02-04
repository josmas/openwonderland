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
package org.jdesktop.wonderland.modules.xremwin.client;

import java.io.Serializable;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.client.AppConventional;
import org.jdesktop.wonderland.modules.appbase.client.AppConventionalCell;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.AppTypeConventional;
import org.jdesktop.wonderland.modules.appbase.client.ProcessReporterFactory;

/**
 * An Xremwin client-side app cell.
 *
 * @author deronj
 */
@ExperimentalAPI
public class AppCellXrw extends AppConventionalCell {

    /** The session used by the cell cache of this cell to connect to the server */
    private WonderlandSession session;

    /**
     * Create an instance of AppCellXrw.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public AppCellXrw(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        session = cellCache.getSession();
    }

    /** 
     * {@inheritDoc}
     */
    public AppType getAppType() {
        return new AppTypeXrw();
    }

    /**
     * {@inheritDoc}
     */
    protected Serializable startMaster(String appName, String command, boolean initInBestView) {
        try {
            app = new AppXrwMaster((AppTypeXrw) getAppType(), appName, command,
                    pixelScale, ProcessReporterFactory.getFactory().create(appName), session);
        } catch (InstantiationException ex) {
            return null;
        }

        ((AppConventional) app).setInitInBestView(initInBestView);
        app.setDisplayer(this);
        return ((AppXrwMaster) app).getConnectionInfo();
    }

    /**
     * {@inheritDoc}
     */
    protected void startSlave(Serializable connectionInfo) {
        app = new AppXrwSlave((AppTypeConventional) getAppType(), appName, pixelScale,
                ProcessReporterFactory.getFactory().create(appName),
                (AppXrwConnectionInfo) connectionInfo, session);
        app.setCell(this);
    }
}
