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

/**
 * The ModuleResourceURI class represents a universal resource description of
 * resources contained within modules. It is a string that has three components
 * and takes the general form of:
 * <p>
 * wlm:<module name>:<resource path>
 * <p>
 * where 'wlm' identifies the URI as a Wonderland module resource, the module
 * name is a unique name of a module, and the resource path is the path within
 * that module corresponding to a particular resource. This resource may either
 * be some artwork or runnable piece of code.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleResourceURI {
    private String moduleName   = null;  /* The unique name of the module */
    private String resourcePath = null;  /* The path of the resource */
    
    /** Constructor, takes the module name and resource path as arguments */
    public ModuleResourceURI(String moduleName, String resourcePath) {
        this.moduleName = moduleName;
        this.resourcePath = resourcePath;
    }
    
    /**
     * Returns the unique name of the module.
     * <p>
     * @return The unique name of the module
     */
    public String getModuleName() {
        return this.moduleName;
    }
    
    /**
     * Sets the unique name of the module. This method assumes the name is not
     * null.
     * <p>
     * @param moduleName The unique name of the module
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    /**
     * Returns the path of the resource within the module.
     * <p>
     * @return The path of the resource within the module
     */
    public String getResourcePath() {
        return this.resourcePath;
    }
    
    /**
     * Sets the path of the resource within the module. This method assumes the
     * path is not null.
     * <p>
     * @param resourcePath The path of the resource within the module
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }
    
    /**
     * Takes a string of the form "wlm:<module name>:<resource path> and returns
     * a new ModuleResourceURI object. If the given string is invalid, this
     * method throws IllegalArgumentException.
     * <p>
     * @param uri The module resource URI
     * @return A new ModuleResourceURI object for the URI
     * @throw IllegalArgumentException If the given URI is invalid.
     */
    public static ModuleResourceURI parse(String uri) throws IllegalArgumentException {
        /*
         * As a sanity check, see if null is passed as an argument
         */
        if (uri == null) {
            throw new IllegalArgumentException("Null URI given to parse() method");
        }
        
        
        /*
         * Look for the ":" tokens and take the 2nd and 3rd parts of it. If
         * either is null (empty string) then throw an exception. Also, check
         * to see if the first token is "wlm".
         * <p>
         * Strictly speaking, if there is a final (and third) ":", then the
         * URI is still parsed properly. This is just how Sring.split() works,
         * there is no great need to reject this slightly malformed URI.
         */
        String tokens[] = uri.split(":");
        if (tokens.length != 3 || tokens[0].compareTo("wlm") != 0) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        
        if (tokens[1].compareTo("") == 0 || tokens[2].compareTo("") == 0) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        return new ModuleResourceURI(tokens[1], tokens[2]);
    }
    
    /**
     * Test harness method
     */
    private static void parseTest(String uri) {
        try {
            ModuleResourceURI.parse(uri);
        } catch (IllegalArgumentException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /**
     * Testing main method
     */
    public static void main(String args[]) {
        ModuleResourceURI.parseTest("wlm:mpk20:textures/fubar.png");
        ModuleResourceURI.parseTest(null);
        ModuleResourceURI.parseTest("wlc:mpk20:textures/fubar.png");
        ModuleResourceURI.parseTest("::");
        ModuleResourceURI.parseTest(":mpk20:textures/fubar.png");
        ModuleResourceURI.parseTest("wlm::texturs/fubar.png");
        ModuleResourceURI.parseTest("wlm::");
        ModuleResourceURI.parseTest("wlm:mpk20:textures/fubar.png:");
        ModuleResourceURI.parseTest("wlm:mpk20:");
    }
}
