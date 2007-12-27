/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.serverlistenertest.common;

/**
 *
 * @author jkaplan
 */
public class TestMessageThree extends TestMessageOne {
    private int number;
    
    public TestMessageThree(String text, int number) {
        super (text);
        
        this.number = number;
    }
    
    public int getNumber() {
        return number;
    }
    
     @Override
    public String toString() {
        return "Message " + getMessageID() + ": TestMessageThree : " + 
               getText() + " " + getNumber();
    }
}
