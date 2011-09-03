/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.http.fs.NullSecurityManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author ryan
 */
public class DocumentAttachmentCollection implements CollectionResource, Resource, DigestResource,
        PutableResource, PropFindableResource,  GetableResource {


    private ObjectNode node;
    private CouchDbConnector connector;
    String host;
    private NullSecurityManager security = new NullSecurityManager();
    private Date d = new Date();


    DocumentAttachmentCollection(String id, String host, CouchDbConnector connector) {
        if (id.endsWith("/")) {
            id = id.substring(0, id.length() - 1);
        }
        if (id.startsWith("_design:")) {
            id = id.replaceFirst("_design:", "_design/");
        }


        try {
            System.out.println("Looking for doc: " + id);
            ObjectNode node = connector.get(ObjectNode.class, id);
            this.node = node;
            this.connector = connector;
            this.host = host;
        } catch (Exception e) {
            System.out.println("The doc: " + id + " is not found");
            throw new IllegalArgumentException("The doc: " + id + " is not found");
        }
    }

    DocumentAttachmentCollection(ObjectNode node, String host, CouchDbConnector connector) {
        this.node = node;
        this.connector = connector;
        this.host = host;
    }


    @Override
    public Resource child(String name) {
        System.out.println("doc get name");
        return new AttachmentResource(name, host, connector, node.get("_id").getTextValue());
    }

    @Override
    public List<? extends Resource> getChildren() {
        System.out.println("doc get children");
        List<AttachmentResource> result = new ArrayList<AttachmentResource>();
        JsonNode attachments = node.get("_attachments");
        if (attachments == null) return result;
        for (Iterator<String> i = attachments.getFieldNames(); i.hasNext();) {
            result.add(new AttachmentResource(i.next(), host, connector, node.get("_id").getTextValue()));
        }                
        return result;
    }

    @Override
    public String getUniqueId() {
        System.out.println("doc get uniq id");
        return node.get("_rev").getTextValue();
    }

    @Override
    public String getName() {
        System.out.println("doc get id");
        return node.get("_id").getTextValue();
    }

    @Override
    public Object authenticate(String user, String password) {
        System.out.println("doc get auth");
        return security.authenticate(user, password);
    }


    @Override
    public boolean authorise(Request rqst, Method method, Auth auth) {
        System.out.println("doc get auth2");
        return security.authorise(rqst, method, auth, this);
    }

    @Override
    public String getRealm() {
        System.out.println("doc get realm");
        return security.getRealm(host);
    }

    @Override
    public Date getModifiedDate() {
        System.out.println("doc get modified");
        return d;
    }

    @Override
    public String checkRedirect(Request rqst) {
        System.out.println("doc get name");
        return null;
    }

    @Override
    public boolean isDigestAllowed() {
        System.out.println("doc get is digest ");
        return true;
    }

    @Override
    public Resource createNew(String name, InputStream in, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        AttachmentInputStream ais = new AttachmentInputStream(name, in, contentType);
        connector.createAttachment(node.get("_id").getTextValue(), node.get("_rev").getTextValue(), ais);
        return new AttachmentResource(name, host, connector, node.get("_id").getTextValue());


    }

    @Override
    public Date getCreateDate() {
        System.out.println("doc get created date");
        return d;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> map, String string) throws IOException, NotAuthorizedException, BadRequestException {
        System.out.println("doc get send content");
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        System.out.println("doc get max age");
        return null;
    }

    @Override
    public String getContentType(String string) {
        System.out.println("doc get created content type");
        return "application/json";
    }

    @Override
    public Long getContentLength() {
        System.out.println("doc get content lenght");
        return null;
    }

    @Override
    public Object authenticate(DigestResponse dr) {
        System.out.println("doc get auth dr");
        return security.authenticate(dr);
    }

}
