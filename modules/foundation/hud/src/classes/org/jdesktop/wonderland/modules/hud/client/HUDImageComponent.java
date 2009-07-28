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
package org.jdesktop.wonderland.modules.hud.client;

import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * A HUD component that displays an image
 * @author nsimpson
 */
public class HUDImageComponent extends HUDComponent2D {

    private static final Logger logger = Logger.getLogger(HUDImageComponent.class.getName());
    private ImageIcon imageIcon;

    public HUDImageComponent() {
        super();
        setDecoratable(false);
    }

    public HUDImageComponent(ImageIcon imageIcon) {
        this();
        setImage(imageIcon);
    }

    /**
     * Sets the image to be displayed by this component
     * @param imageIcon the image to display
     */
    public void setImage(ImageIcon imageIcon) {
        this.imageIcon = imageIcon;
        if (component == null) {
            component = new JButton(imageIcon);
        } else {
            ((JButton)component).setIcon(imageIcon);
        }
        setBounds(0, 0, component.getWidth(), component.getHeight());
    }

    /**
     * Gets the image to be displayed by this component
     * @return the image
     */
    public ImageIcon getImage() {
        return imageIcon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComponent(JComponent component) {
        super.setComponent(component);
    }
}
