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
package org.jdesktop.wonderland.modules.userlist.client.views;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.modules.userlist.client.views.NamePropertiesPanel.NameTagAttribute;

/**
 *
 * @author JagWire
 */
public interface NamePropertiesView {
 
    public void addOKButtonActionListener(ActionListener listener);
    
    public void addCancelButtonActionListener(ActionListener listener);
    
    public void addShowMyNameItemListener(ItemListener listener);
    
    public void addShowOthersNamesItemListener(ItemListener listener);
    
    public void addMyFontSizeChangeListener(ChangeListener listener);
    
    public void addOthersFontSizeChangeListener(ChangeListener listener);
    
    public void setVisible(boolean visible);
    
    public JCheckBox getShowMyNameCheckbox();
    
    public JCheckBox getShowOthersNamesCheckbox();
    
    public void updateMyNameTag(boolean showingName);
    
    public void updateOthersNameTag(boolean showingName);
    
    public void makeOrbsVisible(boolean visible);
    
    public NameTagAttribute getMyNameTagAttribute();
    
    public NameTagAttribute getMyOriginalNameTagAttribute();
    
    public NameTagAttribute getOthersNameTagAttribute();
    
    public NameTagAttribute getOthersOriginalNameTagAttribute();
    
    public void setMyOriginalNameTagAttribute(NameTagAttribute nta);
    
    public void setOthersOriginalNameTagAttributes(NameTagAttribute nta);
}
