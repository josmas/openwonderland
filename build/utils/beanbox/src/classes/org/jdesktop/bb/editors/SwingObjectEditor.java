/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.jdesktop.bb.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Beans;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A property editor which allows for the display and instantaition of
 * arbitrary objects. To instantiate the object, type the package and the
 * class in the text field and press Enter. The Class should be in the 
 * classpath.
 *
 * @version %I% %G%
 * @author  Mark Davidson
 */
public class SwingObjectEditor extends SwingEditorSupport {
    
    private JTextField textfield;
    
    public SwingObjectEditor() {
        textfield = new JTextField();
        
        textfield.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent evt)  {
                handleAction();
            }
        });
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(textfield);
    }
    
    public void setValue(Object value)  {
        super.setValue(value);
        
        if (value != null)  {
            // Truncate the address from the object reference.
            String text = value.toString();
            
            // XXX javax.swing.AccessibleRelationSet.toString() has a bug in which
            // null is returned. Intecept this and other cases so that the tool
            // doens't get hosed.
            if (text == null) text = "";
            
            int index = text.indexOf('@');
            if (index != -1)  {
                text = text.substring(0, index);
            }
            textfield.setText(text);
        } else {
            textfield.setText("");
        }
    }
    
    /** 
     * Callback method which gets handled for actionPerformed.
     */
    private void handleAction()  {
        String beanText = textfield.getText();
        
        try {
            Object obj = Beans.instantiate(this.getClass().getClassLoader(), beanText);
            setValue(obj);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel, "Can't find or load\n" + beanText);
        }
    }
    
}
