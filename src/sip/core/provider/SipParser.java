package sip.core.provider;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.header.Header;
import com.allcam.gbgw.protocol.sip.core.header.RequestLine;
import com.allcam.gbgw.protocol.sip.core.header.StatusLine;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.tools.DateFormat;
import com.allcam.gbgw.protocol.tools.Parser;

import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class SipParser extends class Parser for parsing of SIP messages.
 */
public class SipParser extends Parser
{
    /**
     * Creates a new SipParser based on String <i>s</i>
     */
    public SipParser(String s)
    {
        super(s);
    }

    /**
     * Creates a new SipParser based on String <i>s</i> and starting from
     * position <i>i</i>
     */
    public SipParser(String s, int i)
    {
        super(s, i);
    }

    /**
     * Creates a new SipParser based on StringBuffer <i>sb</i>
     */
    public SipParser(StringBuffer sb)
    {
        super(sb);
    }

    /**
     * Creates a new SipParser based on StringBuffer <i>sb</i> and starting
     * from position <i>i</i>
     */
    public SipParser(StringBuffer sb, int i)
    {
        super(sb, i);
    }

    /**
     * Creates a new SipParser starting from the current position.
     */
    public SipParser(Parser p)
    {
        super(p.getWholeString(), p.getPos());
    }

    /**
     * MARK char[], composed by: '-' , '_' , '.' , '!' , '~' , '*' , '\'' , '|'
     */
    public static char[] MARK = {'-', '_', '.', '!', '~', '*', '\'', '|'};

    /**
     * SEPARATOR char[], composed by: ' ','\t','\r','\n','(',')','<','>',',',';','\\','"','/','[',']','?','=','{','}'
     */
    public static char[] SEPARATOR =
        {' ', '\t', '\r', '\n', '(', ')', '<', '>', ',', ';', '\\', '"', '/', '[', ']', '?', '=', '{', '}'};

    /**
     * Checks whether a char is any MARK
     */
    public static boolean isMark(char c)
    { // return (c=='-' || c=='_' ||
        // c=='.' || c=='!' || c=='~' ||
        // c=='*' || c=='\'' || c=='|');
        return isAnyOf(MARK, c);
    }

    /**
     * Unreserved char; that is an alphanum or a mark
     */
    public static boolean isUnreserved(char c)
    {
        return (isAlphanum(c) || isMark(c));
    }

    /**
     * Separator; differently form RFC2543, do not include '@' and ':', while
     * include '\r' and '\n'
     */
    public static boolean isSeparator(char c)
    { // return (isSpace(c) ||
        // isCRLF(c) || c=='(' || c==')'
        // || c=='<' || c=='>' || c==','
        // || c==';' || c=='\\' ||
        // c=='"' || c=='/' || c=='[' ||
        // c==']' || c=='?' || c=='=' ||
        // c=='{' || c=='}');
        return isAnyOf(SEPARATOR, c);
    }

    /**
     * Returns the first occurence of a separator or the end of the string
     */
    public int indexOfSeparator()
    {
        int begin = index;
        while (begin < str.length() && !isSeparator(str.charAt(begin)))
        {
            begin++;
        }
        return begin;
    }

    /**
     * Index of the end of the header (EOH)
     */
    public int indexOfEOH()
    {
        SipParser par = new SipParser(str, index);
        while (true)
        {
            // find the first CR or LF
            par.goTo(CRLF);
            if (!par.hasMore())
            {
                // if no CR/LF found return the end of
                return str.length();
            }
            // string
            int end = par.getPos();
            // skip the CR/LF chars
            par.goToNextLine();
            if (!par.hasMore() || !isWSP(par.nextChar()))
            {
                return end;
            }
        }
    }

    /**
     * Returns the begin of next header
     */
    public int indexOfNextHeader()
    {
        SipParser par = new SipParser(str, index);
        par.goToNextHeader();
        return par.getPos();
    }

    /**
     * Returns the index of the begin of the first occurence of the Header
     * <i>hname</i>
     */
    public int indexOfHeader(String hname)
    {
        Pattern p = Pattern.compile("^" + hname + ": ", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = p.matcher(str);
        if (m.find(index))
        {
            return m.start();
        }
        return str.length();
    }

    /**
     * Goes to the begin of next header
     */
    public SipParser goToNextHeader()
    {
        index = indexOfEOH();
        goToNextLine();
        return this;
    }

    /**
     * Go to the end of the last header. The final empty line delimiter is not
     * considered as header
     */
    public SipParser goToEndOfLastHeader()
    {
        // double newline
        String[] delimiters = {"\r\n\r\n", "\n\n"};
        goTo(delimiters);
        // no double newline found
        if (!hasMore())
        {
            if (str.startsWith("\r\n", str.length() - 2))
            {
                index = str.length() - 2;
            }
            else if (str.charAt(str.length() - 1) == '\n')
            {
                index = str.length() - 1;
            }
            else
            {
                index = str.length();
            }
        }
        return this;
    }

    /**
     * Go to the begin (first char of) Message Body
     */
    public SipParser goToBody()
    {
        goToEndOfLastHeader();
        goTo('\n').skipChar();
        goTo('\n').skipChar();
        return this;
    }

    /**
     * Returns the first header and goes to the next line.
     */
    public Header getHeader()
    {
        if (!hasMore())
        {
            return null;
        }
        int begin = getPos();
        int end = indexOfEOH();
        String headerStr = getString(end - begin);
        goToNextLine();
        int colon = headerStr.indexOf(':');
        if (colon < 0)
        {
            return null;
        }
        String hname = headerStr.substring(0, colon).trim();
        String hvalue = headerStr.substring(++colon).trim();
        return new Header(hname, hvalue);
    }

    /**
     * Returns the first occurence of Header <i>hname</i>.
     */
    public Header getHeader(String hname)
    {
        SipParser par = new SipParser(str, indexOfHeader(hname));
        if (!par.hasMore())
        {
            return null;
        }
        par.skipN(hname.length());
        int begin = par.indexOf(':') + 1;
        int end = par.indexOfEOH();
        if (begin > end)
        {
            return null;
        }
        String hvalue = str.substring(begin, end).trim();
        index = end;
        return new Header(hname, hvalue);
    }

    // ************************ first-line ************************

    /**
     * Returns the request-line.
     */
    public RequestLine getRequestLine()
    {
        String method = getString();
        skipWSP();
        int begin = getPos();
        int end = indexOfEOH();
        String requestUri = getString(end - begin);
        goToNextLine();
        return new RequestLine(method, (new SipParser(requestUri)).getSipURL());
    }

    /**
     * Returns the status-line or null (if it doesn't start with "SIP/").
     */
    public StatusLine getStatusLine()
    {
        String version = getString(4);
        if (!version.equalsIgnoreCase("SIP/"))
        {
            index = str.length();
            return null;
        }
        skipString().skipWSP(); // "SIP/2.0 "
        int code = getInt();
        int begin = getPos();
        int end = indexOfEOH();
        String reason = getString(end - begin).trim();
        goToNextLine();
        return new StatusLine(code, reason);
    }

    // *************************** URIs ***************************

    public static char[] uri_separators = {' ', '>', '\n', '\r'};

    /**
     * Returns the first URL. If no URL is found, it returns <b>null</b>
     */
    public SipURL getSipURL()
    {
        goTo("sip:");
        if (!hasMore())
        {
            return null;
        }
        int begin = getPos();
        int end = indexOf(uri_separators);
        if (end < 0)
        {
            end = str.length();
        }
        String url = getString(end - begin);
        if (hasMore())
        {
            skipChar();
        }
        return new SipURL(url);
    }

    /*
     * public static SipURL parseSipURL(String s) { SipParser par=new
     * SipParser(s); return par.parseSipURL(); }
     */

    /**
     * Returns the first NameAddress in the string <i>str</i>. If no
     * NameAddress is found, it returns <b>null</b>. A NameAddress is a string
     * of the form of: <BR>
     * <BLOCKQUOTE>
     *
     * <PRE>
     * &amp;nbsp&amp;nbsp &quot;user's name&quot; &lt;sip url&gt;
     * </PRE>
     *
     * </BLOCKQUOTE>
     */
    public NameAddress getNameAddress()
    {
        String text;
        SipURL url;
        int begin = getPos();
        int beginUrl = indexOf("<sip:");
        if (beginUrl < 0)
        {
            url = getSipURL();
            if (url == null)
            {
                setPos(begin);
                url = new SipURL(getString());
            }
            return new NameAddress(url);
        }
        else
        {
            text = getString(beginUrl - begin).trim();
            url = getSipURL();
            if (text.length() > 0 && text.charAt(0) == '\"' && text.charAt(text.length() - 1) == '\"')
            {
                text = text.substring(1, text.length() - 1);
                // now you should eliminate escape chars ('\')..
            }
            if (text.length() == 0)
            {
                return new NameAddress(url);
            }
            else
            {
                return new NameAddress(text, url);
            }
        }
    }

    // *************************** DATE ***************************

    /**
     * Returns a Date object according with the SIP standard date format
     */
    public Date getDate()
    {
        // DateFormat df=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",Locale.US);
        try
        {
            // Date d=df.parse(str,new ParsePosition(index));
            Date d = DateFormat.parseEEEddMMM(str, index);
            index = str.indexOf("GMT", index) + 3;
            return d;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            index = str.length();
            return null;
        }
    }

    // *************************** PARAMETERS ***************************

    public static char[] param_separators = {' ', '=', ';', ',', '\n', '\r'};

    /**
     * Gets the value of specified parameter.
     *
     * @return the parameter value or null if parameter does not exist or
     * doesn't have a value (i.e. in case of flag parameter).
     */
    public String getParameter(String name)
    {
        while (hasMore())
        {
            if (getWord(param_separators).equals(name))
            {
                skipWSP();
                if (nextChar() == '=')
                {
                    skipChar();
                    return getWordSkippingQuoted(param_separators);
                }
                else
                {
                    return null;
                }
            }
            goToSkippingQuoted(';');
            if (hasMore())
            {
                // skip ';'
                skipChar();
            }
        }
        return null;
    }

    /**
     * Gets a String Vector of parameter names. <BR>
     * Returns null if no parameter is present
     */
    public Vector<String> getParameters()
    {
        String name;
        Vector<String> params = new Vector<>();
        while (hasMore())
        {
            name = getWord(param_separators);
            if (name.length() > 0)
            {
                params.addElement(name);
            }
            goToSkippingQuoted(';');
            if (hasMore())
            {
                // skip ';'
                skipChar();
            }
        }
        return params;
    }

    /**
     * Whether there is the specified parameter
     */
    public boolean hasParameter(String name)
    {
        while (hasMore())
        {
            if (getWord(param_separators).equals(name))
            {
                return true;
            }
            goToSkippingQuoted(';');
            if (hasMore())
            {
                // skip ';'
                skipChar();
            }
        }
        return false;
    }

    // ************************ MULTIPLE HEADERS ************************

    /**
     * Finds the first comma-separator. Return -1 if no comma is found.
     */
    public int indexOfCommaHeaderSeparator()
    {
        boolean insideQuotedString = false;
        for (int i = index; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (c == '"')
            {
                insideQuotedString = !insideQuotedString;
            }
            if (!insideQuotedString && c == ',')
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Goes to the first comma-separator. Goes to the end of string if no comma
     * is found.
     */
    public SipParser goToCommaHeaderSeparator()
    {
        int comma = indexOfCommaHeaderSeparator();
        if (comma < 0)
        {
            index = str.length();
        }
        else
        {
            index = comma;
        }
        return this;
    }

    // ************************** SIP MESSAGE ***************************

    /**
     * Gets the first SIP message (all bytes until the first end of SIP
     * message), if a SIP message delimiter is found.
     * <p>
     * The message begins from the first non-CRLF char.
     */
    public Message getSipMessage()
    {
        // skip any CRLF sequence
        skipCRLF();
        // Get content length; if no Content-Length header found return null
        String text;
        if (getPos() == 0)
        {
            text = str;
        }
        else
        {
            text = getRemainingString();
        }
        Message msg = new Message(text);
        if (!msg.hasContentLengthHeader())
        {
            return null;
        }
        int bodyLen = msg.getContentLengthHeader().getContentLength();

        // gets the message (and go ahead), or returns null
        int begin = getPos();
        goToEndOfLastHeader();
        if (!hasMore())
        {
            return null;
        }
        goTo('\n');
        if (!hasMore())
        {
            return null;
        }
        // skip the LF of last header and go the the new
        skipChar().goTo('\n');
        // line
        if (!hasMore())
        {
            return null;
        }
        // skip the LF of the empty line and
        int bodyPos = skipChar().getPos();
        // go the the body

        int end = bodyPos + bodyLen;
        if (end <= str.length())
        {
            index = end;
            return new Message(str.substring(begin, end));
        }
        else
        {
            return null;
        }
    }
}
