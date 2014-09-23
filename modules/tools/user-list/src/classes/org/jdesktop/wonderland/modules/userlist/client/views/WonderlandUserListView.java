/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

/*
 * Project Wonderland
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.userlist.client.views;

import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.userlist.client.UserListCellRenderer;

/**
 *
 * @author nsimpson
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 * @author JagWire
 */
public class WonderlandUserListView 
        extends javax.swing.JPanel implements UserListView
{

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/userlist/client/resources/Bundle");
    private static final Logger logger =
            Logger.getLogger(WonderlandUserListView.class.getName());

    private DefaultListModel userListModel;
    private int outOfRangeIndex = 0;

    private ImageIcon mutedIcon;
    private ImageIcon unmutedIcon;
    private ImageIcon upIcon;
    private ImageIcon downIcon;
    private HUDComponent userListHUDComponent;
    private HUDComponent addHUDComponent;

    public WonderlandUserListView(UserListCellRenderer cellRenderer) {
        initComponents();

        mutedIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/userlist/client/" +
                "resources/UserListMicMuteOn24x24.png"));
        unmutedIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/userlist/client/" +
                "resources/UserListMicMuteOff24x24.png"));
        upIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/userlist/client/" +
                "resources/upArrow23x10.png"));
        downIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/userlist/client/" +
                "resources/downArrow23x10.png"));

        userListModel = new DefaultListModel();
        userList.setModel(userListModel);
        userList.setCellRenderer(cellRenderer);

        textChatButton.setEnabled(false);
        voiceChatButton.setEnabled(false);
        gotoUserButton.setEnabled(false);

        controlPanel.setVisible(false);
        controlPanel.setEnabled(true);
        editButton.setEnabled(false);
        propertiesButton.setEnabled(true);

    }

    public void setHUDComponent(HUDComponent userListHUDComponent) {
        this.userListHUDComponent = userListHUDComponent;
    }


    
    public void updateMuteButton(boolean shouldBeMuted) {
        if(shouldBeMuted) {
            muteButton.setIcon(mutedIcon);
        } else {
            muteButton.setIcon(unmutedIcon);
        }
    }

    public void done() {
        setVisible(false);
    }
    
    public void setTitleOfViewWindow(String title) {
        if(userListHUDComponent != null) {
            userListHUDComponent.setName(title);
        }
    }
    
    public void setSelectedIndex(int index) {
        userList.setSelectedIndex(index);
    }
    
    public boolean isIndexCurrentlySelected(int index) {
        return userList.isSelectedIndex(index);
    }
    
    public int getIndexForName(String displayName) {
        return userListModel.indexOf(displayName);
    }
    

    public DefaultListModel getListModel() {
        return userListModel;
    }
    
    public int getNumberOfElements() {
        return userListModel.getSize();
    }
    
    public Object[] getSelectedEntries() {
        return userList.getSelectedValues();
    }
    
    public Object getSelectedEntry() {
        return userList.getSelectedValue();
    }
                   
    public void addEntryToView(String username) {
       getListModel().addElement(username);
    }

    public void removeAllEntries() {
//        getListModel().removeAllElements();
        getListModel().clear();
    }
    
    public void addEntryToView(String username, int position) {
        //TODO: Add event handler 
        getListModel().insertElementAt(username, position);
    }

    public void removeEntryAtIndexFromView(int index) {
        //TODO: Add event handler
        userListModel.removeElementAt(index);
        
    }

    public void changeEntryInView(String source, String target) {
        //TODO: Add event handler
    }

    public void addEditButtonActionListener(ActionListener listener) {
        editButton.addActionListener(listener);
    }

    public void addPropertiesButtonActionListener(ActionListener listener) {
        propertiesButton.addActionListener(listener);
    }

    public void addListSelectionChangedListener(ListSelectionListener listener) {
        userList.addListSelectionListener(listener);
    }

    public void addTextChatButtonActionListener(ActionListener listener) {
        textChatButton.addActionListener(listener);
    }

    public void addVoiceChatButtonActionListener(ActionListener listener) {
        voiceChatButton.addActionListener(listener);
    }

    public void addMuteButtonActionListener(ActionListener listener) {
        muteButton.addActionListener(listener);
    }

    public void addPhoneButtonActionListener(ActionListener listener) {
        phoneButton.addActionListener(listener);
    }

    public void addGoToUserButtonActionListener(ActionListener listener) {
        gotoUserButton.addActionListener(listener);
    }

    public void addPanelToggleButtonActionListener(ActionListener listener) {
        panelToggleButton.addActionListener(listener);
    }

    public void addVolumeSliderChangeListener(ChangeListener listener) {
        volumeSlider.addChangeListener(listener);
    }

    public void updateWidgetsForNoSelectedValues() {
            editButton.setEnabled(false);
            volumeLabel.setText(BUNDLE.getString("Private_Volume"));
	    volumeSlider.setEnabled(false);
            controlPanel.setVisible(false);
            textChatButton.setEnabled(false);
            voiceChatButton.setEnabled(false);
            gotoUserButton.setEnabled(false);
            panelToggleButton.setIcon(upIcon);
    }
    
    public void updateWidgetsForOneSelectedValue(String username, boolean isMe, Integer v) {
        // one user (self or someone else)
        controlPanel.setVisible(true);
        volumeSlider.setEnabled(true);
        textChatButton.setEnabled(true);
        panelToggleButton.setIcon(downIcon);

        if (isMe) {
            // this user
            volumeLabel.setText(BUNDLE.getString("Master_Volume"));
            editButton.setEnabled(true);
            textChatButton.setEnabled(false);
            voiceChatButton.setEnabled(false);
            gotoUserButton.setEnabled(false);
        } else {
            // another user
            String text = BUNDLE.getString("Private_Volume_For_Single");
            text = MessageFormat.format(text, username);
            volumeLabel.setText(text);
            editButton.setEnabled(false);
            textChatButton.setEnabled(true);
            voiceChatButton.setEnabled(true);
            gotoUserButton.setEnabled(true);
        }

        int volume;

        if (v == null) {
            volume = (volumeSlider.getMaximum() - volumeSlider.getMinimum()) / 2;
        } else {
            volume = v;
        }

        volumeSlider.setValue(volume);

    }
    
    public void updateWidgetsForMultipleSelectedValues(int numberOfSelectedEntries) {
        String text = BUNDLE.getString("Private_Volume_For_Multiple");
        text = MessageFormat.format(text, numberOfSelectedEntries);
        volumeLabel.setText(text);
        textChatButton.setEnabled(false);
        voiceChatButton.setEnabled(true);
        panelToggleButton.setIcon(downIcon);
    }

    public int getVolumeSliderMaximum() {
        return volumeSlider.getMaximum();
    }
    
    public int getVolumeSliderValue() {
        return volumeSlider.getValue();
    }
    
    public void toggleControlPanel() {
        controlPanel.setVisible(!controlPanel.isVisible());
        
        if(controlPanel.isVisible()) {
            panelToggleButton.setIcon(downIcon);
        } else {
            panelToggleButton.setIcon(upIcon);
        }
    }
    
    public HUDComponent getUserListHUDComponent() {
        return userListHUDComponent;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlPanel = new javax.swing.JPanel();
        volumeLabel = new javax.swing.JLabel();
        volumeSlider = new javax.swing.JSlider();
        jPanel2 = new javax.swing.JPanel();
        muteButton = new javax.swing.JButton();
        textChatButton = new javax.swing.JButton();
        voiceChatButton = new javax.swing.JButton();
        phoneButton = new javax.swing.JButton();
        gotoUserButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        propertiesButton = new javax.swing.JButton();
        userListScrollPane = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        panelToggleButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(194, 310));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/userlist/client/resources/Bundle"); // NOI18N
        volumeLabel.setText(bundle.getString("UserListHUDPanel.volumeLabel.text")); // NOI18N

        volumeSlider.setMinorTickSpacing(10);
        volumeSlider.setPaintTicks(true);

        jPanel2.setLayout(new java.awt.GridLayout());

        muteButton.setBackground(new java.awt.Color(255, 255, 255));
        muteButton.setForeground(new java.awt.Color(255, 255, 255));
        muteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/userlist/client/resources/UserListMicMuteOff24x24.png"))); // NOI18N
        muteButton.setToolTipText(bundle.getString("UserListHUDPanel.muteButton.toolTipText")); // NOI18N
        muteButton.setBorderPainted(false);
        muteButton.setContentAreaFilled(false);
        muteButton.setMaximumSize(new java.awt.Dimension(24, 24));
        muteButton.setMinimumSize(new java.awt.Dimension(24, 24));
        muteButton.setPreferredSize(new java.awt.Dimension(24, 24));
        jPanel2.add(muteButton);

        textChatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/userlist/client/resources/UserListChatText24x24.png"))); // NOI18N
        textChatButton.setToolTipText(bundle.getString("UserListHUDPanel.textChatButton.toolTipText")); // NOI18N
        textChatButton.setBorderPainted(false);
        textChatButton.setContentAreaFilled(false);
        textChatButton.setMaximumSize(new java.awt.Dimension(24, 24));
        textChatButton.setMinimumSize(new java.awt.Dimension(24, 24));
        textChatButton.setPreferredSize(new java.awt.Dimension(24, 24));
        jPanel2.add(textChatButton);

        voiceChatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/userlist/client/resources/UserListChatVoice24x24.png"))); // NOI18N
        voiceChatButton.setToolTipText(bundle.getString("UserListHUDPanel.voiceChatButton.toolTipText")); // NOI18N
        voiceChatButton.setContentAreaFilled(false);
        voiceChatButton.setMaximumSize(new java.awt.Dimension(24, 24));
        voiceChatButton.setMinimumSize(new java.awt.Dimension(24, 24));
        voiceChatButton.setPreferredSize(new java.awt.Dimension(24, 24));
        jPanel2.add(voiceChatButton);

        phoneButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/userlist/client/resources/UserListPhone24x24.png"))); // NOI18N
        phoneButton.setToolTipText(bundle.getString("UserListHUDPanel.phoneButton.toolTipText")); // NOI18N
        phoneButton.setContentAreaFilled(false);
        phoneButton.setMaximumSize(new java.awt.Dimension(24, 24));
        phoneButton.setMinimumSize(new java.awt.Dimension(24, 24));
        phoneButton.setPreferredSize(new java.awt.Dimension(24, 24));
        jPanel2.add(phoneButton);

        gotoUserButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/userlist/client/resources/UserListGoto24x24.png"))); // NOI18N
        gotoUserButton.setToolTipText(bundle.getString("UserListHUDPanel.gotoUserButton.toolTipText")); // NOI18N
        gotoUserButton.setContentAreaFilled(false);
        gotoUserButton.setMaximumSize(new java.awt.Dimension(24, 24));
        gotoUserButton.setMinimumSize(new java.awt.Dimension(24, 24));
        gotoUserButton.setPreferredSize(new java.awt.Dimension(24, 24));
        jPanel2.add(gotoUserButton);

        editButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/userlist/client/resources/UserListEdit24x24.png"))); // NOI18N
        editButton.setToolTipText(bundle.getString("UserListHUDPanel.editButton.toolTipText")); // NOI18N
        editButton.setContentAreaFilled(false);
        editButton.setMaximumSize(new java.awt.Dimension(24, 24));
        editButton.setMinimumSize(new java.awt.Dimension(24, 24));
        editButton.setPreferredSize(new java.awt.Dimension(24, 24));
        jPanel2.add(editButton);

        propertiesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/userlist/client/resources/UserListProperties24x24.png"))); // NOI18N
        propertiesButton.setToolTipText(bundle.getString("UserListHUDPanel.propertiesButton.toolTipText")); // NOI18N
        propertiesButton.setContentAreaFilled(false);
        propertiesButton.setMaximumSize(new java.awt.Dimension(24, 24));
        propertiesButton.setMinimumSize(new java.awt.Dimension(24, 24));
        propertiesButton.setPreferredSize(new java.awt.Dimension(24, 24));
        jPanel2.add(propertiesButton);

        org.jdesktop.layout.GroupLayout controlPanelLayout = new org.jdesktop.layout.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(volumeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controlPanelLayout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(volumeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        userListScrollPane.setPreferredSize(new java.awt.Dimension(260, 300));
        userListScrollPane.setViewportView(userList);

        jPanel1.setMaximumSize(new java.awt.Dimension(32767, 17));
        jPanel1.setMinimumSize(new java.awt.Dimension(100, 17));
        jPanel1.setPreferredSize(new java.awt.Dimension(164, 17));

        panelToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/upArrow23x10.png"))); // NOI18N
        panelToggleButton.setBorder(null);
        panelToggleButton.setMaximumSize(new java.awt.Dimension(63, 14));
        panelToggleButton.setMinimumSize(new java.awt.Dimension(63, 14));
        panelToggleButton.setPreferredSize(new java.awt.Dimension(63, 14));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(panelToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(userListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
            .add(controlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(userListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton editButton;
    private javax.swing.JButton gotoUserButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton muteButton;
    private javax.swing.JButton panelToggleButton;
    private javax.swing.JButton phoneButton;
    private javax.swing.JButton propertiesButton;
    private javax.swing.JButton textChatButton;
    private javax.swing.JList userList;
    private javax.swing.JScrollPane userListScrollPane;
    private javax.swing.JButton voiceChatButton;
    private javax.swing.JLabel volumeLabel;
    private javax.swing.JSlider volumeSlider;
    // End of variables declaration//GEN-END:variables

    public void setPanelVisible(boolean viewPanelVisibility) {
        controlPanel.setVisible(viewPanelVisibility);
        
        if(viewPanelVisibility) {
            panelToggleButton.setIcon(downIcon);
        } else {
            panelToggleButton.setIcon(upIcon);
        }
        
    }

    public boolean isPanelVisible() {
        return controlPanel.isVisible();
    }
}
