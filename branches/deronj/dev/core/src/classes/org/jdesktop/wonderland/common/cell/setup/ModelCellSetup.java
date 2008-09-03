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
package org.jdesktop.wonderland.common.cell.setup;

import org.jdesktop.wonderland.common.InternalAPI;

/**
 *
 */
@InternalAPI
public class ModelCellSetup implements CellSetup {
    private String baseURL;
    private String modelFile;
    private String checksum;
    
    public ModelCellSetup() {
        this (null);
    }

    public ModelCellSetup(String modelFile) {
        this (null, modelFile, null);
    }

    public ModelCellSetup(String baseURL, String modelFile, String checksum) {
        this.baseURL   = baseURL;
        this.modelFile = modelFile;
        this.checksum  = checksum;
    }

    /**
     * Get the model file, if one exists
     * @return the model file, or null if there is no model files
     */
    public String getModelFile() {
        return modelFile;
    }

    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
    }
    
    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public boolean compareChecksum(String checksum) {
        return (checksum == null) ?
                    (getChecksum() == null) : checksum.equals(getChecksum());
    }
    
    /**
     * Returns a string representation of this class for human-readable
     * viewing.
     */
    @Override
    public String toString() {
        return "(ModelCellSetup) file: " + this.modelFile;
    }
}
