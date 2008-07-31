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

import java.io.InputStream;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The WFSCellChildren class simply represent an array of child names for a
 * given cell. It is used to serialize this list across a network in XML form
 * or out to disk. It also contains the date the cell was last modified, so
 * that the cell loading and reloading scheme in Wonderland can check whether
 * a cell has been updated or not.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wfs-children")
public class WFSCellChildren {
    /* An array of cell children names */
    @XmlElements({
        @XmlElement(name="child")
    })
    private CellChild[] children = null;

    /* The relative path of the parent of the children */
    @XmlTransient private String relativePath = null;
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(WFSCellChildren.class);
            WFSCellChildren.unmarshaller = jc.createUnmarshaller();
            WFSCellChildren.marshaller = jc.createMarshaller();
            WFSCellChildren.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /**
     * The Child inner class simply stores the name of the cell child and the
     * date it was last modified.
     */
    public static class CellChild {
        /* The name of the cell */
        @XmlElement(name="name")
        public String name = null;
        
        /* The date the cell was last modified, or -1 if unset */
        @XmlElement(name="last_modified")
        public long lastModified = -1;
        
        /** Default constructor */
        public CellChild() {
        }
        
        /** Constructor, takes the name and last modified date */
        public CellChild(String name, long lastModified) {
            this.name = name;
            this.lastModified = lastModified;
        }
    }
    
    /** Default constructor */
    public WFSCellChildren() {
    }
    
    /** Constructor, takes the relative path and names of the children */
    public WFSCellChildren(String relativePath, CellChild[] children) {
        this.relativePath = relativePath;
        this.children = children;
    }
    
    /**
     * Returns the array of cell child names
     * 
     * @return An array of cell child names
     */
    @XmlTransient public CellChild[] getChildren() {
        return this.children;
    }
    
    /**
     * Returns the relative path of these children.
     * 
     * @return The relative path
     */
    @XmlTransient public String getRelativePath() {
        return this.relativePath;
    }
    
    /**
     * Takes the input stream of the XML and instantiates an instance of
     * the WFSCellChildren class
     * <p>
     * @param is The input stream of the XML representation
     * @throw ClassCastException If the input file does not map to WFSCellChildren
     * @throw JAXBException Upon error reading the XML stream
     */
    public static WFSCellChildren decode(String relativePath, InputStream is) throws JAXBException {
        WFSCellChildren children = (WFSCellChildren)WFSCellChildren.unmarshaller.unmarshal(is);
        children.relativePath = relativePath;
        return children;
    }
    
    /**
     * Writes the ModuleInfo class to an output stream.
     * <p>
     * @param w The output write to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        WFSCellChildren.marshaller.marshal(this, w);
    }
}
