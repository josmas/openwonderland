/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation;

import java.util.*;



public class ArticulatedAnimation
{
    private String m_Name = "";
    
    private ArrayList m_AnimationMaterials = new ArrayList();
    private ArrayList m_AnimationGeometries = new ArrayList();
    private ArrayList m_AnimationBones = new ArrayList();

    private ArrayList m_AnimationLoops = new ArrayList();

    //  Contains all the Root Bones.
    private ArrayList m_RootBones = new ArrayList();


    
    //  Constructor.
    public ArticulatedAnimation()
    {
        
    }
    public ArticulatedAnimation(String name)
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
    
    
    
    
    
    //  *******************************
    //  ArticulatedAnimationMaterial management methods.
    //  *******************************

    //  Adds an ArticulatedAnimationMaterial.
    public void addAnimationMaterial(ArticulatedAnimationMaterial pAnimationMaterial)
    {
        m_AnimationMaterials.add(pAnimationMaterial);
    }
    
    public int getAnimationMaterialCount()
    {
        return(m_AnimationMaterials.size());
    }

    public ArticulatedAnimationMaterial getAnimationMaterial(int Index)
    {
        if (Index < 0 || Index >= m_AnimationMaterials.size())
            return(null);

        return ((ArticulatedAnimationMaterial)m_AnimationMaterials.get(Index));
    }

    public ArticulatedAnimationMaterial getAnimationMaterial(String name)
    {
        int a;
        ArticulatedAnimationMaterial pAnimationMaterial;
        
        for (a=0; a<getAnimationMaterialCount(); a++)
        {
            pAnimationMaterial = getAnimationMaterial(a);
            
            if (pAnimationMaterial.getName().equals(name))
                return(pAnimationMaterial);
        }
        
        return(null);
    }

    
    
    
    
    //  *******************************
    //  ArticulatedAnimationGeometry management methods.
    //  *******************************

    //  Adds an AnimationGeometry.
    public void addAnimationGeometry(ArticulatedAnimationGeometry pAnimationGeometry)
    {
        m_AnimationGeometries.add(pAnimationGeometry);
    }
    
    public int getAnimationGeometryCount()
    {
        return(m_AnimationGeometries.size());
    }

    public ArticulatedAnimationGeometry getAnimationGeometry(int Index)
    {
        if (Index < 0 || Index >= m_AnimationGeometries.size())
            return(null);

        return ((ArticulatedAnimationGeometry)m_AnimationGeometries.get(Index));
    }

    public ArticulatedAnimationGeometry getAnimationGeometry(String name)
    {
        int a;
        ArticulatedAnimationGeometry pAnimationGeometry;
        
        for (a=0; a<getAnimationGeometryCount(); a++)
        {
            pAnimationGeometry = getAnimationGeometry(a);
            
            if (pAnimationGeometry.getName().equals(name))
                return(pAnimationGeometry);
        }
        
        return(null);
    }
    

    
    

    //  *******************************
    //  ArticulatedAnimationBone management methods.
    //  *******************************

    //  Adds an AnimationBone.
    public void addAnimationBone(ArticulatedAnimationBone pAnimationBone)
    {
        pAnimationBone.setIndex(m_AnimationBones.size());

        m_AnimationBones.add(pAnimationBone);
    }
    
    public int getAnimationBoneCount()
    {
        return(m_AnimationBones.size());
    }

    public int getAnimationBoneIndex(String name)
    {
        int a;
        ArticulatedAnimationBone pAnimationBone;
        
        for (a=0; a<getAnimationBoneCount(); a++)
        {
            pAnimationBone = getAnimationBone(a);
            
            if (pAnimationBone.getName().equals(name))
                return(a);
        }
        
        return(-1);
    }

    public ArticulatedAnimationBone getAnimationBone(int Index)
    {
        if (Index < 0 || Index >= m_AnimationBones.size())
            return(null);

        return ((ArticulatedAnimationBone)m_AnimationBones.get(Index));
    }

    public ArticulatedAnimationBone getAnimationBone(String name)
    {
        int a;
        ArticulatedAnimationBone pAnimationBone;
        
        for (a=0; a<getAnimationBoneCount(); a++)
        {
            pAnimationBone = getAnimationBone(a);
            
            if (pAnimationBone.getName().equals(name))
                return(pAnimationBone);
        }
        
        return(null);
    }


    
    
    
    //  *******************************
	//  ArticulatedAnimationLoop management methods.
    //  *******************************

    //  Adds an AnimationLoop.
    public void addAnimationLoop(ArticulatedAnimationLoop pAnimationLoop)
    {
        m_AnimationLoops.add(pAnimationLoop);
    }
    
    public int getAnimationLoopCount()
    {
        return(m_AnimationLoops.size());
    }

    public ArticulatedAnimationLoop getAnimationLoop(int Index)
    {
        if (Index < 0 || Index >= m_AnimationLoops.size())
            return(null);

        return ((ArticulatedAnimationLoop)m_AnimationLoops.get(Index));
    }

    public ArticulatedAnimationLoop getAnimationLoop(String name)
    {
        int a;
        ArticulatedAnimationLoop pAnimationLoop;
        
        for (a=0; a<getAnimationLoopCount(); a++)
        {
            pAnimationLoop = getAnimationLoop(a);
            
            if (pAnimationLoop.getName().equals(name))
                return(pAnimationLoop);
        }
        
        return(null);
    }





    //  *******************************
    //  Root Bone management methods.
    //  *******************************

    //  Adds a RootBone.
    public void addRootBone(ArticulatedAnimationBone pRootBone)
    {
        m_RootBones.add(pRootBone);
    }
    
    public int getRootBoneCount()
    {
        return(m_RootBones.size());
    }

    public int getRootBoneIndex(String name)
    {
        int a;
        ArticulatedAnimationBone pRootBone;
        
        for (a=0; a<getRootBoneCount(); a++)
        {
            pRootBone = getRootBone(a);
            
            if (pRootBone.getName().equals(name))
                return(a);
        }
        
        return(-1);
    }

    public ArticulatedAnimationBone getRootBone(int Index)
    {
        if (Index < 0 || Index >= m_RootBones.size())
            return(null);

        return ((ArticulatedAnimationBone)m_RootBones.get(Index));
    }

    public ArticulatedAnimationBone getRootBone(String name)
    {
        int a;
        ArticulatedAnimationBone pRootBone;
        
        for (a=0; a<getRootBoneCount(); a++)
        {
            pRootBone = getRootBone(a);
            
            if (pRootBone.getName().equals(name))
                return(pRootBone);
        }
        
        return(null);
    }


    


/*
    //  Dumps the Animation.
    public void dump()
    {
        int a;
        ArticulatedAnimationBone pAnimationBone;

        System.out.println("Bones:  " + getAnimationBoneCount());
        for (a=0; a<getAnimationBoneCount(); a++)
        {
            pAnimationBone = getAnimationBone(a);
            
            System.out.println("   Bone:  " + pAnimationBone.getName() + ", " + pAnimationBone.getIndex());
        }
        System.out.println("");
        System.out.println("RootBones:  " + getRootBoneCount());
        for (a=0; a<getRootBoneCount(); a++)
        {
            pAnimationBone = getRootBone(a);
            
            System.out.println("   Bone:  " + pAnimationBone.getName() + ", " + pAnimationBone.getIndex());
        }
    
    }
*/
}
