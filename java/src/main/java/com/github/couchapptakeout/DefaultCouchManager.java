/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.SystemUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ini4j.Wini;

/**
 *
 * @author ryan.ramage
 */
public class DefaultCouchManager implements LocalCouch{


    private static String COUCH_DIR = "couch";
    private int localCouchPort = 5984;
    private CouchDownloader couchDownloader;
    private Unzipper unzipper;
    private int cachedCouchPort = 5984;


    public void setCouchDownloader(CouchDownloader couchDownloader) {
        this.couchDownloader = couchDownloader;
    }

    public void setUnzipper(Unzipper unzipper) {
        this.unzipper = unzipper;
    }

    public void setLocalCouchPort(int localCouchPort) {
        this.localCouchPort = localCouchPort;
    }

    @Override
    public int getCouchPort() {
        return cachedCouchPort;
    }


    @Override
    public CouchDbInstance getCouchInstance() throws CouchDBNotFoundException {
        // check if we already have a embedded couch setup, if yes startEmbeded
        if (haveEmbeddedCouch()) {
            String exe = getCouchExe();
            CouchRunner runner = new CouchRunner(exe);
            runner.setWorkingDir(new File(getWorkingDir(), COUCH_DIR));

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
        try {
            // download couchdb for os
            File couchZip = couchDownloader.downloadLatestCouch(getWorkingDir());

            File couchDir = new File(getWorkingDir(), COUCH_DIR);

            unzipper.doUnzip(couchZip, couchDir);

            // find a random port
            int port = findFreePort();
            // set random port in ini
            String iniFile = getCouchIniLocation();
            setEmbeddedCouchPort(iniFile, port);
        } catch (IOException ex) {
            Logger.getLogger(DefaultCouchManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public CouchDbConnector getCouchConnector(String name, CouchDbInstance instance) {
        return  new StdCouchDbConnector(name, instance);
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
        File couchdir = new File(workDir, COUCH_DIR);
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
        this.cachedCouchPort = localCouchPort;
        HttpClient httpClient = new StdHttpClient.Builder()
                                    .host("localhost")
                                    .port(localCouchPort)
                                    .build();
        return new StdCouchDbInstance(httpClient);
    }

    private String getCouchExe() {
        File workDir = getWorkingDir();
        File couchdir = new File(workDir, COUCH_DIR);
        if (SystemUtils.IS_OS_WINDOWS) {
            File couchdbBinDir = new File(couchdir, OSUtils.getCouchBinLocation());
            return couchdbBinDir.getAbsolutePath();
        } else {
            return OSUtils.getCouchBinLocation();
        }
    }

    private String getCouchIniLocation() {
        File workDir = getWorkingDir();
        File couchdir = new File(workDir, COUCH_DIR);
        File localIni = new File(couchdir, OSUtils.getCouchIniLocation());
        return localIni.getAbsolutePath();
    }

    public CouchDbInstance getEmbeddedCouchInstance() throws IOException {
        String ini = getCouchIniLocation();
        int port = getEmbeddedCouchPort(ini);
        this.cachedCouchPort = port;
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

    protected void setEmbeddedCouchPort(String localIniFile, int port) throws IOException {
        Wini ini = new Wini(new File(localIniFile));
        ini.put("httpd", "port", port);
        ini.store();
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
