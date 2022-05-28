package atomicJ.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atomicJ.utilities.GraphUtilities;
import atomicJ.utilities.Pair;
import atomicJ.utilities.UnOrderedPair;

public class IndexRange 
{
    private final int minIndex;
    private final int maxIndex;

    //immutable   
    public IndexRange(int minIndex, int maxIndex)
    {
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
    }

    //including both minIndex and maxIndex, must be non-negative
    public int getLengthIncludingEdges()
    {
        int length = Math.max(0, maxIndex - minIndex + 1);
        return length;
    }

    public int getMinIndex()
    {
        return minIndex;
    }

    public int getMaxIndex()
    {
        return maxIndex;
    }

    public boolean contains(int index)
    {
        boolean contains = maxIndex >= index && minIndex <= index;
        return contains;
    }

    public boolean intersects(IndexRange other)
    {
        boolean intersects = contains(other.getMinIndex()) || contains(other.getMaxIndex()) || other.contains(this.getMinIndex()); // it is NOT necessary to add || other.contains(thie.getMaxIndex())

        return intersects;
    }

    public boolean isWellFormed(int arrayLength)
    {
        boolean wellFormed = minIndex < maxIndex && minIndex >= 0 && minIndex < arrayLength;

        return wellFormed;
    }

    public static IndexRange compose(Collection<IndexRange> indexRanges)
    {
        List<IndexRange> indexRangeList = new ArrayList<>(indexRanges);

        IndexRange firstIndex = indexRangeList.get(0);
        int smallestMinIndex = firstIndex.getMinIndex();
        int greatestMaxIndex = firstIndex.getMaxIndex();

        for(int i = 0; i<indexRangeList.size();i++)
        {
            IndexRange range = indexRangeList.get(i);
            smallestMinIndex = Math.min(smallestMinIndex, range.getMinIndex());
            greatestMaxIndex = Math.max(greatestMaxIndex, range.getMaxIndex());
        }

        return new IndexRange(smallestMinIndex, greatestMaxIndex);
    }

    public static List<IndexRange> simplify(Collection<IndexRange> indexRanges)
    {
        List<IndexRange> simplifiedRanges = new ArrayList<>();
        Set<Set<IndexRange>> partitionedIntoOverlappingGroups = partitionIndexRanges(indexRanges);

        for(Set<IndexRange> set : partitionedIntoOverlappingGroups)
        {
            simplifiedRanges.add(compose(set));
        }

        return simplifiedRanges;
    }

    @Override
    public String toString()
    {
        String s = "Minimal index: " + this.minIndex + ", maximal index: " + this.maxIndex;
        return s;
    }


    private static Set<Set<IndexRange>> partitionIndexRanges(Collection<IndexRange> indexRanges)
    {
        int n = indexRanges.size();

        if(n == 1)
        {
            Set<Set<IndexRange>> partitionROI = new LinkedHashSet<>();
            partitionROI.add(new LinkedHashSet<>(indexRanges));

            return partitionROI;
        }

        Set<Pair<IndexRange>> overlapingPairs = getOverlapingPairs(indexRanges);
        Set<Set<IndexRange>> partitionROI = GraphUtilities.findConnectedComponents(indexRanges, overlapingPairs);

        return partitionROI;
    }

    private static Set<Pair<IndexRange>> getOverlapingPairs(Collection<IndexRange> indexRanges)
    {
        List<IndexRange> indexRangesList = new ArrayList<>(indexRanges);

        int n = indexRanges.size();

        Set<Pair<IndexRange>> overlappingPairs = new LinkedHashSet<>();

        for(int i = 0; i<n - 1; i++)
        {
            IndexRange firstRange = indexRangesList.get(i);

            for(int j = i + 1; j<n; j++)
            {
                IndexRange secondRange = indexRangesList.get(j);
                boolean intersect = secondRange.intersects(firstRange);
                if(intersect)
                {
                    overlappingPairs.add(new UnOrderedPair<>(firstRange, secondRange));
                }
            }
        }

        return overlappingPairs;
    }

    @Override
    public int hashCode()
    {
        int result = Integer.hashCode(minIndex);
        result = 31*result + Integer.hashCode(maxIndex);

        return result;
    }

    @Override
    public boolean equals(Object that)
    {
        if(that instanceof IndexRange)
        {
            if(this.minIndex != ((IndexRange) that).minIndex)
            {
                return false;
            }
            if(this.maxIndex != ((IndexRange) that).maxIndex)
            {
                return false;
            }

            return true;
        }

        return false;
    }
}
