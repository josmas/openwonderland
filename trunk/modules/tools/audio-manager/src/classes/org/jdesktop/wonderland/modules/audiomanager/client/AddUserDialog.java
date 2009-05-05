package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.modules.audiomanager.client.*;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.common.cell.CellID;

import java.util.Arrays;
import java.util.ArrayList;

import java.util.logging.Logger;

import java.awt.Point;

/*
 * AddUserDialog.java
 *
 * Created on April 22, 2009, 8:25 AM
 */
/**
 *
 * @author  jp
 */
public class AddUserDialog extends javax.swing.JFrame implements PresenceManagerListener,
	MemberChangeListener, KeypadListener {

    private static final Logger logger =
            Logger.getLogger(AddUserDialog.class.getName());
    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo presenceInfo;
    private String group;
    private InCallDialog inCallDialog;

    /** Creates new form AddUserDialog */
    public AddUserDialog() {
        initComponents();
    }

    public AddUserDialog(AudioManagerClient client, WonderlandSession session,
            CellID cellID, String group, InCallDialog inCallDialog) {

        this.client = client;
        this.session = session;
        this.group = group;
	this.inCallDialog = inCallDialog;

        initComponents();

	inCallDialog.addMemberChangeListener(this);

        pm = PresenceManagerFactory.getPresenceManager(session);

        pm.addPresenceManagerListener(this);

        presenceInfo = pm.getPresenceInfo(cellID);

        if (presenceInfo == null) {
            logger.warning("No Presence info for cell " + cellID);
            return;
        }

        setUserList();
        setVisible(true);
    }

    private void setUserList() {
        ArrayList<PresenceInfo> members = inCallDialog.getMembers();

        PresenceInfo[] presenceInfoList = pm.getAllUsers();

        ArrayList<String> userData = new ArrayList();

        for (int i = 0; i < presenceInfoList.length; i++) {
            PresenceInfo info = presenceInfoList[i];

            if (info.callID == null) {
                // It's a virtual player, skip it.
                continue;
            }

            if (members.contains(info)) {
                continue;
            }

	    userData.add(NameTagNode.getDisplayName(info.usernameAlias, info.isSpeaking,
		info.isMuted));
	}

	Arrays.sort(userData.toArray(new String[0]), String.CASE_INSENSITIVE_ORDER);
        userList.setListData(userData.toArray(new String[0]));

	enableButtons();
    }

    private void enableButtons() {
        if (userList.getSelectedValues().length > 0 ||
                (phoneNumberText.getText().replaceAll(" ", "").length() > 0 &&
                nameTextField.getText().replaceAll(" ", "").length() > 0)) {

	    okButton.setEnabled(true);
	} else {
	    okButton.setEnabled(false);
	}
    }

    public void presenceInfoChanged(PresenceInfo info, ChangeType type) {
        setUserList();
    }

    public void aliasChanged(String previousAlias, PresenceInfo info) {
        setUserList();
    }

    public void memberAdded(PresenceInfo info) {
        setUserList();
    }

    public void memberRemoved(PresenceInfo info) {
        setUserList();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        phoneNumberText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        keypadButton = new javax.swing.JButton();

        jLabel1.setText("Ask User to Join");

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

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Phone Number:");

        phoneNumberText.setText("                            ");
        phoneNumberText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberTextActionPerformed(evt);
            }
        });

        jLabel3.setText("Name:");

        nameTextField.setText("                            ");
        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });

        keypadButton.setText("KeyPad");
        keypadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keypadButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(84, 84, 84)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(65, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(keypadButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(69, 69, 69)
                                .add(jLabel3))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel2)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                            .add(phoneNumberText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 58, Short.MAX_VALUE)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(phoneNumberText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(keypadButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    Object[] selectedValues = userList.getSelectedValues();

    for (int i = 0; i < selectedValues.length; i++) {
        PresenceInfo[] info = pm.getAliasPresenceInfo((String) selectedValues[i]);

	/*
	 * Caller is already in group and doesn't need to be added again.
	 */
	session.send(client, new VoiceChatJoinMessage(group, presenceInfo, info, null,
	    phoneNumberText.getText(), nameTextField.getText()));
    }

    userList.clearSelection();
    setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    userList.clearSelection();
    setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
    enableButtons();
}//GEN-LAST:event_userListValueChanged

private void phoneNumberTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberTextActionPerformed
    enableButtons();
}//GEN-LAST:event_phoneNumberTextActionPerformed

private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
    enableButtons();
}//GEN-LAST:event_nameTextFieldActionPerformed

private KeypadDialog keypad;

private void keypadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keypadButtonActionPerformed
    if (keypad == null) {
        keypad = new KeypadDialog(this);
        keypad.setListener(this);
        keypad.setLocation(new Point((int) getLocation().getX() + getWidth(), (int) getLocation().getY()));
    }

    keypad.setVisible(true);
}//GEN-LAST:event_keypadButtonActionPerformed

public void keypadPressed(char key) {
    System.out.println("Got key " + key);
}

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new AddUserDialog().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton keypadButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField phoneNumberText;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables
}
