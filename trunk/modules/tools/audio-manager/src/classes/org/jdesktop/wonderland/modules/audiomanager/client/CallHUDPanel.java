/*
 * CallHUDPanel.java
 *
 * Created on July 10, 2009, 11:00 AM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

import java.util.ArrayList;

import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent.ComponentEventType;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/**
 *
 * @author  jp
 */
public class CallHUDPanel extends javax.swing.JPanel implements PresenceManagerListener,
	MemberChangeListener, DisconnectListener {

    private static final Logger logger = Logger.getLogger(CallHUDPanel.class.getName());

    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo myPresenceInfo;

    private DefaultListModel userListModel;

    private InCallHUDPanel inCallHUDPanel;

    private String group;

    private PropertyChangeSupport listeners;

    private HUDComponent callHUDComponent;

    /** Creates new form CallHUDPanel */
    public CallHUDPanel() {
        initComponents();
    }

    public CallHUDPanel(AudioManagerClient client, WonderlandSession session,
	    PresenceInfo myPresenceInfo) {

	this(client, session, myPresenceInfo, null);
    }

    public CallHUDPanel(AudioManagerClient client, WonderlandSession session,
	    PresenceInfo myPresenceInfo, InCallHUDPanel inCallHUDPanel) {

	this.client = client;
	this.session = session;
        this.myPresenceInfo = myPresenceInfo;
	this.inCallHUDPanel = inCallHUDPanel;

        initComponents();

        userListModel = new DefaultListModel();
        userList.setModel(userListModel);

  	userList.setEnabled(false);

	if (inCallHUDPanel != null) {
	    group = inCallHUDPanel.getGroup();

	    callJLabel.setText("Call " + group);
	    client.addMemberChangeListener(group, this);
	    secretRadioButton.setEnabled(false);
	    privateRadioButton.setEnabled(false);
	}

        pm = PresenceManagerFactory.getPresenceManager(session);
	pm.addPresenceManagerListener(this);

	setUserList();

	userList.setEnabled(false);

	inviteButton.setEnabled(false);
	setVisible(true);
    }

    public void setHUDComponent(HUDComponent callHUDComponent) {
	this.callHUDComponent = callHUDComponent;
    }

    /**
     * Adds a bound property listener to the dialog
     * @param listener a listener for dialog events
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new PropertyChangeSupport(this);
        }
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes a bound property listener from the dialog
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listeners != null) {
            listeners.removePropertyChangeListener(listener);
        }
    }

    private ArrayList<PresenceInfo> members = new ArrayList();

    private void setUserList() {
	PresenceInfo[] presenceInfoList = pm.getAllUsers();

        ArrayList<String> userList = new ArrayList();

        for (int i = 0; i < presenceInfoList.length; i++) {
            PresenceInfo info = presenceInfoList[i];

            if (info.callID == null) {
                // It's a virtual player, skip it.
                continue;
            }

	    if (info.equals(myPresenceInfo)) {
		continue;
	    }

            if (members.contains(info)) {
                logger.finer("members already has " + info);
                continue;
            }

            userListModel.removeElement(info.usernameAlias);
            userListModel.addElement(info.usernameAlias);
        }
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
	System.out.println("PI CHANGED " + presenceInfo);
        setUserList();
    }

    public void disconnected() {
        setVisible(false);
    }

    public void memberChange(PresenceInfo info, boolean added) {
	logger.finer("memberChange " + info + " added " + added);

	if (added) {
	    if (members.contains(info)) {
		logger.warning("AddMemberDialog:  already a member " + info);
		return;
	    }
	    members.add(info);
	} else {
	    members.remove(info);
	}

        setUserList();
    }

    public void setMemberList(PresenceInfo[] members) {
	this.members.clear();

	for (int i = 0; i < members.length; i++) {
	    this.members.add(members[i]);
	}

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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        callJLabel = new javax.swing.JLabel();
        phoneNumberRadioButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        numberTextField = new javax.swing.JTextField();
        inWorldRadioButton = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        secretRadioButton = new javax.swing.JRadioButton();
        privateRadioButton = new javax.swing.JRadioButton();
        cancelButton = new javax.swing.JButton();
        inviteButton = new javax.swing.JButton();

        callJLabel.setText("Call");

        buttonGroup1.add(phoneNumberRadioButton);
        phoneNumberRadioButton.setSelected(true);
        phoneNumberRadioButton.setText("Phone Number");
        phoneNumberRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberRadioButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Name:");

        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyReleased(evt);
            }
        });

        jLabel3.setText("Number:");

        numberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numberTextFieldKeyReleased(evt);
            }
        });

        buttonGroup1.add(inWorldRadioButton);
        inWorldRadioButton.setText("In-World");
        inWorldRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inWorldRadioButtonActionPerformed(evt);
            }
        });

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

        jLabel4.setText("Privacy:");

        buttonGroup2.add(secretRadioButton);
        secretRadioButton.setText("Secret");

        buttonGroup2.add(privateRadioButton);
        privateRadioButton.setSelected(true);
        privateRadioButton.setText("Private");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        inviteButton.setText("Invite");
        inviteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inviteButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(30, 30, 30)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(secretRadioButton)
                        .add(18, 18, 18)
                        .add(privateRadioButton))
                    .add(phoneNumberRadioButton)
                    .add(inWorldRadioButton))
                .addContainerGap(84, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(callJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(layout.createSequentialGroup()
                            .add(59, 59, 59)
                            .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 37, Short.MAX_VALUE)
                            .add(inviteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(60, 60, 60)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(65, 65, 65)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jLabel3))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(numberTextField)
                                .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)))))
                .addContainerGap(68, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(callJLabel)
                .add(26, 26, 26)
                .add(phoneNumberRadioButton)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(numberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(28, 28, 28)
                .add(inWorldRadioButton)
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(secretRadioButton)
                    .add(privateRadioButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inviteButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
    inviteButton.setEnabled(userList.getSelectedValues().length > 0);
}//GEN-LAST:event_userListValueChanged

private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
    inviteButton.setEnabled(nameTextField.getText().length() > 0 && numberTextField.getText().length() > 0);
}//GEN-LAST:event_nameTextFieldKeyReleased

private void numberTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberTextFieldKeyReleased
    inviteButton.setEnabled(nameTextField.getText().length() > 0 && numberTextField.getText().length() > 0);
}//GEN-LAST:event_numberTextFieldKeyReleased

private void phoneNumberRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberRadioButtonActionPerformed
    nameTextField.setEnabled(true);
    numberTextField.setEnabled(true);
    userList.setEnabled(false);

    inviteButton.setEnabled(nameTextField.getText().length() > 0 && numberTextField.getText().length() > 0);
}//GEN-LAST:event_phoneNumberRadioButtonActionPerformed

private void inWorldRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inWorldRadioButtonActionPerformed
    nameTextField.setEnabled(false);
    numberTextField.setEnabled(false);
    userList.setEnabled(true);
    inviteButton.setEnabled(userList.getSelectedValues().length > 0);
}//GEN-LAST:event_inWorldRadioButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    if (listeners == null) {
	callHUDComponent.setVisible(false);
	return;
    }

    listeners.firePropertyChange("cancel", new String(""), null);
}//GEN-LAST:event_cancelButtonActionPerformed

private HUDComponent inCallHUDComponent;

private void inviteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inviteButtonActionPerformed
    Object[] selectedValues = userList.getSelectedValues();

    ArrayList<PresenceInfo> usersToInvite = new ArrayList();

    if (inWorldRadioButton.isSelected() && selectedValues.length > 0) {
	for (int i = 0; i < selectedValues.length; i++) {
            String username = NameTagNode.getUsername((String) selectedValues[i]);

            PresenceInfo[] info = pm.getAliasPresenceInfo(username);

            if (info == null) {
                logger.warning("no PresenceInfo for " + username);
                continue;
            }

	    if (info[0].equals(myPresenceInfo)) {
                /*
                 * I'm the caller and will be added automatically
                 */
                continue;
            }

            usersToInvite.add(info[0]);
        }
    }

    secretRadioButton.setEnabled(false);
    privateRadioButton.setEnabled(false);

    if (inCallHUDPanel != null) {
	inCallHUDPanel.addUsers(usersToInvite, secretRadioButton.isSelected());
    } else {
        inCallHUDPanel = new InCallHUDPanel(client, session, myPresenceInfo, usersToInvite, 
	    secretRadioButton.isSelected(), this);

	HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
	inCallHUDComponent = mainHUD.createComponent(inCallHUDPanel);

	inCallHUDPanel.setHUDComponent(inCallHUDComponent);

	//inCallHUDComponent.setPreferredLocation(Layout.NORTH);
	mainHUD.addComponent(inCallHUDComponent);
	inCallHUDComponent.addComponentListener(new HUDComponentListener() {
	    public void HUDComponentChanged(HUDComponentEvent e) {
	        if (e.getEventType().equals(ComponentEventType.DISAPPEARED)) {
	        }
	    }
	});

        inCallHUDComponent.setVisible(true);
        inCallHUDComponent.setLocation(callHUDComponent.getX() - callHUDComponent.getWidth(), 
	    callHUDComponent.getY() + callHUDComponent.getHeight() - inCallHUDComponent.getHeight());
    }

    if (phoneNumberRadioButton.isSelected() && nameTextField.getText().length() > 0 && 
	    numberTextField.getText().length() > 0) {

	inCallHUDPanel.callUser(nameTextField.getText(), numberTextField.getText());
    }

    inCallHUDComponent.setVisible(true);

    System.out.println("Call x,y " + callHUDComponent.getX() + ", " + callHUDComponent.getY()
        + " width " + callHUDComponent.getWidth() + " height " + callHUDComponent.getHeight()
        + " Incall x,y " + (callHUDComponent.getX() - callHUDComponent.getWidth())
        + ", " + (callHUDComponent.getY() + callHUDComponent.getHeight() - inCallHUDComponent.getHeight()));
}//GEN-LAST:event_inviteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel callJLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton inWorldRadioButton;
    private javax.swing.JButton inviteButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTextField numberTextField;
    private javax.swing.JRadioButton phoneNumberRadioButton;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables

}
