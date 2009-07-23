/*
 * AddInWorldHUDPanel.java
 *
 * Created on July 10, 2009, 11:00 AM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

import java.util.ArrayList;

import java.util.logging.Logger;

import javax.swing.DefaultListModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatInfoRequestMessage;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/**
 *
 * @author  jp
 */
public class AddInWorldHUDPanel extends javax.swing.JPanel implements PresenceManagerListener,
	MemberChangeListener, DisconnectListener {

    private static final Logger logger = Logger.getLogger(AddInWorldHUDPanel.class.getName());

    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo myPresenceInfo;

    private DefaultListModel userListModel;

    private InCallHUDPanel inCallHUDPanel;

    private PresenceInfo caller;
    private String group;

    private PropertyChangeSupport listeners;

    private HUDComponent addInWorldHUDComponent;

    /** Creates new form AddInWorldHUDPanel */
    public AddInWorldHUDPanel() {
        initComponents();
    }

    public AddInWorldHUDPanel(AudioManagerClient client, WonderlandSession session,
	    PresenceInfo myPresenceInfo) {

	this(client, session, myPresenceInfo, null);
    }

    public AddInWorldHUDPanel(AudioManagerClient client, WonderlandSession session,
	    PresenceInfo myPresenceInfo, InCallHUDPanel inCallPanel) {

	this.client = client;
	this.session = session;
        this.myPresenceInfo = myPresenceInfo;
	this.inCallHUDPanel = inCallPanel;

        initComponents();

        userListModel = new DefaultListModel();
        userList.setModel(userListModel);

	if (inCallPanel == null) {
            this.inCallHUDPanel = new InCallHUDPanel(client, session, myPresenceInfo, myPresenceInfo);

	    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
	    inCallHUDComponent = mainHUD.createComponent(this.inCallHUDPanel);

	    this.inCallHUDPanel.setHUDComponent(inCallHUDComponent);

	    mainHUD.addComponent(inCallHUDComponent);

	    inCallHUDComponent.addEventListener(new HUDEventListener() {
	        public void HUDObjectChanged(HUDEvent e) {
	            if (e.getEventType().equals(HUDEventType.CLOSED)) {
			inCallHUDPanel = null;
			inCallHUDComponent = null;
	            }
	        }
	    });

            //inCallHUDComponent.setVisible(true);

            //System.out.println("Call x,y " + addInWorldHUDComponent.getX() + ", " + addInWorldHUDComponent.getY()
            //    + " width " + addInWorldHUDComponent.getWidth() + " height " + addInWorldHUDComponent.getHeight()
            //    + " Incall x,y " + (addInWorldHUDComponent.getX() - addInWorldHUDComponent.getWidth())
            //    + ", " + (addInWorldHUDComponent.getY() + addInWorldHUDComponent.getHeight() - inCallHUDComponent.getHeight()));
	}

        caller = this.inCallHUDPanel.getCaller();
	group = this.inCallHUDPanel.getGroup();

	inCallHUDComponent = this.inCallHUDPanel.getHUDComponent();

	groupNameTextField.setText(group);

	client.addMemberChangeListener(group, this);

        pm = PresenceManagerFactory.getPresenceManager(session);
	pm.addPresenceManagerListener(this);

	session.send(client, new VoiceChatInfoRequestMessage(group));

	inviteButton.setEnabled(false);
	setVisible(true);
    }

    public void setHUDComponent(HUDComponent addInWorldHUDComponent) {
	this.addInWorldHUDComponent = addInWorldHUDComponent;
	inCallHUDPanel.setAddInWorldPanel(this, addInWorldHUDComponent);
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

    private void addElement(final String usernameAlias) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		userListModel.removeElement(usernameAlias);
		userListModel.addElement(usernameAlias);
            }
	});
    }

    private void removeElement(final String usernameAlias) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		userListModel.removeElement(usernameAlias);
            }
	});
    }

    private void addToUserList(final PresenceInfo presenceInfo) {
	if (presenceInfo.equals(myPresenceInfo)) {
	    return;
	}

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		removeElement(presenceInfo.usernameAlias);
		addElement(presenceInfo.usernameAlias);
            }
        });
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
	if (type.equals(ChangeType.USER_REMOVED)) {
	    removeElement(presenceInfo.usernameAlias);
	} else if (type.equals(ChangeType.USER_ADDED)) {
	    if (members.contains(presenceInfo)) {
		removeElement(presenceInfo.usernameAlias);
	    } else {
		addToUserList(presenceInfo);
	    }
	} 
    }

    ArrayList<PresenceInfo> members = new ArrayList();

    public void memberChange(PresenceInfo presenceInfo, boolean added) {
	logger.finer("memberChange " + presenceInfo + " added " + added);

	if (added) {
	    if (members.contains(presenceInfo)) {
		return;
	    }

	    members.add(presenceInfo);
	    removeElement(presenceInfo.usernameAlias);
	} else {
	    members.remove(presenceInfo);
	    synchronized (userListModel) {
		addToUserList(presenceInfo);
	    }
	}
    }

    public void setMemberList(PresenceInfo[] memberList) {
	members.clear();

	for (int i = 0; i < memberList.length; i++) {
	    if (members.contains(memberList[i]) == false) {
	        members.add(memberList[i]);
	    }
	}

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		userListModel.clear();
		PresenceInfo[] presenceInfoList = pm.getAllUsers();

	    	for (int i = 0; i < presenceInfoList.length; i++) {
		    PresenceInfo info = presenceInfoList[i];

	            if (members.contains(info)) {
		        removeElement(info.usernameAlias);
		    } else {
		        addElement(info.usernameAlias);
		    }
	        }
	    }
        });
    }

    public void disconnected() {
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	addInWorldHUDComponent.setVisible(false);
            }
        });
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
        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        cancelButton = new javax.swing.JButton();
        inviteButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        groupNameTextField = new javax.swing.JLabel();

        userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userList);

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

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setText("Add in-world user:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .addContainerGap()
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                        .add(layout.createSequentialGroup()
                            .addContainerGap()
                            .add(jLabel1)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(groupNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inviteButton)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, inviteButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(groupNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(inviteButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
    inviteButton.setEnabled(userList.getSelectedValues().length > 0);
}//GEN-LAST:event_userListValueChanged

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    if (listeners == null) {
	addInWorldHUDComponent.setVisible(false);
	return;
    }

    listeners.firePropertyChange("cancel", new String(""), null);
}//GEN-LAST:event_cancelButtonActionPerformed

private HUDComponent inCallHUDComponent;

private boolean locationSet;

private void inviteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inviteButtonActionPerformed
    Object[] selectedValues = userList.getSelectedValues();

    ArrayList<PresenceInfo> usersToInvite = new ArrayList();

    if (selectedValues.length > 0) {
	for (int i = 0; i < selectedValues.length; i++) {
            String username = NameTagNode.getUsername((String) selectedValues[i]);

            PresenceInfo info = pm.getAliasPresenceInfo(username);

            if (info == null) {
                logger.warning("no PresenceInfo for " + username);
                continue;
            }

	    if (info.equals(myPresenceInfo)) {
                /*
                 * I'm the caller and will be added automatically
                 */
                continue;
            }

            usersToInvite.add(info);
        }
    }

    inCallHUDPanel.inviteUsers(usersToInvite) ;

    inCallHUDComponent.setVisible(true);

    if (locationSet == false) {
	locationSet = true;
   
        inCallHUDComponent.setLocation(addInWorldHUDComponent.getX() - addInWorldHUDComponent.getWidth(), 
	    addInWorldHUDComponent.getY() + addInWorldHUDComponent.getHeight() - inCallHUDComponent.getHeight());
    }
}//GEN-LAST:event_inviteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel groupNameTextField;
    private javax.swing.JButton inviteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables

}
