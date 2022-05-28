
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

package atomicJ.sources;


import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



import org.jfree.data.Range;

import atomicJ.analysis.NumericalSpectroscopyProcessingResults;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.SimpleProcessedPackFunction;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.Channel2DStandard;
import atomicJ.data.Grid2D;
import atomicJ.data.GridBlock;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.GridIndex;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.readers.ReadingPack;
import atomicJ.readers.regularImage.Channel2DSourceMetadata;
import atomicJ.utilities.ArrayUtilities;


public class MapGridSource extends AbstractChannel2DSource<Channel2D> implements MapSource<Channel2D>
{
    private final List<SimpleSpectroscopySource> simpleSources;
    private final Map<Point2D, ProcessedSpectroscopyPack> processedPacks = new LinkedHashMap<>();

    private final SimpleSpectroscopySource[][] gridSourcesData;
    private final ProcessedSpectroscopyPack[][] gridProcessedPackData;
    private ReadingPack<ImageSource> mapAreaImageInfo;

    private boolean processed = false; // if it is processed at least one node is filled
    private boolean processedAsFullGrid = false; //if it is processed as a full grid, i.e. each node is present
    private boolean isSealed = false;

    private Grid2D mapGrid;

    private final Map<ProcessedPackFunction, String> channelPackFunctionMap = new LinkedHashMap<>();

    public MapGridSource(File f, List<SimpleSpectroscopySource> simpleSources, Grid2D mapGrid)
    {
        super(f);	

        int n = mapGrid.getRowCount();
        int m = mapGrid.getColumnCount();

        this.mapGrid = mapGrid;

        this.gridSourcesData = new SimpleSpectroscopySource[n][m];
        this.gridProcessedPackData = new ProcessedSpectroscopyPack[n][m];

        this.simpleSources = simpleSources;

        initSourceList();
        initializeSourceGrid();
    }

    public MapGridSource(File f, Channel2DSourceMetadata metadata, List<SimpleSpectroscopySource> simpleSources,
            Grid2D grid, String shortName, String longName)
    {
        super(metadata, f, shortName, longName);

        int n = grid.getRowCount();
        int m = grid.getColumnCount();

        this.gridSourcesData = new SimpleSpectroscopySource[n][m];
        this.gridProcessedPackData = new ProcessedSpectroscopyPack[n][m];

        this.simpleSources = simpleSources;

        initSourceList();
        initializeSourceGrid();
    }

    public MapGridSource(String pathname, Channel2DSourceMetadata metadata, List<SimpleSpectroscopySource> simpleSources,
            Grid2D grid, double probingDensity, String shortName, String longName)
    {
        super(metadata, pathname, shortName, longName);

        int n = grid.getRowCount();
        int m = grid.getColumnCount();

        this.gridSourcesData = new SimpleSpectroscopySource[n][m];
        this.gridProcessedPackData = new ProcessedSpectroscopyPack[n][m];

        this.simpleSources = simpleSources;

        initSourceList();
        initializeSourceGrid();
    }

    public MapGridSource(MapGridSource that)
    {
        this(that, that.getIdentifiers()); 
    }

    public MapGridSource(MapGridSource that, Collection<String> identifiers)
    {
        super(that, identifiers);

        this.gridSourcesData = ArrayUtilities.deepCopy(that.gridSourcesData);
        this.gridProcessedPackData = ArrayUtilities.deepCopy(that.gridProcessedPackData);
        this.simpleSources = copyAndInitSimpleSources(that.simpleSources);	
        this.mapAreaImageInfo = that.mapAreaImageInfo;
        this.channelPackFunctionMap.putAll(that.channelPackFunctionMap);
        this.mapGrid = that.mapGrid;//Grid2D is immutable
    }

    @Override
    public List<Channel2D> getChannelCopies()
    {
        List<Channel2D> channels = getChannels();
        List<Channel2D> channelsCopied = new ArrayList<>();

        for(Channel2D channel : channels)
        {
            channelsCopied.add(channel.getCopy());
        }

        return channelsCopied;
    }

    @Override
    public List<Channel2D> getChannelCopies(Collection<String> identifiers)
    {
        List<Channel2D> channelsForIdentifiers = new ArrayList<>();

        for(String identifier : identifiers)
        {
            Channel2D channel = getChannel(identifier);
            if(channel != null)
            {
                channelsForIdentifiers.add(channel);
            }
        }

        return channelsForIdentifiers;
    }

    @Override
    public Channel2D duplicateChannel(String identifier)
    {
        Channel2D channel = getChannel(identifier);
        Channel2D channelCopy = channel.duplicate();

        int indexNew = getChannelPosition(identifier) + 1;
        insertChannel(channelCopy, indexNew);

        return channelCopy;
    }


    @Override
    public Channel2D duplicateChannel(String identifier, String identifierNew)
    {
        Channel2D channel = getChannel(identifier);
        Channel2D channelCopy = channel.duplicate(identifierNew);

        int indexNew = getChannelPosition(identifier) + 1;
        insertChannel(channelCopy, indexNew);

        return channelCopy;
    }

    private void initSourceList()
    {
        int n = simpleSources.size();

        for(int i = 0; i<n; i++)
        {
            SimpleSpectroscopySource source = simpleSources.get(i);
            source.setForceMap(this);
            source.setMapPosition(i);
        }
    }

    private void initializeSourceGrid()
    {
        for(SimpleSpectroscopySource source : simpleSources)
        {   
            Point2D p = source.getRecordingPoint();

            int i = mapGrid.getRow(p);
            int j = mapGrid.getColumn(p);

            gridSourcesData[i][j] = source;                 
        }          
    }

    @Override
    public ReadingPack<ImageSource> getMapAreaImageReadingPack()
    {
        return mapAreaImageInfo;
    }

    @Override
    public boolean isMapAreaImagesAvailable()
    {
        boolean available = (this.mapAreaImageInfo != null);
        return available;
    }

    @Override
    public void setMapAreaImageReadingPack(ReadingPack<ImageSource> mapAreaImageInfo)
    {
        this.mapAreaImageInfo = mapAreaImageInfo;
    }

    //we should call setForceMap(null) on the old map
    @Override
    public void replaceSpectroscopySource(SimpleSpectroscopySource source, int index)
    {
        SimpleSpectroscopySource oldSource = simpleSources.get(index);
        if(oldSource != null)
        {
            oldSource.setForceMap(null);
        }

        simpleSources.set(index, source);

        //this has to called after oldSource.setForceMap(null), because it may be the case that oldSource and source are the same object
        source.setForceMap(this);
        source.setMapPosition(index);

        Point2D p = source.getRecordingPoint();

        int i = mapGrid.getRow(p);
        int j = mapGrid.getColumn(p);

        gridSourcesData[i][j] = source;  

        setPackForPoint(null, p);    
    }

    private List<SimpleSpectroscopySource> copyAndInitSimpleSources(List<SimpleSpectroscopySource> sourcesOld)
    {
        List<SimpleSpectroscopySource> sourcesNew = new ArrayList<>();

        int n = sourcesOld.size();

        for(int i = 0; i<n; i++)
        {
            SimpleSpectroscopySource sourceOld = sourcesOld.get(i);
            SimpleSpectroscopySource sourceNew = sourceOld.copy();
            sourceNew.setForceMap(this);
            sourceNew.setMapPosition(i);
            sourcesNew.add(sourceNew);
        }

        return sourcesNew;
    }

    public GridIndex getClosestGridIndex(Point2D p)
    {
        return mapGrid.getGridIndex(p);
    }

    @Override
    public boolean isRecordedAsGrid()
    {
        return true;
    }

    @Override
    public boolean isProcessedAsFullGrid()
    {
        return processedAsFullGrid;
    }

    @Override
    public boolean isProcessed()
    {
        return processed;
    }

    @Override
    public boolean isSealed()
    {
        return isSealed;
    }

    @Override
    public void seal()
    {        
        isSealed = true;

        checkIfIsProcessed();
        checkIfProcessedAsGrid();
        initMapChannels();    
    }

    private void checkIfProcessedAsGrid()
    {
        processedAsFullGrid = true;
        for(ProcessedSpectroscopyPack[] row: gridProcessedPackData)
        {
            for(ProcessedSpectroscopyPack item : row)
            {
                boolean itemIsNull = (item == null);
                if(itemIsNull)
                {
                    processedAsFullGrid = false;
                    return;
                }
            }
        }
    }

    private void checkIfIsProcessed()
    {
        boolean processed = false;

        for(ProcessedSpectroscopyPack[] packs : gridProcessedPackData)
        {
            for(ProcessedSpectroscopyPack pack : packs)
            {
                processed = processed || (pack != null);

                if(processed)
                {
                    this.processed = processed;
                    return;
                }
            }
        }

        this.processed = processed;
    }

    @Override
    public ProcessedSpectroscopyPack getProcessedPack(int i)
    {
        List<ProcessedSpectroscopyPack> packs = new ArrayList<>(processedPacks.values());
        return packs.get(i);
    }

    @Override
    public SimpleSpectroscopySource getSimpleSpectroscopySource(Point2D p)
    {
        int i = mapGrid.getRow(p);
        int j = mapGrid.getColumn(p);

        int n = mapGrid.getRowCount();
        int m = mapGrid.getColumnCount();


        SimpleSpectroscopySource source = null;
        if(i >= 0 && i<n && j >= 0 && j<m)
        {
            source = gridSourcesData[i][j];
        }
        return source;
    }

    @Override
    public ProcessedSpectroscopyPack getProcessedPack(Point2D p)
    {
        int i = mapGrid.getRow(p);
        int j = mapGrid.getColumn(p);

        int n = mapGrid.getRowCount();
        int m = mapGrid.getColumnCount();

        ProcessedSpectroscopyPack pack = (i<n && j<m) ? gridProcessedPackData[i][j] : null;
        return pack;
    }

    @Override
    public Map<Point2D, ProcessedSpectroscopyPack> getProcessedPacksMap()
    {
        return processedPacks;
    }

    @Override
    public void registerProcessedPack(ProcessedSpectroscopyPack pack)
    {
        SimpleSpectroscopySource source = pack.getSource();
        Point2D p = source.getRecordingPoint();

        setPackForPoint(pack, p);
    }

    @Override
    public ProcessedPackReplacementResults replaceProcessedPacks(List<ProcessedSpectroscopyPack> packsToReplace)
    {
        updatePacks(packsToReplace);
        Map<String, Channel2D> updatedChannels = updateChannels(packsToReplace);
        Map<String, Channel2D> addedChannels = addPreviouslyAbsentChannelsFromReplacedPacks(packsToReplace);

        ProcessedPackReplacementResults results = new ProcessedPackReplacementResults(addedChannels, updatedChannels);

        return results;
    }

    private Map<String, Channel2D> updateChannels(List<ProcessedSpectroscopyPack> packsToReplace)
    {
        Map<String, Channel2D> changedChannels = new LinkedHashMap<>();

        for(Entry<ProcessedPackFunction, String> entry : channelPackFunctionMap.entrySet())
        {
            ProcessedPackFunction function = entry.getKey();

            String id = entry.getValue();
            String universalId = getChannelUniversalIdentifier(id);

            Channel2D channel = getChannel(id);

            if(channel != null)
            {
                channel.transform(new ProcessedPackTransformation(packsToReplace, function));
                changedChannels.put(universalId, channel);
            }  
        }

        return changedChannels;
    }

    private Map<String, Channel2D> addPreviouslyAbsentChannelsFromReplacedPacks(List<ProcessedSpectroscopyPack> packsToReplace)
    {
        Map<String, Channel2D> addedChannels = new LinkedHashMap<>();
        Set<ProcessedPackFunction> previouslyAbsentPackFunctions = getSpecialPackFunctionsPreviouslyAbsent(packsToReplace);

        for(ProcessedPackFunction f : previouslyAbsentPackFunctions)
        {
            Quantity quantity = f.getEvaluatedQuantity();
            double[][] data = getGridStyleData(f);  

            Channel2DData channelData = new GridChannel2DData(data, mapGrid, quantity);
            Channel2D channel = new Channel2DStandard(channelData, quantity.getName());
            addChannel(channel);

            String id = channel.getIdentifier();
            channelPackFunctionMap.put(f, id);

            String universalId = getChannelUniversalIdentifier(id);
            addedChannels.put(universalId, channel);
        }

        return addedChannels;
    }

    private Set<ProcessedPackFunction> getSpecialPackFunctionsPreviouslyAbsent(List<ProcessedSpectroscopyPack> packsToReplace)
    {
        Set<ProcessedPackFunction> newSpecialFunctions = new LinkedHashSet<>();

        for(ProcessedSpectroscopyPack pack : packsToReplace)
        {
            if(pack != null)
            {
                newSpecialFunctions.addAll(pack.getSpecialFunctions());                
            }
        }

        newSpecialFunctions.removeAll(channelPackFunctionMap.keySet());

        return newSpecialFunctions;
    }


    private void updatePacks(List<ProcessedSpectroscopyPack> packsToReplace) 
    {
        for(ProcessedSpectroscopyPack pack : packsToReplace)
        {
            SimpleSpectroscopySource source = pack.getSource();
            Point2D p = source.getRecordingPoint();

            if(p != null)
            {   
                int i = mapGrid.getRow(p);
                int j = mapGrid.getColumn(p);

                ProcessedSpectroscopyPack packOld = gridProcessedPackData[i][j];
                gridProcessedPackData[i][j] = pack;  
                gridSourcesData[i][j] = pack.getSource();

                if(packOld != null)
                {
                    SimpleSpectroscopySource sourceOld = packOld.getSource();
                    sourceOld.setForceMap(null);

                    int indexReplaced = sourceOld.getMapPosition();
                    simpleSources.set(indexReplaced, source);
                    source.setMapPosition(indexReplaced);
                }

                //this has to called after oldSource.setForceMap(null), because it may be the case that oldSource and source are the same object
                source.setForceMap(this);
            }
        }
    }

    private void setPackForPoint(ProcessedSpectroscopyPack pack, Point2D p)
    {
        if(p != null)
        {	
            int i = mapGrid.getRow(p);
            int j = mapGrid.getColumn(p);

            gridProcessedPackData[i][j] = pack;		            
        }	       
    }

    @Override
    public Set<ProcessedSpectroscopyPack> getProcessedPacks()
    {        
        Set<ProcessedSpectroscopyPack> packs = new LinkedHashSet<>();

        int rowCount = mapGrid.getRowCount();
        int columnCount = mapGrid.getColumnCount();

        for(int i = 0; i<rowCount; i++)
        {
            for(int j = 0; j<columnCount; j++)
            {
                packs.add(gridProcessedPackData[i][j]);
            }
        }
        return packs;
    }

    @Override
    public Set<ProcessedSpectroscopyPack> getProcessedPacks(ROI roi, ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getProcessedPacks();
        }

        final Set<ProcessedSpectroscopyPack> packs = new HashSet<>();

        roi.addPoints(mapGrid, position, new GridPointRecepient() 
        {                
            @Override
            public void addPoint(int i, int j) 
            {
                packs.add(gridProcessedPackData[i][j]);                 
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    ProcessedSpectroscopyPack[] row = gridProcessedPackData[i];
                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        packs.add(row[j]);                 
                    }
                }
            }
        });

        return packs;
    }

    @Override
    public List<SimpleSpectroscopySource> getSimpleSources(ROI roi, ROIRelativePosition position) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getSimpleSources();
        }

        final List<SimpleSpectroscopySource> sources = new ArrayList<>();

        roi.addPoints(mapGrid, position, new GridPointRecepient() 
        {                
            @Override
            public void addPoint(int i, int j) 
            {
                sources.add(gridSourcesData[i][j]);                 
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    SimpleSpectroscopySource[] row = gridSourcesData[i];
                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        sources.add(row[j]);                 
                    }
                }
            }
        });

        return sources;
    }

    @Override
    public double getProbingDensity()
    {
        return mapGrid.getGridDensity();
    }

    @Override
    public MapGridSource copy() 
    {
        return new MapGridSource(this);
    }

    @Override
    public MapGridSource copy(Collection<String> identifiers) 
    {
        return new MapGridSource(this, identifiers);
    }

    @Override
    public List<SimpleSpectroscopySource> getSimpleSources() 
    {		
        return simpleSources;
    }

    @Override
    public Channel2D getChannel(ProcessedPackFunction f)
    {
        Quantity quantity = f.getEvaluatedQuantity();

        double[][] data = getGridStyleData(f);   

        Channel2D channel = new Channel2DStandard(new GridChannel2DData(data, mapGrid, quantity), quantity.getName());
        return channel;
    }

    @Override
    public Channel2D getChannel(ROI roi, ROIRelativePosition position, final ProcessedPackFunction f)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getChannel(f);
        }

        Quantity quantity = f.getEvaluatedQuantity();		

        GridBlock block = mapGrid.getInscribedBlock(roi, position);

        final int minColumn = block.getMinimalColumn();
        final int minRow = block.getMinimalRow();

        int rowCountNew = block.getRowCount();
        int columnCountNew = block.getColumnCount();

        Grid2D gridNew = new Grid2D(mapGrid.getXIncrement(), mapGrid.getYIncrement(), mapGrid.getX(minColumn), mapGrid.getY(minRow), rowCountNew, columnCountNew, mapGrid.getXQuantity(), mapGrid.getYQuantity());

        final double[][] data = new double[rowCountNew][columnCountNew];

        roi.addPoints(mapGrid, position, new GridPointRecepient() {

            @Override
            public void addPoint(int row, int column) {
                data[row - minRow][column - minColumn] = f.evaluate(gridProcessedPackData[row][column]);                             
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) {
                for(int row = rowFrom; row<rowTo; row++)
                {
                    for(int column = columnFrom; column<columnTo; column++)
                    {
                        data[row - minRow][column - minColumn] = f.evaluate(gridProcessedPackData[row][column]);                             
                    }             
                }
            }
        });


        Channel2DData channelData = new GridChannel2DData(data, gridNew, quantity);
        Channel2D channel = new Channel2DStandard(channelData, quantity.getName());
        return channel;
    }

    private void initMapChannels()
    {
        List<ProcessedPackFunction> allFunctions = new ArrayList<>(Arrays.asList(SimpleProcessedPackFunction.values()));
        allFunctions.addAll(getSpecialPackFunctions());

        List<Channel2D> channels = new ArrayList<>();

        if(processed)
        {
            for(ProcessedPackFunction f: allFunctions)
            {
                Quantity quantity = f.getEvaluatedQuantity();
                double[][] data = getGridStyleData(f);  

                Channel2DData channelData = new GridChannel2DData(data, mapGrid, quantity);
                Channel2D channel = new Channel2DStandard(channelData, quantity.getName());
                channels.add(channel);

                channelPackFunctionMap.put(f, channel.getIdentifier());
            }   
        }

        setChannels(channels);
    }

    private Set<ProcessedPackFunction> getSpecialPackFunctions()
    {
        Set<ProcessedPackFunction> specialFunctions = new LinkedHashSet<>();

        int n = mapGrid.getRowCount();
        int m = mapGrid.getColumnCount();

        for(int i = 0; i<n; i++)
        {
            ProcessedSpectroscopyPack[] packRow = gridProcessedPackData[i];
            for(int j = 0; j<m; j++)
            {
                ProcessedSpectroscopyPack pack = packRow[j];
                if(pack != null)
                {
                    specialFunctions.addAll(pack.getSpecialFunctions());                
                }
            }
        }

        return specialFunctions;
    }

    public double[][] getGridStyleData(ProcessedPackFunction f)
    {
        int n = mapGrid.getRowCount();
        int m = mapGrid.getColumnCount();
        double[][] channelData = new double[n][m];

        for(int i = 0; i<n; i++)
        {
            ProcessedSpectroscopyPack[] packRow = gridProcessedPackData[i];

            for(int j = 0; j<m; j++)
            {
                ProcessedSpectroscopyPack pack = packRow[j];
                channelData[i][j] = pack != null ? f.evaluate(pack) : Double.NaN;				
            }
        }

        return channelData;
    }

    public double[][] getGridStyleData(Shape shape, ProcessedPackFunction f)
    {
        int n = mapGrid.getRowCount();
        int m = mapGrid.getColumnCount();

        Rectangle2D bounds = shape.getBounds2D();

        int minColumn = Math.max(0, mapGrid.getColumn(bounds.getMinX()) - 1);
        int maxColumn = Math.min(m, mapGrid.getColumn(bounds.getMaxX()) + 1);
        int minRow = Math.max(0, mapGrid.getRow(bounds.getMinY()) - 1);
        int maxRow = Math.min(n, mapGrid.getRow(bounds.getMaxY()) + 1);

        int rowCountNew = maxRow - minRow;
        int columnCountNew = maxColumn - minColumn;

        Grid2D gridNew = new Grid2D(mapGrid.getXIncrement(), mapGrid.getYIncrement(), mapGrid.getX(minColumn), mapGrid.getY(minRow), rowCountNew, columnCountNew, mapGrid.getXQuantity(), mapGrid.getYQuantity());

        double[][] channelData = new double[rowCountNew][columnCountNew];

        for(int i = minRow; i<maxRow; i++)
        {
            for(int j = minColumn; j<maxColumn;j++)
            {
                Point2D point = mapGrid.getPoint(i, j);
                if(shape.contains(point))
                {
                    channelData[i - minRow][j - minColumn] = f.evaluate(gridProcessedPackData[i][j]);
                }
            }
        }

        return channelData;
    }


    @Override
    public Range getHeightRange()
    {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for(ProcessedSpectroscopyPack[] packs : gridProcessedPackData)
        {
            for(ProcessedSpectroscopyPack pack : packs)
            {
                NumericalSpectroscopyProcessingResults result = pack.getResults();
                double contactHeight = result.getContactDisplacement();
                min = min > contactHeight ? contactHeight : min;
                max = max < contactHeight ? contactHeight : max;
            }         
        }

        Range range = new Range(min, max);

        return range;
    }
}
