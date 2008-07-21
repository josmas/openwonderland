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

package org.jdesktop.wonderland.cells;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The SimpleTerrainCellSetup is the setup information for "simple terrain"
 * cells, that simply display a static model in 3D space. It extends the
 * BasicCellSetup class for basic cell setup information and also includes the
 * following cell components.
 * <p>
 * StaticModelCellSetup: describes the location of the model geometry file.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="simple-terrain-cell")
public class SimpleTerrainCellSetup extends BasicCellSetup {

    /** Default constuctor */
    public SimpleTerrainCellSetup() {
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.server.cell.SimpleTerrainCellMO";
    }
}
