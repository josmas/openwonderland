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
package org.jdesktop.wonderland.modules.appbase.common;

import java.util.HashSet;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A Conventional app is a is a 2D app type which was written for a traditional 2D system.
 * When you are implementing this kind of app module you must implement the <code>getLaunchMethods</code> 
 * method of the app's <code>AppType</code> and <code>AppTypeGLO</code> to tell the system the different ways apps of 
 * that type are allowed to be launched. <code>getLaunchMethods</code> should return an instance of this class. 
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppLaunchMethodsConventional extends AppLaunchMethods {

    /**
     * Specifies where the conventional application program is to be run.
     * <br><br>
     * <code>SERVER</code> means the app is to be run on a Wonderland-determined Shared App Server (SAS).
     * <br><br>
     * <code>LOCAL</code> means the app is to be run on the the launching user's local machine.
     * <br><br>
     * <code>LOCAL_SERVER</code> means run on a SAS running on the launching user's local machine.
     * (Note: <code>LOCAL_SERVER</code> is not yet supported).
     * <br><br>
     * An app type may support any and all of these sites.
     */
    public static enum ExecutionSite { SERVER, LOCAL, LOCAL_SERVER };
    
    /** The execution site */
    protected HashSet<ExecutionSite> executionSites = new HashSet<ExecutionSite>();

    /** The required execution capability */
    protected String executionCapability;

    /**
     * {@inheritDoc}
     */
    public Style getStyle () {
	return Style.CONVENTIONAL;
    }

    /** 
     * Specify that this type of launcher is supported 
     *
     * @param site The execution site that is supported.
     */
    public void addExecutionSite (ExecutionSite site) {
	executionSites.add(site);
    }

    /** 
     * Is the specified execution type supported? 
     *
     * @param site The execution site to check whether it is supported.
     */
    public boolean containsExecutionSite (ExecutionSite site) {
	return executionSites.contains(site);
    }

    /**
     * Specifies the execution environment the execution site must support in order to 
     * be able to run apps of this type. Some examples: "Xremwin", "TightVNC", "SSGD".
     *
     * @param capability The capability supported.
     */
    public void setExecutionCapability (String capability) {
	executionCapability = capability;
    }

    /** 
     * Returns the execution capability of this app type.
     */
    public String getExecutionCapability () {
	return executionCapability;
    }
}
