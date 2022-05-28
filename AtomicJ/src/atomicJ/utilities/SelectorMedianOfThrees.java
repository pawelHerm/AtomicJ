package atomicJ.utilities;

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


/**
 * Implementation of Tony Hoare's quickselect algorithm.
 * Running time is generally O(n), but worst case is O(n^2)
 * Pivot choice is median of three method, providing better performance
 * than a random pivot for partially sorted data.
 * http://en.wikipedia.org/wiki/Quickselect
 * @author Jon Renner
 */
public class SelectorMedianOfThrees {

    public static int select(double[] array, int n, int size) {
        return recursiveSelect(array, 0, size - 1, n);
    }

    private static int partition(double[] array, int left, int right, int pivot) {
        double pivotValue = array[pivot];
        swap(array, right, pivot);
        int storage = left;
        for (int i = left; i < right; i++) {
            if (array[i] < pivotValue) {
                swap(array, storage, i);
                storage++;
            }
        }
        swap(array, right, storage);
        return storage;
    }

    private static int recursiveSelect(double[] array, int left, int right, int k) {
        if (left == right) return left;
        int pivotIndex = medianOfThreePivot(array, left, right);
        int pivotNewIndex = partition(array, left, right, pivotIndex);
        int pivotDist = (pivotNewIndex - left) + 1;
        int result;
        if (pivotDist == k) {
            result = pivotNewIndex;
        }
        else if (k < pivotDist) {
            result = recursiveSelect(array, left, pivotNewIndex - 1, k);
        } else {
            result = recursiveSelect(array, pivotNewIndex + 1, right, k - pivotDist);
        }
        return result;
    }

    /** Median of Three has the potential to outperform a random pivot, especially for partially sorted arrays */
    private static int medianOfThreePivot(double[] array, int leftIdx, int rightIdx) {
        double left = array[leftIdx];
        int midIdx = (leftIdx + rightIdx) / 2;
        double mid = array[midIdx];
        double right = array[rightIdx];

        // spaghetti median of three algorithm
        // does at most 3 comparisons
        if (left > mid) {
            if (mid >right) {
                return midIdx;
            } else if (left>right) {
                return rightIdx;
            } else {
                return leftIdx;
            }
        } else {
            if (left>right) {
                return leftIdx;
            } else if (mid > right) {
                return rightIdx;
            } else {
                return midIdx;
            }
        }
    }

    private static void swap(double[] array, int left, int right) {
        double tmp = array[left];
        array[left] = array[right];
        array[right] = tmp;
    }
}
