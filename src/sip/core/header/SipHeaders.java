package sip.core.header;

/**
 * SipHeaders extends class sip.header.SipHeaders by adding new SIP header
 * names.
 */
public class SipHeaders extends BaseSipHeaders
{

    // ****************************** Extensions
    // *******************************/

    /**
     * String "Accept-Contact"
     */
    public static final String ACCEPT_CONTACT = "Accept-Contact";

    /**
     * Whether <i>str</i> is "Accept-Contact"
     */
    public static boolean isAcceptContact(String str)
    {                // added by mandrajg
        return same(str, ACCEPT_CONTACT);
    }

    /**
     * String "Refer-To"
     */
    public static final String REFER_TO = "Refer-To";

    /**
     * Whether <i>str</i> is "Refer-To"
     */
    public static boolean isReferTo(String str)
    {
        return same(str, REFER_TO);
    }

    /**
     * String "Referred-By"
     */
    public static final String REFERRED_BY = "Referred-By";

    /**
     * Whether <i>str</i> is "Referred-By"
     */
    public static boolean isReferredBy(String str)
    {
        return same(str, REFERRED_BY);
    }

    /**
     * String "EVENT"
     */
    public static final String EVENT = "Event";

    /**
     * String "o"
     */
    public static final String EVENT_SHORT = "o";

    /**
     * Whether <i>str</i> is an EVENT field
     */
    public static boolean isEvent(String str)
    {
        return same(str, EVENT) || same(str, EVENT_SHORT);
    }

    /**
     * String "Allow-Events"
     */
    public static final String ALLOW_EVENTS = "Allow-Events";

    /**
     * Whether <i>str</i> is "Allow-Events"
     */
    public static boolean isAllowEvents(String str)
    {
        return same(str, ALLOW_EVENTS);
    }

    /**
     * String "Subscription-State"
     */
    public static final String SUBSCRIPTION_STATE = "Subscription-State";

    /**
     * Whether <i>str</i> is an SUBSCRIPTION_STATE field
     */
    public static boolean isSubscriptionState(String str)
    {
        return same(str, SUBSCRIPTION_STATE);
    }

}
