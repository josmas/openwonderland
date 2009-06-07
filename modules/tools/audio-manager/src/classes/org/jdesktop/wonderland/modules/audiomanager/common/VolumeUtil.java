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
package org.jdesktop.wonderland.modules.audiomanager.common;

public class VolumeUtil {

    private VolumeUtil() {
    }

    public static int getClientVolume(double serverVolume) {

	int clientVolume;

	if (serverVolume <= 1) {
	    clientVolume = (int) (Math.round(serverVolume * 5 * 10) / 10.);
	} else {
	    clientVolume = (int) (Math.round((((serverVolume - 1) / .6) + 5) * 10) / 10.);
	}

	//System.out.println(" Server Volume " + serverVolume + " Client Volume " + clientVolume);
	return clientVolume;
    }

    public static double getServerVolume(double clientVolume) {
	double serverVolume;

        if (clientVolume > 5) {
            serverVolume = (double) (1 + ((clientVolume - 5) * .6));
	} else {
	    serverVolume = (double) (clientVolume / 5.);
	}

	//System.out.println("Client Volume " + clientVolume + " Server Volume " + serverVolume);
	return serverVolume;
    }

}
