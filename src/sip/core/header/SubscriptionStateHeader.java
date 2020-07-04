package sip.core.header;

import com.allcam.gbgw.protocol.tools.Parser;

/**
 * Subscription-State header (see RFC3265 for details).
 */
public class SubscriptionStateHeader extends ParametricHeader
{
    /**
     * State "active"
     */
    private static final String ACTIVE = "active";

    /**
     * State "pending"
     */
    private static final String PENDING = "pending";

    /**
     * State "terminated"
     */
    private static final String TERMINATED = "terminated";

    /**
     * State delimiters.
     */
    private static final char[] DELIM = {',', ';', ' ', '\t', '\n', '\r'};

    /**
     * Costructs a new SubscriptionStateHeader.
     */
    public SubscriptionStateHeader(String state)
    {
        super(SipHeaders.SUBSCRIPTION_STATE, state);
    }

    /**
     * Costructs a new SubscriptionStateHeader.
     */
    public SubscriptionStateHeader(Header hd)
    {
        super(hd);
    }

    /**
     * Gets the subscription state.
     */
    public String getState()
    {
        return new Parser(value).getWord(DELIM);
    }

    /**
     * Whether the subscription is active.
     */
    public boolean isActive()
    {
        return getState().equals(ACTIVE);
    }

    /**
     * Whether the subscription is pending.
     */
    public boolean isPending()
    {
        return getState().equals(PENDING);
    }

    /**
     * Whether the subscription is terminated.
     */
    public boolean isTerminated()
    {
        return getState().equals(TERMINATED);
    }

    /**
     * Sets the 'expires' param.
     */
    public SubscriptionStateHeader setExpires(int secs)
    {
        setParameter("expires", Integer.toString(secs));
        return this;
    }

    /**
     * Whether there is the 'expires' param.
     */
    public boolean hasExpires()
    {
        return hasParameter("expires");
    }

    /**
     * Gets the 'expires' param.
     */
    public int getExpires()
    {
        String exp = getParameter("expires");
        if (exp != null)
        {
            return Integer.parseInt(exp);
        }
        else
        {
            return -1;
        }
    }

    /**
     * Sets the 'reason' param.
     */
    public SubscriptionStateHeader setReason(String reason)
    {
        setParameter("reason", reason);
        return this;
    }

    /**
     * Whether there is the 'reason' param.
     */
    public boolean hasReason()
    {
        return hasParameter("reason");
    }

    /**
     * Gets the 'reason' param.
     */
    public String getReason()
    {
        return getParameter("reason");
    }

}
