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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Base class of all Swing based property editors.
 *
 * @version 1.4 02/27/02
 * @author  Tom Santos
 * @author  Mark Davidson
 */
public class SwingEditorSupport extends PropertyEditorSupport {

    /** 
     * Component which holds the editor. Subclasses are responsible for
     * instantiating this panel.
     */
    protected JPanel panel;
    
    protected static final Dimension LARGE_DIMENSION = new Dimension(150,20);
    protected static final Dimension MEDIUM_DIMENSION = new Dimension(120,20);
    protected static final Dimension SMALL_DIMENSION = new Dimension(50,20);
    protected static final Insets BUTTON_MARGIN = new Insets(0,0,0,0);

    /** 
     * Returns the panel responsible for rendering the PropertyEditor.
     */
    public Component getCustomEditor() {
        return panel;
    }

    public boolean supportsCustomEditor() {
        return true;
    }
    
    // layout stuff
    protected final void setAlignment(JComponent c){
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.setAlignmentY(Component.CENTER_ALIGNMENT);
    }
    
    /** 
     * For property editors that must be initialized with values from
     * the property descriptor.
     */
    public void init(PropertyDescriptor descriptor)  {  
    }
}
