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

import com.jme.renderer.ColorRGBA;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jdesktop.wonderland.modules.defaultenvironment.common.ColorRGBAAdapter.ColorRGBAHandler;

/**
 *
 * Loosely adapted from Vector3fAdapter.java
 * 
 * @author JagWire
 */

public class ColorRGBAAdapter extends XmlAdapter<ColorRGBAHandler, ColorRGBA> {
    
    
    @Override
    public ColorRGBA unmarshal(ColorRGBAHandler h) throws Exception {
        if(h==null) 
            return null;
        
        return new ColorRGBA(h.red, h.green, h.blue, h.alpha);
    }
    
    @Override
    public ColorRGBAHandler marshal(ColorRGBA color) throws Exception {
        if(color==null)
            return null;
        
        return new ColorRGBAHandler(color);
    }
    
    static public class ColorRGBAHandler {
        @XmlElement
        private float red;
        
        @XmlElement
        private float green;
        
        @XmlElement
        private float blue;
        
        @XmlElement
        private float alpha;
        
        public ColorRGBAHandler() {
            
        }
        
        public ColorRGBAHandler(ColorRGBA color) {
            red = color.r;
            green = color.g;
            blue = color.b;
            alpha = color.a;
        }
    }
    
}
