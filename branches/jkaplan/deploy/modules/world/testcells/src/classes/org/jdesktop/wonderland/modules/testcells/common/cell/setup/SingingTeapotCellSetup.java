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

package org.jdesktop.wonderland.modules.testcells.common.cell.setup;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.spi.CellSetupSPI;

/**
 *
 * @author jordanslott
 */
@XmlRootElement(name="singing-teapot-cell")
public class SingingTeapotCellSetup extends BasicCellSetup implements Serializable, CellSetupSPI {

    /** Default constructor */
    public SingingTeapotCellSetup() {
    }
    
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.testcells.server.cell.SingingTeapotCellMO";
    }
}
