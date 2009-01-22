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
package org.jdesktop.wonderland.client.app.sas.provider;

/**
 * A process reporter which reports app output and exit value to the given user client.
 *
 * @author deronj
 */

@ExperimentalAPI
public class ProviderUserReporter extends ProcessReporter {

    /** The provider client's Wonderland session. */
    private WonderlandServer session;

    /** The provider-to-user connection used to communicate output and exit value messages */
    private ProviderUserConnection connection;

    /** 
     * Create a new instance of ProviderUserReporter.
     *
     * @param processName The name of the process on which to report.
     * @param userClientID The ID of the user client to which to report.
     * @param The provider client's Wonderland session.
     */
    ProcessReporter (String processName, BigInteger userClientID, WonderlandServer session) {
	super(processName);
	this.userClientID = userClientID;

	connection = new ProviderUserConnection(userClientID);
	session.connection(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup () {
	session.disconnect(connection);
	super.cleanup();
    }


    /**
     * {@inheritDoc}
     */
    public void output (String str) {
	connection.output(str);
    }

    /**
     * {@inheritDoc}
     */
    public abstract void exitValue (int value) {
	connection.exitValue(value);
    }
}
