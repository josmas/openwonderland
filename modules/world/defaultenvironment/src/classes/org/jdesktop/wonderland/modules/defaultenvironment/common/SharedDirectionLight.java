/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.defaultenvironment.common;

import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.common.utils.jaxb.Vector3fAdapter;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author JagWire
 */
@ServerState
@XmlRootElement(name="shared-direction-light")
public class SharedDirectionLight extends SharedData {
    private static final long serialVersionUID =1L;
    
    @XmlElement(name="ambient")
    @XmlJavaTypeAdapter(ColorRGBAAdapter.class)
    private ColorRGBA ambient = new ColorRGBA();
   
    @XmlElement(name="diffuse")
    @XmlJavaTypeAdapter(ColorRGBAAdapter.class)
    private ColorRGBA diffuse = new ColorRGBA();
    
    @XmlElement(name="diffuse")
    @XmlJavaTypeAdapter(ColorRGBAAdapter.class)
    private ColorRGBA specular = new ColorRGBA();
 
    @XmlElement(name="translation")
    @XmlJavaTypeAdapter(Vector3fAdapter.class)
    private Vector3f translation = new Vector3f();
    
    @XmlElement(name="direction")
    @XmlJavaTypeAdapter(Vector3fAdapter.class)
    private Vector3f direction = new Vector3f();

    
    @XmlElement(name="cast-shadows")
    private boolean castShadows = false;
    
    
    public SharedDirectionLight() {
        
    }
    
    public SharedDirectionLight(ColorRGBA ambient,
                               ColorRGBA diffuse,
                               ColorRGBA specular,
                               Vector3f translation,
                               Vector3f direction,
                               boolean castShadows) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.translation = translation;
        this.direction = direction;
        this.castShadows = castShadows;
    }
    
    @XmlTransient
    public ColorRGBA getAmbient() {
        return ambient;
    }

    public void setAmbient(ColorRGBA ambient) {
        this.ambient = ambient;
    }

    @XmlTransient
    public ColorRGBA getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(ColorRGBA diffuse) {
        this.diffuse = diffuse;
    }
    
    @XmlTransient
    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    @XmlTransient
    public ColorRGBA getSpecular() {
        return specular;
    }

    public void setSpecular(ColorRGBA specular) {
        this.specular = specular;
    }

    @XmlTransient
    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    @XmlTransient
    public boolean isCastShadows() {
        return castShadows;
    }

    public void setCastShadows(boolean castShadows) {
        this.castShadows = castShadows;
    }
    
    public static SharedDirectionLight valueOf(ColorRGBA ambient,
                                                ColorRGBA diffuse,
                                                ColorRGBA specular,
                                                Vector3f translation,
                                                Vector3f direction,
                                                boolean castShadows) {
        return new SharedDirectionLight(ambient, diffuse, specular, translation, direction, castShadows);
    }
    

    public LightNode toLightNode() {
        LightNode node = new LightNode();
        DirectionalLight light = new DirectionalLight();
        light.setAmbient(ambient);
        light.setDiffuse(diffuse);
        light.setSpecular(specular);
        light.setShadowCaster(castShadows);
        light.setDirection(direction);
        
        node.setLight(light);
        node.setLocalTranslation(translation);
        
        return node;
    } 
    
    @Override
    public String toString() {
        return "ambient: "+ambient+"\n"
                + "diffuse: "+diffuse+"\n"
                + "specular: "+specular+"\n"
                + "translation: "+translation+"\n"
                + "direction: " +direction+"\n"
                + "castShadows: " +castShadows;
    }
}
