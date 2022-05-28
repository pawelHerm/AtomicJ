package atomicJ.gui.rois;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;

import atomicJ.utilities.MultiMap;

public class Crossing2D implements Comparable<Crossing2D>
{
    private final double xVal;
    private final double yVal;

    private final double previousPolylineSegmentsLength;
    private final double polylinePosition;
    private final double crossedSegmentPosition;
    private final int cuttingSegmentIndex;

    private final boolean rightToLeft;
    private boolean polyLineSelfCrossing = false;

    //if the ray is horizontal line, then crossing from below to above the line is rightToLeft
    public Crossing2D(double crossedSegmentPosition, double currentPolylinePosition, double previousPolylineSegmentsLength, int cuttingPolylineSegmentIndex, double xVal, double yVal, boolean rightToLeft)
    {
        this.xVal = xVal;
        this.yVal = yVal;
        this.crossedSegmentPosition = crossedSegmentPosition;
        this.previousPolylineSegmentsLength = previousPolylineSegmentsLength;
        this.polylinePosition = currentPolylinePosition;
        this.cuttingSegmentIndex = cuttingPolylineSegmentIndex;
        this.rightToLeft = rightToLeft;
    }

    public boolean isPolyLineSelfCrossing()
    {
        return polyLineSelfCrossing;
    }

    public void setPolylineSelfCrossing(boolean polyLineSelfCrossing)
    {
        this.polyLineSelfCrossing = polyLineSelfCrossing;
    }

    public boolean isCrossingFromRightToLeft()
    {
        return rightToLeft;
    }

    public int getCuttingCurveSegmentIndex()
    {
        return cuttingSegmentIndex;
    }

    public double getCuttingCurvePosition()
    {
        return polylinePosition;
    }

    public double getPreviousPolylineSegmentsLength()
    {
        return previousPolylineSegmentsLength;
    }

    public double getCurrentPolygonSegmentPosition()
    {
        return crossedSegmentPosition;
    }

    public double[] getVertex()
    {
        return new double[] {xVal, yVal};
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof Crossing2D))
        {
            return false;
        }

        Crossing2D that = (Crossing2D)other;
        if(Double.compare(this.xVal, that.xVal) != 0)
        {
            return false;
        }
        if(Double.compare(this.yVal, that.yVal) != 0)
        {
            return false;
        }
        if(Double.compare(this.polylinePosition, that.polylinePosition) != 0)
        {
            return false;
        }
        if(this.rightToLeft != that.rightToLeft)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int code = 17;

        long xValBits = Double.doubleToLongBits(xVal);
        long yValBits = Double.doubleToLongBits(yVal);
        long tValBits = Double.doubleToLongBits(polylinePosition);

        code= 31*code + (int)(xValBits ^ (xValBits >>> 32)); 
        code= 31*code + (int)(yValBits ^ (yValBits >>> 32)); 
        code= 31*code + (int)(tValBits ^ (tValBits >>> 32)); 

        code = 31 + (rightToLeft ? 1 : 0);

        return code;
    }

    @Override
    public int compareTo(Crossing2D other) 
    {       
        return Double.compare(this.polylinePosition, other.polylinePosition);
    }

    @Override
    public String toString()
    {
        String s = "t " + Double.toString(polylinePosition) + ", x " + Double.toString(xVal) + ", y " + Double.toString(yVal) + " is right to left " + Boolean.toString(rightToLeft);

        return s;
    }

    public static void sortByCurrentPolygonSegmentPosition(List<Crossing2D> crossings)
    {
        Comparator<Crossing2D> comp = new Comparator<Crossing2D>() {

            @Override
            public int compare(Crossing2D o1, Crossing2D o2) 
            {
                return Double.compare(o1.crossedSegmentPosition, o2.crossedSegmentPosition);
            }
        };

        Collections.sort(crossings, comp);
    }

    public static void sortByCuttingCurvePosition(List<Crossing2D> crossings)
    {
        Comparator<Crossing2D> comp = new Comparator<Crossing2D>() {

            @Override
            public int compare(Crossing2D o1, Crossing2D o2) 
            {
                return Double.compare(o1.polylinePosition, o2.polylinePosition);
            }
        };

        Collections.sort(crossings, comp);
    }

    public static TDoubleList resolveInsidness(Collection<Crossing2D> crossings)
    {
        int windingNumber = 0;

        TDoubleList insidnessLimits = new TDoubleArrayList();

        for(Iterator<Crossing2D> it = crossings.iterator();it.hasNext();)
        {
            Crossing2D crossing = it.next();
            int w = crossing.rightToLeft ? 1 : -1;
            int windingNumberNew = windingNumber + w;

            if((windingNumberNew == 0) != (windingNumber == 0))
            {
                insidnessLimits.add(crossing.xVal);
            }

            windingNumber = windingNumberNew;
        }

        return insidnessLimits;
    }

    public static List<Crossing2D> removeInternal(Collection<Crossing2D> crossings)
    {
        int windingNumber = 0;

        List<Crossing2D> insidnessLimits = new ArrayList<>();

        for(Iterator<Crossing2D> it = crossings.iterator();it.hasNext();)
        {
            Crossing2D crossing = it.next();
            int w = crossing.rightToLeft ? 1 : -1;
            int windingNumberNew = windingNumber + w;

            if((windingNumberNew == 0) != (windingNumber == 0))
            {
                insidnessLimits.add(crossing);
            }

            windingNumber = windingNumberNew;
        }

        return insidnessLimits;
    }

    public static class CrossingsHierarchy
    {
        private final MultiMap<Integer, Crossing2D> crossings;

        private CrossingsHierarchy(MultiMap<Integer, Crossing2D> crossings)
        {
            this.crossings = crossings;
        }

        public List<Crossing2D> getCrossings(int level)
        {
            return new ArrayList<>(crossings.get(level));
        }

        public Crossing2D getNextCrossing(Crossing2D currentFinalCrossing)
        {            
            Crossing2D nextCrossing = null;

            for(Entry<Integer, List<Crossing2D>> crossingsForLevelEntry : crossings.entrySet())
            {
                List<Crossing2D> crossingsForLevel = crossingsForLevelEntry.getValue();
                if(crossingsForLevel.contains(currentFinalCrossing))
                {
                    int currentFinalCrossingIndex = crossingsForLevel.indexOf(currentFinalCrossing);
                    int nextCrossingIndex = (currentFinalCrossingIndex % 2 == 0) ? currentFinalCrossingIndex + 1 : currentFinalCrossingIndex - 1;
                    nextCrossing = (nextCrossingIndex < crossingsForLevel.size()) ? crossingsForLevel.get(nextCrossingIndex): currentFinalCrossing;
                }
            }      

            return nextCrossing;
        }

        public static CrossingsHierarchy buildInstance(SortedSet<Crossing2D> crossings)
        {
            int windingNumber = 0;

            MultiMap<Integer, Crossing2D> crossingsMap = new MultiMap<>();

            for(Iterator<Crossing2D> it = crossings.iterator();it.hasNext();)
            {
                Crossing2D crossing = it.next();
                int w = crossing.rightToLeft ? 1 : -1;
                int windingNumberNew = windingNumber + w;

                crossingsMap.put(Math.min(windingNumberNew, windingNumber), crossing);
                windingNumber = windingNumberNew;
            }

            return new CrossingsHierarchy(crossingsMap);
        }

        public Crossing2D getCrossing(int level, int index)
        {
            List<Crossing2D> crossingsForLevel = crossings.get(level);
            return crossingsForLevel.get(index);
        }
    }
}