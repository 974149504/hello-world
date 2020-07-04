package sip.core.message;

/**
 * Class BaseSipMethods collects all SIP method names.
 */
public abstract class BaseSipMethods
{

    /**
     * String "INVITE"
     */
    public static final String INVITE = "INVITE";

    /**
     * Whether <i>str</i> is INVITE
     */
    public static boolean isInvite(String str)
    {
        return same(str, INVITE);
    }

    /**
     * String "ACK"
     */
    public static final String ACK = "ACK";

    /**
     * Whether <i>str</i> is ACK
     */
    public static boolean isAck(String str)
    {
        return same(str, ACK);
    }

    /**
     * String "CANCEL"
     */
    public static final String CANCEL = "CANCEL";

    /**
     * Whether <i>str</i> is CANCEL
     */
    public static boolean isCancel(String str)
    {
        return same(str, CANCEL);
    }

    /**
     * String "BYE"
     */
    public static final String BYE = "BYE";

    /**
     * Whether <i>str</i> is BYE
     */
    public static boolean isBye(String str)
    {
        return same(str, BYE);
    }

    /**
     * String "INFO"
     */
    public static final String INFO = "INFO";

    /**
     * Whether <i>str</i> is INFO
     */
    public static boolean isInfo(String str)
    {
        return same(str, INFO);
    }

    /**
     * String "OPTION"
     */
    public static final String OPTION = "OPTION";

    /**
     * Whether <i>str</i> is OPTION
     */
    public static boolean isOption(String str)
    {
        return same(str, OPTION);
    }

    /**
     * String "OPTION"
     */
    public static final String OPTIONS = "OPTIONS";

    /**
     * Whether str is OPTIONS
     */
    public static boolean isOptions(String str)
    {
        return same(str, OPTIONS);
    }

    /**
     * String "REGISTER"
     */
    public static final String REGISTER = "REGISTER";

    /**
     * Whether <i>str</i> is REGISTER
     */
    public static boolean isRegister(String str)
    {
        return same(str, REGISTER);
    }

    /**
     * String "UPDATE"
     */
    public static final String UPDATE = "UPDATE";

    /**
     * Whether <i>str</i> is UPDATE
     */
    public static boolean isUpdate(String str)
    {
        return same(str, UPDATE);
    }

    /**
     * Whether <i>s1</i> and <i>s2</i> are case-unsensitive-equal.
     */
    protected static boolean same(String s1, String s2)
    { // return
        // s1.compareToIgnoreCase(s2)==0;
        return s1.equalsIgnoreCase(s2);
    }
}
