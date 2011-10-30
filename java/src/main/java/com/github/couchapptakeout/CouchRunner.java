/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import com.github.couchapptakeout.events.ExitApplicationMessage;
import com.github.couchapptakeout.events.LoadingMessage;
import java.io.File;
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

    private File workingDir = null;

    public CouchRunner(String couchExe) {
        this.couchExe = couchExe;
        
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }


   @Override
    public void onEvent(ExitApplicationMessage t) {
        running = false;
        Logger.getLogger(CouchRunner.class.getName()).log(Level.INFO, "Killing couch.");
        if (couchProcess != null) {
            couchProcess.destroy();
            couchProcess = null;
        }
        System.out.println("Exit Received in Couch");
    }

    @Override
    public void run() {
         try {
            EventBus.subscribeStrongly(ExitApplicationMessage.class, this);
            Logger.getLogger(CouchRunner.class.getName()).log(Level.INFO, "Starting couch.");
            EventBus.publish(new LoadingMessage(-1, -1, null, 0, 0, "Starting Database" ));

            if (couchProcess != null) {
                couchProcess.destroy();
            }

            ProcessBuilder pb = new ProcessBuilder(new String[]{couchExe});
            pb.redirectErrorStream(true);
            if (workingDir != null) pb.directory(workingDir);
            couchProcess = pb.start();
            running = true;
            couchStream = couchProcess.getInputStream();
            int chr = couchStream.read();
            while (chr != -1 && running) {
                chr = couchStream.read();
            }
            Logger.getLogger(CouchRunner.class.getName()).log(Level.INFO, "Couch Process Disconnected.");
            if (couchProcess != null) couchProcess.destroy();
            couchProcess = null;

            System.out.println("Publish sd from run");
            EventBus.publish(new ShutDownMessage());


        } catch (IOException ex) {
            Logger.getLogger(CouchRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
