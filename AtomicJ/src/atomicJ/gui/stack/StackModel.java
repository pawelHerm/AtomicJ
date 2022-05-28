
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui.stack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.Timer;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.ArrayChannel2DUtilities;
import atomicJ.data.ArraySupport2D;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.Channel2DStandard;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.DataAxis1D;
import atomicJ.data.Datasets;
import atomicJ.data.Grid1D;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitArray1DExpression;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.ConcurrentChannelTransformationTask;
import atomicJ.gui.Channel2DReceiver;
import atomicJ.gui.Gridding2DSettings;
import atomicJ.gui.ChannelResourceDialogModel;
import atomicJ.gui.ResourceGroupListener;
import atomicJ.gui.ResourceTypeListener;
import atomicJ.gui.ResourceViewModel;
import atomicJ.gui.SelectionListener;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.rois.ROI;
import atomicJ.imageProcessing.Gridding2DTransformation;
import atomicJ.resources.CrossSectionSettings;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;
import atomicJ.sources.ChannelGroup;
import atomicJ.sources.EqualGridChannelGroup;
import atomicJ.sources.IdentityTag;
import atomicJ.sources.IdentityTagger;
import atomicJ.sources.ROITagger;
import atomicJ.sources.ChannelGroup.ROISamplesResult;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.SampleStatistics;
import atomicJ.utilities.MetaMap;

public class StackModel <R extends Channel2DResource>
{
    private static final NumberFormat END_LABEL_FORMAT = NumberFormat.getInstance(Locale.US);
    private static final NumberFormat CURRENT_LABEL_FORMAT = NumberFormat.getInstance(Locale.US);

    static 
    {
        CURRENT_LABEL_FORMAT.setMinimumFractionDigits(CURRENT_LABEL_FORMAT.getMaximumFractionDigits());
    }

    public static final String FRAME_INDEX = "FRAME_INDEX";
    public static final String FRAME_FIRST = "FRAME_FIRST";
    public static final String FRAME_LAST = "FRAME_LAST";
    public static final String DISPLAY_RUNNING = "DISPLAY_RUNNING";
    public static final String FRAME_RATE = "FRAME_RATE";
    public static final String MOVIE_LENGTH = "MOVIE_LENGTH";
    public static final String PLAYED_FORWARD = "PLAYED_FORWARD";
    public static final String STACK_EMPTY = "STACK_EMPTY";

    private final Quantity depthQuantity;
    private final DataAxis1D stackAxis;
    private final String stackType;

    private final int frameCount;

    private int currentFrame;
    private boolean currentFrameLast;
    private boolean currentFrameFirst;
    private boolean isRunning;

    private final String sourceShortName;
    private final File defaultOutputDirectory;

    private List<Channel2D> channels;
    private final ChannelResourceDialogModel<Channel2D, Channel2DData, String, StackQuasiResource> resourceModel;

    private boolean playForward;
    private double frameRate; //frames per second
    private double movieLength; //movie length in seconds
    private Timer timer;

    private final R parentResource;

    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    public StackModel(DataAxis1D stackAxis, Quantity depthQuantity, String stackType, File defaultOutputDirectory, String sourceShortName, List<Channel2D> channels,  R parentResource)
    {
        this.stackAxis = stackAxis;
        this.depthQuantity = depthQuantity;
        this.stackType = stackType;
        this.frameCount = stackAxis.getIndexCount();
        this.parentResource = parentResource;
        this.sourceShortName = sourceShortName;
        this.defaultOutputDirectory = defaultOutputDirectory;

        this.channels = new ArrayList<>(channels);

        List<StackQuasiResource> quasiResources = new ArrayList<>();
        for(Channel2D channel : channels)
        {
            quasiResources.add(new StackQuasiResource(channel, stackType));
        }

        this.resourceModel = new ChannelResourceDialogModel<>(quasiResources, stackType);
        if(!quasiResources.isEmpty())
        {
            this.resourceModel.setSelectedResource(currentFrame);
        }

        initModel();
        initPropertyChangeListener();
    }


    private void initPropertyChangeListener()
    {
        resourceModel.addPropertyChangeListener(ResourceViewModel.ALL_RESOURCES_EMPTY, 
                new PropertyChangeListener() 
        {                   
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                propertySupport.firePropertyChange(STACK_EMPTY, evt.getOldValue(), evt.getNewValue());
            }
        });
    }

    public Channel2D getSection(Profile profile, CrossSectionSettings sectionSettings)
    {
        int pointCount = sectionSettings.getPointCount();
        Channel2D guidingChannel = getGuidingChannel();

        Quantity xQuantity = guidingChannel.getXQuantity();
        int rowCount = frameCount;
        int columnCount = pointCount;

        double xIncrement = (profile.getLength()/(pointCount - 1.));

        Grid1D xAxis = new Grid1D(xIncrement, 0, columnCount, xQuantity);

        double[][] gridData = new double[rowCount][columnCount];

        for(int j = 0; j<frameCount; j++)
        {
            double[] row = channels.get(j).getProfileValues(profile.getDistanceShape(), sectionSettings);
            gridData[j] = row;
        }        

        Channel2DData sectionData = ArrayChannel2DUtilities.buildChannel(xAxis, stackAxis, gridData, depthQuantity);
        Channel2D section = new Channel2DStandard(sectionData, depthQuantity.getName()); 

        return section;
    }

    public void getSectionParallelized(Profile profile, CrossSectionSettings sectionSettings, Channel2DReceiver receiver)
    {
        int pointCount = sectionSettings.getPointCount();
        Channel2D guidingChannel = getGuidingChannel();

        Quantity xQuantity = guidingChannel.getXQuantity();
        int columnCount = pointCount;

        double xIncrement = (profile.getLength()/(pointCount - 1.));
        Grid1D xAxis = new Grid1D(xIncrement, 0, columnCount, xQuantity);

        ArraySupport2D gridNew = ArrayChannel2DUtilities.getGrid(xAxis, stackAxis);

        Concurrent2DSlicingTask task = new Concurrent2DSlicingTask(channels, profile.getDistanceShape(), sectionSettings, gridNew, depthQuantity, receiver);      
        task.execute();
    }

    public StackModel<R> getHorizontalSections()
    {        
        Channel2D guidingChannel = getGuidingChannel();
        Grid2D guidingGrid = guidingChannel.getDefaultGriddingGrid();

        List<GridChannel2DData> griddedChannelData = getGriddedChannelData(guidingGrid);

        int rowCount = frameCount;
        int columnCount = guidingGrid.getColumnCount();

        Grid1D xAxis = guidingGrid.getXAxis();

        List<Channel2D> sectionChannels = new ArrayList<>();

        int sectionCount = guidingGrid.getRowCount();
        for(int i = 0; i<sectionCount; i++)
        {
            double[][] gridData = new double[rowCount][columnCount];

            for(int j = 0; j<frameCount; j++)
            {
                double[] row = griddedChannelData.get(j).getRow(i);
                gridData[j] = row;
            }

            Channel2DData channelData = ArrayChannel2DUtilities.buildChannel(xAxis, stackAxis, gridData, depthQuantity);
            Channel2D section = new Channel2DStandard(channelData, depthQuantity.getName());

            sectionChannels.add(section);        
        }

        StackModel<R> stackModel = new StackModel<>(guidingGrid.getYAxis(), depthQuantity, "Horizontal sections", defaultOutputDirectory, sourceShortName, sectionChannels, null);

        return stackModel;
    }

    public StackModel<R> getHorizontalResizedSection(InterpolationMethod2D interpolationMethod, int xResizedCount)
    {          
        Channel2D guidingChannel = getGuidingChannel();
        Grid2D guidingGrid = guidingChannel.getDefaultGriddingGrid();

        List<Channel2DData> resizedChannels = getResizedChannelsParallelized(new Gridding2DSettings(interpolationMethod, guidingGrid.getRowCount(), xResizedCount));

        Channel2DData resizedGuidedChannel = resizedChannels.get(0);
        Grid2D guidingResizedGrid = resizedGuidedChannel.getDefaultGriddingGrid();

        List<GridChannel2DData> griddedChannelData = GridChannel2DData.getGriddedChannels(resizedChannels, guidingResizedGrid);

        int rowCount = frameCount;
        int columnCount = guidingResizedGrid.getColumnCount();

        Grid1D xAxis = guidingResizedGrid.getXAxis();

        List<Channel2D> sections = new ArrayList<>();

        int sectionCount = guidingResizedGrid.getRowCount();
        for(int i = 0; i<sectionCount; i++)
        {
            double[][] gridData = new double[rowCount][columnCount];

            for(int j = 0; j<frameCount; j++)
            {
                double[] row = griddedChannelData.get(j).getRow(i);
                gridData[j] = row;
            }

            Channel2DData channelData = ArrayChannel2DUtilities.buildChannel(xAxis, stackAxis, gridData, depthQuantity);
            Channel2D section = new Channel2DStandard(channelData, depthQuantity.getName());

            sections.add(section);        
        }        

        StackModel<R> stackModel = new StackModel<>(guidingResizedGrid.getYAxis(),
                depthQuantity, "Horizontal sections", defaultOutputDirectory, sourceShortName, sections, null);

        return stackModel;
    }

    //xResizeCount are specified in the new stack's coordinate system;
    public StackModel<R> getHorizontalResizedSection(InterpolationMethod2D interpolationMethod, int xResizeCount, int ySmoothCount, int depthSmoothCount)
    {	    	        
        Gridding2DSettings resizeSettings = new Gridding2DSettings(interpolationMethod, depthSmoothCount, xResizeCount);
        List<Channel2DData> resizedChannels = getResizedChannelsParallelized(resizeSettings);

        Channel2DData guidingResizedChannel = resizedChannels.get(0);
        Grid2D guidingResizedGrid = guidingResizedChannel.getDefaultGriddingGrid();

        List<GridChannel2DData> griddedChannelData = GridChannel2DData.getGriddedChannels(resizedChannels, guidingResizedGrid);

        int rowCount = frameCount;
        int columnCount = guidingResizedGrid.getColumnCount();

        Grid1D xAxis = guidingResizedGrid.getXAxis();

        List<Channel2DData> sections = new ArrayList<>();

        int sectionCount = guidingResizedGrid.getRowCount();
        for(int i = 0; i<sectionCount; i++)
        {
            double[][] gridData = new double[rowCount][columnCount];

            for(int j = 0; j<frameCount; j++)
            {               
                double[] row = griddedChannelData.get(j).getRow(i);
                gridData[j] = row;
            }

            Channel2DData channelData = ArrayChannel2DUtilities.buildChannel(xAxis, stackAxis, gridData, depthQuantity);

            sections.add(channelData);        
        }        


        List<Channel2DData> sectionResized = getResizedChannelsParallelized(sections, new Gridding2DSettings(interpolationMethod, ySmoothCount, columnCount));

        List<Channel2D> sectionChannels = new ArrayList<>();
        for(Channel2DData sectionData : sectionResized)
        {
            sectionChannels.add(new Channel2DStandard(sectionData, depthQuantity.getName()));
        }

        StackModel<R> stackModel = new StackModel<>(guidingResizedGrid.getYAxis(),
                depthQuantity, "Horizontal sections", defaultOutputDirectory, sourceShortName, sectionChannels, null);

        return stackModel;
    }

    public StackModel<R> getVerticalSections()
    {        
        Channel2D guidingChannel = getGuidingChannel();
        Grid2D guidingGrid = guidingChannel.getDefaultGriddingGrid();

        List<GridChannel2DData> griddedChannelData = getGriddedChannelData(guidingGrid);

        int rowCount = frameCount;//
        int columnCount = guidingGrid.getRowCount();//

        Grid1D xAxis = guidingGrid.getYAxis();

        List<Channel2D> sectionChannels = new ArrayList<>();

        int sectionCount = guidingGrid.getColumnCount();
        for(int i = 0; i<sectionCount; i++)
        {
            double[][] gridData = new double[rowCount][columnCount];

            for(int j = 0; j<frameCount; j++)
            {
                gridData[j] = griddedChannelData.get(j).getColumn(i);
            }

            Channel2DData channelData = ArrayChannel2DUtilities.buildChannel(xAxis, stackAxis, gridData, depthQuantity);
            Channel2D section = new Channel2DStandard(channelData, depthQuantity.getName());

            sectionChannels.add(section);        
        }


        StackModel<R> stackModel = new StackModel<>(guidingGrid.getXAxis(), 
                depthQuantity, "Vertical sections", defaultOutputDirectory, sourceShortName, sectionChannels, null);

        return stackModel;    
    }

    public StackModel<R> getVerticalSections(InterpolationMethod2D interpolationMethod, int xCount)
    {  
        Channel2D guidingChannel = getGuidingChannel();
        Grid2D guidingGrid = guidingChannel.getDefaultGriddingGrid();

        Gridding2DSettings resizeSettings = new Gridding2DSettings(interpolationMethod, xCount, guidingGrid.getColumnCount());
        List<Channel2DData> resizedChannels = getResizedChannelsParallelized(resizeSettings);

        Channel2DData guidingResizedChannel = resizedChannels.get(0);
        Grid2D guidingResizedGrid = guidingResizedChannel.getDefaultGriddingGrid();

        List<GridChannel2DData> griddedChannelData = GridChannel2DData.getGriddedChannels(resizedChannels, guidingResizedGrid);

        int rowCount = frameCount;//
        int columnCount = guidingResizedGrid.getRowCount();//

        Grid1D xAxis = guidingResizedGrid.getYAxis();

        List<Channel2D> sections = new ArrayList<>();

        int sectionCount = guidingResizedGrid.getColumnCount();
        for(int i = 0; i<sectionCount; i++)
        {
            double[][] gridData = new double[rowCount][columnCount];

            for(int j = 0; j<frameCount; j++)
            {
                double[] row = griddedChannelData.get(j).getColumn(i);
                gridData[j] = row;
            }

            Channel2DData channelData = ArrayChannel2DUtilities.buildChannel(xAxis, stackAxis, gridData, depthQuantity);
            Channel2D section = new Channel2DStandard(channelData, depthQuantity.getName());

            sections.add(section);        
        }	        

        StackModel<R> stackModel = new StackModel<>(guidingResizedGrid.getXAxis(), 
                depthQuantity, "Vertical sections", defaultOutputDirectory, sourceShortName, sections, null);

        return stackModel;
    }

    //counts are specified in the new stack's coordinate system;

    public StackModel<R> getVerticalSections(InterpolationMethod2D interpolationMethod, int xCount, int yCount, int depthCount)
    {
        Gridding2DSettings resizeSettings = new Gridding2DSettings(interpolationMethod, xCount, depthCount);
        List<Channel2DData> resizedChannels = getResizedChannelsParallelized(resizeSettings);

        Channel2DData guidingResizedChannel = resizedChannels.get(0);
        Grid2D guidingResizedGrid = guidingResizedChannel.getDefaultGriddingGrid();

        List<GridChannel2DData> griddedChannelData = GridChannel2DData.getGriddedChannels(resizedChannels, guidingResizedGrid);

        int rowCount = frameCount;//
        int columnCount = guidingResizedGrid.getRowCount();//

        Grid1D xAxis = guidingResizedGrid.getYAxis();

        List<Channel2DData> sections = new ArrayList<>();

        int sectionCount = guidingResizedGrid.getColumnCount();
        for(int i = 0; i<sectionCount; i++)
        {
            double[][] gridData = new double[rowCount][columnCount];

            for(int j = 0; j<frameCount; j++)
            {
                gridData[j] = griddedChannelData.get(j).getColumn(i);
            }

            Channel2DData channelData = ArrayChannel2DUtilities.buildChannel(xAxis, stackAxis, gridData, depthQuantity);
            sections.add(channelData);        
        }    

        List<Channel2DData> sectionResized = getResizedChannelsParallelized(sections, new Gridding2DSettings(interpolationMethod, yCount, columnCount));

        List<Channel2D> sectionChannels = new ArrayList<>();
        for(Channel2DData sectionData : sectionResized)
        {
            sectionChannels.add(new Channel2DStandard(sectionData, depthQuantity.getName()));
        }

        StackModel<R> stackModel = new StackModel<>(guidingResizedGrid.getXAxis(), 
                depthQuantity, "Vertical sections", defaultOutputDirectory, sourceShortName, sectionChannels, null);

        return stackModel;    
    }

    private List<Channel2DData> getResizedChannelsParallelized(Gridding2DSettings resizeSettings)
    {
        return getResizedChannelsParallelized(getChannelData(), resizeSettings);      
    }

    private List<Channel2DData> getResizedChannelsParallelized(List<Channel2DData> channels, Gridding2DSettings sizeSettings)
    {  
        Gridding2DTransformation tr = new Gridding2DTransformation(sizeSettings);
        ConcurrentChannelTransformationTask task = new ConcurrentChannelTransformationTask(channels, tr, AtomicJ.getApplicationFrame());
        task.execute();

        List<Channel2DData> resizedChannels = new ArrayList<>();
        try {
            task.get();
            resizedChannels = task.getChannels();
        } catch (InterruptedException | ExecutionException e) 
        {
            e.printStackTrace();
        }

        return resizedChannels;
    }

    private List<Channel2DData> getChannelData()
    {
        List<Channel2DData> channelData = new ArrayList<>();

        for(Channel2D channel : channels)
        {
            channelData.add(channel.getChannelData());
        }

        return channelData;
    }

    private List<GridChannel2DData> getGriddedChannelData(Grid2D guidingGrid)
    {
        List<GridChannel2DData> channelData = new ArrayList<>();

        for(Channel2D channel : channels)
        {
            channelData.add(channel.getChannelData().getGridding(guidingGrid));
        }

        return channelData;
    }

    public File getDefaultOutputDirectory()
    {
        return defaultOutputDirectory;
    }

    public R getCorrespondingResource()
    {
        return parentResource;
    }

    public QuantitativeSample getCurrentSample()
    {
        Channel2D currentChannel = channels.get(currentFrame);
        QuantitativeSample currentSample = currentChannel.getZSample();
        return currentSample;
    }

    public List<SampleCollection> getCurrentSampleCollections()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        Channel2D currentChannel = channels.get(currentFrame);

        QuantitativeSample currentSample = currentChannel.getZSample();

        String currentSampleKey = "Current frame";
        samples.put(currentSampleKey, currentSample);

        SampleCollection collection = new StandardSampleCollection(samples, sourceShortName, sourceShortName, defaultOutputDirectory);

        List<SampleCollection> collections = Collections.singletonList(collection);

        return collections;
    }

    public List<SampleCollection> getSampleCollections()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        Channel2D firstChannel = channels.get(0);
        Channel2D currentChannel = channels.get(currentFrame);
        Channel2D lastChannel = channels.get(channels.size() - 1);

        QuantitativeSample firstSample = firstChannel.getZSample();
        QuantitativeSample currentSample = currentChannel.getZSample();
        QuantitativeSample lastSample = lastChannel.getZSample();

        String firstSampleKey = "First frame " + firstChannel.getChannelInfo();		
        String currentSampleKey = "Current frame " + currentChannel.getChannelInfo();
        String lastSampleKey = "Last frame " + currentChannel.getChannelInfo();		

        samples.put(firstSampleKey, firstSample);
        samples.put(currentSampleKey, currentSample);
        samples.put(lastSampleKey, lastSample);


        SampleCollection collection = new StandardSampleCollection(samples, sourceShortName, sourceShortName, defaultOutputDirectory);

        List<SampleCollection> collections = Collections.singletonList(collection);

        return collections;
    }

    private static class CustomROITagger implements ROITagger<IdentityTag>
    {
        @Override
        public IdentityTag getTag(IdentityTag roiTag) {
            return roiTag;
        }

        @Override
        public String getCoordinateSampleTag() 
        {
            return "Stack";
        }

        @Override
        public String getQuantitativeSampleTag(IdentityTag roiTag)
        {
            return roiTag.getLabel();
        }      
    }

    public MetaMap<SampleStatistics, IdentityTag, UnitArray1DExpression> getROIStatistics(Collection<? extends ROI> rois, Set<SampleStatistics> sampleStatistics)
    {        
        MetaMap<Channel2D, IdentityTag, QuantitativeSample> samples = new MetaMap<>();

        List<ChannelGroup<Channel2D, IdentityTag>> channelGroups = EqualGridChannelGroup.getEqualDomainChannelGroups(new CustomROITagger(), channels, IdentityTagger.getInstance());
        int channelGroupCount = channelGroups.size();

        for(int i = 0; i < channelGroupCount;i++)
        {
            ChannelGroup<Channel2D, IdentityTag> channelGroup = channelGroups.get(i);
            ROISamplesResult<Channel2D, IdentityTag> roiSamples = channelGroup.getROISamples(rois, false);
            samples.putAll(roiSamples.getValueSmples());
        }

        MetaMap<IdentityTag, Channel2D, QuantitativeSample> samplesSwapped = samples.swapKeyOrder();
        MetaMap<SampleStatistics, IdentityTag /*column - i.e. ROI*/, UnitArray1DExpression> calculatedStatistics = new MetaMap<>();

        for(SampleStatistics st : sampleStatistics)
        {
            for(Entry<IdentityTag,Map<Channel2D, QuantitativeSample>> entry : samplesSwapped.entrySet())
            {
                IdentityTag roiTag = entry.getKey();
                Map<Channel2D, QuantitativeSample> samplesForROI = entry.getValue();
                double[] values = new double[frameCount];

                for(int i = 0; i<frameCount;i++)
                {
                    QuantitativeSample sample = samplesForROI.get(channels.get(i));
                    DescriptiveStatistics ds = sample.getDescriptiveStatistics();
                    values[i] = st.getValue(ds);
                }

                UnitArray1DExpression arrayExpression = new UnitArray1DExpression(values, st.getStatisticsUnit(depthQuantity.getUnit()));
                calculatedStatistics.put(st, roiTag, arrayExpression);
            }
        }

        return calculatedStatistics;
    }

    public void cleanup()
    {
        this.channels = null;
    }

    private void initModel()
    {
        this.currentFrame = 0;
        this.currentFrameFirst = true;
        this.currentFrameLast = (currentFrame == (frameCount - 1));
        this.isRunning = (timer != null) && (timer.isRunning());

        this.playForward = true;
        this.frameRate = frameCount/10.;

        this.movieLength = frameCount/frameRate;	
    }

    public boolean isPlayedForward()
    {
        return playForward;
    }

    public void setPlayedForward(boolean playForward)
    {
        boolean playForwardOld = this.playForward;
        this.playForward = playForward;

        propertySupport.firePropertyChange(PLAYED_FORWARD, playForwardOld, this.playForward);
    }

    public double getFrameRate()
    {
        return frameRate;
    }

    public void setFrameRate(double frameRateNew)
    {
        double frameRateOld = this.frameRate;
        this.frameRate = frameRateNew;

        double movieLengthOld = this.movieLength;
        this.movieLength = frameCount/frameRate;

        if(isRunning)
        {
            int delay = (int)(1000./frameRate);
            timer.setDelay(delay);
        }

        propertySupport.firePropertyChange(FRAME_RATE, frameRateOld, this.frameRate);
        propertySupport.firePropertyChange(MOVIE_LENGTH, movieLengthOld, this.movieLength);
    }

    public double getMovieLength()
    {
        return movieLength;
    }

    public void setMovieLength(double movieLengthNew)
    {
        double movieLengthOld = this.movieLength;
        this.movieLength = movieLengthNew;

        double frameRateOld = this.frameRate;
        this.frameRate = frameCount/movieLength;

        if(isRunning)
        {
            int delay = (int)(1000./frameRate);
            timer.setDelay(delay);
        }

        propertySupport.firePropertyChange(MOVIE_LENGTH, movieLengthOld, this.movieLength);
        propertySupport.firePropertyChange(FRAME_RATE, frameRateOld, this.frameRate);
    }

    public void run()
    {  
        int defaultDelay = (int)(1000./frameRate);
        run(defaultDelay);
    }

    public void run(int delay)
    {
        this.timer = new Timer(delay, new TimerListener());
        timer.start();

        boolean isRunningOld = this.isRunning;
        this.isRunning = timer.isRunning();
        propertySupport.firePropertyChange(DISPLAY_RUNNING, isRunningOld, this.isRunning);
    }

    private class TimerListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            if(playForward)
            {
                moveToNextFrame();          
            }
            else
            {
                moveToPreviousFrame();
            }
        }

    }

    public void stop()
    {
        if(timer != null)
        {
            timer.stop();
        }

        boolean isRunningOld = this.isRunning;
        this.isRunning = false;
        propertySupport.firePropertyChange(DISPLAY_RUNNING, isRunningOld, this.isRunning);
    }

    public boolean isTimmerRunning()
    {
        return isRunning;
    }

    public DataAxis1D getStackAxis()
    {
        return stackAxis;
    }

    public int getFrameCount()
    {
        return frameCount;
    }

    public double getStackMinimum()
    {
        return stackAxis.getMaximum();
    }

    public double getStackMaximum()
    {
        return stackAxis.getMinimum();
    }

    public String getStackLowerLabel()
    {
        String unit = stackAxis.getQuantity().getFullUnitName();
        String value = END_LABEL_FORMAT.format(stackAxis.getMinimum());

        String label = value + " " + unit;

        return label;
    }

    public String getStackUpperLabel()
    {
        String unit = stackAxis.getQuantity().getFullUnitName();
        String value = END_LABEL_FORMAT.format(stackAxis.getMaximum());

        String label = value + " " + unit;

        return label;
    }

    public String getStackLabel(int frameIndex)
    {
        String unit = stackAxis.getQuantity().getFullUnitName();
        double v = getStackingValue(frameIndex);
        String value = CURRENT_LABEL_FORMAT.format(v);

        String label = value + " " + unit;

        return label;
    }

    public String getStackCurrentLabel()
    {
        String unit = stackAxis.getQuantity().getFullUnitName();
        double v = getCurrentStackingValue();
        String value = CURRENT_LABEL_FORMAT.format(v);

        String label = value + " " + unit;

        return label;
    }

    public String getSourceName()
    {
        return sourceShortName;
    }

    private Channel2D getGuidingChannel()
    {
        return channels.get(0);
    }

    public double getSuggestedFirstMarkerPosition()
    {
        double position = getGuidingChannel().getXRange().getCentralValue();
        return position;
    }

    public Grid2D getDefaultGriddingGrid()
    {
        return getGuidingChannel().getDefaultGriddingGrid();
    }

    public Quantity getXQuantity()
    {
        return getGuidingChannel().getXQuantity();
    }

    public Quantity getYQuantity()
    {
        return getGuidingChannel().getYQuantity();
    }

    public Quantity getStackingQuantity()
    {
        return stackAxis.getQuantity();
    }

    public Quantity getDepthQuantity()
    {
        return depthQuantity;
    }

    public String getStackType()
    {
        return stackType;
    }

    public int getCurrentFrame()
    {
        return currentFrame;
    }

    public double getStackingValue(int frameIndex)
    {
        double value = stackAxis.getArgumentVal(frameIndex);
        return value;
    }

    public double getCurrentStackingValue()
    {
        return getStackingValue(currentFrame);
    }

    public boolean isCurrentFrameFirst()
    {
        return currentFrameFirst;
    }

    public boolean isCurrentFrameLast()
    {
        return currentFrameLast;
    }

    public void moveToPreviousFrame()
    {
        int decremented = (currentFrame - 1);
        int index = decremented < 0 ? frameCount + decremented : decremented;

        setCurrentFrame(index);
    }

    public void moveToFirstFrame()
    {
        int index = 0;
        setCurrentFrame(index);
    }

    public void moveToNextFrame()
    {
        int index = (currentFrame + 1)%frameCount;
        setCurrentFrame(index);
    }

    public void moveToLastFrame()
    {
        int index = frameCount - 1;
        setCurrentFrame(index);
    }

    public double setFrameClosestTo(double value)
    {
        int frame = stackAxis.getClosestIndexWithinDataBounds(value);

        setCurrentFrame(frame);

        double newVal = stackAxis.getArgumentVal(frame);
        return newVal;
    }

    public void setCurrentFrame(int currentFrameNew)
    {
        int currentFrameOld = this.currentFrame;
        this.currentFrame = currentFrameNew;

        boolean oldFrameFirst = currentFrameFirst;
        this.currentFrameFirst = (this.currentFrame == 0);

        boolean oldFrameLast = currentFrameLast;
        this.currentFrameLast = (this.currentFrame == (frameCount - 1));

        resourceModel.setSelectedResource(currentFrame);

        propertySupport.firePropertyChange(FRAME_INDEX, currentFrameOld, currentFrameNew);
        propertySupport.firePropertyChange(FRAME_FIRST, oldFrameFirst, currentFrameFirst);
        propertySupport.firePropertyChange(FRAME_LAST, oldFrameLast, currentFrameLast);
    }

    public List<Channel2D> getChannels()
    {
        return channels;
    }

    public StackQuasiResource getSelectedResource()
    {
        return resourceModel.getSelectedResource();
    }

    public String getSelectedType()
    {
        return stackType;
    }


    public List<StackQuasiResource> getAllSelectedResources()
    {
        return resourceModel.getAllSelectedResources();
    }

    public List<StackQuasiResource> getResources()
    {
        return resourceModel.getResources();
    }

    public List<StackQuasiResource> getAdditionalResources()
    {
        return Collections.emptyList();
    }

    public int getResourceCount()
    {
        return resourceModel.getResourceCount();
    }

    public Set<String> getSelectedResourcesChannelIdentifiers()
    {
        return resourceModel.getSelectedResourcesChannelIdentifiers();
    }

    public Set<String> getSelectedResourcesChannelIdentifiers(ChannelFilter2<Channel2D> filter)
    {
        return resourceModel.getSelectedResourcesChannelIdentifiers(filter);
    }

    public Set<String> getAllResourcesChannelIdentifiers()
    {
        return resourceModel.getAllResourcesChannelIdentifiers();
    }

    public Set<String> getAllResourcesChannelIdentifiers(ChannelFilter2<Channel2D> filter)
    {
        return resourceModel.getAllResourcesChannelIdentifiers(filter);
    }

    public Set<String> getSelectedResourcesChannelIdentifiers(String type)
    {
        return resourceModel.getSelectedResourcesChannelIdentifiers(type);
    }

    public Set<String> getAllResourcesChannelIdentifiers(String type)
    {
        return resourceModel.getAllResourcesChannelIdentifiers(type);
    }

    public Set<String> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        return resourceModel.getSelectedResourcesChannelIdentifiers(type, filter);
    }

    public Set<String> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        return resourceModel.getAllResourcesChannelIdentifiers(type, filter);
    }

    public void addResourceSelectionListener(SelectionListener<? super Channel2DResource> listener)
    {
        resourceModel.addSelectionListener(listener);
    }

    public void removeResourceSelectionListener(SelectionListener<? super Channel2DResource> listener)
    {
        resourceModel.removeSelectionListener(listener);
    }

    public void addResourceDataListener(ResourceGroupListener<? super Channel2DResource> listener)
    {
        resourceModel.addDataModelListener(listener);
    }

    public void removeResourceDataListener(ResourceGroupListener<? super Channel2DResource> listener)
    {
        resourceModel.removeDataModelListener(listener);
    }

    public void addResourceTypeListener(ResourceTypeListener listener)
    {
        resourceModel.addResourceTypeListener(listener);
    }

    public void removeResourceTypeListener(ResourceTypeListener listener)
    {
        resourceModel.removeResourceTypeListener(listener);
    }

    public boolean canRedoBeEnabled()
    {
        return resourceModel.canRedoBeEnabled();
    }

    public boolean canUndoBeEnabled()
    {
        return resourceModel.canUndoBeEnabled();
    }

    public boolean canRedoAllBeEnabled()
    {
        return resourceModel.canRedoAllBeEnabled();
    }

    public boolean canUndoAllBeEnabled()
    {
        return resourceModel.canUndoAllBeEnabled();
    }

    public void redoAll(ResourceView<?,?,?> manager) 
    {
        this.resourceModel.redoAll(manager);
    }

    public void undoAll(ResourceView<?,?,?> manager)
    {
        this.resourceModel.undoAll(manager);
    }

    public boolean containsResource(Object resource)
    {
        return resourceModel.containsResource(resource);
    }

    public int getResourceIndex(Object resource)
    {
        return resourceModel.getResourceIndex(resource);
    }

    public List<String> getDefaultOutputNames() 
    {
        List<String> outputNames = new ArrayList<>();

        for(int i = 0; i<frameCount; i++)
        {
            String name = sourceShortName + " " + getStackLabel(i) + " (" + i + ")";
            outputNames.add(name);
        }

        return outputNames;
    }

    public Map<String, PrefixedUnit> getIdentifierUnitMap()
    {
        Channel2D channel = getGuidingChannel();
        String identifier = channel.getIdentifier();
        PrefixedUnit unit = channel.getZQuantity().getUnit();

        Map<String, PrefixedUnit> map = Collections.singletonMap(identifier, unit);

        return map; 
    }

    public Map<String, Map<Object, QuantitativeSample>> getSamples()
    {
        Map<String, Map<Object, QuantitativeSample>> samples = new LinkedHashMap<>();
        Map<Object, QuantitativeSample> samplesForType = new LinkedHashMap<>();

        samples.put(stackType, samplesForType);

        for(int i = 0; i<channels.size();i++)
        {
            Channel2D channel = channels.get(i);
            String info = channel.getChannelInfo() + " ("+ (i + 1) + ")";
            QuantitativeSample sample = channel.getZSample();
            samplesForType.put(info, sample);
        }

        return samples;
    }

    public Map<String, Map<Object, QuantitativeSample>> getSamples(int index)
    {
        if(index < 0 || index >= frameCount)
        {
            return Collections.emptyMap();
        }

        Map<String, Map<Object, QuantitativeSample>> samples = new LinkedHashMap<>();
        Map<Object, QuantitativeSample> samplesForType = new LinkedHashMap<>();

        samples.put(stackType, samplesForType);

        Channel2D channel = channels.get(index);
        String info = channel.getChannelInfo() + " ("+ (index + 1) + ")";
        QuantitativeSample sample = channel.getZSample();
        samplesForType.put(info, sample);

        return samples;
    }

    public Map<String, QuantitativeSample> getSamples(boolean includeCoordinates)
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        if(includeCoordinates)
        {
            Channel2D channel = channels.get(currentFrame);
            QuantitativeSample xCoords = channel.getXSample();
            QuantitativeSample yCoords = channel.getYSample();
            samples.put(Datasets.X_COORDINATE, xCoords);
            samples.put(Datasets.Y_COORDINATE, yCoords);
        }

        for(int i = 0; i<channels.size();i++)
        {
            Channel2D channel = channels.get(i);
            QuantitativeSample sample = channel.getZSample();
            String name = channel.getIdentifier() + " " + channel.getChannelInfo() + " ("+ (i + 1) + ")";;
            samples.put(name, sample);
        }

        return samples;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertySupport.removePropertyChangeListener(listener);
    }
}
