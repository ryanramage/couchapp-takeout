/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import com.github.couchapptakeout.ui.AuthenticationDialog;
import com.github.couchapptakeout.ui.LoadingDialog;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.commons.lang.StringUtils;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventServiceLocator;
import org.bushe.swing.event.EventSubscriber;
import org.bushe.swing.event.ThreadSafeEventService;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbInfo;
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

    String src_host;
    String src_db;
    int src_port;
    String src_username;
    String src_password;
    String localDbName;
    boolean sync = false;
    LocalCouch localCouchManager;
    AuthenticationDialog dialog;
    LoadingDialog loadingDialog;

    String applicationUrl;


    public App(String src_host, String src_db, int src_port, String src_username) {
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
        builder.append("(");
        String cleanHost = src_host.replaceAll("\\.", "_").toLowerCase();
        builder.append(cleanHost);
        if (src_port > 0 && src_port != 80) {
            builder.append("-").append(src_port);
        }
        builder.append(")");
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
            CouchDbInstance instance = localCouchManager.getCouchInstance();
            CouchDbConnector db = localCouchManager.getCouchConnector(localDbName, instance);
            DbInfo info = db.getDbInfo();
            ready(db);
        } catch(CouchDBNotFoundException nfe) {
            ready(loadNeeded(true));
        } catch (Exception noInfo) {
            ready(loadNeeded(false));
        }
    }


    protected CouchDbConnector loadNeeded( boolean haveToInstallCouch ) throws CouchDbInstallException, CouchDBNotFoundException {
            // we need to prompt for credentials if there is a username
            if (StringUtils.isNotBlank(src_username)) {
                promptForCredientials();
            }
            showLoadingDialog();

            int step = 1;
            int totalSteps = 3;

            if (haveToInstallCouch) {
                totalSteps = 4; // one extra step
                EventBus.publish(new LoadingMessage(step++, totalSteps, "Installing DB...", 0, 0, "Starting..." ));
                localCouchManager.installCouchDbEmbedded();

            }

            CouchDbInstance instance = localCouchManager.getCouchInstance();
            CouchDbConnector db = localCouchManager.getCouchConnector(localDbName, instance);
            db.createDatabaseIfNotExists();

           
            // replicate
            EventBus.publish(new LoadingMessage(step, totalSteps, "Downloading Data", 0, 0, "Copy data from " + getSrcReplicationUrl(false) ));            
            String src_fullurl = getSrcReplicationUrl(true);
            ReplicationStatus status = db.replicateFrom(src_fullurl);
            //should check status.isOk();

            if (sync) {
                // create a continous replication
                CouchDbConnector rep_db = localCouchManager.getCouchConnector("_replicator", instance);
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode rep_info = mapper.createObjectNode();
                rep_info.put("_id", "couchapp-takout-" + localDbName);
                rep_info.put("source", src_fullurl);
                rep_info.put("target", localDbName);
                rep_info.put("continuous", true);
                rep_db.create(rep_info);
            }

            EventBus.publish(new LoadingMessage(totalSteps, totalSteps, "Downloading Data", 4, 4, "Complete!"));
            hideLoadingDialog();
            return db;
    }

    protected void ready(CouchDbConnector db) {

        applicationUrl = "http://localhost:" + localCouchManager.getCouchPort() + "/" + db.getDatabaseName() + "/_design/app/index.html";

        // now setup the tray
        List menuItems = createMenu();
        Tray tray = new Tray("/plate.png", "App on ", menuItems);
        
        try {
            showUrl(new URL(applicationUrl));
        } catch (MalformedURLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    protected void showLoadingDialog() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (loadingDialog == null) {
                    loadingDialog = new LoadingDialog(new javax.swing.JFrame(), true);
                }
                loadingDialog.setVisible(true);
            }
        });
        // wait for it to be visable
        while (loadingDialog == null || !loadingDialog.isShowing()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }




    }

    private void hideLoadingDialog() {
        
        // sleep a bit so user sees the final state.
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        loadingDialog.dispose();
    }


    private void promptForCredientials() {
        dialog = new AuthenticationDialog(new javax.swing.JFrame(), true);
        dialog.setSrcUrl(getSrcReplicationUrl(false));
        dialog.setUsername(src_username);

        dialog.setVisible(true);
        if (dialog.isOk()) {
            src_username = dialog.getUsername();
            src_password = new String(dialog.getPassword());
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

            String host = null;
            String db   = null;
            int port    = -1;
            String username = null;

            if (args.length >= 1) {
                String[] hostport = parseUsernamePass(args[0]);
                host = hostport[0];
                if (hostport.length == 2) {
                    try {
                        port = Integer.parseInt(hostport[1]);
                    } catch (Exception e) {}
                }
            }
            if (args.length >= 2) {
                db = args[1];
            }
            if (args.length == 3) {
                String[] up = parseUsernamePass(args[2]);
                username = up[0];
            }
            new App(host, db, port, username).start();

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
        return false;
    }


    // menu actions.
    private List createMenu() {
        List menuItems = new ArrayList();
        menuItems.add(createSiteMenuItem());
        menuItems.add(Tray.MENU_SEPERATOR);
        menuItems.add(createExitMenuItem());

        return menuItems;
    }
    protected MenuItem createSiteMenuItem() {
        MenuItem item = new MenuItem("Open Application");
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

    // used for testing...
    protected AuthenticationDialog getAuthenticationDialog() {
        return dialog;
    }

    protected LoadingDialog getLoadingDialog() {
        return loadingDialog;
    }



}
