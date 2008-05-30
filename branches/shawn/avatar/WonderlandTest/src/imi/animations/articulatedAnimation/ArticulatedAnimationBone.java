/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation;

import java.util.*;
import com.jme.math.*;

import imi.utils.*;



public class ArticulatedAnimationBone
{
    private ArticulatedAnimationBone m_pParentBone = null;
    
    private int m_Index = 0;
    private int m_Level = 0;
    private String m_Name = "";

    private ArticulatedAnimationGeometry m_pAnimationGeometry = null;

    private MatrixInfo m_MatrixInfo = new MatrixInfo();

    private Matrix4f m_Matrix = new Matrix4f();

    private ArrayList m_ChildBones = new ArrayList();



    //  Constructor.
    public ArticulatedAnimationBone()
    {
    }
    public ArticulatedAnimationBone(String name)
    {
        m_Name = name;
    }



    //  Gets the ParentBone.
    public ArticulatedAnimationBone getParentBone()
    {
        return(m_pParentBone);
    }
    
    //  Sets the ParentBone.
    public void setParentBone(ArticulatedAnimationBone pParentBone)
    {
        m_pParentBone = pParentBone;
    }



    //  Gets the Level.
    public int getLevel()
    {
        return(m_Level);
    }
    //  Sets the Level.
    public void setLevel(int Level)
    {
        m_Level = Level;
    }



    //  Gets the Index.
    public int getIndex()
    {
        return(m_Index);
    }
    
    //  Sets the Index.
    public void setIndex(int Index)
    {
        m_Index = Index;
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



    //  Gets the ArticulatedAnimationGeometry.
    public ArticulatedAnimationGeometry getAnimationGeometry()
    {
        return(m_pAnimationGeometry);
    }

    //  Sets the ArticulatedAnimationGeometry.
    public void setAnimationGeometry(ArticulatedAnimationGeometry pAnimationGeometry)
    {
        m_pAnimationGeometry = pAnimationGeometry;
    }



    //  Gets the MatrixInfo.
    public MatrixInfo getMatrixInfo()
    {
        return(m_MatrixInfo);
    }

    public void setMatrixInfo(MatrixInfo pMatrixInfo)
    {
        m_MatrixInfo = pMatrixInfo;
    }
    
    public void setMatrixInfo(Vector3f pTranslation,
                              Vector3f pRotation,
                              Vector3f pScale)
    {
        if (m_MatrixInfo == null)
            m_MatrixInfo = new MatrixInfo();

        m_MatrixInfo.set(pTranslation, pRotation, pScale);
    }

    
    
    //  Gets the Matrix.
    public Matrix4f getMatrix()
    {
        return(m_Matrix);
    }
    //  Sets the Matrix.
    public void setMatrix(Matrix4f pMatrix)
    {
        m_Matrix.copy(pMatrix);
    }

 
    
    
    
    //  *******************************
    //  ChildBone management methods.
    //  *******************************

    //  Adds an ChildBone.
    public void addChildBone(ArticulatedAnimationBone pChildBone)
    {
        if (pChildBone != null)
        {
            setParentBone(pChildBone);
            m_ChildBones.add(pChildBone);
        }
    }

    public int getChildBoneCount()
    {
        return(m_ChildBones.size());
    }

    public ArticulatedAnimationBone getChildBone(int Index)
    {
        if (Index < 0 || Index >= m_ChildBones.size())
            return(null);

        return ((ArticulatedAnimationBone)m_ChildBones.get(Index));
    }

    public ArticulatedAnimationBone getChildBone(String name)
    {
        int a;
        ArticulatedAnimationBone pChildBone;
        ArticulatedAnimationBone pTheBone = null;

        for (a=0; a<getChildBoneCount(); a++)
        {
            pChildBone = getChildBone(a);

            if (pChildBone.getName().equals(name))
                return(pChildBone);

            pTheBone = pChildBone.getChildBone(name);
            if (pTheBone != null)
                return(pTheBone);
        }

        return(null);
    }

}



