package atomicJ.utilities;

import java.util.Objects;

public class UnOrderedPair<E> implements Pair<E>
{
    private final E first;
    private final E second;

    public UnOrderedPair(E first, E second)
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
        int firstHashCode = Objects.hashCode(this.first);
        int secondHashCode = Objects.hashCode(this.second);

        int result = firstHashCode + secondHashCode;

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof UnOrderedPair)
        {
            UnOrderedPair<?> that = ((UnOrderedPair<?>) other);
            boolean firstEqual = Objects.equals(this.first, that.first);

            if(firstEqual)
            {
                boolean secondEqual = Objects.equals(this.first, that.first);

                if(secondEqual)
                {
                    return true;
                }
            }

            boolean firstSecondEqual = Objects.equals(this.first, that.second);

            if(firstSecondEqual)
            {
                boolean secondFirstEqual = Objects.equals(this.second,that.first);
                if(secondFirstEqual)
                {
                    return true;
                }
            }

            return false;
        }

        return false;
    }
}
