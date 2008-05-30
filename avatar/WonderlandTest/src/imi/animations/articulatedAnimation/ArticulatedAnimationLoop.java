/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation;

import java.util.*;



public class ArticulatedAnimationLoop
{
    private String m_Name = "";
    private ArrayList m_AnimationKeyframes = new ArrayList();
    private float m_fDuration = 0.0f;

    
    
    //  Constructor.
    public ArticulatedAnimationLoop()
    {
        
    }
	public ArticulatedAnimationLoop(String name)
    {
        m_Name = name;
    }

    
    
    //  Gets the name.
    public String getName()
    {
        return(m_Name);
    }
    
    //  Sets the name.
    public void setName(String name)
    {
        m_Name = name;
    }



    //  Gets the duration.
    public float getDuration()
    {
        return (m_fDuration);
    }

    //  Sets the duration.
    public void setDuration(float fDuration)
    {
        m_fDuration = fDuration;

        if (m_AnimationKeyframes.size() > 0)
        {
            float fTimeStep = m_fDuration / (float)getAnimationKeyframeCount();
            float fTime = 0.0f;

            int a;
            ArticulatedAnimationKeyframe pKeyframe;

            for (a=0; a<getAnimationKeyframeCount(); a++)
            {
                pKeyframe = getAnimationKeyframe(a);

                pKeyframe.setTime(fTime);
                pKeyframe.setDuration(fTimeStep);

                fTime += fTimeStep;
            }
        }
    }



    //  Adds an AnimationKeyframe.
    public void addAnimationKeyframe(ArticulatedAnimationKeyframe pAnimationKeyframe)
    {
        m_AnimationKeyframes.add(pAnimationKeyframe);
    }
    
    public int getAnimationKeyframeCount()
    {
        return(m_AnimationKeyframes.size());
    }

    public ArticulatedAnimationKeyframe getAnimationKeyframe(int Index)
    {
        if (Index < 0 || Index >= m_AnimationKeyframes.size())
            return(null);

        return ((ArticulatedAnimationKeyframe)m_AnimationKeyframes.get(Index));
    }

    public ArticulatedAnimationKeyframe getAnimationKeyframeByTime(int Time)
    {
        int a;
        ArticulatedAnimationKeyframe pAnimationKeyframe;
        
        for (a=0; a<getAnimationKeyframeCount(); a++)
        {
            pAnimationKeyframe = getAnimationKeyframe(a);
            
            if (pAnimationKeyframe.getTime() == Time)
                return(pAnimationKeyframe);
        }
        
        return(null);
    }
}


