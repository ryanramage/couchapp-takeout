/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import com.github.couchapptakeout.events.ExitApplicationMessage;
import com.github.couchapptakeout.events.utils.Unzipper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.SystemUtils;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
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
    private String username = null;
    private String password = null;
    private boolean couchStarted = false;


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
    public synchronized CouchDbInstance getCouchInstance() throws CouchDBNotFoundException {
        // check if we already have a embedded couch setup, if yes startEmbeded
        if (haveEmbeddedCouch()) {

            if (isLocalCouchRunning()) {
                setupShutdownHook();
                return getLocalCouchInstance();
            }

            if (!couchStarted) {
                String exe = getCouchExe();
                CouchRunner runner = new CouchRunner(exe);
                if (!SystemUtils.IS_OS_WINDOWS) {
                    runner.setWorkingDir(new File(getWorkingDir(), COUCH_DIR));
                }
                new Thread(runner).start();
                couchStarted = true;
                setupShutdownHookForLocal();
            }
            // wait for couch
            return waitForEmbeddedCouch();
        }
        // check for local couch on 5984, use that
        if (isLocalCouchRunning()) {
            setupShutdownHook();
            return getLocalCouchInstance();
        }
        // no luck
        throw new CouchDBNotFoundException();
                
    }

    protected void setupShutdownHookForLocal() {
       // create a annon listener
        EventBus.subscribeStrongly(ExitApplicationMessage.class, new EventSubscriber<ExitApplicationMessage>() {
            @Override
            public void onEvent(ExitApplicationMessage t) {
                System.out.println("Exiting Local DB");
                // wait for the local db to shutdown
                for (int i=0; i < 4; i++) {
                    try {
                        Thread.sleep(1000);
                        if (!isLocalCouchRunning()) {
                            System.out.println("Local couch shutdown successfully");
                            break;
                        } else {
                            System.out.println("Waiting for local couch to shutdown");
                        }
                    } catch (Exception e) {}

                }
                EventBus.publish(new ShutDownMessage());
            }
        });
    }

    
    protected void setupShutdownHook() {
        // create a annon listener
        EventBus.subscribeStrongly(ExitApplicationMessage.class, new EventSubscriber<ExitApplicationMessage>() {
            @Override
            public void onEvent(ExitApplicationMessage t) {
                System.out.println("Exiting Local DB");
                EventBus.publish(new ShutDownMessage());
            }
        });
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
            throw new CouchDbInstallException(ex.getMessage());
        }
    }

    @Override
    public CouchDbConnector getCouchConnector(String name, CouchDbInstance instance) {
        return  new StdCouchDbConnector(name, instance);
    }

    public static File getWorkingDir() {
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
         StdHttpClient.Builder builder =  new StdHttpClient.Builder()
                                    .host("localhost")
                                    .port(localCouchPort);
         if (username != null) {
             builder.username(username);
             builder.password(password);
         }
         

         HttpClient httpClient = builder.build();
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
        StdHttpClient.Builder builder = new StdHttpClient.Builder()
                                    .host("localhost")
                                    .port(port);
        if (username != null) {
            builder.username(username);
            builder.password(password);
        }
        HttpClient httpClient  = builder.build();
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
        // side effects!!! need to ensure bind address is 127.0.0.1, add lucene
        ini.put("httpd", "bind_address", "127.0.0.1");
        ini.put("httpd_global_handlers", "_fti", "{couch_httpd_proxy, handle_proxy_req, <<\"http://127.0.0.1:5985\">>}");



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

    @Override
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }


}
