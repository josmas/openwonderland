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

package org.jdesktop.wonderland.common.cell.state;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;
import sun.misc.Service;

/**
 * The CellServerStateFactory returns marshallers and unmarshallers that can encode
 * and decode XML that is bound to JAXB-annotated classes. This class uses
 * Java's service provider mechanism to fetch the list of fully-qualified class
 * names of Java objects that have JAXB annotations.
 * <p>
 * Classes that provide such a service must have an entry in the JAR file in
 * which they are contained. In META-INF/services, a file named
 * org.jdesktop.wonderland.common.cell.setup.CellServerStateSPI should contain the
 * fully-qualified class name(s) of all classes that implement the CellServerStateSPI
 * interface.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellServerStateFactory {
    /* A list of core cell setup class names, currently only for components */
    private static String[] coreSetup = {
        "org.jdesktop.wonderland.common.cell.setup.CellComponentSetup",
        "org.jdesktop.wonderland.common.AssetURI",
    };
    
    /* The JAXB contexts used to create marshallers and unmarshallers */
    private static JAXBContext jaxbContext = null;
    
    /* The Logger for this class */
    private static Logger logger = Logger.getLogger(CellServerStateFactory.class.getName());
     
    /* Create the XML marshaller and unmarshaller once for all setup classes */
    static {
        try {
            /* Attempt to load the class names using the service providers */
            Iterator<CellServerStateSPI> it = Service.providers(CellServerStateSPI.class);
            Collection<Class> names = CellServerStateFactory.getCoreCellSetup();
            while (it.hasNext() == true) {
                names.add(it.next().getClass());
            }

            jaxbContext = JAXBContext.newInstance(names.toArray(new Class[]{}));
        } catch (javax.xml.bind.JAXBException excp) {
            CellServerStateFactory.logger.log(Level.SEVERE, "[CELL] SETUP FACTORY Failed to create JAXBContext", excp);
        }
    }
    
    /**
     * Returns the object that marshalls JAXB-annotated classes into XML using
     * classes available in the supplied classLoader. If classLoader is null the
     * classloader for this class will be used.
     * 
     * @return A marhsaller for JAXB-annotated classes
     */
    public static Marshaller getMarshaller(ClassLoader classLoader) {
        try {
            if (classLoader == null) {
                Marshaller m = jaxbContext.createMarshaller();
                m.setProperty("jaxb.formatted.output", true);
                return m;
            }
        
            Class[] clazz = getClasses(classLoader);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty("jaxb.formatted.output", true);
            return m;

        } catch (JAXBException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
            
        return null;
        
    }
    
    /**
     * Returns the object that unmarshalls XML into JAXB-annotated classes using
     * classes available in the supplied classLoader. If classLoader is null the
     * classloader for this class will be used.
     * 
     * @return An unmarshaller for XML
     */
    public static Unmarshaller getUnmarshaller(ClassLoader classLoader) {
        try {
            if (classLoader == null) {
                return jaxbContext.createUnmarshaller();
            }

            Class[] clazz = getClasses(classLoader);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            return jc.createUnmarshaller();

        } catch (JAXBException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
            
        return null;
    }
    
    /**
     * Find and return all the classes from the classLoader that implement the CellServerStateSPI
     * inteface
     * 
     * @param classLoader
     * @return
     */
    private static Class[] getClasses(ClassLoader classLoader) {
        Iterator<CellServerStateSPI> it = Service.providers(CellServerStateSPI.class, classLoader);
        Collection<Class> names = CellServerStateFactory.getCoreCellSetup();
        while (it.hasNext() == true) {
            names.add(it.next().getClass());
        }

        return names.toArray(new Class[]{} );
    }
    
    /**
     * Returns a collection of classes that represent the core cell setups.
     */
    private static Collection<Class> getCoreCellSetup() {
        Collection<Class> list = new LinkedList<Class>();
        for (String className : coreSetup) {
            try {
                list.add(Class.forName(className));
            } catch (ClassNotFoundException excp) {
                CellServerStateFactory.logger.log(Level.WARNING, "[CELL] SETUP FACTORY Failed to find class", excp);
            }
        }
        return list;
    }
}
