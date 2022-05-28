package atomicJ.functions;

import atomicJ.utilities.Validation;

public enum MonotonicityType
{
    STRICTLY_INCREASING(true, true, true), WEAKLY_INCREASING(true, true,false), WEAKLY_DECREASING(true, false,false), STRICTLY_DECREASING(true, false, true), NON_MONOTONIC(false, false, false);

    private final boolean monotonic;
    private final boolean mustBeNonDecreasing;//weakly decreasing function can be non-decreasing, so we called this variable mustBeNonDecresing instead of just nondecreasing
    private final boolean strict;

    MonotonicityType(boolean monotonic, boolean mustBeNonDecresing, boolean strict)
    {
        assert !(!monotonic && mustBeNonDecresing);//Non-monotonic function cannot be monotonically increasing
        assert !(!monotonic && strict); //Non-monotonic function cannot be strictly monotonic

        this.monotonic = monotonic;
        this.mustBeNonDecreasing = mustBeNonDecresing;
        this.strict = strict;
    }    

    //multiplication of the y axis, so this method returns MonotonicityType of g(x) = yScale*f(x)
    public MonotonicityType getMonotnicityTypeAfterValueMultiplication(double yScale)
    {
        Validation.requireNotNaNParameterName(yScale, "yScale");
        Validation.requireNotInfiniteParameterName(yScale, "yScale");

        boolean strictNew = (yScale != 0) ? this.strict : false;
        boolean mustBeNonDecreasingNew = (yScale == 0); 

        if(this.monotonic)
        {
            if(yScale > 0)
            {
                mustBeNonDecreasingNew = this.mustBeNonDecreasing;
            }
            else if(yScale < 0)
            {
                mustBeNonDecreasingNew = !this.mustBeNonDecreasing;
            }
        }

        return getMonotonicityType(this.monotonic, mustBeNonDecreasingNew, strictNew);
    }

    //multiplication of the x axis, so this method returns MonotonicityType of g(x) = f(xScale*x)
    public MonotonicityType getMonotnicityTypeAfterArgumentMultiplication(double xScale)
    {
        Validation.requireNotNaNParameterName(xScale, "xScale");
        Validation.requireNotInfiniteParameterName(xScale, "xScale");
        Validation.requireNonZeroParameterName(xScale, "xScale");

        boolean strictNew = this.strict;
        boolean mustBeNonDecreasingNew = false; 

        if(this.monotonic)
        {
            if(xScale > 0)
            {
                mustBeNonDecreasingNew = this.mustBeNonDecreasing;
            }
            else if(xScale < 0)
            {
                mustBeNonDecreasingNew = !this.mustBeNonDecreasing;
            }
        }

        return getMonotonicityType(this.monotonic, mustBeNonDecreasingNew, strictNew);
    }

    public boolean isMonotonic()
    {
        return monotonic;
    }

    public boolean isKnownToBeNonDecreasing()
    {
        return mustBeNonDecreasing;
    }

    public boolean isStrictlyMonotonic()
    {
        return strict;
    }

    public static MonotonicityType getMonotonicityType(boolean monotonic, boolean nondecreasing, boolean strict)
    {
        for(MonotonicityType type : MonotonicityType.values())
        {
            boolean found = (type.monotonic == monotonic) && (type.mustBeNonDecreasing == nondecreasing) && (type.strict == strict);
            if(found)
            {
                return type;
            }
        }

        throw new IllegalArgumentException("No monotonicity type known for the combination of the parameters: monotonic "+monotonic + " , increasing "+nondecreasing + " and strict " + strict);
    }
}
