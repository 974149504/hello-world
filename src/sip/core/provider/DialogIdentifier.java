package sip.core.provider;

/**
 * DialogIdentifier is used to address specific dialogs to the SipProvider.
 */
public class DialogIdentifier extends Identifier
{
    /**
     * Costructs a new DialogIdentifier based on call-id, local and remote tags.
     */
    public DialogIdentifier(String callId, String localTag, String remoteTag)
    {
        id = callId + "-" + localTag + "-" + remoteTag;
    }

    /**
     * Costructs a new DialogIdentifier.
     */
    public DialogIdentifier(DialogIdentifier i)
    {
        super(i);
    }
}
