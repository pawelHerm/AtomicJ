package atomicJ.sources;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import atomicJ.data.Channel2D;
import atomicJ.data.QuantitativeSample;
import atomicJ.gui.rois.ROI;
import atomicJ.utilities.MetaMap;

public interface ChannelGroup<E, T>
{
    public List<Channel2D> getChannels();
    public Map<String, QuantitativeSample> getCoordinateSamples();
    public ROISamplesResult<E, T> getROISamples(Collection<? extends ROI> rois, boolean includeCoordinates);

    public static class ROISamplesResult<E, T>
    {
        private final MetaMap<E, T, QuantitativeSample> valueSamples;
        private final MetaMap<String, T, QuantitativeSample> coordinateSamples;

        public ROISamplesResult(MetaMap<E, T, QuantitativeSample> valueSamples, MetaMap<String, T, QuantitativeSample> quantitativeSamples)
        {
            this.valueSamples = valueSamples;
            this.coordinateSamples = quantitativeSamples;
        }

        public MetaMap<String, T, QuantitativeSample> getCalculatedCoordinateSamples()
        {
            return coordinateSamples;
        }

        public MetaMap<E, T, QuantitativeSample> getValueSmples()
        {
            return valueSamples;
        }
    }
}