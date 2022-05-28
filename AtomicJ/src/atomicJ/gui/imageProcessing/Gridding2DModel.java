package atomicJ.gui.imageProcessing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.gui.Gridding2DSettings;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.imageProcessing.Gridding2DTransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.MultiMap;

public class Gridding2DModel extends ImageBatchSimpleProcessingModel
{
    public static final String ROW_COUNT = "RowCount";
    public static final String COLUMN_COUNT = "ColumnCount";
    public static final String INTERPOLATION_METHOD = "InterpolationMethod";
    public static final String UNIQUE_GRID_SIZE = "UniqueGridSize";

    private int rowCount = 0;
    private int columnCount = 0;
    private MultiMap<String, GridSize> gridSizes;
    private GridSize uniqueGridSize; //null when there is no unique gridSize;

    private InterpolationMethod2D interpolationMethod = InterpolationMethod2D.BILINEAR;

    public Gridding2DModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter) 
    {
        super(manager, channelFilter, true, false);

        MultiMap<String, Channel2D> channelsToProcessMap = getSelectedResourceChannelsToProcess();
        Collection<Channel2D> channelsToProcess = channelsToProcessMap.allValues();
        GridSize initialGridSize = calculateInitialGridSize(channelsToProcess);

        this.rowCount = initialGridSize.getRowCount();
        this.columnCount = initialGridSize.getColumnCount();
        this.gridSizes = calculateGidSizes(channelsToProcess);
        Set<GridSize> differentGridSizes = new HashSet<>(gridSizes.allValues());
        this.uniqueGridSize = differentGridSizes.size() == 1 ? differentGridSizes.iterator().next() : null;
    }

    private MultiMap<String, GridSize> calculateGidSizes(Collection<Channel2D> channels)
    {
        MultiMap<String, GridSize> gridSizes = new MultiMap<>();

        for(Channel2D channel : channels)
        {
            GridSize gridSize;
            Channel2DData channelData = channel.getChannelData();
            if(channelData instanceof GridChannel2DData)
            {
                Grid2D grid = ((GridChannel2DData) channelData).getGrid();
                gridSize = new GridSize(grid.getRowCount(), grid.getColumnCount());
            }
            else
            {
                gridSize = new GridSize(channelData.getItemCount(), 0);
            }

            gridSizes.put(channel.getIdentifier(), gridSize);
        }

        return gridSizes;
    }

    private GridSize calculateInitialGridSize(Collection<Channel2D> channels)
    {
        int greatestRowCount = 0;
        int greatestColumnCount = 0;

        for(Channel2D channel : channels)
        {
            Grid2D grid = channel.getDefaultGriddingGrid();
            greatestRowCount = Math.max(greatestRowCount, grid.getRowCount());
            greatestColumnCount = Math.max(greatestColumnCount, grid.getColumnCount());
        }

        GridSize initialGridSize = new GridSize(2*greatestRowCount, 2*greatestColumnCount);

        return initialGridSize;
    }

    public GridSize getUniqueGridSize()
    {
        return uniqueGridSize;
    }

    @Override
    protected void handleIdentifierSelectionChange(String identifier, boolean selectedOld, boolean selectedNew)
    {
        super.handleIdentifierSelectionChange(identifier, selectedOld, selectedNew);

        MultiMap<String, Channel2D> channelsToProcessMap = getSelectedResourceChannelsToProcess();
        Collection<Channel2D> channelsToProcess = channelsToProcessMap.allValues();
        MultiMap<String, GridSize> gridSizesNew = calculateGidSizes(channelsToProcess);

        if(!Objects.equals(gridSizes, gridSizesNew))
        {
            this.gridSizes = gridSizesNew;

            Set<GridSize> differentGridSizes = new HashSet<>(gridSizes.allValues());
            GridSize uniqueGridSizeNew = differentGridSizes.size() == 1 ? differentGridSizes.iterator().next() : null;
            if(!Objects.equals(this.uniqueGridSize, uniqueGridSizeNew))
            {
                GridSize uniqueGridSizeOld = this.uniqueGridSize;
                this.uniqueGridSize = uniqueGridSizeNew;

                firePropertyChange(UNIQUE_GRID_SIZE, uniqueGridSizeOld, uniqueGridSizeNew);
            }
        }
    }

    public static class GridSize
    {
        private final int rowCount;
        private final int columnCount;

        public GridSize(int rowCount, int columnCount)
        {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
        }

        public int getRowCount()
        {
            return rowCount;
        }

        public int getColumnCount()
        {
            return columnCount;
        }

        @Override
        public int hashCode()
        {
            int hashCode = 17;

            hashCode = 32*hashCode + rowCount;
            hashCode = 32*hashCode + columnCount;

            return hashCode;
        }

        @Override
        public boolean equals(Object that)
        {
            if(!(that instanceof GridSize))
            {
                return false;
            }

            GridSize thatGridSize = (GridSize)that;

            if(this.rowCount != thatGridSize.rowCount || this.columnCount != thatGridSize.columnCount)
            {
                return false;
            }

            return true;
        }
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public void setRowCount(int rowCountNew)
    {
        if(this.rowCount != rowCountNew)
        {
            int rowCountOld = this.rowCount;
            this.rowCount = rowCountNew;

            firePropertyChange(ROW_COUNT, rowCountOld, rowCountNew);

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public void setGridSize(int rowCountNew, int columnCountNew)
    {
        int rowCountOld = this.rowCount;
        this.rowCount = rowCountNew;

        int columnCountOld = this.columnCount;
        this.columnCount = columnCountNew;

        if( this.rowCount != rowCountNew || this.columnCount != columnCountNew)
        {
            firePropertyChange(ROW_COUNT, rowCountOld, rowCountNew);
            firePropertyChange(COLUMN_COUNT, columnCountOld, columnCountNew); 

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public int getColumnCount()
    {
        return columnCount;
    }

    public void setColumnCount(int columnCountNew)
    {
        if(this.columnCount != columnCountNew)
        {
            int columnCountOld = this.columnCount;
            this.columnCount = columnCountNew;

            firePropertyChange(COLUMN_COUNT, columnCountOld, columnCountNew);      

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public InterpolationMethod2D getInterpolationMethod()
    {
        return interpolationMethod;
    }

    public void setInterpolationMethod(InterpolationMethod2D interpolationMethodNew)
    {
        if(!Objects.equals(this.interpolationMethod, interpolationMethodNew))
        {
            InterpolationMethod2D interpolationMethodOld = this.interpolationMethod;
            this.interpolationMethod = interpolationMethodNew;

            firePropertyChange(INTERPOLATION_METHOD, interpolationMethodOld, interpolationMethodNew);

            checkIfApplyEnabled();         
            updatePreview();
        }
    }

    protected boolean calculateIfApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && rowCount >= 0 && columnCount >= 0 && interpolationMethod != null;

        return applyEnabled;
    }

    @Override
    protected Channel2DDataTransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        Gridding2DSettings settings = new Gridding2DSettings(interpolationMethod, rowCount, columnCount);
        Channel2DDataTransformation tr = new Gridding2DTransformation(settings);
        return tr;
    }
}