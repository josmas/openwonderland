/**
 * Project Looking Glass
 *
 * $RCSfile: WFSAliases.java,v $
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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The WFSAlises class represents a series of mappings between 'wfs' type URIs
 * and disk locations within a Wonderland File System (WFS). Currently this
 * mapping is stored as a hashmap of Strings, and there is no checking of
 * whether these Strings map to valid names.
 * <p>
 * This class follows the Java Bean pattern (default constructor, setter/getter
 * methods) so that it may be serialised to/from disk. The static decode() and
 * encode methods take an instance of a WFSAlises class and perform the loading
 * and saving from/to disk.
 *
 * @author jslott
 */
public class WFSAliases {
    /* Java Bean Properties */
    private HashMap<String, String> aliases = new HashMap<String, String>();
    
    /** Default constructor */
    public WFSAliases() {}
    
    /* Java Bean Setter/Getter methods */
    public HashMap<String, String> getAliases() { return this.aliases; }
    public void setAliases(HashMap<String, String> aliases) { this.aliases = aliases; }
    
    /**
     * Returns the aliases as a string, where each line represents a single entry
     * of the form: <aliases>.<location>
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> i = this.aliases.entrySet().iterator();
        while (i.hasNext() == true) {
            Map.Entry<String, String> entry = i.next();
            sb.append(entry.toString() + "\n");
        }
        return sb.toString();
    }
    
    /**
     * Takes the input stream of the XML file on disk (or elsewhere) and
     * instantiates an instance of the WFSAliases class
     * <p>
     * @param is The input stream of the XML file
     * @throw FileNotFoundException If the input file does not exist
     * @throw ClassCastException If the input file does not map to WFSAlises
     * @throw ArrayIndexOutOfBoundsException If the file contains no objects
     */
    public static WFSAliases decode(InputStream is) {
        WFSAliases aliases = null;
        try {
            XMLDecoder d = new XMLDecoder(new BufferedInputStream(is));
            aliases      = (WFSAliases)d.readObject();
            d.close();
        } catch (java.lang.Exception excp) {
            excp.printStackTrace();
        }
        
        
        return aliases;
    }
    
    /**
     * Takes the File of the XML file on disk (or elsewhere) and writes the
     * XMLAliases class to that file.
     * <p>
     * @param file The output XML file to encode
     * @throw FileNotFoundException If the file cannot be created
     * @throw SecurityException If the file is not permitted to be created
     *
     */
    public void encode(File file) throws FileNotFoundException {
        FileOutputStream fos     = new FileOutputStream(file);
        XMLEncoder       e       = new XMLEncoder(new BufferedOutputStream(fos));
        e.writeObject(this);
        e.close();
    }
    
    /**
     * Main method which writes a sample WFSAlises class to disk
     */
    public static void main(String args[]) {
        try {
            WFSAliases aliases = new WFSAliases();
            HashMap<String, String> a = new HashMap<String, String>();
            a.put("rooms/office", "cells/offce/room.xml");
            aliases.encode(new File("aliases.xml"));
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}