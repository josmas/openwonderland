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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import com.jme.math.Vector3f;
import imi.character.AttachmentParams;
import imi.character.CharacterParams;
import imi.scene.PMatrix;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Avatar attributes for generating Wonderland avatars
 * @author jkaplan
 */
@XmlRootElement
public class WonderlandAvatarAttributes {
    private static final Logger logger =
            Logger.getLogger(WonderlandAvatarAttributes.class.getName());

    private static final String MALE_CONFIGS   = "resources/male-configs.xml";
    private static final String FEMALE_CONFIGS = "resources/female-configs.xml";

    private static JAXBContext jaxbContext;

    public enum ConfigType { GENDER, HAIR, HAIR_COLOR, HEAD, SKIN_COLOR, TORSO,
                             SHIRT_COLOR, JACKET, HANDS, LEGS, PANTS_COLOR, FEET,
                             SHOE_COLOR };

    private URL configURL;
    private final Map<ConfigType, List<ConfigElement>> allElements =
            new EnumMap<ConfigType, List<ConfigElement>>(ConfigType.class);
    private final Map<ConfigType, ConfigElement> elements =
            new EnumMap<ConfigType, ConfigElement>(ConfigType.class);

    static {
        try {
            jaxbContext = JAXBContext.newInstance(WonderlandAvatarAttributes.class,
                                                  ConfigList.class, HeadConfigElement.class);
        } catch (javax.xml.bind.JAXBException excp) {
            logger.log(Level.WARNING, "Error creating JAXB context", excp);
        }
    }

    public WonderlandAvatarAttributes() {
        this (null);
    }

    public WonderlandAvatarAttributes(URL configURL) {
        this.configURL = configURL;
        loadConfig(configURL);
    }

    public static WonderlandAvatarAttributes loadMale() throws IOException {
        URL maleURL = WonderlandAvatarAttributes.class.getResource(MALE_CONFIGS);
        return new WonderlandAvatarAttributes(maleURL);
    }

    public static WonderlandAvatarAttributes loadFemale() throws IOException {
        URL femaleURL = WonderlandAvatarAttributes.class.getResource(FEMALE_CONFIGS);
        return new WonderlandAvatarAttributes(femaleURL);
    }

    @XmlTransient
    public CharacterParams getCharacterAttributes() {
        WonderlandCharacterParams out = new WonderlandCharacterParams();
        for (ConfigElement element : getElements()) {
            element.apply(out);
        }

        return out;
    }

    @XmlElement
    public URL getConfigURL() {
        return configURL;
    }

    public void setConfigURL(URL configURL) {
        this.configURL = configURL;
        loadConfig(configURL);
    }

    @XmlTransient
    public Collection<ConfigElement> getElements() {
        return elements.values();
    }

    public ConfigElement getElement(ConfigType type) {
        return elements.get(type);
    }

    public int getElementIndex(ConfigType type) {
        ConfigElement ce = getElement(type);
        List<ConfigElement> el = getElements(type);
        return el.indexOf(ce);
    }

    public ConfigElement getElement(ConfigType type, int index) {
        List<ConfigElement> el = getElements(type);
        return el.get(index);
    }

    public void setElement(ConfigType type, int index) {
        List<ConfigElement> el = getElements(type);
        ConfigElement ce = el.get(index);
        elements.put(type, ce);
    }

    public void setElement(ConfigType type, ConfigElement element) {
        elements.put(type, element);
    }

    public int getElementCount(ConfigType type) {
        return getElements(type).size();
    }

    protected List<ConfigElement> getElements(ConfigType type) {
        List<ConfigElement> el = allElements.get(type);
        if (el == null) {
            el = Collections.emptyList();
        }

        return el;
    }

    public void randomize() {
        for (ConfigType type : ConfigType.values()) {
            List<ConfigElement> el = getElements(type);
            if (el.size() > 0) {
                int rand = (int) (Math.random() * 100) % el.size();
                setElement(type, rand);
            }
        }
    }

    protected void loadConfig(URL configURL) {
        allElements.clear();

        try {
            ConfigList config = ConfigList.decode(configURL.openStream());
            allElements.put(ConfigType.GENDER, Arrays.asList((ConfigElement[]) config.getGenders()));
            allElements.put(ConfigType.HEAD, Arrays.asList((ConfigElement[]) config.getHeads()));
            allElements.put(ConfigType.HAIR, Arrays.asList((ConfigElement[]) config.getHair()));
            allElements.put(ConfigType.TORSO, Arrays.asList((ConfigElement[]) config.getTorsos()));
            allElements.put(ConfigType.JACKET, Arrays.asList((ConfigElement[]) config.getJackets()));
            allElements.put(ConfigType.HANDS, Arrays.asList((ConfigElement[]) config.getHands()));
            allElements.put(ConfigType.LEGS, Arrays.asList((ConfigElement[]) config.getLegs()));
            allElements.put(ConfigType.FEET, Arrays.asList((ConfigElement[]) config.getFeet()));

            // load the first element of each type
            for (ConfigType type : ConfigType.values()) {
                List<ConfigElement> el = getElements(type);
                if (el.size() > 0) {
                    setElement(type, 0);
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error loading config from " +
                       configURL, ioe);
        }
    }

    public static class WonderlandCharacterParams extends CharacterParams {
        // RED: Does this need to be a subclass?
    }

    public static abstract class ConfigElement {
        private String name;
        private String description;
        private URL previewImage;

        @XmlElement
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @XmlElement
        public URL getPreviewImage() {
            return previewImage;
        }

        public void setPreviewImage(URL previewImage) {
            this.previewImage = previewImage;
        }

        public abstract void apply(WonderlandCharacterParams attrs);

        // compare ConfigElements by name.  Each element should have a unique
        // name.
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ConfigElement other = (ConfigElement) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }

    public static class GenderConfigElement extends ConfigElement {
        private int gender;

        @XmlElement
        public int getGender() {
            return gender;
        }

        public void setGender(int gender) {
            this.gender = gender;
        }

        @Override
        public void apply(WonderlandCharacterParams attrs) {
            attrs.setGender(gender).setUsePhongLightingForHead(true);
        }
    }

    public static abstract class ModelConfigElement extends ConfigElement {
        private String model;

        @XmlElement
        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class HeadConfigElement extends ModelConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            attrs.setHeadAttachment(getModel());
        }
    }

    public static abstract class ShapesConfigElement extends ModelConfigElement {
        private String[] shapes;

        @XmlElement
        public String[] getShapes() {
            return shapes;
        }

        public void setShapes(String[] shapes) {
            this.shapes = shapes;
        }
    }

    public static class HairConfigElement extends ShapesConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            PMatrix orientation = new PMatrix(new Vector3f((float)Math.toRadians(10),0,0),
                                              Vector3f.UNIT_XYZ, Vector3f.ZERO);
            attrs.addLoadInstruction(getModel());
            attrs.addAttachmentInstruction(new AttachmentParams(getModel(), "Head", orientation, "Hair", getModel()));
        }
    }

    public static class HandsConfigElement extends ShapesConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            // add hands
            attrs.addLoadInstruction(getModel());

            for (String shape : getShapes()) {
                attrs.addSkinnedMeshParams(new CharacterParams.SkinnedMeshParams(shape, "Hands", getModel()));
            }
        }
    }

    public static class TorsoConfigElement extends ShapesConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            attrs.addLoadInstruction(getModel());

            for (String shape : getShapes())
                attrs.addSkinnedMeshParams(new CharacterParams.SkinnedMeshParams(shape, "UpperBody", getModel()));
        }
    }

    public static class JacketConfigElement extends ShapesConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            if (getModel() == null) {
                return;
            }

            attrs.addLoadInstruction(getModel());

            for (String shape : getShapes()) {
                attrs.addSkinnedMeshParams(new CharacterParams.SkinnedMeshParams(shape, "UpperBody", getModel()));
            }
        }
    }

    public static class LegsConfigElement extends ShapesConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {

            for (String shape : getShapes()) {
                attrs.addSkinnedMeshParams(new CharacterParams.SkinnedMeshParams(shape, "LowerBody", getModel()));
            }
        }
    }

    public static class FeetConfigElement extends ShapesConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {

            for (String shape : getShapes()) {
                attrs.addSkinnedMeshParams(new CharacterParams.SkinnedMeshParams(shape, "Feet", getModel()));
            }
        }
    }

    public static abstract class ColorConfigElement extends ConfigElement {
        private float r;
        private float g;
        private float b;

        @XmlElement
        public float getR() {
            return r;
        }

        public void setR(float r) {
            this.r = r;
        }

        @XmlElement
        public float getG() {
            return g;
        }

        public void setG(float g) {
            this.g = g;
        }

        @XmlElement
        public float getB() {
            return b;
        }

        public void setB(float b) {
            this.b = b;
        }
    }

    public static class SkinColorConfigElement extends ColorConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            attrs.setSkinTone(getR(), getG(), getB());
        }
    }

    public static class HairColorConfigElement extends ColorConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            attrs.setHairColor(getR(), getG(), getB());
        }
    }

    public static class ShirtColorConfigElement extends ColorConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            attrs.setShirtColor(getR(), getG(), getB(), 0f, 0f, 0f);
        }
    }

    public static class PantsColorConfigElement extends ColorConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            attrs.setPantsColor(getR(), getG(), getB(), 0f, 0f, 0f);
        }
    }

    public static class ShoeColorConfigElement extends ColorConfigElement {
        @Override
        public void apply(WonderlandCharacterParams attrs) {
            attrs.setShirtColor(getR(), getG(), getB(), 0f, 0f, 0f);
        }
    }
    
    @XmlRootElement(name="config-list")
    public static class ConfigList {
        private GenderConfigElement[] genders = new GenderConfigElement[0];
        private HeadConfigElement[]   heads   = new HeadConfigElement[0];
        private HairConfigElement[]   hair    = new HairConfigElement[0];
        private TorsoConfigElement[]  torsos  = new TorsoConfigElement[0];
        private JacketConfigElement[] jackets = new JacketConfigElement[0];
        private HandsConfigElement[]  hands   = new HandsConfigElement[0];
        private LegsConfigElement[]   legs    = new LegsConfigElement[0];
        private FeetConfigElement[]   feet    = new FeetConfigElement[0];

        public GenderConfigElement[] getGenders() {
            return genders;
        }
        
        public void setGenders(GenderConfigElement[] genders) {
            this.genders = genders;
        }

        public HeadConfigElement[] getHeads() {
            return heads;
        }

        public void setHeads(HeadConfigElement[] heads) {
            this.heads = heads;
        }

        public HairConfigElement[] getHair() {
            return hair;
        }

        public void setHair(HairConfigElement[] hair) {
            this.hair = hair;
        }

        public TorsoConfigElement[] getTorsos() {
            return torsos;
        }

        public void setTorsos(TorsoConfigElement[] torsos) {
            this.torsos = torsos;
        }

        public JacketConfigElement[] getJackets() {
            return jackets;
        }

        public void setJackets(JacketConfigElement[] jackets) {
            this.jackets = jackets;
        }

        public HandsConfigElement[] getHands() {
            return hands;
        }

        public void setHands(HandsConfigElement[] hands) {
            this.hands = hands;
        }

        public FeetConfigElement[] getFeet() {
            return feet;
        }

        public void setFeet(FeetConfigElement[] feet) {
            this.feet = feet;
        }

        public LegsConfigElement[] getLegs() {
            return legs;
        }

        public void setLegs(LegsConfigElement[] legs) {
            this.legs = legs;
        }

        public static ConfigList decode(InputStream is) throws IOException {
            try {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                return (ConfigList) unmarshaller.unmarshal(is);
            } catch (JAXBException ex) {
                IOException ioe = new IOException("Unmarshalling error");
                ioe.initCause(ex);
                throw ioe;
            }
        }
    }
}
