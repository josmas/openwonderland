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

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.AppTypeConventional;
import org.jdesktop.wonderland.modules.appbase.common.AppLaunchMethods;
import org.jdesktop.wonderland.modules.xremwin.common.AppLaunchMethodsXrw;
import org.jdesktop.wonderland.modules.xremwin.common.AppTypeNameXrw;

/**
 * The AppType for X11 applications that use the Xremwin protocol.
 *
 * @author deronj
 */
@ExperimentalAPI
public class AppTypeXrw extends AppTypeConventional {

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return AppTypeNameXrw.XREMWIN_APP_TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppLaunchMethods getLaunchMethods() {
        return new AppLaunchMethodsXrw();
    }
}