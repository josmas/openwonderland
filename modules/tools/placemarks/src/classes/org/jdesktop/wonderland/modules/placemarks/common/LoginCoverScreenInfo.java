/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.placemarks.common;

import com.jme.renderer.ColorRGBA;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Abhishek Upadhyay
 */
@XmlRootElement(name="login-cover-screen")
public class LoginCoverScreenInfo implements Serializable {
    private ColorRGBA backgroundColor=ColorRGBA.black;
    private ColorRGBA textColor=ColorRGBA.white;
    private String imageURL="";
    private String message="Teleporting. Please Wait...";

    /* The JAXB content to (de)serialize to/from XML */
    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(LoginCoverScreenInfo.class);
        } catch (javax.xml.bind.JAXBException excp) {
            excp.printStackTrace();
        }
    }
    
    public LoginCoverScreenInfo(ColorRGBA backgroundColor, ColorRGBA textColor, String imageURL, String message) {
        this.backgroundColor = backgroundColor;
        this.textColor =textColor;
        this.imageURL = imageURL;
        this.message = message;
    }
    
    public LoginCoverScreenInfo() {
        
    }
    
    public ColorRGBA getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(ColorRGBA backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public ColorRGBA getTextColor() {
        return textColor;
    }

    public void setTextColor(ColorRGBA textColor) {
        this.textColor = textColor;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the LoginCoverScreenInfo class
     * <p>
     * @param r The input reader of the version XML file
     * @throw ClassCastException If the input file does not map to PlacemarkList
     * @throw JAXBException Upon error reading the XML file
     */
    public static LoginCoverScreenInfo decode(Reader r) throws JAXBException {
        // Unmarshall the XML into a PlacemarkList class
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        LoginCoverScreenInfo loginCoverScreenInfo = (LoginCoverScreenInfo)unmarshaller.unmarshal(r);

        return loginCoverScreenInfo;
    }

    /**
     * Writes the LoginCoverScreenInfo class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        
        // Marshall the LoginCoverScreenInfo class into XML
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, w);
    }
    
}
