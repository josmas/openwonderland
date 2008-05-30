/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.morphanimation;

import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class MorphAnimationLoop 
{
 
    private String m_Name = "";
    private ArrayList m_MorphAnimationKeyframes = new ArrayList();
    private float m_fDuration;
    
    //  Constructor.
    public MorphAnimationLoop()
    {
        
    }
    public MorphAnimationLoop(String name)
    {
        m_Name = name;
    }

    public String getName()
    {
        return(m_Name);
    }
    public void setName(String name)
    {
        m_Name = name;
    }
    
    public void addMorphAnimationKeyframe(MorphAnimationKeyframe pAnimationKeyframe)
    {
        m_MorphAnimationKeyframes.add(pAnimationKeyframe);
    }
    
    public int getMorphAnimationKeyframeCount()
    {
        return(m_MorphAnimationKeyframes.size());
    }
    
    public int getPositionsCount()
    {
        if (m_MorphAnimationKeyframes.size() > 0)
        {
            return ((MorphAnimationKeyframe)(m_MorphAnimationKeyframes.get(0))).getPositionsCount();
        }
        return 0;
    }

    public MorphAnimationKeyframe getMorphAnimationKeyframe(int Index)
    {
        if (Index < 0 || Index >= m_MorphAnimationKeyframes.size())
            return(null);

        return ((MorphAnimationKeyframe)m_MorphAnimationKeyframes.get(Index));
    }
    
    public void setDuration(float fDuration)
    {
        if (fDuration < 0.0f)
            return;
        
        m_fDuration = fDuration;

        if (m_MorphAnimationKeyframes.size() > 0)
        {
            float fTimeStep = m_fDuration / (float)getMorphAnimationKeyframeCount();
            float fTime = 0.0f;
            
            MorphAnimationKeyframe pKeyframe;
            for (int i = 0; i < getMorphAnimationKeyframeCount(); i++)
            {
                pKeyframe = getMorphAnimationKeyframe(i);

                pKeyframe.setTime(fTime);
                pKeyframe.setDuration(fTimeStep);

                fTime += fTimeStep;
            }
        }
    }
    
    public float getDuration()
    {
        return m_fDuration;
    }
    
}
