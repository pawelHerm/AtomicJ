package atomicJ.gui;

import java.util.Objects;

import org.jfree.util.ObjectUtilities;

public class IndexedStyleTag implements StyleTag
{
    private final Object initialStyleKey;
    private final String preferredStyleKey;
    private final int index;

    public IndexedStyleTag(Object initialStyleKeyGroup, int index)
    {
        this.initialStyleKey = initialStyleKeyGroup;
        this.preferredStyleKey = initialStyleKeyGroup.toString() + Integer.toString(index);
        this.index = index;
    }

    public IndexedStyleTag(Object initialStyleKeyGroup, String preferredStyleKeyGroup, int index)
    {
        this.initialStyleKey = initialStyleKeyGroup;
        this.preferredStyleKey = preferredStyleKeyGroup + Integer.toString(index);
        this.index = index;
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

    public int getIndex()
    {
        return index;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 17;

        hashCode = 31*hashCode + index;
        hashCode = 31*hashCode + ObjectUtilities.hashCode(preferredStyleKey);
        hashCode = 31*hashCode + ObjectUtilities.hashCode(initialStyleKey);

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof IndexedStyleTag))
        {
            return false;
        }

        IndexedStyleTag that= (IndexedStyleTag)other;
        boolean equal = (this.index == that.index) & (Objects.equals(this.preferredStyleKey, that.preferredStyleKey)) && (Objects.equals(this.initialStyleKey, that.initialStyleKey));

        return equal;
    }
}
