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


package atomicJ.imageProcessing;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.statistics.L1Regression;
import atomicJ.statistics.L2Regression;


public enum LineFitRegressionStrategy 
{
    CLASSICAL_L2("Least squares")
    {   
        @Override
        public UnivariateFunction performRegression(double[] data, int deg)
        {
            return L2Regression.findFitedFunction(data, deg, true);
        }

        @Override
        public UnivariateFunction performRegression(double[][] data, int deg)
        {
            return L2Regression.findFitedFunction(data, deg, true);
        }

        @Override
        public UnivariateFunction[] performRegressionsOnEquispacedLines(double[][] dataSets, int columnCount, int deg) 
        {
            return L2Regression.performRegressionsOnEquispacedLines(dataSets, columnCount, deg);
        }
    },
    CLASSICAL_L1("Least absolute deviations")
    {   
        @Override
        public UnivariateFunction performRegression(double[]data, int deg)
        {
            return L1Regression.findFitFunction(data, deg, true);
        }

        @Override
        public UnivariateFunction performRegression(double[][] data, int deg)
        {
            return L1Regression.findFitFunction(data, deg, true);
        }

        @Override
        public UnivariateFunction[] performRegressionsOnEquispacedLines(double[][] dataSets,
                int columnCount, int deg) 
        {
            int n = dataSets.length;
            UnivariateFunction[] functions = new UnivariateFunction[n];

            for(int i = 0; i<n; i++)
            {
                functions[i] = L1Regression.findFitFunction(dataSets[i], deg, true);
            }

            return functions;
        }
    };

    private final String prettyName;

    LineFitRegressionStrategy(String prettyName)
    {
        this.prettyName = prettyName;
    }	

    public static LineFitRegressionStrategy getValue(String identifier)
    {
        LineFitRegressionStrategy strategy = null;

        if(identifier != null)
        {
            for(LineFitRegressionStrategy str : LineFitRegressionStrategy.values())
            {
                String estIdentifier =  str.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    strategy = str;
                    break;
                }
            }
        }


        return strategy;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public abstract UnivariateFunction performRegression(double[] data, int deg);

    public abstract UnivariateFunction[] performRegressionsOnEquispacedLines(double[][] dataSets, int columnCount, int deg);

    public abstract UnivariateFunction performRegression(double[][] data, int deg);

    public String getIdentifier()
    {
        return super.toString();
    }
}
