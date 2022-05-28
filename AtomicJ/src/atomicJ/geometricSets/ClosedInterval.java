package atomicJ.geometricSets;

import java.util.Collections;
import java.util.Set;

import atomicJ.utilities.Validation;

public class ClosedInterval implements Interval
{
    private final double min;
    private final double max;

    public ClosedInterval(double min, double max)
    {
        Validation.requireNotNaNParameterName(min, "min");
        Validation.requireNotNaNParameterName(max, "max");

        if(min > max)
        {
            throw new IllegalArgumentException("The value of the min parameter cannot be greater than the value of the max parameter");
        }

        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isNumberOfElementsFinite()
    {
        boolean finite = (Double.compare(this.min, this.max) == 0);

        return finite;
    }

    @Override
    public double getGreatestContainedDoubleValue()
    {
        return this.max;
    }

    @Override
    public double getSmallestContainedDoubleValue() 
    {
        return this.min;
    }

    @Override
    public double getClosestContainedDoubleValue(double val)
    {
        double closestVal = Math.min(this.max, Math.max(this.min, val));
        return closestVal;
    }

    public double getClosestContainedDoubleValueNotSmallerThan(double val)
    {
        if(val > this.max)
        {
            return Double.NaN;
        }

        double closestVal = Math.max(this.min, val);
        return closestVal;
    }

    @Override
    public Set<Double> getIsolatedPoints()
    {
        if(Double.compare(this.min, this.max) == 0)
        {
            return Collections.singleton(this.min);
        }

        return Collections.emptySet();
    }

    @Override
    public double getMinimumEnd()
    {
        return min;
    }

    @Override
    public double getMaximumEnd()
    {
        return max;
    }

    //sprawdzone
    @Override
    public Interval intersect(OpenInterval otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        double minNew = Math.max(this.min, otherSet.getMinimum());
        double maxNew = Math.min(this.max, otherSet.getMaximum());

        if(maxNew <= minNew)
        {
            return EmptyRealSet.getInstance();
        }

        boolean leftClosed = this.min > otherSet.getMinimum();
        boolean rightClosed = this.max < otherSet.getMaximum();

        if(leftClosed && rightClosed)
        {
            return new ClosedInterval(minNew, maxNew);
        }
        else if(leftClosed)
        {
            return new LeftClosedInterval(minNew, maxNew);
        }
        else
        {
            return new OpenInterval(minNew, maxNew);
        }        
    }

    //sprawdzone
    @Override
    public Interval intersect(LeftClosedInterval otherSet)
    {
        return otherSet.intersect(this);
    }

    //sprawdzone
    @Override
    public Interval intersect(RightClosedInterval otherSet)
    {
        return otherSet.intersect(this);
    }

    //SPRAWDZONE
    @Override
    public Interval intersect(ClosedInterval otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        double minNew = Math.max(this.min, otherSet.getMinimumEnd());
        double maxNew = Math.min(this.max, otherSet.getMaximumEnd());

        if(maxNew < minNew)
        {
            return EmptyRealSet.getInstance();
        }

        if(Double.compare(minNew, maxNew) == 0)
        {
            return new SingleValueDoubleSet(minNew);
        }

        return new ClosedInterval(minNew, maxNew);       
    }

    @Override
    public boolean contains(double val) 
    {
        boolean contained = val >= min && val <= max;
        return contained;
    }  

    //the parameter validation in the constructor makes sure that it is never empty
    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        int hashCode = Double.hashCode(this.min);
        hashCode = 31*hashCode + Double.hashCode(this.max);

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof ClosedInterval))
        {
            return false;
        }

        ClosedInterval that = (ClosedInterval)other;

        if(Double.compare(this.min, that.min) != 0)
        {
            return false;
        }

        if(Double.compare(this.max, that.max) != 0)
        {
            return false;
        }

        return true;
    }
}
