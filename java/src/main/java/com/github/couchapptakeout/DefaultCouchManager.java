/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.SystemUtils;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.ini4j.Wini;

/**
 *
 * @author ryan.ramage
 */
public class DefaultCouchManager implements LocalCouch{

    private int localCouchPort = 5984;

    public void setLocalCouchPort(int localCouchPort) {
        this.localCouchPort = localCouchPort;
    }

    @Override
    public CouchDbInstance getCouchInstance() throws CouchDBNotFoundException {
        // check if we already have a embedded couch setup, if yes startEmbeded
        if (haveEmbeddedCouch()) {
            String exe = getCouchExe();
            CouchRunner runner = new CouchRunner(exe);
            new Thread(runner).start();
            // wait for couch
            return waitForEmbeddedCouch();
        }
        // check for local couch on 5984, use that
        if (isLocalCouchRunning()) {
            return getLocalCouchInstance();
        }
        // no luck
        throw new CouchDBNotFoundException();
                
    }

    @Override
    public void installCouchDbEmbedded() throws CouchDbInstallException {
        // download couchdb for os
        // find a random port
        // set random port in ini
        // start embedded
    }



    protected static File getWorkingDir() {
       String userHome = System.getProperty("user.home");
       File homeDir = new File(userHome, ".couchapptakeout");
       if (!homeDir.exists()) {
           homeDir.mkdirs();
       }
       return homeDir;
    }

    
    public boolean haveEmbeddedCouch() {
        File workDir = getWorkingDir();
        File couchdir = new File(workDir, "couch");
        if (couchdir.exists()) return true;
        return false;
    }








    public static int findFreePort() throws IOException {
      ServerSocket server = new ServerSocket(0);
      int port = server.getLocalPort();
      server.close();
      return port;
    }

    public CouchDbInstance getLocalCouchInstance() {
        HttpClient httpClient = new StdHttpClient.Builder()
                                    .host("localhost")
                                    .port(localCouchPort)
                                    .build();
        return new StdCouchDbInstance(httpClient);
    }

    private String getCouchExe() {
        File workDir = getWorkingDir();
        File couchdir = new File(workDir, "couch");
        File couchdbBinDir = new File(couchdir, "bin");
        if (!couchdbBinDir.exists() || !couchdbBinDir.isDirectory()) return null;
        File location = new File(couchdbBinDir, "couchdb");
        if (SystemUtils.IS_OS_WINDOWS) {
            location = new File(couchdbBinDir, "couchdb.bat");
        }
        return location.getAbsolutePath();
    }

    private String getCouchIniLocation() {
        File workDir = getWorkingDir();
        File couchdir = new File(workDir, "couch");
        File localIni = new File(couchdir, "etc/couchdb/local.ini");
        return localIni.getAbsolutePath();
    }

    public CouchDbInstance getEmbeddedCouchInstance() throws IOException {
        String ini = getCouchIniLocation();
        int port = getEmbeddedCouchPort(ini);
        HttpClient httpClient = new StdHttpClient.Builder()
                                    .host("localhost")
                                    .port(port)
                                    .build();
        return new StdCouchDbInstance(httpClient);
    }


    protected int getEmbeddedCouchPort(String localIniFile) throws IOException {
        Wini ini = new Wini(new File(localIniFile));
        int port = ini.get("httpd", "port", int.class);
        return port;
    }



    private CouchDbInstance waitForEmbeddedCouch() {
        for (int i =0; i < 4; i++) {
            try {
                if (isEmbeddedCouchRunning()) {
                    return getEmbeddedCouchInstance();
                }
                Thread.sleep(2100);
            } catch (Exception ex) {
                Logger.getLogger(DefaultCouchManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public boolean isLocalCouchRunning() {
        CouchDbInstance instance = getLocalCouchInstance();
        return isCouchInstanceUp(instance);

    }

    private boolean isEmbeddedCouchRunning() throws IOException {
        CouchDbInstance instance = getEmbeddedCouchInstance();
        return isCouchInstanceUp(instance);
    }

    private boolean isCouchInstanceUp(CouchDbInstance dbInstance) {
        try {
            List<String> names = dbInstance.getAllDatabases();
            if (names != null && names.size() > 0) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }


}
