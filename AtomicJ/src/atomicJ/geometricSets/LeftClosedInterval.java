package atomicJ.geometricSets;

import java.util.Collections;
import java.util.Set;

import atomicJ.utilities.Validation;

public class LeftClosedInterval implements Interval
{
    private final double min;
    private final double max;
    //represents an interval closed at lower end
    public LeftClosedInterval(double min, double max)
    {
        Validation.requireNotNaNParameterName(min, "min");
        Validation.requireNotNaNParameterName(max, "max");

        if(min >= max)
        {
            throw new IllegalArgumentException("The value of the min parameter cannot be greater or equal than the value of the max parameter");
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
        return this.min;
    }

    @Override
    public double getClosestContainedDoubleValue(double val)
    {
        if(val >= this.min && val < this.max)
        {
            return val;
        }

        if(val >= this.max)
        {
            return Math.nextDown(this.max);
        }

        if(val < this.min)
        {
            return this.min;
        }

        return Double.NaN;
    }

    public double getClosestContainedDoubleValueNotSmallerThan(double val)
    {
        if(val >= this.max)
        {
            return Double.NaN;
        }

        double closestVal = Math.max(this.min, val);
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

    //sprawdzone
    @Override
    public Interval intersect(OpenInterval otherSet)
    {
        double minNew = Math.max(this.min, otherSet.getMinimum());
        double maxNew = Math.min(this.max, otherSet.getMaximum());

        if(maxNew <= minNew)
        {
            return EmptyRealSet.getInstance();
        }

        boolean leftClosed = this.min > otherSet.getMinimum();
        if(leftClosed)
        {
            return new LeftClosedInterval(minNew, maxNew);
        }

        return new OpenInterval(minNew, maxNew);
    }

    //sprawdzone
    @Override
    public Interval intersect(LeftClosedInterval otherSet)
    {
        double minNew = Math.max(this.min, otherSet.getMinimum());
        double maxNew = Math.min(this.max, otherSet.getMaximum());

        if(maxNew <= minNew)
        {
            return EmptyRealSet.getInstance();
        }

        return new LeftClosedInterval(minNew, maxNew);
    }

    //sprawdzone
    @Override
    public Interval intersect(RightClosedInterval otherSet)
    {
        double minNew = Math.max(this.min, otherSet.getMinimum());
        double maxNew = Math.min(this.max, otherSet.getMaximum());

        if(maxNew < minNew)
        {
            return EmptyRealSet.getInstance();
        }

        if((Double.compare(maxNew, minNew) == 0) && (Double.compare(this.min, minNew) == 0) && (Double.compare(otherSet.getMaximum(), maxNew) == 0))
        {
            return new SingleValueDoubleSet(minNew);
        }

        boolean leftClosed = this.min > otherSet.getMinimum();
        boolean rightClosed = this.max > otherSet.getMaximum();
        if(leftClosed && rightClosed)
        {
            return new ClosedInterval(minNew, maxNew);
        }
        else if(leftClosed)
        {
            return new LeftClosedInterval(minNew, maxNew);
        }
        else if(rightClosed)
        {
            return new RightClosedInterval(minNew, maxNew);
        }

        return new OpenInterval(minNew, maxNew);
    }

    //sprawdzone
    @Override
    public Interval intersect(ClosedInterval otherSet)
    {
        double minNew = Math.max(this.min, otherSet.getMinimumEnd());
        double maxNew = Math.min(this.max, otherSet.getMaximumEnd());

        if(maxNew < minNew)
        {
            return EmptyRealSet.getInstance();
        }

        if(Double.compare(minNew, maxNew) == 0)
        {
            if(Double.compare(maxNew, this.max) == 0)
            {
                return EmptyRealSet.getInstance();
            }
            return new SingleValueDoubleSet(minNew);
        }

        boolean rightClosed = this.max > otherSet.getMaximumEnd();
        if(rightClosed)
        {
            return new ClosedInterval(minNew, maxNew);       
        }

        return new LeftClosedInterval(minNew, maxNew);
    }

    @Override
    public boolean contains(double val) 
    {
        boolean contained = val >= min && val < max;
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
        if(!(other instanceof LeftClosedInterval))
        {
            return false;
        }

        LeftClosedInterval that = (LeftClosedInterval)other;

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
