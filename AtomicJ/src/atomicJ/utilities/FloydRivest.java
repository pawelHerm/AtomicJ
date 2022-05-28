
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

public class FloydRivest {

    public static double select(double[] array, int k) {
        select(array, 0, array.length - 1, k);
        return array[k];
    }

    /**
     * @param array data array
     * @param left left index for the interval
     * @param right right index for the interval
     * @param k desired index value, where array[k] is the (k+1)th smallest element when left = 0
     */
    private static void select(double[] array, int left, int right, int k) {
        while (right > left)
        {
            // use select recursively to sample a smaller set of size s
            // the arbitrary constants 600 and 0.5 are used in the original
            // version to minimize execution time
            if (right - left > 600) 
            {
                final int n = right - left + 1;
                final int i = k - left + 1;
                final double z = Math.log(n);
                // 0.5 is used in the original version to minimize execution time
                final int s = (int)(0.5 * Math.exp(2 * z / 3));
                final int sd = (int)(0.5 * Math.sqrt(z * s * (n - s) / n) * Integer.signum(i - n / 2));
                final int newLeft = Math.max(left, (k - i * s / n + sd));
                final int newRight = Math.min(right, (k + (n - i) * s / n + sd));
                select(array, newLeft, newRight, k);
            }

            // partition the elements between left and right around pivot
            final double pivot = array[k];
            int i = left;
            int j = right;
            swap(array, left, k);

            if (array[right] > pivot) 
            {
                swap(array, right, left);
            }
            while (i < j) 
            {
                swap(array, i, j);
                while (array[++i] < pivot); //finds the least index i that array[i] >=  pivot . i.e. is greater than pivot, for all indices x smaller than i array[x] < pivot
                while (array[--j] > pivot);
            }
            if (array[left] == pivot) 
            {
                swap(array, left, j);

            } else {
                j++;
                swap(array, i, right);
            }
            // adjust left and right towards the boundaries of the subset
            // containing the (k - left + 1)th smallest element
            if (j <= k) {
                left = j + 1;
            }
            if (k <= j) {
                right = j - 1;
            }
        }
    }

    private static void swap(double[] a, int index1, int index2) 
    {
        double tmp = a[index1];
        a[index1] = a[index2];
        a[index2] = tmp;
    }
}