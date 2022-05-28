package atomicJ.utilities;

public class OrderedIntegerPair
{
    private final int first;
    private final int second;

    public OrderedIntegerPair(int first, int second)
    {
        this.first = first;
        this.second = second;
    }

    public boolean contains(double other)
    {      
        boolean contains = this.first == other || this.second == other;

        return contains;
    }

    public int getFirst()
    {
        return first;
    }

    public int getSecond()
    {
        return second;
    }


    @Override
    public int hashCode()
    {
        int result = 20;

        int firstHashCode = Integer.hashCode(first);
        int secondHashCode = Integer.hashCode(second);

        result = 31*result + firstHashCode;
        result = 31*result + secondHashCode;

        return result;
    }

    @Override
    public boolean equals(Object that)
    {
        if(that instanceof OrderedIntegerPair)
        {
            if(this.first != ((OrderedIntegerPair) that).first)
            {
                return false;
            }
            if(this.second != ((OrderedIntegerPair) that).second)
            {
                return false;
            }

            return true;
        }

        return false;
    }

}
