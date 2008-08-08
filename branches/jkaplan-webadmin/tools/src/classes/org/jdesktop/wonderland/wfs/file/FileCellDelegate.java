/**
 * Project Looking Glass
 *
 * $RCSfile: WFSFileCell.java,v $
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
 * $Revision: 1.3.8.4 $
 * $Date: 2008/04/08 10:44:31 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.wfs.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.cells.BasicCellSetup;
import org.jdesktop.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.delegate.CellDelegate;
import org.jdesktop.wonderland.wfs.delegate.DirectoryDelegate;


/**
 * The WFSCell class represents an XML file on disk corresponding to the
 * definition of Wonderland cell. This class extends WFSCell.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class FileCellDelegate implements CellDelegate {
    
    /* The File of the referring WFS Directory */
    private File file = null;
    
    /** Constructor, takes a new instance of the cell file */
    public FileCellDelegate(File file) {
        this.file = file;
    }
    
    /**
     * Creates a new directory delegate for the cell directory containing the
     * child cells of this cell.
     * 
     * @return A new directory delegate.
     */
    public DirectoryDelegate createDirectoryDelegate() {
        /*
         * Parse off the -wlc.xml suffix from the cell file and put a -wld
         * suffix instead.
         */
        Logger logger = WFS.getLogger();
        try {
            String filePath = this.getFile().getAbsolutePath();
            int index = filePath.indexOf(WFS.CELL_FILE_SUFFIX);
            String path = filePath.substring(0, index) + WFS.CELL_DIRECTORY_SUFFIX;
            File newFile = new File(path);
            return new FileDirectoryDelegate(newFile);
        } catch (java.lang.IndexOutOfBoundsException excp) {
            /* Quietly return null */
        }
        return null;
    }

    /**
     * Calculate the last modified date of the file this cell represents
     * @return the time this file was last modified
     */
    public long getLastModified() {
        return this.getFile().lastModified();
    }

    /**
     * Returns true if the cell directory associated with this cell exists,
     * false if not.
     * 
     * @return True if the cell's directory exists, false if not
     */
    public boolean cellDirectoryExists() {
        /*
         * Parse off the cell file suffix. Create a new file object, and
         * check if it exists
         */
        String fileName = this.getFile().getAbsolutePath();
        String path = WFSCell.stripCellFileSuffix(fileName) + WFS.CELL_DIRECTORY_SUFFIX;
        File newFile = new File(path);
        return newFile.exists();
    }
    
    /**
     * Creates the cell's directory on the medium, if it does not exist
     */
    public void createCellDirectory() {
        /*
         * Parse off the cell file suffix. Create a new file object, and
         * if it does not exist, create it
         */
        String fileName = this.getFile().getAbsolutePath();
        String path = WFSCell.stripCellFileSuffix(fileName) + WFS.CELL_DIRECTORY_SUFFIX;
        File newFile = new File(path);
        if (newFile.exists() == false) {
            newFile.mkdir();
        }
    }
    
    /**
     * Removes the cell's directory on the medium, if it exists.
     */
    public void removeCellDirectory() {
        /*
         * Parse off the cell file suffix. Create a new file object, and
         * if it exists, delete it
         */
        String fileName = this.getFile().getAbsolutePath();
        String path = WFSCell.stripCellFileSuffix(fileName) + WFS.CELL_DIRECTORY_SUFFIX;
        File newFile = new File(path);
        WFSFileUtils.deleteDirectory(newFile);
    }
   
    /**
     * Returns the File associated with this WFS file
     */
    private File getFile() {
        return this.file;
    }
        
    /**
     * Returns the instance of a subclass of the CellProperties class that is
     * decoded from the XML file representation.
     *
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    public <T extends BasicCellSetup> T decode() throws FileNotFoundException, InvalidWFSCellException {
        /*
         * Decode the XML file from disk and return the Properties class read
         */
        T setup = null;
        try {
            FileInputStream fis = new FileInputStream(this.getFile());
            setup = (T)BasicCellSetup.decode(new InputStreamReader(fis));
        } catch (java.lang.Exception excp) {
            /* Ignore any exception here and just set the class to null */
            setup = null;
        }
        
        /* If the object returned is invalid, check if null */
        if (setup == null) {
            throw new InvalidWFSCellException("Invalid Cell from file: " + this.getFile());
        }
        
        return setup;
    }
 
    /**
     * Updates the cell's properties. Depending upon the type of the WFS, this
     * method may immediately serialize the cell properties to disk or store
     * them for later serialization.
     * 
     * @param cellSetup The cell properties class
     * @throw IOException Upon general I/O error
     * @throw InvalidWFSCellException If the cell properties is invalid
     */
    public <T extends BasicCellSetup> void encode(T cellSetup) throws IOException, InvalidWFSCellException, JAXBException {
        FileWriter fw = new FileWriter(this.getFile());
        cellSetup.encode(fw);
    }
}
