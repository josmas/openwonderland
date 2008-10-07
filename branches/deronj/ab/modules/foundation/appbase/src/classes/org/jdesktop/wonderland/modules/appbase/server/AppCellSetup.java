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

package org.jdesktop.wonderland.modules.appbase.server;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

/**
 * The WFS setup class for AppCellMO.
 * 
 * @author deronj
 */
@XmlRootElement(name="app-cell")
public class AppCellSetup extends BasicCellSetup implements Serializable {
    
    /** Default constructor */
    public AppCellSetup() {}
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.appbase.server.AppCellMO";
    }
    
    /**
     * Returns a string representation of this class.
     *
     * @return The setup information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [AppCellSetup]";
    }
}
