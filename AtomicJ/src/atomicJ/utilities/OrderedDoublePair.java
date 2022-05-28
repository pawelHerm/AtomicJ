package atomicJ.utilities;

public class OrderedDoublePair
{
    private final double first;
    private final double second;

    public OrderedDoublePair(double first, double second)
    {
        this.first = first;
        this.second = second;
    }

    public boolean contains(double other)
    {      
        boolean contains = Double.compare(this.first, other) == 0 || Double.compare(this.second, other) == 0;

        return contains;
    }

    public double getFirst()
    {
        return first;
    }

    public double getSecond()
    {
        return second;
    }

    @Override
    public int hashCode()
    {
        int result = 20;

        int firstHashCode = Double.hashCode(first);
        int secondHashCode = Double.hashCode(second);

        result = 31*result + firstHashCode;
        result = 31*result + secondHashCode;

        return result;
    }

    @Override
    public boolean equals(Object that)
    {
        if(that instanceof OrderedDoublePair)
        {
            if(Double.compare(this.first, ((OrderedDoublePair) that).first) != 0)
            {
                return false;
            }
            if(Double.compare(this.second, ((OrderedDoublePair) that).second) != 0)
            {
                return false;
            }

            return true;
        }

        return false;
    }

}
