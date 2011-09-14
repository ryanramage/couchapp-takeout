/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.lucene;

import com.github.rnewson.couchdb.lucene.Config;
import com.github.rnewson.couchdb.lucene.JSONErrorHandler;
import com.github.rnewson.couchdb.lucene.LuceneServlet;
import java.io.File;
import java.io.IOException;
import org.apache.commons.configuration.ConfigurationException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.servlet.GzipFilter;

/**
 *
 * @author ryan
 */
public class LuceneServer {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LuceneServer.class);

    Server server;

    public void start() throws ConfigurationException, IOException, Exception {

        final Config config = new Config();
        final File dir = config.getDir();

        server = new Server();
        final SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost("localhost");
        connector.setPort(5985);

        log.info("Accepting connections with " + connector);

        server.setConnectors(new Connector[]{connector});
        server.setStopAtShutdown(true);
        server.setSendServerVersion(false);

        final LuceneServlet servlet = new LuceneServlet(config.getClient(), dir, config.getConfiguration());

        final Context context = new Context(server, "/", Context.NO_SESSIONS | Context.NO_SECURITY);
        context.addServlet(new ServletHolder(servlet), "/*");
        context.addFilter(new FilterHolder(new GzipFilter()), "/*", Handler.DEFAULT);
        context.setErrorHandler(new JSONErrorHandler());
        server.setHandler(context);

        server.start();
        server.join();
    }


    public void stop() throws Exception {
        server.stop();
    }


}
