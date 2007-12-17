/**
 * Project Looking Glass
 *
 * $RCSfile: UserListJFrame.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.29 $
 * $Date: 2007/12/03 16:56:25 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client;

import com.sun.sgs.client.SessionId;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Transform3D;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.vecmath.Point3f;
import org.jdesktop.lg3d.toolkit.NonLGJFrame;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.User;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.AvatarCell;
import org.jdesktop.lg3d.wonderland.darkstar.common.AvatarInfo;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellStatus;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarP2PMessage.SpeakingStatus;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.UserChangedMessage;
import org.jdesktop.lg3d.wonderland.scenemanager.AvatarControlBehavior;
import org.jdesktop.lg3d.wonderland.scenemanager.UserMapPainter;
import org.jdesktop.lg3d.wonderland.scenemanager.avatar.SimpleAvatar;
import org.jdesktop.lg3d.wonderland.appshare.ServerMasterClient;

/**
 *
 * @author  paulby
 */
public class UserListJFrame extends NonLGJFrame {
    
    private static Logger logger = Logger.getLogger("wonderland.main");

    /**
     * The amount by which the user list font size is increased for visible
     * users.
     */
    private static final float LIST_FONT_VISIBLE_GROWTH = 1f;

    /**
     * The amount by which the user list font size is increased for inactive
     * users (expect a negative value).
     */
    private static final float LIST_FONT_INACTIVE_GROWTH = -1f;

    private DefaultListModel userList = new DefaultListModel();
    
    private static UserListJFrame userListJFrame=null;
    
    private UserMapPainter userMapPainter;

    private User selectedUser = null;
    
    /** Creates new form UserListJFrame */
    UserListJFrame() {
        initComponents();
        userJList.setCellRenderer(new UserListCellRenderer());
        userJList.setVisibleRowCount(5);
        
        userMapPainter = new UserMapPainter();
        eastPanel.add(BorderLayout.CENTER, new MapPanel(userMapPainter));
    }
    
    @Override
    public void setVisible(boolean visible) {
	super.setVisible(visible);
	Main.getMain().updateWindowMenuItems();
    }

    private void addUser(final User user) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
//                logger.warning("UserListDebug adding user "+user.getUserName());
                userList.addElement(user);
            }
        });
    }
    
    private void removeUser(final User user) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                userList.removeElement(user);
            }
        });
    }
    
    public void changeUserModel(String userName, byte[] userID, AvatarInfo avatarInfo) {
        SessionId sessionID = SessionId.fromBytes(userID);

	try {
	    SimpleAvatar avatar = (SimpleAvatar)
		ChannelController.getController().getUser(sessionID).getAvatarCell().getAvatar();
	    avatar.setAvatarInfo(avatarInfo);
	} catch (NullPointerException e) {
	    logger.warning("Avatar not yet set!");
	    return;
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}
    }

    public void handleMessage(UserChangedMessage msg) {
        User user;

        logger.fine("UserList msg "+msg.getActionType());
        switch(msg.getActionType()) {
            case USER_ADDED:
                user = ChannelController.getController().getUser(SessionId.fromBytes(msg.getUserID()));
                user.setUserColor(msg.getUserColor());
                
                // TODO - remove server master client checks from this
                // code
                if (ServerMasterClient.isServerMasterClientUser(user)) break;
                
                addUser(user);
                if (null != msg.getSpeakingStatus()) {
                    user.setSpeakingStatus(msg.getSpeakingStatus());
                }
                if (userMapPainter!=null) {
                    userMapPainter.addUser(user);
                }
                break;
            case USER_LEFT:
                user = ChannelController.getController().getUser(SessionId.fromBytes(msg.getUserID()));
                removeUser(user);
                if (userMapPainter!=null) {
                    userMapPainter.removeUser(user);
                }
                break;
	    case USER_MODEL_CHANGED:
		changeUserModel(msg.getUserName(), msg.getUserID(), msg.getAvatarInfo());
		break;

            default :
                throw new RuntimeException("Unhandled Message Type");
        }
    }

    public void speakingStatusChanged(SpeakingStatus newStatus, User user) {
        if (logger.isLoggable(Level.FINEST)) {
	    logger.log(Level.FINEST, "user={0}, newStatus={1}", 
		new Object[]{user, newStatus});
	}
        userListJFrame.repaint(); // TODO: change to update only needed cells
    }    
    /**
     * Per-JVM singleton factory.
     * Guarded by class.
     */
    public static synchronized UserListJFrame getUserListJFrame() {
        if (userListJFrame==null)
            userListJFrame = new UserListJFrame();
        return userListJFrame;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        userJList = new javax.swing.JList();
        eastPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        gotoUserB = new javax.swing.JButton();

        setTitle("Users");

        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(jSeparator1, java.awt.BorderLayout.SOUTH);

        userJList.setModel(userList);
        userJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        userJList.setMaximumSize(new java.awt.Dimension(200, 1024));
        userJList.setMinimumSize(new java.awt.Dimension(80, 80));
        userJList.setPreferredSize(new java.awt.Dimension(80, 200));
        userJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userJListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userJList);

        jSplitPane1.setLeftComponent(jScrollPane1);

        eastPanel.setMinimumSize(new java.awt.Dimension(80, 80));
        eastPanel.setPreferredSize(new java.awt.Dimension(80, 80));
        eastPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                eastPanelMouseWheelMoved(evt);
            }
        });
        eastPanel.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setRightComponent(eastPanel);

        jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        gotoUserB.setText("Goto User");
        gotoUserB.setToolTipText("Goto the selected User");
        gotoUserB.setEnabled(false);
        gotoUserB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoUserBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(242, Short.MAX_VALUE)
                .addComponent(gotoUserB)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gotoUserB)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void eastPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_eastPanelMouseWheelMoved
    float scale = userMapPainter.getScale();
    scale += evt.getWheelRotation();
    if (scale>0)
        userMapPainter.setScale(scale);
}//GEN-LAST:event_eastPanelMouseWheelMoved

private void gotoUserBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoUserBActionPerformed
    Transform3D t3d = selectedUser.getAvatarCell().getAvatar().getAvatarTransform();
    Point3f pos = new Point3f();
    t3d.transform(pos);
    AvatarControlBehavior.getAvatarControlBehavior().setPosition(pos, true);
}//GEN-LAST:event_gotoUserBActionPerformed

private void userJListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userJListValueChanged
    if (evt.getValueIsAdjusting())
        return;

//    System.out.println("User selected "+evt.getFirstIndex());
    if (evt.getFirstIndex()>=0 && evt.getFirstIndex()<userList.getSize()) {
        selectedUser = (User) userList.get(evt.getFirstIndex());
        gotoUserB.setEnabled(true);
    } else {
        selectedUser = null;
        gotoUserB.setEnabled(false);
    }
}//GEN-LAST:event_userJListValueChanged
    
   

    class MapPanel extends JPanel {
        private UserMapPainter userMapPainter;
                
        public MapPanel(UserMapPainter mapPainter) {
            this.userMapPainter = mapPainter;
             
            addComponentListener(new ComponentListener() {

                public void componentResized(ComponentEvent e) {
                    userMapPainter.setSize(getWidth(), getHeight());
                    repaint();
                }

                public void componentMoved(ComponentEvent e) {
                 }

                public void componentShown(ComponentEvent e) {
                    userMapPainter.setSize(getWidth(), getHeight());
                    repaint();
                }

                public void componentHidden(ComponentEvent e) {
                }
            });

            mapPainter.addUpdateListener(new UserMapPainter.UpdateListener() {
                public void mapUpdated() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            repaint();
                            // Update user list user states
                            userListJFrame.repaint();
                        }
                    });
                }
            });
        }
        
        @Override
        public void paint(Graphics g) {
            userMapPainter.paint((Graphics2D)g);
        }
    }

    class UserListCellRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list, 
                                                      Object value, 
                                                      int index, 
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            User user = (User)value;
            AvatarCell avatarCell = user.getAvatarCell();
            // TODO: switch to drawing an animation for speaking instead
            String s = user.getUserNameWithSpeakingStatus();
            
            // Debug code, to be removed
//            if (avatarCell!=null) {
//                SimpleAvatar av = ((SimpleAvatar)avatarCell.getAvatar());
//                if (av!=null) {
//                    int animation = av.getCurrentAnimation();
//                    switch(animation) {
//                    case 0:
//                        s+=" walking";
//                        break;
//                    case 1:
//                        s+=" idle";
//                        break;
//                    default:
//                    }
//                }
//            }
            // End debug code
            
//            logger.warning("Rendering "+s+"  clr "+user.getUserColor());
            setText(s);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(user.getUserColor());
            } else {
                setBackground(list.getBackground());
                setForeground(user.getUserColor());
            }
            setEnabled(list.isEnabled());

            float fontSize = list.getFont().getSize2D();
            int fontStyle = list.getFont().getStyle();

            // Italicize the un-voiced
            if (user.getSpeakingStatus() == SpeakingStatus.CALL_ENDED) {
                fontStyle |= Font.ITALIC;
            }

            // Grow those near, even more those visible
            if (null != avatarCell) {
                CellStatus status = avatarCell.getStatus();
                if (null != status) {
                    switch (status) {
                        case VISIBLE:
                            fontSize += LIST_FONT_VISIBLE_GROWTH;
                            break;

                        case ACTIVE:
                            // (no font size change)
                            break;

                        default:
                            fontSize += LIST_FONT_INACTIVE_GROWTH;
                    }
                }
            }
            setFont(list.getFont().deriveFont(fontStyle, fontSize));

            setOpaque(true);

            return this;
        }

    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel eastPanel;
    private javax.swing.JButton gotoUserB;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList userJList;
    // End of variables declaration//GEN-END:variables
    

}
