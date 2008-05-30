/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.morphanimation;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.batch.TriangleBatch;
import com.jme.util.geom.BufferUtils;
import imi.loaders.MeshBuffer;

/**
 *
 * @author Lou Hayt
 */
public class MorphAnimationInstance extends Node  //implements Savable 
{
    private  MorphAnimation m_pAnimation = null;

    boolean m_bPlay                 =   true;
    boolean m_bForward              =   true;  // direction
    float   m_fSpeed                =   1.0f;
    
    public enum PlayType { PLAY_ONCE, LOOP, OSCILATE }
    PlayType    m_PlayType = PlayType.OSCILATE;
    
    int m_CurrentLoopIndex          =   0;
    int m_CurrentKeyFrameIndex      =   0;
    int m_NextLoopIndex             =   0;
    int m_NextKeyFrameIndex         =   0;
    
    MorphAnimationLoop currentLoop  =   null;   //  updated before interpolation in update()
    MorphAnimationLoop nextLoop     =   null;   //  updated before interpolation in update()
    
    float   m_AnimationTime         =   0.0f;
    float   m_AnimationDuration     =   0.0f;
    
    int     m_PositionsCount        =   0;
    
    TriMesh mesh                    =   null;
    
    // Constructor
    public MorphAnimationInstance(MorphAnimation pAnimation)
    {
        setMorphAnimation(pAnimation);
        
        // Test
        populateWithTestData();
        
        this.attachChild(mesh);
    }
    
    public void populateWithTestData()
    {
        m_pAnimation.populateWithTestData();
     
        m_PositionsCount    =   m_pAnimation.getMorphedAnimationLoop(0).getPositionsCount();
        
        chooseAnimation(1, 0, 1, 1);
        
        // Creating the tri mesh
        MeshBuffer mb = new MeshBuffer();
        int index1, index2, index3;
         // first vertex
        index1 = mb.addPosition(0.0f, -5.0f, 1.0f);
        mb.addNormal(0.0f, 1.0f, 0.0f);
        mb.addTexCoord(0, 0.5f, 0.0f);
        // secound vertex
        index2 = mb.addPosition(1.0f, -5.0f, -1.0f);
        mb.addNormal(0.0f, 1.0f, 0.0f);
        mb.addTexCoord(0, 1.0f, 1.0f);
        // third vertex
        index3 = mb.addPosition(-1.0f, -5.0f, -1.0f);
        mb.addNormal(0.0f, 1.0f, 0.0f);
        mb.addTexCoord(0, 0.0f, 1.0f);
        // first triangle
        mb.addTriangle(index1, index2, index3); 
        
        // Build the mesh for the first time (TexCoords and indecies should not change past this point)
        mesh = new TriMesh("test trimesh", BufferUtils.createFloatBuffer(mb.getPositions()), BufferUtils.createFloatBuffer(mb.getNormals()), null, BufferUtils.createFloatBuffer(mb.getTexCoords()), BufferUtils.createIntBuffer(mb.getIndices()));         
    }
    
    public void chooseAnimation(int beginLoop, int beginKeyframe, int endLoop, int endKeyframe)   
    {
        m_CurrentLoopIndex     = beginLoop;
        m_CurrentKeyFrameIndex = beginKeyframe;
        m_NextLoopIndex        = endLoop;
        m_NextKeyFrameIndex    = endKeyframe;

        m_AnimationTime      = 0.0f;
        
        if (m_pAnimation.getMorphedAnimationLoop(beginLoop) != null)
            m_AnimationDuration  = m_pAnimation.getMorphedAnimationLoop(beginLoop).getDuration();
    }
    
     public void setMorphAnimation(MorphAnimation animation)
     {
         if(animation != null)
         {
            m_pAnimation        =   animation;
         }
     }
     
     public MorphAnimation getMorphAnimation()
     {
        return m_pAnimation;
     }
    
     private Vector3f interpolatePosition(int index)
     {
         Vector3f initialPosition   = currentLoop.getMorphAnimationKeyframe(m_CurrentKeyFrameIndex).getPosition(index);
         Vector3f finalPosition     = nextLoop.getMorphAnimationKeyframe(m_NextKeyFrameIndex).getPosition(index);
         
         // Interpolate the positions using linear interpolation:
         // CurrentPosition = InitialPosition(Duration-Time) + FinalPosition*Time     0<=Time<=Duration
         Vector3f currentPosition = new Vector3f();
         currentPosition.x = initialPosition.x * (m_AnimationDuration - m_AnimationTime) + finalPosition.x * m_AnimationTime;
         currentPosition.y = initialPosition.y * (m_AnimationDuration - m_AnimationTime) + finalPosition.y * m_AnimationTime;
         currentPosition.z = initialPosition.z * (m_AnimationDuration - m_AnimationTime) + finalPosition.z * m_AnimationTime;
         
         return currentPosition;
     }
     
     private Vector3f interpolateNormal(int index)
     {
         Vector3f initialNormal   = currentLoop.getMorphAnimationKeyframe(m_CurrentKeyFrameIndex).getNormal(index);
         Vector3f finalNormal     = nextLoop.getMorphAnimationKeyframe(m_NextKeyFrameIndex).getNormal(index);
         
         // Interpolate the Normals using linear interpolation:
         // CurrentNormal = InitialNormal(Duration-Time) + FinalNormal*Time     0<=Time<=Duration
         Vector3f currentNormal = new Vector3f();
         currentNormal.x = initialNormal.x * (m_AnimationDuration - m_AnimationTime) + finalNormal.x * m_AnimationTime;
         currentNormal.y = initialNormal.y * (m_AnimationDuration - m_AnimationTime) + finalNormal.y * m_AnimationTime;
         currentNormal.z = initialNormal.z * (m_AnimationDuration - m_AnimationTime) + finalNormal.z * m_AnimationTime;
         
         return currentNormal.normalize();
     }
     
    /**
     * updateGeometricState overrides Spatials updateGeometric state to update
     * the mesh 
     * 
     * @param time
     *            the time that has passed between calls.
     * @param initiator
     *            true if this is the top level being called.
     */
//     @Override
//    public void updateGeometricState(float time, boolean initiator) 
//    {
//         updateAnimation(time);
//         super.updateGeometricState(time, initiator);
//    }

    public void updateAnimation(float deltaTime)
    {
        if(!m_bPlay)
            return;
        
        m_AnimationTime += deltaTime * m_fSpeed;
        
        //  Are we past the duration time?
        if (m_AnimationTime > m_AnimationDuration)
        {
            switch(m_PlayType)
            {
                case PLAY_ONCE:
                {           
                    m_bPlay = false;
                    return;
                    //break;
                }
                case LOOP:
                {     
                    m_AnimationTime = 0.0f;
                    break;
                }
                case OSCILATE:
                {                    
                    m_bForward = !m_bForward;
                    chooseAnimation(m_NextLoopIndex, m_NextKeyFrameIndex, m_CurrentLoopIndex, m_CurrentKeyFrameIndex);
                    break;
                }
            }
        }
        
        //  Get ready for interpolation
        currentLoop =  m_pAnimation.getMorphedAnimationLoop(m_CurrentLoopIndex);
        nextLoop    =  m_pAnimation.getMorphedAnimationLoop(m_NextLoopIndex);
        if (currentLoop == null || nextLoop == null)
             return;
         
        MeshBuffer mb = new MeshBuffer();
        for (int i = 0; i < m_PositionsCount; i = i + 3)
        {
            // interpolate positions
            mb.addPosition(interpolatePosition(i));
            // interpolate the normals
            mb.addNormal(interpolateNormal(i));
                
            mb.addPosition(interpolatePosition(i+1));
            mb.addNormal(interpolateNormal(i+1));
                
            mb.addPosition(interpolatePosition(i+2));
            mb.addNormal(interpolateNormal(i+2));
        } 
        
         // Reconstruct the mesh - only positions and normals
        TriangleBatch triBatch = mesh.getBatch(0);
        triBatch.setVertexBuffer(BufferUtils.createFloatBuffer(mb.getPositions()));
        triBatch.setNormalBuffer(BufferUtils.createFloatBuffer(mb.getNormals()));
    
    }
    
    public void Stop()
    {
        m_bPlay = false;
    }
    public void Play()
    {
        m_bPlay = true;
    }
    public void Play(boolean play)
    {
        m_bPlay = play;
    }
    public boolean isPlaying()
    {
        return m_bPlay;
    }
    public boolean isDirectionForward()
    {
        return m_bForward;
    }
    public void setSpeed(float speed)
    {
        m_fSpeed = speed; // error checking needed?
    }
    public float getSpeed()
    {
        return m_fSpeed;
    }
    
 
    
//       - taken for reference from AnimationController.java - 
//    public void write(JMEExporter e) throws IOException {
//        super.write(e);
//        OutputCapsule cap = e.getCapsule(this);
//        cap.writeSavableArrayList(animationSets, "animationSets", null);
//        cap.write(skeleton, "skeleton", null);
//        cap.write(activeAnimation, "activeAnimation", null);
//    }
//
//    @SuppressWarnings("unchecked")
//    public void read(JMEImporter e) throws IOException {
//        super.read(e);
//        InputCapsule cap = e.getCapsule(this);
//        animationSets = cap.readSavableArrayList("animationSets", null);
//        skeleton = (Bone)cap.readSavable("skeleton", null);
//        activeAnimation = (BoneAnimation)cap.readSavable("activeAnimation", null);
//    }
    
}
