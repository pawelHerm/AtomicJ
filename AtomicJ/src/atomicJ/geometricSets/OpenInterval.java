package atomicJ.geometricSets;

import java.util.Collections;
import java.util.Set;

import atomicJ.utilities.Validation;

public class OpenInterval implements Interval
{
    private final double min;
    private final double max;

    public OpenInterval(double min, double max)
    {
        Validation.requireNotNaNParameterName(min, "min");
        Validation.requireNotNaNParameterName(max, "max");

        if(min >= max)
        {
            throw new IllegalArgumentException("The value of the min parameter cannot be equal or greater than the value of the max parameter");
        }

        this.min = min;
        this.max = max;
    }

    @Override
    public double getMinimumEnd()
    {
        return this.min;
    }

    @Override
    public double getMaximumEnd() 
    {
        return this.max;
    }

    @Override
    public double getGreatestContainedDoubleValue()
    {
        return Math.nextDown(this.max);
    }

    @Override
    public double getSmallestContainedDoubleValue()
    {
        return Math.nextUp(this.min);
    }

    @Override
    public double getClosestContainedDoubleValue(double val)
    {
        if(val > this.min && val < this.max)
        {
            return val;
        }

        if(val <= this.min)
        {
            return Math.nextUp(this.min);
        }
        if(val >= this.max)
        {
            return Math.nextDown(this.max);
        }

        return Double.NaN;
    }

    public double getClosestContainedDoubleValueNotSmallerThan(double val)
    {
        if(val >= this.max)
        {
            return Double.NaN;
        }

        double closestVal = Math.max(Math.nextUp(this.min), val);
        return closestVal;
    }

    @Override
    public Set<Double> getIsolatedPoints()
    {
        return Collections.emptySet();
    }

    public double getMinimum()
    {
        return min;
    }

    public double getMaximum()
    {
        return max;
    }

    //SPRAWDZONE
    @Override
    public Interval intersect(OpenInterval otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        double minNew = Math.max(this.min, otherSet.min);
        double maxNew = Math.min(this.max, otherSet.max);

        Interval intersection = maxNew <= minNew ? EmptyRealSet.getInstance() : new OpenInterval(minNew, maxNew);

        return intersection;
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

        if(maxNew <= minNew)
        {
            return EmptyRealSet.getInstance();
        }

        boolean leftClosed = this.min < otherSet.getMinimumEnd();
        boolean rightClosed = this.max > otherSet.getMaximumEnd();

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

    @Override
    public boolean contains(double val) 
    {
        boolean contained = val > min && val < max;
        return contained;
    } 

    //the parameter validation in the constructor makes sure that it is never empty
    @Override
    public boolean isEmpty() 
    {
        return false;
    }

    @Override
    public boolean isNumberOfElementsFinite()
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
        if(!(other instanceof OpenInterval))
        {
            return false;
        }

        OpenInterval that = (OpenInterval)other;

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
