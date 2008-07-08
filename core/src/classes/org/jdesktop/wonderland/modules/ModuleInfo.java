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

package org.jdesktop.wonderland.modules;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * The ModuleInfo class represents the basic information about a module: its
 * unique name and its major.minor version.
 * <p>
 * This class follows the Java Bean pattern (default constructor, setter/getter
 * methods) so that it may be serialised to/from disk. The static decode() and
 * encode methods take an instance of a ModuleVersion class and perform the
 * loading and saving from/to disk.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleInfo implements Serializable {
    /* Flag indicating that a version component is unset */
    public static final int VERSION_UNSET = -1;
    
    /* The unique module name */
    private String name = null;
    
    /* The major and minor version numbers */
    private int major = ModuleInfo.VERSION_UNSET;
    private int minor = ModuleInfo.VERSION_UNSET;
    
    /** Default constructor */
    public ModuleInfo() {}
    
    /** Constructor which takes major/minor version number */
    public ModuleInfo(String name, int major, int minor) {
        this.name  = name;
        this.major = major;
        this.minor = minor;
    }
    
    /* Java Bean Setter/Getter methods */
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public int getMajor() { return this.major; }
    public void setMajor(int major) { this.major = major; }
    public int getMinor() { return this.minor; }
    public void setMinor(int minor) { this.minor = minor; }
    
    /**
     * Returns the version as a string: <major>.<minor>
     */
    @Override
    public String toString() {
        return "Module Info: " + this.getName() + "(v" +
                Integer.toString(this.getMajor()) + "." +
                Integer.toString(this.getMinor()) + ")";
    }
    
    /**
     * Takes the input stream of the XML file and instantiates an instance of
     * the WFSVersion class
     * <p>
     * @param is The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to WFSAlises
     * @throw ArrayIndexOutOfBoundsException If the file contains no objects
     */
    public static ModuleInfo decode(InputStream is) {
        XMLDecoder d       = new XMLDecoder(new BufferedInputStream(is));
        ModuleInfo version = (ModuleInfo)d.readObject();
        d.close();
        
        return version;
    }
    
    /**
     * Writes the XMLVersion class to an output stream.
     * <p>
     * @param os The output stream to write to
     */
    public void encode(OutputStream os) {
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(os));
        e.writeObject(this);
        e.close();
    }
    
    /**
     * Main method which writes a sample WFSVersion class to disk
     */
    public static void main(String args[]) {
        try {
            ModuleInfo info = new ModuleInfo();
            info.setName("MPK20 Demo");
            info.setMajor(5);
            info.setMinor(3);
            info.encode(new FileOutputStream(new File("/Users/jordanslott/module-wlm/module.xml")));
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}
