/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.serverprotocoltest.common;

import org.jdesktop.wonderland.server.comms.DefaultProtocolVersion;

/**
 *
 * @author jkaplan
 */
public class TestProtocolVersion extends DefaultProtocolVersion {
    public static final String PROTOCOL_NAME = "test_protocol";
    
    public TestProtocolVersion() {
        super (1, 0, 0);
    }
}
