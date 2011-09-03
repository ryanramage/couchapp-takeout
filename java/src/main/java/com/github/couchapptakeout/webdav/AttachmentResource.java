/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.ettrema.http.fs.NullSecurityManager;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author ryan
 */
public class AttachmentResource implements Resource, CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource, PropPatchableResource, LockableResource, DigestResource {


    String name;
    String host;
    CouchDbConnector connector;
    String docId;
    private NullSecurityManager security = new NullSecurityManager();
    


    AttachmentResource(String name, String host, CouchDbConnector connector, String docId) {
        System.out.println("Attachement resource on " + docId + " with name " + name);
        this.name = name;
        this.connector = connector;
        this.docId = docId;
        this.host = host;
    }


    @Override
    public String getUniqueId() {
        System.out.println("attach get id");
        return docId + "/" + name;
    }

    @Override
    public String getName() {
        System.out.println("attach get name");
        return name;
    }

    @Override
    public Object authenticate(String user, String password) {
        System.out.println("attach get auth");
        return security.authenticate(user, password);
    }

    @Override
    public boolean authorise(Request rqst, Method method, Auth auth) {
        System.out.println("attach get auth 1");
        return security.authorise(rqst, method, auth, this);
    }

    @Override
    public String getRealm() {
        System.out.println("attach get realm");
        return security.getRealm(host);
    }

    @Override
    public Date getModifiedDate() {
        System.out.println("attach get mod date");
        return new Date();
    }

    @Override
    public String checkRedirect(Request rqst) {
        System.out.println("attach get rqs");
        return null;
    }

    @Override
    public void copyTo(CollectionResource cr, String string) throws NotAuthorizedException, BadRequestException, ConflictException {
        if ( cr instanceof DocumentAttachmentCollection) {
            DocumentAttachmentCollection dac = (DocumentAttachmentCollection) cr;
            
        } else {
            throw new RuntimeException("Destination must be a Document");
        }

    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        JsonNode object = connector.get(JsonNode.class, docId);
        connector.deleteAttachment(docId, object.get("_rev").getTextValue(), name);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> map, String string) throws IOException, NotAuthorizedException, BadRequestException {
        JsonNode object = connector.get(JsonNode.class, docId);

        AttachmentInputStream ais = connector.getAttachment(docId, object.get("_rev").getTextValue());
        IOUtils.copy(ais, out);
        ais.close();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        System.out.println("Att max age");
        return null;
    }

    @Override
    public String getContentType(String string) {
        System.out.println("get content type");
        return "application/binary";
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public void moveTo(CollectionResource cr, String string) throws ConflictException, NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getCreateDate() {
        return new Date();
    }

    @Override
    public void setProperties(Fields fields) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LockResult lock(LockTimeout lt, LockInfo li) throws NotAuthorizedException, PreConditionFailedException, LockedException {
        System.out.println("Lock");
        LockToken newToken = new LockToken(UUID.randomUUID().toString(), li, lt);
        return LockResult.success( newToken );
    }

    @Override
    public LockResult refreshLock(String string) throws NotAuthorizedException, PreConditionFailedException {
        System.out.println("Refresh");
        return LockResult.failed( LockResult.FailureReason.PRECONDITION_FAILED );
    }

    @Override
    public void unlock(String string) throws NotAuthorizedException, PreConditionFailedException {
        System.out.println("Unlock");
        return;
    }

    @Override
    public LockToken getCurrentLock() {
        System.out.println("Current");
        return null;
    }



    @Override
    public Object authenticate(DigestResponse dr) {
        return security.authenticate(dr);
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

}
