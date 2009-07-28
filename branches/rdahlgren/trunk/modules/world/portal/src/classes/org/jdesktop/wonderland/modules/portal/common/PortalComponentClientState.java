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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.utils.jaxb.QuaternionAdapter;
import org.jdesktop.wonderland.common.utils.jaxb.Vector3fAdapter;

/**
 * Client state for portal cell component
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class PortalComponentClientState extends CellComponentClientState {
    private String serverURL;
    private Vector3f location;
    private Quaternion look;

    /** Default constructor */
    public PortalComponentClientState() {
    }

    public PortalComponentClientState(String serverURL, Vector3f location,
                                      Quaternion look)
    {
        this.serverURL = serverURL;
        this.location = location;
        this.look = look;
    }

    @XmlElement
    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    @XmlElement
    @XmlJavaTypeAdapter(Vector3fAdapter.class)
    public Vector3f getLocation() {
        return location;
    }

    public void setLocation(Vector3f location) {
        this.location = location;
    }

    @XmlElement
    @XmlJavaTypeAdapter(QuaternionAdapter.class)
    public Quaternion getLook() {
        return look;
    }

    public void setLook(Quaternion look) {
        this.look = look;
    }
}
