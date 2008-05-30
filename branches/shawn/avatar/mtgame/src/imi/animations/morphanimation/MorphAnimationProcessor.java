/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.morphanimation;

import com.jme.app.mtgame.WorldManager;
import com.jme.app.mtgame.entity.ProcessorComponent;

/**
 * This class calls the update of our animation instance
 * 
 * @author Lou Hayt
 */
public class MorphAnimationProcessor extends ProcessorComponent {

    /**
     * The WorldManager - used for adding to update list
     */
    private WorldManager worldManager = null;
  
    /**
     * The animation target
     */
    private MorphAnimationInstance target = null;
    
    /**
     * A name
     */
    private String name = null;
    
    /**
     * The constructor
     */
    public MorphAnimationProcessor(String name, WorldManager worldManager, MorphAnimationInstance target) {
        this.worldManager = worldManager;
        this.target = target;
        this.name = name;
    }
    
    public String toString() {
        return (name);
    }
    
    /**
     * The initialize method
     */
    public void initialize() {
        setArmingConditions(ProcessorComponent.NEW_FRAME_COND);
    }
    
    /**
     * The Calculate method
     */
    public void compute(long conditions) {
        
        
        target.updateAnimation(0.1f);                   // TODO : need deltaTime ... frame elapsed time
    }

    /**
     * The commit method
     */
    public void commit(long conditions) {
        
        // TODO : am I missing something? - everything is done in compute()
        worldManager.addToUpdateList(target);
    }
}
