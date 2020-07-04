package sip.core.provider;

/**
 * Generic Identifier.
 */
public class Identifier
{
    /**
     * The actual id
     */
    String id;

    /**
     * Costructs a new void Identifier.
     */
    Identifier()
    {
    }

    /**
     * Costructs a new Identifier.
     */
    Identifier(String id)
    {
        this.id = id;
    }

    /**
     * Costructs a new Identifier.
     */
    Identifier(Identifier i)
    {
        this.id = i.id;
    }

    /**
     * Whether the Identifier equals to <i>obj</i>.
     */
    @Override
    public boolean equals(Object obj)
    {
        try
        {
            Identifier i = (Identifier)obj;
            return id.equals(i.id);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Gets an int hashCode for the Identifier.
     */
    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * Gets a String value for the Identifier
     */
    @Override
    public String toString()
    {
        return id;
    }
}
