
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

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jfree.data.Range;
import atomicJ.statistics.DescriptiveStatistics;

public class ArrayUtilities 
{        
    public static double[][] convertUInt8ToDouble(byte[][] originalArray)
    {
        double[][] convertedArray = new double[originalArray.length][];

        for(int i = 0; i<originalArray.length; i++)
        {
            byte[] row = originalArray[i];
            double[] convertedRow = new double[row.length];

            for(int j = 0; j<row.length; j++)
            {
                convertedRow[j] = row[j] & 0x000000ff;
            }         

            convertedArray[i] = convertedRow;
        }

        return convertedArray;
    }

    public static double[][] convertUInt16ToDouble(short[][] originalArray)
    {
        double[][] converted = new double[originalArray.length][];

        for(int i = 0; i<originalArray.length; i++)
        {
            short[] originalRow = originalArray[i];
            double[] convertedRow = new double[originalRow.length];

            for(int j = 0; j<originalRow.length; j++)
            {
                convertedRow[j] = originalRow[j] & 0x0000ffff;
            }  

            converted[i] = convertedRow;
        }

        return converted;
    }

    public ArrayIndex getFirstIndexOf(Object[][] array, Object element)
    {
        if(element == null)
        {
            return null;
        }
        int n = array.length;

        for(int i = 0; i<n; i++)
        {
            Object[] row= array[i];
            int m = row.length;

            for(int j = 0; j<m; j++)
            {
                Object e = row[j];
                if(element.equals(e))
                {
                    ArrayIndex index = new ArrayIndex(i, j);
                    return index;
                }
            }
        }
        return null;
    }

    //operation performed in place
    public static void negateXs(double[][] data)
    {
        int n = data.length;
    
        for(int i = 0; i<n; i++)
        {
            double[] p = data[i];
            double x = p[0];
            p[0] = -x;
        }
    }

    //operation performed in place
    public static void negate(double[] data)
    {
        int n = data.length;
    
        for(int i = 0; i<n; i++)
        {
            double x = data[i];
            data[i] = -x;
        }
    }

    public static void negateAndShiftXs(double[][] data, double shift)
    {
        int n = data.length;
    
        for(int i = 0; i<n; i++)
        {
            double[] p = data[i];
            double x = p[0];
            p[0] = -x + shift;
        }
    }

    public static boolean containsAtLeastNNonZero(double[] array, int n, double tolerance)
    {
        int count = 0;

        for(double r : array)
        {
            count = !MathUtilities.equalWithinTolerance(0, r, tolerance) ? count + 1 : count;
            if(count >= n)
            {
                return true;
            }
        }

        return false;
    }

    public static boolean allElementsEqual(double[] array, double value, double tolerance)
    {
        for(double r : array)
        {
            if(!MathUtilities.equalWithinTolerance(r, value, tolerance))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean allElementsEqual(boolean[] array, boolean value)
    {
        for(boolean b : array)
        {
            if(b != value)
            {
                return false;
            }
        }

        return true;
    }

    public static boolean allElementsEqual(boolean[][] matrix, boolean value)
    {
        for(boolean[] row : matrix)
        {
            for(boolean b : row)
            {
                if(b != value)
                {
                    return false;
                }
            }
        }

        return true;
    }

    //trims endpoints that are more distant from the next point than ratioTolerance times the mean of k most left  or the mean of k most right intervals between successive points, excluding endpoints
    //if the array's length is smaller than k + 3, the method returns original array
    public static double[][] trimIsolatedEndPoints(double[][] points, int k, double ratioTolerance)
    {
        if(points == null || points.length < (k + 3))
        {
            return points;
        }

        int n = points.length;
        double leftEndPointDiff = Math.abs(points[1][0] - points[0][0]);

        double[] absLeftDifferences = new double[k];

        for(int i = 1; i<k + 1; i++)
        {
            absLeftDifferences[i - 1] = Math.abs(points[i + 1][0] - points[i][0]);
        }

        int firstGoodIndex = leftEndPointDiff/DescriptiveStatistics.arithmeticMean(absLeftDifferences) < ratioTolerance ? 0 : 1;

        double rightEndPointDiff = Math.abs(points[n - 1][0] - points[n - 2][0]);

        double[] absRightDifferences = new double[k];

        for(int i = 1; i<k + 1; i++)
        {
            absRightDifferences[i - 1] = Math.abs(points[n - i - 1][0] - points[n - i - 2][0]);
        }

        int lastGoodIndex = rightEndPointDiff/DescriptiveStatistics.arithmeticMean(absRightDifferences) < ratioTolerance ? n - 1 : n - 2;

        boolean trimmingNecessary = firstGoodIndex > 0 || lastGoodIndex < n - 1;

        double[][] trimmed = trimmingNecessary ? Arrays.copyOfRange(points, firstGoodIndex, lastGoodIndex + 1) : points;

        return trimmed; 
    }

    public static double[][] trimXRepetitionsIfNecessary(double[][] points, double tolerance)
    {
        if(points == null || points.length < 2)
        {
            return points;
        }

        int n = points.length;

        int firstGoodIndex = 0;

        double firstX = points[0][0];
        double secondX = points[1][0];

        if(MathUtilities.equalWithinTolerance(firstX,secondX,tolerance))
        {
            for(int i = 2; i<n; i++)
            {
                double x = points[i][0];
                boolean equal = MathUtilities.equalWithinTolerance(x, secondX, tolerance);

                if(!equal)
                {
                    firstGoodIndex = i;
                    break;
                }          
            }
        }

        int lastGoodIndex = n - 1;

        double lastX = points[n - 1][0];
        double beforeLastX = points[n - 2][0];

        if(MathUtilities.equalWithinTolerance(beforeLastX,lastX,tolerance))
        {
            for(int i = n - 3; i >= 0; i--)
            {
                double x = points[i][0];
                boolean equal = MathUtilities.equalWithinTolerance(x, beforeLastX, tolerance);

                if(!equal)
                {
                    lastGoodIndex = i;
                    break;
                }          
            }
        }      

        boolean trimmingNecessary = firstGoodIndex > 0 || lastGoodIndex < n - 1;

        double[][] trimmed = trimmingNecessary ? Arrays.copyOfRange(points, firstGoodIndex, lastGoodIndex + 1) : points;

        return trimmed;
    }

    //gives the first element of the array sorted in descending order which is lower then key, or hi if all elements are larger
    // the search is done from low (inclusive) to hi (exclusive)
    public static int binarySearchDescending(double[] array, int fromIndex, int toIndex, double key)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("The start index is higher than " + "the finish index.");
        }
        if (fromIndex < 0 || toIndex > array.length)
        {
            throw new ArrayIndexOutOfBoundsException("One of the indices is out " + "of bounds.");// Must use Double.compare to take into account NaN, +-0.
        }

        int mid = 0;
        int low = fromIndex;
        int hi = toIndex - 1;

        while (low <= hi)
        {
            mid = (low + hi) >>> 1;
        final int r = Double.compare(array[mid], key);
        if (r == 0)
        {
            return mid;
        }
        else if (r < 0) //klucz jest wiekszy niz array[mid], czyli klucz ma indeks mniejszy niz mid
        {   
            hi = mid - 1;
        }
        else //klucz jest mniejszy niz array[mid], czyli jego indeks jest wiekszy niz mid
        {  // This gets the insertion point right on the last loop
            low = ++mid;
        }
        }

        return mid;
    }

    //(x-s of the array are sorted in descending order) the method gives the first position in the array 'insertPosition'
    //such that array[insertPosition][0] <= keyX, or returns toIndex if all elements are greater than keyX
    // the search is done from fromIndex (inclusive) to toIndex (exclusive)
    public static int binarySearchDescendingX(double[][] array, int fromIndex, int toIndex, double keyX)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("The start index is higher than " + "the finish index.");
        }
        if (fromIndex < 0 || toIndex > array.length)
        {
            throw new ArrayIndexOutOfBoundsException("One of the indices is out of bounds.");// we must use Double.compare to take into account NaN, +-0.
        }

        int mid = 0;
        int low = fromIndex;
        int hi = toIndex - 1;

        while (low <= hi)
        {
            mid = (low + hi) >>> 1;

        final int r = Double.compare(array[mid][0], keyX);
        if (r == 0)
        {
            return mid;
        }
        else if (r < 0) //klucz jest wiekszy niz array[mid], czyli klucz ma indeks mniejszy niz mid
        {   
            hi = mid - 1;
        }
        else //klucz jest mniejszy niz array[mid], czyli jego indeks jest wiekszy niz mid
        {  // This gets the insertion point right on the last loop
            low = ++mid;
        }
        }

        return mid;
    }

    //returns the first position in the the array 'insertPosition' such that  array[insertPosition] >= keyX
    //or returns toIndex, if there is no such element 
    public static int binarySearchAscending(double[] array, int fromIndex, int toIndex, double keyX)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("The start index is higher than " + "the finish index.");
        }
        if (fromIndex < 0 || toIndex > array.length)
        {
            throw new ArrayIndexOutOfBoundsException("One of the indices is out " + "of bounds.");// Must use Double.compare to take into account NaN, +-0.
        }   

        int low = fromIndex;
        int hi = toIndex - 1;

        while (low <= hi)
        {
            int mid = (low + hi) >>> 1;
        final int r = Double.compare(array[mid], keyX);
        if (r == 0)
        {
            return mid;
        }
        else if (r > 0) //klucz jest mniejszy niz array[mid], czyli klucz ma indeks mniejszy niz mid
        {   
            hi = mid - 1;
        }
        else //klucz jest wiekszy niz array[mid], czyli jego indeks jest wiekszy niz mid
        {  // This gets the insertion point right on the last loop
            low = mid + 1;
        }
        }

        return low;
    }

    //returns the first position in the the array 'insertPosition' such that  array[insertPosition][0] >= keyX
    //or returns toIndex, if there is no such element 
    public static int binarySearchAscendingX(double[][] array, int fromIndex, int toIndex, double keyX)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("The start index is higher than " + "the finish index.");
        }
        if (fromIndex < 0 || toIndex > array.length)
        {
            throw new ArrayIndexOutOfBoundsException("One of the indices is out " + "of bounds.");// Must use Double.compare to take into account NaN, +-0.
        }   

        int low = fromIndex;
        int hi = toIndex - 1;

        while (low <= hi)
        {
            int mid = (low + hi) >>> 1;
            final int r = Double.compare(array[mid][0], keyX);
            if (r == 0)
            {
                return mid;
            }
            else if (r > 0) //klucz jest mniejszy niz array[mid], czyli klucz ma indeks mniejszy niz mid
            {   
                hi = mid - 1;
            }
            else //klucz jest wiekszy niz array[mid], czyli jego indeks jest wiekszy niz mid
            {  // This gets the insertion point right on the last loop
                low = mid + 1;
            }
        }

        return low;
    }

    //gives the first element of the array whose ys are sorted in descending order which is lower then key, or toIndex if all elements are larger
    // the search is done from fromIndex (inclusive) to toIndex (exclusive)
    public static int binarySearchDescendingY(double[][] array, int fromIndex, int toIndex, double keyY)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("The start index is higher than " + "the finish index.");
        }
        if (fromIndex < 0 || toIndex > array.length)
        {
            throw new ArrayIndexOutOfBoundsException("One of the indices is out " + "of bounds.");// Must use Double.compare to take into account NaN, +-0.
        }
        int mid = 0;
        while (fromIndex <= toIndex)
        {
            mid = (fromIndex + toIndex) >>> 1;
            final int r = Double.compare(array[mid][1], keyY);
            if (r == 0)
            {
                return mid;
            }
            else if (r < 0) //klucz jest wiekszy niz array[mid], czyli klucz ma indeks mniejszy niz mid
            {   
                toIndex = mid - 1;
            }
            else //klucz jest mniejszy niz array[mid[, czyli jego indeks jest wiekszy niz mid
            {  // This gets the insertion point right on the last loop
                fromIndex = ++mid;
            }
        }

        return mid;
    }

    public static int binarySearchAscendingY(double[][] array, int fromIndex, int toIndex, double keyY)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("The start index is higher than " + "the finish index.");
        }
        if (fromIndex < 0 || toIndex > array.length)
        {
            throw new ArrayIndexOutOfBoundsException("One of the indices is out " + "of bounds.");// Must use Double.compare to take into account NaN, +-0.
        }

        int low = fromIndex;
        int hi = toIndex - 1;

        while (low <= hi)
        {
            int mid = (low + hi) >>> 1;
            final int r = Double.compare(array[mid][1], keyY);
            if (r == 0)
            {
                return mid;
            }
            else if (r > 0) 
            {   
                hi = mid - 1;
            }
            else 
            {  
                low = mid + 1;
            }
        }

        return low;
    }


    //array of points must be sorted in ascending order in terms of the absolute values of residuals (vertical distances) to function f
    public static int binarySearchAscendingResidual(double[][] array, int fromIndex, int toIndex, double residual, UnivariateFunction f)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("The start index is higher than " + "the finish index.");
        }
        if (fromIndex < 0 || toIndex > array.length)
        {
            throw new ArrayIndexOutOfBoundsException("One of the indices is out " + "of bounds.");// Must use Double.compare to take into account NaN, +-0.
        } 

        int low = fromIndex;
        int hi = toIndex - 1;

        while (low <= hi)
        {            
            int mid = (low + hi) >>> 1;

            double[] p = array[mid];
            final int r = Double.compare(Math.abs(f.value(p[0]) - p[1]), residual);
            if (r == 0)
            {
                return mid;
            }
            else if (r > 0) //klucz jest mniejszy niz array[mid], czyli klucz ma indeks mniejszy niz mid
            {   
                hi = mid - 1;
            }
            else //klucz jest wiêkszy niz array[mid], czyli jego indeks jest wiekszy niz mid
            { 
                low = mid + 1;
            }
        }

        return low;
    }

    //array of points must be sorted in descending order in terms of the absolute values of residuals (vertical distances) to function f
    public static int binarySearchDescendingResidual(double[][] array, int fromIndex, int toIndex, double residual, UnivariateFunction f)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("The start index is higher than " + "the finish index.");
        }
        if (fromIndex < 0 || toIndex > array.length)
        {
            throw new ArrayIndexOutOfBoundsException("One of the indices is out " + "of bounds.");// Must use Double.compare to take into account NaN, +-0.
        }
        int mid = 0;
        while (fromIndex <= toIndex)
        {
            mid = (fromIndex + toIndex) >>> 1;
            double[] p = array[mid];
            final int r = Double.compare(Math.abs(f.value(p[0]) - p[1]), residual);
            if (r == 0)
            {
                return mid;
            }
            else if (r < 0) 
            {   
                toIndex = mid - 1;
            }
            else 
            {  
                fromIndex = ++mid;
            }
        }

        return mid;
    }


    public static int total(int[] data)
    {
        int total = 0;

        if(data != null)
        {
            for(int d: data)
            {
                total += d;
            }
        }

        return total;
    }


    public static int total(int[][] data)
    {
        int total = 0;

        if(data != null)
        {
            for(int[] row: data)
            {
                for(int d: row)
                {
                    total += d;
                }
            }
        }

        return total;
    }


    public static long total(long[] data)
    {
        long total = 0;

        if(data != null)
        {
            for(long d: data)
            {
                total += d;
            }
        }

        return total;
    }

    public static long total(long[][] data)
    {
        long total = 0;

        if(data != null)
        {
            for(long[] row: data)
            {
                for(long d: row)
                {
                    total += d;
                }
            }
        }

        return total;
    }

    public static float total(float[] data)
    {
        float total = 0;

        if(data != null)
        {
            for(float d: data)
            {
                total += d;
            }
        }

        return total;
    }

    public static float total(float[][] data)
    {
        float total = 0;

        if(data != null)
        {
            for(float[] row: data)
            {
                for(float d: row)
                {
                    total += d;
                }
            }
        }

        return total;
    }

    public static double total(double[] data)
    {
        double total = 0;

        if(data != null)
        {
            for(double d: data)
            {
                total += d;
            }
        }

        return total;
    }


    public static double total(double[][] data)
    {
        double total = 0;

        if(data != null)
        {
            for(double[] row: data)
            {
                for(double d: row)
                {
                    total += d;
                }
            }
        }

        return total;
    }

    public static double total(double[] array, int c)
    {
        double total = 0;
        for(int i = 0;i<c;i++)
        {
            total += array[i];
        }
        return total;
    }

    public static void print(char[] data) 
    {
        int count = data.length;
        System.out.print("{");
        for(int j = 0;j<count;j++)
        {
            int item = data[j];
            System.out.print(item);
            if(j<count - 1)
            {
                System.out.print(",");
            }               
        }
        System.out.println("}");        
    }


    public static void print(int[] data) 
    {
        print(data,0,data.length); 
    }

    //from inclusive, to exclusive
    public static void print(int[] data, int from, int to) 
    {        
        Validation.requireValueEqualToOrBetweenBounds(from, 0, data.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, data.length, "to");

        System.out.print("{");
        for(int j = from;j<to;j++)
        {
            int item = data[j];
            System.out.print(item);
            if(j<to - 1)
            {
                System.out.print(",");
            }               
        }
        System.out.println("}");        
    }


    public static void print(double[] data) 
    {
        int count = data.length;
        System.out.print("{");
        for(int j = 0;j<count;j++)
        {
            double item = data[j];
            System.out.print(item);
            if(j<count - 1)
            {
                System.out.print(",");
            }               
        }
        System.out.println("}");        
    }

    public static void print(double[] data, int step, double scaling) 
    {
        int count = data.length;
        System.out.print("{");
        for(int j = 0; j < count; j += step)
        {
            double item = data[j];
            System.out.print(scaling*item);
            if(j<count - 1)
            {
                System.out.print(",");
            }               
        }
        System.out.println("}");        
    }

    public static void print(double[][] data)
    {
        for(int i = 0; i<data.length;i++)
        {
            double[] row = data[i];
            int count = row.length;
            System.out.print("{");
            for(int j = 0;j<count;j++)
            {
                double item = row[j];
                System.out.print(item);
                if(j<count - 1)
                {
                    System.out.print(",");
                }               
            }
            System.out.println("},");
        }
    }

    public static void print(double[][] data, int step)
    {
        for(int i = 0; i<data.length;i+=step)
        {
            double[] row = data[i];
            int count = row.length;
            System.out.print("{");
            for(int j = 0;j<count;j++)
            {
                double item = row[j];
                System.out.print(item);
                if(j<count - 1)
                {
                    System.out.print(",");
                }               
            }
            System.out.println("},");
        }
    }

    public static void print(double[][] data, int step, double scaling)
    {
        for(int i = 0; i<data.length;i+=step)
        {
            double[] row = data[i];
            int count = row.length;
            System.out.print("{");
            for(int j = 0;j<count;j++)
            {
                double item = row[j];
                System.out.print(scaling*item);
                if(j<count - 1)
                {
                    System.out.print(",");
                }               
            }
            System.out.println("},");
        }
    }

    public static void print(double[][] data, NumberFormat format)
    {
        for(int i = 0; i<data.length;i++)
        {
            double[] row = data[i];
            int count = row.length;
            System.out.print("{");
            for(int j = 0;j<count;j++)
            {
                double item = row[j];
                String s = format.format(item);                
                System.out.print(s);
                if(j<count - 1)
                {
                    System.out.print(",");
                }               
            }
            System.out.println("},");
        }
    }

    public static void print(double[][] data, NumberFormat format, int step, double scaling)
    {
        for(int i = 0; i<data.length;i+=step)
        {
            double[] row = data[i];
            int count = row.length;
            System.out.print("{");
            for(int j = 0;j<count;j++)
            {
                double item = row[j];
                System.out.print(format.format(scaling*item));
                if(j<count - 1)
                {
                    System.out.print(",");
                }               
            }
            System.out.println("},");
        }
    }

    public static void print(int[][] data)
    {
        for(int i = 0; i<data.length;i++)
        {
            int[] row = data[i];
            int count = row.length;
            System.out.print("{");

            for(int j = 0; j<count; j++)
            {
                int item = row[j];
                System.out.print(item);

                if(j<count - 1)
                {
                    System.out.print(",");
                }               
            }

            System.out.println("},");
        }
    }    

    public static void print(boolean[][] data)
    {
        for(int i = 0; i<data.length;i++)
        {
            boolean[] row = data[i];
            int count = row.length;
            System.out.print("{");

            for(int j = 0; j<count; j++)
            {
                boolean item = row[j];
                System.out.print(item);

                if(j<count - 1)
                {
                    System.out.print(",");
                }               
            }

            System.out.println("},");
        }
    }    

    public static double[] flatten(double[][] data)
    {
        int count = 0;
        for(double[] row: data)
        {
            count = count + row.length;
        }

        double[] flattened = new double[count];

        int i = 0;
        for(double[] row: data)
        {
            for(double item: row)
            {
                flattened[i++] = item;
            }
        }

        return flattened;
    }

    public static float[] flatten(float[][] data)
    {
        int count = 0;
        for(float[] row: data)
        {
            count = count + row.length;
        }

        float[] flattened = new float[count];

        int i = 0;
        for(float[] row: data)
        {
            for(float item: row)
            {
                flattened[i++] = item;
            }
        }

        return flattened;
    }

    public static int[] flatten(int[][] data)
    {
        int count = 0;
        for(int[] row: data)
        {
            count = count + row.length;
        }

        int[] flattened = new int[count];

        int i = 0;
        for(int[] row: data)
        {
            for(int item: row)
            {
                flattened[i++] = item;
            }
        }

        return flattened;
    }

    public static byte[] flatten(byte[][] data)
    {
        int count = 0;
        for(byte[] row: data)
        {
            count = count + row.length;
        }

        byte[] flattened = new byte[count];

        int i = 0;
        for(byte[] row: data)
        {
            for(byte item: row)
            {
                flattened[i++] = item;
            }
        }

        return flattened;
    }


    public static Object[] flatten(Object[][] data)
    {
        int count = 0;
        for(Object[] row: data)
        {
            count = count + row.length;
        }

        Object[] flattened = new Object[count];

        int i = 0;
        for(Object[] row: data)
        {
            for(Object item: row)
            {
                flattened[i++] = item;
            }
        }

        return flattened;
    }

    public static double[][] combineTo2DArray(double[] xValues, double[] yValues)
    {
        Validation.requireTwoArraysNonNullAndOfEqualLengthParameterName(xValues, yValues, "xValues", "yValues");

        int n = xValues.length;

        double[][] points = new double[n][];
        for(int i = 0; i < n; i++)
        {
            points[i] = new double[] {xValues[i], yValues[i]};
        }

        return points;
    }
    
    public static double[] getDoubleArray(Collection<Double> collection)
    {        
        int n = collection.size();

        double[] array = new double[n];

        int i = 0;
        for(Double val : collection)
        {
            array[i++] = val.doubleValue();
        }

        return array;
    }

    public static int[] getIntArray(Collection<Integer> collection)
    {
        int n = collection.size();

        int[] array = new int[n];

        int i = 0;

        for(Integer val : collection)
        {
            array[i] = val.intValue();
        }

        return array;
    }

    public static float[] getFloatArray(List<Float> list)
    {
        int n = list.size();

        float[] array = new float[n];

        for(int i = 0; i<n;i++)
        {
            array[i] = list.get(i).floatValue();
        }

        return array;
    }

    public static double[][] addRightColumns(double[][] data, double padding, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = data.length;
        double[][] padded = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] dataRow = data[i];
            int m = dataRow.length;

            double[] paddedRow = new double[m + count]; 

            //copied old values
            for(int j = 0; j<m; j++)
            {
                paddedRow[j] = dataRow[j];
            }            

            //sets the padding values
            for(int k = 0; k<count; k++)
            {
                paddedRow[m + k] = padding;
            }

            padded[i] = paddedRow;
        }

        return padded;
    }


    public static double[][] addLeftColumns(double[][] data, double padding, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = data.length;
        double[][] padded = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] dataRow = data[i];
            int m = dataRow.length;

            double[] paddedRow = new double[m + count];           

            //sets the padding values
            for(int k = 0; k<count; k++)
            {
                paddedRow[k] = padding;
            }

            //copied old values
            for(int j = 0; j<m; j++)
            {
                paddedRow[j + count] = dataRow[j];
            }            


            padded[i] = paddedRow;
        }

        return padded;
    }

    public static double[][] addColumns(double[][] data, double padding, int countLeft, int countRight)
    {
        Validation.requireValueGreaterOrEqualToParameterName(countLeft, 0, "countLeft");
        Validation.requireValueGreaterOrEqualToParameterName(countRight, 0, "countRight");

        int n = data.length;
        double[][] padded = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] dataRow = data[i];
            int m = dataRow.length;

            double[] paddedRow = new double[m + countLeft + countRight]; 

            //sets the padding values to the left
            for(int j = 0; j<countLeft; j++)
            {
                paddedRow[j] = padding;
            }

            //copied old values
            for(int j = 0; j<m; j++)
            {
                paddedRow[countLeft + j] = dataRow[j];
            }            

            //sets the padding values the right
            for(int j = 0; j<countRight; j++)
            {
                paddedRow[m + countLeft + j] = padding;
            }

            padded[i] = paddedRow;
        }

        return padded;
    }


    public static double[][] addTopRows(double[][] data, double padding, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = data.length;
        if(n == 0)
        {
            return new double[][] {{}};
        }

        int firstRowLength = data[0].length;

        double[][] padded = new double[n + count][];    

        //builds padding rows
        for(int i = 0; i<count; i++)
        {
            double[] paddedRow = new double[firstRowLength]; 
            Arrays.fill(paddedRow, padding);

            padded[i] = paddedRow;
        }

        //copies old rows
        for(int k = 0; k<n; k++)
        {
            double[] dataRow = data[k];                 
            double[] paddedRow = Arrays.copyOf(dataRow, dataRow.length); 

            padded[k + count] = paddedRow;
        }

        return padded;
    }

    public static double[][] addBottomRows(double[][] data, double padding, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = data.length;
        if(n == 0)
        {
            return new double[][] {{}};
        }

        int lastRowLength = data[n - 1].length;

        double[][] padded = new double[n + count][];    


        //copies old rows
        for(int k = 0; k<n; k++)
        {
            double[] dataRow = data[k];                 
            double[] paddedRow = Arrays.copyOf(dataRow, dataRow.length); 

            padded[k] = paddedRow;
        }

        //builds padding rows
        for(int i = 0; i<count; i++)
        {
            double[] paddedRow = new double[lastRowLength]; 
            Arrays.fill(paddedRow, padding);

            padded[n + i] = paddedRow;
        }


        return padded;
    }

    public static double[][] addRows(double[][] data, double padding, int countTop, int countBottom)
    {
        Validation.requireValueGreaterOrEqualToParameterName(countTop, 0, "countTop");
        Validation.requireValueGreaterOrEqualToParameterName(countBottom, 0, "countBottom");

        int n = data.length;
        if(n == 0)
        {
            return new double[][] {{}};
        }

        int firstRowLength = data[0].length;
        int lastRowLength = data[n - 1].length;

        double[][] padded = new double[n + countTop + countBottom][];    

        //builds padding rows at the top
        for(int i = 0; i<countTop; i++)
        {
            double[] paddedRow = new double[firstRowLength]; 
            Arrays.fill(paddedRow, padding);

            padded[i] = paddedRow;
        }

        //copies old rows
        for(int i = 0; i<n; i++)
        {
            double[] dataRow = data[i];                 
            double[] paddedRow = Arrays.copyOf(dataRow, dataRow.length); 

            padded[i + countTop] = paddedRow;
        }

        //builds padding rows at the bottom
        for(int i = 0; i<countBottom; i++)
        {
            double[] paddedRow = new double[lastRowLength]; 
            Arrays.fill(paddedRow, padding);

            padded[n + countTop + i] = paddedRow;
        }


        return padded;
    }

    public static double[][] removeLeftColumns(double[][] data, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = data.length;
        double[][] dataNew = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] rowOld = data[i];
            int lengthOld = rowOld.length;

            int lengthNew = Math.max(0, lengthOld - count);
            double[] rowNew = Arrays.copyOf(rowOld, lengthNew); 

            dataNew[i] = rowNew;
        }

        return dataNew;
    }

    public static double[][] removeRightColumns(double[][] data, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = data.length;
        double[][] dataNew = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] rowOld = data[i];
            int lengthOld = rowOld.length;

            double[] rowNew = Arrays.copyOfRange(rowOld, Math.min(count,lengthOld), lengthOld); 

            dataNew[i] = rowNew;
        }

        return dataNew;
    }

    public static double[][] removeColumns(double[][] data, int countLeft, int countRight)
    {
        Validation.requireValueGreaterOrEqualToParameterName(countLeft, 0, "countLeft");

        int n = data.length;
        double[][] dataNew = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] rowOld = data[i];
            int lengthOld = rowOld.length;

            int lengthNew = Math.max(0, lengthOld - countLeft - countRight);
            int left = Math.min(lengthOld, countLeft);
            double[] rowNew = Arrays.copyOfRange(rowOld, left, left + lengthNew);

            dataNew[i] = rowNew;
        }

        return dataNew;
    }


    public static double[][] removeTopRow(double[][] data, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = data.length;
        int removedCount = Math.min(n, count);

        double[][] dataNew = new double[n - removedCount][];

        for(int i = removedCount; i<n; i++)
        {
            double[] rowOld = data[i];
            int lengthOld = rowOld.length;

            double[] rowNew = Arrays.copyOf(rowOld, lengthOld); 

            dataNew[i - removedCount] = rowNew;
        }

        return dataNew;
    }

    public static double[][] removeBottomRows(double[][] data, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = data.length;
        int removedCount = Math.min(n, count);
        int lengthNew = n - removedCount;

        double[][] dataNew = new double[lengthNew][];

        for(int i = 0; i<lengthNew; i++)
        {
            double[] rowOld = data[i];
            int lengthOld = rowOld.length;

            double[] rowNew = Arrays.copyOf(rowOld, lengthOld); 

            dataNew[i] = rowNew;
        }

        return dataNew;
    }

    public static double[][] removeRows(double[][] data, int countTop, int countBottom)
    {
        Validation.requireValueGreaterOrEqualToParameterName(countBottom, 0, "countBottom");
        Validation.requireValueGreaterOrEqualToParameterName(countBottom, 0, "countBottom");

        int n = data.length;
        int actualRemovedTop = Math.min(n, countTop);
        int actualRemovedBottom = Math.min(n - actualRemovedTop, countBottom);

        double[][] dataNew = new double[n - actualRemovedTop - actualRemovedBottom][];

        for(int i = actualRemovedTop; i< n - actualRemovedBottom; i++)
        {
            double[] rowOld = data[i];
            int lengthOld = rowOld.length;

            double[] rowNew = Arrays.copyOf(rowOld, lengthOld); 

            dataNew[i - actualRemovedTop] = rowNew;
        }

        return dataNew;
    }

    public static double[] padLeft(double[] array, double padding, int count)
    {
        Validation.requireValueGreaterOrEqualToParameterName(count, 0, "count");

        int n = array.length;
        double[] padded = new double[n + count];

        for(int i = 0; i< count; i++)
        {
            padded[i] = count;
        }

        for(int i = 0; i<n; i++)
        {
            padded[i + count] = array[i];
        }

        return padded;
    }

    public static double[][] convertToPoints(double ys[], double[] xs)
    {      
        Validation.requireTwoArraysNonNullAndOfEqualLengthParameterName(ys, xs, "ys", "xs");

        int n = ys.length;
        double[][] points = new double[n][];

        for(int i = 0; i<n;i++)
        {
            points[i] = new double[] {xs[i], ys[i]};
        }

        return points;
    }

    public static double[][] convertToPoints(double ys[], double[] xs, int from, int to)
    {
        int n = to - from;
        double[][] points = new double[n][];

        for(int i = from; i<to; i++)
        {
            points[i - from] = new double[] {xs[i], ys[i]};
        }

        return points;
    }   

    public static double[][] convertToPoints(double[] data)
    {
        int n = data.length;

        double[][] points = new double[n][];

        for(int i = 0; i<n; i++)
        {
            points[i] = new double[] {i, data[i]};
        }

        return points;
    }


    public static double[] getColumn(double[][] matrix, int columnIndex)
    {
        int rowCount = matrix.length;
        double[] column = new double[rowCount];

        for(int i = 0; i<rowCount;i++)
        {
            column[i] = matrix[i][columnIndex];
        }

        return column;
    }

    public static double[] reverse(double[] array)
    {
        int n = array.length;
        double[] reversed = new double[n];

        for(int i = 0; i<n; i++)
        {
            reversed[i] = array[n - 1 - i];
        }

        return reversed;
    }

    public static void reverseInPlace(double[] array)
    {
        int n = array.length;

        for(int i = 0; i<n/2; i++)
        {
            double tmp = array[i];
            array[i] = array[n - 1 - i];
            array[n - 1 - i] = tmp;
        }        
    }

    //reverse both columns and rows
    public static double[][] deepReverse(double[][] array)
    {
        int n = array.length;

        double[][] reversed = new double[n][];

        for(int i = 0; i<n; i++)
        {
            reversed[i] = reverse(array[n - 1 - i]);
        }

        return reversed;
    }

    public static double[] getXCoordinateCopy(double[][] points, int to)
    {
        double[] xCoordinates = new double[to];
        for(int i = 0; i<to; i++)
        {
            xCoordinates[i] = points[i][0];
        }

        return xCoordinates;
    }

    public static double[] getYCoordinateCopy(double[][] points, int to)
    {
        double[] yCoordinates = new double[to];
        for(int i = 0; i<to; i++)
        {
            yCoordinates[i] = points[i][1];
        }

        return yCoordinates;
    }

    public static double[][] transpose(double[][] data, int oldRowCount, int oldColumnCount)
    {
        double[][] transposedData = new double[oldColumnCount][oldRowCount];

        for(int i = 0; i < oldColumnCount; i++)
        {
            for(int j = 0;j<oldRowCount; j++)
            {
                transposedData[i][j] = data[j][i];
            }
        }

        return transposedData;
    }

    public static boolean containsNegativeValues(double[] data)
    {
        boolean negative = false;
        for(double v: data)
        {
            if(v < 0)
            {
                negative = true;
                break;
            }
        }
        return negative;
    }

    public static boolean containsNonPositiveValues(double[] data)
    {
        boolean nonpositive = false;
        for(double v: data)
        {
            if(v <= 0)
            {
                nonpositive = true;
                break;
            }
        }
        return nonpositive;
    }

    public static boolean containsNaN(double[] array)
    {   
        boolean nans = false;
        for(double d : array)
        {
            if(Double.isNaN(d))
            {
                nans = true;
                break;
            }
        }

        return nans;
    }

    public static boolean containsNumericFiniteValues(double[] array)
    {   
        boolean numeric = false;
        for(double d : array)
        {
            if(Double.isFinite(d))
            {
                numeric = true;
                break;
            }
        }

        return numeric;
    }

    public static boolean containsNull(Object[] array)
    {
        boolean containsNull = false;
        for(Object d : array)
        {
            if(d == null)
            {
                containsNull = true;
                break;
            }
        }

        return containsNull;
    }

    public static boolean contains(int[] array, int val)
    {
        boolean contains = false;
        for(int el : array)
        {
            if(el == val)
            {
                contains = true;
                break;
            }
        }

        return contains;
    }

    public static double[] join(double[] arr1, double[] arr2)
    {
        int n = arr1.length;
        int m = arr2.length;
        double[] result = new double[n + m];
        System.arraycopy(arr1, 0, result, 0, n);
        System.arraycopy(arr2, 0, result, n, m);

        return result;
    }

    public static double[][] join(double[][] arr1, double[][] arr2) 
    {
        int n = arr1.length;
        int m = arr2.length;
        double[][] result = new double[n + m][2];
        System.arraycopy(arr1, 0, result, 0, n);
        System.arraycopy(arr2, 0, result, n, m);
        return result;
    }

    public static Object[][] join(Object[][] arr1, Object[][] arr2) 
    {
        int n = arr1.length;
        int m = arr2.length;
        Object[][] result =  new Object[n + m][2];
        System.arraycopy(arr1, 0, result, 0, n);
        System.arraycopy(arr2, 0, result, n, m);
        return result;
    }

    public static double[][] deepCopy(double[][] orig)
    {
        if(orig == null)
        {
            return null;
        }

        int n = orig.length;
        double[][] copy = new double[n][];
        for(int i = 0;i<n;i++)
        {
            double[] row = orig[i];                        
            copy[i] = Arrays.copyOf(row, row.length);
        }
        return copy;
    }

    //from inclusive, to exclusive
    //should throw the same exceptions as Arrays.copyOfRange()
    public static double[][] deepCopyOfRange(double[][] orig, int from, int to)
    {      
        if(orig == null)
        {
            return null;
        }

        if(from < 0 || from >= orig.length)
        {
            throw new IllegalArgumentException("Index 'from' " + from + " is outside the index range");
        }

        if(to < 0 || to > orig.length)
        {
            throw new IllegalArgumentException("Index 'to' " + to + " is outside the index range");
        }

        int n = to - from;
        double[][] copy = new double[n][];
        for(int i = from;i<to;i++)
        {
            double[] row = orig[i];                        
            copy[i - from] = Arrays.copyOf(row, row.length);
        }
        return copy;
    }

    public static double[][] deepCopy(double[][] orig, int maxRow, int maxColumn)
    {
        if(orig == null)
        {
            return null;
        }

        Validation.requireValueGreaterOrEqualToParameterName(maxRow, 0, "maxRow");
        Validation.requireValueGreaterOrEqualToParameterName(maxColumn, 0, "maxColumn");

        int n = Math.min(orig.length, maxRow);

        double[][] copy = new double[n][];
        for(int i = 0;i<n;i++)
        {
            double[] row = orig[i];
            copy[i] = Arrays.copyOf(row, Math.min(row.length, maxColumn));
        }
        return copy;
    }

    public static int[][] deepCopy(int[][] orig)
    {
        if(orig == null)
        {
            return null;
        }

        int n = orig.length;
        int[][] copy = new int[n][];
        for(int i = 0;i<n;i++)
        {
            int[] row = orig[i];
            copy[i] = Arrays.copyOf(row, row.length);
        }
        return copy;
    }


    public static int[][] deepCopy(int[][] orig, int maxRow, int maxColumn)
    {
        if(orig == null)
        {
            return null;
        }

        Validation.requireValueGreaterOrEqualToParameterName(maxRow, 0, "maxRow");
        Validation.requireValueGreaterOrEqualToParameterName(maxColumn, 0, "maxColumn");

        int n = Math.min(orig.length, maxRow);
        int[][] copy = new int[n][];
        for(int i = 0;i<n;i++)
        {
            int[] row = orig[i];
            copy[i] = Arrays.copyOf(row, Math.min(row.length, maxColumn));
        }
        return copy;
    }

    public static <T> T[][] deepCopy(T[][] orig)
    {
        if(orig == null)
        {
            return null;
        }

        int n = orig.length;

        Class<? extends T[][]> newType = (Class<? extends T[][]>) orig.getClass();

        T[][] copy = ((Object)newType == (Object)Object[][].class)
                ? (T[][]) new Object[n][] : (T[][]) Array.newInstance(newType.getComponentType(), n);

                for(int i = 0;i<n;i++)
                {
                    T[] row = orig[i];
                    copy[i] = Arrays.copyOf(row, row.length);
                }
                return copy;
    }

    public static int countGreaterXs(double[][] points, double d)
    {
        int count = 0;
        for(double[] p: points)
        {
            double x = p[0]; 
            if(x > d){count++;}
        }
        return count;
    }

    public static int countGreaterOrEqualXs(double[][] points, double d)
    {
        int count = 0;
        for(double[] p: points)
        {
            double x = p[0]; 
            if(x >= d){count++;}
        }
        return count;
    }

    public static int countSmallerXs(double[][] points, double d)
    {
        int count = 0;
        for(double[] p: points)
        {
            double x = p[0]; 
            if(x < d){count++;}
        }
        return count;
    }

    public static int countSmallerOrEqualXs(double[][] points, double d)
    {
        int count = 0;
        for(double[] p: points)
        {
            double x = p[0]; 
            if(x <= d){count++;}
        }
        return count;
    }

    public static int countXValuesWithinRange(double[][] points, double lowerBound, double upperBound)
    {
        int count = 0;
        for(double[] p: points)
        {
            double x = p[0]; 
            if(x >= lowerBound && x <= upperBound){count++;}
        }
        return count;
    }

    public static int countValuesWithinRange(double[] values, double lowerBound, double upperBound)
    {
        int n = values.length;
        int count = 0;
        for(int i = 0;i<n;i++)
        {
            double x = values[i]; 
            if(x >= lowerBound && x <= upperBound){count++;}
        }
        return count;
    }

    public static int countGreaterOrEqual(double[] values, int from, int to, double d)
    {
        int count = 0;
        for(int i = from;i<to;i++)
        {
            double x = values[i]; 
            if(x >= d){count++;}
        }
        return count;
    }

    public static int countSmallerOrEqual(double[] values, int from, int to, double d)
    {
        int count = 0;
        for(int i = from;i<to;i++)
        {
            double x = values[i]; 
            if(x <= d){count++;}
        }
        return count;
    }

    public static double getBestValue(double[][] points, final double arg)
    {
        double[][] pointsCopy = ArrayUtilities.deepCopy(points);
        final RealValuedFunction<double[]> f = new RealValuedFunction<double[]>() 
        {           
            @Override
            public double value(double[] p) 
            {
                double x = p[0];
                double d = Math.abs(arg - x);
                return d;
            }
        };
        Arrays.sort(pointsCopy,new RealValueBasedComparator<>(f));
        return pointsCopy[0][1];
    }

    public static double getBestArgument(double[][] points, final double val)
    {
        double[][] pointsCopy = ArrayUtilities.deepCopy(points);
        final RealValuedFunction<double[]> f = new RealValuedFunction<double[]>() 
        {           
            @Override
            public double value(double[] p) 
            {
                double y = p[1];
                double d = Math.abs(val - y);
                return d;
            }
        };
        Arrays.sort(pointsCopy,new RealValueBasedComparator<>(f));
        return pointsCopy[0][0];
    }

    public static int getMaximum(int[] values)
    {
        int max = -Integer.MAX_VALUE;
        for(int x: values)
        {
            if(x>max)
            {
                max = x;
            }
        }

        return max;
    }

    public static int getMinimum(int[] values)
    {
        int min = Integer.MAX_VALUE;
        for(int x: values)
        {
            if(x<min)
            {
                min = x;
            }
        }

        return min;
    }

    public static double getMaximum(double[] values)
    {
        return getMaximum(values, 0, values.length);
    }

    public static double getMaximum(double[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        double max = Double.NEGATIVE_INFINITY;
        for(int i = from; i< to; i++)
        {
            double x= values[i];
            if(x>max)
            {
                max = x;
            }
        }

        return max;
    }

    public static double getMaximum(double[][] values)
    {
        double max = Double.NEGATIVE_INFINITY;
        for(double[] row: values)
        {
            for(double x : row)
            {
                if(x>max)
                {
                    max = x;
                }
            }         
        }

        return max;
    }

    public static Range getRange(double[] values)
    {
        if(values == null || values.length == 0)
        {
            return null;
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for(double x: values)
        {
            if(x < min)
            {
                min = x;
            }
            if(x > max)
            {
                max = x;
            }
        }

        Range range = new Range(min, max);

        return range;
    }

    //from inclusive, to exclusive
    public static Range getRange(double[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        if(values == null || values.length == 0 || values.length <= from)
        {
            return null;
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        int validFrom = Math.max(from, 0);
        int validTo = Math.min(to, values.length);

        for(int i = validFrom; i<validTo;i++)
        {
            double x = values[i];
            if(x < min)
            {
                min = x;
            }
            if(x > max)
            {
                max = x;
            }
        }

        Range range = new Range(min, max);

        return range;
    }

    public static double getNumericMaximum(double[] values)
    {
        double max = Double.NEGATIVE_INFINITY;
        for(double x: values)
        {
            if(!Double.isNaN(x))
            {
                if(x > max)
                {
                    max = x;
                }
            }
        }

        return max;
    }


    //array data must be ordered - the last point must have the largest y
    public static double getClosestX(double[][] data, double y)
    {
        int index = -1;
        double distance = Double.POSITIVE_INFINITY;     

        int n = data.length;
        if(data[n - 1][1] > y)
        {
            for(int i = 0; i < n; i++)
            {
                double[] p = data[i];
                double distanceCurrent = Math.abs(p[1] - y);
                if(distanceCurrent < distance)
                {
                    index = i;
                    distance = distanceCurrent;
                }

            }
        }
        else 
        {
            index = n - 1;
        }

        return data[index][0];
    }

    //returns - 1 when the array is empty or all values are either infinite or NaN
    public static int getIndexOfValueClosestTo(double[] array, double testVal)
    {
        return getIndexOfValueClosestTo(array, testVal, 0, array.length);
    }

    //returns - 1 when to <= from or all values are either infinite or NaN
    public static int getIndexOfValueClosestTo(double[] array, double testVal, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, array.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, array.length, "to");

        int index = -1;
        double difference = Double.POSITIVE_INFINITY;     

        for(int i = from; i < to; i++)
        {
            double differenceCurrent = Math.abs(array[i] - testVal);
            if(differenceCurrent < difference)
            {
                index = i;
                difference = differenceCurrent;
            }
        }

        return index;
    }

    //returns - 1 when the array is empty or all y-values are either infinite or NaN
    public static int getIndexOfPointWithYCoordinateClosestTo(double[][] points, double testY)
    {
        return getIndexOfPointWithYCoordinateClosestTo(points, testY, 0, points.length);
    }

    //returns - 1 when to <= from is empty or all y-values are either infinite or NaN
    public static int getIndexOfPointWithYCoordinateClosestTo(double[][] points, double testY, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int index = -1;
        double difference = Double.POSITIVE_INFINITY;     

        for(int i = from; i< to; i++)
        {
            double d = points[i][1];
            double differenceCurrent = Math.abs(d - testY);
            if(differenceCurrent < difference)
            {
                index = i;
                difference = differenceCurrent;
            }
        }

        return index;
    }

    //returns - 1 when the array is empty or all x-values are either infinite or NaN
    public static int getIndexOfPointWithXCoordinateClosestTo(double[][] points, double testX)
    {
        return getIndexOfPointWithXCoordinateClosestTo(points, testX, 0, points.length);
    }

    //returns - 1 when to <= from or all x-values are either infinite or NaN
    public static int getIndexOfPointWithXCoordinateClosestTo(double[][] points, double testX, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int index = -1;
        double difference = Double.POSITIVE_INFINITY;     

        for(int i = from; i<to; i++)
        {
            double d = points[i][0];
            double differenceCurrent = Math.abs(d - testX);
            if(differenceCurrent < difference)
            {
                index = i;
                difference = differenceCurrent;
            }
        }

        return index;
    }

    //returns - 1 when the array is empty or all values are either infinite or NaN
    public static int getIndexOfValueMostDistantFrom(double[] array, double testVal)
    {
        return getIndexOfValueMostDistantFrom(array, testVal, 0, array.length);
    }

    //returns - 1 when the to <= from or all values are either infinite or NaN
    public static int getIndexOfValueMostDistantFrom(double[] array, double testVal, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, array.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, array.length, "to");

        int index = -1;
        double difference = Double.NEGATIVE_INFINITY;     

        for(int i = from; i < to; i++)
        {
            double differenceCurrent = Math.abs(array[i] - testVal);
            if(differenceCurrent > difference)
            {
                index = i;
                difference = differenceCurrent;
            }
        }

        return index;
    }

    //returns -1 when the array is empty or all y-values are either infinite or NaN
    public static int getIndexOfPointWithYCoordinateMostDistantFrom(double[][] points, double testY)
    {
        return getIndexOfPointWithYCoordinateMostDistantFrom(points, testY, 0, points.length);
    }

    //returns -1 when to <= from is empty or all y-values are either infinite or NaN
    public static int getIndexOfPointWithYCoordinateMostDistantFrom(double[][] points, double testY, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int index = -1;
        double difference = Double.NEGATIVE_INFINITY;     

        for(int i = from; i< to; i++)
        {
            double d = points[i][1];
            double differenceCurrent = Math.abs(d - testY);
            if(differenceCurrent > difference)
            {
                index = i;
                difference = differenceCurrent;
            }
        }

        return index;
    }


    public static double getNumericMaximum(double[][] values)
    {
        double max = Double.NEGATIVE_INFINITY;
        for(double[] xs: values)
        {
            for(double x: xs)
            {
                if(!Double.isNaN(x))
                {
                    if(x > max)
                    {
                        max = x;
                    }
                }
            }           
        }

        return max;
    }

    public static Range getBoundedRange(double[] values)
    {      
        if(values.length == 0)
        {
            return null;
        }

        //we need find the first index for which the y-value is not infinite or NaN
        //the alternative approach would be to initially set min to Double.POSITIVE_INFINITY and max to Double.NEGATIVE_INFINITY
        //but then we could not use if(y>max){max = y}else if(y < min) {min = y} in the main loop
        //because the code would be broken if the first 'good' y - value were the lowest
        //we would have to use if if instead of if else if, which is less efficient

        int firstGoodIndex = -1;
        for(int i = 0; i<values.length; i++)
        {
            double v = values[i];

            if(Double.isFinite(v))
            {
                firstGoodIndex = i;
                break;
            }
        }

        if(firstGoodIndex < 0)
        {
            return null;
        }

        double firstGoodValue = values[firstGoodIndex];
        double min = firstGoodValue;
        double max = firstGoodValue;


        for(double x: values)
        {
            if(Double.isFinite(x))
            {
                if(x > max)
                {
                    max = x;
                }
                else if(x<min)
                {
                    min = x;
                }
            }
        }

        Range range = new Range(min, max);      
        return range;
    }

    //from inclusive, to exclusive
    public static Range getBoundedRange(double[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        if(values == null || values.length == 0 || values.length <= from || from >= to)
        {
            return null;
        }      

        //we need find the first index for which the y-value is not infinite or NaN
        //the alternative approach would be to initially set min to Double.POSITIVE_INFINITY and max to Double.NEGATIVE_INFINITY
        //but then we could not use if(y>max){max = y}else if(y < min) {min = y} in the main loop
        //because the code would be broken if the first 'good' y - value were the lowest
        //we would have to use if if instead of if else if, which is less efficient

        int firstGoodIndex = -1;
        for(int i = from; i<to;i++)
        {
            double v = values[i];
            if(Double.isFinite(v))
            {
                firstGoodIndex = i;
                break;
            }
        }

        if(firstGoodIndex < 0)
        {
            return null;
        }

        double firstGoodValue = values[firstGoodIndex];
        double min = firstGoodValue;
        double max = firstGoodValue;

        for(int i = firstGoodIndex + 1; i<to;i++)
        {
            double x = values[i];

            if(Double.isFinite(x))
            {
                if(x > max)
                {
                    max = x;
                }
                else if(x<min)
                {
                    min = x;
                }
            }
        }

        Range range = max >= min ? new Range(min, max) : null;

        return range;
    }

    //this method is designed for grids (or matrices) 
    public static Range getBoundedRange(double[][] gridValues)
    {
        int n = gridValues.length;

        boolean empty = true;

        double min = 0;
        double max = 0;

        //this finds the initial values for min and max and checks whether values array is empty or not
        for(int i = 0; i<n;i++)
        {
            double[] row = gridValues[i];

            int k = row.length;
            for(int j = 0; j<k; j++)
            {
                double v = row[j];

                if(Double.isFinite(v))
                {
                    min = v;
                    max = v;
                    empty = false;
                    break;
                }                           
            }           
        }

        if(empty)
        {
            return null;
        }

        for(double[] xs: gridValues)
        {
            for(double x: xs)
            {
                if(Double.isFinite(x))
                {
                    if(x > max)
                    {
                        max = x;
                    }
                    else if(x < min)
                    {
                        min = x;
                    }
                }
            }           
        }

        Range range = new Range(min, max);      
        return range;
    }

    //this method is designed for array of points 
    public static Range getXRange(double[][] points)
    {
        int n = points.length;

        if(n == 0)
        {
            return null;
        }

        double min = points[0][0];        
        double max = points[0][0];

        for(int i = 1;i<n;i++)
        {
            double x = points[i][0];
            if(x > max)
            {
                max = x;
            }
            else if(x<min)
            {
                min = x;
            }       
        }

        Range range = new Range(min, max);
        return range;
    }

    //this method is designed for array of points 
    //from - inclusive, to-exclusive
    public static Range getXRange(double[][] points, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int n = points.length;

        if(n == 0)
        {
            return null;
        }

        double min = points[from][0];        
        double max = points[from][0];

        for(int i = from + 1;i<to;i++)
        {
            double x = points[i][0];
            if(x > max)
            {
                max = x;
            }
            else if(x<min)
            {
                min = x;
            }       
        }

        Range range = new Range(min, max);
        return range;
    }

    //this method is designed for array of points 
    public static Range getBoundedXRange(double[][] points)
    {
        int n = points.length;

        if(n == 0)
        {
            return null;
        }

        //we need find the first index for which the y-value is not infinite or NaN
        //the alternative approach would be to initially set min to Double.POSITIVE_INFINITY and max to Double.NEGATIVE_INFINITY
        //but then we could not use if(y>max){max = y}else if(y < min) {min = y} in the main loop
        //because the code would be broken if the first 'good' y - value were the lowest
        //we would have to use if if instead of if else if, which is less efficient

        int firstGoodIndex = -1;
        for(int i = 0; i<n;i++)
        {
            double x = points[i][0];
            if(Double.isFinite(x))
            {
                firstGoodIndex = i;
                break;
            }
        }

        if(firstGoodIndex < 0)
        {
            return null;
        }

        double firstGoodValue = points[firstGoodIndex][0];
        double min = firstGoodValue;        
        double max = firstGoodValue;

        for(int i = firstGoodIndex + 1;i<n;i++)
        {
            double x = points[i][0];
            if(Double.isFinite(x))
            {
                if(x > max)
                {
                    max = x;
                }
                else if(x<min)
                {
                    min = x;
                }
            }         
        }

        Range range = new Range(min, max);
        return range;
    }

    //this method is designed for list of points 
    public static Range getBoundedXRange(List<double[]> points)
    {
        if(points.isEmpty())
        {
            return null;
        }

        int n = points.size();

        //we need find the first index for which the y-value is not infinite or NaN
        //the alternative approach would be to initially set min to Double.POSITIVE_INFINITY and max to Double.NEGATIVE_INFINITY
        //but then we could not use if(y>max){max = y}else if(y < min) {min = y} in the main loop
        //because the code would be broken if the first 'good' y - value were the lowest
        //we would have to use if if instead of if else if, which is less efficient

        int firstGoodIndex = -1;
        for(int i = 0; i<n;i++)
        {
            double x = points.get(i)[0];
            if(Double.isFinite(x))
            {
                firstGoodIndex = i;
                break;
            }
        }

        if(firstGoodIndex < 0)
        {
            return null;
        }

        double firstGoodX = points.get(firstGoodIndex)[0];
        double min = firstGoodX;
        double max = firstGoodX;

        for(int i = firstGoodIndex + 1; i<n ;i++)
        {
            double x = points.get(i)[0];
            if(Double.isFinite(x))
            {
                if(x > max)
                {
                    max = x;
                }
                else if(x<min)
                {
                    min = x;
                }
            }         
        }

        Range range = max >= min ? new Range(min, max) : null;
        return range;
    }

    //this method is designed for array of points 
    public static Range getBoundedYRange(double[][] points)
    {
        int n = points.length;

        if(n == 0)
        {
            return null;
        }

        //we need find the first index for which the y-value is not infinite or NaN
        //the alternative approach would be to initially set min to Double.POSITIVE_INFINITY and max to Double.NEGATIVE_INFINITY
        //but then we could not use if(y>max){max = y}else if(y < min) {min = y} in the main loop
        //because the code would be broken if the first 'good' y - value were the lowest
        //we would have to use if if instead of if else if, which is less efficient

        int firstGoodIndex = -1;
        for(int i = 0; i<n;i++)
        {
            double y = points[i][1];
            if(Double.isFinite(y))
            {
                firstGoodIndex = i;
                break;
            }
        }

        if(firstGoodIndex < 0)
        {
            return null;
        }

        double firstGoodY = points[firstGoodIndex][1];
        double min = firstGoodY;
        double max = firstGoodY;

        for(int i = firstGoodIndex + 1; i<n; i++)
        {
            double y = points[i][1];
            if(Double.isFinite(y))
            {
                if(y > max)
                {
                    max = y;
                }
                else if(y < min)
                {
                    min = y;
                }
            }         
        }

        Range range = new Range(min, max);
        return range;
    }


    //this method is designed for array of points 
    public static Range getBoundedYRange(double[][] points, Range xRange)
    {
        int n = points.length;

        if(n == 0 || xRange == null)
        {
            return null;
        }

        double lowerBound = xRange.getLowerBound();
        double upperBound = xRange.getUpperBound();

        double min = 0;
        double max = 0;

        int firstIndex = -1;
        for(int i = 0; i<n;i++)
        {
            double[] p = points[i];

            double x = p[0];
            double y = p[1];

            if(x >= lowerBound && x <= upperBound && Double.isFinite(y))
            {
                firstIndex = i;
                min = y;
                max = y;
                break;
            }
        }

        if(firstIndex < 0)        
        {
            return null;
        }

        for(int i = firstIndex; i < n; i++)
        {
            double[] p = points[i];

            double x = p[0];
            double y = p[1];

            if(x >= lowerBound && x <= upperBound && Double.isFinite(y))
            {         
                if(y>max)
                {
                    max = y;
                }
                else if(y<min)
                {
                    min = y;
                }
            }         
        }

        Range range = new Range(min, max);      
        return range;
    }

    //xValues and yValues should be of the same length 
    public static Range getBoundedYRange(double[] xValues, double[] yValues, Range xRange)
    {
        int n = xValues.length;

        if(n == 0 || xRange == null)
        {
            return null;
        }

        double lowerBound = xRange.getLowerBound();
        double upperBound = xRange.getUpperBound();

        double min = 0;
        double max = 0;

        int firstIndex = -1;
        for(int i = 0; i<n; i++)
        {
            double x = xValues[i];
            double y = yValues[i];

            if(x >= lowerBound && x <= upperBound && Double.isFinite(y))
            {
                firstIndex = i;
                min = y;
                max = y;
                break;
            }
        }

        if(firstIndex < 0)        
        {
            return null;
        }

        for(int i = firstIndex; i < n; i++)
        {
            double x = xValues[i];
            double y = yValues[i];

            if(x >= lowerBound && x <= upperBound && Double.isFinite(y))
            {         
                if(y>max)
                {
                    max = y;
                }
                else if(y<min)
                {
                    min = y;
                }
            }         
        }

        Range range = new Range(min, max);      
        return range;
    }



    /**
     * Returns the range of y-values in the array of point coordinates, ignoring NaNs and infinite values. The array 'points' should have the dimensions of n x m, with m <= 2. 
     * The array is usually an array of point coordinates, with points[i][0] being the x-coordinate of the i-th point and the points[j][1] being the y-coordinates of
     * the i-th point. The method returns the range of values points[rowIndex][1], where rowIndex is between 'from' (inclusive) and 'to' (exclusive), ignoring
     * values which are NaN or infinite.
     * 
     * @param points Array n x m, where m <= 2. Cannot be null.
     * @param from the initial index, inclusive
     * @param to the final index, exclusive
     * @return a range of the values of points[rowIndex][1], where from <= rowIndex < to. Can be null if the array of points is empty, 
     * from >= to, or from > points.length. It is also null when for all rowIndex between 'from' and 'to'  points[rowIndex][1] is NaN of infinite.
     */

    //this method is designed for array of points 
    public static Range getBoundedYRange(double[][] points, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int n = points.length;

        if(n == 0 || from >= to)
        {
            return null;
        }

        int legalFrom = Math.max(from, 0);
        int legalTo = Math.min(to, points.length);    

        //we need find the first index for which the y-value is not infinite or NaN
        //the alternative approach would be to initially set min to Double.POSITIVE_INFINITY and max to Double.NEGATIVE_INFINITY
        //but then we could not use if(y>max){max = y}else if(y < min) {min = y} in the main loop
        //because the code would be broken if the first 'good' y - value were the lowest
        //we would have to use if if instead of if else if, which is less efficient

        int firstGoodIndex = -1;
        for(int i = legalFrom; i<legalTo;i++)
        {
            double y = points[i][1];
            if(Double.isFinite(y))
            {
                firstGoodIndex = i;
                break;
            }
        }

        if(firstGoodIndex < 0)
        {
            return null;
        }

        double firstY = points[firstGoodIndex][1];
        double min = firstY;
        double max = firstY;

        for(int i = firstGoodIndex + 1; i<legalTo; i++)
        {
            double y = points[i][1];
            if(Double.isFinite(y))
            {
                if(y > max)
                {
                    max = y;
                }
                else if(y < min)
                {
                    min = y;
                }
            }         
        }

        Range range = max >= min ? new Range(min, max) : null;      
        return range;
    }

    //this method is designed for list of points 
    public static Range getBoundedYRange(List<double[]> points)
    {
        if(points.isEmpty())
        {
            return null;
        }

        int n = points.size();

        //we need find the first index for which the y-value is not infinite or NaN
        //the alternative approach would be to initially set min to Double.POSITIVE_INFINITY and max to Double.NEGATIVE_INFINITY
        //but then we could not use if(y>max){max = y}else if(y < min) {min = y} in the main loop
        //because the code would be broken if the first 'good' y - value were the lowest
        //we would have to use if if instead of if else if, which is less efficient

        int firstGoodIndex = -1;
        for(int i = 0; i<n;i++)
        {
            double y = points.get(i)[1];
            if(Double.isFinite(y))
            {
                firstGoodIndex = i;
                break;
            }
        }

        if(firstGoodIndex < 0)
        {
            return null;
        }

        double firstY = points.get(firstGoodIndex)[1];
        double min = firstY;
        double max = firstY;

        for(int i = firstGoodIndex + 1;i<n;i++)
        {
            double y = points.get(i)[1];
            if(Double.isFinite(y))
            {
                if(y>max)
                {
                    max = y;
                }
                else if(y<min)
                {
                    min = y;
                }
            }         
        }

        Range range = new Range(min, max);      
        return range;
    }

    public static int getIndexOfGreatestXSmallerOrEqualTo(double[][] points, double upperBound)
    {
        int n = points.length;

        int indexOfFirstXSmallerOrEqualToBound = -1;
        for(int i = 0; i<n; i++)
        {
            double x = points[i][0];
            if(x <= upperBound)
            {
                indexOfFirstXSmallerOrEqualToBound = i;
                break;
            }
        }

        if(indexOfFirstXSmallerOrEqualToBound < 0)
        {
            return -1;
        }

        int indexOfGreatestXSmallerOrEqualToBound = indexOfFirstXSmallerOrEqualToBound;
        double greatestXValueInBounds = points[indexOfFirstXSmallerOrEqualToBound][0];

        for(int i = indexOfFirstXSmallerOrEqualToBound + 1; i<n; i++)
        {
            double x = points[i][0];
            if(x <= upperBound && x > greatestXValueInBounds)
            {
                greatestXValueInBounds = x;
                indexOfGreatestXSmallerOrEqualToBound = i;
            }         
        }

        return indexOfGreatestXSmallerOrEqualToBound;
    }

    public static int getIndexOfGreatestXSmallerThan(double[][] points, double upperBound)
    {
        int n = points.length;

        int indexOfFirstXSmallerThanBound = -1;
        for(int i = 0; i<n; i++)
        {
            double x = points[i][0];
            if(x < upperBound)
            {
                indexOfFirstXSmallerThanBound = i;
                break;
            }
        }

        if(indexOfFirstXSmallerThanBound < 0)
        {
            return -1;
        }

        int indexOfGreatestXSmallerThanBound = indexOfFirstXSmallerThanBound;
        double greatestXValueInBounds = points[indexOfFirstXSmallerThanBound][0];

        for(int i = indexOfFirstXSmallerThanBound + 1; i<n; i++)
        {
            double x = points[i][0];
            if(x < upperBound && x > greatestXValueInBounds)
            {
                greatestXValueInBounds = x;
                indexOfGreatestXSmallerThanBound = i;
            }         
        }

        return indexOfGreatestXSmallerThanBound;
    }



    public static int getIndexOfSmallestXGreaterOrEqualTo(double[][] points, double lowerBound)
    {
        int n = points.length;

        int indexOfFirstXGreaterOrEqualToBound = -1;
        for(int i = 0; i<n; i++)
        {
            double x = points[i][0];
            if(x >= lowerBound)
            {
                indexOfFirstXGreaterOrEqualToBound = i;
                break;
            }
        }

        if(indexOfFirstXGreaterOrEqualToBound < 0)
        {
            return -1;
        }

        int indexOfSmallestXGreaterOrEqualToBound = indexOfFirstXGreaterOrEqualToBound;
        double smallestXValueInBounds = points[indexOfFirstXGreaterOrEqualToBound][0];

        for(int i = indexOfFirstXGreaterOrEqualToBound + 1; i<n; i++)
        {
            double x = points[i][0];
            if(x >= lowerBound && x < smallestXValueInBounds)
            {
                smallestXValueInBounds = x;
                indexOfSmallestXGreaterOrEqualToBound = i;
            }         
        }

        return indexOfSmallestXGreaterOrEqualToBound;
    }

    public static int getIndexOfSmallestXGreaterThan(double[][] points, double lowerBound)
    {
        int n = points.length;

        int indexOfFirstXGreaterThanBound = -1;
        for(int i = 0; i<n; i++)
        {
            double x = points[i][0];
            if(x > lowerBound)
            {
                indexOfFirstXGreaterThanBound = i;
                break;
            }
        }

        if(indexOfFirstXGreaterThanBound < 0)
        {
            return -1;
        }

        int indexOfSmallestXGreaterThanBound = indexOfFirstXGreaterThanBound;
        double smallestXValueInBounds = points[indexOfFirstXGreaterThanBound][0];

        for(int i = indexOfFirstXGreaterThanBound + 1; i<n; i++)
        {
            double x = points[i][0];
            if(x > lowerBound && x < smallestXValueInBounds)
            {
                smallestXValueInBounds = x;
                indexOfSmallestXGreaterThanBound = i;
            }         
        }

        return indexOfSmallestXGreaterThanBound;
    }

    public static int getIndexOfGreatestValueSmallerOrEqualTo(double[] values, double upperBound)
    {
        int n = values.length;

        int indexOfFirstValueSmallerOrEqualToBound = -1;
        for(int i = 0; i<n; i++)
        {
            double v = values[i];
            if(v <= upperBound)
            {
                indexOfFirstValueSmallerOrEqualToBound = i;
                break;
            }
        }

        if(indexOfFirstValueSmallerOrEqualToBound < 0)
        {
            return -1;
        }

        int indexOfGreatestValueSmallerOrEqualToBound = indexOfFirstValueSmallerOrEqualToBound;
        double greatestValueInBounds = values[indexOfFirstValueSmallerOrEqualToBound];

        for(int i = indexOfFirstValueSmallerOrEqualToBound + 1; i < n; i++)
        {
            double v = values[i];
            if(v <= upperBound && v > greatestValueInBounds)
            {
                greatestValueInBounds = v;
                indexOfGreatestValueSmallerOrEqualToBound = i;
            }         
        }

        return indexOfGreatestValueSmallerOrEqualToBound;
    }

    public static int getIndexOfSmallestValueGreaterOrEqualTo(double[] values, double lowerBound)
    {
        int n = values.length;

        int indexOfFirstValueGreaterOrEqualToBound = -1;
        for(int i = 0; i<n; i++)
        {
            double v = values[i];
            if(v >= lowerBound)
            {
                indexOfFirstValueGreaterOrEqualToBound = i;
                break;
            }
        }

        if(indexOfFirstValueGreaterOrEqualToBound < 0)
        {
            return -1;
        }

        int indexOfSmallestValueGreaterOrEqualToBound = indexOfFirstValueGreaterOrEqualToBound;
        double smallestValueInBounds = values[indexOfFirstValueGreaterOrEqualToBound];

        for(int i = indexOfFirstValueGreaterOrEqualToBound + 1; i<n; i++)
        {
            double v = values[i];
            if(v >= lowerBound && v < smallestValueInBounds)
            {
                smallestValueInBounds = v;
                indexOfSmallestValueGreaterOrEqualToBound = i;
            }         
        }

        return indexOfSmallestValueGreaterOrEqualToBound;
    }

    public static OrderedIntegerPair getIndicesOfExtrema(double[] values)
    { 
        return getIndicesOfExtrema(values, 0, values.length);
    }

    //from inclusive, to exclusive
    public static OrderedIntegerPair getIndicesOfExtrema(double[] values, int from, int to)
    {        
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        int n = to - from;  

        int minIndex = -1;
        int maxIndex = -1;

        if(n < 1)
        {
            return new OrderedIntegerPair(minIndex, maxIndex);
        }

        double min = values[from];
        double max = values[from];

        for(int i = from + 1; i < to; i++)
        {
            double x = values[i];
            if(x < min)
            {
                min = x;
                minIndex = i;
            }
            else if(x > max)
            {
                max = x;
                maxIndex = i;
            }
        }

        return new OrderedIntegerPair(minIndex, maxIndex);
    }

    public static OrderedIntegerPair getIndicesOfYExtrema(double[][] points)
    { 
        return getIndicesOfYExtrema(points, 0, points.length);
    }

    //from inclusive, to exclusive
    public static OrderedIntegerPair getIndicesOfYExtrema(double[][] points, int from, int to)
    {        
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int n = to - from;  

        int minIndex = -1;
        int maxIndex = -1;

        if(n < 1)
        {
            return new OrderedIntegerPair(minIndex, maxIndex);
        }

        double min = points[from][1];
        double max = points[from][1];

        for(int i = from + 1; i < to; i++)
        {
            double y = points[i][1];
            if(y < min)
            {
                min = y;
                minIndex = i;
            }
            else if(y > max)
            {
                max = y;
                maxIndex = i;
            }
        }

        return new OrderedIntegerPair(minIndex, maxIndex);
    }

    public static double getMinimum(double[] values)
    {
        return getMinimum(values, 0, values.length);
    }

    //from inclusive, to exclusive
    public static double getMinimum(double[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        double min = Double.POSITIVE_INFINITY;
        for(int i = from; i < to;i++)
        {
            double x = values[i];
            if(x < min)
            {
                min = x;
            }
        }

        return min;
    }

    public static double getMinimum(double[][] values)
    {
        double min = Double.POSITIVE_INFINITY;

        for(double[] row: values)
        {
            for(double x: row)
            {
                if(x < min)
                {
                    min = x;
                }
            }
        }

        return min;
    }

    public static double getNumericMinimum(double[] values)
    {
        double min = Double.POSITIVE_INFINITY;
        for(double x: values)
        {
            if(!Double.isNaN(x))
            {
                if(x<min)
                {
                    min = x;
                }
            }
        }

        return min;
    }


    public static double getNumericFiniteMinimum(double[] values)
    {
        Validation.requireNonNullAndNonEmptyParameterName(values, "values");

        double min = Double.MAX_VALUE;
        for(double x: values)
        {
            if(Double.isFinite(x))
            {
                if(x<min)
                {
                    min = x;
                }
            }
        }

        return min;
    }

    public static double getNumericFiniteMinimum(double[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        double min = Double.MAX_VALUE;
        for(int i = from; i<to;i++)
        {
            double x = values[i];
            if(Double.isFinite(x))
            {
                if(x < min)
                {
                    min = x;
                }
            }
        }

        return min;
    }


    public static double getNumericFiniteMaximum(double[] values)
    {
        Validation.requireNonNullAndNonEmptyParameterName(values, "values");

        double max = -Double.MAX_VALUE;
        for(double x: values)
        {
            if(Double.isFinite(x))
            {
                if(x > max)
                {
                    max = x;
                }
            }
        }

        return max;
    }

    public static double getNumericFiniteMaximum(double[] values, int from,int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        double max = -Double.MAX_VALUE;
        for(int i = from; i<to;i++)
        {
            double x = values[i];
            if(Double.isFinite(x))
            {
                if(x > max)
                {
                    max = x;
                }
            }
        }

        return max;
    }


    public static double getNumericMinimum(double[][] values)
    {
        Validation.requireNonNullAndNonEmptyParameterName(values, "values");

        double min = Double.POSITIVE_INFINITY;
        for(double[] xs: values)
        {
            for(double x: xs)
            {
                if(!Double.isNaN(x))
                {
                    if(x < min)
                    {
                        min = x;
                    }
                }
            }           
        }

        return min;
    }

    public static int getMinimumIndex(double[] values)
    {
        return getMinimumIndex(values, 0, values.length);
    }

    //from inclusive, to exclusive
    public static int getMinimumIndex(double[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        int minIndex = -1;
        double min = Double.POSITIVE_INFINITY;

        for(int i = from; i<to; i++)
        {
            double x = values[i];
            if(x < min)
            {
                min = x;
                minIndex = i;
            }
        }

        return minIndex;
    }

    public static int getMinimumIndex(int[] values)
    {
        return getMinimumIndex(values, 0, values.length);
    }

    //from inclusive, to exclusive
    public static int getMinimumIndex(int[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        int minIndex = -1;
        double min = Double.POSITIVE_INFINITY;

        for(int i = from; i<to; i++)
        {
            double x = values[i];
            if(x < min)
            {
                min = x;
                minIndex = i;
            }
        }

        return minIndex;
    }


    public static int getMaximumIndex(double[] values)
    {
        return getMaximumIndex(values, 0, values.length);
    }

    //from inclusive, to exclusive
    public static int getMaximumIndex(double[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        int maxIndex = -1;
        double max = Double.NEGATIVE_INFINITY;

        for(int i = from; i<to; i++)
        {
            double x = values[i];
            if(x > max)
            {
                max = x;
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    public static int getMaximumIndex(int[] values)
    {
        return getMaximumIndex(values, 0, values.length);
    }

    //from inclusive, to exclusive
    public static int getMaximumIndex(int[] values, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.length, "to");

        int maxIndex = -1;
        double max = Double.NEGATIVE_INFINITY;

        for(int i = from; i < to; i++)
        {
            int x = values[i];
            if(x > max)
            {
                max = x;
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    public static int getMinimumXIndex(double[][] points)
    {
        return getMinimumXIndex(points, 0, points.length);       
    }

    public static int getMinimumXIndex(double[][] points, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int minIndex = -1;
        double min = Double.POSITIVE_INFINITY;

        for(int i = from; i<to; i++)
        {
            double x = points[i][0];
            if(x < min)
            {
                min = x;
                minIndex = i;
            }
        }

        return minIndex;
    }

    public static int getMaximumXIndex(double[][] points)
    {
        return getMaximumXIndex(points, 0, points.length);
    }

    public static int getMaximumXIndex(double[][] points, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int maxIndex = -1;
        double max = Double.NEGATIVE_INFINITY;

        for(int i = from; i<to; i++)
        {
            double x = points[i][0];
            if(x > max)
            {
                max = x;
                maxIndex = i;
            }
        }

        return maxIndex;
    }


    public static int getMinimumYIndex(double[][] points)
    {
        return getMinimumYIndex(points, 0, points.length);
    }

    public static int getMinimumYIndex(double[][] points, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int minIndex = -1;
        double min = Double.POSITIVE_INFINITY;

        for(int i = from; i<to; i++)
        {
            double x = points[i][1];
            if(x < min)
            {
                min = x;
                minIndex = i;
            }
        }

        return minIndex;
    }

    public static int getMaximumYIndex(double[][] points)
    {
        return getMaximumYIndex(points, 0, points.length);
    }

    //from inclusive, to exclusive
    public static int getMaximumYIndex(double[][] points, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        int maxIndex = -1;
        double max = Double.NEGATIVE_INFINITY;

        for(int i = from; i<to; i++)
        {
            double x = points[i][1];
            if(x > max)
            {
                max = x;
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    public static <T> T getMaximum(T[] data, Comparator<? super T> comparator)
    {
        Validation.requireNonNullAndNonEmptyParameterName(data, "data");

        int n = data.length;

        T maximum = data[0];
        for(int i = 0; i<n; i++)
        {
            T currentElement = data[i];
            int r = comparator.compare(maximum, currentElement);

            if(r < 0)
            {
                maximum = currentElement;
            }
        }
        return maximum;       
    }

    public static <T> T getMinimum(T[] data, Comparator<? super T> comparator)
    {
        Validation.requireNonNullAndNonEmptyParameterName(data, "data");

        int n = data.length;

        T minimum = data[0];
        for(int i = 0; i<n; i++)
        {
            T currentElement = data[i];
            int r = comparator.compare(minimum, currentElement);

            if(r > 0)
            {
                minimum = currentElement;
            }
        }
        return minimum;       
    }

    public static <T> T getMinimum(Collection<T> data, Comparator<? super T> comparator)
    {
        Validation.requireNonNullAndNonEmptyParameterName(data, "data");

        Iterator<T> iterator = data.iterator();

        T minimum = iterator.next();

        while(iterator.hasNext())
        {
            T currentElement = iterator.next();
            int r = comparator.compare(minimum, currentElement);

            if(r > 0)
            {
                minimum = currentElement;
            }
        }
        return minimum;       
    }

    public static <T> T getMaximum(Collection<T> data, Comparator<? super T> comparator)
    {
        Validation.requireNonNullAndNonEmptyParameterName(data, "data");

        Iterator<T> iterator = data.iterator();

        T maximum = iterator.next();

        while(iterator.hasNext())
        {
            T currentElement = iterator.next();
            int r = comparator.compare(maximum, currentElement);

            if(r < 0)
            {
                maximum = currentElement;
            }
        }
        return maximum;       
    }

    public static double getMaximumX(double[][] points)
    {
        return getMaximum(points, new AbscissaComparator())[0];
    }

    public static double[] getPointWithMaximumX(double[][] points)
    {
        return getMaximum(points, new AbscissaComparator());
    }

    public static double getMaximumX(List<double[]> points)
    {
        return getMaximum(points, new AbscissaComparator())[0];
    }

    public static double getMinimumX(double[][] points)
    {   
        return getMinimum(points, new AbscissaComparator())[0];
    }

    public static double getMinimumX(List<double[]> points)
    {
        return getMinimum(points, new AbscissaComparator())[0];
    }

    public static double getMaximumY(double[][] points)
    {
        return getMaximum(points, new OrdinateComparator())[1];
    }

    public static double getMaximumY(List<double[]> points)
    {
        return getMaximum(points, new OrdinateComparator())[1];
    }

    public static double getMinimumY(double[][] points)
    {   
        return getMinimum(points, new OrdinateComparator())[1];
    }

    public static double getMinimumY(List<double[]> points)
    {
        return getMinimum(points, new OrdinateComparator())[1];
    }


    public static boolean equal(double[][] first, double[][] second) 
    {
        if (first == null) {
            return (second == null);
        }
        if (second == null) {
            return false;  
        }
        if (first.length != second.length) {
            return false;
        }
        for (int i = 0; i < first.length; i++) 
        {
            if (!Arrays.equals(first[i], second[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean equal(int[][] first, int[][] second) 
    {
        if (first == null) {
            return (second == null);
        }
        if (second == null) {
            return false;  
        }
        if (first.length != second.length) {
            return false;
        }
        for (int i = 0; i < first.length; i++) 
        {
            if (!Arrays.equals(first[i], second[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isConstant(Collection<?> collection)
    {
        Iterator<?> it = collection.iterator();

        if(!it.hasNext())
        {
            return true; //empty collection has all elements equal
        }

        Object firstElement = it.next();

        while(it.hasNext())
        {
            Object next = it.next();
            if(!Objects.equals(firstElement, next))
            {
                return false;
            }
        }

        return true;
    }

    public static float[] toFloat(double[] doubleArray)
    {
        if(doubleArray == null)
        {
            return null;
        }

        float[] floatArray = new float[doubleArray.length];
        for (int i = 0 ; i < doubleArray.length; i++)
        {
            floatArray[i] = (float) doubleArray[i];
        }

        return floatArray;
    }

    public static double[] toDouble(float[] floatArray)
    {
        if(floatArray == null)
        {
            return null;
        }

        double[] doubleArray = new double[floatArray.length];
        for (int i = 0 ; i < floatArray.length; i++)
        {
            doubleArray[i] = floatArray[i];
        }

        return doubleArray;
    }

    public static double[][] toDouble(float[][] floatArray)
    {
        if(floatArray == null)
        {
            return null;
        }

        double[][] doubleArray = new double[floatArray.length][];
        for (int i = 0 ; i < floatArray.length; i++)
        {
            doubleArray[i] = toDouble(floatArray[i]);
        }

        return doubleArray;
    }

    public static double[] createAndPopulateConstantArray(double value, int arrayLength)
    {
        double[] array = new double[arrayLength];

        for(int i = 0; i<arrayLength; i++)
        {
            array[i] = value;
        }

        return array;
    }
}
