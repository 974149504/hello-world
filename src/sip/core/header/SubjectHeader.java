package sip.core.header;

import org.apache.commons.lang3.StringUtils;

/**
 * SIP Header Subject.
 */
public class SubjectHeader extends Header
{
    /**
     * Creates a SubjectHeader with value <i>hvalue</i>
     */
    public SubjectHeader(String hvalue)
    {
        super(SipHeaders.Subject, hvalue);
    }

    /**
     * Creates a new SubjectHeader equal to SubjectHeader <i>hd</i>
     */
    public SubjectHeader(Header hd)
    {
        super(hd);
    }

    public SubjectHeader(String... items)
    {
        super(SipHeaders.Subject, String.join(",", items));
    }

    /**
     * Gets the subject
     */
    public String getSubject()
    {
        return getValue().trim();
    }

    public String[] getSubjectItems()
    {
        String subjectValue = getSubject();
        if (StringUtils.isBlank(subjectValue))
        {
            return null;
        }

        return subjectValue.split(",");
    }
}
