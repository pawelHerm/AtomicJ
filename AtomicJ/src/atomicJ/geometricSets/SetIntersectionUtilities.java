package atomicJ.geometricSets;

import java.util.Iterator;
import java.util.List;
import atomicJ.utilities.Validation;

public class SetIntersectionUtilities 
{
    public static RealSet calculateSimpleDoubleSetIntersection(List<? extends Interval> intervals, List<? extends DiscreteRealSet> discreteSets)
    {
        Validation.requireNonNullParameterName(intervals, "intervals");
        Validation.requireNonNullParameterName(discreteSets, "discreteSets");

        if(intervals.isEmpty())
        {
            return calculateIntersectionOfDiscreteSets(discreteSets);  
        }

        if(discreteSets.isEmpty())
        {
            return calculateIntersectionOfIntervals(intervals);
        }

        Interval intersectionOfIntervals = calculateIntersectionOfIntervals(intervals);
        DiscreteRealSet intersectionOfDiscreteSets = calculateIntersectionOfDiscreteSets(discreteSets);  

        return intersectionOfDiscreteSets.intersect(intersectionOfIntervals);
    }

    public static Interval calculateIntersectionOfIntervals(List<? extends Interval> intervals)
    {
        Validation.requireNonNullParameterName(intervals, "intervals");

        Interval intersectionOfIntervals = EmptyRealSet.getInstance();
        Iterator<? extends Interval> iteratorIntervals = intervals.iterator();

        if(iteratorIntervals.hasNext())
        {
            intersectionOfIntervals = iteratorIntervals.next();

            while(iteratorIntervals.hasNext())
            {
                intersectionOfIntervals = intersectionOfIntervals.intersect(iteratorIntervals.next());
            }
        }

        return intersectionOfIntervals;
    }

    public static DiscreteRealSet calculateIntersectionOfDiscreteSets(List<? extends DiscreteRealSet> discreteSets)
    {
        Validation.requireNonNullParameterName(discreteSets, "discreteSets");

        DiscreteRealSet intersectionOfDiscreteSets = EmptyRealSet.getInstance();
        Iterator<? extends DiscreteRealSet> iteratorDiscrete = discreteSets.iterator();

        if(iteratorDiscrete.hasNext())
        {
            intersectionOfDiscreteSets = iteratorDiscrete.next();

            while(iteratorDiscrete.hasNext())
            {
                intersectionOfDiscreteSets = intersectionOfDiscreteSets.intersect(iteratorDiscrete.next());
            }
        }

        return intersectionOfDiscreteSets;
    }

}
