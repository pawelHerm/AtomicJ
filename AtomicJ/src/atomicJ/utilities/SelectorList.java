
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
import java.util.List;

public class SelectorList
{
    private static final int CUTOFF = 10;

    public SelectorList() {}

    public static <T>  void sortSmallest(List<T> list, int k, Comparator<T> comparator)
    {
        int n = list.size() - 1;
        quickSelectSmallest(list, 0, n, k, comparator);
    }

    public static <T> void sortSmallest(List<T> list, int low, int high, int k, Comparator<T> comparator)
    {
        quickSelectSmallest(list, low, high, k, comparator);
    }

    public static <T> void sortHighest(List<T> list, int k, Comparator<T> comparator)
    {
        int n = list.size() - 1;
        quickSelectHighest(list, 0, n, k, comparator);
    }

    public static <T> void sortHighest(List<T> list, int low, int high, int k, Comparator<T> comparator)
    {
        quickSelectHighest(list, low, high, k, comparator);
    }

    private static <T> void quickSelectSmallest(List<T> list, int low, int high, int k, Comparator<T> comparator) 
    {
        if(low + CUTOFF > high)
        {
            insertionSort(list, low, high, comparator);
        }
        else 
        {
            int i  = partition(list, low, high, comparator);

            // Recurse; only this part changes
            if( k <= i )
            {
                quickSelectSmallest(list, low, i - 1, k, comparator);

            }
            else if(k > i + 1)
            {
                quickSelectSmallest(list, i + 1, high, k, comparator);
            }
        }
    }

    private static <T> void quickSelectHighest(List<T> list, int low, int high, int k, Comparator<T> comparator) 
    {
        if(low + CUTOFF > high)
        {
            insertionSort(list, low, high, comparator);
        }
        else 
        {
            int i  = partition(list, low, high, comparator);
            int r = (list.size() - i);

            // Recurse; only this part changes
            if( k < r )
            {
                quickSelectHighest( list,  i + 1, high, k, comparator);

            }
            else if(k > r)
            {
                quickSelectHighest( list, low,  i - 1, k, comparator);
            }
        }
    }

    private static <T> int partition(List<T> list, int low, int high, Comparator<T> comparator)
    {
        // Sort low, middle, high
        int middle = (low + high)/ 2;

        if(comparator.compare(list.get(middle), list.get(low)) < 0)
        {
            swap(list, low, middle);
        }
        if(comparator.compare(list.get(high), list.get(low)) < 0)
        {
            swap(list, low, high);
        }
        if(comparator.compare(list.get(high), list.get(middle)) < 0)
        {
            swap(list, middle, high);
        }

        // Place pivot at position high - 1
        swap(list, middle, high - 1);
        T pivot = list.get(high - 1);

        // Begin partitioning
        int i, j;
        for(i = low, j = high - 1; ;) 
        {
            while(comparator.compare(list.get(++i), pivot) < 0);
            while(comparator.compare(pivot,list.get(--j)) < 0);
            if(i >= j)
            {
                break;
            }
            swap(list, i, j);
        }

        // Restore pivot
        swap(list, i, high - 1);
        return i;
    }

    private static <T> void insertionSort(List<T> a, int low, int high, Comparator<T> comparator) 
    {
        for(int p = low + 1; p <= high; p++ ) 
        {
            T tmp = a.get(p);
            int j;

            for(j = p; j > low && comparator.compare(tmp, a.get(j - 1)) < 0 ; j--)
            {
                a.set(j, a.get(j - 1));
            }
            a.set(j, tmp);
        }
    }

    private static <T> void swap(List<T> a, int index1, int index2) 
    {
        T tmp = a.get(index1);
        a.set(index1, a.get(index2));
        a.set(index2, tmp);
    }
}