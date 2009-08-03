/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.runner.wrapper;

import org.jdesktop.wonderland.runner.*;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.runner.Runner;

/**
 *
 * @author jkaplan
 */
@XmlRootElement(name="service")
public class RunnerInfo {
    private String name;
    private String status;
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleRepositorys */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(RunnerInfo.class);
            RunnerInfo.unmarshaller = jc.createUnmarshaller();
            RunnerInfo.marshaller = jc.createMarshaller();
            RunnerInfo.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    public RunnerInfo() {
    }
   
    public RunnerInfo(Runner runner) {
        this.name = runner.getName();
        this.status = runner.getStatus().toString();
    }
    
    @XmlElement
    public String getName() {
        return name;
    }
    
    @XmlElement
    public String getStatus() {
        return status;
    }
     
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the RunnerInfo class
     * <p>
     * @param r The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to RunnerInfo
     * @throw JAXBException Upon error reading the XML file
     */
    public static RunnerInfo decode(Reader r) throws JAXBException {
        RunnerInfo info = (RunnerInfo) RunnerInfo.unmarshaller.unmarshal(r);
        return info;
    }
    
    /**
     * Writes the RunnerInfo class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        RunnerInfo.marshaller.marshal(this, w);
    }

    /**
     * Writes the RunnerInfo class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        RunnerInfo.marshaller.marshal(this, os);
    }
}