package atomicJ.sources;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.data.Channel2D;
import atomicJ.data.Datasets;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.StandardSample;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.ROI;
import atomicJ.utilities.MetaMap;

public class EqualScatteredChannelGroup<E, T> implements ChannelGroup<E, T>
{
    private final List<Channel2D> channels;

    private final int groupIndex;
    private final boolean multipleGroups;
    private final ChannelTagger<E> channelTagger;
    private final ROITagger<T> roiTagger;

    public EqualScatteredChannelGroup(ROITagger<T> roiTagger, List<Channel2D> channels, int groupIndex, boolean multipleGroups, ChannelTagger<E> channelTagger)
    {
        this.roiTagger = roiTagger;
        this.channels = new ArrayList<>(channels);
        this.groupIndex = groupIndex;
        this.multipleGroups = multipleGroups;
        this.channelTagger = channelTagger;
    }

    @Override
    public List<Channel2D> getChannels()
    {
        return Collections.unmodifiableList(channels);
    }

    @Override
    public Map<String, QuantitativeSample> getCoordinateSamples()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        if(channels.isEmpty())
        {
            return samples;
        }

        Channel2D channel = channels.get(0);
        double[] xCoords = channel.getXCoordinatesCopy();
        double[] yCoords = channel.getYCoordinatesCopy();

        Quantity xQuantity = channel.getXQuantity();
        Quantity yQuantity = channel.getYQuantity();

        String xSampleName = multipleGroups ? "X (" + groupIndex + " )" : "X";
        String ySampleName = multipleGroups ? "Y (" + groupIndex + " )" : "Y";

        QuantitativeSample xCoordSample = new StandardSample(xCoords, roiTagger.getCoordinateSampleTag(), xQuantity.changeName(xSampleName));
        QuantitativeSample yCoordSample = new StandardSample(yCoords, roiTagger.getCoordinateSampleTag(), yQuantity.changeName(ySampleName));

        samples.put(Datasets.X_COORDINATE, xCoordSample);
        samples.put(Datasets.Y_COORDINATE, yCoordSample);

        return samples;
    }

    @Override
    public ROISamplesResult<E, T> getROISamples(Collection<? extends ROI> rois, boolean includeCoordinates)
    {                   
        if(channels.isEmpty())
        {
            return new ROISamplesResult<>(new MetaMap<E, T, QuantitativeSample>(), new MetaMap<String, T, QuantitativeSample>());
        }

        MetaMap<E, T, QuantitativeSample> valueSamples = new MetaMap<>();
        MetaMap<String, T, QuantitativeSample> coordinateSamples = new MetaMap<>();

        //these maps will contain a list for coordinates of points in each of the ROIs
        Map<IdentityTag, TDoubleList> allXCoords = new LinkedHashMap<>();
        Map<IdentityTag, TDoubleList> allYCoords = new LinkedHashMap<>();


        final MetaMap<E, IdentityTag, TDoubleList> sampleData = buildSampleDataMap(rois);

        Channel2D guidingChannel = channels.get(0);

        int count = guidingChannel.getItemCount();
        int channelCount = channels.size();

        for(ROI roi: rois)
        {   
            IdentityTag roiIDTag = roi.getIdentityTag();

            TDoubleList currentXCoords = new TDoubleArrayList();
            TDoubleList currentYCoords = new TDoubleArrayList();

            allXCoords.put(roiIDTag, currentXCoords);
            allYCoords.put(roiIDTag, currentYCoords);

            Shape roiShape = roi.getROIShape();

            for(int i = 0; i<count; i++)
            {
                double x = guidingChannel.getX(i);
                double y = guidingChannel.getY(i);

                if(roiShape.contains(x, y))
                {
                    currentXCoords.add(x);
                    currentYCoords.add(y);

                    for(int channelIndex = 0; channelIndex<channelCount;channelIndex++)
                    {
                        Channel2D channel = channels.get(channelIndex);
                        E identifier = channelTagger.getTag(channel, channelIndex);
                        double z = channel.getZ(i);
                        sampleData.get(identifier, roiIDTag).add(z);
                    }
                }
            }          
        }   

        if(includeCoordinates)
        {
            String xSampleName = multipleGroups ? "X (" + groupIndex + " )" : "X";
            String ySampleName = multipleGroups ? "Y (" + groupIndex + " )" : "Y";

            Quantity xQuantity = guidingChannel.getXQuantity().changeName(xSampleName);
            Quantity yQuantity = guidingChannel.getYQuantity().changeName(ySampleName);

            coordinateSamples.putAll(buildCoordinateSamples(rois, allXCoords, allYCoords, xQuantity, yQuantity));
        }


        for(int i = 0; i<channelCount;i++)
        {
            Channel2D channel = channels.get(i);
            E channelIdentifier = channelTagger.getTag(channel, i);
            Quantity quantity = channel.getZQuantity();

            Map<IdentityTag, TDoubleList> dataMap = sampleData.get(channelIdentifier);

            for(Entry<IdentityTag, TDoubleList> entry: dataMap.entrySet())
            {
                IdentityTag idTag = entry.getKey();

                TDoubleList rawData = entry.getValue();
                double[] dataArray = rawData.toArray();                  

                QuantitativeSample sample = new StandardSample(dataArray, roiTagger.getQuantitativeSampleTag(idTag), quantity, idTag.getLabel(), getSampleTag());                                
                valueSamples.put(channelIdentifier, roiTagger.getTag(idTag), sample);
            }           
        }
        ROISamplesResult<E, T> results = new ROISamplesResult<>(valueSamples, coordinateSamples);

        return results;
    }

    private MetaMap<E, IdentityTag, TDoubleList> buildSampleDataMap(Collection<? extends ROI> rois)
    {
        final MetaMap<E, IdentityTag,TDoubleList> sampleData = new MetaMap<>();

        //ensures that sampleData contains for each type (i.e. channel) an inner map, which, in turn, contains an empty
        //list for values for each roi key

        int channelCount = channels.size();
        for(int i = 0; i<channelCount;i++)
        {
            Channel2D channel = channels.get(i);
            E channelIdentifier = channelTagger.getTag(channel, i);

            for(ROI roi: rois)
            {
                IdentityTag idManager = roi.getIdentityTag();
                sampleData.put(channelIdentifier, idManager, new TDoubleArrayList());
            }
        }

        return sampleData;
    }

    private MetaMap<String, T, QuantitativeSample> buildCoordinateSamples(Collection<? extends ROI> rois, Map<IdentityTag, TDoubleList> allXCoords, Map<IdentityTag, TDoubleList> allYCoords, Quantity xQuantity, Quantity yQuantity)
    {
        MetaMap<String, T, QuantitativeSample> samples = new MetaMap<>();

        for(ROI roi: rois)
        {
            IdentityTag idTag = roi.getIdentityTag();

            TDoubleList xCoords = allXCoords.get(idTag);
            TDoubleList yCoords = allYCoords.get(idTag);

            double[] xCoordsArray = xCoords.toArray();                  
            double[] yCoordsArray = yCoords.toArray();

            QuantitativeSample xCoordSample = new StandardSample(xCoordsArray, roiTagger.getQuantitativeSampleTag(idTag), xQuantity, idTag.getLabel(), getSampleTag());
            QuantitativeSample yCoordSample = new StandardSample(yCoordsArray, roiTagger.getQuantitativeSampleTag(idTag), yQuantity, idTag.getLabel(), getSampleTag());

            samples.put(Datasets.X_COORDINATE, roiTagger.getTag(idTag), xCoordSample);
            samples.put(Datasets.Y_COORDINATE, roiTagger.getTag(idTag), yCoordSample);

        }

        return samples;
    }

    private String getSampleTag()
    {
        String sampleTag = " (" + roiTagger.getCoordinateSampleTag() + ")";

        return sampleTag;
    }
}