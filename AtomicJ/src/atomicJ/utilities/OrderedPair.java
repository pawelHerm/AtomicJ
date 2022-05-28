package atomicJ.utilities;

import java.util.Objects;

public class OrderedPair<E> implements Pair<E>
{
    private final E first;
    private final E second;

    public OrderedPair(E first, E second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean contains(Object o)
    {      
        boolean contains = Objects.equals(this.first, o) || Objects.equals(this.second, o);

        return contains;
    }

    @Override
    public E getFirst()
    {
        return first;
    }

    @Override
    public E getSecond()
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
        if(other instanceof OrderedPair)
        {
            OrderedPair<?> that = (OrderedPair<?>) other;
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
