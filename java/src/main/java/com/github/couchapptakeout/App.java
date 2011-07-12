/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.bushe.swing.event.EventServiceLocator;
import org.bushe.swing.event.ThreadSafeEventService;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbInfo;
import org.ektorp.ReplicationStatus;
import org.ektorp.http.HttpClient;
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
    boolean sync = true;
    LocalCouch localCouchManager;


    public App(String src_host, String src_db, int src_port, String src_username, String src_password) {
        this.src_host = src_host;
        this.src_db = src_db;
        this.src_port = src_port;
        this.src_username = src_username;
        this.src_password = src_password;
        this.localDbName = createLocalDbName();
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


    public void start() {



        CouchDbInstance instance = null;
        try {
            instance = localCouchManager.getCouchInstance();
        } catch(CouchDBNotFoundException nfe) {
            try {
                localCouchManager.installCouchDbEmbedded();
                instance = localCouchManager.getCouchInstance();
            } catch (CouchDbInstallException ie) {

            } catch (CouchDBNotFoundException nfe2) {
                
            }
        }
        CouchDbConnector db = new StdCouchDbConnector(localDbName, instance);
        DbInfo info = db.getDbInfo();
        if (info == null) {
            db.createDatabaseIfNotExists();
            // replicate
            String src_fullurl = getSrcReplicationName();
            ReplicationStatus status = db.replicateFrom(src_fullurl);
            status.isOk();

            if (sync) {
                // create a continous replication
                CouchDbConnector rep_db = new StdCouchDbConnector("_replicator", instance);
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode rep_info = mapper.createObjectNode();
                rep_info.put("_id", "couchapp-takout");
                rep_info.put("source", src_fullurl);
                rep_info.put("target", localDbName);
                rep_info.put("continuous", true);
                rep_db.create(rep_info);
            }
        }

    }



    public String getSrcReplicationName() {
        String protocol = "http";

        StringBuilder builder = new StringBuilder(protocol);
        builder.append("://");



        if (src_username != null && !src_username.equals("")) {
            builder.append(src_username);
            builder.append(":").append(src_password);
            builder.append("@");
        }

        builder.append(src_host).append("/");
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
            String password = null;

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
                if (up.length == 2) password = up[1];
            }
            new App(host, db, port, username, password).start();

        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static String[] parseUsernamePass(String arg) {
        if (arg == null) return null;
        return arg.split(":");
    }
}
