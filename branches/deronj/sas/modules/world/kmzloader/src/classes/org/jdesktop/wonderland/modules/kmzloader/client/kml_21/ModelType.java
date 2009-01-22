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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.09.19 at 09:37:59 AM PDT 
//


package org.jdesktop.wonderland.modules.kmzloader.client.kml_21;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ModelType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModelType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://earth.google.com/kml/2.1}GeometryType">
 *       &lt;sequence>
 *         &lt;element name="altitudeMode" type="{http://earth.google.com/kml/2.1}altitudeModeEnum" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.1}Location" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.1}Orientation" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.1}Scale" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.1}Link" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelType", propOrder = {
    "altitudeMode",
    "location",
    "orientation",
    "scale",
    "link"
})
public class ModelType
    extends GeometryType
{

    @XmlElement(defaultValue = "clampToGround")
    protected AltitudeModeEnum altitudeMode;
    @XmlElement(name = "Location")
    protected LocationType location;
    @XmlElement(name = "Orientation")
    protected OrientationType orientation;
    @XmlElement(name = "Scale")
    protected ScaleType scale;
    @XmlElement(name = "Link")
    protected LinkType link;

    /**
     * Gets the value of the altitudeMode property.
     * 
     * @return
     *     possible object is
     *     {@link AltitudeModeEnum }
     *     
     */
    public AltitudeModeEnum getAltitudeMode() {
        return altitudeMode;
    }

    /**
     * Sets the value of the altitudeMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link AltitudeModeEnum }
     *     
     */
    public void setAltitudeMode(AltitudeModeEnum value) {
        this.altitudeMode = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link LocationType }
     *     
     */
    public LocationType getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType }
     *     
     */
    public void setLocation(LocationType value) {
        this.location = value;
    }

    /**
     * Gets the value of the orientation property.
     * 
     * @return
     *     possible object is
     *     {@link OrientationType }
     *     
     */
    public OrientationType getOrientation() {
        return orientation;
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrientationType }
     *     
     */
    public void setOrientation(OrientationType value) {
        this.orientation = value;
    }

    /**
     * Gets the value of the scale property.
     * 
     * @return
     *     possible object is
     *     {@link ScaleType }
     *     
     */
    public ScaleType getScale() {
        return scale;
    }

    /**
     * Sets the value of the scale property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScaleType }
     *     
     */
    public void setScale(ScaleType value) {
        this.scale = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link LinkType }
     *     
     */
    public LinkType getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkType }
     *     
     */
    public void setLink(LinkType value) {
        this.link = value;
    }

}
