package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlayTreatmentMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatDialOutMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.common.cell.CellID;

import java.util.Arrays;
import java.util.ArrayList;

import java.util.logging.Logger;

import java.awt.Point;

/*
 * AddMemberDialog.java
 *
 * Created on April 22, 2009, 8:25 AM
 */
/**
 *
 * @author  jp
 */
public class AddMemberDialog extends javax.swing.JFrame implements PresenceManagerListener,
	MemberChangeListener, KeypadListener {

    private static final Logger logger =
            Logger.getLogger(AddMemberDialog.class.getName());
    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo presenceInfo;
    private String group;
    private InCallDialog inCallDialog;

    private PresenceInfo mostRecentDialout;

    /** Creates new form AddMemberDialog */
    public AddMemberDialog() {
        initComponents();
    }

    public AddMemberDialog(AudioManagerClient client, WonderlandSession session,
            CellID cellID, String group, InCallDialog inCallDialog) {

        this.client = client;
        this.session = session;
        this.group = group;
	this.inCallDialog = inCallDialog;

        initComponents();

	setTitle(group);

	client.addMemberChangeListener(group, this);

        pm = PresenceManagerFactory.getPresenceManager(session);

        pm.addPresenceManagerListener(this);

        presenceInfo = pm.getPresenceInfo(cellID);

        if (presenceInfo == null) {
            logger.warning("No Presence info for cell " + cellID);
            return;
        }

	session.send(client, new VoiceChatInfoRequestMessage(group));
        setVisible(true);
    }

    private ArrayList<PresenceInfo> members = new ArrayList();

    public void setMemberList() {
	//System.out.println("---------AddMemberDialog----------");
	//pm.dump();
	//System.out.println("---------AddMemberDialog----------");

        PresenceInfo[] presenceInfoList = pm.getAllUsers();

        ArrayList<String> memberData = new ArrayList();

	//pm.dump();

	for (PresenceInfo info : members) {
	    //System.out.println("AddUser:  Member:  " + info);
	}

        for (int i = 0; i < presenceInfoList.length; i++) {
            PresenceInfo info = presenceInfoList[i];

	    //System.out.println("AddUser:  PI: " + info);

            if (info.callID == null) {
                // It's a virtual player, skip it.
		//System.out.println("AddUser:  skipping virtual player " + info);
                continue;
            }

            if (memberscontains(info)) {
		//System.out.println("members already has " + info);
                continue;
            }

	    members.add(info);

	    memberData.add(info.usernameAlias);
	}

	String[] memberArray = memberData.toArray(new String[0]);

	SortUsers.sort(memberArray);

        memberList.setListData(memberArray);

	enableButtons();
    }

    private void enableButtons() {
        if (memberList.getSelectedValues().length > 0 ||
                (phoneNumberTextField.getText().replaceAll(" ", "").length() > 0 &&
                nameTextField.getText().replaceAll(" ", "").length() > 0)) {

	    joinButton.setEnabled(true);
	} else {
	    joinButton.setEnabled(false);
	}
    }

    public void presenceInfoChanged(PresenceInfo info, ChangeType type) {
	//System.out.println("AddMember presenceInfo changed " + info 
	//    + " type " + type);

        setMemberList();
    }

    public void usernameAliasChanged(PresenceInfo info) {
        setMemberList();
    }

    public void memberChange(PresenceInfo info, boolean added) {
	if (added) {
	    if (mostRecentDialout != null &&
                    mostRecentDialout.userID.getUsername().equals(info.userID.getUsername())) {

                mostRecentDialout = info;
            }

	    if (memberscontains(info)) {
		logger.warning("AddMemberDialog:  already a member " + info);
		return;
	    }
	    members.add(info);
	} else {
	    members.remove(info);
	}

        setMemberList();
    }

    public void setMemberList(PresenceInfo[] members) {
	this.members.clear();

	for (int i = 0; i < members.length; i++) {
	    this.members.add(members[i]);
	}

	setMemberList();
    }

    private boolean memberscontains(PresenceInfo presenceInfo) {
        for (PresenceInfo info : members) {
            //if (info.usernameAlias.equals(presenceInfo.userNameAlias())
            if (info.callID.equals(presenceInfo.callID)) {
                return true;
            }
        }

        return false;
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
        memberList = new javax.swing.JList();
        joinButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        phoneNumberTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        keypadButton = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("Ask User to Join");

        memberList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        memberList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                memberListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(memberList);

        joinButton.setText("Join");
        joinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Phone Number:");

        phoneNumberTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberTextFieldActionPerformed(evt);
            }
        });
        phoneNumberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                phoneNumberTextFieldKeyReleased(evt);
            }
        });

        jLabel3.setText("Name:");

        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });
        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyReleased(evt);
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel2))
                    .add(layout.createSequentialGroup()
                        .add(69, 69, 69)
                        .add(jLabel3)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(phoneNumberTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(85, Short.MAX_VALUE)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(64, 64, 64))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(191, Short.MAX_VALUE)
                .add(keypadButton)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 58, Short.MAX_VALUE)
                .add(joinButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(nameTextField))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(phoneNumberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(keypadButton)
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(joinButton))
                .add(41, 41, 41))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void joinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinButtonActionPerformed
    Object[] selectedValues = memberList.getSelectedValues();

    for (int i = 0; i < selectedValues.length; i++) {
        PresenceInfo[] info = pm.getAliasPresenceInfo((String) selectedValues[i]);

	/*
	 * Caller is already in group and doesn't need to be added again.
	 */
	session.send(client, new VoiceChatJoinMessage(group, presenceInfo, info, null));
    }

    String name = nameTextField.getText();

    if (phoneNumberTextField.getText().length() > 0 && name.length() > 0) {
        mostRecentDialout = new PresenceInfo(null, null, new WonderlandIdentity(name, name, null), null);
        mostRecentDialout.usernameAlias = name;

        session.send(client, new VoiceChatDialOutMessage(group, presenceInfo.callID, 
	    ChatType.PRIVATE, mostRecentDialout, phoneNumberTextField.getText()));

        nameTextField.setText("");
        phoneNumberTextField.setText("");
    }

    memberList.clearSelection();
}//GEN-LAST:event_joinButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    memberList.clearSelection();
    client.removeMemberChangeListener(group, this);
    setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void memberListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_memberListValueChanged
    enableButtons();
}//GEN-LAST:event_memberListValueChanged

private void phoneNumberTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberTextFieldActionPerformed
    enableButtons();

    if (joinButton.isEnabled()) {
	joinButton.doClick();
    }
}//GEN-LAST:event_phoneNumberTextFieldActionPerformed

private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
    enableButtons();

    if (joinButton.isEnabled()) {
	joinButton.doClick();
    }
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

private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
    enableButtons();
}//GEN-LAST:event_nameTextFieldKeyReleased

private void phoneNumberTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phoneNumberTextFieldKeyReleased
    enableButtons();
}//GEN-LAST:event_phoneNumberTextFieldKeyReleased

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    client.removeMemberChangeListener(group, this);
}//GEN-LAST:event_formWindowClosing

public void keypadPressed(char key) {
    System.out.println("Got key " + key);

    if (mostRecentDialout == null) {
        return;
    }

    session.send(client, new PlayTreatmentMessage(mostRecentDialout.callID, "dtmf:" + key));
}

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new AddMemberDialog().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton joinButton;
    private javax.swing.JButton keypadButton;
    private javax.swing.JList memberList;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTextField phoneNumberTextField;
    // End of variables declaration//GEN-END:variables
}
