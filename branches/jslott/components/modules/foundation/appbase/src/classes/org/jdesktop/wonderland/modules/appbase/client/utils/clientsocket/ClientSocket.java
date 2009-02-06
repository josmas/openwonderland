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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.utils.stats.StatisticsReporter;

public class ClientSocket {

    private static Logger logger = Logger.getLogger(ClientSocket.class.getName());
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_IO = false;
    protected static boolean ENABLE_STATS = false;

    // For backport to Java 5
    private static byte[] arrayCopyOf(byte[] buf, int len) {
        byte[] copyBuf = new byte[len];
        System.arraycopy(buf, 0, copyBuf, 0, len);
        return copyBuf;
    }

    private static class Message {

        public byte[] buf;
        public int len;

        public Message(byte[] buf) {
            this(buf, buf.length);
        }

        public Message(byte[] buf, int len) {
            // Backport to Java 5
            //this.buf = Arrays.copyOf(buf, len);
            this.buf = arrayCopyOf(buf, len);
            this.len = len;
        }
    };
    private static int msgCountSent;
    private static int msgCountReceived;

    // 0 means unrestricted.
    private static long maxWriteQueueSize = 1024 * 1024 *
            Integer.getInteger(ClientSocket.class.getName() + ".maxWriteQueueSizeMB", 0);
    private ClientSocketListener listener;
    private Socket socket;
    protected boolean master;
    BigInteger myClientID = null;
    BigInteger otherClientID = null;
    private Thread readSocketThread = null;
    private Thread writeSocketThread = null;
    private boolean stopReading = false;
    private boolean stopWriting = false;
    private DataOutputStream dos = null;
    private DataInputStream dis = null;
    private final LinkedList<ClientSocket.Message> writeQueue = new LinkedList<ClientSocket.Message>();
    private long writeQueueSize = 0;
    private long numBytesRead;
    protected StatisticsReporter statReporter;
    private long numBytesWritten;
    protected boolean enable = false;

    protected ClientSocket(BigInteger clientID, Socket socket, ClientSocketListener listener) {
        myClientID = clientID;
        this.socket = socket;
        this.listener = listener;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnabled() {
        return enable;
    }

    public BigInteger getOtherClientID() {
        return otherClientID;
    }

    long getNumBytesRead() {
        return numBytesRead;
    }

    void setNumBytesRead(long n) {
        numBytesRead = n;
    }

    long getNumBytesWritten() {
        return numBytesWritten;
    }

    void setNumBytesWritten(long n) {
        numBytesWritten = n;
    }

    long getWriteQueueNumBytesInQueue() {
        return writeQueueSize;
    }

    int getWriteQueueNumMsgsInQueue() {
        return writeQueue.size();
    }

    int getNumBytesInSocketBuf () {
        try {
            return socket.getSendBufferSize();
        } catch (SocketException ex) {
            return 0;
        }
    }

    /* For debug */
    public static void toggleStatsEnable() {
        ENABLE_STATS = !ENABLE_STATS;
        logger.severe("Client socket statistics are " +
                (ENABLE_STATS ? "enabled" : "disabled"));
    }
    /**/

    public boolean initialize() {
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (Exception ex) {
            logger.warning("Failed to get Data Input/Output streams!");
            return false;
        }

        if (otherClientID == null) {
            if ((otherClientID = readClientID()) == null) {
                logger.warning("Failed to read remote session id");
                return false;
            }
        } else {
            if (!writeClientID(myClientID)) {
                logger.warning("Failed to write my session id");
                return false;
            }
        }

        readSocketThread = new Thread(new Runnable() {

            public void run() {
                readLoop();
            }
        });
        readSocketThread.setName("-readThread");

        writeSocketThread = new Thread(new Runnable() {

            public void run() {
                writeLoop();
            }
        });
        writeSocketThread.setName("-writeThread");

        logger.info("STARTED client socket\n" +
                "  remote ClientID = " + otherClientID +
                ", maxWriteQueueSize = " + maxWriteQueueSize);

        return true;
    }

    public void start() {
        readSocketThread.start();
        writeSocketThread.start();
    }

    public void close() {
        enable = false;
        close(true);
    }

    public void close(boolean remove) {
        if (!socket.isClosed()) {
            logger.info("CLOSING socket");
            try {
                socket.close();
            } catch (Exception e) {
                logger.warning("Error CLOSING socket!!");
            }
        }

        if (remove) {
            listener.slaveLeft(otherClientID);
        }

        if (ENABLE_STATS) {
            statReporter.stop();
        }
    }

    protected byte[] readMessage() throws IOException, EOFException {
        if (DEBUG) {
            msgCountReceived = dis.readInt();
            logger.fine("Received message " + msgCountReceived);
        }

        int len = dis.readInt();
        byte[] buf = new byte[len];

        dis.readFully(buf, 0, len);

        if (DEBUG_IO) {
            logger.fine("RECVD: " + len + " bytes");
            print10bytes(buf);
        }

        return buf;
    }

    private boolean writeQueueWait() {
        if (socket.isClosed()) {
            return true;
        }

        try {
            writeQueue.wait();
        } catch (InterruptedException e) {
        }

        return socket.isClosed();
    }

    public void send(byte[] buf) throws IOException {
        send(buf, true);
    }

    public void send(byte[] buf, boolean blocking) throws IOException {
        send(buf, buf.length, blocking);
    }

    public void send(byte[] buf, int len, boolean blocking) throws IOException {
        if (master && !enable) {
            return;
        }

        if (blocking) {
            writeBlocking(buf, len);
        } else {
            writeNonblocking(buf, len);
        }
    }

    private void writeBlocking(byte[] buf, int len) throws IOException {
        synchronized (writeQueue) {
            while (writeQueue.size() > 0) {
                if (writeQueueWait()) {
                    return;
                }
            }

            if (DEBUG) {
                dos.writeInt(++msgCountSent);
                logger.fine("Wrote message " + msgCountSent);
            }

            dos.writeInt(len);
            dos.write(buf, 0, len);

            if (DEBUG_IO) {
                logger.fine("SENT: " + len + " bytes");
                print10bytes(buf);
            }
        }
    }

    private void writeNonblocking(byte[] buf, int len) throws IOException {
        synchronized (writeQueue) {
            // TODO: This might blow up the memory if too many messages
            //	 get backed up. Use ArrayBlockingQueue<E> of fixed capacity
            //	 or better still, keep track of total bytes backed up and limit it.
            if (maxWriteQueueSize > 0) {
                while (writeQueueSize >= maxWriteQueueSize) {
                    logger.finer("Waiting for socket to drain:\n" +
                            "     writeQueueSize = " + writeQueueSize + "\n" +
                            "     maxWriteQueueSize = " + maxWriteQueueSize);

                    if (writeQueueWait()) {
                        return;
                    }
                }
            }

            writeQueue.add(new Message(buf, len));
            writeQueue.notifyAll();

            writeQueueSize += len;
        }
    }

    private boolean writeClientID(BigInteger clientID) {
        try {
            byte[] buf = clientID.toByteArray();
            writeMessageBuf(buf, buf.length);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private BigInteger readClientID() {
        try {
            byte[] buf = readMessage();
            if (buf != null) {
                return new BigInteger(buf);
            }
        } catch (Exception e) {
        }

        return null;
    }

    public void readLoop() {
        while (!stopReading && !socket.isClosed()) {
            try {

                byte buf[] = readMessage();

                if ((buf != null) && (listener != null)) {
                    listener.receivedMessage(otherClientID, buf);
                }

                if (ENABLE_STATS && !master) {
                    synchronized (ClientSocket.this) {
                        numBytesRead += buf.length;
                    }
                }

            } catch (EOFException e) {
                close();
                break;

            } catch (SocketException e) {
                if (!socket.isClosed()) {
                    e.printStackTrace();
                } else {
                    logger.info("Socket closed on the other side. Closing and cleaning up...");
                }

                close();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                close();
                break;
            }
        }
        logger.info("EXIT client socket read thread\n" +
                "  remote ClientID = " + otherClientID);
    }

    public void writeLoop() {
        while (!stopWriting && !socket.isClosed()) {
            Message msg = null;

            synchronized (writeQueue) {
                boolean closed = false;

                while (!closed && (writeQueue.size() <= 0)) {
                    closed = writeQueueWait();
                }

                if (!closed && (writeQueue.size() > 0)) {
                    msg = writeQueue.remove(0);
                    writeQueueSize -= msg.len;
                    writeQueue.notifyAll();
                }
            }

            if (msg == null || msg.buf == null) {
                continue;
            }

            if (ENABLE_STATS) {
                synchronized (this) {
                    numBytesWritten += msg.len;
                }
            }

            if (!writeMessageBuf(msg.buf, msg.len)) {
                return;
            }

            if (DEBUG_IO) {
                logger.info("SENT: " + msg.len + " bytes");
                print10bytes(msg.buf);
            }
        }

        logger.info("EXIT client socket write thread\n" +
                " remote ClientID = " + otherClientID);
    }

    protected final boolean writeMessageBuf(byte[] buf, int len) {
        try {
            dos.writeInt(len);
            dos.write(buf, 0, len);
        } catch (EOFException e) {
            close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            close();
            return false;
        }

        return true;
    }

    // For Debug
    private static void print10bytes(byte[] bytes) {
        int n = (bytes.length > 10) ? 10 : bytes.length;
        for (int i = 0; i < n; i++) {
            System.err.print(Integer.toHexString(bytes[i] & 0xff) + " ");
        }
        System.err.println();
    }
}
