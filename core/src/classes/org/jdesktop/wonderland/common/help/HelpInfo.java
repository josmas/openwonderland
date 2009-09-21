/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.common.help;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wl-help-info")
public class HelpInfo {

    /*
     * An array of entries, either folders, items, or separators.
     */
    @XmlElementRefs({
        @XmlElementRef()
    })
    private HelpMenuEntry helpEntries[] = null;
    
    /**
     * Base class for all entries in the help system
     */
    @XmlRootElement(name="entry")
    public static class HelpMenuEntry {
        /** Default constructor */
        public HelpMenuEntry() {
        }
    }
    
    /**
     * A menu entry in the help menu, has a name and ordered list of elements
     * that the submenu contains.
     */
    @XmlRootElement(name="folder")
    public static class HelpMenuFolder extends HelpMenuEntry {
        @XmlAttribute(name="name")
        public String name = null;
        
        @XmlElementRefs({
         @XmlElementRef()
        })
        public HelpMenuEntry[] entries = null;
        
        /** Default constructor */
        public HelpMenuFolder() {
        }
        
        /** Constructor */
        public HelpMenuFolder(String name, HelpMenuEntry[] entries) {
            this.name = name;
            this.entries = entries;
        }
    }
    
    /**
     * A help item in the help menu, that also has the uri of the HTML resource
     * within the module and the URL of the external page
     */
    @XmlRootElement(name="item")
    public static class HelpMenuItem extends HelpMenuEntry {
        @XmlElement(name = "name")
        public String name = null;
                
        @XmlElement(name="help-page-uri")
        public String helpURI = null;
        
        /** Default constructor */
        public HelpMenuItem() {
        }
        
        /** Constructor */
        public HelpMenuItem(String name, String uri) {
            this.name = name;
            this.helpURI = uri;
        }
    }
    
    /**
     * A separator in the help menu
     */
    @XmlRootElement(name="separator")
    public static class HelpMenuSeparator extends HelpMenuEntry {
        /** Defualt constructor */
        public HelpMenuSeparator() {
        }
    }
    
    /**
     * A category in the help menu, to be replaced by other entries
     */
    @XmlRootElement(name="category")
    public static class HelpMenuCategory extends HelpMenuEntry {
        @XmlAttribute(name="name")
        public String name = null;
        
        /** Default constructor */
        public HelpMenuCategory() {
        }
        
        /** Constructor, takes the category name */
        public HelpMenuCategory(String name) {
            this.name = name;
        }
    }
    
    private static JAXBContext jaxbContext = null;
    static {
        try {
            Collection<Class> clazz = getJAXBClasses();
            jaxbContext = JAXBContext.newInstance(clazz.toArray(new Class[] {}));
        } catch (javax.xml.bind.JAXBException excp) {
            Logger.getLogger(HelpInfo.class.getName()).log(Level.WARNING,
                    "Unable to create JAXBContext", excp);
        }
    }
    
    /**
     * Default Constructor
     */
    public HelpInfo() {
    }
    
    /**
     * Returns a collection of classes to initialize the JAXBContext path
     */
    public static Collection<Class> getJAXBClasses() {
        Collection<Class> list = new LinkedList();
        list.add(HelpInfo.class);
        list.add(HelpMenuEntry.class);
        list.add(HelpMenuItem.class);
        list.add(HelpMenuFolder.class);
        list.add(HelpMenuSeparator.class);
        list.add(HelpMenuCategory.class);
        
        return list;
    }
    
    /** Setters and getters */
    @XmlTransient public HelpMenuEntry[] getHelpEntries() { return this.helpEntries; }
    public void setHelpEntries(HelpMenuEntry[] helpEntries) { this.helpEntries = helpEntries; }
    
    /**
     * Takes the input reader of the XML stream and instantiates an instance of
     * the HelpInfo class
     * <p>
     * @param r The input reader of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleInfo
     * @throw JAXBException Upon error reading the XML file
     */
    public static HelpInfo decode(Reader r) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (HelpInfo)unmarshaller.unmarshal(r);        
    }
    
    /**
     * Writes the HelpInfo class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, w);
    }
    
    public static void main(String[] args) throws JAXBException, IOException {
        HelpInfo info = new HelpInfo();
        info.setHelpEntries(new HelpMenuEntry[] {
            new HelpMenuItem("Moving About", "wlh://fubar"),
            new HelpMenuItem("Audio", "fubar"),
            new HelpMenuSeparator(),
            new HelpMenuFolder("My item", new HelpMenuEntry[] {
                new HelpMenuItem("New Imte", "fubar1")
            })
        });
        info.encode(new FileWriter("fubar.xml"));
    }
}
