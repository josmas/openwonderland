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
package org.jdesktop.wonderland.client.app.utils.clientsocket;

public MasterSocketSet implements Runnable {

    private BigInteger masterClientID;
    private ServerSocket serverSocket;
    private MasterClientSocketListener listener;
    private Thread acceptThread;
    private boolean stop = false;

    private HashMap<SessionId, MasterClientSocket> clientSocketMap = new HashMap<SessionId, MasterClientSocket>();

    public MasterSocketSet (BigInteger masterClientID, ServerSocket serverSocket, MasterClientSocketListener listener) {
	this.masterClientID = masterClientID;
	this.serverSocket = serverSocket;
	this.listener = listener;
	acceptThread = new Thread(this);
	acceptThread.setName("MasterSocketSet-AcceptThread");
    }

    public void start () {
	serverSocketAcceptThread.start();
    }

    public void stop () {
	stop = true;
    }

    /**
     * Closes this MasterSocketSet. Also closes the server socket which was passed into the constructor.
     */
    public void close () {
	stop();

	// Close all subordinate MasterClientSockets
	synchronized (clientSocketMap) {
	    for (MasterClientSocket mcs : clientSocketMap.values()) {
		mcs.close();
	    }
	    clientSocketMap.clear();
	}

	try {
	    serverSocket.close();
	} catch (Exception e) {
	    logger.warning("Cannot close server socket");
	}
    }

    /**
     * The run method of the thread accepting connections.
     */
    public void run() {
	while (!stop) {
	    try {
		Socket s = serverSocket.accept();
		MasterClientSocket mcs = new MasterClientSocket(masterClientID, s, new MyListener());
		if (mcs.initialize()) {
		    addSlave(mcs);
		    mcs.start();
		} else {
		    logger.warning("Failed to start client socket : " + s);
		}
	    } catch (Exception e) {
		if (serverSocket.isClosed())
		    break;

		e.printStackTrace();
	    }
	}
    }

    private class MyListener extends MasterClientSocketListener {

	public void receivedMessage (...) {
	    if (listener != null) {
		listener.receivedMessage();
	    }
	}

	public slaveLeft (BigInteger slave) {
	    removeSlave(slave);
	}
    }

    protected void addSlave (MasterClientSocket mcs) {
	synchronized (clientSocketMap) {
	    clientSocketMap.put(mcs.getOtherSessionID(), mcs);
	}
    }

    protected void removeSlave (MasterClientSocket mcs) {
	synchronized (clientSocketMap) {
	    clientSocketMap.remove(mcs.otherSessionId);
	}
	if (listener != null) {
	    listener.slaveLeft(mcs.otherSessionId);
	}
    }

    /**
     * Send an entire byte array to the given slave. Don't block.
     */
    public void send (SessionId recipient, byte[] buf) throws IOException {
	send(recipient, buf, buf.length);
    }

    /**
     * Send the given number of bytes of the given byte array to the given slave. Don't block.
     */
    public void send (BigInteger slave, byte[] buf, int len) throws IOException {
	if (len <= 0) return;
	MasterClientSocket mcs = clientSocketMap.get(slave);
	if (mcs != null) {
	    mcs.write(buf, len, false);
	}
    }

    /**
     * Send an entire byte array to all connected slaves. Don't block.
     */
    public void send (byte[] buf) throws IOException {
	send(buf, buf.length);
    }

    /**
     * Send the given number of bytes of the given byte array to all conencted slaves. Don't block.
     */
    public void send (byte[] buf, int len) throws IOException {
	if (len <= 0) return;
	synchronized (clientSocketMap) {
       	    for (BigInteger slave : clientSocketMap.keySet()) {
		MasterClientSocket mcs = clientSocketMap.get(slave);
		if (mcs != null) {
		    mcs.write(buf, len, false);
		}
	    }
	}
    }
}
