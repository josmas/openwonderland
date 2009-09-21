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
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.help.HelpInfo;

/**
 * Defines the format of the layout.xml file. TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wl-help-layout")
public class HelpLayout extends HelpInfo {
    
    /* The XML context for later use */
    private static JAXBContext context;

    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            Collection<Class> clazz = getJAXBClasses();
            context = JAXBContext.newInstance(clazz.toArray(new Class[] {}));
        } catch (javax.xml.bind.JAXBException excp) {
            Logger.getLogger(HelpLayout.class.getName()).log(Level.WARNING,
                    "Unable to get JAXBContext", excp);
        }
    }
    
        /**
     * Returns a collection of classes to initialize the JAXBContext path
     */
    public static Collection<Class> getJAXBClasses() {
        Collection<Class> list = new LinkedList();
        list.add(HelpLayout.class);
        list.add(HelpMenuEntry.class);
        list.add(HelpMenuItem.class);
        list.add(HelpMenuFolder.class);
        list.add(HelpMenuSeparator.class);
        list.add(HelpMenuCategory.class);
        
        return list;
    }
    
    /**
     * Takes the input reader of the XML stream and instantiates an instance of
     * the HelpInfo class
     * <p>
     * @param r The input reader of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleInfo
     * @throw JAXBException Upon error reading the XML file
     */
    public static HelpLayout decode(Reader r) throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (HelpLayout) unmarshaller.unmarshal(r);
    }
    
    /**
     * Writes the HelpInfo class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    @Override
    public void encode(Writer w) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, w);
    }
    
    public static void main(String args[]) throws JAXBException, IOException {
        HelpLayout info = new HelpLayout();
        info.setHelpEntries(new HelpMenuEntry[] {
            new HelpMenuItem("Moving About", "wlh://fubar"),
            new HelpMenuItem("Audio", "fubar"),
            new HelpMenuSeparator(),
            new HelpMenuFolder("My item", new HelpMenuEntry[] {
                new HelpMenuCategory("core-audio")
            })
        });
        info.encode(new FileWriter("fubar.xml"));
    }
}
