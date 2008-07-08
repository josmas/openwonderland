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

/**
 * The ModuleRequires class represents a dependency this module has on another
 * module. Dependencies are declared using the unique name of the module and an
 * optional major.minor version. The version may be unspecified, in which case
 * any version is acceptable.
 * <p>
 * This convenience method isAcceptable() takes an instance of a module and
 * returns true whether it satisfies the dependency specified by the instance
 * of the ModuleRequires class.
 * <p>
 * This class follows the Java Bean pattern (default constructor, setter/getter
 * methods) so that it may be serialised to/from disk. The static decode() and
 * encode methods take an instance of a ModuleRequires class and perform the
 * loading and saving from/to disk.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleRequires {
    
    /* An array of modules that are dependencies */
    private ModuleInfo[] requires = null;
    
    /** Default constructor */
    public ModuleRequires() {}
    
    /** Constructor which takes an array of dependencies */
    public ModuleRequires(ModuleInfo[] requires) {
        this.requires = requires;
    }
    
    /* Java Bean Setter/Getter methods */
    public ModuleInfo[] getRequires() { return this.requires; }
    public void setRequires(ModuleInfo[] requires) { this.requires = requires; }
    
    /**
     * Returns the list of module dependencies as a string: name vX.Y
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Module Dependencies:\n");
        for (ModuleInfo info : requires) {
            str.append("  " + info.toString() + "\n");
        }
        return str.toString();
    }
    
    /**
     * Returns true if the Module object passed as an argument satisifies the
     * dependency requirement specified by this class.
     */
    public boolean isAcceptable(Module module) {
        return true; // XXX
    }
    
    /**
     * Takes the input stream of the XML file and instantiates an instance of
     * the ModuleRequiests class
     * <p>
     * @param is The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to WFSAlises
     * @throw ArrayIndexOutOfBoundsException If the file contains no objects
     */
    public static ModuleRequires decode(InputStream is) {
        XMLDecoder     d       = new XMLDecoder(new BufferedInputStream(is));
        ModuleRequires version = (ModuleRequires)d.readObject();
        d.close();
        
        return version;
    }
    
    /**
     * Writes the ModuleRequires class to an output stream.
     * <p>
     * @param os The output stream to write to
     */
    public void encode(OutputStream os) {
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(os));
        e.writeObject(this);
        e.close();
    }
    
    /**
     * Main method which writes a sample ModuleRequires class to disk
     */
    public static void main(String args[]) {
        try {
            ModuleRequires requires = new ModuleRequires();
            ModuleInfo info1 = new ModuleInfo("MPK20 Demo", 1, ModuleInfo.VERSION_UNSET);
            ModuleInfo info2 = new ModuleInfo("Basic Textures", ModuleInfo.VERSION_UNSET, ModuleInfo.VERSION_UNSET);
            requires.setRequires(new ModuleInfo[] { info1, info2 });
            requires.encode(new FileOutputStream(new File("/Users/jordanslott/module-wlm/requires.xml")));
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}
