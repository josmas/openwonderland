/**
 * Project Looking Glass
 *
 * $RCSfile: WFSCell.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.2 $
 * $Date: 2007/10/17 17:11:06 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.utils.wfs;

import java.io.FileNotFoundException;
import org.jdesktop.wonderland.server.setup.WfsCellMOSetup;

/**
 * The WFSCell interface represents a single cell in a WFS, whether it be
 * located on disk, in an archive file or over the network somewhere.
 *
 * @author jsott
 */
public interface WFSCell {
    /**
     * Returns the instance of a subclass of the CellProperties class that is
     * decoded from the XML file representation.
     *
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    public <T extends WfsCellMOSetup> T decode() throws FileNotFoundException, InvalidWFSCellException;
    
    /**
     * Returns the canonical name for this cell
     */
    public String getCanonicalName();
    
    /**
     * Returns the name of the cell, without the standard suffix
     */
    public String getCellName();
    
    /**
     * Returns the time (in milliseconds since the epoch) this file was last
     * modified.
     */
    public long getLastModified();
}
