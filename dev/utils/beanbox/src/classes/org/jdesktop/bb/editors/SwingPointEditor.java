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

import java.awt.Point;

import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A PropertyEditor for editing a Point object.
 *
 * @version   1.2 02/27/02
 * @author  Mark Davidson
 */
public class SwingPointEditor extends SwingEditorSupport {

    private JTextField xTF;
    private JTextField yTF;

    public SwingPointEditor() {
        xTF = new JTextField();
        xTF.setDocument(new NumberDocument());
        yTF = new JTextField();
        yTF.setDocument(new NumberDocument());
        
        JLabel wlabel = new JLabel(" X: ");
        JLabel hlabel = new JLabel(" Y: ");
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(wlabel);
        panel.add(xTF);
        panel.add(hlabel);
        panel.add(yTF);
    }
    
    public void setValue(Object value)  {
        super.setValue(value);
        
	if (value != null) {
	    Point point = (Point)value;
        
	    xTF.setText(Integer.toString(point.x));
	    yTF.setText(Integer.toString(point.y));
	} else {
	    // null value
	    xTF.setText("");
	    yTF.setText("");
	}
    }
    
    public Object getValue()  {
	try {
	    int x = Integer.parseInt(xTF.getText());
	    int y = Integer.parseInt(yTF.getText());
        
	    return new Point(x, y);
	} catch (NumberFormatException ex) {
	    // Fall out but return null
	}
	return null;
    }
}
