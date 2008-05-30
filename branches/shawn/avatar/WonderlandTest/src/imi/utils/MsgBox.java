/*
 * a message box, this how you use it:
 * 
 *       // Message box test -   several messages
 *       MsgBox error = new MsgBox();
 *       error.addMessage("message1");
 *       error.addMessage("message2");
 *       error.show();
 *       // Message box test -    single quick  message
 *       error = new MsgBox("single line error");
 */

package imi.utils;

import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Lou Hayt
 */
public class MsgBox 
{
    ArrayList<String> messages = new ArrayList<String>();
    
   public MsgBox()
   {

   }
   
   /** 
    *   use this constructor to display a single message
    * @param message the single message to display
    */
   public MsgBox(String message)
   {
        messages.add(message);
        JOptionPane.showMessageDialog(null, message);
   }
   
   public void addMessage(String message)
   {
       if (message != null)
       {
            messages.add(message + "\n");   
       }
   }
   
   public void show()
   {
       String message = "";
       
       for (int i = 0; i < messages.size(); i++)
       {
           message += messages.get(i);
       }
       
       if (message != null)
       {
           JOptionPane.showMessageDialog(null, message);
       }
   }
   
}
