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
package org.jdesktop.wonderland.client.app.xremwin;

import org.jdesktop.wonderland.client.app.base.AppConventional;
import org.jdesktop.wonderland.client.app.base.AppConventionalCell;
import org.jdesktop.wonderland.client.app.base.AppTypeConventional;
import org.jdesktop.wonderland.client.app.base.AppType;
import org.jdesktop.wonderland.client.app.base.AppTypeCell;
import org.jdesktop.wonderland.client.app.base.ProcessReporterFactory;
import org.jdesktop.wonderland.common.app.xremwin.AppTypeNameXrw;

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

    public AppCellXrw (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
	session = cellCache.getSession();
    }

    /** 
     * {@inheritDoc}
     */
    public AppType getAppType () {
	// TODO: changes 0.5
	return AppTypeCell.findAppType(AppTypeNameXrw.XREMWIN_APP_TYPE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    protected void startMaster (String command, boolean initInBestView) {
	try { 
	    AppXrw app = new AppXrwMaster((AppTypeConventional)getAppType(), appName, masterHost, command, 
					  pixelScale, ProcessReporterFactory.getFactory().create(appName), session);
	} catch (InstantiationException ex) {
	    return;
	}

	((AppConventional)app).setInitInBestView(initInBestView);
	app.setCell(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void startSlave () {
	app = new AppXrwSlave((AppTypeConventional)getAppType(), appName, pixelScale, 
			      ProcessReporterFactory.getFactory().create(appName), connectionInfo, session);
	app.setCell(this);
    }
}
