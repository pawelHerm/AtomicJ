
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

package atomicJ.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Size2D;

import atomicJ.data.Channel1D;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.UnitPoint2D;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.NameChangeModel.DatasetNameChangeModel;
import atomicJ.utilities.Validation;


public abstract class CustomizableXYBasePlot extends XYPlot implements PreferencesSource, AxisSource, RoamingTitleChangeListener
{
    private static final long serialVersionUID = 1L;

    private RoamingLegend roamingLegend;
    private RoamingTextTitle roamingTextTitle;

    private Map<Object, Integer> layerMap = new LinkedHashMap<>();    

    //we will store the axes because when a layer is removed, the corresponding axis should be set to null,
    //by calling setRangeAxis(layerIndex) or setDomainAxis(layerIndex). However, next time the same layer (with the same key) is added
    //we want to add the axis with exactly the same style as previously
    //to do so, it is easiest to keep references in this map

    private Map<Object, CustomizableNumberAxis> layerSpecificRangeAxes = new LinkedHashMap<>();
    private Map<Object, CustomizableNumberAxis> generalDomainAxes = new LinkedHashMap<>();
    private Map<Object, CustomizableNumberAxis> generalRangeAxes = new LinkedHashMap<>();

    private int activeLayerIndex = 0;

    private List<RoamingLegend> roamingSublegends = new ArrayList<>(); 

    private final PreferredBasePlotStyle prefStyle;

    public CustomizableXYBasePlot(Preferences pref)
    {
        this.prefStyle = PreferredBasePlotStyle.getInstance(pref, null);

        setPreferredStyle(prefStyle);
        setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        setDomainZeroBaselineVisible(false);
        setRangeZeroBaselineVisible(false);

        setRangePannable(true);
        setDomainPannable(true);

        setGridlinesOnTop(false);     
    }

    public CustomizableXYBasePlot(Preferences pref, String styleKey)
    {
        this.prefStyle = PreferredBasePlotStyle.getInstance(pref, styleKey);

        setFixedDataAreaSize(new Size2D(500,500));
        setUseFixedDataAreaSize(true);

        setPreferredStyle(this.prefStyle);
        setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        setDomainZeroBaselineVisible(false);
        setRangeZeroBaselineVisible(false);

        setRangePannable(true);
        setDomainPannable(true);

        setGridlinesOnTop(false);
    }

    private void setPreferredStyle(PreferredBasePlotStyle prefStyle)
    {       
        Paint backgroundPaint = prefStyle.getBackgroundPaint();
        Paint outlinePaint = prefStyle.getOutlinePaint();
        Paint domainGridlinePaint = prefStyle.getDomainGridlinePaint();
        Paint rangeGridlinePaint = prefStyle.getRangeGridlinePaint();
        Stroke outlineStroke = prefStyle.getOutlineStroke();
        Stroke domainGridlineStroke = prefStyle.getDomainGridlineStroke();
        Stroke rangeGridlineStroke = prefStyle.getRangeGridlineStroke();
        PlotOrientation plotOrientation = prefStyle.getPlotOrientation();

        boolean outlineVisible = prefStyle.isOutlineVisible();
        boolean domainGridlinesVisible = prefStyle.isDomainGridlinesVisible();
        boolean rangeGridlinesVisible = prefStyle.isRangeGridlinesVisible();

        setBackgroundPaint(backgroundPaint);

        setOutlineStroke(outlineStroke);
        setOutlinePaint(outlinePaint);
        setOutlineVisible(outlineVisible);

        setDomainGridlineStroke(domainGridlineStroke);
        setDomainGridlinesVisible(domainGridlinesVisible);
        setDomainGridlinePaint(domainGridlinePaint);

        setRangeGridlineStroke(rangeGridlineStroke);        
        setRangeGridlinePaint(rangeGridlinePaint);
        setRangeGridlinesVisible(rangeGridlinesVisible);        

        setOrientation(plotOrientation);
    }   



    ////////////////////// RENDERERS /////////////////////////////////////

    @Override
    public ChannelRenderer getRenderer()
    {
        ChannelRenderer renderer = (ChannelRenderer)super.getRenderer();
        return renderer;
    }

    @Override
    public ChannelRenderer getRenderer(int i)
    {
        ChannelRenderer renderer = (ChannelRenderer)super.getRenderer(i);
        return renderer;
    }

    public List<ChannelRenderer> getRenderers()
    {
        List<ChannelRenderer> renderers = new ArrayList<>();

        for(int i = 0; i<getRendererCount(); i++)
        {
            renderers.add(getRenderer(i));
        }

        return renderers;
    }

    @Override 
    public void setRenderer(XYItemRenderer renderer)
    {
        if(renderer instanceof ChannelRenderer)
        {
            super.setRenderer(renderer);
        }
        else 
        {
            throw new IllegalArgumentException();
        }
    }

    @Override 
    public void setRenderer(int index, XYItemRenderer renderer)
    {
        if(renderer instanceof ChannelRenderer)
        {
            super.setRenderer(index, renderer);
        }
        else 
        {
            throw new IllegalArgumentException();
        }
    }

    @Override 
    public void setRenderer(int index, XYItemRenderer renderer, boolean notify)
    {
        if(renderer instanceof ChannelRenderer)
        {
            super.setRenderer(index, renderer, notify);
        }
        else 
        {
            throw new IllegalArgumentException();
        }
    }

    ///////////////////// END OF RENDERERS /////////////////////////////////////////////////

    public int getActiveLayerIndex()
    {
        return activeLayerIndex;
    }

    public void setActiveLayerIndex(int activeLayerNew)
    {
        int layerCount = layerMap.size();

        boolean withinRange = (activeLayerNew >= 0 && activeLayerNew < layerCount);
        if(withinRange)
        {
            this.activeLayerIndex = activeLayerNew;
        }
    }


    public boolean removeLayer(Object key)
    {
        return removeLayer(key, true);
    }

    public boolean removeLayer(Object key, boolean removeAxisIfUnused)
    {
        boolean removed = false;
        Integer layerIndex = layerMap.get(key);

        if(layerIndex != null)
        {
            removed = (layerMap.remove(key) != null);

            setDataset(layerIndex, null);
            ValueAxis axisForRemovedDataset = getRangeAxisForDataset(layerIndex);      

            if(removeAxisIfUnused)
            {
                int axisIndex = getRangeAxisIndex(axisForRemovedDataset);
                removeRangeAxisIfUnusedAndCleanUp(axisIndex, false);
            }   
        }

        return removed;
    }


    public void simplifyLayerIndices()
    {
        int layerCount = layerMap.size();
        int maxLayerIndex = getMaxLayerIndex();

        List<Map.Entry<Object, Integer>> sortedIntegerIndices = new ArrayList<>(layerMap.entrySet());
        sortedIntegerIndices.sort((Map.Entry<Object, Integer> entry1, Map.Entry<Object, Integer> entry2) -> entry1.getValue().compareTo(entry2.getValue()));

        for(int i= 0; i < layerCount;i++)
        {
            Map.Entry<Object, Integer> entry = sortedIntegerIndices.get(i);
            Integer layerIndex = entry.getValue();
            Object key = entry.getKey();

            layerMap.put(key, Integer.valueOf(i));

            if(layerIndex.intValue() != i)
            {
                XYDataset dataset = getDataset(layerIndex);
                ChannelRenderer renderer = getRenderer(layerIndex);

                setDataset(i,dataset);
                setRenderer(i,renderer);
            }
        }

        for(int i = layerCount; i<= maxLayerIndex; i++)
        {
            setDataset(i,null);
        }
    }

    private int getMaxLayerIndex()
    {
        int maxIndex = -1;

        for(Integer index: layerMap.values())
        {
            maxIndex = Math.max(index.intValue(), maxIndex);
        }

        return maxIndex;
    }

    public void addOrReplaceLayerWithOwnAxis(Object key, XYDataset dataset, XYItemRenderer renderer, Quantity rangeQuantity)
    {
        int index = layerMap.containsKey(key) ? getLayerIndex(key) : layerMap.size();

        layerMap.put(key, index);

        ValueAxis axis = getRangeAxis(index);

        if(axis == null)
        {
            CustomizableNumberAxis newRangeAxis = layerSpecificRangeAxes.get(key);

            if(newRangeAxis == null)
            {           
                newRangeAxis = new CustomizableNumberAxis(rangeQuantity, prefStyle.getPreferences().node(AxisType.RANGE.toString()).node(key.toString()));
                layerSpecificRangeAxes.put(key, newRangeAxis);
            }

            setRangeAxis(index, newRangeAxis);

            mapDatasetToRangeAxis(index, index);          
            AxisLocation previousLocation = getRangeAxisLocation(Math.min(0, index - 1));
            setRangeAxisLocation(index, AxisLocation.getOpposite(previousLocation));                   
        }     

        setRenderer(index, renderer);
        setDataset(index, dataset);           
    }

    public void addOrReplaceData(Channel1D channel)
    {        
        Object key = channel.getIdentifier();
        ProcessableXYDataset<?> dataset = new Channel1DDataset(channel, channel.getName());
        XYItemRenderer oldRender = getLayerRenderer(key);
        XYItemRenderer renderer = (oldRender != null) ? oldRender : RendererFactory.getChannel1DRenderer(channel);
        addOrReplaceLayer(dataset, renderer);
    }

    public void addOrReplaceLayer(ProcessableXYDataset<?> dataset, XYItemRenderer renderer)
    {
        addOrReplaceLayer(dataset.getKey(), dataset, renderer);              
    }

    public void addOrReplaceLayer(Object key, ProcessableDataset<?>  dataset, XYItemRenderer renderer)
    {        
        int index = layerMap.containsKey(key) ? getLayerIndex(key) : layerMap.size();

        layerMap.put(key, index);

        ifNecessaryCreateDomainAxisAndMapDatasetToIt(key, index, dataset.getXQuantity());        
        ifNecessaryCreateRangeAxisAndMapDatasetToIt(key, index, dataset.getYQuantity());        

        setRenderer(index, renderer, false);
        setDataset(index, dataset);         
    }

    public void addOrReplaceLayer2(Object key, XYDataset  dataset, XYItemRenderer renderer, Quantity xQuantity, Quantity yQuantity)
    {                
        int index = layerMap.containsKey(key) ? getLayerIndex(key) : layerMap.size();

        layerMap.put(key, index);

        ifNecessaryCreateDomainAxisAndMapDatasetToIt(key, index, xQuantity);        
        ifNecessaryCreateRangeAxisAndMapDatasetToIt(key, index, yQuantity);        

        setRenderer(index, renderer);
        setDataset(index, dataset);             
    }

    private void ifNecessaryCreateDomainAxisAndMapDatasetToIt(Object key, int layerIndex, Quantity domainQuantity)
    {
        CustomizableNumberAxis domainAxis = generalDomainAxes.get(domainQuantity);

        if(domainAxis == null)
        {           
            domainAxis = buildNewDomainAxis(prefStyle.getPreferences().node(AxisType.DOMAIN.toString()).node(domainQuantity.getName()), domainQuantity);
            generalDomainAxes.put(domainQuantity, domainAxis);

            setDomainAxis(layerIndex, domainAxis, false);

            AxisLocation previousLocation = getDomainAxisLocation(Math.min(0, layerIndex - 1));
            setDomainAxisLocation(layerIndex, AxisLocation.getOpposite(previousLocation), false);      
        }

        int domainAxisIndex = getDomainAxisIndex(domainAxis);
        if(domainAxisIndex > 0)
        {
            mapDatasetToDomainAxis(layerIndex, domainAxisIndex);          
        }
    }

    private void ifNecessaryCreateRangeAxisAndMapDatasetToIt(Object key, int layerIndex, Quantity rangeQuantity)
    {
        CustomizableNumberAxis rangeAxis = generalRangeAxes.get(rangeQuantity);

        if(rangeAxis == null)
        {           
            rangeAxis = buildNewRangeAxis(prefStyle.getPreferences().node(AxisType.RANGE.toString()).node(rangeQuantity.getName()), rangeQuantity);

            generalRangeAxes.put(rangeQuantity, rangeAxis);

            int axisIndex = getIndexToAddNewRangeAxis();
            setRangeAxis(axisIndex, rangeAxis, false);

            AxisLocation previousLocation = getRangeAxisLocation(Math.max(0, axisIndex - 1));
            setRangeAxisLocation(axisIndex, AxisLocation.getOpposite(previousLocation), false);    
        } 

        int rangeAxisIndex = getRangeAxisIndex(rangeAxis);

        if(rangeAxisIndex < 0)//the axis was removed from the XYPlot, just kept in this class in the generalRangeAxes map
        {
            rangeAxisIndex = getIndexToAddNewRangeAxis();
            setRangeAxis(rangeAxisIndex, rangeAxis, false);
        }

        mapDatasetToRangeAxis(layerIndex, rangeAxisIndex);          
    }

    protected CustomizableNumberAxis buildNewRangeAxis(Preferences pref, Quantity quantity)
    {        
        CustomizableNumberAxis rangeAxis = new CustomizableNumberAxis(quantity, pref);

        return rangeAxis;
    }

    protected CustomizableNumberAxis buildNewDomainAxis(Preferences pref, Quantity quantity)
    {
        CustomizableNumberAxis domainAxis = new CustomizableNumberAxis(quantity, pref);

        return domainAxis;
    }

    public void insertLayer(Object key, Integer index, XYDataset dataset, XYItemRenderer renderer)
    {
        Map<Integer, Layer> layers = new LinkedHashMap<>();

        int oldIndexForKey = getLayerIndex(key);
        if(oldIndexForKey>=0)
        {
            removeLayer(key, false);
        }
        if(oldIndexForKey != index)
        {
            //changes indices of old layers if necessary
            for(Entry<Object, Integer> entry : layerMap.entrySet())
            {
                Object entryKey = entry.getKey();
                Integer entryIndex = entry.getValue();

                if(entryIndex>= index)
                {                
                    Integer newEntryIndex = entryIndex + 1;
                    XYDataset entryDataset =  getDataset(entryIndex);       
                    XYItemRenderer entryRenderer = getRenderer(entryIndex);

                    layerMap.put(entryKey, newEntryIndex);
                    layers.put(newEntryIndex, new Layer(entryKey, entryDataset, entryRenderer));
                }
            }

            for(Entry<Integer, Layer> entry: layers.entrySet())
            {
                Layer layer = entry.getValue();
                Integer entryIndex = entry.getKey();

                XYItemRenderer layerRenderer = layer.getRenderer();
                XYDataset layerDataset = layer.getDataset();

                setRenderer(entryIndex, layerRenderer);
                setDataset(entryIndex, layerDataset);
            }
        }


        //sets the inserted layer
        layerMap.put(key, index);

        setRenderer(index, renderer);     
        setDataset(index, dataset); 
    }

    public void setLayer(Object key, XYDataset dataset, XYItemRenderer renderer)
    {
        Integer layerIndex = layerMap.get(key);
        if(layerIndex != null)
        {
            setRenderer(layerIndex, renderer);
            setDataset(layerIndex, dataset);       
        }
    }

    public void replaceLayerDataset(Object key, XYDataset dataset)
    {
        Integer layerIndex = layerMap.get(key);

        if(layerIndex != null)
        {                          
            setDataset(layerIndex, dataset);
        }
    }

    public boolean containsLayer(Object key)
    {
        boolean contains = layerMap.containsKey(key);
        return contains;
    }

    public void clearAllLayerData()
    {
        for(Object key : layerMap.keySet())
        {
            clearLayerData(key);
        }
    }

    public void clearLayerData(Object key)
    {
        clearLayerData(key, false);
    }   

    public void clearLayerData(Object key, boolean removeAxis)
    {
        if(layerMap.containsKey(key))
        {
            int layerIndex = layerMap.get(key);
            setDataset(layerIndex, null);  

            System.err.println("LAYER INDEX "+layerIndex);

            if(removeAxis)
            {
                ValueAxis axis = getRangeAxis(layerIndex);

                System.err.println("AXIS IS MAIN "+(axis == getRangeAxis()));
                System.err.println("AXIS 1 IS MAIN "+(axis == getRangeAxis(1)));


                if(axis != null)
                {
                    setRangeAxis(layerIndex, null);
                }
            }          
        }
    }   


    public ChannelRenderer getLayerRenderer(Object key)
    {
        ChannelRenderer renderer = null;
        Integer index = layerMap.get(key);

        if(index != null)
        {
            renderer = getRenderer(index);
        }

        return renderer;
    }

    public int getLayerIndex(Object key)
    {
        Integer index = layerMap.get(key);

        int result = (index == null)? -1 : index.intValue();

        return result;
    }

    public int getLayerCount()
    {
        return layerMap.size();
    }

    public RoamingLegend getRoamingLegend()
    {
        return roamingLegend;
    }

    public void setRoamingLegend(RoamingLegend legend)
    {
        Validation.requireNonNullParameterName(legend, "legend");

        if(this.roamingLegend  != null)
        {
            this.roamingLegend.removeRoamingTitleChangeListener(this);
        }
        this.roamingLegend = legend;
        this.roamingLegend.addRoamingTitleChangeListener(this); 
    }


    public List<RoamingLegend> getRoamingSublegends()
    {
        return roamingSublegends;
    }

    public void addRoamingSublegend(RoamingLegend legend)
    {
        Validation.requireNonNullParameterName(legend, "legend");

        legend.addRoamingTitleChangeListener(this);
        this.roamingSublegends.add(legend); 
    }

    public RoamingTextTitle getRoamingTitle()
    {
        return roamingTextTitle;
    }   

    public void setRoamingTitle(RoamingTextTitle title)
    {
        if(this.roamingTextTitle != null)
        {
            this.roamingTextTitle.removeRoamingTitleChangeListener(this);
        }

        this.roamingTextTitle = title;

        if(title != null)
        {
            this.roamingTextTitle.addRoamingTitleChangeListener(this);
        }
    }   

    @Override
    public void datasetChanged(DatasetChangeEvent evt)
    {
        super.datasetChanged(evt);

        Dataset dataset = evt.getDataset();
        if(dataset instanceof ProcessableDataset<?>)
        {
            ProcessableDataset<?> processableDataset = (ProcessableDataset<?>)dataset;
            int layerIndex = getLayerIndex(processableDataset.getKey());

            if(layerIndex < 0)
            {
                return;
            }

            ValueAxis rangeAxis = getRangeAxisForDataset(layerIndex);

            Range rangeAxisRangeNew = getDataRange(rangeAxis);
            if(rangeAxisRangeNew != null)
            {
                rangeAxis.setDefaultAutoRange(rangeAxisRangeNew);
                rangeAxis.configure();
            }

            if(rangeAxis instanceof CustomizableNumberAxis)
            {
                Quantity yQuantity = processableDataset.getYQuantity();
                ((CustomizableNumberAxis) rangeAxis).setDataQuantity(yQuantity);               
            }     

            ValueAxis domainAxis = getDomainAxisForDataset(layerIndex);

            Range domainAxisRangeNew = getDataRange(domainAxis);

            if(domainAxisRangeNew != null)
            {
                domainAxis.setDefaultAutoRange(domainAxisRangeNew);
                domainAxis.configure();
            }

            if(domainAxis instanceof CustomizableNumberAxis)
            {
                Quantity xQuantity = processableDataset.getXQuantity();
                ((CustomizableNumberAxis) domainAxis).setDataQuantity(xQuantity);                
            }
        }
    }



    @Override
    public void axisChanged(AxisChangeEvent event)
    {
        Axis axis = event.getAxis();

        if(axis instanceof CustomizableNumberBaseAxis)
        {
            CustomizableNumberBaseAxis customizableAxis = (CustomizableNumberBaseAxis)axis;

            AxisLocation preferredLocation = customizableAxis.getPreferredAxisLocation();

            if(preferredLocation != null)
            {
                if(axis.equals(getDomainAxis()))
                {
                    setDomainAxisLocation(preferredLocation);
                }
                else if(axis.equals(getRangeAxis()))
                {
                    setRangeAxisLocation(preferredLocation);
                }
            }           
        }
        super.axisChanged(event);
    }

    @Override
    public CustomizableXYBasePlot clone()
    {
        CustomizableXYBasePlot clone;
        try 
        {
            clone = (CustomizableXYBasePlot) super.clone();

            //we don't clone any roaming titles (neither legend nor titles)
            //because also charts have their references 

            clone.layerMap = new LinkedHashMap<>();
            clone.roamingLegend = null;
            clone.roamingTextTitle = null;

            clone.roamingSublegends = new ArrayList<>();

            clone.generalDomainAxes = new LinkedHashMap<>();
            clone.generalRangeAxes = new LinkedHashMap<>();
            clone.layerSpecificRangeAxes = new LinkedHashMap<>();

            for(int i = 0; i < clone.getDatasetCount(); i++)
            {
                XYDataset dataset = clone.getDataset(i);
                if(dataset != null)
                {
                    dataset.removeChangeListener(clone);
                    clone.setDataset(i, null);
                }
            }


        } catch (CloneNotSupportedException e) 
        {
            e.printStackTrace();
            return null;
        }
        return clone;
    }

    public void updateFixedDataAreaSize(Graphics2D g2, Rectangle2D area)
    {
        Rectangle2D dataArea = getDataArea(g2, area);

        double availableWidth = dataArea.getWidth();
        double availableHeight = dataArea.getHeight();

        ValueAxis domainAxis = getDomainAxis();
        ValueAxis rangeAxis = getRangeAxis();

        double r = 1;

        //if axes have units, than we have to take this into account while
        //calculating the "natural" aspect ratio
        if(domainAxis instanceof CustomizableNumberAxis && rangeAxis instanceof CustomizableNumberAxis)
        {
            UnitExpression domainLength = ((CustomizableNumberAxis) domainAxis).getRangeLength();
            UnitExpression rangeLength = ((CustomizableNumberAxis) rangeAxis).getRangeLength();

            r = domainLength.getValue()/rangeLength.derive(domainLength.getUnit()).getValue();
        }
        else
        {
            double domainLength = domainAxis.getRange().getLength();
            double rangeLength = rangeAxis.getRange().getLength();

            r = domainLength/rangeLength;
        }

        double newRangeLength = Math.min(availableWidth/r, availableHeight);
        double newDomainLength = r*newRangeLength;

        Size2D areaSize = PlotOrientation.VERTICAL.equals(getOrientation()) ? new Size2D(newDomainLength, newRangeLength) : new Size2D(newRangeLength, newDomainLength);
        setFixedDataAreaSize(areaSize);
    }

    @Override
    public Preferences getPreferences() 
    {
        return prefStyle.getPreferences();
    }

    @Override
    public void drawAnnotations(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) 
    {
        ValueAxis xAxis = getDomainAxis();
        ValueAxis yAxis = getRangeAxis();
        Iterator<?> iterator = this.getAnnotations().iterator();
        while (iterator.hasNext()) 
        {
            XYAnnotation annotation = (XYAnnotation) iterator.next();   
            annotation.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
        }

        drawRoamingLegend(g2, dataArea, info);
        drawRoamingSublegends(g2, dataArea, info);
        drawRoamingTextTitle(g2, dataArea, info);
    }


    public void drawRoamingSublegends(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info)
    {                       
        for(RoamingLegend roamingLegend : roamingSublegends)
        {
            boolean visible = roamingLegend.isVisible();
            boolean inside = roamingLegend.isInside();

            if(visible && inside)
            {
                XYAnnotation roamingLegendInside = roamingLegend.getInsideTitle();

                ValueAxis xAxis = getDomainAxis();
                ValueAxis yAxis = getRangeAxis();

                roamingLegendInside.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
            }
        }   
    }

    public void drawRoamingTextTitle(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info)
    {       
        drawRoamingTitle(roamingTextTitle, g2, dataArea, info);
    }

    public void drawRoamingLegend(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info)
    {       
        drawRoamingTitle(roamingLegend, g2, dataArea, info);
    }

    protected void drawRoamingTitle(RoamingTitle roamingTitle, Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info)
    {
        if(roamingTitle != null)
        {
            boolean visible = roamingTitle.isVisible();
            boolean inside = roamingTitle.isInside();

            if(visible && inside)
            {
                XYAnnotation roamingTitleInside = roamingTitle.getInsideTitle();

                ValueAxis xAxis = getDomainAxis();
                ValueAxis yAxis = getRangeAxis();

                roamingTitleInside.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
            }
        }       
    }

    @Override
    public void roamingTitleChanged(RoamingTitleChangeEvent event)
    {
        fireChangeEvent();
    }


    private int findDatasetIndex(Comparable<?> datasetName)
    {
        int n = getDatasetCount();

        int datasetIndex = -1;

        for(int i = 0; i<n; i++)
        {
            XYDataset dataset = getDataset(i);
            if(dataset instanceof ProcessableXYDataset)
            {
                if(((ProcessableXYDataset)dataset).isNamedAs(datasetName))
                {
                    datasetIndex = i;
                }
            }
        }

        return datasetIndex;
    }

    public ChannelRenderer findRenderer(Comparable<?> key)
    {
        int datasetIndex = findDatasetIndex(key);

        boolean rendererFound = (datasetIndex >= 0);
        ChannelRenderer renderer = rendererFound ? getRenderer(datasetIndex) : null;

        return renderer;
    }

    public void attemptChangeSeriesKey(Comparable<?> seriesKey)
    {
        int datasetIndex = findDatasetIndex(seriesKey);

        if(datasetIndex < 0)
        {
            return;
        }

        XYDataset dataset = getDataset(datasetIndex);
        if(!(dataset instanceof ProcessableXYDataset))
        {
            return;
        }

        ProcessableXYDataset<?> procDataset = (ProcessableXYDataset<?>)dataset;

        if(procDataset != null)
        {
            NameChangeModel nameModel = new DatasetNameChangeModel(procDataset);
            NameChangeDialog nameDialog = new NameChangeDialog(AtomicJ.getApplicationFrame(), "Series name");
            boolean approved = nameDialog.showDialog(nameModel);
            if(approved)
            {
                Comparable<?> keyNew = nameDialog.getNewKey();

                ChannelRenderer renderer = findRenderer(seriesKey);

                procDataset.changeName(keyNew);
                renderer.setName(keyNew.toString());

                fireChangeEvent();
            }
        }           
    }


    public Paint getDefaultAnnotationPaint()
    {
        return Color.black;
    }


    //AXIS SOURCE METHODS


    //DOMAIN AXES
    @Override
    public boolean hasDomainAxes()
    {
        int axisCount = getDomainAxisCount();
        boolean hasDomainAxes = axisCount >0;

        return hasDomainAxes;
    }

    @Override
    public String getDomainAxisName(int index)
    {
        Axis axis = getDomainAxis(index);

        return getAxisName(axis);
    }

    @Override
    public Preferences getDomainAxisPreferences(int index)
    {
        Axis axis = getDomainAxis(index);
        Preferences pref =  (axis instanceof PreferencesSource) ? ((PreferencesSource) axis).getPreferences() : Preferences.userNodeForPackage(getClass()).node("DomainAxis");

        return pref;
    }

    //RANGE AXES

    @Override
    public boolean hasRangeAxes()
    {
        int axisCount = getRangeAxisCount();
        boolean hasRangeAxes = axisCount > 0;

        return hasRangeAxes;
    }

    @Override
    public String getRangeAxisName(int index)
    {
        Axis axis = getRangeAxis(index);

        return getAxisName(axis);
    }

    @Override
    public Preferences getRangeAxisPreferences(int index)
    {
        Axis axis = getRangeAxis(index);
        Preferences pref =  (axis instanceof PreferencesSource) ? ((PreferencesSource) axis).getPreferences() : Preferences.userNodeForPackage(getClass()).node("RangeAxis");

        return pref;
    }

    //DEPTH AXES
    @Override
    public boolean hasDepthAxes()
    {
        int axisCount = getDepthAxisCount();
        boolean hasAxes = axisCount>0;
        return hasAxes;
    }

    @Override
    public int getDepthAxisCount()
    {
        return 0;
    }

    @Override
    public Axis getDepthAxis(int index)
    {
        throw new IllegalArgumentException("Plot does not have a depth axis of index " + index);
    }

    @Override
    public String getDepthAxisName(int index)
    {     
        Axis axis = getDepthAxis(index);

        return getAxisName(axis);
    }

    @Override
    public Preferences getDepthAxisPreferences(int index)
    {
        Axis axis = getDepthAxis(index);
        Preferences pref =  (axis instanceof PreferencesSource) ? ((PreferencesSource) axis).getPreferences() : Preferences.userNodeForPackage(getClass()).node("DepthAxis");
        return pref;
    }

    private String getAxisName(Axis axis)
    {
        String axisName = (axis instanceof CustomizableNumberAxis) ? ((CustomizableNumberAxis) axis).getName() : axis.getLabel();
        return axisName;
    }

    public Point2D getDataPoint(Point2D java2Dpoint, PlotRenderingInfo info)
    {  
        Point2D point = new Point2D.Double(getDomainValue(java2Dpoint, info, 0), getRangeValue(java2Dpoint, info, 0));
        return point;
    }   

    public UnitPoint2D getDataPointWithUnits(Point2D java2Dpoint, PlotRenderingInfo info)
    {  
        UnitPoint2D point = new UnitPoint2D(getDomainUnitExpression(java2Dpoint, info, 0), getRangeUnitExpression(java2Dpoint, info, 0));
        return point;
    }  

    public Point2D getDataPoint(Point2D java2DPoint, PlotRenderingInfo info, int domainAxisIndex, int rangeAxisIndex)
    {
        Point2D point = new Point2D.Double(getDomainValue(java2DPoint, info, domainAxisIndex),
                getRangeValue(java2DPoint, info, rangeAxisIndex));
        return point;
    }

    public void setRangeOfDomainAxisForDatasetIfPossible(Object key, double rangeMin, double rangeMax)
    {
        Validation.requireFirstValueSmallerThanSecond(rangeMin, rangeMax, "rangeMin", "rangeMax");

        Integer index = layerMap.get(key);
        if(index != null)
        {
            ValueAxis axis = getDomainAxisForDataset(index);
            axis.setRange(rangeMin, rangeMax);
        }
    }


    public void setRangeOfRangeAxisForDatasetIfPossible(Object key, double rangeMin, double rangeMax)
    {
        Validation.requireFirstValueSmallerThanSecond(rangeMin, rangeMax, "rangeMin", "rangeMax");

        Integer index = layerMap.get(key);
        if(index != null)
        {
            ValueAxis axis = getRangeAxisForDataset(index);
            axis.setRange(rangeMin, rangeMax);
        }
    }

    public Point2D getDataPointForDataset(Point2D java2DPoint, PlotRenderingInfo info, int datasetIndex)
    {
        ValueAxis domainAxis = getDomainAxisForDataset(datasetIndex);
        ValueAxis rangeAxis = getRangeAxisForDataset(datasetIndex);

        int domainAxisIndex = getDomainAxisIndex(domainAxis);
        int rangeAxisIndex = getRangeAxisIndex(rangeAxis);

        Point2D point = new Point2D.Double(getDomainValue(java2DPoint, info, domainAxisIndex),
                getRangeValue(java2DPoint, info, rangeAxisIndex));
        return point;
    }

    public double getDomainValue(Point2D java2Dpoint, PlotRenderingInfo info, int axisIndex)
    {  
        Rectangle2D dataArea = info.getDataArea();                  
        ValueAxis domainAxis = getDomainAxis(axisIndex);
        RectangleEdge domainAxisEdge = getDomainAxisEdge(axisIndex);

        PlotOrientation orientation = getOrientation();
        boolean isVertical = (orientation == PlotOrientation.VERTICAL);

        double dataX = isVertical ? domainAxis.java2DToValue(java2Dpoint.getX(), dataArea, domainAxisEdge)
                :domainAxis.java2DToValue(java2Dpoint.getY(), dataArea, domainAxisEdge);

        return dataX;
    }    

    public UnitExpression getDomainUnitExpression(Point2D java2Dpoint, PlotRenderingInfo info, int axisIndex)
    {
        ValueAxis domainAxis = getDomainAxis(axisIndex);
        PrefixedUnit unit = (domainAxis instanceof CustomizableNumberAxis) ? ((CustomizableNumberAxis)domainAxis).getDataUnit() : SimplePrefixedUnit.getNullInstance();

        UnitExpression unitExpression = new UnitExpression(getDomainValue(java2Dpoint, info, axisIndex), unit);
        return unitExpression;
    }

    public double getRangeValue(Point2D java2Dpoint, PlotRenderingInfo info, int axisIndex)
    {  
        Rectangle2D dataArea = info.getDataArea();                  
        ValueAxis rangeAxis = getRangeAxis(axisIndex);

        PlotOrientation orientation = getOrientation();
        boolean isVertical = (orientation == PlotOrientation.VERTICAL);

        RectangleEdge rangeAxisEdge = getRangeAxisEdge(axisIndex);
        double dataY = isVertical ? rangeAxis.java2DToValue(java2Dpoint.getY(), dataArea, rangeAxisEdge)
                : rangeAxis.java2DToValue(java2Dpoint.getX(), dataArea, rangeAxisEdge);

        return dataY;
    }   

    public UnitExpression getRangeUnitExpression(Point2D java2Dpoint, PlotRenderingInfo info, int axisIndex)
    {  
        ValueAxis rangeAxis = getRangeAxis(axisIndex);
        PrefixedUnit unit = (rangeAxis instanceof CustomizableNumberAxis) ? ((CustomizableNumberAxis)rangeAxis).getDataUnit() : SimplePrefixedUnit.getNullInstance();

        UnitExpression unitExpression = new UnitExpression(getRangeValue(java2Dpoint, info, axisIndex), unit);
        return unitExpression;
    }   

    //DOMAIN UNITS 

    public PrefixedUnit getDomainDataUnit(int index)
    {
        ValueAxis axis = getDomainAxis(index);       
        return getDataUnit(axis);
    }

    public PrefixedUnit getDomainDataUnit()
    {
        ValueAxis axis = getDomainAxis();       
        return getDataUnit(axis);
    }

    public PrefixedUnit getDomainDisplayedUnit(int index)
    {
        ValueAxis axis = getDomainAxis(index);      
        return getDisplayedUnit(axis);
    }

    public PrefixedUnit getDomainDisplayedUnit()
    {
        ValueAxis axis = getDomainAxis();      
        return getDisplayedUnit(axis);
    }

    //DOMAIN QUANTITIES

    public Quantity getDomainDataQuantity(int index)
    {
        ValueAxis axis = getDomainAxis(index);       
        return getDataQuantity(axis);
    }

    public Quantity getDomainDataQuantity()
    {
        ValueAxis axis = getDomainAxis();       
        return getDataQuantity(axis);
    }

    public Quantity getDomainDisplayedQuantity(int index)
    {
        ValueAxis axis = getDomainAxis(index);      
        return getDisplayedQuantity(axis);
    }

    public Quantity getDomainDisplayedQuantity()
    {
        ValueAxis axis = getDomainAxis();      
        return getDisplayedQuantity(axis);
    }

    // RANGE UNITS

    public PrefixedUnit getRangeDataUnit(int index)
    {
        ValueAxis axis = getRangeAxis(index);     
        return getDataUnit(axis);
    }

    public PrefixedUnit getRangeDataUnit()
    {
        ValueAxis axis = getRangeAxis();     
        return getDataUnit(axis);
    }


    public PrefixedUnit getRangeDisplayedUnit(int index)
    {
        ValueAxis axis = getRangeAxis(index);      
        return getDisplayedUnit(axis);
    }

    public PrefixedUnit getRangeDisplayedUnit()
    {
        ValueAxis axis = getRangeAxis();      
        return getDisplayedUnit(axis);
    }

    //RANGE QUANTITIES

    public Quantity getRangeDataQuantity(int index)
    {
        ValueAxis axis = getRangeAxis(index);     
        return getDataQuantity(axis);
    }

    public Quantity getRangeDataQuantity()
    {
        ValueAxis axis = getRangeAxis();     
        return getDataQuantity(axis);
    }

    public Quantity getRangeDisplayedQuantity(int index)
    {
        ValueAxis axis = getRangeAxis(index);      
        return getDisplayedQuantity(axis);
    }

    public Quantity getRangeDisplayedQuantity()
    {
        ValueAxis axis = getRangeAxis();      
        return getDisplayedQuantity(axis);
    }


    private static PrefixedUnit getDisplayedUnit(ValueAxis axis)
    {
        PrefixedUnit unit = SimplePrefixedUnit.getNullInstance();
        if(axis instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis customizableAxis = (CustomizableNumberAxis) axis;
            unit = customizableAxis.getDisplayedUnit();
        }

        return unit;
    }

    private static PrefixedUnit getDataUnit(ValueAxis axis)
    {
        PrefixedUnit unit = SimplePrefixedUnit.getNullInstance();
        if(axis instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis customizableAxis = (CustomizableNumberAxis) axis;
            unit = customizableAxis.getDataUnit();
        }

        return unit;
    }

    private static Quantity getDisplayedQuantity(ValueAxis axis)
    {
        Quantity quantity = DimensionlessQuantity.getNullInstance();
        if(axis instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis customizableAxis = (CustomizableNumberAxis) axis;
            quantity = customizableAxis.getDisplayedQuantity();
        }

        return quantity;
    }


    private static Quantity getDataQuantity(ValueAxis axis)
    {
        Quantity quantity = DimensionlessQuantity.getNullInstance();
        if(axis instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis customizableAxis = (CustomizableNumberAxis) axis;
            quantity = customizableAxis.getDataQuantity();
        }

        return quantity;
    }

    public UnitExpression getDomainAxisUnitExpression(Point2D java2Dpoint, PlotRenderingInfo info, int index)
    {  
        Rectangle2D dataArea = info.getDataArea();                  
        ValueAxis domainAxis = getDomainAxis(index);
        RectangleEdge domainAxisEdge = getDomainAxisEdge(index);

        PlotOrientation orientation = getOrientation();
        boolean isVertical = (orientation == PlotOrientation.VERTICAL);

        UnitExpression expr;
        if(domainAxis instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis customizableAxis = (CustomizableNumberAxis) domainAxis;
            expr = isVertical ? customizableAxis.java2DToAxisUnitExpression(java2Dpoint.getX(), dataArea, domainAxisEdge)
                    :customizableAxis.java2DToAxisUnitExpression(java2Dpoint.getY(), dataArea, domainAxisEdge);

        }
        else
        {                    
            double dataX = isVertical ? domainAxis.java2DToValue(java2Dpoint.getX(), dataArea, domainAxisEdge)
                    :domainAxis.java2DToValue(java2Dpoint.getY(), dataArea, domainAxisEdge);
            expr = new UnitExpression(dataX, SimplePrefixedUnit.getNullInstance());
        }

        return expr;
    }    

    public UnitExpression getRangeAxisUnitExpression(Point2D java2Dpoint, PlotRenderingInfo info, int index)
    {  
        Rectangle2D dataArea = info.getDataArea();                  
        ValueAxis rangeAxis = getRangeAxis(index);

        PlotOrientation orientation = getOrientation();
        boolean isVertical = (orientation == PlotOrientation.VERTICAL);

        RectangleEdge rangeAxisEdge = getRangeAxisEdge(index);

        UnitExpression expr;

        if(rangeAxis instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis customizableAxis = (CustomizableNumberAxis) rangeAxis;
            expr = isVertical ? customizableAxis.java2DToAxisUnitExpression(java2Dpoint.getY(), dataArea, rangeAxisEdge)
                    : customizableAxis.java2DToAxisUnitExpression(java2Dpoint.getX(), dataArea, rangeAxisEdge);
        }
        else
        {
            double dataY = isVertical ? rangeAxis.java2DToValue(java2Dpoint.getY(), dataArea, rangeAxisEdge)
                    : rangeAxis.java2DToValue(java2Dpoint.getX(), dataArea, rangeAxisEdge);
            expr = new UnitExpression(dataY, SimplePrefixedUnit.getNullInstance());

        }

        return expr;
    }   


    public void attemptChangeCategoryLabel(final String category, final String label, final CustomizableNamedNumberAxis axis)
    {
        NameChangeDialog nameDialog = new NameChangeDialog(AtomicJ.getApplicationFrame(), "Sample name");
        boolean approved = nameDialog.showDialog(new NameChangeModel() 
        {

            @Override
            public void setName(Comparable<?> keyNew) 
            {
                axis.putUserSelectedCategoryLabel(category, keyNew.toString());               
            }

            @Override
            public Comparable<?> getName() 
            {
                return label;
            }
        });
        if(approved)
        {
            Comparable<?> keyNew = nameDialog.getNewKey();

            axis.putUserSelectedCategoryLabel(category, keyNew.toString());               
            fireChangeEvent();
        }          
    }

    public double[][] getSeriesDataCopy(Comparable<?> key)
    {
        double[][] seriesData = new double[][] {};

        int datasetCount = getDatasetCount();
        for(int i = 0; i<datasetCount; i++)
        {
            XYDataset dataset = getDataset(i);
            int seriesIndex = dataset.indexOf(key);
            if(seriesIndex > -1)
            {
                if(dataset instanceof ProcessableDataset)
                {
                    return ((ProcessableDataset) dataset).getDataCopy(seriesIndex);
                }
                else
                {
                    return getDatasetData(dataset, seriesIndex);
                }
            }
        }
        return seriesData;
    }

    private static double[][] getDatasetData(XYDataset dataset, int seriesIndex)
    {
        int itemCount = dataset.getItemCount(seriesIndex);
        double[][] data = new double[itemCount][];

        for(int i = 0; i<itemCount; i++)
        {
            data[i][0] = dataset.getXValue(seriesIndex, i);
            data[i][1] = dataset.getYValue(seriesIndex, i);
        }

        return data;
    }

    public void resetData(Channel1D channel)
    {   
        Object channelKey = channel.getIdentifier();
        int datasetIndex =  getLayerIndex(channelKey);

        XYDataset dataset = getDataset(datasetIndex);
        if(dataset instanceof ProcessableXYDataset)
        {
            ((ProcessableXYDataset) dataset).setData(0, channel);
        }
    }

    public void resetData(Collection<? extends Channel1D> channels)
    {               
        for(Channel1D channel : channels)
        {      
            int datasetIndex =  getLayerIndex(channel.getIdentifier());

            XYDataset dataset = getDataset(datasetIndex);
            if(dataset instanceof ProcessableXYDataset)
            {
                ((ProcessableXYDataset) dataset).setData(0, channel);
            }
        }
    }

    public void setDataTo(Collection<? extends Channel1D> channels)
    {   
        List<Object> layersToClear = new ArrayList<>(layerMap.keySet());

        for(Channel1D channel : channels)
        {      
            Object channelKey = channel.getIdentifier();
            int datasetIndex =  getLayerIndex(channelKey);
            layersToClear.remove(channelKey);

            XYDataset dataset = getDataset(datasetIndex);
            if(dataset instanceof ProcessableXYDataset)
            {
                ((ProcessableXYDataset) dataset).setData(0, channel);
            }
        }

        for(Object layerToClear : layersToClear)
        {
            clearLayerData(layerToClear, false); 
        }
    }


    public void notifyOfDataChange(Object key)
    {       
        int index = getLayerIndex(key);

        if(index >= 0)
        {
            XYDataset dataset = getDataset(index);

            if(dataset instanceof ProcessableDataset)
            {
                ProcessableDataset<?> processableDataset = (ProcessableDataset<?>)dataset;
                processableDataset.notifyOfDataChange(true);
            }
        }       
    }
}
