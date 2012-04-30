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
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.wonderland.client.hud.HUDComponent;

/**
 *
 * @author JagWire
 */
public interface UserListView {
    
    public void addEntryToView(String username);
    
    public void removeAllEntries();
   
    public HUDComponent getUserListHUDComponent();            
    
    public void toggleControlPanel();
    
    public int getVolumeSliderValue();
    
    public int getVolumeSliderMaximum();
    
    public void updateWidgetsForNoSelectedValues();
    
    public void updateWidgetsForOneSelectedValue(String username, boolean isMe, Integer v);
    
    public void updateWidgetsForMultipleSelectedValues(int numberOfSelectedEntries);
    
    public Object getSelectedEntry();
    
    public Object[] getSelectedEntries();
    
    public void setTitleOfViewWindow(String title);
    
    public void setSelectedIndex(int index);
            
    public boolean isIndexCurrentlySelected(int index);
    
    public int getNumberOfElements();
    
    public int getIndexForName(String displayName); 
    
    public void addEntryToView(String username, int position);
    
    public void removeEntryAtIndexFromView(int index);
    
    public void changeEntryInView(String source, String target);
    
    public void updateMuteButton(boolean shouldBeMuted);
    
    /** Existing User List Buttons **/
    
    public void addEditButtonActionListener(ActionListener listener);
    
    public void addPropertiesButtonActionListener(ActionListener listener);
    
    public void addListSelectionChangedListener(ListSelectionListener listener);
    
    public void addTextChatButtonActionListener(ActionListener listener);
    
    public void addVoiceChatButtonActionListener(ActionListener listener);
    
    public void addMuteButtonActionListener(ActionListener listener);
    
    public void addPhoneButtonActionListener(ActionListener listener);
    
    public void addGoToUserButtonActionListener(ActionListener listener);
    
    public void addPanelToggleButtonActionListener(ActionListener listener);
    
    public void addVolumeSliderChangeListener(ChangeListener listener);

    public void setPanelVisible(boolean viewPanelVisibility);

    public boolean isPanelVisible();
    
    /** Proposed User List Buttons **/
    
//    public void addPullToMeButtonActionListener(ActionListener listener);
//    
//    public void addSecureStudentClientButtionActionListener(ActionListener listener);
//    
//    public void addAdjustStudentAudioButtonActionListener(ActionListener listener);
//    
//    public void addScreenSnapshotButtonActionListener(ActionListener listener);
    
    
}
