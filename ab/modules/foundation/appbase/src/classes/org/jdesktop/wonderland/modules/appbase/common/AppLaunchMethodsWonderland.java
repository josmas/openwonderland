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
package org.jdesktop.wonderland.modules.appbase.common;

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A Wonderland app is a 2D or 3D share-aware Java app which uses Wonderland sharing APIs.
 * When you are implementing this kind of app module you must implement the <code>getLaunchMethods</code> 
 * method of the app's <code>AppType</code> and <code>AppTypeGLO</code> to tell the system the different ways apps of 
 * that type are allowed to be launched. <code>getLaunchMethods</code> should an instance of this class. 
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppLaunchMethodsWonderland extends AppLaunchMethods {

    /**
     * {@inheritDoc}
     */
    public Style getStyle () {
	return Style.WONDERLAND;
    }
}