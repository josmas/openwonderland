/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.morphanimation;

import com.jme.math.Vector3f;
import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class MorphAnimationKeyframe 
{
    private float       m_Time      =   0.0f;
    private float       m_Duration  =   0.0f;
    
    private ArrayList   m_Positions =   new ArrayList();
    private ArrayList   m_Normals   =   new ArrayList();
      
    //  Constructor.
    public MorphAnimationKeyframe()
    {
        
    }
    
    public float getTime()
    {
        return(m_Time);
    }
    
    public void setTime(float Time)
    {
        m_Time = Time;
    }
    public float getDuration()
    {
        return(m_Duration);
    }
    
    public void setDuration(float Duration)
    {
        m_Duration = Duration;
    }
    
    public void addPosition(Vector3f position)
    {
        m_Positions.add(position);
    }
    
    public void addPosition(float x, float y, float z)
    {
        m_Positions.add(new Vector3f(x, y, z));
    }
    
    public int getPositionsCount()
    {
        return m_Positions.size();
    }
    
    public Vector3f getPosition(int index)
    {
        return (Vector3f)m_Positions.get(index);
    }
    
    public ArrayList getPositions()
    {
        return m_Positions;
    }
    
    public void addNormal(Vector3f Normal)
    {
        m_Normals.add(Normal);
    }
    
    public void addNormal(float x, float y, float z)
    {
        m_Normals.add(new Vector3f(x, y, z));
    }
    
    public int getNormalsCount()
    {
        return m_Normals.size();
    }
    
    public Vector3f getNormal(int index)
    {
        return (Vector3f)m_Normals.get(index);
    }
    
    public ArrayList getNormals()
    {
        return m_Normals;
    }
    
}
