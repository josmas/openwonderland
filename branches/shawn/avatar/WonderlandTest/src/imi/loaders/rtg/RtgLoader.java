/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.loaders.rtg;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipOutputStream;

import com.jme.math.*;
import com.jme.renderer.*;

import imi.animations.articulatedanimation.*;
import imi.loaders.MeshBuffer;



/**
 *
 * @author Chris Nagle
 */
public class RtgLoader
{
    private String m_Filename = "";
    private StreamTokenizer m_Stream = null;

    private BufferedReader m_Reader = null;
    
    private long m_StartTime = 0;
    private long m_EndTime = 0;
    private long m_Duration = 0;
    
    private int m_ShapeCount = 0;

    
    boolean OUTPUT_VERT_NORMS;
    boolean OUTPUT_VERT_COLORS;
    boolean OUTPUT_TEX_COORDS;
    boolean OUTPUT_POLY_NORMS;
    boolean OUTPUT_HIERARCHY;
    boolean OUTPUT_LOCAL;
    boolean SHOW_INDEX_COUNTERS;
    boolean OUTPUT_MATERIALS;
    boolean OUTPUT_ANIMATION;
    boolean OUTPUT_ALL_NODES;
    boolean OUTPUT_DECOMP;
    boolean OUTPUT_DEGREES;
    
    boolean m_bEndOfAnimationReached = false;



    ArrayList m_Materials = new ArrayList();
    ArrayList m_SubMeshes = new ArrayList();
    ArrayList m_Bones = new ArrayList();
    ArrayList m_RootBones = new ArrayList();
    ArrayList m_QueueOfBones = new ArrayList(); 
    ArrayList m_Frames = new ArrayList();


    ArticulatedAnimation m_pArticulatedAnimation = null;



    //  Constructor.
    public RtgLoader()
    {

    }
            
    
    //  Gets the loaded ArticulatedAnimation.
    public ArticulatedAnimation getArticulatedAnimation()
    {
        return(m_pArticulatedAnimation);
    }

    public static StreamTokenizer getDefaultTokenStream(BufferedReader bReader)
    {
        StreamTokenizer stream;
        try
        {
            stream = new StreamTokenizer(bReader);
            stream.eolIsSignificant(false);
            stream.commentChar('#');
            stream.quoteChar(((int) '"'));
            stream.wordChars(((int) '_'), ((int) '_'));
            stream.wordChars(((int) '{'), ((int) '{'));
            stream.wordChars(((int) '}'), ((int) '}'));
            stream.wordChars(((int) ')'), ((int) ')'));
            stream.wordChars(((int) '('), ((int) '('));
            stream.wordChars(((int) '='), ((int) '='));
            stream.wordChars(((int) ','), ((int) ','));
            stream.wordChars(((int) '/'), ((int) '/'));
            stream.wordChars(((int) '\\'), ((int) '\\'));
            stream.wordChars(((int) ';'), ((int) ';'));
            //stream.whitespaceChars( ((int)','), ((int)','));
            stream.wordChars(((int) '.'), ((int) '.'));
            stream.eolIsSignificant(false);
        }
        catch (Exception e)
        {
            //Logger.getLogger("j3d.utils.loaders.rtg").log(Level.WARNING,"",e);
            return null;
        }

        return stream;
    }

        
        
    //  Loads the File.
    public boolean load(String filename)
        throws java.io.FileNotFoundException, IncorrectFormatException, Exception
    {
        //  Sanity check.
        if (filename == null)
            throw new FileNotFoundException();

        m_StartTime = System.currentTimeMillis();
        m_EndTime = 0;
        m_Duration = 0;
        m_ShapeCount = 0;

        try
        {
            InputStream inputStream = new FileInputStream(filename);

            if (filename.endsWith(".gz"))
                inputStream = new GZIPInputStream(inputStream);
            else if (filename.endsWith(".zip"))
                inputStream = new ZipInputStream(inputStream);


            m_Reader = new BufferedReader(new InputStreamReader(inputStream));
            m_Stream = getDefaultTokenStream(m_Reader);
            
            m_Filename = filename;
        }
	catch (Exception e)
	{
            throw new FileNotFoundException();
	}
	
        try
        {
            boolean bResult = readFile(m_Stream);
            
            postLoad();

            //DumpMaterials();
            //DumpBones();
            //DumpFrames();
            
            return(true);
        }

        catch (IncorrectFormatException e)
        {
            //Logger.getLogger("j3d.utils.loaders.rtg").info(e);
            throw e;
        }

/*
        catch (ParsingErrorException e)
        {
            //Logger.getLogger("j3d.utils.loaders.rtg").info(e);
            throw e;
        }
*/
        catch (Exception e)
        {
            throw e;
        }
    }
        

    static private void debugln(int level, String string)
    {
//        if (debugLevel >= level)
            System.out.println("jmonkey_v1.loaders.rtg:  " + string);
    }
        

    //  Reads the contents of the file.
    private boolean readFile(StreamTokenizer stream)
        throws IncorrectFormatException, Exception
    {
	String title = "";
        int numberOfObjects = 0;
        boolean gotAttributes, gotgroup, gotEntity, gotSubEntity;

        
        // Read header
	stream.nextToken();
//	debugln(1, "TOKEN: " + stream.sval);
        if (!"HEADER_TITLE".equals(stream.sval))
            throw new IncorrectFormatException();
	
        title = m_Reader.readLine();
	//stream.nextToken();
//	debugln(1, "TOKEN: " + title);

	stream.nextToken();
//	debugln(1, "TOKEN: " + stream.sval);
	if (!"HEADER_VERSION".equals(stream.sval))
            throw new IncorrectFormatException();
	stream.nextToken();
//	debugln(1, "TOKEN: " + stream.nval);

	stream.nextToken();
//	debugln(1, "TOKEN: " + stream.sval);
	if (!"NUMBER_OF_OBJECTS".equals(stream.sval))
		throw new IncorrectFormatException();
	stream.nextToken();
	numberOfObjects = (int) stream.nval;
//	debugln(1, "Number Of Objects: " + numberOfObjects);


        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_VERT_NORMS".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_VERT_NORMS = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_VERT_COLORS".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_VERT_COLORS = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_TEX_COORDS".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_TEX_COORDS = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_POLY_NORMS".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_POLY_NORMS = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_HIERARCHY".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_HIERARCHY = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_LOCAL".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_LOCAL = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"SHOW_INDEX_COUNTERS".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        SHOW_INDEX_COUNTERS = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_MATERIALS".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_MATERIALS = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_ANIMATION".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_ANIMATION = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_ALL_NODES".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_ALL_NODES = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_DECOMP".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_DECOMP = ("on".equals(stream.sval)) ? true : false;

        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        if (!"OUTPUT_DEGREES".equals(stream.sval))
            throw new IncorrectFormatException();
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        OUTPUT_DEGREES = ("on".equals(stream.sval)) ? true : false;

                     
        
        
        
        //  ****************************
        //  Read the Material list.
        //  ****************************
        
        String TEXTURE_FILENAME = null;
        boolean reading;
        stream.nextToken();
//        debugln(1, "TOKEN: " + stream.sval);
        int matCount = 0;
        if ("MATERIAL_LIST".equals(stream.sval))
        {
            boolean bReadingMaterials = true;
            RtgMaterial pMaterial = null;
            
            while (bReadingMaterials)
            {
                stream.nextToken();
                if ("MATERIAL".equals(stream.sval))
                {
                    if (pMaterial != null)
                        m_Materials.add(pMaterial);
                
                    pMaterial = new RtgMaterial();
                    
                    pMaterial.m_Index = m_Materials.size();

                    stream.nextToken();
                }

                else if ("NAME".equals(stream.sval))
                {
                    stream.nextToken();
                    
                    pMaterial.m_Name = stream.sval;
                }

                else if ("AMBIENT".equals(stream.sval))
                {
                    float red, green, blue;
                    stream.nextToken();
                    
                    red = (float)stream.nval;  stream.nextToken();
                    green = (float)stream.nval;  stream.nextToken();
                    blue = (float)stream.nval;
                    
                    pMaterial.m_Ambient.set(red, green, blue, 1.0f);
                }                

                else if ("DIFFUSE".equals(stream.sval))
                {
                    float red, green, blue;
                    stream.nextToken();
                    
                    red = (float)stream.nval;  stream.nextToken();
                    green = (float)stream.nval;  stream.nextToken();
                    blue = (float)stream.nval;
                    
                    pMaterial.m_Diffuse.set(red, green, blue, 1.0f);
                }
                
                else if ("SPECULAR".equals(stream.sval))
                {
                    float red, green, blue;
                    stream.nextToken();
                    
                    red = (float)stream.nval;  stream.nextToken();
                    green = (float)stream.nval;  stream.nextToken();
                    blue = (float)stream.nval;
                    
                    pMaterial.m_Specular.set(red, green, blue, 1.0f);
                }
                
                else if ("EMISSION".equals(stream.sval))
                {
                    float red, green, blue;
                    stream.nextToken();
                    
                    red = (float)stream.nval;  stream.nextToken();
                    green = (float)stream.nval;  stream.nextToken();
                    blue = (float)stream.nval;
                    
                    pMaterial.m_Emission.set(red, green, blue, 1.0f);
                }
                
                else if ("SHININESS".equals(stream.sval))
                {
                    float fShininess;
                    stream.nextToken();
                    
                    fShininess = (float)stream.nval;

                    pMaterial.m_fShininess = fShininess;
                }

                else if ("TRANSPARENCY".equals(stream.sval))
                {
                    float fTransparency;
                    stream.nextToken();
                    
                    fTransparency = (float)stream.nval;

                    pMaterial.m_fTransparency = fTransparency;
                }

                else if ("TEXTURE_NAME".equals(stream.sval))
                {
                    stream.nextToken();
                    
                    pMaterial.m_TextureName = stream.sval;
                }

                else if ("TEXTURE_FILENAME".equals(stream.sval))
                {
                    stream.nextToken();
                    
                    pMaterial.m_TextureFilename = stream.sval;
                }

                if ("END_MATERIAL_LIST".equals(stream.sval))
                    bReadingMaterials = false;
            }

            //Logger.getLogger("j3d.utils.loaders.rtg").info("Done reading Materials");
	}

        
        while (true)
        {
            stream.nextToken();
            
            if ("END_HIERARCHY_LIST".equals(stream.sval))
                break;
            
            if ("HIERARCHY_LIST".equals(stream.sval))
            {
                readHierarchyList(stream);
                break;
            }
        }


        //  Read all the SubMeshes.
        while (true)
        {
            stream.nextToken();
            
            if ("ANIMATION_LIST".equals(stream.sval))
                break;

            if ("END_ANIMATION_LIST".equals(stream.sval))
                break;

            if ("OBJECT_START".equals(stream.sval))
            {
                RtgSubMesh pSubMesh = readSubMesh(stream);
                
                m_SubMeshes.add(pSubMesh);
            }
        }
        
        
        //  Read all the Frames.
        while (true)
        {
            if (m_bEndOfAnimationReached)
                break;

            stream.nextToken();
            
            if ("END_ANIMATION_LIST".equals(stream.sval))
                break;

            if ("FRAME".equals(stream.sval))
            {
                RtgFrame pFrame = readFrame(stream);

                m_Frames.add(pFrame);
            }
        }
        
        
        return(true);
    }
    
    
    private RtgFrame readFrame(StreamTokenizer stream)
        throws IncorrectFormatException, Exception
    {
        RtgFrame pFrame = new RtgFrame();
        RtgFrameObject pFrameObject = null;

        stream.nextToken();
        
        pFrame.m_Index = (int)stream.nval; stream.nextToken();
        
        while (true)
        {
            if ("FRAME".equals(stream.sval))
                break;

            if ("END_ANIMATION_LIST".equals(stream.sval))
            {
                m_bEndOfAnimationReached = true;
                break;
            }

            if ("OBJECT".equals(stream.sval))
            {
                stream.nextToken();
                
                pFrameObject = new RtgFrameObject();
                pFrameObject.m_ObjectName = stream.sval;
                
                stream.nextToken();
                
                readMatrixInfo(pFrameObject.m_MatrixInfo, stream);

                readMatrix(pFrameObject.m_Matrix, stream);

                pFrame.m_FrameObjects.add(pFrameObject);
            }
        }

        return(pFrame);
    }
    

    private void readHierarchyList(StreamTokenizer stream)
        throws IncorrectFormatException, Exception
    {
        RtgBone pParentBone = null;
        RtgBone pBone = null;

        stream.nextToken();
        stream.nextToken();
        stream.nextToken();
        stream.nextToken();


        while (true)
        {
            if ("END_HIERARCHY_LIST".equals(stream.sval))
                break;

            pBone = new RtgBone();
            
            pBone.m_Level = (int)stream.nval; stream.nextToken();
            stream.nextToken();
            pBone.m_Name = stream.sval; stream.nextToken();

            if (pParentBone != null)
            {
                //  Reading a child bone of last bone.
                if (pBone.m_Level > pParentBone.m_Level)
                {
                }
                else if (pBone.m_Level == pParentBone.m_Level)
                {
                    pParentBone = popBone();
                }
                else
                {
                    while (true)
                    {
                        pParentBone = popBone();
                        if (pParentBone == null)
                            break;
                        if (pParentBone.m_Level == pBone.m_Level - 1)
                            break;
                    }
                }
            }
            

            readMatrixInfo(pBone.m_MatrixInfo, stream);

            readMatrix(pBone.m_Matrix, stream);


            if (pParentBone == null)
                m_RootBones.add(pBone);
            else
                pParentBone.m_ChildBones.add(pBone);
            
            pushBone(pBone);
            pParentBone = pBone;

            pBone.m_Index = m_Bones.size();
            m_Bones.add(pBone);
        }
    }

                
    public RtgSubMesh readSubMesh(StreamTokenizer stream)
        throws IncorrectFormatException, Exception
    {
//        System.out.println("RtgLoader.readSubMesh()");


        RtgSubMesh pSubMesh = new RtgSubMesh();


        //  OBJECT_START was already read.
        stream.nextToken();
        pSubMesh.m_Name = stream.sval;
        m_Reader.readLine();
        stream.nextToken();

        //  Uses a Material.
        if ("USES_MATERIAL".equals(stream.sval))
        {
            stream.nextToken();
            pSubMesh.m_MaterialIndex = (int)stream.nval;
            stream.nextToken();
            
            pSubMesh.m_MaterialName = stream.sval;
            stream.nextToken();
        }

        if ("VERTEX".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
                        
            int Index = 0;
            float x, y, z;
            
            while (true)
            {
                stream.nextToken();
                
                x = (float)stream.nval; stream.nextToken();
                y = (float)stream.nval; stream.nextToken();
                z = (float)stream.nval; stream.nextToken();

//                if (m_SubMeshes.size() == 0)
//                    System.out.println("   Vertex[" + Index + "]:  (" + x + ", " + y + ", " + z + ")");

                pSubMesh.m_Positions.add(new Vector3f(x, y, z));
                
                Index++;

                if ("COLOR".equals(stream.sval))
                    break;
                if ("NORMAL".equals(stream.sval))
                    break;
                if ("TEXCOORD".equals(stream.sval))
                    break;
            }
        }

        
        if ("COLOR".equals(stream.sval))
        {
            stream.nextToken();
                    
            int Index = 0;
            float red, green, blue;
            
            while (true)
            {
                stream.nextToken();
                
                red = (float)stream.nval; stream.nextToken();
                green = (float)stream.nval; stream.nextToken();
                blue = (float)stream.nval; stream.nextToken();
                stream.nextToken();

//                if (m_SubMeshes.size() == 0)
//                    System.out.println("   Color[" + Index + "]:  (" + red + ", " + green + ", " + blue + ")");

                pSubMesh.m_Colors.add(new Vector3f(red, green, blue));
                
                Index++;

                if ("NORMAL".equals(stream.sval))
                    break;
            }
        }        

        if ("NORMAL".equals(stream.sval))
        {
            stream.nextToken();
                   
            int Index = 0;
            float x, y, z;
            
            while (true)
            {
                stream.nextToken();
                
                x = (float)stream.nval; stream.nextToken();
                y = (float)stream.nval; stream.nextToken();
                z = (float)stream.nval; stream.nextToken();

//                if (m_SubMeshes.size() == 0)
//                    System.out.println("   Normal[" + Index + "]:  (" + x + ", " + y + ", " + z + ")");

                pSubMesh.m_Normals.add(new Vector3f(x, y, z));
                
                Index++;

                if ("TEXCOORD".equals(stream.sval))
                    break;
            }
        }
        
        
        if ("TEXCOORD".equals(stream.sval))
        {
            stream.nextToken();
            
            int Index = 0;
            float u, v;
            
            while (true)
            {
                if (stream.sval != null && stream.sval.equals("POLYGON"))
                    break;

                stream.nextToken();
                
                u = (float)stream.nval; stream.nextToken();
                v = (float)stream.nval; stream.nextToken();

//                if (m_SubMeshes.size() == 0)
//                    System.out.println("   TexCoord[" + Index + "]:  (" + u + ", " + v + ")");

                pSubMesh.m_TexCoords.add(new Vector2f(u, v));
                
                Index++;

                if ("POLYGON".equals(stream.sval))
                    break;
            }
        }
           
        if ("POLYGON".equals(stream.sval))
        {
            if (pSubMesh.m_Name.equals("Hair2"))
            {
                int a = 0;
                a = 4;
            }
            stream.nextToken();
                        
            int Index = 0;
            RtgPolygon pPolygon = null;
            boolean bExtraIndex = false;
            
            while (true)
            {
                stream.nextToken();

//                0 v 0 1 2 n 0 1 2 t 0 1 2

                pPolygon = new RtgPolygon();

                bExtraIndex = false;

                if (pSubMesh.m_Name.equals("Hair2") && Index == 60)
                {
                    int a = 0;
                }
                //  v.
                stream.nextToken();
                pPolygon.m_PositionIndices[0] = (int)stream.nval; stream.nextToken();
                pPolygon.m_PositionIndices[1] = (int)stream.nval; stream.nextToken();
                pPolygon.m_PositionIndices[2] = (int)stream.nval; stream.nextToken();
                if (stream.sval == null)
                {
                    bExtraIndex = true;
                    stream.nextToken();
                }

                //  n.
                if (stream.sval != null && stream.sval.equals("n"))
                {
                    stream.nextToken();
                    pPolygon.m_NormalIndices[0] = (int)stream.nval; stream.nextToken();
                    pPolygon.m_NormalIndices[1] = (int)stream.nval; stream.nextToken();
                    pPolygon.m_NormalIndices[2] = (int)stream.nval; stream.nextToken();
                    if (bExtraIndex)
                        stream.nextToken();
                }

                //  t.
                if (stream.sval != null && stream.sval.equals("t"))
                {
                    stream.nextToken();
                    pPolygon.m_TexCoordIndices[0] = (int)stream.nval; stream.nextToken();
                    pPolygon.m_TexCoordIndices[1] = (int)stream.nval; stream.nextToken();
                    pPolygon.m_TexCoordIndices[2] = (int)stream.nval; stream.nextToken();
                    if (bExtraIndex)
                        stream.nextToken();
                }

/*                
                if (m_SubMeshes.size() == 0)
                {
                    System.out.print("   Polygon[" + Index + "]:  ");
                    System.out.print("v (" + pPolygon.m_PositionIndices[0] + ", " + pPolygon.m_PositionIndices[1] + ", " + pPolygon.m_PositionIndices[2] + ")  ");
                    System.out.print("n (" + pPolygon.m_NormalIndices[0] + ", " + pPolygon.m_NormalIndices[1] + ", " + pPolygon.m_NormalIndices[2] + ")  ");
                    System.out.print("t (" + pPolygon.m_TexCoordIndices[0] + ", " + pPolygon.m_TexCoordIndices[1] + ", " + pPolygon.m_TexCoordIndices[2] + ")");
                    System.out.println("");
                }
*/

                pSubMesh.m_Polygons.add(pPolygon);

                Index++;

                if ("OBJECT_END".equals(stream.sval))
                    break;
            }
        }

        return(pSubMesh);
    }
    
    
    private void readMatrixInfo(RtgMatrixInfo pMatrixInfo, StreamTokenizer stream)
        throws IncorrectFormatException, Exception
    {
        if ("tran".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrixInfo.m_Tran.x = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_Tran.y = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_Tran.z = (float)stream.nval; stream.nextToken();
        }
        if ("rot".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrixInfo.m_Rot.x = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_Rot.y = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_Rot.z = (float)stream.nval; stream.nextToken();
        }
        if ("scal".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrixInfo.m_Scal.x = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_Scal.y = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_Scal.z = (float)stream.nval; stream.nextToken();
        }
        if ("sPiv".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrixInfo.m_sPiv.x = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_sPiv.y = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_sPiv.z = (float)stream.nval; stream.nextToken();
        }
        if ("rPiv".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrixInfo.m_rPiv.x = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_rPiv.y = (float)stream.nval; stream.nextToken();
            pMatrixInfo.m_rPiv.z = (float)stream.nval; stream.nextToken();
        }
    }
 
        
    private void readMatrix(Matrix4f pMatrix, StreamTokenizer stream)
        throws IncorrectFormatException, Exception
    {
        if ("ltm0".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrix.m00 = (float)stream.nval; stream.nextToken();
            pMatrix.m01 = (float)stream.nval; stream.nextToken();
            pMatrix.m02 = (float)stream.nval; stream.nextToken();
            pMatrix.m03 = (float)stream.nval; stream.nextToken();
        }
        if ("ltm1".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrix.m10 = (float)stream.nval; stream.nextToken();
            pMatrix.m11 = (float)stream.nval; stream.nextToken();
            pMatrix.m12 = (float)stream.nval; stream.nextToken();
            pMatrix.m13 = (float)stream.nval; stream.nextToken();
        }
        if ("ltm2".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrix.m20 = (float)stream.nval; stream.nextToken();
            pMatrix.m21 = (float)stream.nval; stream.nextToken();
            pMatrix.m22 = (float)stream.nval; stream.nextToken();
            pMatrix.m23 = (float)stream.nval; stream.nextToken();
        }
        if ("ltm3".equals(stream.sval))
        {
            stream.nextToken();
            stream.nextToken();
            pMatrix.m30 = (float)stream.nval; stream.nextToken();
            pMatrix.m31 = (float)stream.nval; stream.nextToken();
            pMatrix.m32 = (float)stream.nval; stream.nextToken();
            pMatrix.m33 = (float)stream.nval; stream.nextToken();
        }
    }

    
    
    public void DumpMaterials()
    {
        int a;
        RtgMaterial pMaterial;

        System.out.println("Materials:  " + m_Materials.size());

        for (a=0; a<m_Materials.size(); a++)
        {
            pMaterial = (RtgMaterial)m_Materials.get(a);
            
            System.out.println("   Material:  " + a);
            System.out.println("      Name:             " + pMaterial.m_Name);
            System.out.println("      Ambient:          (" + pMaterial.m_Ambient.red + ", " + pMaterial.m_Ambient.green + ", " + pMaterial.m_Ambient.blue + ")");
            System.out.println("      Diffuse:          (" + pMaterial.m_Diffuse.red + ", " + pMaterial.m_Diffuse.green + ", " + pMaterial.m_Diffuse.blue + ")");
            System.out.println("      Specular:         (" + pMaterial.m_Specular.red + ", " + pMaterial.m_Specular.green + ", " + pMaterial.m_Specular.blue + ")");
            System.out.println("      Emission:         (" + pMaterial.m_Emission.red + ", " + pMaterial.m_Emission.green + ", " + pMaterial.m_Emission.blue + ")");
            System.out.println("      Shininess:        " + pMaterial.m_fShininess);
            System.out.println("      Transparency:     " + pMaterial.m_fTransparency);
            System.out.println("      TextureName:      " + pMaterial.m_TextureName);
            System.out.println("      TextureFilename:  " + pMaterial.m_TextureFilename);
        }
    }

    public void DumpBones()
    {
        int a;
        RtgBone pBone;
        
        System.out.println("****  Bone dump:");
        for (a=0; a<m_RootBones.size(); a++)
        {
            pBone = (RtgBone)m_RootBones.get(a);
            
            DumpBone("", pBone);
        }
        System.out.println("****  Bone dump:");
        System.out.flush();
    }
    
    public void DumpBone(String spacing, RtgBone pBone)
    {
        System.out.println(spacing + pBone.m_Level + " " + pBone.m_Name);

        if (pBone.m_ChildBones.size() > 0)
        {
            int a;
            RtgBone pChildBone;

            for (a=0; a<pBone.m_ChildBones.size(); a++)
            {
                pChildBone = (RtgBone)pBone.m_ChildBones.get(a);

                DumpBone(spacing + "   ", pChildBone);
            }
        }
    }

    public void DumpFrames()
    {
        int a;
        RtgFrame pFrame;

        System.out.println("Frames:  " + m_Frames.size());

        for (a=0; a<m_Frames.size(); a++)
        {
            pFrame = (RtgFrame)m_Frames.get(a);
            
            DumpFrame(pFrame);
        }
    }
    
    public void DumpFrame(RtgFrame pFrame)
    {
        int a;
        RtgFrameObject pFrameObject;

        System.out.println("   FrameObjects:  " + pFrame.m_FrameObjects.size());

        for (a=0; a<pFrame.m_FrameObjects.size(); a++)
        {
            pFrameObject = (RtgFrameObject)pFrame.m_FrameObjects.get(a);
            
            System.out.println("      Object:  " + pFrameObject.m_ObjectName);
            
            dumpMatrixInto("         ", pFrameObject.m_MatrixInfo);
            dumpMatrix("         ", pFrameObject.m_Matrix);
        }
    }
    
    public void dumpMatrixInto(String spacing, RtgMatrixInfo pMatrixInfo)
    {
        System.out.println(spacing + "Tran:   " + pMatrixInfo.m_Tran.x + ", " + pMatrixInfo.m_Tran.y + ", " + pMatrixInfo.m_Tran.z);
        System.out.println(spacing + "Rot:    " + pMatrixInfo.m_Rot.x + ", " + pMatrixInfo.m_Rot.y + ", " + pMatrixInfo.m_Rot.z);
        System.out.println(spacing + "Scal:   " + pMatrixInfo.m_Scal.x + ", " + pMatrixInfo.m_Scal.y + ", " + pMatrixInfo.m_Scal.z);
        System.out.println(spacing + "sPiv:   " + pMatrixInfo.m_sPiv.x + ", " + pMatrixInfo.m_sPiv.y + ", " + pMatrixInfo.m_sPiv.z);
        System.out.println(spacing + "rPiv:   " + pMatrixInfo.m_rPiv.x + ", " + pMatrixInfo.m_rPiv.y + ", " + pMatrixInfo.m_rPiv.z);
    }
    
    public void dumpMatrix(String spacing, Matrix4f pMatrix)
    {
        System.out.println(spacing + "Matrix:");
        System.out.println(spacing + "   " + pMatrix.m00 + ", " + pMatrix.m01 + ", " + pMatrix.m02 + ", " + pMatrix.m03);
        System.out.println(spacing + "   " + pMatrix.m10 + ", " + pMatrix.m11 + ", " + pMatrix.m12 + ", " + pMatrix.m13);
        System.out.println(spacing + "   " + pMatrix.m20 + ", " + pMatrix.m21 + ", " + pMatrix.m22 + ", " + pMatrix.m23);
        System.out.println(spacing + "   " + pMatrix.m30 + ", " + pMatrix.m31 + ", " + pMatrix.m32 + ", " + pMatrix.m33);
    }


    private void pushBone(RtgBone pBone)
    {
        m_QueueOfBones.add(pBone);
    }
    
    private RtgBone popBone()
    {
        if (m_QueueOfBones.size() == 0)
            return(null);

        m_QueueOfBones.remove(m_QueueOfBones.size()-1);

        if (m_QueueOfBones.size() == 0)
            return(null);

        RtgBone pBone = (RtgBone)m_QueueOfBones.get(m_QueueOfBones.size()-1);

        return(pBone);
    }

    
    
    private void postLoad()
    {
        //  Create the Animation.
        if (m_pArticulatedAnimation == null)
            m_pArticulatedAnimation = new ArticulatedAnimation();
       
        
        //  Convert all the Materials.
        if (m_Materials.size() > 0)
        {
            int a;
            RtgMaterial pRtgMaterial;
            ArticulatedAnimationMaterial pAnimationMaterial;
            
            for (a=0; a<m_Materials.size(); a++)
            {
                pRtgMaterial = (RtgMaterial)m_Materials.get(a);
                
                //  Create an AnimationMaterial and translate.
                pAnimationMaterial = new ArticulatedAnimationMaterial();
         
                pAnimationMaterial.setName(pRtgMaterial.m_Name);
                pAnimationMaterial.setAmbient(pRtgMaterial.m_Ambient);
                pAnimationMaterial.setDiffuse(pRtgMaterial.m_Diffuse);
                pAnimationMaterial.setSpecular(pRtgMaterial.m_Specular);
                pAnimationMaterial.setEmission(pRtgMaterial.m_Emission);
                pAnimationMaterial.setShininess(pRtgMaterial.m_fShininess);
                pAnimationMaterial.setTransparency(pRtgMaterial.m_fTransparency);
                pAnimationMaterial.setTextureFilename(pRtgMaterial.m_TextureFilename);
                
                m_pArticulatedAnimation.addAnimationMaterial(pAnimationMaterial);
            }
        }

    
        //  Convert all the Meshes.
        if (m_SubMeshes.size() > 0)
        {
            int a;
            RtgSubMesh pRtgSubMesh;
            ArticulatedAnimationGeometry pAnimationGeometry;
            MeshBuffer pMeshBuffer;
            int PolygonIndex;
            RtgPolygon pPolygon;
            int PositionIndex;
            int NormalIndex;
            int TexCoordIndex;
            Vector3f pPosition;
            Vector3f pNormal;
            Vector2f pTexCoord;
            int []Indices = new int[3];


            for (a=0; a<m_SubMeshes.size(); a++)
            {
                pRtgSubMesh = (RtgSubMesh)m_SubMeshes.get(a);

                pMeshBuffer = new MeshBuffer();

                //  Iterate through all the polygons.
                for (PolygonIndex=0; PolygonIndex<pRtgSubMesh.m_Polygons.size(); PolygonIndex++)
                {
                    pPolygon = (RtgPolygon)pRtgSubMesh.m_Polygons.get(PolygonIndex);
                    
                    for (int i=0; i<3; i++)
                    {
                        PositionIndex = pPolygon.m_PositionIndices[i];
                        NormalIndex = pPolygon.m_NormalIndices[i];
                        TexCoordIndex = pPolygon.m_TexCoordIndices[i];

                        try
                        {
                            pPosition = (Vector3f)pRtgSubMesh.m_Positions.get(PositionIndex);
                            pNormal = (Vector3f)pRtgSubMesh.m_Normals.get(NormalIndex);
                            pTexCoord = (Vector2f)pRtgSubMesh.getTexCoord(TexCoordIndex);

                            Indices[i] = pMeshBuffer.addPosition(pPosition);
                            pMeshBuffer.addNormal(pNormal);
                            if (pTexCoord != null)
                                pMeshBuffer.addTexCoord(0, pTexCoord);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    
                    pMeshBuffer.addTriangle(Indices[0], Indices[1], Indices[2]);
                }
                
                
                pAnimationGeometry = new ArticulatedAnimationGeometry();
                
                pAnimationGeometry.setName(pRtgSubMesh.m_Name);
                pAnimationGeometry.setAnimationMaterial(m_pArticulatedAnimation.getAnimationMaterial(pRtgSubMesh.m_MaterialName));
                pAnimationGeometry.populateTriMesh(pMeshBuffer);
                
                m_pArticulatedAnimation.addAnimationGeometry(pAnimationGeometry);
            }
        }

    
        //  Convert all the Bones.
        if (m_RootBones.size() > 0)
        {
            int a;
            RtgBone pRtgBone;
            ArticulatedAnimationBone pAnimationBone;
            
            for (a=0; a<m_RootBones.size(); a++)
            {
                pRtgBone = (RtgBone)m_RootBones.get(a);
                
                pAnimationBone = new ArticulatedAnimationBone();
                pAnimationBone.setIndex(pRtgBone.m_Index);

                m_pArticulatedAnimation.addAnimationBone(pAnimationBone);

                copyAnimationBone(pRtgBone, pAnimationBone);
                
                m_pArticulatedAnimation.addRootBone(pAnimationBone);
            }
        }

    
        //  Convert the loaded animation into a AnimationKeyframe.
        if (m_Frames.size() > 0)
        {
            int a, b;
            RtgFrame pRtgFrame;
            RtgFrameObject pRtgFrameObject;
            ArticulatedAnimationLoop pAnimationLoop;
            ArticulatedAnimationKeyframe pAnimationKeyframe;
            ArticulatedAnimationKeyframeObject pAnimationKeyframeObject;
            
            pAnimationLoop = new ArticulatedAnimationLoop();
         

            for (a=0; a<m_Frames.size(); a++)
            {
                pRtgFrame = (RtgFrame)m_Frames.get(a);
                
                pAnimationKeyframe = new ArticulatedAnimationKeyframe();
                
                pAnimationKeyframe.setTime(pRtgFrame.m_Index);
                
                //  Copy over all the FrameObjects.
                for (b=0; b<pRtgFrame.m_FrameObjects.size(); b++)
                {
                    pRtgFrameObject = (RtgFrameObject)pRtgFrame.m_FrameObjects.get(b);

                    pAnimationKeyframeObject = new ArticulatedAnimationKeyframeObject();

                    int boneIndex = m_pArticulatedAnimation.getAnimationBoneIndex(pRtgFrameObject.m_ObjectName);

                    pAnimationKeyframeObject.setBoneName(pRtgFrameObject.m_ObjectName);
                    pAnimationKeyframeObject.setBoneIndex(boneIndex);
                    pAnimationKeyframeObject.setMatrix(pRtgFrameObject.m_Matrix.transpose());

                    pAnimationKeyframe.addAnimationKeyframeObject(pAnimationKeyframeObject);
                }

                pAnimationLoop.addAnimationKeyframe(pAnimationKeyframe);
            }

            pAnimationLoop.setDuration(2.0f);
                    
            m_pArticulatedAnimation.addAnimationLoop(pAnimationLoop);
        }
    }


    public void copyAnimationBone(RtgBone pRtgBone, ArticulatedAnimationBone pAnimationBone)
    {
        pAnimationBone.setLevel(pRtgBone.m_Level);
        pAnimationBone.setName(pRtgBone.m_Name);
        pAnimationBone.setMatrixInfo(pRtgBone.m_MatrixInfo.m_Tran,
                                     pRtgBone.m_MatrixInfo.m_Rot,
                                     pRtgBone.m_MatrixInfo.m_Scal);
        pAnimationBone.setMatrix(pRtgBone.m_Matrix.transpose());
        pAnimationBone.setAnimationGeometry(m_pArticulatedAnimation.getAnimationGeometry(pRtgBone.m_Name));

        if (pRtgBone.m_ChildBones.size() > 0)
        {
            int a;
            RtgBone pChildRtgBone;
            ArticulatedAnimationBone pChildAnimationBone;

            for (a=0; a<pRtgBone.m_ChildBones.size(); a++)
            {
                pChildRtgBone = (RtgBone)pRtgBone.m_ChildBones.get(a);

                pChildAnimationBone = new ArticulatedAnimationBone();
                pChildAnimationBone.setIndex(pChildRtgBone.m_Index);

                m_pArticulatedAnimation.addAnimationBone(pChildAnimationBone);

                copyAnimationBone(pChildRtgBone, pChildAnimationBone);

                pAnimationBone.addChildBone(pChildAnimationBone);
            }
        }
    }
}

                    

  

class RtgMaterial
{
    public int m_Index = 0;
    public String m_Name = "";
    public ColorRGBA m_Ambient = new ColorRGBA();
    public ColorRGBA m_Diffuse = new ColorRGBA();
    public ColorRGBA m_Specular = new ColorRGBA();
    public ColorRGBA m_Emission = new ColorRGBA();
    public float m_fShininess = 0.0f;
    public float m_fTransparency = 0.0f;

    public String m_TextureName = "";
    public String m_TextureFilename = "";   

    public RtgMaterial()
    {
        
    }
}

class RtgPolygon
{
    public int []m_PositionIndices = new int[3];
    public int []m_NormalIndices = new int[3];
    public int []m_TexCoordIndices = new int[3];
}

class RtgSubMesh
{
    public String m_Name = "";

    public int m_MaterialIndex = -1;
    public String m_MaterialName = "";
    
    public ArrayList m_Positions = new ArrayList();
    public ArrayList m_Normals = new ArrayList();
    public ArrayList m_Colors = new ArrayList();
    public ArrayList m_TexCoords = new ArrayList();

    public ArrayList m_Polygons = new ArrayList();


    //  Constructor.
    public RtgSubMesh()
    {
        
    }


    public Vector2f getTexCoord(int Index)
    {
        if (Index < 0 || Index >= m_TexCoords.size())
            return(null);

        return( (Vector2f)m_TexCoords.get(Index));
    }

}


class RtgMatrixInfo
{
    public Vector3f m_Tran = new Vector3f();
    public Vector3f m_Rot = new Vector3f();
    public Vector3f m_Scal = new Vector3f();
    public Vector3f m_sPiv = new Vector3f();
    public Vector3f m_rPiv = new Vector3f();

}

class RtgBone
{
    public int m_Index = 0;
    public int m_Level = 0;
    public String m_Name = "";
    
    public RtgMatrixInfo m_MatrixInfo = new RtgMatrixInfo();

    public Matrix4f m_Matrix = new Matrix4f();

    public ArrayList m_ChildBones = new ArrayList();



    public RtgBone()
    {
        
    }


    public void dump(String spacing)
    {
        System.out.println(spacing + "RtgBone:  " + m_Name);
        
        if (m_ChildBones.size() > 0)
        {
            int a;
            RtgBone pRtgBone;
            
            for (a=0; a<m_ChildBones.size(); a++)
            {
                pRtgBone = (RtgBone)m_ChildBones.get(a);
                
                pRtgBone.dump(spacing + "   ");
            }
        }
    }
}


class RtgFrame
{
    public int m_Index = 0;
    
    public ArrayList m_FrameObjects = new ArrayList();
    
}


class RtgFrameObject
{
    public String m_ObjectName = "";
    
    RtgMatrixInfo m_MatrixInfo = new RtgMatrixInfo();
    
    Matrix4f m_Matrix = new Matrix4f();
    
}


