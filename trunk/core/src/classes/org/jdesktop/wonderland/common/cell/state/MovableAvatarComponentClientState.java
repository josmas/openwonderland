/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.common.cell.state;

/**
 *
 * @author Wish6
 */
public class MovableAvatarComponentClientState extends CellComponentClientState {
    
    private int trigger=-1;
    private String animationName=null;
    
    public MovableAvatarComponentClientState() {
        
    }
    
    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }
    public int getTrigger() {
        return trigger;
    }
    
    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }
    public String getAnimationName() {
        return animationName;
    }
}
