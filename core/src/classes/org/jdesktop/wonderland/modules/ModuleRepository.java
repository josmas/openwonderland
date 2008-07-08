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
 * A module's repository information, as represented by this class, represents
 * the collection of art assets and where they can be found over the Internet.
 * <p>
 * A "repository" is a collection of art assets located somewhere on the
 * Internet and available to clients for download. Two fundamental kinds of
 * repositories exist: master and mirror. Master repositories represent the
 * primary copy of art assets; mirror repositories represent copies of the
 * art assets, available perhaps by a server that provides quicker downloads.
 * <p>
 * This class stores a list of "resources", each some piece of art generally.
 * This list is optional -- entries need not exist for resources that exist
 * themselves in the module. This list, therefore, provides a means to include
 * a resource in the module without including the actual artwork itself.
 * <p>
 * This class also stores the name of the master repository where the artwork
 * can be downloaded and also a list of mirror repositories. Both the master
 * and mirror repositories are optional. If no master or mirror is specified,
 * then it is assumed the artwork is made available by the Wonderland server
 * in which the module is installed.
 * <p>
 * This class follows the Java Bean pattern (default constructor, setter/getter
 * methods) so that it may be serialised to/from disk. The static decode() and
 * encode methods take an instance of a ModuleVersion class and perform the
 * loading and saving from/to disk.
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleRepository implements Serializable {
        
    private String[] resources = null;   /* The list of module resources */
    private String   master    = null;   /* The name of the master       */
    private String[] mirrors   = null;   /* The list of mirrors          */
    
    /** Default constructor */
    public ModuleRepository() {}
    
    /* Setters and getters */
    public String[] getResources() { return this.resources; }
    public void setResources(String[] resources) { this.resources = resources; }
    public String getMaster() { return this.master; }
    public void setMaster(String master) { this.master = master; }
    public String[] getMirrors() { return this.mirrors; }
    public void setMirrors(String[] mirrors) { this.mirrors = mirrors; }
    
        /**
     * Returns the version as a string: <major>.<minor>
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Module Repository:\n");
        str.append("Master:\n  " + this.getMaster() + "\n");
        str.append("Mirrors:\n");
        for (String mirror : mirrors) {
            str.append("  " + mirror + "\n");
        }
        str.append("Resources:\n");
        for (String resource : resources) {
            str.append("  " + resource + "\n");
        }
        return str.toString();
    }
    
    /**
     * Takes the input stream of the XML file and instantiates an instance of
     * the ModuleRepository class
     * <p>
     * @param is The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to its class
     * @throw ArrayIndexOutOfBoundsException If the file contains no objects
     */
    public static ModuleRepository decode(InputStream is) {
        XMLDecoder d         = new XMLDecoder(new BufferedInputStream(is));
        ModuleRepository rep = (ModuleRepository)d.readObject();
        d.close();
        
        return rep;
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
            ModuleRepository rep = new ModuleRepository();
            rep.setMaster("http://www.arts.com/");
            rep.setMirrors(new String[] { "http://www.foo.com" });
            rep.setResources(new String[] { "mpk20/models/building.j3s.gz"});
            rep.encode(new FileOutputStream(new File("/Users/jordanslott/module-wlm/repository.xml")));
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}
