package atomicJ.gui;

import org.jfree.util.ObjectUtilities;

public class StandardStyleTag implements StyleTag
{
    private final Object initialStyleKey;
    private final String preferredStyleKey;

    public StandardStyleTag(Object initialStyleKey)
    {
        this.initialStyleKey = initialStyleKey;
        this.preferredStyleKey = initialStyleKey.toString();
    }

    public StandardStyleTag(Object initialStyleKey, String preferredStyleKey)
    {
        this.initialStyleKey = initialStyleKey;
        this.preferredStyleKey = preferredStyleKey;
    }

    @Override
    public String getPreferredStyleKey()
    {
        return preferredStyleKey;
    }

    @Override
    public Object getInitialStyleKey()
    {
        return initialStyleKey;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 17;

        hashCode = 31*hashCode + ObjectUtilities.hashCode(preferredStyleKey);
        hashCode = 31*hashCode + ObjectUtilities.hashCode(initialStyleKey);

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof StandardStyleTag))
        {
            return false;
        }

        StandardStyleTag that= (StandardStyleTag)other;
        boolean equal = (ObjectUtilities.equal(this.preferredStyleKey, that.preferredStyleKey)) && (ObjectUtilities.equal(this.initialStyleKey, that.initialStyleKey));

        return equal;
    }
}
