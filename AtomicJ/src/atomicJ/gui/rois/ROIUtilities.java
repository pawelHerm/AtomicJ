package atomicJ.gui.rois;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atomicJ.data.Grid2D;
import atomicJ.gui.Identifiable;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.GeometryUtilities;
import atomicJ.utilities.GraphUtilities;
import atomicJ.utilities.Pair;
import atomicJ.utilities.UnOrderedPair;


public class ROIUtilities
{    
    public static boolean[][] getInsidnessArray(ROI roi, Grid2D grid)
    {
        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        final boolean[][] insidness = new boolean[rowCount][columnCount];

        roi.addPointsInside(grid, new GridPointRecepient()
        {            
            @Override
            public void addPoint(int row, int column)
            {
                insidness[row][column] = true;                
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    boolean[] row = insidness[i];
                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        row[j] = true;
                    }
                }
            }
        });

        return insidness;
    }

    public static <E extends ROI> ROI composeROIs(Collection<E> rois, Object key)
    {
        int roiCount = rois.size();
        boolean singleROI = (roiCount == 1);

        if(singleROI)
        {
            return new ROIWrapper(rois.iterator().next(), key);
        }

        Set<Pair<E>> overlapingPairs = getOverlapingPairs(rois);
        Set<Set<E>> partitionROI = GraphUtilities.findConnectedComponents(rois, overlapingPairs);

        List<ROI> disjointROIs = new ArrayList<>();

        int index = 0;

        for(Set<E> overlappingROIs : partitionROI)
        {
            if(overlappingROIs.isEmpty())
            {
                continue;
            }

            if(overlappingROIs.size() == 1)
            {
                disjointROIs.addAll(overlappingROIs);
            }
            else
            {
                disjointROIs.add(ROIComposite.getROIForRois(overlappingROIs, key + Integer.toString(index++)));
            }
        }

        ROI composed = new ROIDisjointComposition(disjointROIs, key);

        return composed;
    }

    public static <E extends ROI> Set<Set<E>> partitionConnectedROIs(Collection<E> rois)
    {
        if(rois.size() == 1)
        {
            Set<Set<E>> partitionROI = new LinkedHashSet<>();
            partitionROI.add(new LinkedHashSet<>(rois));

            return partitionROI;
        }

        Set<Pair<E>> overlapingPairs = getOverlapingPairs(rois);
        Set<Set<E>> partitionROI = GraphUtilities.findConnectedComponents(rois, overlapingPairs);

        return partitionROI;
    }

    public static <E extends ROI> Set<E> getOverlappingROIs(ROI roi, Collection<E> other)
    {
        Set<E> overlapping = new LinkedHashSet<>();

        for(E r : other)
        {
            boolean intersect = GeometryUtilities.isShapeAIntersectingB(roi.getROIShape(), r.getROIShape());
            if(intersect)
            {
                overlapping.add(r);
            }
        }

        return overlapping;
    }

    private static <E extends ROI>  Set<Pair<E>> getOverlapingPairs(Collection<E> rois)
    {
        List<E> shapeList = new ArrayList<>(rois);
        int n = rois.size();

        Set<Pair<E>> overlappingPairs = new LinkedHashSet<>();

        for(int i = 0; i<n - 1; i++)
        {
            E firstROI = shapeList.get(i);

            for(int j = i + 1; j<n; j++)
            {
                E secondROI = shapeList.get(j);
                boolean intersect = GeometryUtilities.isShapeAIntersectingB(firstROI.getROIShape(), secondROI.getROIShape());
                if(intersect)
                {
                    overlappingPairs.add(new UnOrderedPair<>(firstROI, secondROI));
                }
            }
        }

        return overlappingPairs;
    }

    public static Collection<ROIDrawable> selectROIs(Map<Object, ROIDrawable> allRois, Collection<Object> keys)
    {
        List<ROIDrawable> selectedROIs = new ArrayList<>();

        for(Object key : keys)
        {
            ROIDrawable roi = allRois.get(key);

            if(roi != null)
            {
                selectedROIs.add(roi);
            }
        }

        return selectedROIs;
    }

    public static Collection<ROIDrawable> selectROIsFromIds(Map<Object, ROIDrawable> allRois, Collection<IdentityTag> ids)
    {
        List<ROIDrawable> selectedROIs = new ArrayList<>();

        for(IdentityTag id : ids)
        {
            ROIDrawable roi = allRois.get(id.getKey());

            if(roi != null)
            {
                selectedROIs.add(roi);
            }
        }

        return selectedROIs;
    }

    public static List<IdentityTag> getIds(Collection<? extends Identifiable> identifiables)
    {
        List<IdentityTag> ids = new ArrayList<>();
        for(Identifiable r : identifiables)
        {
            ids.add(r.getIdentityTag());
        }

        return ids;
    }

    public static Integer getUnionKey(Collection<ROIDrawable> rois)
    {
        Integer key = Integer.MAX_VALUE;

        for(ROIDrawable roi : rois)
        {
            Integer currentKey = roi.getKey();
            if(currentKey < key)
            {
                key = currentKey;
            }
        }

        return key;
    }

    public static <E extends ROI> Map<E, Set<E>> findPossibleDiffrences(Collection<E> rois)
    {
        Map<E, Set<E>> possibleDifferences = new LinkedHashMap<>();

        for(E r : rois)
        {
            Collection<E> otherROIs = new LinkedHashSet<>(rois);
            otherROIs.remove(r);

            Set<E> overlappingROIs = getOverlappingROIs(r, otherROIs);
            if(!overlappingROIs.isEmpty())
            {
                possibleDifferences.put(r, overlappingROIs);
            }
        }

        return possibleDifferences;
    }
}
