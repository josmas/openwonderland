/**
 * Project Wonderland
 *
 * $RCSfile: ChecksumManagerGLO.java,v $
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
 * $Revision: 1.8 $
 * $Date: 2007/10/26 17:47:16 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * Manages asset checksums
 *
 * TODO put checksum in a JavaDB instead of using darkstar.
 *
 * @author paulby
 */
@ExperimentalAPI
public class ChecksumManager implements ManagedObject, Serializable {
    
    // Mapping of filename to checksums
    private HashMap<String, String> checksums = new HashMap<String, String>();

    private static final String BINDING_NAME="CHECKSUM_MANAGER";
    
    private static ChecksumManager getChecksumManagerMO() {
        return (ChecksumManager) AppContext.getDataManager().getBinding(BINDING_NAME);
    }
    
    /**
     * Setup the checksum db from the provided asset server
     */
    public ChecksumManager(String assetServerUrl) {
        // File format is checksum filedata filename
        AppContext.getDataManager().setBinding(BINDING_NAME, this);
        
        long startTime=System.currentTimeMillis();
        try {
            URL url = new URL(assetServerUrl+"/checksums.txt");
            StreamTokenizer tok = new StreamTokenizer(new InputStreamReader(new BufferedInputStream(url.openStream())));
            tok.resetSyntax();
            tok.wordChars(33, 127);
            tok.whitespaceChars(' ', ' ');
            tok.eolIsSignificant(false);
            int tokType;
            tokType = tok.nextToken();
            while(tokType!=StreamTokenizer.TT_EOF) {
                String checksum = tok.sval;
                tokType = tok.nextToken();
                String date = tok.sval;
                tokType = tok.nextToken();
                String filename = tok.sval;
                
                checksums.put(filename, checksum);
                
                tokType = tok.nextToken();
                tokType = tok.nextToken();
            } 
            
        } catch (Exception ex) {
            Logger.getAnonymousLogger().config("Failed to get checksum file");
        }
        
        Logger.getLogger("wonderland").info("Checksum setup took "+(System.currentTimeMillis()-startTime)+" ms.");        
    }
        
    String getChecksumImpl(String file) {
        String ret;
        if (file==null) {
            ret = "ignore_checksum";
        } else {
            ret = checksums.get(file);
            if (ret==null) {
                ret = "ignore_checksum";
                Logger.getLogger("wonderland").warning("No Checksum for "+file);
            }
        }
        return ret;
    }
    
    public static String getChecksum(String file) {
        return ChecksumManager.getChecksumManagerMO().getChecksumImpl(file);
    }
    
}
