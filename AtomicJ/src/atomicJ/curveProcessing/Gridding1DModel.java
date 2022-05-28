package atomicJ.curveProcessing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import atomicJ.analysis.InterpolationMethod1D;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DMinimumSizeFilter;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.utilities.MultiMap;

public class Gridding1DModel<R extends SpectroscopyResource> extends CurveBatchProcessingModel<R>
{
    public static final String COLUMN_COUNT = "ColumnCount";
    public static final String INTERPOLATION_METHOD = "InterpolationMethod";
    public static final String UNIQUE_COLUMN_COUNT = "UniqueColumnCount";

    private int columnCount = 0;
    private MultiMap<Object, Integer> columnCounts;
    private int uniqueColumnCount; //null when there is no unique gridSize;

    private InterpolationMethod1D interpolationMethod = InterpolationMethod1D.LINEAR;

    public Gridding1DModel(ResourceView<R, Channel1D, String> manager) 
    {
        super(manager, new Channel1DMinimumSizeFilter(3), false, false);

        MultiMap<String, Channel1D> channelsToProcessMap = getSelectedResourceChannelsToProcess();
        Collection<Channel1D> channelsToProcess = channelsToProcessMap.allValues();

        this.columnCount = calculateInitialColumnCount(channelsToProcess);
        this.columnCounts = calculateItemCounts(channelsToProcess);
        Set<Integer> differentItemCounts = new HashSet<>(columnCounts.allValues());
        this.uniqueColumnCount = differentItemCounts.size() == 1 ? differentItemCounts.iterator().next() : -1;
    }

    private MultiMap<Object, Integer> calculateItemCounts(Collection<Channel1D> channels)
    {
        MultiMap<Object, Integer> gridSizes = new MultiMap<>();

        for(Channel1D channel : channels)
        {
            int itemCount = channel.getItemCount();

            gridSizes.put(channel.getIdentifier(), Integer.valueOf(itemCount));
        }

        return gridSizes;
    }

    private Integer calculateInitialColumnCount(Collection<Channel1D> channels)
    {
        int greatestItemCount = 0;

        for(Channel1D channel : channels)
        {
            greatestItemCount = Math.max(greatestItemCount, channel.getItemCount());
        }

        return 2*greatestItemCount;
    }

    public Integer getUniqueColumnCount()
    {
        return uniqueColumnCount;
    }

    @Override
    protected void handleIdentifierSelectionChange(String identifier, boolean selectedOld, boolean selectedNew)
    {
        super.handleIdentifierSelectionChange(identifier, selectedOld, selectedNew);

        MultiMap<String, Channel1D> channelsToProcessMap = getSelectedResourceChannelsToProcess();
        Collection<Channel1D> channelsToProcess = channelsToProcessMap.allValues();
        MultiMap<Object, Integer> itemCountsNew = calculateItemCounts(channelsToProcess);

        if(!Objects.equals(columnCounts, itemCountsNew))
        {
            this.columnCounts = itemCountsNew;

            Set<Integer> differentColumnCounts = new HashSet<>(columnCounts.allValues());
            Integer uniqueItemCountNew = differentColumnCounts.size() == 1 ? differentColumnCounts.iterator().next() : null;
            if(!Objects.equals(this.uniqueColumnCount, uniqueItemCountNew))
            {
                Integer uniqueItemCountOld = this.uniqueColumnCount;
                this.uniqueColumnCount = uniqueItemCountNew;

                firePropertyChange(UNIQUE_COLUMN_COUNT, uniqueItemCountOld, uniqueItemCountNew);
            }
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

    public InterpolationMethod1D getInterpolationMethod()
    {
        return interpolationMethod;
    }

    public void setInterpolationMethod(InterpolationMethod1D interpolationMethodNew)
    {
        if(!Objects.equals(this.interpolationMethod, interpolationMethodNew))
        {
            InterpolationMethod1D interpolationMethodOld = this.interpolationMethod;
            this.interpolationMethod = interpolationMethodNew;

            firePropertyChange(INTERPOLATION_METHOD, interpolationMethodOld, interpolationMethodNew);

            checkIfApplyEnabled();         
            updatePreview();
        }
    }

    protected boolean calculateIfApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && columnCount >= 0 && interpolationMethod != null;

        return applyEnabled;
    }

    @Override
    protected Channel1DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        Channel1DDataInROITransformation tr = new Gridding1DTransformation(columnCount, interpolationMethod);
        return tr;
    }
}