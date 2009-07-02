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
package org.jdesktop.wonderland.modules.buttonboxtest1.common.cell;


import com.jme.math.Vector2f;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;
import org.jdesktop.wonderland.modules.testcells.common.cell.state.SimpleShapeCellServerState;

/**
 * The WFS server state class for ButtonBoxTestMO.
 * 
 * @author deronj
 */
@XmlRootElement(name="buttonboxtest1-cell")
public class ButtonBoxTest1ServerState 
    extends SimpleShapeCellServerState 
    implements Serializable, CellServerStateSPI 
{
    
    /** Default constructor */
    public ButtonBoxTest1ServerState() {}
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.buttonboxtest1.server.cell.ButtonBoxTest1MO";
    }
}
