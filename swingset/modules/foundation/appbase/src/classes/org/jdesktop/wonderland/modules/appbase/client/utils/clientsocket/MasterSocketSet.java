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
package org.jdesktop.wonderland.modules.appbase.client.utils.clientsocket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class MasterSocketSet implements Runnable {

    private static Logger logger = Logger.getLogger(MasterSocketSet.class.getName());

    private BigInteger masterClientID;
    private ServerSocket serverSocket;
    private ClientSocketListener listener;
    private Thread acceptThread;
    private boolean stop = false;
    private HashMap<BigInteger, MasterClientSocket> clientSocketMap = new HashMap<BigInteger, MasterClientSocket>();

    public MasterSocketSet(BigInteger masterClientID, ServerSocket serverSocket, ClientSocketListener listener) {
        this.masterClientID = masterClientID;
        this.serverSocket = serverSocket;
        this.listener = listener;
        acceptThread = new Thread(this);
        acceptThread.setName("MasterSocketSet-AcceptThread");
    }

    public void start() {
        acceptThread.start();
    }

    public void stop() {
        stop = true;
    }

    public void setEnable (BigInteger slaveID, boolean enable) {
	MasterClientSocket mcs;
        synchronized (clientSocketMap) {
	    mcs = clientSocketMap.get(slaveID);
	}
	mcs.setEnable(enable);
    }

    /**
     * Closes this MasterSocketSet. Also closes the server socket which was passed into the constructor.
     */
    public void close() {
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
                if (serverSocket.isClosed()) {
                    break;
                }

                e.printStackTrace();
            }
        }
    }

    private class MyListener implements ClientSocketListener {

	public void receivedMessage(BigInteger slave, byte[] buf) {
            if (listener != null) {
                listener.receivedMessage(slave, buf);
            }
        }

        public void slaveLeft(BigInteger slave) {
            removeSlave(slave);
        }
    }

    protected void addSlave(MasterClientSocket mcs) {
        synchronized (clientSocketMap) {
            clientSocketMap.put(mcs.getOtherClientID(), mcs);
        }
    }

    protected void removeSlave(BigInteger slave) {
        synchronized (clientSocketMap) {
            clientSocketMap.remove(slave);
        }
        if (listener != null) {
            listener.slaveLeft(slave);
        }
    }

    /**
     * Send an entire byte array to the given slave. Don't block.
     */
    public void send(BigInteger recipient, byte[] buf) throws IOException {
        send(recipient, buf, buf.length);
    }

    /**
     * Send the given number of bytes of the given byte array to the given slave. Don't block.
     */
    public void send(BigInteger slave, byte[] buf, int len) throws IOException {
        if (len <= 0) {
            return;
        }
	MasterClientSocket mcs;
        synchronized (clientSocketMap) {
	    mcs = clientSocketMap.get(slave);
	}
        if (mcs != null) {
            mcs.writeMessageBuf(buf, len);
        }
    }

    /**
     * Send an entire byte array to all connected slaves. Don't block.
     */
    public void send(byte[] buf) throws IOException {
        send(buf, buf.length);
    }

    /**
     * Send the given number of bytes of the given byte array to all conencted slaves. Don't block.
     */
    public void send(byte[] buf, int len) throws IOException {
        if (len <= 0) {
            return;
        }
        synchronized (clientSocketMap) {
            for (BigInteger slave : clientSocketMap.keySet()) {
                MasterClientSocket mcs = clientSocketMap.get(slave);
                if (mcs != null) {
                    mcs.writeMessageBuf(buf, len);
                }
            }
        }
    }
}
