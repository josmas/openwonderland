/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.morphanimation;

import com.jme.math.Vector2f;
import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class MorphAnimation 
{
    private String m_Name = "";
    
    private ArrayList m_MorphedAnimationLoops = new ArrayList();
    
    // Texture Coordinates
    Vector2f []m_TexCoords = null;
    
    // Indicies
    int []m_Indices = null;
    
    //  Constructor.
    public MorphAnimation()
    {
        
    }
    public MorphAnimation(String name)
    {
        m_Name = name;
    }

    public String getName()
    {
        return m_Name;
    }
    public void setName(String name)
    {
        m_Name = name;
    }
    
    public Vector2f getTexCoord(int index)
    {
        if (index < 0 || index > m_TexCoords.length)
            return null;
        
        return m_TexCoords[index];
    }
    public Vector2f [] getTexCoords()
    {
        return m_TexCoords;
    }
    public void setTexCoords(Vector2f [] coords)
    {
        m_TexCoords = coords;
    }
    
    public int getIndex(int index)
    {
        return m_Indices[index];
    }
    public int [] getIndices()
    {
        return m_Indices;
    }
    public void setIndices(int [] indecies)
    {
        m_Indices = indecies;
    }
    
    //  ************************************
    //    MorphedAnimationLoop management methods.
    //  ************************************

    public void addMorphedAnimationLoop(MorphAnimationLoop pAnimationLoop)
    {
        m_MorphedAnimationLoops.add(pAnimationLoop);
    }
    
    public int getMorphedAnimationLoopCount()
    {
        return(m_MorphedAnimationLoops.size());
    }
    
    public MorphAnimationLoop getMorphedAnimationLoop(int Index)
    {
        if (Index < 0 || Index >= m_MorphedAnimationLoops.size())
            return(null);
        
        return( (MorphAnimationLoop)m_MorphedAnimationLoops.get(Index));
    }
   
    public MorphAnimationLoop getMorphedAnimationLoop(String name)
    {
        int a;
        MorphAnimationLoop pAnimationLoop;
        
        for (a=0; a<getMorphedAnimationLoopCount(); a++)
        {
            pAnimationLoop = getMorphedAnimationLoop(a);
            
            if (pAnimationLoop.getName().equals(name))
                return(pAnimationLoop);
        }
        
        return(null);
    }

    
    
    public void populateWithTestData()
    {
        //  Create array of Texture Coordinates
        m_TexCoords = new Vector2f[3];
        m_TexCoords[0] = new Vector2f(0.5f, 0.0f);
        m_TexCoords[1] = new Vector2f(1.0f, 1.0f);
        m_TexCoords[2] = new Vector2f(0.0f, 1.0f);
        
        //  Create array of Indicies
        m_Indices = new int[3];
        m_Indices[0] = 0;
        m_Indices[1] = 1;
        m_Indices[2] = 2;
    
        
        // Test loop 1
        MorphAnimationLoop pAnimationLoop1 = new MorphAnimationLoop("TestLoop1");
        
        MorphAnimationKeyframe pKeyframe1 = new MorphAnimationKeyframe();
        pKeyframe1.addPosition(0, -5, 1);
        pKeyframe1.addNormal(0, 1, 0);
        pKeyframe1.addPosition(1, -5, -1);
        pKeyframe1.addNormal(0, 1, 0);
        pKeyframe1.addPosition(-1, -5, -1);
        pKeyframe1.addNormal(0, 1, 0);
        
        MorphAnimationKeyframe pKeyframe2 = new MorphAnimationKeyframe();
        pKeyframe2.addPosition(0, -5, 10);
        pKeyframe2.addNormal(0, 1, 0);
        pKeyframe2.addPosition(10, -5, -10);
        pKeyframe2.addNormal(0, 1, 0);
        pKeyframe2.addPosition(-10, -5, -10);
        pKeyframe2.addNormal(0, 1, 0);
        
        pAnimationLoop1.addMorphAnimationKeyframe(pKeyframe1);
        pAnimationLoop1.addMorphAnimationKeyframe(pKeyframe2);

        pAnimationLoop1.setDuration(5.0f);
        this.addMorphedAnimationLoop(pAnimationLoop1);
        
        // Test loop 2
        MorphAnimationLoop pAnimationLoop2 = new MorphAnimationLoop("TestLoop2");
        
        MorphAnimationKeyframe pKeyframe21 = new MorphAnimationKeyframe();
        pKeyframe21.addPosition(0, -5, 1);
        pKeyframe21.addNormal(0, 1, 0);
        pKeyframe21.addPosition(1, -5, -1);
        pKeyframe21.addNormal(0, 1, 0);
        pKeyframe21.addPosition(-1, -5, -1);
        pKeyframe21.addNormal(0, 1, 0);
        
        MorphAnimationKeyframe pKeyframe22 = new MorphAnimationKeyframe();
        pKeyframe22.addPosition(0, 5, 1);
        pKeyframe22.addNormal(0, 1, 0);
        pKeyframe22.addPosition(1, 5, -1);
        pKeyframe22.addNormal(0, 1, 0);
        pKeyframe22.addPosition(-1, 5, -1);
        pKeyframe22.addNormal(0, 1, 0);
        
        pAnimationLoop2.addMorphAnimationKeyframe(pKeyframe21);
        pAnimationLoop2.addMorphAnimationKeyframe(pKeyframe22);

        pAnimationLoop2.setDuration(5.0f);
        this.addMorphedAnimationLoop(pAnimationLoop2);
    }
    
    
}
