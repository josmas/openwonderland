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
package org.jdesktop.wonderland.modules.appbase.client;

import java.io.Serializable;
import java.util.UUID;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Describes the type of a conventional 2D application.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppTypeConventional extends AppType2D {

    /** The return type of AppTypeConventional.executeMasterProgram */
    public static class ExecuteMasterProgramReturn {

	/** The app instance created */
	public AppConventional app;

	/** The app's unique ID (note: the ID is unique only within the master client session) */
	public UUID appId;

	/** Subclass-specific data for making a peer-to-peer connection between master and slave. */
	public Serializable connectionInfo;
    }

    /**
     * Used by the master client to execute an app with a given type and name. 
     *
     * @param appName A short name describing the app instance.
     * @param command The platform command to execute in order to start the app.
     * @param pixelScale The size of the window pixels.
     * @param reporter The reporter with which to report to the user. If null, a default reporter is used.
     * @return An instance of ExecuteMasterProgramReturn which contains information about the new app.
     */
    public abstract ExecuteMasterProgramReturn executeMasterProgram (String appName, String command, 
								     Vector2f pixelScale, ProcessReporter reporter);
}
