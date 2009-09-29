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
package org.jdesktop.wonderland.web.help;

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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.help.HelpInfo.HelpMenuEntry;
import org.jdesktop.wonderland.common.help.HelpInfo.HelpMenuFolder;
import org.jdesktop.wonderland.common.help.HelpInfo.HelpMenuItem;
import org.jdesktop.wonderland.common.help.HelpInfo.HelpMenuSeparator;

/**
 * Defines the format of the layout.xml file. TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wl-help-content")
public class HelpContent {
    /*
     * An array of content entries. These must be of type HelpMenuContent,
     * although beneath that an entire Help menu tree may exist.
     */
    @XmlElements({
        @XmlElement(name="content")
    })
    private HelpMenuContent helpContent[] = null;
    
    /**
     * Content in the help menu, assigned to a category
     */
    public static class HelpMenuContent extends HelpMenuEntry {
        @XmlAttribute(name="category", required=true)
        public String category = null;
        
        @XmlElementRefs({
            @XmlElementRef()
        })
        public HelpMenuEntry[] entries = null;
        
        /** Constructor, takes the args */
        public HelpMenuContent(String category, HelpMenuEntry[] entries) {
            this.category = category;
            this.entries = entries;
        }
        
        /** Default constructor */
        public HelpMenuContent() {
        }
    }
    
    /* The JAXB Context for later use */
    private static JAXBContext context;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            Collection<Class> clazz = HelpContent.getJAXBClasses();
            context = JAXBContext.newInstance(clazz.toArray(new Class[] {}));
        } catch (javax.xml.bind.JAXBException excp) {
            Logger.getLogger(HelpContent.class.getName()).log(Level.WARNING,
                    "Unable to get JAXBContext", excp);
        }
    }
    
    /**
     * Returns a collection of classes to initialize the JAXBContext path
     */
    public static Collection<Class> getJAXBClasses() {
        Collection<Class> list = new LinkedList();
        list.add(HelpMenuEntry.class);
        list.add(HelpMenuItem.class);
        list.add(HelpMenuFolder.class);
        list.add(HelpMenuSeparator.class);
        list.add(HelpMenuContent.class);
        list.add(HelpContent.class);
        
        return list;
    }
    
    /** Setters and getters */
    @XmlTransient public HelpMenuContent[] getHelpContent() { return this.helpContent; }
    public void setHelpContent(HelpMenuContent[] helpContent) { this.helpContent = helpContent; }
    
    /**
     * Takes the input reader of the XML stream and instantiates an instance of
     * the HelpInfo class
     * <p>
     * @param r The input reader of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleInfo
     * @throw JAXBException Upon error reading the XML file
     */
    public static HelpContent decode(Reader r) throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (HelpContent) unmarshaller.unmarshal(r);
    }
    
    /**
     * Writes the HelpInfo class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(this, w);
    }
    
    public static void main(String[] args) throws JAXBException, IOException {
        HelpContent content = new HelpContent();
        content.setHelpContent(new HelpMenuContent[] {
            new HelpMenuContent("core-navigation", new HelpMenuEntry[] {
                new HelpMenuItem("Getting Arround", "uri")
            })
        });
        content.encode(new FileWriter("fubar.xml"));
    }
}
