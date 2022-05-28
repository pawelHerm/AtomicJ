package atomicJ.gui;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PathSegment
{
    MOVETO(PathIterator.SEG_MOVETO, 1)
    {
        @Override
        public PathSegment getSegmentAfterPointRemoval() 
        {
            return null;
        }

        @Override
        public List<PathSegment> getSegmentsAfterPrecedingMoveToRemoval() {
            return Collections.singletonList(MOVETO);
        }

        @Override
        public Path2D addSegment(Path2D path, double[] coords) 
        {
            path.moveTo(coords[0], coords[1]);
            return path;
        }
    }, 
    LINETO(PathIterator.SEG_LINETO, 1)
    {
        @Override
        public PathSegment getSegmentAfterPointRemoval() {
            return null;
        }

        @Override
        public List<PathSegment> getSegmentsAfterPrecedingMoveToRemoval() 
        {
            return Collections.singletonList(MOVETO);
        }

        @Override
        public Path2D addSegment(Path2D path, double[] coords) 
        {
            path.lineTo(coords[0], coords[1]);
            return path;
        }
    }, 

    QUADTO(PathIterator.SEG_QUADTO, 2)
    {
        @Override
        public PathSegment getSegmentAfterPointRemoval()
        {
            return LINETO;
        }

        @Override
        public List<PathSegment> getSegmentsAfterPrecedingMoveToRemoval() {
            return Arrays.asList(PathSegment.MOVETO,PathSegment.LINETO);
        }

        @Override
        public Path2D addSegment(Path2D path, double[] coords)
        {
            path.quadTo(coords[0], coords[1], coords[2], coords[3]);
            return path;
        }
    }, 

    CUBICTO(PathIterator.SEG_CUBICTO,3) {
        @Override
        public PathSegment getSegmentAfterPointRemoval()
        {
            return QUADTO;
        }

        @Override
        public List<PathSegment> getSegmentsAfterPrecedingMoveToRemoval()
        {
            return Arrays.asList(PathSegment.MOVETO,PathSegment.QUADTO);
        }

        @Override
        public Path2D addSegment(Path2D path, double[] coords)
        {
            path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
            return path;
        }
    },

    CLOSE(PathIterator.SEG_CLOSE,0) 
    {
        @Override
        public PathSegment getSegmentAfterPointRemoval() 
        {
            return CLOSE;
        }

        @Override
        public List<PathSegment> getSegmentsAfterPrecedingMoveToRemoval() {
            return Collections.emptyList();
        }

        @Override
        public Path2D addSegment(Path2D path, double[] coords) 
        {
            path.closePath();
            return path;
        }
    };

    private final int segmentType;

    private final int pointCount;

    PathSegment(int segmentType, int pointCount)
    {
        this.segmentType = segmentType;
        this.pointCount = pointCount;
    }

    public int getSegmentType()
    {
        return segmentType;
    }

    public int getPointCount()
    {
        return pointCount;
    }

    public abstract PathSegment getSegmentAfterPointRemoval();
    public abstract List<PathSegment> getSegmentsAfterPrecedingMoveToRemoval();
    public abstract Path2D addSegment(Path2D path, double[] coords);

    public static PathSegment instanceFor(int segmentType)
    {
        for(PathSegment segment : PathSegment.values())
        {
            if(segment.segmentType == segmentType)
            {
                return segment;
            }
        }

        throw new IllegalArgumentException("No PathSegment known for the segmentType " + segmentType);
    }
}