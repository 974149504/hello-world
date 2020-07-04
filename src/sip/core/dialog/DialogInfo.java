package sip.core.dialog;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;

import java.util.Vector;

/**
 * Class DialogInfo maintains a complete information status of a generic SIP
 * dialog. It has the following attributes:
 * <ul>
 * <li>sip-provider</li>
 * <li>call-id</li>
 * <li>local and remote URLs</li>
 * <li>local and remote contact URLs</li>
 * <li>local and remote cseqs</li>
 * <li>local and remote tags</li>
 * <li>dialog-id</li>
 * <li>route set</li>
 * </ul>
 */
public class DialogInfo
{

    // ************************ Private attributes ************************
    /**
     * Local name
     */
    NameAddress localName;

    /**
     * Remote name
     */
    NameAddress remoteName;

    /**
     * Local contact url
     */
    NameAddress localContact;

    /**
     * Remote contact url
     */
    NameAddress remoteContact;

    /**
     * Call-id
     */
    String callId;

    /**
     * Local tag
     */
    String localTag;

    /**
     * Remote tag
     */
    String remoteTag;
    /** Sets the remote tag */

    /**
     * Local CSeq number
     */
    long localCseq;

    /**
     * Remote CSeq number
     */
    long remoteCseq;

    /**
     * Route set (Vector of NameAddresses)
     */
    /* HSC CHANGES START */ Vector<NameAddress> route;

    /* HSC CHANGES END */
    // **************************** Costructors ***************************

    /**
     * Creates a new empty DialogInfo
     */
    public DialogInfo()
    {
        this.localName = null;
        this.remoteName = null;
        this.localContact = null;
        this.remoteContact = null;
        this.callId = null;
        this.localTag = null;
        this.remoteTag = null;
        this.localCseq = -1;
        this.remoteCseq = -1;
        this.route = null;
    }

    // ************************** Public methods **************************

    /**
     * Sets the local name
     */
    public void setLocalName(NameAddress url)
    {
        localName = url;
    }

    /**
     * Gets the local name
     */
    public NameAddress getLocalName()
    {
        return localName;
    }

    /**
     * Sets the remote name
     */
    public void setRemoteName(NameAddress url)
    {
        remoteName = url;
    }

    /**
     * Gets the remote name
     */
    public NameAddress getRemoteName()
    {
        return remoteName;
    }

    /**
     * Sets the local contact url
     */
    public void setLocalContact(NameAddress nameAddress)
    {
        localContact = nameAddress;
    }

    /**
     * Gets the local contact url
     */
    public NameAddress getLocalContact()
    {
        return localContact;
    }

    /**
     * Sets the remote contact url
     */
    public void setRemoteContact(NameAddress nameAddress)
    {
        remoteContact = nameAddress;
    }

    /**
     * Gets the remote contact url
     */
    public NameAddress getRemoteContact()
    {
        return remoteContact;
    }

    /**
     * Sets the call-id
     */
    public void setCallID(String id)
    {
        callId = id;
    }

    /**
     * Gets the call-id
     */
    public String getCallID()
    {
        return callId;
    }

    /**
     * Sets the local tag
     */
    public void setLocalTag(String tag)
    {
        localTag = tag;
    }

    /**
     * Gets the local tag
     */
    public String getLocalTag()
    {
        return localTag;
    }

    public void setRemoteTag(String tag)
    {
        remoteTag = tag;
    }

    /**
     * Gets the remote tag
     */
    public String getRemoteTag()
    {
        return remoteTag;
    }

    /**
     * Sets the local CSeq number
     */
    public void setLocalCSeq(long cseq)
    {
        localCseq = cseq;
    }

    /**
     * Increments the local CSeq number
     */
    public void incLocalCSeq()
    {
        localCseq++;
    }

    /**
     * Gets the local CSeq number
     */
    public long getLocalCSeq()
    {
        return localCseq;
    }

    /**
     * Sets the remote CSeq number
     */
    public void setRemoteCSeq(long cseq)
    {
        remoteCseq = cseq;
    }

    /**
     * Increments the remote CSeq number
     */
    public void incRemoteCSeq()
    {
        remoteCseq++;
    }

    /**
     * Gets the remote CSeq number
     */
    public long getRemoteCSeq()
    {
        return remoteCseq;
    }

    /* HSC CHANGES BEGIN */

    /**
     * Sets the route set
     */
    public void setRoute(Vector<NameAddress> r)
    {
        route = r;
    }

    /**
     * Gets the route set
     */
    public Vector<NameAddress> getRoute()
    {
        return route;
    }
    /* HSC CHANGES END */
}
