package atomicJ.sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.data.ArrayChannel2DData;
import atomicJ.data.ArraySupport2D;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Datasets;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.StandardSample;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;

public class EqualGridChannelGroup<E, T> implements ChannelGroup<E, T>
{
    private final ArraySupport2D grid;
    private final List<Channel2D> channels;
    private final int groupIndex;
    private final boolean multipleGroups;
    private final ChannelTagger<E> tagger;
    private final ROITagger<T> roiTagger;

    public EqualGridChannelGroup(ROITagger<T> roiTagger, ArraySupport2D grid, List<Channel2D> channels, int groupIndex, boolean multipleGroups, ChannelTagger<E> tagger)
    {
        this.roiTagger = roiTagger;
        this.grid = grid;
        this.channels = new ArrayList<>(channels);
        this.groupIndex = groupIndex;
        this.multipleGroups = multipleGroups;
        this.tagger = tagger;
    }

    @Override
    public List<Channel2D> getChannels()
    {
        return channels;
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

        String xSampleName = multipleGroups ? "X (" + groupIndex + " )" : "X";
        String ySampleName = multipleGroups ? "Y (" + groupIndex + " )" : "Y";

        QuantitativeSample xCoordSample = new StandardSample(xCoords, roiTagger.getCoordinateSampleTag(), grid.getXQuantity().changeName(xSampleName));
        QuantitativeSample yCoordSample = new StandardSample(yCoords, roiTagger.getCoordinateSampleTag(), grid.getYQuantity().changeName(ySampleName));

        samples.put(Datasets.X_COORDINATE, xCoordSample);
        samples.put(Datasets.Y_COORDINATE, yCoordSample);

        return samples;
    }

    @Override
    public ROISamplesResult<E, T> getROISamples(Collection<? extends ROI> rois, boolean includeCoordinates)
    {
        MetaMap<E, T, QuantitativeSample> valueSamples = new MetaMap<>();

        //these maps will contain a list for coordinates of points in each of the rois
        Map<IdentityTag, double[]> allXCoords = new LinkedHashMap<>();
        Map<IdentityTag, double[]> allYCoords = new LinkedHashMap<>();

        final MetaMap<E, IdentityTag, double[]> sampleData = buildSampleDataMap(rois);

        final int channelCount = channels.size();

        Map<IdentityTag, Integer> trueCounts = new LinkedHashMap<>();

        for(ROI roi: rois)
        {                   
            final IdentityTag idTag = roi.getIdentityTag();
            int sizeBound = roi.getPointsInsideCountUpperBound(grid);

            final double[] xCoords = new double[sizeBound];
            final double[] yCoords = new double[sizeBound];

            allXCoords.put(idTag, xCoords);
            allYCoords.put(idTag, yCoords);

            class GridPointRecepientCustom implements GridPointRecepient
            {                         
                int count = 0;

                @Override
                public void addPoint(int row, int column)
                {
                    xCoords[count] = grid.getX(column);
                    yCoords[count] = grid.getY(row);

                    for(int i = 0; i<channelCount; i++)
                    {
                        Channel2D channel = channels.get(i);
                        E channelIdentifier = tagger.getTag(channel, i);

                        double[][] channelData = channel.getChannelData().getDefaultGridding().getData();
                        double[] valuesInside = sampleData.get(channelIdentifier, idTag);
                        valuesInside[count] = channelData[row][column];
                    }     

                    count++;
                }

                @Override
                public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo)
                {           
                    for(int channelIndex = 0; channelIndex<channelCount; channelIndex++)
                    {
                        Channel2D channel = channels.get(channelIndex);
                        E channelIdentifier = tagger.getTag(channel, channelIndex);
                        int countInner = count;
                        double[][] channelData = channel.getChannelData().getDefaultGridding().getData();
                        double[] valuesInside = sampleData.get(channelIdentifier, idTag);

                        for(int i = rowFrom; i<rowTo; i++)
                        {
                            double[] channelRow = channelData[i];

                            for(int j = columnFrom; j<columnTo; j++)
                            {                                
                                valuesInside[countInner++] = channelRow[j];
                            }
                        }                        
                    }  

                    for(int i = rowFrom; i<rowTo; i++)
                    {
                        double y = grid.getY(i);

                        for(int j = columnFrom; j<columnTo; j++)
                        {
                            xCoords[count] = grid.getX(j);
                            yCoords[count++] = y;
                        }
                    }
                }
            };

            GridPointRecepientCustom receipient = new GridPointRecepientCustom();
            roi.addPointsInside(grid, receipient);

            trueCounts.put(idTag, receipient.count);         
        }   

        MetaMap<String, T, QuantitativeSample> coordinateSamples = new MetaMap<>();

        if(includeCoordinates)
        {
            String xSampleName = multipleGroups ? "X (" + groupIndex + " )" : "X";
            String ySampleName = multipleGroups ? "Y (" + groupIndex + " )" : "Y";

            coordinateSamples.putAll(buildCoordinateSamples(grid, rois, allXCoords, allYCoords, trueCounts, xSampleName, ySampleName));
        }

        for(int channelIndex= 0; channelIndex < channelCount; channelIndex++)
        {
            Channel2D channel = channels.get(channelIndex);
            E channelIdentifier = tagger.getTag(channel, channelIndex);
            Quantity quantity = channel.getZQuantity();

            Map<IdentityTag, double[]> dataMap = sampleData.get(channelIdentifier);

            for(Entry<IdentityTag, double[]> entry2: dataMap.entrySet())
            {
                IdentityTag idTag = entry2.getKey();

                double[] rawData = entry2.getValue();

                int trueCount = trueCounts.get(idTag);
                boolean filled = (rawData.length == trueCount);

                double[] dataArray = filled ? rawData : Arrays.copyOf(rawData, trueCount);                  

                //densitySource.getUniversalROIKey(idTag.getKey())
                QuantitativeSample sample = new StandardSample(dataArray, roiTagger.getQuantitativeSampleTag(idTag), quantity, idTag.getLabel(), getSampleTag());                                
                valueSamples.put(channelIdentifier, roiTagger.getTag(idTag), sample);
            }           
        }

        ROISamplesResult<E, T> results = new ROISamplesResult<>(valueSamples, coordinateSamples);

        return results;
    }

    private MetaMap<E, IdentityTag, double[]> buildSampleDataMap(Collection<? extends ROI> rois)
    {
        final MetaMap<E, IdentityTag, double[]> sampleData = new MetaMap<>();

        //ensures that sampleData contains for each type (i.e. channel) an inner map, which, in turn, contains an empty
        //list for values for each roi key

        int channelCount = channels.size();
        for(int i = 0; i<channelCount; i++)
        {
            Channel2D channel = channels.get(i);
            E channelIdentifier = tagger.getTag(channel, i);

            for(ROI roi: rois)
            {
                IdentityTag idTag = roi.getIdentityTag();
                int sizeBound = roi.getPointsInsideCountUpperBound(grid);
                double[] valuesInside = new double[sizeBound];
                sampleData.put(channelIdentifier, idTag, valuesInside);
            }
        }

        return sampleData;
    }

    private MetaMap<String, T, QuantitativeSample> buildCoordinateSamples(ArraySupport2D grid, Collection<? extends ROI> rois, Map<IdentityTag, double[]> allXCoords, Map<IdentityTag, double[]> allYCoords, Map<IdentityTag, Integer> trueCounts, String xSampleName, String ySampleName)
    {
        MetaMap<String, T, QuantitativeSample> samples = new MetaMap<>();

        for(ROI roi: rois)
        {
            IdentityTag idTag = roi.getIdentityTag();

            double[] xCoords = allXCoords.get(idTag);
            double[] yCoords = allYCoords.get(idTag);

            int trueCount = trueCounts.get(idTag);
            boolean filled = (xCoords.length == trueCount);

            double[] xCoordsArray = filled ? xCoords : Arrays.copyOf(xCoords, trueCount);                  
            double[] yCoordsArray = filled ? yCoords : Arrays.copyOf(yCoords, trueCount);

            QuantitativeSample xCoordSample = new StandardSample(xCoordsArray, roiTagger.getQuantitativeSampleTag(idTag), grid.getXQuantity().changeName(xSampleName), idTag.getLabel(), getSampleTag());
            QuantitativeSample yCoordSample = new StandardSample(yCoordsArray, roiTagger.getQuantitativeSampleTag(idTag), grid.getYQuantity().changeName(ySampleName), idTag.getLabel(), getSampleTag());

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

    public static  <E, T> List<ChannelGroup<E, T>> getEqualDomainChannelGroups(ROITagger<T> roiTagger, List<? extends Channel2D> channelDataList, ChannelTagger<E> tagger)
    {
        MultiMap<ArraySupport2D, Channel2D> mapGrids = new MultiMap<>();
        MultiMap<ChannelDomainIdentifier, Channel2D> mapFlexible = new MultiMap<>();

        for(Channel2D channel : channelDataList)
        {
            Channel2DData channelData = channel.getChannelData();
            if(channelData instanceof ArrayChannel2DData)
            {
                ArrayChannel2DData gridChannelData = (ArrayChannel2DData)channelData;
                mapGrids.put(gridChannelData.getGrid(), channel);
            }
            else
            {
                mapFlexible.put(channel.getDomainIdentifier(), channel);
            }
        }

        List<ChannelGroup<E, T>> channelGroups = new ArrayList<>();
        boolean multipleGroups = (mapGrids.size() + mapFlexible.size()) > 1;

        int groupIndex = 0;
        for(Entry<ArraySupport2D, List<Channel2D>> entry : mapGrids.entrySet())
        {
            channelGroups.add(new EqualGridChannelGroup<>(roiTagger, entry.getKey(), entry.getValue(), groupIndex, multipleGroups, tagger));
            groupIndex++;
        }

        for(Entry<ChannelDomainIdentifier, List<Channel2D>> entry : mapFlexible.entrySet())
        {
            channelGroups.add(new EqualScatteredChannelGroup<>(roiTagger, entry.getValue(), groupIndex, multipleGroups, tagger));
            groupIndex++;
        }

        return channelGroups;
    }
}