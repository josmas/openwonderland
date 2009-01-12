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

package org.jdesktop.wonderland.modules.sample.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;

/**
 * The SampleCellSetup class is the cell that renders a sample cell in
 * world.
 * 
 * @author jkaplan
 */
@XmlRootElement(name="sample")
public class SampleCellSetup extends CellServerState
        implements Serializable, CellServerStateSPI {

    /** Default constructor */
    public SampleCellSetup() {
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.sample.server.SampleCellMO";
    }
}
