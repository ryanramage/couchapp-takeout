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
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author ryan
 */
public class AttachmentResource implements Resource, CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource, PropPatchableResource, LockableResource, DigestResource, ReplaceableResource{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AttachmentResource.class);

    private static HashMap<String,LockToken> locks = new HashMap<String,LockToken>();


    String name;
    String host;
    CouchDbConnector connector;
    String docId;
    private NullSecurityManager security = new NullSecurityManager();

    JsonNode object;
    JsonNode attachmentInfo;
    Date createDate = new Date(1); // just to keep consistant. 
    

    AttachmentResource(String name, String host, CouchDbConnector connector, String docId) {


        System.out.println("Attachement resource on " + docId + " with name " + name);
        this.name = name;
        this.connector = connector;
        this.docId = docId;
        this.host = host;

        object = connector.get(JsonNode.class, docId);

        log.info("object: " + object);

        attachmentInfo =  object.get("_attachments").get(name);
        if (attachmentInfo == null) {
            throw new RuntimeException("Attachment does not exisit");
        }
    }

    public AttachmentResource(String name, String host, CouchDbConnector connector, ObjectNode node) {
        this.name = name;
        this.connector = connector;
        this.docId = node.get("_id").getTextValue();
        this.host = host;

        object = node;

        log.info("object: " + object);

        attachmentInfo =  object.get("_attachments").get(name);
        if (attachmentInfo == null) {
            throw new RuntimeException("Attachment does not exisit");
        }

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
        return createDate;
    }

    @Override
    public String checkRedirect(Request rqst) {
        System.out.println("attach get rqs");
        return null;
    }

    @Override
    public void copyTo(CollectionResource cr, String newName) throws NotAuthorizedException, BadRequestException, ConflictException {
        System.out.println("copy to");
        if ( cr instanceof DocumentAttachmentCollection) {
            

            AttachmentInputStream ais = connector.getAttachment(docId, name);

            DocumentAttachmentCollection dac = (DocumentAttachmentCollection) cr;
            try {
                dac.createNew(newName, ais, getContentLength(), getContentType(""));
            } catch (IOException ex) {
                Logger.getLogger(AttachmentResource.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException(cr);
            }
            
        } else {
            throw new RuntimeException("Destination must be a Document");
        }

    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        connector.deleteAttachment(docId, object.get("_rev").getTextValue(), name);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> map, String string) throws IOException, NotAuthorizedException, BadRequestException {

        System.out.println("send content: " + name);


        AttachmentInputStream ais = connector.getAttachment(docId, name);
        IOUtils.copy(ais, out);
        ais.close();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        System.out.println("Att max age");
        return 315360000l;
    }

    @Override
    public String getContentType(String string) {
        try {
            return attachmentInfo.get("content_type").getTextValue();
        } catch (Exception e) {

        }
        return "application/binary";
    }

    @Override
    public Long getContentLength() {
        try {
            return attachmentInfo.get("length").getLongValue();
        } catch (Exception e) {}
        return null;
    }

    @Override
    public void moveTo(CollectionResource cr, String string) throws ConflictException, NotAuthorizedException, BadRequestException {
        copyTo(cr, string);
        delete();
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public void setProperties(Fields fields) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LockResult lock(LockTimeout lt, LockInfo li) throws NotAuthorizedException, PreConditionFailedException, LockedException {
        System.out.println("Lock");

        
        LockToken newToken = new LockToken(object.get("_rev").getTextValue(), li, lt);
        locks.put(object.get("_rev").getTextValue(), newToken);
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
        locks.remove(object.get("_rev").getTextValue());
        return;
    }

    @Override
    public LockToken getCurrentLock() {
        return locks.get(object.get("_rev").getTextValue());
    }



    @Override
    public Object authenticate(DigestResponse dr) {
        return security.authenticate(dr);
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    @Override
    public void replaceContent(InputStream in, Long l) throws BadRequestException, ConflictException, NotAuthorizedException {
        // update the
        log.info("REPLACE resource");

        String contentType = "application/binary";
        try {
            contentType = attachmentInfo.get("content_type").getTextValue();
        } catch (Exception e) {}



        try {

            String rev = object.get("_rev").getTextValue();

            System.out.println("Attach rev: " + rev);

            AttachmentInputStream ais = new AttachmentInputStream(name, in, contentType);

            rev  = connector.createAttachment(docId, rev, ais);
            
            System.out.println("Attach rev: " + rev);
            
            object = connector.get(JsonNode.class, docId);

            log.info("object: " + object);

            attachmentInfo =  object.get("_attachments").get(name);
            if (attachmentInfo == null) {
                throw new RuntimeException("Attachment does not exisit");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
