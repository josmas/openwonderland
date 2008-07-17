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
package org.jdesktop.wonderland.wfs;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    @Override
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
     * Writes the XMLAliases class to an output stream.
     * <p>
     * @param os The output stream to write to
     */
    public void encode(OutputStream os) {
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(os));
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
            aliases.encode(new FileOutputStream(new File("aliases.xml")));
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}
