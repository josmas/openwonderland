/*
 *       An actor uses a shared mesh with any animation
 */

package imi.actors;


import com.jme.scene.*;
import com.jme.renderer.*;

import imi.animations.articulatedanimation.*;
import imi.animations.articulatedanimation.instance.*;
import imi.animations.morphanimation.MorphAnimation;
import imi.animations.morphanimation.MorphAnimationInstance;




public class Actor extends Node
{
    protected SharedMesh m_SharedMesh = null;

    protected ArticulatedAnimation m_pArticulatedAnimation = null;
    protected MorphAnimation m_pMorphAnimation = null;

    //  Gets created an attached when the (data) Animation is assigned to the Actor.
    protected ArticulatedAnimationInstance m_pArticulatedAnimationInstance = null;
    protected MorphAnimationInstance m_pMorphAnimationInstance = null;

    
    
    
     /**
     * Empty Constructor to be used internally only.
     */
    public Actor()
    {
        setLastFrustumIntersection(Camera.INSIDE_FRUSTUM);
    }

    /**
     * Constructor creates a new Actor object with the supplied name.
     * 
     * @param name
     *            the name of this Actor
     */
    public Actor(String name)
    {
        super(name);
    }
    public Actor(String name, TriMesh target) 
    {
        super(name);
        
        if (target != null)
        {
            m_SharedMesh = new SharedMesh(name + "Shared", target);
            attachChild(m_SharedMesh);
        }
    }
    
    public SharedMesh setSharedMesh(String name, TriMesh target)
    {
        if (target == null)
            return null;
        
        m_SharedMesh = new SharedMesh(name, target);
        attachChild(m_SharedMesh);
        return m_SharedMesh;
    }
    
    public SharedMesh getSharedMesh()
    {
        return m_SharedMesh;
    }
    
    
    
    //  Gets the ArticulatedAnimation.
    public ArticulatedAnimation getArticulatedAnimation()
    {
        return(m_pArticulatedAnimation);
    }
    public MorphAnimation getMorphAnimation()
    {
        return m_pMorphAnimation;
    }
    
    //  Set the ArticulatedAnimation.
    public void setArticulatedAnimation(ArticulatedAnimation pArticulatedAnimation)
    {
        if (m_pArticulatedAnimation == pArticulatedAnimation)
            return;

        //  If the Actor already has an ArticulatedAnimationInstance, get rid of it.
        if (m_pArticulatedAnimationInstance != null)
        {
            this.detachChild(m_pArticulatedAnimationInstance);
            m_pArticulatedAnimationInstance = null;
        }

        m_pArticulatedAnimation = pArticulatedAnimation;

        //  If an Animation is assigned to the Actor, create an AnimationInstance
        //  and attach it as a child node.  This child AnimationInstance is what
        //  will control the display of an instance of the Animation.
        if (m_pArticulatedAnimation != null)
        {
            m_pArticulatedAnimationInstance = new ArticulatedAnimationInstance(m_pArticulatedAnimation);
            attachChild(m_pArticulatedAnimationInstance);

            //  Make the ArticulatedAnimationInstance use the base pose initially.
            m_pArticulatedAnimationInstance.useBasePose();
            m_pArticulatedAnimationInstance.calculateWorldVectors();
            
//            m_pArticulatedAnimationInstance.dumpWorldVectors();
       
        
//            m_pArticulatedAnimationInstance.setLocalTranslation(-30.0f, 0.0f, 0.0f);
        }
    }
    public void setMorphAnimation(MorphAnimation pMorphAnimation)
    {
        if (m_pMorphAnimation == pMorphAnimation)
            return;

        //  If the Actor already has a MorphAnimationInstance, get rid of it.
        if (m_pMorphAnimationInstance != null)
        {
            this.detachChild(m_pMorphAnimationInstance);
            m_pMorphAnimationInstance = null;
        }

        m_pMorphAnimation = pMorphAnimation;

        //  If an Animation is assigned to the Actor, create an AnimationInstance
        //  and attach it as a child node.  This child AnimationInstance is what
        //  will control the display of an instance of the Animation.
        if (m_pMorphAnimation != null)
        {
            m_pMorphAnimationInstance = new MorphAnimationInstance(m_pMorphAnimation);
            attachChild(m_pMorphAnimationInstance);
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
}



