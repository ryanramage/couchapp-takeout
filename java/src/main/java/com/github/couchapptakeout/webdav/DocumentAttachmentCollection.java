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
        ObjectNode node = connector.get(ObjectNode.class, id);
        this.node = node;
        this.connector = connector;
        this.host = host;

    }

    DocumentAttachmentCollection(ObjectNode node, String host, CouchDbConnector connector) {
        this.node = node;
        this.connector = connector;
        this.host = host;
    }


    @Override
    public Resource child(String name) {
        return new AttachmentResource(name, host, connector, node.get("_id").getTextValue());
    }

    @Override
    public List<? extends Resource> getChildren() {
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
        return node.get("_rev").getTextValue();
    }

    @Override
    public String getName() {
        return node.get("_id").getTextValue();
    }

    @Override
    public Object authenticate(String user, String password) {
        return security.authenticate(user, password);
    }


    @Override
    public boolean authorise(Request rqst, Method method, Auth auth) {
        return security.authorise(rqst, method, auth, this);
    }

    @Override
    public String getRealm() {
        return security.getRealm(host);
    }

    @Override
    public Date getModifiedDate() {
        return d;
    }

    @Override
    public String checkRedirect(Request rqst) {
        return null;
    }

    @Override
    public boolean isDigestAllowed() {
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
        return d;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> map, String string) throws IOException, NotAuthorizedException, BadRequestException {
        
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String string) {
        return "application/json";
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public Object authenticate(DigestResponse dr) {
        return security.authenticate(dr);
    }

}
