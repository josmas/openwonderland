/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation;

import java.util.*;



public class ArticulatedAnimationKeyframe
{
    private float m_fTime = 0.0f;
    private float m_fDuration = 0.0f;
    private ArrayList m_AnimationKeyframeObjects = new ArrayList();
    
    
    
    //  Constructor.
    public ArticulatedAnimationKeyframe()
    {
        
    }
    public ArticulatedAnimationKeyframe(float fTime)
    {
        m_fTime = fTime;
    }

    
    
    //  Gets the time.
    public float getTime()
    {
        return(m_fTime);
    }
    
    //  Sets the time.
    public void setTime(float fTime)
    {
        m_fTime = fTime;
    }
    
    

    //  Gets the duration.
    public float getDuration()
    {
        return(m_fDuration);
    }
    
    //  Sets the duration.
    public void setDuration(float fDuration)
    {
        m_fDuration = fDuration;
    }



    //  Adds an AnimationKeyframe.
    public void addAnimationKeyframeObject(ArticulatedAnimationKeyframeObject pAnimationKeyframeObject)
    {
        m_AnimationKeyframeObjects.add(pAnimationKeyframeObject);
    }
    
    public int getAnimationKeyframeObjectCount()
    {
        return(m_AnimationKeyframeObjects.size());
    }

    public ArticulatedAnimationKeyframeObject getAnimationKeyframeObject(int Index)
    {
        if (Index < 0 || Index >= m_AnimationKeyframeObjects.size())
            return(null);

        return ((ArticulatedAnimationKeyframeObject)m_AnimationKeyframeObjects.get(Index));
    }

    public ArticulatedAnimationKeyframeObject getAnimationKeyframeByTime(String boneName)
    {
        int a;
        ArticulatedAnimationKeyframeObject pAnimationKeyframeObject;
        
        for (a=0; a<getAnimationKeyframeObjectCount(); a++)
        {
            pAnimationKeyframeObject = getAnimationKeyframeObject(a);
            
            if (pAnimationKeyframeObject.getBoneName() == boneName)
                return(pAnimationKeyframeObject);
        }
        
        return(null);
    }
    
}



