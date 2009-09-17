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
package org.jdesktop.wonderland.modules.microphone.common;


import com.jme.math.Vector3f;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.common.utils.jaxb.Vector3fAdapter;

/**
 * The MicrophoneCellServerState class is the cell that renders a microphone cell in
 * world.
 * 
 * @author jprovino
 */
@XmlRootElement(name="microphone-cell")
@ServerState
public class MicrophoneCellServerState extends CellServerState {

    /** Default constructor */
    public MicrophoneCellServerState() {
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.microphone.server.cell.MicrophoneCellMO";
    }

}
