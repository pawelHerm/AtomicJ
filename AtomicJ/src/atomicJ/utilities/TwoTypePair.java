package atomicJ.utilities;

import java.util.Objects;

public class TwoTypePair<E, V>
{
    private final E first;
    private final V second;

    public TwoTypePair(E first, V second)
    {
        this.first = first;
        this.second = second;
    }

    public boolean contains(Object o)
    {      
        boolean contains = Objects.equals(this.first, o) || Objects.equals(this.second, o);

        return contains;
    }

    public E getFirst()
    {
        return first;
    }

    public V getSecond()
    {
        return second;
    }


    @Override
    public int hashCode()
    {
        int result = Objects.hashCode(this.first);
        result = 31*result + Objects.hashCode(this.second);

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof TwoTypePair)
        {
            TwoTypePair<?,?> that = (TwoTypePair<?,?>) other;
            if(!Objects.equals(this.first, that.first))
            {
                return false;
            }
            if(!Objects.equals(this.second, that.second))
            {
                return false;
            }

            return true;
        }

        return false;
    }
}
