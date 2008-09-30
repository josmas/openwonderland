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
package org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.config;

import org.jdesktop.wonderland.common.cell.config.*;

/**
 * The CalladaCellConfig class represents the information communicated
 * between the client and Darkstar server for collada model cells.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class JmeColladaCellConfig extends CellConfig {
    /* The unique URL that describes the model data */
    private String modelURI = null;
    
    /** Default constructor */
    public JmeColladaCellConfig() {
    }
    
    /** Constructor, takes the model URI */
    public JmeColladaCellConfig(String modelURI) {
        this.modelURI = modelURI;
    }
    
    /**
     * Returns the unique model URI, null if none.
     * 
     * @return The unique model URI
     */
    public String getModelURI() {
        return this.modelURI;
    }
    
    /**
     * Sets the unique model URI, null for none.
     * 
     * @param modelURI The unique model URI
     */
    public void setModelURI(String modelURI) {
        this.modelURI = modelURI;
    }
}