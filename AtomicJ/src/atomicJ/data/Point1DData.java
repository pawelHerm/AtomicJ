package atomicJ.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jfree.data.Range;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.OrderedIntegerPair;
import atomicJ.utilities.Validation;

public class Point1DData implements Channel1DData
{
    private final double x;
    private final double y;

    private final Quantity xQuantity;
    private final Quantity yQuantity;

    public Point1DData(double x, double y, Quantity xQuantity, Quantity yQuantity)
    {
        this.x = x;
        this.y = y;
        this.xQuantity = xQuantity;
        this.yQuantity = yQuantity;
    }

    public Point1DData(Point1DData that)
    {
        this.x = that.x;
        this.y = that.y;
        this.xQuantity = that.xQuantity;
        this.yQuantity = that.yQuantity;
    }

    @Override
    public Quantity getXQuantity()
    {
        return xQuantity;
    }

    @Override
    public Quantity getYQuantity()
    {
        return yQuantity;
    }

    @Override
    public Point1DData getCopy()
    {
        return new Point1DData(this);
    }

    @Override
    public Point1DData getCopy(double scale)
    {
        return getCopy(scale, this.yQuantity);
    }

    @Override
    public Point1DData getCopy(double yScale, Quantity yQuantityNew)
    {
        Point1DData copy = new Point1DData(x, y*yScale, this.xQuantity, yQuantityNew);

        return copy;
    }


    @Override
    public Channel1DData getCopyWithXQuantityReplaced(double xScale, Quantity xQuantityNew)
    {
        Point1DData copy = new Point1DData(xScale*x, y, xQuantityNew, this.yQuantity);

        return copy;
    }

    @Override
    public Channel1DData getCopyWithXAndYQuantitesReplaced(double xScale, Quantity xQuantityNew, double yScale, Quantity yQuantityNew)
    {
        Point1DData copy = new Point1DData(xScale*x, yScale*y, xQuantityNew, yQuantityNew);
        return copy;
    }

    @Override
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x)
    {
        return 0;
    }

    @Override
    public double[][] getPoints() 
    {
        return new double[][] {{x, y}};
    }

    @Override
    public double[][] getPointsCopy()
    {
        return getPoints();
    }

    @Override
    public double[][] getPointsCopy(int from, int to) 
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, 1, "from");//we allow from to be equal to the length of the array, in such a case, the returned array is empty
        Validation.requireValueEqualToOrBetweenBounds(to, 0, 0, "to");

        int n = to - from;

        double[][] copy = n > 0 ? new double[][] {{x,y}} : new double[][] {};

        return copy;
    }

    @Override
    public double[][] getPointsCopy(double conversionFactor) 
    {
        return new double[][] {{x, conversionFactor*y}};

    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public int getItemCount()
    {
        return 1;
    }

    public double getX()
    {
        return x;
    }

    @Override
    public double getX(int item)
    {
        if(item == 0)
        {
            return x;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public double getXMinimum()
    {        
        return x;
    }

    @Override
    public double getXMaximum()
    {
        return x;
    }

    @Override
    public Range getXRange()
    {        
        return new Range(x,x);
    }

    @Override
    public double getYMinimum()
    {
        return y;
    }

    @Override
    public int getYMinimumIndex(int from, int to) 
    {
        return 0;
    }

    @Override
    public double getYMaximum()
    {
        return y;
    }

    @Override
    public int getYMaximumIndex(int from, int to)
    {
        return 0;
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema()
    {
        return new OrderedIntegerPair(0, 0);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to)
    {
        return new OrderedIntegerPair(0, 0);
    }

    @Override
    public Range getYRange()
    {
        return new Range(y, y);
    }

    @Override
    public Range getYRange(Range xRange)
    {
        Range yRange = xRange.contains(x) ? new Range(y, y) : null;
        return yRange;
    }

    @Override
    public int getIndexOfGreatestXSmallerOrEqualTo(double upperBound)
    {
        int index = this.x <= upperBound ? 0 : -1;
        return index;
    }

    @Override
    public int getIndexOfSmallestXGreaterOrEqualTo(double lowerBound)
    {
        int index = this.x >= lowerBound ? 0 : -1;
        return index;
    }

    @Override
    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        int lowerBoundIndex = getIndexOfSmallestXGreaterOrEqualTo(lowerBound);
        int upperBoundIndex = getIndexOfGreatestXSmallerOrEqualTo(upperBound);

        IndexRange range = SortedArrayOrder.ASCENDING.equals(order) ? new IndexRange(lowerBoundIndex, upperBoundIndex) : new IndexRange(upperBoundIndex, lowerBoundIndex);

        return range;
    }

    @Override
    public int getIndexCountBoundedBy(double lowerBound, double upperBound)
    {
        int count = this.x >= lowerBound && this.x <= upperBound ? 1 : 0;
        return count;
    }

    @Override
    public double getY(int item)
    {
        if(item == 0)
        {
            return y;
        }
        throw new IndexOutOfBoundsException();
    }

    public double getY()
    {
        return y;
    }

    @Override
    public double[] getPoint(int item)
    {
        if(item == 0)
        {
            return new double[] {x, y}; 
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public SortedArrayOrder getXOrder()
    {
        return null;
    }

    @Override
    public double[] getXCoordinates() 
    {
        return new double[] {x};
    }

    @Override
    public double[] getXCoordinatesCopy() 
    {
        return new double[] {x};
    }

    @Override
    public double[] getXCoordinatesCopy(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, 1, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, 1, "to");

        double[] yCoordinates = (from == 0 && to > 0) ? new double[] {x} : new double[] {};

        return yCoordinates;
    }

    @Override
    public double[] getYCoordinates()
    {
        return new double[] {y};
    }

    @Override
    public double[] getYCoordinatesCopy()
    {
        return new double[] {y};
    }

    @Override
    public double[] getYCoordinatesCopy(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, 1, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, 1, "to");

        double[] yCoordinates = (from == 0 && to > 0) ? new double[] {y} : new double[] {};

        return yCoordinates;
    }

    @Override
    public double[][] getXYViewCopy()
    {
        return new double[][] {{x}, {y}};
    }

    @Override
    public double[][] getXYViewCopy(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, 1, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, 1, "to");

        return new double[][] {{x}, {y}};
    }

    public Map<String, QuantitativeSample> getSamples(String name)
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        QuantitativeSample approachXsSample = new StandardSample(new double[] {x}, name + " X", getXQuantity().changeName(name + " X"));
        QuantitativeSample approachYsSample = new StandardSample(new double[] {y}, name + " Y", getYQuantity().changeName(name + " Y"));

        samples.put(name + " X", approachXsSample );
        samples.put(name + " Y", approachYsSample );

        return samples;
    }


    @Override
    public double getXMinimumIncludingErrors() {
        return getXMinimum();
    }

    @Override
    public double getXMaximumIncludingErrors() {
        return getXMaximum();
    }

    @Override
    public double getYMinimumIncludingErrors() {
        return getYMinimum();
    }

    @Override
    public double getYMaximumIncludingErrors() {
        return getYMaximum();
    }

    @Override
    public Range getYRangeIncludingErrors(Range xRange)
    {
        return getYRange(xRange);
    }
}

