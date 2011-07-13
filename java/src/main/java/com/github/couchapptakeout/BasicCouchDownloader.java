/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.bushe.swing.event.EventBus;

/**
 *
 * @author ryan.ramage
 */
public class BasicCouchDownloader implements CouchDownloader{


    private String rootUrl;
    private boolean cancel = false;

    

    public BasicCouchDownloader(String rootUrl) {
        this.rootUrl = rootUrl;
        if (!rootUrl.endsWith("/")) {
            this.rootUrl = rootUrl + "/";
        }

    }

    @Override
    public void cancelDownload() {
        cancel = true;
    }

    @Override
    public File downloadLatestCouch(File destDir) {
        try {
            String os = OSUtils.getGenericOSName();
            String version = getLatestVersion();
            URL source = new URL(rootUrl + "couchdb-" + os + "-" + version + ".zip");
            return download(destDir, source);
        } catch (Exception ex) {
            Logger.getLogger(BasicCouchDownloader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public File download(File destDir, URL source) throws InterruptedException {
        File dest = new File(destDir, "couchdb.zip");

        FileDownloader downloader = new FileDownloader(source, dest);
        new Thread(downloader).start();

        while(downloader.getStatus() == FileDownloader.DOWNLOADING ) {
            if (cancel) downloader.cancel();
            EventBus.publish(new LoadingMessage(-1, -1, null, (int)downloader.getProgress(), 100, "Downloading..." ));
            Thread.sleep(1000);
        }
        return dest;
    }


    public String getLatestVersion()  {
        InputStream stream = null;
        try {
            URL latestVersionURL = new URL(rootUrl + "latest.txt");
            stream = latestVersionURL.openStream();
            return IOUtils.toString(stream);
        } catch (IOException ex) {
            Logger.getLogger(BasicCouchDownloader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(BasicCouchDownloader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }




}
