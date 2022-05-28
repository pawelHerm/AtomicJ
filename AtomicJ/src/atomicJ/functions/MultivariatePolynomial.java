
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

import java.util.ArrayList;
import java.util.List;

import atomicJ.statistics.MultivariateFittedFunction;


public final class MultivariatePolynomial implements MultivariateFittedFunction
{
    private final List<Polynomial> polynomials = new ArrayList<>();

    private MultivariatePolynomial()
    {}

    public MultivariatePolynomial(List<double[]> coefficients)
    {
        for(double[] coeff: coefficients)
        {
            Polynomial pol = new Polynomial(coeff);
            polynomials.add(pol);
        }
    }

    @Override
    public MultivariatePolynomial multiply(double k)
    {
        MultivariatePolynomial polNew = new MultivariatePolynomial();
        for(Polynomial pol : polynomials)
        {
            polNew.polynomials.add(pol.multiply(k));
        }

        return polNew;
    }

    @Override
    public double value(double[] x)
    {
        double result = 0;

        for(int i = 0; i<x.length; i++)
        {
            Polynomial pol = polynomials.get(i);
            result += pol.value(x[i]);
        }
        return result;
    }

    @Override
    public double residual(double[] p)
    {
        int varCount = p.length - 1;
        double response = p[varCount];
        double[] predictor = new double[varCount];

        for(int i = 0; i<varCount; i++)
        {
            predictor[i] = p[i];
        }
        double r = response - value(predictor);
        return r;
    }

    @Override
    public boolean isZero()
    {
        for(Polynomial p : polynomials)
        {
            if(!p.isZero())
            {
                return false;
            }
        }

        return true;
    }
}