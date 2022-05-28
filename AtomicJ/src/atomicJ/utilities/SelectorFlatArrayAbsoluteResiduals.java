
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

package atomicJ.utilities;

import atomicJ.functions.FittedUnivariateFunction;

public class SelectorFlatArrayAbsoluteResiduals
{
    private static final int CUTOFF = 10;

    public SelectorFlatArrayAbsoluteResiduals() {}

    public static void sortSmallest(double[] arrayYs, double[] arrayXs, int k, FittedUnivariateFunction f)
    {
        int n = arrayYs.length - 1;
        quickSelectSmallest(arrayYs, arrayXs, 0, n, k, f);
    }

    public static void sortSmallest(double[] arrayYs, double[] arrayXs, int low, int high, int k, FittedUnivariateFunction f)
    {
        quickSelectSmallest(arrayYs, arrayXs, low, high, k, f);
    }

    public static void sortHighest(double[] arrayYs, double[] arrayXs, int k, FittedUnivariateFunction f)
    {
        int n = arrayYs.length - 1;
        quickSelectHighest(arrayYs, arrayXs, 0, n, k, f);
    }

    public static void sortHighest(double[] arrayYs, double[] arrayXs, int low, int high, int k, FittedUnivariateFunction f)
    {
        quickSelectHighest(arrayYs, arrayXs, low, high, k, f);
    }

    private static void quickSelectSmallest(double[] arrayYs, double[] arrayXs, int low, int high, int k, FittedUnivariateFunction f) 
    {
        if(low + CUTOFF > high)
        {
            insertionSort(arrayYs, arrayXs, low, high, f);
        }
        else 
        {
            int i  = partition(arrayYs, arrayXs, low, high, f);

            // Recurse; only this part changes
            if( k <= i )
            {
                quickSelectSmallest(arrayYs, arrayXs, low, i - 1, k, f);

            }
            else if(k > i + 1)
            {
                quickSelectSmallest(arrayYs, arrayXs, i + 1, high, k, f);
            }
        }
    }

    private static void quickSelectHighest(double[] arrayYs, double[] arrayXs, int low, int high, int k, FittedUnivariateFunction f) 
    {
        if(low + CUTOFF > high)
        {
            insertionSort(arrayYs, arrayXs, low, high, f);
        }
        else 
        {
            int i  = partition(arrayYs, arrayXs, low, high, f);
            int r = (arrayYs.length - i);

            // Recurse; only this part changes
            if( k < r )
            {
                quickSelectHighest(arrayYs, arrayXs,  i + 1, high, k, f);

            }
            else if(k > r)
            {
                quickSelectHighest(arrayYs, arrayXs, low,  i - 1, k, f);
            }
        }
    }

    private static int partition(double[] arrayYs, double[] arrayXs, int low, int high, FittedUnivariateFunction f)
    {
        // Sort low, middle, high
        int middle = (low + high)/ 2;

        if(Math.abs(f.residual(arrayXs[middle], arrayYs[middle])) < Math.abs(f.residual(arrayXs[low],arrayYs[low])))
        {
            swap(arrayYs, arrayXs, low, middle);
        }
        if(Math.abs(f.residual(arrayXs[high], arrayYs[high])) < Math.abs(f.residual(arrayXs[low],arrayYs[low])))
        {
            swap(arrayYs, arrayXs, low, high);
        }
        if(Math.abs(f.residual(arrayXs[high],arrayYs[high])) < Math.abs(f.residual(arrayXs[middle],arrayYs[middle])))
        {
            swap(arrayYs, arrayXs, middle, high);
        }

        // Place pivot at position high - 1
        swap(arrayYs, arrayXs, middle, high - 1);
        int pivotIndex = high - 1;

        // Begin partitioning
        int i, j;
        for(i = low, j = high - 1; ;) 
        {
            while(((Math.abs(getResidual(arrayXs, arrayYs, ++i, f))) < Math.abs(getResidual(arrayXs,arrayYs,pivotIndex,f))));
            while(Math.abs(getResidual(arrayXs, arrayYs, pivotIndex, f)) < Math.abs(getResidual(arrayXs, arrayYs, --j, f)));
            if(i >= j)
            {
                break;
            }
            swap(arrayYs, arrayXs, i, j);
        }

        // Restore pivot
        swap(arrayYs, arrayXs, i, high - 1);
        return i;
    }

    private static double getResidual(double[] xs, double[] ys, int index, FittedUnivariateFunction f)
    {
        return f.residual(xs[index], ys[index]);
    }

    private static void insertionSort(double[] arrayYs, double[] arrayXs, int low, int high, FittedUnivariateFunction f) 
    {
        for(int p = low + 1; p <= high; p++ ) 
        {
            double tempX = arrayXs[p];
            double tempY = arrayYs[p];
            int j;

            for(j = p; j > low && f.residual(tempX,tempY) < f.residual(arrayXs[j - 1],arrayYs[j - 1]) ; j--)
            {
                arrayXs[j]= arrayXs[j - 1];
                arrayYs[j]= arrayYs[j - 1];
            }
            arrayXs[j] = tempX;
            arrayYs[j] = tempY;
        }
    }

    private static void swap(double[] arrayYs, double[] arrayXs, int index1, int index2) 
    {
        double tmpX = arrayXs[index1];
        double tmpY = arrayYs[index1];
        arrayXs[index1] = arrayXs[index2];
        arrayYs[index1] = arrayYs[index2];
        arrayXs[index2] = tmpX;
        arrayYs[index2] = tmpY;

    }
}