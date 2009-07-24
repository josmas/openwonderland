/*
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
package org.jdesktop.wonderland.client.hud;

import java.beans.PropertyChangeListener;

/**
 * A generic type of dialog to display on the HUD
 * 
 * @author nsimpson
 */
public interface HUDDialog extends HUDComponent {

    /**
     * Sets the string to be displayed on the text field label
     * @param text the string to display
     */
    public void setLabelText(String text);

    /**
     * Gets the string displayed on the text field label
     * @return the text field label
     */
    public String getLabelText();

    /**
     * Sets the string displayed in the text field
     * @param text the string to display
     */
    public void setValueText(String text);

    /**
     * Gets the string entered by the user in the text field
     * @return the text field string
     */
    public String getValueText();

    /**
     * Adds a bound property listener to the dialog
     * @param listener a listener for dialog events
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a bound property listener from the dialog
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
