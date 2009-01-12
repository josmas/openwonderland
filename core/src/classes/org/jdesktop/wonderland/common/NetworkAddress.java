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
package org.jdesktop.wonderland.common;

import java.io.IOException;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import java.util.logging.Logger;

import com.sun.stun.NetworkAddressManager;

public class NetworkAddress {

    private static Logger logger = Logger.getLogger(NetworkAddress.class.getName());

    public static InetAddress getPrivateLocalAddress() throws UnknownHostException {
	InetAddress ia = NetworkAddressManager.getPrivateLocalAddress();

	logger.finer("Default:  " + ia);
	return ia;
    } 

    public static InetAddress getPrivateLocalAddress(String s) throws UnknownHostException {
	if (s == null || s.length() == 0) {
	    InetAddress ia = NetworkAddressManager.getPrivateLocalAddress();

	    logger.finer("Default:  " + ia);
	    return ia;
	}

	logger.finer(s);

	String[] tokens = s.split(":");

	if (tokens.length == 1 || tokens[0].equalsIgnoreCase("host")) {
	    /*
	     * It's a host name or address
	     */
	    InetAddress ia = InetAddress.getByName(tokens[0]);

	    logger.finer("Host " + s + ": " + ia);
	    return ia;
	}

	if (tokens[0].equalsIgnoreCase("interface")) {
	    InetAddress ia = NetworkAddressManager.getPrivateLocalAddress(tokens[1]);

	    logger.finer("Interface " + tokens[1] + ": " + ia);
	    return ia;
	}

	if (tokens[0].equalsIgnoreCase("server") == false) {
	    logger.warning("Invalid specification:  " + s);
	    throw new UnknownHostException("Invalid specification:  " + s);
	}

	if (tokens.length < 3) {
	    logger.warning("Invalid server specified:  " + s);
	    throw new UnknownHostException("Invalid server specified:  " + s);
	}

	int port;

	try {
	    port = Integer.parseInt(tokens[2]);	
	} catch (NumberFormatException e) {
	    logger.warning("Invalid port specified:  " + s);
	    throw new UnknownHostException("Invalid port specified:  " + s);
	}
	
	int timeout = 500;

	if (tokens.length == 4) {
	    try {
		timeout = Integer.parseInt(tokens[3]);
	    } catch (NumberFormatException e) {
		logger.warning("Invalid timeout specified:  " + s
		    + " defaulting to " + timeout);
	    }
	}

	InetAddress ia = NetworkAddressManager.getPrivateLocalAddress(
	    tokens[1], port, timeout);

	logger.finer("server " + tokens[1] + ":" + port + ": " + ia);
	return ia;
    }

    /*
     * Ask stunServer to resolve socket.getAddress().
     */
    public static InetSocketAddress getPublicAddressFor(
            InetSocketAddress stunServer, DatagramSocket socket)
            throws IOException {

	return NetworkAddressManager.getPublicAddressFor(stunServer, socket);
    }

}
