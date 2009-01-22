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
package org.jdesktop.wonderland.server.app.sas;

/**
 * Contains the SAS apps supported by all SAS clients connected to this server.
 *
 * @author deronj
 */

@ExperimentalAPI
class Registry {

    class ProviderInfo {
	BigInteger providerClientID;
	WonderlandClientSender providerSender;
	int providerNumApps;
    }

    private class CapabilityInfo {
	BigInteger providerClientID;
	String capabilityName;
	AppCollection apps;
	CapabilityInfo (BigInteger providerClientID, String capabilityName, AppCollection apps) {
	    this.providerClientID = providerClientID;
	    this.capabilityName = capabilityName;
	    this.apps = apps;
	}
    }

    private HashMap<BigInteger,ProviderInfo> providers = new HashMap<BigInteger,ProviderInfo>();

    private HashMap<BigInteger,HashMap<String,CapabilityInfo>> providerToCapabilitiesMap = 
	new HashMap<BigInteger,HashMap<String,CapabilityInfo>>();

    boolean isProviderRegistered (BigInteger providerClientID) {
	ProviderInfo providerInfo = providers.get(providerClientID);
	return providerInfo != null;
    }

    void registerProvider (BigInteger providerClientID, WonderlandClientSender providerSender) {
	ProviderInfo providerInfo;

	if (isProviderRegistered(providerClientID)) {
	    // Just update it's sender if the client ID is already in the map
	    providerInfo = providers.get(providerClientID);
	} else {
	    // Add a newly registered provider to the map
	    ProviderInfo providerInfo = new ProviderInfo();
	    providerInfo.providerClientID = providerClientID;
	    providers.put(providerClientID, providerInfo);
	}
	
	provider.providerSender = providerSender;
    }

    void unregisterProvider (BigInteger providerClientID) {
	providers.remove(providerClientID);
    }

    boolean synchronized addExecutionCapability (BigInteger providerClientID, String executionCapability, 
						 AppCollection apps) {

	HashMap<String,CapabilityInfo> capabilities = providerToCapabilitiesMap.get(providerClientID);
	if (capabilities == null) {
	    // First capability for this provider
	    capabilities = new HashMap<String,CapabilityInfo>();
	    capabilities.put(executionCapability, new CapabilityInfo(providerClientID, executionCapability, apps));
	    providerToCapabilitiesMap.put(providerClientID, capabilities);	    
	    return;
	}

	CapabilityInfo capability = capabilities.get(executionCapability);
	if (capability == null) {
	    // First of this particular capability
	    capability = new CapabilityInfo(providerClientID, executionCapability, apps);
	    capabilities.put(executionCapability, new CapabilityInfo(providerClientID, executionCapability, apps));
	}

	// Union the newly supported apps into the existing apps
	capability.apps = capability.apps.union(apps);
    }

    boolean synchronized removeExecutionCapability (BigInteger providerClientID, String executionCapability, 
						    AppCollection apps) {
    }

    synchronized LinkedList<ProviderInfo> getProviders (String executionCapability, String appName, 
							LinkedList<ProviderInfo> providersAlreadyTried) {
	
	LinkedList<ProviderInfo> providers = new LinkedList<ProviderInfo>();

	for (BigInteger providerClientID : providerToCapabilitiesMap.keySet()) {

	    // Skip providers we've already tried
	    if (providerInList(providerClientID, providersAlreadyTried)) {
		continue;
	    }

	    // Does this provider have any capabilities? If not, skip it
	    HashMap<String,CapabilityInfo> capabilities = providerToCapabilitiesMap.get(providerClientID);
	    if (capabilities == null) {
		continue;
	    }

	    // Does this provider have the desired execution capability? If not, skip it
	    CapabilityInfo capability = capabilities.get(executionCapability);
	    if (capability == null) {
		continue;
	    }

	    // Does this execution capability support the app?
	    if (!capability.apps.supports(appName)) {
		continue;
	    }

	    ProviderInfo provider = providers.get(capability.providerClientID);
	    if (provider == null) {
		continue;
	    }
	    providers.add(provider);
	}

	if (providers.size()) {
	    return null;
	} else {
	    return providers;
	}
    }

    private boolean providerInList (BigInteger providerClientID, LinkedList<ProviderInfo> providersAlreadyTried) {
	for (ProviderInfo provider : providersAlreadyTried) {
	    if (provider.providerClientID.equals(providerClientID)) {
		return true;
	    }
	}
	return false;
    }
}

