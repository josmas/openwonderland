/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scenegraph;

import java.util.ArrayList;




/**
 *
 * @author Chris Nagle
 */
public class Group extends Node
{
    //  Contains the child nodes.
    private ArrayList       m_ChildNodes = null;
    
    
    
    //  Constructor.
    public Group()
    {
    }
    
    public Group(String name)
    {
        setName(name);
    }

    
    
    //  **********************************
    //  Child Node management methods.
    //  **********************************

    //  Adds a ChildNode.
    public void addChild(Node childNode)
    {
        m_ChildNodes.add(childNode);
    }
    
    //  Gets the ChildNode at the specified index.
    public Node getChild(int Index)
    {
        if (Index < 0 || Index >= m_ChildNodes.size())
            return(null);
        
        return( (Node)m_ChildNodes.get(Index));
    }
    
    //  Gets the number of ChildNodes.
    public int getChildCount()
    {
        return(m_ChildNodes.size());
    }

}




