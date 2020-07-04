package sip.core.provider;

/**
 * MethodIdentifier is used to address specific methods to the SipProvider.
 */
public class MethodIdentifier extends Identifier
{
    /**
     * Costructs a new MethodIdentifier.
     */
    public MethodIdentifier(String method)
    {
        super(method);
    }

    /**
     * Costructs a new MethodIdentifier based only on method name.
     */
    public MethodIdentifier(String method, String username)
    {
        super((null == username || username.isEmpty()) ? method : (method + "-" + username));
    }


    /**
     * Costructs a new MethodIdentifier.
     */
    public MethodIdentifier(MethodIdentifier i)
    {
        super(i);
    }
}
