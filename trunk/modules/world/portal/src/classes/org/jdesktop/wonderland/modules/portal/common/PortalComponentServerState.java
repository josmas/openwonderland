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

package org.jdesktop.wonderland.modules.portal.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Translation;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Rotation;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for portal cell component
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@XmlRootElement(name="portal-component")
@ServerState
public class PortalComponentServerState extends CellComponentServerState {
    private String serverURL;
    private Translation location = new Translation();
    private Rotation look = new Rotation();

    /** Default constructor */
    public PortalComponentServerState() {
    }

    public PortalComponentServerState(String serverURL, Translation location,
                                      Rotation look)
    {
        this.serverURL = serverURL;
        this.location = location;
        this.look = look;
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.portal.server.PortalComponentMO";
    }

    @XmlElement
    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    @XmlElement
    public Translation getLocation() {
        return location;
    }

    public void setLocation(Translation location) {
        this.location = location;
    }

    @XmlElement
    public Rotation getLook() {
        return look;
    }

    public void setLook(Rotation look) {
        this.look = look;
    }
}
