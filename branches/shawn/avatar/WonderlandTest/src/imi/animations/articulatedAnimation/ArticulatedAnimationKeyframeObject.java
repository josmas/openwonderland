/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation;

import com.jme.math.*;




public class ArticulatedAnimationKeyframeObject
{
    private String m_BoneName = "";
    private int m_BoneIndex;
    private Matrix4f m_Matrix = new Matrix4f();
    
    

    //  Constructor.
    public ArticulatedAnimationKeyframeObject()
    {
        
    }
    public ArticulatedAnimationKeyframeObject(String boneName)
    {
        m_BoneName = boneName;
    }

    
    
    //  Gets the BoneName.
    public String getBoneName()
    {
        return(m_BoneName);
    }
    
    //  Sets the BoneName.
    public void setBoneName(String boneName)
    {
        m_BoneName = boneName;
    }

    

    //  Gets the BoneIndex.
    public int getBoneIndex()
    {
        return(m_BoneIndex);
    }
    
    //  Sets the BoneIndex.
    public void setBoneIndex(int boneIndex)
    {
        m_BoneIndex = boneIndex;
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

}
