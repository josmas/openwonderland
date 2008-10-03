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
package org.jdesktop.wonderland.server.app.simplewhiteboard;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.app.base.AppLaunchMethods;
import org.jdesktop.wonderland.server.app.base.AppTypeMO;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardLaunchMethods;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardTypeName;

/**
 * The app type on the server side for the Whiteboard. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class WhiteboardAppTypeMO extends AppTypeMO {

    /**
     * {@inheritDoc}
     */
    public String getName () {
	return WhiteboardTypeName.WHITEBOARD_APP_TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppLaunchMethods getLaunchMethods () {
	return new WhiteboardLaunchMethods();
    }
}