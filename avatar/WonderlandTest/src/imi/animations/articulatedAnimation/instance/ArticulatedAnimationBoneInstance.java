/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation.instance;

import com.jme.math.*;
import com.jme.scene.*;
import com.jme.renderer.*;

import imi.animations.articulatedanimation.*;





public class ArticulatedAnimationBoneInstance extends Node
{
    //  Pointer to the AnimationBone containing the data.
    private ArticulatedAnimationBone m_pAnimationBone;

    private int m_Index;

    private SharedMesh m_pSharedMesh;

    private Matrix4f m_LocalMatrix = new Matrix4f();
    private Matrix4f m_Matrix = new Matrix4f();





    //  Constructor.
    public ArticulatedAnimationBoneInstance()
    {
    }
    public ArticulatedAnimationBoneInstance(ArticulatedAnimationBone pAnimationBone)
    {
        setAnimationBone(pAnimationBone);
    }

    
    
    //  Gets the AnimationBone.
    public ArticulatedAnimationBone getAnimationBone()
    {
        return(m_pAnimationBone);
    }

    //  Sets the AnimationBone.
    public void setAnimationBone(ArticulatedAnimationBone pAnimationBone)
    {
        if (m_pAnimationBone == pAnimationBone)
            return;

        if (m_pSharedMesh != null)
        {
            detachChild(m_pSharedMesh);
            m_pSharedMesh = null;
        }

        m_pAnimationBone = pAnimationBone;

        if (m_pAnimationBone != null)
        {
            m_Index = m_pAnimationBone.getIndex();
            m_LocalMatrix.set(pAnimationBone.getMatrix());
            //m_LocalMatrix = m_LocalMatrix.transpose();
            m_Matrix.set(m_LocalMatrix);
            
            resolveWorldVectors();
                    
            
            //  Create a new SharedMesh.
            if (m_pAnimationBone.getAnimationGeometry() != null)
            {
                m_pSharedMesh = new SharedMesh();
                m_pSharedMesh.setTarget(m_pAnimationBone.getAnimationGeometry().getTriMesh());
                m_pSharedMesh.setRenderState(m_pAnimationBone.getAnimationGeometry().getMaterialState());

                attachChild(m_pSharedMesh);
            }
/*
            //  Now iterate through all the child bones.
            if (m_pAnimationBone.getChildBoneCount() > 0)
            {
                int a;
                ArticulatedAnimationBone pChildBone;
                ArticulatedAnimationBoneInstance pChildBoneInstance;
                
                for (a=0; a<m_pAnimationBone.getChildBoneCount(); a++)
                {
                    pChildBone = m_pAnimationBone.getChildBone(a);
                    
                    pChildBoneInstance = new ArticulatedAnimationBoneInstance(pChildBone);
                    
                    attachChild(pChildBoneInstance);
                }
            }
*/
        }
    }

    //  Gets the Index.
    public int getIndex()
    {
        return(m_Index);
    }


    public void resolveWorldVectors()
    {
/*
        m_Matrix.toRotationQuat(this.worldRotation);
        m_Matrix.toTranslationVector(this.worldTranslation);

//        this.worldTranslation.set(0.0f, -25.0f, 0.0f);

        if (m_pSharedMesh != null)
        {
            m_pSharedMesh.setLocalRotation(this.worldRotation);
            m_pSharedMesh.setLocalTranslation(this.worldTranslation);
        }
*/

        m_Matrix.toRotationQuat(this.localRotation);
        m_Matrix.toTranslationVector(this.localTranslation);

//        this.localTranslation.set(0.0f, -25.0f, 0.0f);
/*
        if (m_pSharedMesh != null)
        {
            m_pSharedMesh.setLocalRotation(this.localRotation);
            m_pSharedMesh.setLocalTranslation(this.localTranslation);
        }
*/
    }

    //  Gets the LocalMatrix.
    public Matrix4f getLocalMatrix()
    {
        return(m_LocalMatrix);
    }

    //  Gets the Matrix.
    public Matrix4f getMatrix()
    {
        return(m_Matrix);
    }
    

    public void forceBasePose()
    {
        m_Matrix.set(m_pAnimationBone.getMatrix());
        
        worldTranslation.set(m_Matrix.toTranslationVector());
        m_Matrix.toRotationQuat(worldRotation);
        worldRotation.normalize();

        float []Rotation = new float[3];
        worldRotation.toAngles(Rotation);
        
        Rotation[0] *= 180.0f / 3.141592f;
        Rotation[1] *= 180.0f / 3.141592f;
        Rotation[2] *= 180.0f / 3.141592f;

//        System.out.println("ArticulatedAnimationBoneInstance.forceBasePose()  " + getName());
//        System.out.println("   Rotation:  (" + Rotation[0] + ", " + Rotation[1] + ", " + Rotation[2] + ")");
//        System.out.println("   Translation:  (" + worldTranslation.x + ", " + worldTranslation.y + ", " + worldTranslation.z + ")");    
    }

                

    //  Draws the Node.
    public void draw(Renderer r)
    {
        super.draw(r);
    }

    //  Gets the name of the Bone.
    public String getName()
    {
        return( (m_pAnimationBone != null) ? m_pAnimationBone.getName() : "");
    }

/*
    public void forceBasePose()
    {
        Vector3f Rotation = m_pAnimationBone.getMatrixInfo().m_Rotation;
        Vector3f Translation = m_pAnimationBone.getMatrixInfo().m_Translation;
        
        
        Quaternion RotationQuaternion = new Quaternion();
        
        RotationQuaternion.fromAngles(Rotation.x, Rotation.y, Rotation.z);

        this.setLocalTranslation(Translation);
        this.setLocalRotation(RotationQuaternion);

        updateWorldVectors();

        System.out.println("AnimationBoneInstance.forceBasePose()  " + getName());
        System.out.println("   Rotation:  (" + Rotation.x + ", " + Rotation.y + ", " + Rotation.z + ")");
        System.out.println("   Translation:  (" + Translation.x + ", " + Translation.y + ", " + Translation.z + ")");
    }
*/
    

    public void dumpWorldVectors()
    {
        float []Rotation = new float[3];

        worldRotation.toAngles(Rotation);

        System.out.println("AnimationBoneInstance.dumpWorldVectors()  " + getName());
        System.out.println("   Rotation:  (" + Rotation[0] + ", " + Rotation[1] + ", " + Rotation[2] + ")");
        System.out.println("   Translation:  (" + worldTranslation.x + ", " + worldTranslation.y + ", " + worldTranslation.z + ")");

        if (this.getChildren() != null)
        {
            int childNodeCount = this.getChildren().size();
            if (childNodeCount > 0)
            {
                int a;
                Spatial pChildSpatial;
 
                for (a=0; a<childNodeCount; a++)
                {
                    pChildSpatial = this.getChild(a);
                    if (pChildSpatial instanceof ArticulatedAnimationBoneInstance)
                        ((ArticulatedAnimationBoneInstance)pChildSpatial).dumpWorldVectors();
                }
            }
        }
    }


    //  Updates the WorldVectors.
    public void calculateWorldVectors()
    {
//        System.out.println("BoneInstance " + getName() + " calculateWorldVectors()");

        updateWorldVectors();

        if (this.getChildren() != null)
        {
            int childNodeCount = this.getChildren().size();
            if (childNodeCount > 0)
            {
                int a;
                Spatial pChildSpatial;
 
                for (a=0; a<childNodeCount; a++)
                {
                    pChildSpatial = this.getChild(a);
                    if (pChildSpatial instanceof ArticulatedAnimationBoneInstance)
                        ((ArticulatedAnimationBoneInstance)pChildSpatial).calculateWorldVectors();
                }
            }
        }
    }

    
}
