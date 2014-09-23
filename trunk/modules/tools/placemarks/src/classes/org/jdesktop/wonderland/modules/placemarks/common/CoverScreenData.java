/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.placemarks.common;

import com.jme.renderer.ColorRGBA;

/**
 *
 * @author Abhishek Upadhyay
 */
public class CoverScreenData {
    private ColorRGBA backgroundColor=ColorRGBA.black;
    private ColorRGBA textColor=ColorRGBA.white;
    private String imageURL="";
    private String message="Teleporting. Please Wait...";

    public CoverScreenData(ColorRGBA backgroundColor, ColorRGBA textColor, String imageURL, String message) {
        this.backgroundColor = backgroundColor;
        this.textColor =textColor;
        this.imageURL = imageURL;
        this.message = message;
    }
    
    public CoverScreenData() {
        
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
    
}
