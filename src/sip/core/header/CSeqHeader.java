package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.provider.SipParser;

/**
 * SIP Header CSeq. The CSeq header field serves as a way to identify and order
 * transactions. It consists of a sequence number and a method. The method MUST
 * match that of the request. For non-REGISTER requests outside of a dialog, the
 * sequence number value is arbitrary.
 */
public class CSeqHeader extends Header
{
    public CSeqHeader(String hvalue)
    {
        super(SipHeaders.CSeq, hvalue);
    }

    public CSeqHeader(Header hd)
    {
        super(hd);
    }

    public CSeqHeader(long seq, String method)
    {
        super(SipHeaders.CSeq, seq + " " + method);
    }

    /**
     * Gets method of CSeqHeader
     */
    public String getMethod()
    {
        SipParser par = new SipParser(value);
        par.skipString(); // skip sequence number
        return par.getString();
    }

    /**
     * Gets sequence number of CSeqHeader
     */
    public long getSequenceNumber()
    {
        return (new SipParser(value)).getInt();
    }

    /**
     * Sets method of CSeqHeader
     */
    public void setMethod(String method)
    {
        value = getSequenceNumber() + " " + method;
    }

    /**
     * Sets sequence number of CSeqHeader
     */
    public void setSequenceNumber(long sequenceNumber)
    {
        value = sequenceNumber + " " + getMethod();
    }

    /**
     * Increments sequence number of CSeqHeader
     */
    public CSeqHeader incSequenceNumber()
    {
        value = (getSequenceNumber() + 1) + " " + getMethod();
        return this;
    }
}
