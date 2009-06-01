/*
 * UserListJFrame.java
 *
 * Created on January 22, 2009, 2:52 PM
 */
package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.ChangeUsernameAliasMessage;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ChannelComponent;

import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.common.cell.CellID;

import java.util.Arrays;
import java.util.HashMap;

import java.awt.Point;

import java.util.logging.Logger;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

/**
 *
 * @author  jp
 */
public class UserListJFrame extends javax.swing.JFrame implements PresenceManagerListener,
        VolumeChangeListener, UsernameAliasChangeListener {

    private static final Logger logger =
            Logger.getLogger(UserListJFrame.class.getName());
    private ChannelComponent channelComp;
    private PresenceManager pm;
    private PresenceInfo presenceInfo;

    /** Creates new form UserListJFrame */
    public UserListJFrame(PresenceManager pm, Cell cell) {
        this.pm = pm;

        initComponents();

        channelComp = cell.getComponent(ChannelComponent.class);

        setTitle("Users");

        pm.addPresenceManagerListener(this);

        presenceInfo = pm.getPresenceInfo(cell.getCellID());

        if (presenceInfo == null) {
            logger.warning("No Presence info for cell " + cell.getCellID());
            return;
        }

	volumeButton.setEnabled(false);
	editButton.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        volumeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        propertiesButton = new javax.swing.JButton();

        userList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userList);

        volumeButton.setText("Volume");
        volumeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                volumeButtonActionPerformed(evt);
            }
        });

        editButton.setText("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        propertiesButton.setText("Properties");
        propertiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertiesButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(volumeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(editButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .add(12, 12, 12)
                .add(propertiesButton))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(propertiesButton)
                    .add(volumeButton)
                    .add(editButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private HashMap<PresenceInfo, VolumeControlJFrame> volumeControlMap = new HashMap();
    private HashMap<PresenceInfo, ChangeNameJFrame> changeNameMap = new HashMap();

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
    Object[] selectedValues = userList.getSelectedValues();

    if (selectedValues.length == 1) {
        editButton.setEnabled(false);

        String username = NameTagNode.getUsername((String) selectedValues[0]);

	PresenceInfo[] info = pm.getAliasPresenceInfo(username);

	if (info == null) {
	    System.out.println("No PresenceInfo for " + username);
	    editButton.setEnabled(false);
	    return;
	}

	if (presenceInfo.equals(info[0])) {
	    editButton.setEnabled(true);
	}
    } else {
	editButton.setEnabled(false);
    }

    if (selectedValues.length >= 1) {
	volumeButton.setEnabled(true);
    }

}//GEN-LAST:event_userListValueChanged

private void volumeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_volumeButtonActionPerformed
    Object[] selectedValues = userList.getSelectedValues();

    for (int i = 0; i < selectedValues.length; i++) {
        String username = NameTagNode.getUsername((String) selectedValues[i]);

	PresenceInfo[] info = pm.getAliasPresenceInfo(username);

	if (info == null) {
	    System.out.println("No PresenceInfo for " + username);
	    continue;
	}

	PresenceInfo pi = info[0];

        VolumeControlJFrame volumeControl = volumeControlMap.get(pi);

        if (volumeControl == null) {
            volumeControl = new VolumeControlJFrame(pi.cellID, this, username, pi.callID);
            volumeControl.setLocation(new Point((int) (getLocation().getX() + getWidth()),
                    (int) getLocation().getY()));
            volumeControlMap.put(pi, volumeControl);

            if (presenceInfo.equals(pi)) {
                volumeControl.setTitle("Master Volume for " + username);
            } else {
                volumeControl.setTitle("Private Volume for " + username);
            }
        }

        volumeControl.setVisible(true);
    }

    userList.clearSelection();
}//GEN-LAST:event_volumeButtonActionPerformed

private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
    ChangeNameJFrame changeNameJFrame = changeNameMap.get(presenceInfo);

    if (changeNameJFrame == null) {
	changeNameJFrame = new ChangeNameJFrame(this, presenceInfo);
	changeNameJFrame.setLocation(new Point((int) (getLocation().getX() + getWidth()),
            (int) getLocation().getY()));
	changeNameMap.put(presenceInfo, changeNameJFrame);
    }

    changeNameJFrame.setVisible(true);
}//GEN-LAST:event_editButtonActionPerformed

private NamePropertiesJFrame namePropertiesJFrame;

private void propertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertiesButtonActionPerformed
    if (namePropertiesJFrame == null) {
	namePropertiesJFrame = new NamePropertiesJFrame(presenceInfo);
	namePropertiesJFrame.setLocation(new Point((int) (getLocation().getX() + getWidth()),
            (int) getLocation().getY()));
    }

    namePropertiesJFrame.setVisible(true);
}//GEN-LAST:event_propertiesButtonActionPerformed

    public void changeUsernameAlias(PresenceInfo info) {
        channelComp.send(new ChangeUsernameAliasMessage(info.cellID, info));
    }

    public void volumeChanged(CellID cellID, String otherCallID, double volume) {
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

        channelComp.send(new AudioVolumeMessage(cellID, sc.getCallID(), otherCallID, volume));
    }

    public void done() {
        setVisible(false);
    }
    private String[] userData;

    public void setUserList() {
        PresenceInfo[] presenceInfoList = pm.getAllUsers();

        String[] userData = new String[presenceInfoList.length];

        for (int i = 0; i < presenceInfoList.length; i++) {
            PresenceInfo info = presenceInfoList[i];

	    userData[i] = NameTagNode.getDisplayName(info.usernameAlias, info.isSpeaking,
		info.isMuted);

            if (info.callID == null) {
                // It's a virtual player, skip it.
                continue;
            }

            userData[i] = NameTagNode.getDisplayName(info.usernameAlias, info.isSpeaking,
                    info.isMuted);
        }

	SortUsers.sort(userData);
        setUserList(userData);
    }

    public void setUserList(String[] userData) {
        this.userData = userData;

        userList.setListData(userData);
    }

    public void presenceInfoChanged(PresenceInfo info, ChangeType type) {
        setUserList();
    }

    public void usernameAliasChanged(PresenceInfo info) {
        setUserList();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton propertiesButton;
    private javax.swing.JList userList;
    private javax.swing.JButton volumeButton;
    // End of variables declaration//GEN-END:variables
}
