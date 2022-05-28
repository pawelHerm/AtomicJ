package atomicJ.data;


import java.awt.geom.Point2D;
import java.lang.ref.SoftReference;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.BivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.BicubicSplineInterpolator;
import org.jfree.data.Range;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.utilities.ArrayUtilities;


public class SimpleDataMatrix implements ImageMatrix
{
    private final double[][] data;
    private final Grid2D grid;
    private final Quantity zQuantity;
    private Range zRange;

    private SoftReference<BivariateRealFunction> interpolationFunction;

    public SimpleDataMatrix(double[][] data, Grid2D grid)
    { 
        this(data, grid, UnitQuantity.NULL_QUANTITY);
    }

    public SimpleDataMatrix(double[][] data, Grid2D grid, Quantity zQuantity)
    {
        this.data = data;
        this.grid = grid;
        this.zQuantity = zQuantity;
    }

    @Override
    public double[][] getData()
    {
        return data;
    }

    @Override
    public Grid2D getGrid()
    {
        return grid;
    }

    @Override
    public double getValue(Point2D dataPoint) 
    {
        int row = grid.getRow(dataPoint);
        int column = grid.getColumn(dataPoint);

        double value = Double.NaN;

        if (row < grid.getRowCount() && column < grid.getColumnCount())
        {
            value = data[row][column];
        }

        return value;
    }

    @Override
    public Range getZRange() 
    {
        if(zRange == null)
        {
            zRange = ArrayUtilities.getBoundedRange(data);
        }
        return zRange;
    }

    public boolean isInterpolationFunctionBuilt()
    {
        boolean built = (interpolationFunction != null);
        return built;
    }

    public void buildBicubicSplineInterpolationFunctionIfNecessary()
    {
        if(interpolationFunction == null)
        {
            buildBicubicSplineInterpolationFunction();
        }
    }

    private boolean buildBicubicSplineInterpolationFunction() 
    {                 
        double[][] transposedData = ArrayUtilities.transpose(data, grid.getRowCount(), grid.getColumnCount());

        interpolationFunction = null;

        if (transposedData != null) 
        {
            BicubicSplineInterpolator interpolator = new BicubicSplineInterpolator();

            try 
            {
                interpolationFunction = new SoftReference<>(interpolator.interpolate(grid.getNodeXs(), grid.getNodeYs(), transposedData));

                return true;
            } 
            catch (IllegalArgumentException | MathException e) 
            {
                e.printStackTrace();
            }

        } 
        return false;
    }

    @Override
    public Quantity getZQuantity()
    {
        return zQuantity;
    }

    @Override
    public BivariateRealFunction getBicubicSplineInterpolationFunction() 
    {
        buildBicubicSplineInterpolationFunctionIfNecessary();
        return interpolationFunction.get();
    }
}