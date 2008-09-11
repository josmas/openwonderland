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
package org.jdesktop.wonderland.common.config;

import java.io.Serializable;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * Represents Wonderland configuration options common to the entire system (server/client)
 */
@InternalAPI
public class WonderlandConfig extends WonderlandConfigBase implements Serializable {

    private final static String baseUrl;

    static {
	boolean useLocalArt = Boolean.parseBoolean(System.getProperty("wonderland.useLocalArt", "false"));

        String defBaseUrl = "http://192.18.37.42/compiled_models";
        
	baseUrl = System.getProperty(useLocalArt ? "wonderland.art.url.local" :
						   "wonderland.art.url.base", defBaseUrl);
    }
    
    public static String getBaseURL() {
	return baseUrl;
    }

    public static WonderlandConfig getDefault() {
	return WonderlandConfigUtil.getDefault(WonderlandConfig.class);
    }

    public static WonderlandConfig getSystemDefault() {
	return WonderlandConfigUtil.getSystemDefault(WonderlandConfig.class);
    }

    public static WonderlandConfig getUserDefault() {
	return WonderlandConfigUtil.getUserDefault(WonderlandConfig.class);
    }

    public WonderlandConfig() {}
    public WonderlandConfig(boolean init) { super(init); }

    public void init() {
    }

    public boolean isVoiceBridgeEnabled() {
	return Boolean.getBoolean("voicebridge.enabled");
    }

    public int getRegistrarTimeout() {
	return Integer.getInteger("registrar.timeout");
    }

    public boolean isVoiceManagerTunerEnabled() {
	return Boolean.getBoolean("voicelib.tunerEnabled");
    }

}
