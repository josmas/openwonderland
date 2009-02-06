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
package org.jdesktop.wonderland.servermanager.client;

import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A collection of PingData objects
 * @author jkaplan
 */
@XmlRootElement(name="pingDataCollection")
public class PingDataCollection {
    private PingData[] data = null;
    private int dataSize;
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all 
       PingDataCollections 
     */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(PingDataCollection.class);
            PingDataCollection.unmarshaller = jc.createUnmarshaller();
            PingDataCollection.marshaller = jc.createMarshaller();
            PingDataCollection.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
        
    /** Default constructor */
    public PingDataCollection() {
    }
    
    /** Constructor, takes the data */
    public PingDataCollection(int dataSize, List<PingData> dataList) {
        this.data = dataList.toArray(new PingData[dataList.size()]);
        this.dataSize = dataSize;
    }

    
    /**
     * Returns the array of module names.
     * 
     * @return An array of module names
     */
    @XmlElements(@XmlElement(name="pingData"))
    public PingData[] getData() {
        return this.data;
    }
    
    /**
     * Return the amount of data
     * @
     */
    @XmlElement(name="dataSize")
    public int getDataSize() {
        return dataSize;
    }
    
    /**
     * Takes the input stream of the XML and instantiates an instance of
     * the PingDataCollection class
     * <p>
     * @param is The input stream of the XML representation
     * @throw ClassCastException If the input file does not map to PingDataCollection
     * @throw JAXBException Upon error reading the XML stream
     */
    public static PingDataCollection decode(InputStream is) throws JAXBException {
        return (PingDataCollection) PingDataCollection.unmarshaller.unmarshal(is);
    }
    
    /**
     * Writes the PingDataCollection class to an output stream.
     * <p>
     * @param w The output write to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        PingDataCollection.marshaller.marshal(this, w);
    }
}
