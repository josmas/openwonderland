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
package org.jdesktop.wonderland.modules.userlist.client.presenters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent.EventType;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagComponent;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.userlist.client.WonderlandUserList;
import org.jdesktop.wonderland.modules.userlist.client.views.NamePropertiesPanel.NameTagAttribute;
import org.jdesktop.wonderland.modules.userlist.client.views.NamePropertiesView;

/**
 *
 * @author JagWire
 */
public class NamePropertiesPresenter {
    private NamePropertiesView view;
    private HUDComponent hudComponent;
    private WonderlandUserList model;
    public NamePropertiesPresenter(NamePropertiesView view, HUDComponent c) {
        this.view = view;
        this.hudComponent = c;
        model = WonderlandUserList.INSTANCE;
        addListeners();
    }
    
    private void applyChanges() {
        NameTagAttribute myNameTagAttribute = view.getMyNameTagAttribute();
        NameTagAttribute originalMyNameTagAttribute = view.getMyOriginalNameTagAttribute();
        NameTagAttribute otherNameTagAttributes = view.getOthersNameTagAttribute();
        NameTagAttribute originalOtherNameTagAttributes = view.getOthersOriginalNameTagAttribute();



        if (myNameTagAttribute != originalMyNameTagAttribute) {
//            originalMyNameTagAttribute = myNameTagAttribute;
            view.setMyOriginalNameTagAttribute(myNameTagAttribute);
            switch (myNameTagAttribute) {
                case HIDE:
                    setMyNameTag(AvatarNameEvent.EventType.HIDE);
                    break;

                case SMALL_FONT:
                    setMyNameTag(AvatarNameEvent.EventType.SMALL_FONT);
                    break;

                case REGULAR_FONT:
                    setMyNameTag(AvatarNameEvent.EventType.REGULAR_FONT);
                    break;

                case LARGE_FONT:
                    setMyNameTag(AvatarNameEvent.EventType.LARGE_FONT);
                    break;
            }
        }

        if (otherNameTagAttributes == originalOtherNameTagAttributes) {
            return;
        }

        view.setOthersOriginalNameTagAttributes(otherNameTagAttributes);
        switch (otherNameTagAttributes) {
            case HIDE:
                setOtherNameTags(AvatarNameEvent.EventType.HIDE);
                view.makeOrbsVisible(false);
                break;

            case SMALL_FONT:
                setOtherNameTags(AvatarNameEvent.EventType.SMALL_FONT);
                view.makeOrbsVisible(true);
                break;

            case REGULAR_FONT:
                setOtherNameTags(AvatarNameEvent.EventType.REGULAR_FONT);
                view.makeOrbsVisible(true);
                break;

            case LARGE_FONT:
                setOtherNameTags(AvatarNameEvent.EventType.LARGE_FONT);
                view.makeOrbsVisible(true);
                break;
        }
    }
    
    private void setMyNameTag(EventType eventType) {
        Cell cell = model.getMyCell();
        NameTagComponent ntc = cell.getComponent(NameTagComponent.class);
        PresenceInfo pi = model.getLocalPresenceInfo();
        
        
        ntc.setNameTag(eventType,
                       pi.getUserID().getUsername(),
                       pi.getUsernameAlias());
        
    }
    
    private void setOtherNameTags(EventType eventType) {
        String myUsername = model.getLocalPresenceInfo().getUserID().getUsername();
        
       for(PresenceInfo info: model.getAllUsers()) {
           String username = info.getUserID().getUsername();
           
           if(username.equals(myUsername)) {
               continue;
           }
           
           if(info.getCellID() == null) {
               continue;
           }
           
           Cell cell = model.getCellFromPresenceInfo(info);
           if(cell == null) {
               continue;
           }
           
           NameTagComponent ntc = cell.getComponent(NameTagComponent.class);
           ntc.setNameTag(eventType, username, info.getUsernameAlias());
       }
    }
    
    public void setVisible(boolean visible) {
        hudComponent.setVisible(visible);
    }
    
    public void handleOKButtonPressed() {
        hudComponent.setVisible(false);
    }
    
    public void handleCancelButtonPressed() {
        hudComponent.setVisible(false);
    }
    
    public void handleShowMyNameCheckboxPressed() {
        JCheckBox cb = view.getShowMyNameCheckbox();
        updateMyNameTag(cb.isSelected());
    }
    
    public void handleShowOthersNamesCheckboxPressed() {
        JCheckBox cb = view.getShowOthersNamesCheckbox();
        updateOthersNameTag(cb.isSelected());
    }
    
    public void handleMyFontSizeSpinnerChanged() {
        JCheckBox cb = view.getShowMyNameCheckbox();
        updateMyNameTag(cb.isSelected());
    }
    
    public void handleOthersFontSizeSpinnerChanged() {
        JCheckBox cb = view.getShowOthersNamesCheckbox();
        updateOthersNameTag(cb.isSelected());
    }
    
    private void updateMyNameTag(boolean showingName) {
        view.updateMyNameTag(showingName);
        applyChanges();
    }
    
    private void updateOthersNameTag(boolean showingName) {
        view.updateOthersNameTag(showingName);
        applyChanges();
    }
    
    private void addListeners() {
        view.addOKButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handleOKButtonPressed();
            }
        });
        
        view.addCancelButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handleCancelButtonPressed();
            }
        });
        
        view.addShowMyNameItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent ie) {
                handleShowMyNameCheckboxPressed();
            }
        });
        
        view.addShowOthersNamesItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent ie) {
                handleShowOthersNamesCheckboxPressed();
            }
        });
        
        view.addMyFontSizeChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                handleMyFontSizeSpinnerChanged();
            }
        });
        
        view.addOthersFontSizeChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                handleOthersFontSizeSpinnerChanged();
            }
        });
    }
}
