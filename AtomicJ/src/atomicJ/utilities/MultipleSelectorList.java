
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

import java.util.List;

public class MultipleSelectorList 
{

    private MultipleSelectorList() {}

    public static void sortSmallest(List<Double> array, int[] ranks)
    {
        int n = array.size() - 1;
        int top = ranks.length - 1;
        quickSelectSmallest(array, 0, n, ranks, 0, top);
    }

    private static void quickSelectSmallest(List<Double> array, int low, int high, int[] ranks, int bottom, int top) 
    {
        int i  = partition(array, low, high);

        int topLeft = findLargestIndexOfValueSmallerThanKey(ranks, bottom, top, i);
        int bottomRight = findSmallestIndexOfValueGreaterThanKey(ranks, bottom, top, i);

        if(topLeft > -1)
        {
            quickSelectSmallest(array, low, i - 1, ranks, bottom, topLeft);
        }

        if(bottomRight > -1)
        {
            quickSelectSmallest(array, i + 1, high, ranks, bottomRight, top);
        }
    }

    private static int findLargestIndexOfValueSmallerThanKey(int[] ranks, int bottom, int top, int key)
    {
        int index  = -1;

        for(int i = bottom; i <= top; i++)
        {
            if(ranks[i] < key)
            {
                index = i;
            }
            else
            {
                break;
            }
        }
        return index;       
    }

    private static int findSmallestIndexOfValueGreaterThanKey(int[] ranks, int bottom, int top, int key)
    {
        int index  = -1;

        for(int i = top; i >= bottom; i--)
        {
            if(ranks[i] > key)
            {
                index = i;
            }
            else
            {
                break;
            }
        }
        return index;       
    }

    private static int partition(List<Double> array, int low, int high)
    {
        if(low == high)
        {
            return low;
        }

        if(high - low == 1)
        {
            if(array.get(high)< array.get(low))
            {
                swap(array, low, high);
            }

            return low;
        }


        // Ensures that array[low] =< array[middle] =< array[high]
        //the pivot is the median of these three values
        int middle = (low + high)/ 2;

        if(array.get(middle) < array.get(low))
        {
            swap(array, low, middle);
        }
        if(array.get(high)< array.get(low))
        {
            swap(array, low, high);
        }
        if(array.get(high)< array.get(middle))
        {
            swap(array, middle, high);
        }

        // Place pivot at position high - 1
        swap(array, middle, high - 1);
        double pivot = array.get(high - 1);

        // Begin partitioning
        int i, j;
        for(i = low, j = high - 1; ;) 
        {
            while(array.get(++i)<pivot); //finds the largest index i that array[i] >=  pivot . i.e. is greater than pivot
            // for all indices x smaller than i array[x] < pivot
            // because pivot <= array[high], so this while loop is bound to finish
            while(pivot < array.get(--j)); //find the smallest index j that array[j] =< pivot, i.e. is smaller than pivot
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
        return i; // returns the index i such that for all x < i array[x] =< array[j]
        // and for all x>i array[x] >= array[j]
    }

    private static void swap(List<Double> a, int index1, int index2) 
    {
        double tmp = a.get(index1);
        a.set(index1, a.get(index2));
        a.set(index2, tmp);
    }
}