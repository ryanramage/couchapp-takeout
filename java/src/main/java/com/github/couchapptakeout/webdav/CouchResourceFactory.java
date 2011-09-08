/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.net.URLEncoder;

import org.ektorp.http.URI;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.HttpStatus;

/**
 *
 * @author ryan
 */
public class CouchResourceFactory implements ResourceFactory{


   private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CouchResourceFactory.class);

    private CouchDbConnector connector;
    String contextPath;

    private AllDocsDirectoryResource ROOT;


    public CouchResourceFactory(CouchDbConnector connector) {
        this.connector = connector;
        ROOT = new AllDocsDirectoryResource(connector, "localhost:8080", this);
    }

      public String getSupportedLevels() {
        return "1,2";
     }


    @Override
    public Resource getResource(String host, String url) {
        System.out.println("Request " + host + " url " + url);

        Path path = Path.path(url);
        Resource r = find(host, path);
        System.out.println("Found: " + r);
        return r;
    }


    private Resource find(String host, Path path) {

  
        String doc = path.getFirst();
        String attachment = StringUtils.join(path.getAfterFirst(), "=");

        if (StringUtils.isEmpty(doc)) return ROOT;



        log.info("doc:  " + doc);
        log.info("attachment: " + attachment);
        
        if (!connector.contains(doc)) {
            log.info("document NOT exists: " + doc);
            return null;
        }

        log.info("document exists: " + doc);
        if (StringUtils.isEmpty(attachment)) {
            log.info("Document Object");
            return new DocumentAttachmentCollection(doc, host, connector);
        }  else {
            if (containsAttachment(connector, doc, attachment)) {
                log.info("Attachement Object");
                return new AttachmentResource(attachment, host, connector, doc);
            } else {
                log.info("Attachment not found");
                return null;
            }



        }




    }


    public boolean containsAttachment(CouchDbConnector connector, String doc, String attachment) {
        try {
            String path = connector.path() + "/" + URLEncoder.encode(doc, "UTF-8") + "/" + URLEncoder.encode(attachment, "UTF-8");
            HttpResponse response = connector.getConnection().head(path);
            if (response.getCode() == HttpStatus.NOT_FOUND) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (Exception ex) {
            Logger.getLogger(CouchResourceFactory.class.getName()).log(Level.SEVERE, null, ex);
            return Boolean.FALSE;
        }
    }


    public static String getAttachment(Path path) {
        String[] parts = path.getParts();
        if (parts == null || parts.length < 2) return null;
        return StringUtils.join(ArrayUtils.subarray(parts, 1, parts.length), "/");
    }


    private boolean isRoot( Path path ) {
        log.info("Checking path: " + path.toString());
        if( path == null) return true;
        return false;
    }








}
