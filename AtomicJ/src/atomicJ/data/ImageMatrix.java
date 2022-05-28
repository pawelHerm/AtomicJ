package atomicJ.data;

import java.awt.geom.Point2D;

import org.apache.commons.math.analysis.BivariateRealFunction;
import org.jfree.data.Range;

import atomicJ.data.units.Quantity;


public interface ImageMatrix
{
    public double[][] getData();
    public Grid2D getGrid();
    public double getValue(Point2D dataPoint);
    public Range getZRange();
    public BivariateRealFunction getBicubicSplineInterpolationFunction();
    public Quantity getZQuantity();
}