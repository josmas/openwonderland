/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.datamgr;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Track the IO and notify the listeners of progress
 *
 * @author paulby
 */
public class TrackingInputStream extends FilterInputStream {
    
    private int byteCount = 0;
    private InputStream steam;
    private ArrayList<ProgressListener> listeners = null;
    private int totalSize;
    private int notifySize;
    private int nextNotify;
    
    /**
     * Notify the user every time the number of byte downloaded is a multiple
     * of notifySize.
     */
    public TrackingInputStream(InputStream stream) {
        super(stream);
    }
    
    public int read() throws IOException {
        int ret = in.read();
        if (ret==-1) {
            byteCount=totalSize;
        } else
            byteCount++;
        
        if (byteCount>nextNotify || byteCount==totalSize)
            notifyListeners();
        return ret;
    }
    
    public int read(byte[] b) throws IOException {
        int ret = in.read(b);
        if (ret==-1) {
            byteCount=totalSize;
        } else
            byteCount+=ret;
        
        if (byteCount>nextNotify || byteCount==totalSize)
            notifyListeners();
        return ret;
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = in.read(b, off, len);
        if (ret==-1) {
            byteCount=totalSize;
        } else
            byteCount+=ret;
        
        if (byteCount>nextNotify || byteCount==totalSize)
            notifyListeners();
        
        return ret;
    }
    
    private void notifyListeners() {
        int percentage;
                
        if (listeners!=null) {
            if (byteCount==totalSize) {
                percentage=100;
            } else
                percentage = (int)(byteCount/(float)totalSize * 100f);
        
            if (listeners!=null)
                for(ProgressListener listener : listeners) {
                    if (listener!=null)
                        listener.downloadProgress(byteCount, percentage);
                }
            
            while(nextNotify<byteCount)
                nextNotify += notifySize;
            
            if (percentage==100 && listeners!=null)
                listeners.clear();      // Make sure we dont send two 100 perc. This would happen if we read all the bytes and then get a close
        }
        
    }
    
    public void close() throws IOException {
        byteCount=totalSize;
        notifyListeners();
        super.close();
        if (listeners!=null)
            listeners.clear();
    }
    
    public void setListener(ProgressListener listener, int notifySize, int totalSize) {
        if (listener==null)
            return;
        this.totalSize = totalSize;
        this.notifySize = notifySize;
        while(nextNotify<byteCount)
            nextNotify += notifySize;
        if (listeners==null)
            listeners = new ArrayList();
        listeners.add(listener);
    }
    
    public boolean removeListener(ProgressListener listener) {
        return listeners.remove(listener);
    }
    
    public interface ProgressListener {
        public void downloadProgress(int readBytes, int percentage);
    }
}
