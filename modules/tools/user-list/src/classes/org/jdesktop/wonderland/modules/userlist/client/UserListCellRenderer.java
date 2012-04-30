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
package org.jdesktop.wonderland.modules.userlist.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 *
 * @author JagWire
 */
public class UserListCellRenderer implements ListCellRenderer {

    protected DefaultListCellRenderer defaultRenderer =
            new DefaultListCellRenderer();
    private Font inRangeFont = new Font("SansSerif", Font.PLAIN, 14);
    private Font outOfRangeFont = new Font("SansSerif", Font.PLAIN, 14);

    
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        JLabel renderer =
                (JLabel) defaultRenderer.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
//        if (index < UserListManager.INSTANCE.getLastPositionOfInRangeList()) {
//            renderer.setFont(inRangeFont);
//            renderer.setForeground(Color.BLUE);
//        } else {
//            renderer.setFont(outOfRangeFont);
//            renderer.setForeground(Color.BLACK);
//        }
        
        
        String name = (String)value;
        PresenceInfo info = WonderlandUserList.INSTANCE.getAliasInfo(name);
        if(WonderlandUserList.INSTANCE.isInRange(info)) {
            renderer.setFont(inRangeFont);
            renderer.setForeground(Color.BLUE);
        } else {
            renderer.setFont(outOfRangeFont);
            renderer.setForeground(Color.BLACK);
        }
        return renderer;
    }
}
