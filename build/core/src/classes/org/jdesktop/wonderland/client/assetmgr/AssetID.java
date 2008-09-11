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

package org.jdesktop.wonderland.client.assetmgr;

import org.jdesktop.wonderland.client.modules.Checksum;
import org.jdesktop.wonderland.common.AssetURI;

/**
 * An AssetID class represents a unique identification for an asset: assets
 * are uniquely identified by their URI and their (optional) checksum. This
 * class implements the equals() and hashCode() methods so that instances of
 * this class may be used as keys in hash tables, for example.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AssetID {
    /* The URI of the asset */
    private AssetURI assetURI = null;
    
    /* The optional checksum of the asset */
    private String checksum = null;
    
    /** Constructor, takes both URI and checksum */
    public AssetID(AssetURI assetURI, String checksum) {
        this.assetURI = assetURI;
        this.checksum = checksum;
    }
    
    /**
     * Returns the asset URI.
     * 
     * @return The AssetURI
     */
    public AssetURI getAssetURI() {
        return this.assetURI;
    }
    
    /**
     * Returns the asset checksum.
     * 
     * @return The asset Checksum
     */
    public String getChecksum() {
        return this.checksum;
    }
    
    /**
     * Returns a string representation of the Asset ID
     * 
     * @return A string representation
     */
    @Override
    public String toString() {
        return this.assetURI.toString() + "[" + this.checksum + "]";
    }
    
    /**
     * Returns true if the given asset ID equals this ID, false if not. Asset
     * IDs are equal if both assets have the same URI and checksum
     */
    @Override
    public boolean equals(Object obj) {
        /* Make sure the given object is of type AssetID */
        if (!(obj instanceof AssetID)) {
            return false;
        }
        
        AssetID assetID = (AssetID)obj;
        
        /* Check if they are equal optimization */
        if (this == assetID) {
            return true;
        }
        
        /* Check to make sure the URIs are equals, return false if not */
        if (this.assetURI.getURI().equals(this.getAssetURI().getURI()) == false) {
            return false;
        }
        
        /*
         * Handle the case where "this" checksum is null. If so, we return
         * true if the other is null as well, or false if the other is not
         * null. If the "other" checksum is null, the equals() method handles
         * it.
         */
        if (this.checksum == null && assetID.checksum != null) {
            return false;
        }
        else if (this.checksum == null && assetID.checksum == null) {
            return true;
        }
        
        /* Check to make sure the checksums are equal, return false if not */
        if (this.checksum.equals(assetID.checksum) == false) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.assetURI != null ? this.assetURI.getURI().hashCode() : 0);
        hash = 29 * hash + (this.checksum != null ? this.checksum.hashCode() : 0);
        return hash;
    }
}
