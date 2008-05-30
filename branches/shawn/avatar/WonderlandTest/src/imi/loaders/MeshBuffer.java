package imi.loaders;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.util.ArrayList;

/**     MeshBuffer
 *  Utility to re-arange mesh data from: 
 *  multiple index buffers (one for positions, one for texture coordinates... etc)
 *  - suitable for editing in Digital Conetent Creation tools 
 *  
 * to: 
 *  a single index buffer, this is how :
 * 
 *    int index1 = addPosition(pos1);
 *    addNormal(norm1);
 *    addTexCoord(multiTextureIndex, uv1);
 * 
 *    int index2 = addPosition(pos2);
 *    addNormal(norm2);
 *    addTexCoord(multiTextureIndex, uv2);
 * 
 *    int index3 = addPosition(pos3);
 *    addNormal(norm3);
 *    addTexCoord(multiTextureIndex, uv3);
 * 
 *    addTriangle(index1, index2, index3);
 * 
 *    ... repeat to add more triangles
 * 
 *    then to use with a TriMesh:
 * 
 *       Vector3f[] vertices  = mb.getPositions();
 *       Vector3f[] normals   = mb.getNormals();
 *       Vector2f[] texCoords = mb.getTexCoords();
 *       int[] indecies = mb.getIndices();
 *
 *       triMesh1.reconstruct(BufferUtils.createFloatBuffer(vertices), BufferUtils.createFloatBuffer(normals),
 *                            null, BufferUtils.createFloatBuffer(texCoords), BufferUtils.createIntBuffer(indecies));
 * 
 *   This will arrange the data using a sindle index array 
 *  - suitable for fast rendering in a game run-time engine.
 * 
 * @author Lou Hayt
 * @author Chris Nagle
 */
public class MeshBuffer
{
    static final int MAX_TEXTURES = 8;
    
    ArrayList m_Positions = new ArrayList();
    ArrayList m_Normals   = new ArrayList();
    ArrayList [] m_TexCoords = new ArrayList[MAX_TEXTURES];    // multi-texturing supported
    ArrayList m_TriangleIndices = new ArrayList();

    public MeshBuffer()
    {
        for (int a=0; a<MAX_TEXTURES; a++)
        {
            m_TexCoords[a] = new ArrayList();
        }
    }

    public int addPosition(Vector3f position)
    {
        m_Positions.add(position);

        return (m_Positions.size() - 1);
    }
    public int addPosition(float x, float y, float z)
    {
        m_Positions.add(new Vector3f(x, y, z));

        return (m_Positions.size() - 1);
    }

    public void addNormal(Vector3f normal)
    {
        m_Normals.add(normal);
    }
    public void addNormal(float x, float y, float z)
    {
        m_Normals.add(new Vector3f(x, y, z));
    }

    public void addTexCoord(int index, Vector2f texCoord)
    {
        m_TexCoords[index].add(texCoord);
    }
    public void addTexCoord(int index, float u, float v)
    {
        m_TexCoords[index].add(new Vector2f(u, v));
    }

    public void addTriangle(int Indice1, int Indice2, int Indice3)
    {
        m_TriangleIndices.add(new Integer(Indice1));
        m_TriangleIndices.add(new Integer(Indice2));
        m_TriangleIndices.add(new Integer(Indice3));
    }

    public Vector3f[] getPositions()
    {
//        Vector3f []arrayOfPositions = (Vector3f[])m_Positions.toArray();
//        return(arrayOfPositions);

        Vector3f[] arrayOfPositions = new Vector3f[m_Positions.size()];

        for (int a = 0; a < m_Positions.size(); a++) {
            arrayOfPositions[a] = (Vector3f) m_Positions.get(a);
        }

        return (arrayOfPositions);
    }

    public Vector3f[] getNormals()
    {
        Vector3f[] arrayOfNormals = new Vector3f[m_Normals.size()];

        for (int a = 0; a < m_Normals.size(); a++) {
            arrayOfNormals[a] = (Vector3f) m_Normals.get(a);
        }

        return (arrayOfNormals);
    }

    public Vector2f[] getTexCoords()
    {
        return getTexCoords(0);
    }
    
    public Vector2f[] getTexCoords(int multiTextureIndex)
    {
        if (multiTextureIndex < 0 || multiTextureIndex > MAX_TEXTURES)
            return null;
        
        Vector2f[] arrayOfTexCoords = new Vector2f[m_TexCoords[multiTextureIndex].size()];

        for (int i = 0; i < m_TexCoords[multiTextureIndex].size(); i++) 
        {
            arrayOfTexCoords[i] = (Vector2f) m_TexCoords[multiTextureIndex].get(i);
        }

        return (arrayOfTexCoords);
    }

    public int[] getIndices()
    {
        int[] arrayOfIndices = new int[m_TriangleIndices.size()];

        for (int a = 0; a < m_TriangleIndices.size(); a++) {
            arrayOfIndices[a] = ((Integer) m_TriangleIndices.get(a));
        }

        return (arrayOfIndices);
    }
}
