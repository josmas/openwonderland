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
package org.jdesktop.wonderland.modules.evolver.client.evolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A JAXB-annotated class to represent information about an Evolver avatar.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="evolver-avatar-info")
public class EvolverAvatarInfo {

    private static Logger logger = Logger.getLogger(EvolverAvatarInfo.class.getName());

    /** The possible gender types for avatars: MALE or FEMALE */
    public enum GenderType { MALE, FEMALE };

    // The name of the avatar
    @XmlElement(name="name")
    private String avatarName = null;

    // The gender of the avatar: MALE or FEMALE
    @XmlElement(name="gender")
    private GenderType genderType = null;

    // The JAXB context to (un)marshall (from)to XML
    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(EvolverAvatarInfo.class);
        } catch (javax.xml.bind.JAXBException excp) {
            logger.log(Level.WARNING, "Error creating JAXB context", excp);
        }
    }

    /** Default constructor, required by JAXB */
    public EvolverAvatarInfo() {
    }

    @XmlTransient
    public String getAvatarName() {
        // Returns the avatar name. If it contains spaces, replace with under-
        // scores to get around a bug in WebDav
        return avatarName.replaceAll(" ", "_");
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    @XmlTransient
    public GenderType getGenderType() {
        return genderType;
    }

    public void setGenderType(GenderType genderType) {
        this.genderType = genderType;
    }

    /**
     * Returns a new EvolverAvatarInfo class from the XML read from the input
     * stream.
     *
     * @param is The input stream of XML data
     * @return A new EvolverAvatarInfo class
     * @throw IOException Upon I/O error
     * @throw JAXBException Upon parsing error
     */
    public static EvolverAvatarInfo decode(InputStream is)
            throws IOException, JAXBException  {

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (EvolverAvatarInfo) unmarshaller.unmarshal(is);
    }
}
