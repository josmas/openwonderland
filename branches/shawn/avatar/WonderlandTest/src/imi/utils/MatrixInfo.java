/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.utils;


import com.jme.math.*;




public class MatrixInfo
{
    public Vector3f m_Translation = new Vector3f();
    public Vector3f m_Rotation = new Vector3f();
    public Vector3f m_Scale = new Vector3f();



    //  Constructor.
    public MatrixInfo()
    {
    }



    //  Sets up the MatrixInfo.
    public void set(Vector3f pTranslation,
                    Vector3f pRotation,
                    Vector3f pScale)
    {
        m_Translation.set(pTranslation);
        m_Rotation.set(pRotation);
        m_Scale.set(pScale);
    }

}

