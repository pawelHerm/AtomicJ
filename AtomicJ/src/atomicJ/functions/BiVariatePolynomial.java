
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

import atomicJ.statistics.BivariateFunction;

public final class BiVariatePolynomial implements BivariateFunction
{
    private final Polynomial polynomialX;
    private final Polynomial polynomialY;

    private BiVariatePolynomial(Polynomial polynomialX, Polynomial polynomialY)
    {
        this.polynomialX = polynomialX;
        this.polynomialY = polynomialY;
    }

    public BiVariatePolynomial(double[] coefficientsX, double[] coefficientsY)
    {
        polynomialX = new Polynomial(coefficientsX);
        polynomialY = new Polynomial(coefficientsY);
    }

    @Override
    public BiVariatePolynomial multiply(double k)
    {
        BiVariatePolynomial polNew = new BiVariatePolynomial(this.polynomialX.multiply(k), this.polynomialY.multiply(k));

        return polNew;
    }

    @Override
    public double value(double x, double y)
    {
        double result = polynomialX.value(x) + polynomialY.value(y);

        return result;
    }

    public Polynomial getYPolynomial()
    {
        return polynomialY;
    }

    public Polynomial getXPolynomial()
    {
        return polynomialX;
    }

    @Override
    public boolean isZero()
    {
        if(!polynomialX.isZero())
        {
            return false;
        }

        if(!polynomialY.isZero())
        {
            return false;
        }

        return true;
    }
}