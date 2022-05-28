package atomicJ.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.sources.Channel1DSource;

public interface Channel1DResource <E extends Channel1DSource<?>> extends ChannelResource<Channel1D, Channel1DData, String>
{
    public E getSource();

    public PrefixedUnit getSingleDataUnit(String type);
    public Map<String, Channel1D> getChannels(String type);
    public Map<String, Channel1D> transform(String type, Channel1DDataTransformation tr);
    public Map<String, Channel1D> transform(String type, Set<String> identifiers, Channel1DDataTransformation tr);
    public Map<String, Channel1D> transform(String type, Set<String> identifiers, Channel1DDataInROITransformation tr, ROI roi, ROIRelativePosition position);

    //DISTANCE MEASUREMENTS

    public int getMeasurementCount(String type);

    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements(String type);
    public void addOrReplaceDistanceMeasurement(String type, DistanceMeasurementDrawable measurement);
    public void removeDistanceMeasurement(String type, DistanceMeasurementDrawable measurement);

    public static <E extends Channel1DSource<?>> List<E> getSources(List<? extends Channel1DResource<E>> resources)
    {
        List<E> sources = new ArrayList<>();

        for(Channel1DResource<E> resource : resources)
        {
            sources.add(resource.getSource());
        }

        return sources;
    }
}
