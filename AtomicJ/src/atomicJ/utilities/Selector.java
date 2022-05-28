
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2020 by Pawe³ Hermanowicz
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

public class Selector 
{
    private static final int CUTOFF = 10;

    private Selector() {}

    public static void sortSmallest(double[] array, int k)
    {
        int n = array.length - 1;
        quickSelectSmallest(array, 0, n, k);
    }

    public static void sortSmallest(double[] array, int low, int high, int k)
    {
        quickSelectSmallest(array, low, high, k);
    }

    public static void sortHighest(double[] array, int k)
    {
        int n = array.length - 1;
        quickSelectHighest(array, 0, n, k);
    }

    public static void sortHighest(double[] array, int low, int high, int k)
    {
        quickSelectHighest(array, low, high, k);
    }

    private static void quickSelectSmallest(double[] array, int low, int high, int k) 
    {
        if(low + CUTOFF > high)
        {
            insertionSort(array, low, high);
        }
        else 
        {
            int i = partition(array, low, high);

            if(k <= i)
            {
                quickSelectSmallest(array, low, i - 1, k);
            }
            else if(k > i + 1)
            {
                quickSelectSmallest(array, i + 1, high, k);
            }
        }
    }

    private static void quickSelectHighest(double[] array, int low, int high, int k) 
    {
        if(low + CUTOFF > high)
        {
            insertionSort(array, low, high);
        }
        else 
        {
            int i  = partition(array, low, high);
            int r = (array.length - i);

            // Recurse; only this part changes
            if(k < r)
            {
                quickSelectHighest(array,  i + 1, high, k);

            }
            else if(k > r)
            {
                quickSelectHighest(array, low,  i - 1, k);
            }
        }
    }

    private static int partition(double[] array, int low, int high)
    {
        if(low == high)
        {
            return low;
        }

        // Sort low, middle, high
        int middle = (low + high)/ 2;

        if(array[middle]<array[low])
        {
            swap(array, low, middle);
        }
        if(array[high] < array[low])
        {
            swap(array, low, high);
        }
        if(array[high] < array[middle])
        {
            swap(array, middle, high);
        }

        // Place pivot at position high - 1
        swap(array, middle, high - 1);
        double pivot = array[high - 1];

        // Begin partitioning
        int i = low, j = high - 1;
        while(true)
        {
            while(array[++i] < pivot); //finds the smallest index i that array[i] >=  pivot . i.e. is greater than pivot
            // for all indices x smaller than i array[x] < pivot
            // because pivot <= array[high], so this while loop is bound to finish
            while(array[--j] > pivot); //find the greatest index j such that array[j] <= pivot, i.e. is smaller than pivot
            //pivot >= array[low], so this while loop is bound to finish
            if(i >= j) //if i == j, then array[i] = pivot
                // if i > j, then the partition is already done
            {
                break;
            }
            swap(array, i, j); //array[i] >= array[j] and i < j so they have to be swapped
        }

        // Restore pivot
        swap(array, i, high - 1); // the value array[i] is equal or larger than pivot == array[high - 1], so it swaps 
        // positions with pivot
        return i; // returns the index i such that for all k < i it holds that array[k] =< array[i]
        // and for all k>i it  holds that array[k] >= array[i]
    }



    private static void insertionSort(double[] a, int low, int high) 
    {
        for(int p = low + 1; p <= high; p++) 
        {
            double tmp = a[p];
            int j;

            for(j = p; j > low && tmp<a[j - 1]; j--)
            {
                a[j] = a[j - 1];
            }
            a[j] = tmp;
        }
    }

    private static void swap(double[] a, int index1, int index2) 
    {
        double tmp = a[index1];
        a[index1] = a[index2];
        a[index2] = tmp;
    }
}