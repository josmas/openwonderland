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

package org.jdesktop.wonderland.wfs.delegate;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.cells.BasicCellSetup;
import org.jdesktop.wonderland.wfs.InvalidWFSCellException;

/**
 * The CellDelegate interface represents the methods that must be implemented
 * for each different kind of medium for a WFS (disk, archive, memory). The
 * methods here are used to manipulate the file in the medium.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface CellDelegate {
    
    /**
     * Calculate the last modified date of the file this cell represents -- for
     * cells in memory, this is always zero.
     * 
     * @return The time this file was last modified, always zero.
     */
    public long getLastModified();
    
    /**
     * Creates a new directory delegate for the cell directory containing the
     * child cells of this cell.
     * 
     * @return A new directory delegate.
     */
    public DirectoryDelegate createDirectoryDelegate();

    /**
     * Returns the instance of a subclass of the CellProperties class that is
     * decoded from the XML file representation.
     *
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    public <T extends BasicCellSetup> T decode() throws FileNotFoundException, InvalidWFSCellException;
 
    /**
     * Updates the cell's properties. Depending upon the type of the WFS, this
     * method may immediately serialize the cell properties to disk or store
     * them for later serialization.
     * 
     * @param cellSetup The cell properties class
     * @throw IOException Upon general I/O error
     * @throw InvalidWFSCellException If the cell properties is invalid
     */
    public <T extends BasicCellSetup> void encode(T cellSetup) throws IOException, InvalidWFSCellException, JAXBException;
    
    /**
     * Returns true if the cell directory associated with this cell exists,
     * false if not.
     * 
     * @return True if the cell's directory exists, false if not
     */
    public boolean cellDirectoryExists();
    
    /**
     * Creates the cell's directory on the medium, if it does not exist
     */
    public void createCellDirectory();
    
    /**
     * Removes the cell's directory on the medium, if it exists.
     */
    public void removeCellDirectory();
}
