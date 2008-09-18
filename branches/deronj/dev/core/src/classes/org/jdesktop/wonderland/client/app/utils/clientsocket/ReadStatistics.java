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

class ReadStatistics extends StatisticsSet {

    // The number of bytes read from the master
    public long numBytesRead;

    protected ReadStatistics () {
	super("Socket Read");
    }

    protected void probe () {
	synchronized (ClientSocket.this) {
	    numBytesRead = ClientSocket.this.numBytesRead;
	}
    }

    public void reset () {
	synchronized (ClientSocket.this) {
	    ClientSocket.this.numBytesRead = 0L;
	}
    }

    protected void accumulate (StatisticsSet cumulativeStats) {
	ReadStatistics stats = (ReadStatistics) cumulativeStats;
	stats.numBytesRead += numBytesRead;
    }

    protected void max (StatisticsSet maxStats) {
	ReadStatistics stats = (ReadStatistics) maxStats;
	stats.numBytesRead = max(stats.numBytesRead, numBytesRead);
    }

    protected void printStats () {
	System.err.println("numBytesRead = " + numBytesRead);
    }

    protected void printStatsAndRates (double timeSecs) {
	printStats();

	// Calculate and print rates
	double numBytesReadPerSec = numBytesRead / timeSecs;
	System.err.println("numBytesReadPerSec = " + numBytesReadPerSec);
    }
}

