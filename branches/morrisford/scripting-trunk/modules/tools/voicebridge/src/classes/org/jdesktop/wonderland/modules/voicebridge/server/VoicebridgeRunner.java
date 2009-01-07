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

package org.jdesktop.wonderland.modules.voicebridge.server;

import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.runner.BaseRunner;
import org.jdesktop.wonderland.runner.RunnerConfigurationException;

import org.jdesktop.wonderland.common.NetworkAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An extension of <code>BaseRunner</code> to launch the Darkstar server.
 * @author jkaplan
 */
public class VoicebridgeRunner extends BaseRunner {
    /** the default name if none is specified */
    private static final String DEFAULT_NAME = "Voice Bridge";
    
    /** the logger */
    private static final Logger logger =
            Logger.getLogger(VoicebridgeRunner.class.getName());
    
    /**
     * Configure this runner.  This method sets values to the default for the
     * Darkstar server.
     * 
     * @param props the properties to deploy with
     * @throws RunnerConfigurationException if there is an error configuring
     * the runner
     */
    @Override
    public void configure(Properties props) 
            throws RunnerConfigurationException 
    {
        super.configure(props);
    
        // if the name wasn't configured, do that now
        if (!props.containsKey("runner.name")) {
            setName(DEFAULT_NAME);
        }
    }
    
    
    /**
     * Get the set of files to deploy.  This returns voicebridge-dist.zip
     * in addition to the core deployment files.
     * @return the files to deploy
     */
    @Override
    public Collection<String> getDeployFiles() {
        Collection<String> out = super.getDeployFiles();
        out.add("voicebridge-dist.zip");
        return out;
    }
    
    /**
     * Get the default properties for the bridge.
     * @return the default properties
     */
    @Override
    public Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty("voicebridge.sip.port", "5060");
        props.setProperty("voicebridge.control.port", "6666");
        props.setProperty("voicebridge.status.listeners", "localhost");
        props.setProperty("voicebridge.sip.gateways", "");
        props.setProperty("voicebridge.sip.proxy", "");
        props.setProperty("voicebridge.outside.line.prefix", "9");
        props.setProperty("voicebridge.long.distance.prefix", "1");
        props.setProperty("voicebridge.international.prefix", "011");

        props.setProperty("voicebridge.local.hostAddress", getPrivateLocalAddress());

        return props;
    }
}
