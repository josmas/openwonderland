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

class WriteStatistics extends StatisticsSet {

    // The number of bytes written to the slave
    private long numBytesWritten;

    // The number of bytes in the queue's buffers
    // (Same as writeQueueSize)
    private long numBytesInQueue;

    // The number of messages in the queue
    private long numMsgsInQueue;

    // The number of bytes in the socket write buffer
    private long numBytesInSocketBuf;

    protected WriteStatistics () {
	super("Socket Write");
    }

    // Collect the latest stats
    protected void probe () {

	synchronized (ClientSocket.this) {
	    numBytesWritten = ClientSocket.this.numBytesWritten;
	}

	// Don't need to lock because we never reset these
	numBytesInQueue = writeQueueSize;
	numMsgsInQueue = writeQueue.size();

	try {
	    numBytesInSocketBuf = socket.getSendBufferSize();
	} catch (SocketException ex) {
	    numBytesInSocketBuf = 0;
	}
    }

    protected void reset () {
	synchronized (ClientSocket.this) {
	    ClientSocket.this.numBytesWritten = 0L;
	}
    }

    protected void accumulate (StatisticsSet cumulativeStats) {
	WriteStatistics stats = (WriteStatistics) cumulativeStats;
	stats.numBytesWritten += numBytesWritten;
	stats.numBytesInQueue += numBytesInQueue;
	stats.numMsgsInQueue += numMsgsInQueue;
	stats.numBytesInSocketBuf += numBytesInSocketBuf;
    }

    protected void max (StatisticsSet maxStats) {
	WriteStatistics stats = (WriteStatistics) maxStats;
	stats.numBytesWritten = max(stats.numBytesWritten, numBytesWritten);
	stats.numBytesInQueue = max(stats.numBytesInQueue, numBytesInQueue);
	stats.numMsgsInQueue = max(stats.numMsgsInQueue, numMsgsInQueue);
	stats.numBytesInSocketBuf = max(stats.numBytesInSocketBuf, numBytesInSocketBuf);
    }

    protected void printStats () {
	System.err.println("numBytesWritten = " + numBytesWritten);
	System.err.println("numBytesInQueue = " + numBytesInQueue);
	System.err.println("numMsgsInQueue = " + numMsgsInQueue);
	System.err.println("numBytesInSocketBuf = " + numBytesInSocketBuf);
    }

    protected void printStatsAndRates (double timeSecs) {
	printStats();

	// Calculate and print rates
	double numBytesWrittenPerSec = numBytesWritten / timeSecs;
	System.err.println("numBytesWrittenPerSec = " + numBytesWrittenPerSec);
    }
}

