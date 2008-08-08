/**
 * Project Wonderland
 *
 * $RCSfile: Checksum.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/05/04 23:11:34 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.common;

import org.jdesktop.wonderland.client.datamgr.AssetDB;

/**
 * TODO move to common package
 */
public class ChecksumSha1 extends Checksum {
    private byte[] checksum;

    public ChecksumSha1(byte[] checksum) {
        this.checksum = checksum.clone();
    }

    /**
     * Returns true if this checksum is equals to the supplied checksum
     * @param checksum
     * @return
     */
    public boolean equals(Checksum checksum2) {
        if (!(checksum2 instanceof ChecksumSha1)) {
            return false;
        }
        ChecksumSha1 c2 = (ChecksumSha1) checksum2;
        if (c2.checksum.length != this.checksum.length) {
            return false;
        }
            
        for (int i = 0; i < checksum.length; i++) {
            if (checksum[i] != c2.checksum[i]) {
                return false;
            }
        }

        return true;
    }
        
    /**
     * HACK ALERT !
     * toString must return a toHexString as it's used
     * to store this in the db
     * @return
     */
    @Override
    public String toString() {
        return Checksum.toHexString(checksum);
    }
}