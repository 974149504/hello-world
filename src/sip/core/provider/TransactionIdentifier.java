package sip.core.provider;

import com.allcam.gbgw.protocol.sip.core.message.SipMethods;

/**
 * TransactionIdentifier is used to address specific transaction to the
 * SipProvider.
 */
public class TransactionIdentifier extends Identifier
{
    /**
     * Costructs a new TransactionIdentifier.
     */
    public TransactionIdentifier(TransactionIdentifier i)
    {
        super(i);
    }

    /**
     * Costructs a new TransactionIdentifier based only on method name.
     */
    public TransactionIdentifier(String method)
    {
        id = method;
    }

    /**
     * Costructs a new TransactionIdentifier based only on method name.
     */
    public TransactionIdentifier(String method, String username)
    {
        if (null == username || username.isEmpty())
        {
            id = method;
        }
        else
        {
            id = method + "-" + username;
        }
    }

    /**
     * Costructs a new TransactionIdentifier
     */
    public TransactionIdentifier(String callId, long seqn, String method, String branch)
    {
        if (branch == null)
        {
            branch = "";
        }
        if (method.equals(SipMethods.ACK))
        {
            method = SipMethods.INVITE;
        }
        id = callId + "-" + seqn + "-" + method + "-" + branch;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }
}
