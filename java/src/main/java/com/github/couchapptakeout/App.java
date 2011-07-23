/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import com.github.couchapptakeout.ui.AuthenticationDialog;
import com.github.couchapptakeout.ui.EmbeddedBrowser;
import com.github.couchapptakeout.ui.LoadingDialog;
import com.github.couchapptakeout.ui.Splash;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventServiceLocator;
import org.bushe.swing.event.EventSubscriber;
import org.bushe.swing.event.ThreadSafeEventService;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbInfo;
import org.ektorp.ReplicationCommand;
import org.ektorp.ReplicationStatus;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

/**
 *
 * @author ryan.ramage
 */
public class App {

    String appName;
    String src_host;
    String src_db;
    int src_port;
    String src_username;
    String src_password;
    String localDbName;
    String local_username;
    String local_password;
    boolean sync = true;
    LocalCouch localCouchManager;
    Splash splash;
    AuthenticationDialog dialog;


    String applicationUrl;
    ImageIcon appIcon;
    ImageIcon splashIcon;
    boolean hadToLoad = false;
    CouchDbInstance couchDbInstance;


    public App(String appName, String src_host, String src_db, int src_port, String src_username) {
        this.appName = appName;
        this.src_host = src_host;
        this.src_db = src_db;
        this.src_port = src_port;
        this.src_username = src_username;
        this.localDbName = createLocalDbName();
        DefaultCouchManager lcm = new DefaultCouchManager();
        BasicCouchDownloader bcd = new BasicCouchDownloader("http://couchdb-binary-releases.googlecode.com/svn/trunk");
        DefaultUnzipper unzipper = new DefaultUnzipper();
        lcm.setCouchDownloader(bcd);
        lcm.setUnzipper(unzipper);
        localCouchManager = lcm;
    }

    public void setLocalCouchManager(LocalCouch localCouchManager) {
        this.localCouchManager = localCouchManager;
    }

    


    protected String createLocalDbName() {
        StringBuilder builder = new StringBuilder();
        builder.append(src_db);
        builder.append("-");
        String cleanHost = src_host.replaceAll("\\.", "_").toLowerCase();
        builder.append(cleanHost);
        if (src_port > 0 && src_port != 80) {
            builder.append("-").append(src_port);
        }
        builder.append("");
        return builder.toString();
    }


    public void start() throws Exception {
        // always listen for the exit application message
        EventBus.subscribeStrongly(ExitApplicationMessage.class, new EventSubscriber<ExitApplicationMessage>() {
           @Override
            public void onEvent(ExitApplicationMessage t) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
        });
        
        try {
            showSplashDialog();

            
            couchDbInstance = localCouchManager.getCouchInstance();
            CouchDbConnector db = localCouchManager.getCouchConnector(localDbName, couchDbInstance);
            DbInfo info = db.getDbInfo();
            
            ready(db);
        } catch(CouchDBNotFoundException nfe) {
            ready(loadNeeded(true));
        } catch (Exception noInfo) {
            ready(loadNeeded(false));
        }
    }


    protected CouchDbConnector loadNeeded( boolean haveToInstallCouch ) throws CouchDbInstallException, CouchDBNotFoundException {
            hadToLoad = true;
            

            // we need to prompt for credentials if there is a username

            if (StringUtils.isNotBlank(src_username)) {
                promptForCredientials(false);
            }
            

            int step = 1;
            int totalSteps = 3;

            if (haveToInstallCouch) {
                step++;
                totalSteps = 4; // one extra step
                EventBus.publish(new LoadingMessage(step++, totalSteps, "Installing DB...", 0, 0, "Starting..." ));
                localCouchManager.installCouchDbEmbedded();

            }

            couchDbInstance = localCouchManager.getCouchInstance();
            CouchDbConnector db = localCouchManager.getCouchConnector(localDbName, couchDbInstance);
            try {
                db.createDatabaseIfNotExists();
            }  catch (org.ektorp.DbAccessException dae) {
                // we need to login to the db
                promptForCredientials(true);
                localCouchManager.setCredentials(local_username, local_password);
                couchDbInstance = localCouchManager.getCouchInstance();
                db = localCouchManager.getCouchConnector(localDbName, couchDbInstance);
                db.createDatabaseIfNotExists();
            }
           
            // init one time replicate
            EventBus.publish(new LoadingMessage(step++, totalSteps, "Downloading Data", 0, 0, "Copy data from " + getSrcReplicationUrl(false) ));
            setupReplication(couchDbInstance, db);



            EventBus.publish(new LoadingMessage(totalSteps, totalSteps, "Downloading Data", 4, 4, "Complete!"));
            
            return db;
    }

    protected void setupReplication(CouchDbInstance instance, CouchDbConnector db) {
            String src_fullurl = getSrcReplicationUrl(true);
            ReplicationCommand firstReplication = createSrcReplication(src_fullurl, db.getDatabaseName());

            try {
                // on large dbs, this times out
                // broken waiting for ektorp to fix!!!
                // We need to catch the error, and poll the couch
                ReplicationStatus status = instance.replicate(firstReplication);

            } catch (Exception socketTimeoutException)  {
                //socketTimeoutException.printStackTrace();
                 System.out.println("REPLICATION HACK");
                waitForReplicationToFinishHack(instance);
                System.out.println("DONE");
               
                
            }
            //should check status.isOk();
    }


    protected void waitForReplicationToFinishHack(CouchDbInstance instance) {

        boolean replicationComplete = false;

        while(!replicationComplete) {
            // need to check the active tasks
            HttpResponse response = instance.getConnection().get("/_active_tasks");
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode results = mapper.readTree(response.getContent());
                replicationComplete = true;
                for (JsonNode element : results) {
                    if ("Replication".equals(element.get("type").getTextValue())) {
                        String task = element.get("task").getTextValue();

                        // this is a BIG assumption for now. We are assuming that we
                        // we are the only one using this db.

                        if (!task.contains("couchapp-takeout-")) {
                            replicationComplete = false; // sorry, still going
                        }
                    }
                }

                Thread.sleep(1000);

            } catch (Exception ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ReplicationCommand createSrcReplication(String src_url, String targetdb) {
        return  new ReplicationCommand.Builder()
                    .source(src_url)
                    .target(targetdb)
                    .filter("takeout/not_takeout")
                    .build();
    }


    protected void startSync(CouchDbConnector localdb, CouchDbInstance instance, String syncType) {

        Logger.getLogger(App.class.getName()).log(Level.INFO, "Sync Type: {0}", syncType);

        if (StringUtils.equalsIgnoreCase(syncType, "none")) return;
        String src_fullurl = getSrcReplicationUrl(true); 

        // create a continous replication
        CouchDbConnector rep_db = localCouchManager.getCouchConnector("_replicator", instance);

        try {
            ObjectMapper mapper = new ObjectMapper();
            if (StringUtils.equalsIgnoreCase(syncType, "bi-directional") || StringUtils.equalsIgnoreCase(syncType, "pull")) {

                ObjectNode pull = mapper.createObjectNode();
                pull.put("_id", "couchapp-takeout-" + localDbName + "-pull");
                pull.put("source", src_fullurl);
                pull.put("target", localDbName);
                pull.put("continuous", true);
                rep_db.create(pull);
            }
            if (StringUtils.equalsIgnoreCase(syncType, "bi-directional") || StringUtils.equalsIgnoreCase(syncType, "push")) {
                // other direction
                ObjectNode push = mapper.createObjectNode();
                push.put("_id", "couchapp-takeout-" + localDbName + "-push");
                push.put("target", src_fullurl);
                push.put("source", localDbName);
                push.put("continuous", true);
                rep_db.create(push);
            }
        } catch(org.ektorp.UpdateConflictException uce) {
            // the entry exists already in the replicator.
        } catch (Exception e) {
            // something else...no replicator db
        }
    }


    protected void ready(CouchDbConnector db) {

       

        // get the main design doc
        JsonNode design = db.get(JsonNode.class, "_design/takeout-settings.jnlp");
        String localStartUrl = "/_design/app/index.html";

        try {
            localStartUrl = design.get("localStartUrl").getTextValue();
            if (! localStartUrl.startsWith("/")) localStartUrl = "/" + localStartUrl;
        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.WARNING, "Could not find localStartUrl", ex);
        }

        applicationUrl = "http://localhost:" + localCouchManager.getCouchPort() + "/" + db.getDatabaseName() + localStartUrl;


        if (appIcon == null) {
            String iconUrl =  "http://localhost:" + localCouchManager.getCouchPort() + "/" + db.getDatabaseName() + "/_design/takeout-settings.jnlp/icon.png";
            Logger.getLogger(App.class.getName()).log(Level.INFO, iconUrl);
            try {
                appIcon = createImage(iconUrl);
            } catch (Exception ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        



        try {


            boolean embedded = isEmbeddedRequested(design);
            if (embedded) {
                showEmbedded(applicationUrl);
            } else {
                // now setup the tray
                List menuItems = createMenu(appName);
                Tray tray = new Tray(appIcon, appName, menuItems);
                showUrl(new URL(applicationUrl));
                if (hadToLoad) {
                    // wait a bit and show a message from the tray.
                    Thread.sleep(2000);
                    EventBus.publish(new TrayMessage(appName, "Load Complete! This icon helps you control the application.", MessageType.INFO));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        hideSplashDialog();

        //lastly, if had to load, setup replication
        if (hadToLoad) {
            String syncType = "bi-directional";
            try {
                syncType = design.get("advanced").get("syncType").getTextValue();

            } catch (Exception ex) {
                Logger.getLogger(App.class.getName()).log(Level.WARNING, "Could not find localStartUrl", ex);
            }

            startSync(db, couchDbInstance, syncType);

            // put a local doc to so the app can query to see if it is running local
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode takeoutLocal = mapper.createObjectNode();
                takeoutLocal.put("_id", "_local/takeout");
                takeoutLocal.put("source", getSrcReplicationUrl(false));
                takeoutLocal.put("syncType", syncType);
                couchDbInstance.getConnection().put("/" + db.getDatabaseName() + "/_local/takeout", takeoutLocal.toString());
            } catch (Exception ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }


        }



    }


    protected boolean isEmbeddedRequested(JsonNode design) {
        boolean embedded = false;
        try {
            embedded = design.get("advanced").get("embedded").getBooleanValue();
        } catch (Exception e) {}
        return embedded;
    }


    protected void showSplashDialog() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (splash == null) {
                    splash = new Splash(appName);
                }
                splash.setVisible(true);
            }
        });
        // wait for it to be visable
        while (splash == null || !splash.isShowing()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void hideSplashDialog() {
        if (splash == null) return;
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        splash.dispose();
    }




    private void promptForCredientials(boolean local) {
        dialog = new AuthenticationDialog(new javax.swing.JFrame(), true);
        if (appIcon != null) dialog.setIconImage(appIcon.getImage());
        dialog.setTitle("Please Enter Remote Password");
        
        dialog.setUsername(src_username);
        dialog.isLocalAuth(local);
        if (local) {
            dialog.setSrcUrl("localhost:" + localCouchManager.getCouchPort());
        } else {
            dialog.setSrcUrl(getSrcReplicationUrl(false));
        }

        dialog.setVisible(true);

        if (dialog.isOk()) {
            if (local) {
                local_username = dialog.getUsername();
                local_password = new String(dialog.getPassword());
            } else {
                src_username = dialog.getUsername();
                src_password = new String(dialog.getPassword());
            }
        }
    }


    public String getSrcReplicationUrl(boolean includeUserDetails) {
        String protocol = "http";

        StringBuilder builder = new StringBuilder(protocol);
        builder.append("://");


        if (includeUserDetails) {
            if (src_username != null && !src_username.equals("")) {
                builder.append(src_username);
                builder.append(":").append(src_password);
                builder.append("@");
            }
        }

        builder.append(src_host);
        int couchPort = src_port;
        if (couchPort <= 0) couchPort = 5984;
        
        builder.append(":").append(couchPort);

        builder.append("/");
        builder.append(src_db);

        return builder.toString();
    }
    protected String getSrcIconUrl() {
      String protocol = "http";

        StringBuilder builder = new StringBuilder(protocol);
        builder.append("://");
        builder.append(src_host);
        int couchPort = src_port;
        if (couchPort <= 0) couchPort = 5984;

        builder.append(":").append(couchPort);

        builder.append("/");
        builder.append(src_db);
        builder.append("/_design/takeout/icon.png");

        return builder.toString();
    }
    public CouchDbConnector getSrcConnector() {
        int couchPort = src_port;
        if (couchPort <= 0) couchPort = 5984;
        StdHttpClient.Builder builder = new StdHttpClient.Builder()
                                    .host(src_host)
                                    .port(couchPort);
        if (src_username != null && src_username != "") {
            builder.username(src_username);
            builder.password(src_password);
        }

        HttpClient httpClient = builder.build();

        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        CouchDbConnector db = new StdCouchDbConnector(src_db, dbInstance);

        return db;
    }



    public static void main(String[] args) {
        try {
            // for the event bus
            System.setProperty(EventServiceLocator.SERVICE_NAME_EVENT_BUS, ThreadSafeEventService.class.getName());
            UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
            String appName = null;
            String host = null;
            String db   = null;
            int port    = -1;
            String username = null;

            
            appName = args[0];
            String[] hostport = parseUsernamePass(args[1]);
            host = hostport[0];
            if (hostport.length == 2) {
                try {
                    port = Integer.parseInt(hostport[1]);
                } catch (Exception e) {}
            }
            

            db = args[2];
            
            if (args.length == 4) {
                String[] up = parseUsernamePass(args[3]);
                username = up[0];
            }
            new App(appName, host, db, port, username).start();

        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            errorAndAbort(ex);
        }
    }
    public static String[] parseUsernamePass(String arg) {
        if (arg == null) return null;
        return arg.split(":");
    }

    private static boolean errorAndAbort(Exception ie) {
        // show the user the error and abort
        //custom title, warning icon
        JOptionPane.showMessageDialog(null,"Sorry, something bad happened\n. Shutting down....buuuu\n. Message: " + ie.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);

        EventBus.publish(new ExitApplicationMessage() );
        return false;
    }


    // menu actions.
    private List createMenu(String appName) {
        List menuItems = new ArrayList();
        menuItems.add(createSiteMenuItem(appName));
        menuItems.add(Tray.MENU_SEPERATOR);
        menuItems.add(createExitMenuItem());

        return menuItems;
    }
    protected MenuItem createSiteMenuItem(String appName) {
        MenuItem item = new MenuItem("Open " + appName);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            showUrl(new URL(applicationUrl));
                        } catch (MalformedURLException ex) {
                            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                });
            }
        });
        return item;
    }

    protected MenuItem createExitMenuItem() {
        MenuItem item = new MenuItem("Exit");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventBus.publish(new ExitApplicationMessage() );
            }
        });
        return item;
    }
    protected void showUrl(URL dest) {


        Desktop desktop = null;
        // Before more Desktop API is used, first check
        // whether the API is supported by this particular
        // virtual machine (VM) on this particular host.
        if (Desktop.isDesktopSupported()) {
            Logger.getLogger(App.class.getName()).log(Level.INFO, "Getting Desktop");
            desktop = Desktop.getDesktop();
            try {
                Logger.getLogger(App.class.getName()).log(Level.INFO, "Browse Command");
                desktop.browse(dest.toURI());
                Logger.getLogger(App.class.getName()).log(Level.INFO, "showURl Complete");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                "Exception: " + ex.getMessage());
            }
        }
    }



    //Obtain the image URL
    protected static ImageIcon createImage(String url) throws MalformedURLException {
        URL imageURL = new URL(url);
        return new ImageIcon(imageURL);
    }

    // used for testing...
    protected AuthenticationDialog getAuthenticationDialog() {
        return dialog;
    }


    private void setIconForDialogs() {
        String url = getSrcIconUrl();
        try {
            appIcon = createImage(url);
        } catch (Exception e) {}
    }

    private void showEmbedded(String applicationUrl) {
        final EmbeddedBrowser browser = new EmbeddedBrowser();
        browser.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int value = JOptionPane.showConfirmDialog(browser, "Are you sure you want to exit?", appName, JOptionPane.YES_NO_OPTION);
                if (value == JOptionPane.YES_OPTION) {
                    browser.dispose();
                    EventBus.publish(new ExitApplicationMessage() );
                }
            }
        });

        browser.setVisible(true);
        if (appIcon != null) browser.setIconImage(appIcon.getImage());
        browser.setTitle(appName);

        browser.setExtendedState(JFrame.MAXIMIZED_BOTH);
        browser.setUrl(applicationUrl);
    }





}
