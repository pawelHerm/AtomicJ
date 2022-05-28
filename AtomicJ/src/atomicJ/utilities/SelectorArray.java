
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

import java.util.Comparator;

public class SelectorArray
{
    private static final int CUTOFF = 10;

    public SelectorArray() {}

    public static <T>  void sortSmallest(T[] array, int k, Comparator<T> comparator)
    {
        int n = array.length - 1;
        quickSelectSmallest(array, 0, n, k, comparator);
    }

    public static <T> void sortSmallest(T[] array, int low, int high, int k, Comparator<T> comparator)
    {
        quickSelectSmallest(array, low, high, k, comparator);
    }

    public static <T> void sortHighest(T[] array, int k, Comparator<T> comparator)
    {
        int n = array.length - 1;
        quickSelectHighest(array, 0, n, k, comparator);
    }

    public static <T> void sortHighest(T[] array, int low, int high, int k, Comparator<T> comparator)
    {
        quickSelectHighest(array, low, high, k, comparator);
    }

    private static <T> void quickSelectSmallest(T[] array, int low, int high, int k, Comparator<T> comparator) 
    {
        if(low + CUTOFF > high)
        {
            insertionSort(array, low, high, comparator);
        }
        else 
        {
            int i  = partition(array, low, high, comparator);

            // Recurse; only this part changes
            if( k <= i )
            {
                quickSelectSmallest(array, low, i - 1, k, comparator);

            }
            else if(k > i + 1)
            {
                quickSelectSmallest(array, i + 1, high, k, comparator);
            }
        }
    }

    private static <T> void quickSelectHighest(T[] array, int low, int high, int k, Comparator<T> comparator) 
    {
        if(low + CUTOFF > high)
        {
            insertionSort(array, low, high, comparator);
        }
        else 
        {
            int i  = partition(array, low, high, comparator);
            int r = (array.length - i);

            // Recurse; only this part changes
            if( k < r )
            {
                quickSelectHighest( array,  i + 1, high, k, comparator);

            }
            else if(k > r)
            {
                quickSelectHighest( array, low,  i - 1, k, comparator);
            }
        }
    }

    private static <T> int partition(T[] array, int low, int high, Comparator<T> comparator)
    {
        // Sort low, middle, high
        int middle = (low + high)/ 2;

        if(comparator.compare(array[middle], array[low]) < 0)
        {
            swap(array, low, middle);
        }
        if(comparator.compare(array[high], array[low]) < 0)
        {
            swap(array, low, high);
        }
        if(comparator.compare(array[high], array[middle]) < 0)
        {
            swap(array, middle, high);
        }

        // Place pivot at position high - 1
        swap(array, middle, high - 1);
        T pivot = array[high - 1];

        // Begin partitioning
        int i, j;
        for(i = low, j = high - 1; ;) 
        {
            while(comparator.compare(array[++i], pivot) < 0);
            while(comparator.compare(pivot,array[--j]) < 0);
            if(i >= j)
            {
                break;
            }
            swap(array, i, j);
        }

        // Restore pivot
        swap(array, i, high - 1);
        return i;
    }

    private static <T> void insertionSort(T[] array, int low, int high, Comparator<T> comparator) 
    {
        for(int p = low + 1; p <= high; p++ ) 
        {
            T tmp = array[p];
            int j;

            for(j = p; j > low && comparator.compare(tmp, array[j - 1]) < 0 ; j--)
            {
                array[j]= array[j - 1];
            }
            array[j] = tmp;
        }
    }

    private static <T> void swap(T[] array, int index1, int index2) 
    {
        T tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
    }
}