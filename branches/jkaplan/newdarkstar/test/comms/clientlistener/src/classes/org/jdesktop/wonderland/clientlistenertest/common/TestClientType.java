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
package org.jdesktop.wonderland.clientlistenertest.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * Client type for the test client
 * @author jkaplan
 */
public class TestClientType extends ConnectionType {
    public static final TestClientType CLIENT_ONE_TYPE = 
            new TestClientType("__TestClientOne");
    public static final TestClientType CLIENT_TWO_TYPE = 
            new TestClientType("__TestClientTwo");
    public static final TestClientType CLIENT_THREE_TYPE = 
            new TestClientType("__TestClientThree");
    
    private TestClientType(String typeName) {
        super (typeName);
    }
}
