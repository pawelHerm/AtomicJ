package atomicJ.sources;

import atomicJ.utilities.Validation;

public class IdentityTag
{
    private final Object key;
    private final String label;

    //immutable class

    public IdentityTag(Object key)
    {
        this.key = Validation.requireNonNullParameterName(key, "key");
        this.label = key.toString();
    }

    public IdentityTag(Object key, String label)
    {
        this.key = Validation.requireNonNullParameterName(key, "key");
        this.label = label;
    }

    public Object getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }

    @Override
    public int hashCode()
    {
        return key.hashCode(); 
    }

    @Override
    public boolean equals(Object that)
    {
        if(that instanceof IdentityTag)
        {
            return this.key.equals(((IdentityTag)that).key);
        }

        return false;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
