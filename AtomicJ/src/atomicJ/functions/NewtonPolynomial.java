
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.functions;

import java.util.Arrays;

public final class NewtonPolynomial
{
    private final double[] constants;
    private final double[] centers;

    public NewtonPolynomial(double[] xs, double[] ys)
    {
        int n = xs.length - 1;
        double[] constants = Arrays.copyOf(ys, n + 1);

        for(int k = 1;k<=n;k++)
        {
            for(int j = n;j>=k;j--)
            {
                constants[j] = (constants[j] - constants[j - 1])/(xs[j]-xs[j - k]);
            }
        }
        this.constants = constants;
        this.centers = xs;
    }

    public double value(double x)
    {
        int n = constants.length - 1;
        double v = constants[n];
        for(int i = n - 1;i>=0;i--)
        {
            double a = constants[i];
            double c = centers[i];
            v = v*(x - c) + a;
        }
        return v;
    }

    public double residual(double[] p)
    {
        double x = p[0];
        double y = p[1];
        double r = y - value(x);
        return r;
    }
}
