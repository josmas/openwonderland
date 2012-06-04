/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.errorreport.client;

import com.jme.renderer.jogl.JOGLContextCapabilities;
import com.jme.system.DisplaySystem;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Position;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor;
import org.jdesktop.wonderland.client.jme.LogViewer;
import org.jdesktop.wonderland.client.jme.LogViewer.LogEntry;
import org.jdesktop.wonderland.client.jme.LogViewer.LogViewerButton;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.errorreport.common.ErrorReport;

/**
 * Frame to display error reports
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ErrorReportFrame extends javax.swing.JFrame
    implements LogViewerButton
{
    private static final Logger LOGGER =
            Logger.getLogger(ErrorReportFrame.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/errorreport/client/resources/Bundle");
    
    private static JAXBContext context;
    
    /**
     * Creates new form ErrorReportFrame
     */
    public ErrorReportFrame() {
        initComponents();
        
        errorText.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent ce) {
                String sel = errorText.getSelectedText();
                copyButton.setEnabled(sel != null);
            }
        });
    }
    
    public String getButtonText() {
        return BUNDLE.getString("Error_Report");
    }

    public void activate(List<LogEntry> entries, ActionEvent ae) {
        errorText.setText(generateErrorReport(entries));

        // scroll to the top
        errorText.setCaretPosition(0);

        pack();
        setVisible(true);
        toFront();
    }

    public static String generateErrorReport(List<LogEntry> entries) {
        final StringBuilder out = new StringBuilder();
        out.append("Error report generated ").
                append(DateFormat.getDateTimeInstance().format(new Date())).
                append("\n");
        out.append("\n");

        // java version, etc
        out.append("-------- System Information --------\n");
        out.append("Java version: ").append(System.getProperty("java.version")).append("\n");
        out.append("Java vendor:").append(System.getProperty("java.vendor")).append("\n");
        out.append("OS:").append(System.getProperty("os.name")).append("\n");
        out.append("OS version: ").append(System.getProperty("os.version")).append("\n");
        out.append("OS architecture: ").append(System.getProperty("os.arch")).append("\n");
        out.append("\n");
        out.append("Max memory: ").append(Runtime.getRuntime().maxMemory()).append("\n");
        out.append("Total memory: ").append(Runtime.getRuntime().totalMemory()).append("\n");
        out.append("Free memory: ").append(Runtime.getRuntime().freeMemory()).append("\n");
        out.append("\n");

        // graphics
        out.append("-------- Graphics Information --------\n");

        // grab display information from the renderer
        final Semaphore gs = new Semaphore(0);        
        SceneWorker.addWorker(new WorkProcessor.WorkCommit() {
            public void commit() {
                try {
                    DisplaySystem ds = DisplaySystem.getDisplaySystem("JOGL");
                    out.append("Display adapter:  ").append(ds.getAdapter()).append("\n");
                    out.append("Display vendor:   ").append(ds.getDisplayVendor()).append("\n");
                    out.append("Driver version:   ").append(ds.getDriverVersion()).append("\n");
                    out.append("Display renderer: ").append(ds.getDisplayRenderer()).append("\n");
                    out.append("API Version:      ").append(ds.getDisplayAPIVersion()).append("\n");
                    out.append("\n\n");
                } finally {
                    gs.release();
                } 
            }
        });
        
        // wait for the renderer to run the worker
        try {
            if (!gs.tryAcquire(2, TimeUnit.SECONDS)) {
                out.append("Graphics information unavailable\n");
            }
        } catch (InterruptedException ie) {
            // ignore
            
        }
        
        RenderManager rm = WorldManager.getDefaultWorldManager().getRenderManager();
        JOGLContextCapabilities cap = rm.getContextCaps();
        out.append("GL_ARB_fragment_program...").append(cap.GL_ARB_fragment_program).append("\n");
        out.append("GL_ARB_fragment_shader...").append(cap.GL_ARB_fragment_shader).append("\n");
        out.append("GL_ARB_shader_objects...").append(cap.GL_ARB_shader_objects).append("\n");
        out.append("GL_ARB_texture_non_power_of_two...").append(cap.GL_ARB_texture_non_power_of_two).append("\n");
        out.append("GL_ARB_vertex_buffer_object...").append(cap.GL_ARB_vertex_buffer_object).append("\n");
        out.append("GL_ARB_vertex_program...").append(cap.GL_ARB_vertex_program).append("\n");
        out.append("GL_ARB_vertex_shader...").append(cap.GL_ARB_vertex_shader).append("\n");
        out.append("GL_ARB_imaging...").append(cap.GL_ARB_imaging).append("\n");
        out.append("GL_EXT_blend_color...").append(cap.GL_EXT_blend_color).append("\n");
        out.append("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB...").append(cap.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB).append("\n");
        out.append("GL_MAX_FRAGMENT_UNIFORM_COMPONENTS_ARB...").append(cap.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS_ARB).append("\n");
        out.append("GL_MAX_TEXTURE_COORDS_ARB...").append(cap.GL_MAX_TEXTURE_COORDS_ARB).append("\n");
        out.append("GL_MAX_TEXTURE_IMAGE_UNITS_ARB...").append(cap.GL_MAX_TEXTURE_IMAGE_UNITS_ARB).append("\n");
        out.append("GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT...").append(cap.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT).append("\n");
        out.append("GL_MAX_TEXTURE_UNITS...").append(cap.GL_MAX_TEXTURE_UNITS).append("\n");
        out.append("GL_MAX_VARYING_FLOATS_ARB...").append(cap.GL_MAX_VARYING_FLOATS_ARB).append("\n");
        out.append("GL_MAX_VERTEX_ATTRIBS_ARB...").append(cap.GL_MAX_VERTEX_ATTRIBS_ARB).append("\n");
        out.append("GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB...").append(cap.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB).append("\n");
        out.append("GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB...").append(cap.GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB).append("\n");
        out.append("GL_SGIS_generate_mipmap...").append(cap.GL_SGIS_generate_mipmap).append("\n");
        out.append("GL_SHADING_LANGUAGE_VERSION_ARB...").append(cap.GL_SHADING_LANGUAGE_VERSION_ARB).append("\n");
        out.append("GL_VERSION_1_2...").append(cap.GL_VERSION_1_2).append("\n");
        out.append("GL_VERSION_2_0...").append(cap.GL_VERSION_2_0).append("\n");
        out.append("GL_VERSION_2_1...").append(cap.GL_VERSION_2_1).append("\n");
        out.append("GL_VERSION_3_0...").append(cap.GL_VERSION_3_0).append("\n");
        out.append("\n");

        // error log
        out.append("-------- Error Log --------\n");
        synchronized (entries) {
            for (LogViewer.LogEntry entry : entries) {
                LogViewer.format(entry.getRecord(), out);
            }
        }
        out.append("\n");

        // thread dump
        out.append("-------- Threads --------\n");
        for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
            out.append(e.getKey().getName()).append(" ").append(e.getKey().getState()).append("\n");
            for (StackTraceElement ste : e.getValue()) {
                out.append("    ").append(ste.getClassName());
                out.append(".").append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    out.append("(native)");
                } else {
                    out.append("(").append(ste.getFileName()).append(":");
                    out.append(ste.getLineNumber()).append(")");
                }
                out.append("\n");

            }
            out.append("\n");

        }
        out.append("\n");

        return out.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        submitFrame = new javax.swing.JFrame();
        jLabel1 = new javax.swing.JLabel();
        submitUserTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        submitCommentsTF = new javax.swing.JTextArea();
        submitSubmitButton = new javax.swing.JButton();
        submitCancelButton = new javax.swing.JButton();
        errorScrollPane = new javax.swing.JScrollPane();
        errorText = new javax.swing.JTextArea();
        closeButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        submitButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/errorreport/client/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("ErrorReportFrame.jLabel1.text")); // NOI18N

        submitUserTF.setText(bundle.getString("ErrorReportFrame.submitUserTF.text")); // NOI18N

        jLabel2.setText(bundle.getString("ErrorReportFrame.jLabel2.text")); // NOI18N

        submitCommentsTF.setColumns(20);
        submitCommentsTF.setRows(5);
        jScrollPane1.setViewportView(submitCommentsTF);

        submitSubmitButton.setText(bundle.getString("ErrorReportFrame.submitSubmitButton.text")); // NOI18N
        submitSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitSubmitButtonActionPerformed(evt);
            }
        });

        submitCancelButton.setText(bundle.getString("ErrorReportFrame.submitCancelButton.text")); // NOI18N
        submitCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout submitFrameLayout = new javax.swing.GroupLayout(submitFrame.getContentPane());
        submitFrame.getContentPane().setLayout(submitFrameLayout);
        submitFrameLayout.setHorizontalGroup(
            submitFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(submitFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(submitFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .addComponent(submitUserTF)
                    .addGroup(submitFrameLayout.createSequentialGroup()
                        .addGroup(submitFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, submitFrameLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(submitCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(submitSubmitButton)))
                .addContainerGap())
        );
        submitFrameLayout.setVerticalGroup(
            submitFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(submitFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitUserTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(submitFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(submitSubmitButton)
                    .addComponent(submitCancelButton))
                .addContainerGap())
        );

        setTitle(bundle.getString("ErrorReportFrame.title")); // NOI18N

        errorScrollPane.setPreferredSize(new java.awt.Dimension(600, 600));

        errorText.setColumns(20);
        errorText.setEditable(false);
        errorText.setRows(5);
        errorText.setWrapStyleWord(true);
        errorScrollPane.setViewportView(errorText);

        closeButton.setText(bundle.getString("ErrorReportFrame.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        copyButton.setText(bundle.getString("ErrorReportFrame.copyButton.text")); // NOI18N
        copyButton.setEnabled(false);
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });

        selectButton.setText(bundle.getString("ErrorReportFrame.selectButton.text")); // NOI18N
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        submitButton.setText(bundle.getString("ErrorReportFrame.submitButton.text")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(154, Short.MAX_VALUE)
                .addComponent(submitButton)
                .addGap(18, 18, 18)
                .addComponent(selectButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(copyButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton))
            .addComponent(errorScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(errorScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(copyButton)
                    .addComponent(selectButton)
                    .addComponent(submitButton)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
        StringSelection sel = new StringSelection(errorText.getSelectedText());
        getToolkit().getSystemClipboard().setContents(sel, sel);

        // clear the selection
        int end = errorText.getCaretPosition();
        errorText.getCaret().setDot(end - 1);
    }//GEN-LAST:event_copyButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        Position end = errorText.getDocument().getEndPosition();

        errorText.getCaret().setDot(0);
        errorText.getCaret().moveDot(end.getOffset() - 1);
        errorText.getCaret().setSelectionVisible(true);
    }//GEN-LAST:event_selectButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        String username = LoginManager.getPrimary().getUsername();
        submitUserTF.setText(username);
        submitCommentsTF.setText("");
        
        submitFrame.pack();
        submitFrame.setVisible(true);
        submitFrame.setLocationRelativeTo(this);
        submitFrame.toFront();
    }//GEN-LAST:event_submitButtonActionPerformed

    private void submitSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitSubmitButtonActionPerformed
        submitFrame.setVisible(false);
        
        String username = submitUserTF.getText();
        String comments = submitCommentsTF.getText();
        String log = errorText.getText();
        
        ErrorReport report = new ErrorReport(username, new Date(), log, comments);
        
        try {
            submit(report);
        } catch (ContentRepositoryException ce) {
            LOGGER.log(Level.WARNING, "Error submitting report", ce);
        } catch (JAXBException je) {
            LOGGER.log(Level.WARNING, "Error submitting report", je);
        }
    }//GEN-LAST:event_submitSubmitButtonActionPerformed

    private void submitCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitCancelButtonActionPerformed
        submitFrame.setVisible(false);
    }//GEN-LAST:event_submitCancelButtonActionPerformed

    public static ErrorReport read(String id) 
            throws ContentRepositoryException, JAXBException
    {
        ContentCollection dir = getContentDir();
        ContentResource resource = (ContentResource) dir.getChild(id);
        if (resource == null) {
            throw new ContentRepositoryException("No such file: " + id);
        }
        
        Unmarshaller u = getContext().createUnmarshaller();
        return (ErrorReport) u.unmarshal(resource.getInputStream());
    }
    
    public static void submit(ErrorReport report) 
            throws ContentRepositoryException, JAXBException
    {
        ServerSessionManager ssm = LoginManager.getPrimary();    
        ContentCollection dir = getContentDir();
        
        ContentResource file;
        if (report.getId() == null) {
            // create a new file
            String fileName;        
            int index = 0;
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            do {
                fileName = ssm.getUsername() + "-" + df.format(new Date());
                fileName += (index == 0)?"":index;
            
                index++;
            } while (dir.getChild(fileName) != null);
        
            file = (ContentResource) 
                dir.createChild(fileName, ContentNode.Type.RESOURCE);
        } else {
            // update an existing file
            file = (ContentResource) dir.getChild(report.getId());
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Marshaller m = getContext().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.marshal(report, baos);
    
        file.put(baos.toByteArray());
    }
    
    private static ContentCollection getContentDir() throws ContentRepositoryException {
        ServerSessionManager ssm = LoginManager.getPrimary();
        ContentRepository repo = ContentRepositoryRegistry.getInstance().getRepository(ssm);
    
        ContentCollection dir = (ContentCollection) 
                repo.getRoot().getChild("groups/users/" + ErrorReport.DIR_NAME);
        if (dir == null) {
            throw new ContentRepositoryException("No such directory");
        }
        
        return dir;
    }
    
    private synchronized static JAXBContext getContext() 
            throws JAXBException
    {
        if (context == null) {
            context = JAXBContext.newInstance(ErrorReport.class);
        }
        
        return context;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ErrorReportFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ErrorReportFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ErrorReportFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ErrorReportFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ErrorReportFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton copyButton;
    private javax.swing.JScrollPane errorScrollPane;
    private javax.swing.JTextArea errorText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton selectButton;
    private javax.swing.JButton submitButton;
    private javax.swing.JButton submitCancelButton;
    private javax.swing.JTextArea submitCommentsTF;
    private javax.swing.JFrame submitFrame;
    private javax.swing.JButton submitSubmitButton;
    private javax.swing.JTextField submitUserTF;
    // End of variables declaration//GEN-END:variables

}
