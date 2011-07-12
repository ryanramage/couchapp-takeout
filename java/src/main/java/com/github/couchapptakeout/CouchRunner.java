/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

/**
 *
 * @author ryan.ramage
 */
public class CouchRunner implements Runnable,EventSubscriber<ExitApplicationMessage> {

    protected Process couchProcess;
    private InputStream couchStream;
    private boolean running;
    private String couchExe;

    public CouchRunner(String couchExe) {
        this.couchExe = couchExe;
        
    }

   @Override
    public void onEvent(ExitApplicationMessage t) {
        running = false;
        Logger.getLogger(CouchRunner.class.getName()).log(Level.INFO, "Killing couch.");
        if (couchProcess != null) {
            couchProcess.destroy();
        }
    }

    @Override
    public void run() {
         try {
            EventBus.subscribeStrongly(ExitApplicationMessage.class, this);
            Logger.getLogger(CouchRunner.class.getName()).log(Level.INFO, "Starting couch.");
            if (couchProcess != null) {
                couchProcess.destroy();
            }

            ProcessBuilder pb = new ProcessBuilder(new String[]{couchExe});
            pb.redirectErrorStream(true);
            couchProcess = pb.start();
            running = true;
            couchStream = couchProcess.getInputStream();
            int chr = couchStream.read();
            while (chr != -1 && running) {
                chr = couchStream.read();
            }
            Logger.getLogger(CouchRunner.class.getName()).log(Level.INFO, "Couch Process Disconnected.");
            couchProcess.destroy();
            couchProcess = null;
        } catch (IOException ex) {
            Logger.getLogger(CouchRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
