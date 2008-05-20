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

import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * An editor for editing Insets.
 *
 * @version %I% %G%
 * @author  Mark Davidson
 */
public class SwingInsetsEditor extends SwingEditorSupport {
    
    private JTextField topTF;
    private JTextField leftTF;
    private JTextField bottomTF;
    private JTextField rightTF;

    public SwingInsetsEditor() {
        topTF = new JTextField();
        topTF.setDocument(new NumberDocument());
        leftTF = new JTextField();
        leftTF.setDocument(new NumberDocument());
        bottomTF = new JTextField();
        bottomTF.setDocument(new NumberDocument());
        rightTF = new JTextField();
        rightTF.setDocument(new NumberDocument());
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel("Top: "));
        panel.add(topTF);
        panel.add(new JLabel("Left: "));
        panel.add(leftTF);
        panel.add(new JLabel("Bottom: "));
        panel.add(bottomTF);
        panel.add(new JLabel("Right: "));
        panel.add(rightTF);
    }
    
    public void setValue(Object value)  {
        super.setValue(value);
        
        Insets insets = (Insets)value;
        
        topTF.setText(Integer.toString(insets.top));
        leftTF.setText(Integer.toString(insets.left));
        bottomTF.setText(Integer.toString(insets.bottom));
        rightTF.setText(Integer.toString(insets.right));
    }
    
    public Object getValue()  {
        int top = Integer.parseInt(topTF.getText());
        int left = Integer.parseInt(leftTF.getText());
        int bottom = Integer.parseInt(bottomTF.getText());
        int right = Integer.parseInt(rightTF.getText());
        
        return new Insets(top, left, bottom, right);
    }

}