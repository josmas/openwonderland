/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation.instance;


import com.jme.scene.*;
import com.jme.math.*;

import imi.animations.articulatedanimation.*;



/**
 *
 * @author Chris Nagle
 */
public class ArticulatedAnimationInstance extends Node// implements Controller
{
    ArticulatedAnimationBoneInstance []m_BoneInstances = null;
    
    
    public enum PlayType { PLAY_ONCE, LOOP, OSCILATE }

    private ArticulatedAnimation m_pAnimation = null;

    private int m_CurrentAnimationLoopIndex = -1;
    private int m_CurrentAnimationLoopKeyframeIndex = -1;
    private float m_fCurrentAnimationLoopSpeed = 1.0f;
    private ArticulatedAnimationLoop m_pCurrentAnimationLoop = null;

    private int m_TransitionToAnimationLoopIndex = -1;
    private int m_TransitionToAnimationLoopKeyframeIndex = -1;
    private float m_fTransitionToAnimationLoopSpeed = 1.0f;
    private ArticulatedAnimationLoop m_pTransitionToAnimationLoop = null;

    private float m_fTransitionTime = 0.0f;
    private float m_fTransitionDuration = 0.0f;

    private float m_fFrameTime = 0.0f;

    private PlayType m_PlayType;
    private int m_Direction;
    
    private boolean m_bPlaying;




    //  Constructor.
    public ArticulatedAnimationInstance()
    {
    }
    public ArticulatedAnimationInstance(ArticulatedAnimation pAnimation)
    {
        setAnimation(pAnimation);
    }
    
/*
    @Overide
    public void update(float time)
    {
        
        
    }
*/

    //  Sets the Animation.
    public void setAnimation(ArticulatedAnimation pAnimation)
    {
        if (m_pAnimation == pAnimation)
            return;

//        System.out.println("ArticulatedAnimationInstance.setAnimation()");
//        System.out.println("ArticulatedAnimationInstance.setAnimation()");
//        System.out.println("ArticulatedAnimationInstance.setAnimation()");

        //  Delete all child nodes.
        detachAllChildren();
                
        m_pAnimation = pAnimation;

        m_BoneInstances = null;

        if (m_pAnimation != null)
        {
            //  Dump the Animation.
//            m_pAnimation.dump();
            
            //  Now iterate through all the child bones.
            if (m_pAnimation.getAnimationBoneCount() > 0)
            {
                //  Create array that will hold BoneInstances.
                m_BoneInstances = new ArticulatedAnimationBoneInstance[m_pAnimation.getAnimationBoneCount()];


//                System.out.println("****  Building BoneInstance hiearchy.");

                //  Loop through all the RootBones and build the hiearchy of Bones.
                int a;
                ArticulatedAnimationBone pRootBone;
                ArticulatedAnimationBoneInstance pRootBoneInstance;

                int Count = m_pAnimation.getRootBoneCount();
                        
                for (a=0; a<m_pAnimation.getRootBoneCount(); a++)
                {
                    pRootBone = m_pAnimation.getRootBone(a);

                    //  Create the RootBoneInstance.
                    pRootBoneInstance = new ArticulatedAnimationBoneInstance(pRootBone);

//                    System.out.println("RootBoneInstance:  " + pRootBoneInstance.getName());

                    m_BoneInstances[pRootBoneInstance.getIndex()] = pRootBoneInstance;

                    attachChild(pRootBoneInstance);

                    buildBoneInstanceHiearchy("", pRootBoneInstance, pRootBone);
                }

//                System.out.println("****  Building BoneInstance hiearchy.");
//                System.out.flush();

                this.updateModelBound(); 
            }


//            dump();


            setAnimationLoop(0);
            update(0.1f);

//            System.out.println("ArticulatedAnimationInstance.setAnimation()");
//            System.out.println("ArticulatedAnimationInstance.setAnimation()");        
//            System.out.println("ArticulatedAnimationInstance.setAnimation()");
        }
    }
    
    void buildBoneInstanceHiearchy(String spacing,
                                   ArticulatedAnimationBoneInstance pAnimationBoneInstance,
                                   ArticulatedAnimationBone pAnimationBone)
    {
        int a;
        ArticulatedAnimationBone pChildBone;
        ArticulatedAnimationBoneInstance pChildBoneInstance;
        
//        System.out.println(spacing + "BoneInstance:  " + pAnimationBoneInstance.getName() + " ChildCount:  " + pAnimationBone.getChildBoneCount());

        for (a=0; a<pAnimationBone.getChildBoneCount(); a++)
        {
            pChildBone = pAnimationBone.getChildBone(a);

            //  Create the child BoneInstance.
            pChildBoneInstance = new ArticulatedAnimationBoneInstance(pChildBone);

            m_BoneInstances[pChildBoneInstance.getIndex()] = pChildBoneInstance;

            pAnimationBoneInstance.attachChild(pChildBoneInstance);

            buildBoneInstanceHiearchy(spacing + "   ", pChildBoneInstance, pChildBone);
        }
    }


    //  Sets up the Animation to use the base pose.
    public void useBasePose()
    {
        useBasePose(this);
    }

    public void useBasePose(Node node)
    {
        if (node instanceof ArticulatedAnimationBoneInstance)
        {
            ((ArticulatedAnimationBoneInstance)node).forceBasePose();
        }
        
        if (node.getChildren() != null)
        {
            int childNodeCount = node.getChildren().size();
            if (childNodeCount > 0)
            {
                int a;
                Spatial pChildSpatial;
 
                for (a=0; a<childNodeCount; a++)
                {
                    pChildSpatial = node.getChild(a);
                    if (pChildSpatial instanceof Node)
                        useBasePose((Node)pChildSpatial);
                }
            }
        }
    }

    
    public void calculateWorldVectors()
    {
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
    

    public void dump()
    {
        System.out.println("???  DUMP  ???");

        int childNodeCount = this.getChildren().size();
        if (childNodeCount > 0)
        {
            int a;
            Spatial pChildSpatial;
 
            for (a=0; a<childNodeCount; a++)
            {
                pChildSpatial = this.getChild(a);
                if (pChildSpatial instanceof ArticulatedAnimationBoneInstance)
                    dumpBoneInstance("", (ArticulatedAnimationBoneInstance)pChildSpatial);
            }
        }

        System.out.println("???  DUMP  ???");
        System.out.flush();
    }

    public void dumpBoneInstance(String spacing, ArticulatedAnimationBoneInstance pBoneInstance)
    {
        if (pBoneInstance == null)
            return;

        System.out.println(spacing + "BoneInstance:  " + pBoneInstance.getName() + ", " + pBoneInstance.getIndex());
        System.out.flush();
        
        if (pBoneInstance.getChildren() != null)
        {
            int childNodeCount = pBoneInstance.getChildren().size();
            if (childNodeCount > 0)
            {
                int a;
                Spatial pChildSpatial;
 
                for (a=0; a<childNodeCount; a++)
                {
                    pChildSpatial = pBoneInstance.getChild(a);
                    if (pChildSpatial instanceof ArticulatedAnimationBoneInstance)
                    dumpBoneInstance(spacing + "   ", (ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }
    }


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

    
    
    public void setAnimationLoop(int Index)
    {
        ArticulatedAnimationLoop pAnimationLoop = m_pAnimation.getAnimationLoop(Index);
        if (pAnimationLoop == null)
            return;

        m_CurrentAnimationLoopIndex = Index;
        m_CurrentAnimationLoopKeyframeIndex = 0;
        m_fCurrentAnimationLoopSpeed = 1.0f;
        m_pCurrentAnimationLoop = pAnimationLoop;

        m_TransitionToAnimationLoopIndex = -1;
        m_TransitionToAnimationLoopKeyframeIndex = -1;
        m_fTransitionToAnimationLoopSpeed = 1.0f;
        m_pTransitionToAnimationLoop = null;

        m_fTransitionTime = 0.0f;
        m_fTransitionDuration = 0.0f;
        
        m_fFrameTime = 0.0f;

        m_PlayType = PlayType.LOOP;
        m_Direction = 1;

        calculateMatrices();
//        dumpMatrices();
        
        play();
    }

    
    public void play()
    {
        m_bPlaying = true;
    }

    public void stop()
    {
        m_bPlaying = false;
    }
    
    public boolean isPlaying()
    {
        return(m_bPlaying);
    }
    
    public void play(int AnimationLoopIndex)
    {
        
    }
    
    public void transitionTo(int NextAnimationLoopIndex, float fTransitionDuration)
    {
        
    }



    /**
     * updateGeometricState overrides Spatials updateGeometric state to update
     * the skin mesh based on any changes the bones may have undergone. The
     * update is defined by the updateTime, only when that much time has passed
     * will the updateSkin method be called.
     *
     * @param time
     *            the time that has passed between calls.
     * @param initiator
     *            true if this is the top level being called.
     */
    public void updateGeometricState(float time, boolean initiator)
    {
        update(time);

        super.updateGeometricState(time, initiator);
    }


    //  Updates the AnimationInstance.
    public void update(float fDeltaTime)
    {
        //  Sanity check.
        if (m_pCurrentAnimationLoop == null)
            return;

        //  Do nothing if we're not playing the animation.
        if (!m_bPlaying)
            return;


//        System.out.println("AnimationInstance.update()");
//        System.out.println("   CurrentAnimationLoopIndex:  " + m_CurrentAnimationLoopIndex);
//        System.out.println("   CurrentAnimationLoopKeyframeIndex:  " + m_CurrentAnimationLoopKeyframeIndex);


        m_fFrameTime += m_fCurrentAnimationLoopSpeed * fDeltaTime;

        ArticulatedAnimationKeyframe pKeyframe = m_pCurrentAnimationLoop.getAnimationKeyframe(m_CurrentAnimationLoopKeyframeIndex);
        int NextKeyframeIndex = -1;

        if (m_TransitionToAnimationLoopIndex == -1)
        {
            if (m_Direction > 0)
            {
                //  Should we move onto the next frame.
                if (m_fFrameTime >= pKeyframe.getDuration())
                {
                    m_fFrameTime -= pKeyframe.getDuration();

                    NextKeyframeIndex = getNextCurrentKeyframeIndex();
                    if (NextKeyframeIndex == -1)
                    {
                        m_fFrameTime = 0.0f;
                        m_bPlaying = false;
                    }
                    else
                    {
                        m_CurrentAnimationLoopKeyframeIndex = NextKeyframeIndex;
                    }
                }
            }
            else if (m_Direction < 0)
            {
                NextKeyframeIndex = getNextCurrentKeyframeIndex();
                if (NextKeyframeIndex == -1)
                {
                    m_fFrameTime = 0.0f;
                    m_bPlaying = false;
                }
                else
                {    
                    ArticulatedAnimationKeyframe pNextKeyframe = m_pCurrentAnimationLoop.getAnimationKeyframe(NextKeyframeIndex);

                    //  Should we move onto the previous frame.
                    if (m_fFrameTime >= pNextKeyframe.getDuration())
                    {
                        m_fFrameTime -= pNextKeyframe.getDuration();

                        m_CurrentAnimationLoopKeyframeIndex = NextKeyframeIndex;
                    }
                }
            }
        }

        
        //  Get the index of the next Keyframe.
        NextKeyframeIndex = getNextCurrentKeyframeIndex();
        if (NextKeyframeIndex != -1)
        {
            ArticulatedAnimationKeyframe pCurrentKeyframe = m_pCurrentAnimationLoop.getAnimationKeyframe(m_CurrentAnimationLoopKeyframeIndex);
            ArticulatedAnimationKeyframe pNextKeyframe = m_pCurrentAnimationLoop.getAnimationKeyframe(NextKeyframeIndex);
            
            float fFraction = m_fFrameTime / pCurrentKeyframe.getDuration();
            
//            System.out.println(m_CurrentAnimationLoopKeyframeIndex + ", " + NextKeyframeIndex + ", " + fFraction);

            setBoneMatrices(pCurrentKeyframe, pNextKeyframe, fFraction);
        }

        //  Recalculate the Bone Matrices.
//        calculateBoneMatrices();
//        calculateMatrices();

        resolveWorldVectors();
    }
    
    
    public void setBoneMatrices(ArticulatedAnimationKeyframe pCurrentKeyframe,
                                ArticulatedAnimationKeyframe pNextKeyframe,
                                float fFraction)
    {
        int a;
        ArticulatedAnimationKeyframeObject pCurrentKeyframeObject;
        ArticulatedAnimationKeyframeObject pNextKeyframeObject;
        ArticulatedAnimationBoneInstance pBoneInstance;
        Matrix4f LerpMatrix = new Matrix4f();
            
        for (a=0; a<pCurrentKeyframe.getAnimationKeyframeObjectCount(); a++)
        {
            pCurrentKeyframeObject = pCurrentKeyframe.getAnimationKeyframeObject(a);
            pNextKeyframeObject = pNextKeyframe.getAnimationKeyframeObject(a);

            //  Gets the BoneInstance whose Matrix we need to change.
            pBoneInstance = getBoneInstance(pCurrentKeyframeObject.getBoneIndex());
            
            lerpMatrix(pBoneInstance.getLocalMatrix(),
                       pCurrentKeyframeObject.getMatrix(),
                       pNextKeyframeObject.getMatrix(),
                       fFraction);

            pBoneInstance.getMatrix().set(pBoneInstance.getLocalMatrix());
        }
    }

    void lerpMatrix(Matrix4f result, Matrix4f a, Matrix4f b, float fFraction)
    {
//        result.set(a);
//        result.set(result.transpose());

        if (fFraction <= 0.0f)
            result.set(a);
        else if (fFraction >= 1.0f)
            result.set(b);
        else
        {
            float fInvFraction = 1.0f - fFraction;

            result.m00 = a.m00 * fInvFraction + b.m00 * fFraction;
            result.m01 = a.m01 * fInvFraction + b.m01 * fFraction;
            result.m02 = a.m02 * fInvFraction + b.m02 * fFraction;
            result.m03 = a.m03 * fInvFraction + b.m03 * fFraction;

            result.m10 = a.m10 * fInvFraction + b.m10 * fFraction;
            result.m11 = a.m11 * fInvFraction + b.m11 * fFraction;
            result.m12 = a.m12 * fInvFraction + b.m12 * fFraction;
            result.m13 = a.m13 * fInvFraction + b.m13 * fFraction;

            result.m20 = a.m20 * fInvFraction + b.m20 * fFraction;
            result.m21 = a.m21 * fInvFraction + b.m21 * fFraction;
            result.m22 = a.m22 * fInvFraction + b.m22 * fFraction;
            result.m23 = a.m23 * fInvFraction + b.m23 * fFraction;

            result.m30 = a.m30 * fInvFraction + b.m30 * fFraction;
            result.m31 = a.m31 * fInvFraction + b.m31 * fFraction;
            result.m32 = a.m32 * fInvFraction + b.m32 * fFraction;
            result.m33 = a.m33 * fInvFraction + b.m33 * fFraction;

            //result.set(result.transpose());
        }
    }

    //  Gets the BoneInstance at the specified BoneIndex.
    ArticulatedAnimationBoneInstance getBoneInstance(int BoneIndex)
    {
        return(m_BoneInstances[BoneIndex]);
    }
            
    private int getNextCurrentKeyframeIndex()
    {
        int NextKeyframeIndex = 0;

        if (m_Direction > 0)
        {
            NextKeyframeIndex = m_CurrentAnimationLoopKeyframeIndex + 1;
            
            if (NextKeyframeIndex >= m_pCurrentAnimationLoop.getAnimationKeyframeCount())
            {
                if (m_PlayType == PlayType.LOOP)
                    NextKeyframeIndex = 0;
                else if (m_PlayType == PlayType.OSCILATE)
                {
                    NextKeyframeIndex -= 2;
                    m_Direction = -1;
                }
                else
                    NextKeyframeIndex = -1;
            }
        }
        else if (m_Direction < 0)
        {
            NextKeyframeIndex = m_CurrentAnimationLoopKeyframeIndex - 1;
            
            if (NextKeyframeIndex < 0)
            {
                if (m_PlayType == PlayType.LOOP)
                    NextKeyframeIndex = m_pCurrentAnimationLoop.getAnimationKeyframeCount()-1;
                else if (m_PlayType == PlayType.OSCILATE)
                {
                    NextKeyframeIndex = 1;
                    m_Direction = 1;
                }
                else
                    NextKeyframeIndex = -1;
            }
        }

        return(NextKeyframeIndex);
    }
        
        
    public void calculateBoneMatrices()
    {
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
                        calculateBoneMatrices( (ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }
    }

    public void calculateBoneMatrices(ArticulatedAnimationBoneInstance pBoneInstance)
    {
//        pBoneInstance.calculateMatrix();
                
        if (pBoneInstance.getChildren() != null)
        {
            int childNodeCount = pBoneInstance.getChildren().size();
            if (childNodeCount > 0)
            {
                int a;
                Spatial pChildSpatial;
 
                for (a=0; a<childNodeCount; a++)
                {
                    pChildSpatial = pBoneInstance.getChild(a);
                    if (pChildSpatial instanceof ArticulatedAnimationBoneInstance)
                        calculateBoneMatrices( (ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }
    }
 
    
    
    
        
    public void dumpMatrices()
    {
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
                        dumpMatrices("", (ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }   
    }

    public void dumpMatrices(String spacing, ArticulatedAnimationBoneInstance pBoneInstance)
    {
        Matrix4f localMat = pBoneInstance.getLocalMatrix();
        Matrix4f mat = pBoneInstance.getMatrix();

        System.out.println(spacing + "BoneInstance:  " + pBoneInstance.getName());
        System.out.println(spacing + "   LocalMatrix:");
        System.out.println(spacing + "      " + localMat.m00 + ", " + localMat.m01 + ", " + localMat.m02 + ", " + localMat.m03);
        System.out.println(spacing + "      " + localMat.m10 + ", " + localMat.m11 + ", " + localMat.m12 + ", " + localMat.m13);
        System.out.println(spacing + "      " + localMat.m20 + ", " + localMat.m21 + ", " + localMat.m22 + ", " + localMat.m23);
        System.out.println(spacing + "      " + localMat.m30 + ", " + localMat.m31 + ", " + localMat.m32 + ", " + localMat.m33);

        System.out.println(spacing + "   Matrix:");
        System.out.println(spacing + "      " + mat.m00 + ", " + mat.m01 + ", " + mat.m02 + ", " + mat.m03);
        System.out.println(spacing + "      " + mat.m10 + ", " + mat.m11 + ", " + mat.m12 + ", " + mat.m13);
        System.out.println(spacing + "      " + mat.m20 + ", " + mat.m21 + ", " + mat.m22 + ", " + mat.m23);
        System.out.println(spacing + "      " + mat.m30 + ", " + mat.m31 + ", " + mat.m32 + ", " + mat.m33);
        System.out.flush();



        if (pBoneInstance.getChildren() != null)
        {
            int childNodeCount = pBoneInstance.getChildren().size();
            if (childNodeCount > 0)
            {
                int a;
                Spatial pChildSpatial;
 
                for (a=0; a<childNodeCount; a++)
                {
                    pChildSpatial = pBoneInstance.getChild(a);
                    if (pChildSpatial instanceof ArticulatedAnimationBoneInstance)
                        dumpMatrices(spacing + "   ", (ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }
    }


        
    public void calculateMatrices()
    {
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
                        calculateMatrices((ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }   
    }

    public void calculateMatrices(ArticulatedAnimationBoneInstance pBoneInstance)
    {
        //  First, calculate the world matrix of the Bone.

        Matrix4f localMat = pBoneInstance.getLocalMatrix();
        Matrix4f worldMat = pBoneInstance.getMatrix();

        if (pBoneInstance.getParent() instanceof ArticulatedAnimationBoneInstance)
        {
            Matrix4f parentMat = ((ArticulatedAnimationBoneInstance)pBoneInstance.getParent()).getMatrix();

            parentMat.mult(localMat, worldMat);
        }
        else
            worldMat.set(localMat);

        //pBoneInstance.resolveWorldVectors();

        //pBoneInstance.calculateMatrix();

        if (pBoneInstance.getChildren() != null)
        {
            int childNodeCount = pBoneInstance.getChildren().size();
            if (childNodeCount > 0)
            {
                int a;
                Spatial pChildSpatial;
 
                for (a=0; a<childNodeCount; a++)
                {
                    pChildSpatial = pBoneInstance.getChild(a);
                    if (pChildSpatial instanceof ArticulatedAnimationBoneInstance)
                        calculateMatrices((ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }
    }



    public void resolveWorldVectors()
    {
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
                        resolveWorldVectors((ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }   
    }

    public void resolveWorldVectors(ArticulatedAnimationBoneInstance pBoneInstance)
    {
        pBoneInstance.resolveWorldVectors();

        if (pBoneInstance.getChildren() != null)
        {
            int childNodeCount = pBoneInstance.getChildren().size();
            if (childNodeCount > 0)
            {
                int a;
                Spatial pChildSpatial;
 
                for (a=0; a<childNodeCount; a++)
                {
                    pChildSpatial = pBoneInstance.getChild(a);
                    if (pChildSpatial instanceof ArticulatedAnimationBoneInstance)
                        resolveWorldVectors((ArticulatedAnimationBoneInstance)pChildSpatial);
                }
            }
        }
    }

}






