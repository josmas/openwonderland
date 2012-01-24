/**
 * Open Wonderland
 *
 * Copyright (c) 2011 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.voicebridge.echo;

import com.sun.voip.CallParticipant;
import com.sun.voip.MixDataSource;
import com.sun.voip.RtpPacket;
import com.sun.voip.server.ConferenceManager;
import com.sun.voip.server.ConferenceMember;
import com.sun.voip.server.ConferenceMemberListener;
import com.sun.voip.server.MemberSender;
import com.sun.voip.server.MixDescriptor;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create a special conference that echoes data back to the user
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class BridgeEchoInitializer implements ConferenceMemberListener {
    private static final Logger LOGGER =
            Logger.getLogger(BridgeEchoInitializer.class.getName());
    
    private final ConferenceManager echoConf;
    private final Map<String, EchoBuffer> buffers = 
            new HashMap<String, EchoBuffer>();
    
    public BridgeEchoInitializer() {
        // create a permanent conference named "echo"
        echoConf = ConferenceManager.getConference("echo");
        echoConf.setPermanent(true);
        
        try {
            echoConf.setMediaInfo("PCM/16000/2");
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Unable to set media preference", ex);
        }
    
        echoConf.addMemberListener(this);
    }

    public void memberJoined(ConferenceMember cm) {
    }
    
    public void memberInitialized(ConferenceMember cm) {
        LOGGER.log(Level.INFO, "Member {0} joined {1}",
                   new Object[]{cm.getSourceId(), echoConf.getId()});
        
        try {
            EchoBuffer buffer = new EchoBuffer(echoConf);
            
            buffers.put(cm.getSourceId(), buffer);
            
            MixDescriptor md = new MixDescriptor(buffer, 1.0d);
            cm.getMixManager().addMix(md);
        
            // start writing after we start reading, to ensure that the
            // buffer wraps around once to give the appropriate delay
            cm.getMemberReceiver().addForwardMember(buffer);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error adding recevier to member", ioe);
        }
    }

    public void memberLeft(ConferenceMember cm) {
        LOGGER.log(Level.INFO, "Member {0} left {1}",
                   new Object[]{cm.getSourceId(), echoConf.getId()});
        
        EchoBuffer buffer = buffers.remove(cm.getSourceId());
        cm.getMemberReceiver().removeForwardMember(buffer);
    }
    
    private static class EchoBuffer extends MemberSender
        implements MixDataSource
    {
        private final ConferenceManager conference;
        private final IntBuffer buffer;
        
        private int[] currentContrib;
        private int[] previousContrib;

        private int readPointer;
        private int writePointer;
        
        public EchoBuffer(ConferenceManager conference) throws IOException {
            super (new CallParticipant(), null);
            
            this.conference = conference;
            
            int sampleSize = 8;
            //if (conference.getMediaInfo().getPayload() == RtpPacket.PCM_PAYLOAD) {
            //    sampleSize = 16;
            //}
            
            int sampleRate = conference.getMediaInfo().getSampleRate();
            int channels = conference.getMediaInfo().getChannels();
            
            // buffer required for one second of audio
            int oneSecond = (sampleSize * sampleRate * channels) / 8;
            
            int bufferSize = 3 * oneSecond;
            buffer = IntBuffer.allocate(bufferSize);
        
            // advance the read pointer a little ahead of the write pointer
            // so the buffer wraps around once before playing anything back
            // (in order to delay the output)
            float samples = 5f * (RtpPacket.PACKET_PERIOD / 1000f);
            readPointer = (int) (samples * oneSecond);
            
            LOGGER.log(Level.INFO, "Created echo buffer size {0}", bufferSize);
        }
        
        public String getSourceId() {
            return "echo";
        }

        @Override
        public synchronized boolean sendData(int[] dataToSend) {            
            // store data in the buffer
            int pos = 0;
            while (pos < dataToSend.length) {
                buffer.position(writePointer);

                int amount = Math.min(dataToSend.length - pos, buffer.remaining());
                buffer.put(dataToSend, pos, amount);
                
                pos += amount;
                writePointer += amount;
                
                if (writePointer == buffer.capacity()) {
                    writePointer = 0;
                }
            }
            
            return true;
        }
        
        protected int[] getLinearData(float time) {
            int sampleSize = 8;
            //if (conference.getMediaInfo().getPayload() == RtpPacket.PCM_PAYLOAD) {
            //    sampleSize = 16;
            //}
            
            int sampleRate = conference.getMediaInfo().getSampleRate();
            int channels = conference.getMediaInfo().getChannels();
            
            int readSize = (int) (time * ((sampleSize * sampleRate * channels) / 8));
          
            int[] out = new int[readSize];
            int pos = 0;
            
            while (pos < out.length) {
                buffer.position(readPointer);
                
                int amount = Math.min(out.length - pos, buffer.remaining());
                buffer.get(out, pos, amount);
                
                pos += amount;
                readPointer += amount;
                
                if (readPointer == buffer.capacity()) {
                    readPointer = 0;
                }
            }
            
            return out;
        }

        @Override
        public boolean memberIsReadyForSenderData() {
            return true;
        }
        
        public synchronized void saveCurrentContribution() {
            previousContrib = currentContrib;
            currentContrib = getLinearData(RtpPacket.PACKET_PERIOD / 1000f); 
        }

        public int[] getPreviousContribution() {
            return previousContrib;
        }

        public int[] getCurrentContribution() {
            previousContrib = currentContrib;
            currentContrib = getLinearData(RtpPacket.PACKET_PERIOD / 1000f);
            
            return currentContrib;
        }

        public boolean contributionIsInCommonMix() {
            return false;
        }

        @Override
        public String toAbbreviatedString() {
            return "echo";
        }
    }
    
}
