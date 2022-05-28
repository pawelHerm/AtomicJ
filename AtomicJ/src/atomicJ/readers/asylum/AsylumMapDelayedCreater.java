package atomicJ.readers.asylum;

import java.awt.geom.Point2D;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.Quantities;
import atomicJ.readers.MapDelayedCreator;
import atomicJ.sources.FlexibleMapSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;

public class AsylumMapDelayedCreater implements MapDelayedCreator
{
    private final Path parentDirectory;

    private final int rowCount;
    private final int columnCount;

    public AsylumMapDelayedCreater(Path parentDirectoryPath, int rowCount, int columnCount)
    {
        this.parentDirectory= parentDirectoryPath.toAbsolutePath();

        this.columnCount = columnCount;
        this.rowCount = rowCount;
    }

    @Override
    public boolean accepts(SimpleSpectroscopySource source) 
    {       
        File correspondingFile = source.getCorrespondingFile();
        return correspondingFile.toPath().startsWith(parentDirectory);       
    }

    @Override
    public MapSource<?> buildMapSource(List<SimpleSpectroscopySource> sources)
    {
        int n = sources.size();

        if(n < 2 || rowCount*columnCount/2 >n)
        {
            return null;
        }

        List<Point2D> nodes = new ArrayList<>();
        for(SimpleSpectroscopySource source : sources)
        {
            Point2D node = source.getRecordingPoint();
            if(node != null)
            {
                nodes.add(node);
            }
        }

        Grid2D grid = Grid2D.getGrid(nodes, 1e-3);

        MapSource<?> mapSource = (grid != null) ?  mapSource = new MapGridSource(parentDirectory.toFile(), sources, grid)
                : new FlexibleMapSource(parentDirectory.toFile(), sources, new ChannelDomainIdentifier(FlexibleChannel2DData.calculateProbingDensityGeometryPoints(nodes), ChannelDomainIdentifier.getNewDomainKey()), Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

        return mapSource;
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31*result + this.parentDirectory.hashCode();
        result = 31*result + this.rowCount;
        result = 31*result + this.columnCount;

        return result;
    }

    @Override
    public boolean equals(Object that)
    {
        if(that instanceof AsylumMapDelayedCreater)
        {            
            AsylumMapDelayedCreater thatCreator = (AsylumMapDelayedCreater) that;
            return this.parentDirectory.equals(thatCreator.parentDirectory) && this.columnCount == thatCreator.columnCount && this.rowCount == thatCreator.rowCount;
        }

        return false;
    }
}
