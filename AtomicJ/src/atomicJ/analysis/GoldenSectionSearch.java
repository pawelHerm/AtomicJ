package atomicJ.analysis;

import org.apache.commons.math3.analysis.UnivariateFunction;


public class GoldenSectionSearch 
{
    private final UnivariateFunction f;  

    public GoldenSectionSearch(UnivariateFunction f)
    {
        this.f = f;
    }

    private final double phi = (1 + Math.sqrt(5)) / 2;
    private final double resphi = 2 - phi;

    // a and c are the current bounds; the minimum is between them.
    // b is a center point
    // f(x) is some mathematical function elsewhere defined
    // a corresponds to x1; b corresponds to x2; c corresponds to x3
    // x corresponds to x4

    private double goldenSectionSearchInternal(double left, double center, double right, double tau, double fcenter)
    {
        double x;
        if (right - center > center - left)
        {
            x = center + resphi * (right - center);
        }
        else
        {
            x = center - resphi * (center - left); 
        }
        if (Math.abs(right - left) < tau) 
        {
            return (right + left) / 2.; 
        }

        double fx = f.value(x);
        if (fx < fcenter) 
        {
            if (center < x)
            {
                return goldenSectionSearchInternal(center, x, right, tau, fx);
            }
            else 
            {
                return goldenSectionSearchInternal(left, x, center, tau, fx);
            }
        }
        else if (center < x)
        {
            return goldenSectionSearchInternal(left, center, x, tau, fcenter);
        }
        else 
        {
            return goldenSectionSearchInternal(x, center, right, tau, fcenter);
        }
    }      


    public double goldenSectionSearch(double a, double b, double c, double tau)
    {   
        double fb = f.value(b);

        return goldenSectionSearchInternal(a, b, c, tau, fb);
    }
}
