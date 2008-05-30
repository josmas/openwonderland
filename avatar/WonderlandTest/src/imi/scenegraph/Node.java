/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scenegraph;




/**
 *
 * @author Chris Nagle
 */
public class Node
{
    private String m_Name = "";
    
    
    
    //  Constructor.
    public Node()
    {
        m_Name = "";
    }
    
    public Node(String name)
    {
        m_Name = name;
    }

    
    //  Gets the name of the Node.
    String getName()
    {
        return(m_Name);
    }
    
    public void setName(String name)
    {
        m_Name = name;
    }



    public String toString()
    {
        return("Node:  " + m_Name);
    }

    public void Dump()
    {
        Dump("");
    }
    
    public void Dump(String spacing)
    {
        System.out.println(toString());
        
        if (this instanceof Group)
        {
            Group thisGroup = (Group)this;
            int a;
            Node childNode;
            
            for (a=0; a<thisGroup.getChildCount(); a++)
            {
                childNode = thisGroup.getChild(a);
                
                childNode.Dump(spacing + "   ");
            }
        }
    }
}




