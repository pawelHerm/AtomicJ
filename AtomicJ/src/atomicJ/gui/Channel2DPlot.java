
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

/*
 * The methods draw() are  modifications of a corresponding method from XYPlot class, part of JFreeChart library, copyright by Object Refiner Limited and other contributors.
 * The source code of XYPlot can be found through the API doc at the page http://www.jfree.org/jfreechart/api/javadoc/index.html 
 */

package atomicJ.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.axis.ValueTick;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.profile.CrossSectionsRenderer;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.stack.StackModel;

public class Channel2DPlot extends CustomizableXYPlot implements Cloneable, PublicCloneable, TooltipManagerSource
{
    private static final long serialVersionUID = 1L;

    private EntityCollection lastSavedEntities = new StandardEntityCollection();

    private BufferedImage bufferedImage;
    private boolean refresh = true;
    private boolean useDataImageBuffer = true;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final PropertyChangeListener listener = new LocalPropertyChangeListener();

    ////////////////////////////////  TOOLTIPS  //////////////////////////////////////////

    private final Map<Integer, TooltipStyleManager> domainTooltipStyleManagers = new LinkedHashMap<>();
    private final Map<Integer, TooltipStyleManager> rangeTooltipStyleManagers = new LinkedHashMap<>();

    //////////////////////////////////////////////////////////////////////////////////////4


    private Map<Object, Profile> profiles = new LinkedHashMap<>();
    private Map<Object, ROIDrawable> rois = new LinkedHashMap<>();
    private Map<Object, MapMarker> mapMarkers = new LinkedHashMap<>();

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! MULTIPLE DATASET PLOT

    private MovieProcessableDataset movieDataset;
    private final Map<String, MovieDataset> movieXYdatasets = new LinkedHashMap<>();

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    public Channel2DPlot(ProcessableXYZDataset dataset, Channel2DRenderer renderer,String plotStyleKey, Preferences pref) 
    {
        super(pref, plotStyleKey);

        if(dataset instanceof MovieProcessableDataset)
        {
            this.movieDataset = (MovieProcessableDataset)dataset;
        }

        addOrReplaceLayer(dataset.getKey(), dataset, renderer);

        setUseFixedDataAreaSize(true);
        setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        setGridlinesOnTop(true);       
    }		

    @Override
    protected CustomizableNumberAxis buildNewRangeAxis(Preferences pref, Quantity quantity)
    {
        CustomizableNumberAxis rangeAxis = new CustomizableNumberAxis(quantity, pref, true);
        rangeAxis.setRestoreDefaultAutoRange(true);
        rangeAxis.setAutoRangeIncludesZero(false);

        return rangeAxis;
    }

    @Override
    protected CustomizableNumberAxis buildNewDomainAxis(Preferences pref, Quantity quantity)
    {
        CustomizableNumberAxis domainAxis = new CustomizableNumberAxis(quantity, pref, true);
        domainAxis.setRestoreDefaultAutoRange(true);
        domainAxis.setAutoRangeIncludesZero(false);

        return domainAxis;
    }

    public boolean areROISamplesNeeded()
    {
        GradientPaintReceiver receiver = getGradientPaintReceiver();

        boolean needed = (receiver != null) ? receiver.areROISamplesNeeded() : false;

        return needed;
    }

    public GradientPaintReceiver getGradientPaintReceiver()
    {
        GradientPaintReceiver receiver = null;

        ChannelRenderer renderer = getRenderer(0);
        if(renderer instanceof GradientPaintReceiver)
        {
            receiver = (GradientPaintReceiver)renderer; 
        }       

        return receiver;
    }

    //// TOOLTIPS 

    @Override
    public void setDomainAxis(int index, ValueAxis axis, boolean notify)
    {
        super.setDomainAxis(index, axis, notify);

        String name = index == 0 ? "Domain" : "Domain " + index;

        Preferences pref = getPreferences().node("DomainTooltip");

        domainTooltipStyleManagers.put(index, new TooltipStyleManager(name, " X: ", pref, false));
    }

    //for reason unknown to me, setRangeAxis(ValueAxis) does not call setRangeAxis(index, ValueAxis),
    //while setDomainAxis(ValueAxis) calls it
    @Override
    public void setRangeAxis(ValueAxis axis)
    {
        super.setRangeAxis(axis);

        Preferences pref = getPreferences().node("RangeTooltip");       
        rangeTooltipStyleManagers.put(0, new TooltipStyleManager("Range", " Y: ", pref, false));
    }

    @Override
    public void setRangeAxis(int index, ValueAxis axis, boolean notify)
    {
        super.setRangeAxis(index, axis, notify);

        String name = index == 0 ? "Range" : "Range " + index;

        Preferences pref = getPreferences().node("RangeTooltip");

        rangeTooltipStyleManagers.put(index, new TooltipStyleManager(name, " Y: ", pref, false));
    }

    //!!!!!!!!!!!!!!!!!!!!!!!!!

    public boolean isMovie()
    {
        boolean movie = (movieDataset != null);
        return movie;
    }

    public int getFrameCount()
    {
        int frameCount = 0;

        if(movieDataset != null)
        {
            frameCount = movieDataset.getFrameCount();
        }

        return frameCount;
    }

    public void showNextFrame()
    {
        if(movieDataset != null)
        {
            movieDataset.showNextFrame();
        }


        for(MovieDataset dataset : movieXYdatasets.values())
        {
            dataset.showNextFrame();
        }
    }

    public void showPreviousFrame()
    {
        if(movieDataset != null)
        {
            movieDataset.showPreviousFrame();
        }

        for(MovieDataset dataset : movieXYdatasets.values())
        {
            dataset.showPreviousFrame();
        }
    }

    public void showFrame(int index)
    {
        if(movieDataset != null)
        {
            movieDataset.showFrame(index);
        }     

        for(MovieDataset dataset : movieXYdatasets.values())
        {
            dataset.showFrame(index);
        }
    }

    public void addOrReplaceMovieDataset(String key, MovieDataset dataset, Quantity quantity)
    {
        movieXYdatasets.put(key, dataset);

        if(containsLayer(key))
        {
            replaceLayerDataset(key, dataset);
        }
        else
        {
            StyleTag styleTag = new StandardStyleTag(key);
            Preferences pref = Preferences.userNodeForPackage(Channel1DRenderer.class).node(styleTag.getPreferredStyleKey());
            PreferredContinuousSeriesRendererStyle prefStyle = PreferredContinuousSeriesRendererStyle.getInstance(pref, styleTag);
            CrossSectionsRenderer renderer = new CrossSectionsRenderer(prefStyle, key, styleTag, key);
            addOrReplaceLayerWithOwnAxis(key, dataset, renderer, quantity);
        }
    }

    public void removeMovieDataset(String key)
    {
        MovieDataset dataset = movieXYdatasets.remove(key);

        if(dataset != null)
        {
            dataset.removeChangeListener(this);
        }
        removeLayer(key, true);
    }

    public void setMainMovieDataset(MovieProcessableDataset movieDatasetNew)
    {
        if(movieDataset != null)
        {
            movieDataset.removeChangeListener(this);
        }

        movieDatasetNew.addChangeListener(this);	
        movieDatasetNew.showFrame(movieDataset.getCurrentFrameIndex());
        movieDataset = movieDatasetNew;
    }

    public void cleanUp()
    {
        for(int i = 0; i<getDatasetCount(); i++)
        {
            setDataset(i, null);
        }
        if(movieDataset != null)
        {
            movieDataset.removeChangeListener(this);

            for(MovieDataset dataset : movieXYdatasets.values())
            {
                dataset.removeChangeListener(this);
            }
        }
        movieDataset = null;
        movieXYdatasets.clear();
    }

    public String getRegularDatasetTooltip(Point2D java2DPoint, PlotRenderingInfo info)
    {
        List<String> tooltips = new ArrayList<>();

        tooltips.add(getDomainAxesTooltips(java2DPoint, info));
        tooltips.add(getRangeAxesTooltips(java2DPoint, info));

        int layerCount = getLayerCount();               

        for(int i = 0; i<layerCount; i++)
        {    
            ChannelRenderer renderer = getRenderer(i);
            if(!(renderer instanceof Channel2DRenderer))
            {
                continue;
            }

            Channel2DRenderer renderer2D = (Channel2DRenderer) renderer;


            XYDataset dataset = getDataset(i);

            TooltipStyleManager manager = renderer2D.getTooltipStyleManager();
            boolean processable = dataset instanceof ProcessableXYZDataset;

            if(manager.isVisible() && processable)
            {                
                Point2D dataPoint = getDataPointForDataset(java2DPoint, info, i);
                UnitExpression toolTipExpression = ((ProcessableXYZDataset) dataset).getToolTipExpression(dataPoint);

                if(toolTipExpression != null)
                {                                      
                    PrefixedUnit unit = renderer2D.getDisplayedUnit();
                    String text = (unit != null) ? manager.getTooltip(toolTipExpression.derive(unit)): manager.getTooltip(toolTipExpression);

                    tooltips.add(text);
                }           
            }  
        }


        return concatenateTooltips(tooltips);
    }

    public List<String> getValueLabelTypes()
    {
        List<String> valueLabelTypes = new ArrayList<>();

        int layerCount = getLayerCount();               

        for(int i = 0; i<layerCount; i++)
        {
            XYDataset dataset = getDataset(i);
            ChannelRenderer renderer = getRenderer(i);
            if(!(renderer instanceof Channel2DRenderer))
            {
                continue;
            }

            boolean processable = dataset instanceof ProcessableXYZDataset;
            ProcessableXYZDataset processableDataset = (ProcessableXYZDataset)dataset;

            if(processable)
            {
                valueLabelTypes.add(processableDataset.getKey().toString());
            }
        }

        return valueLabelTypes;
    }

    //should be getValues(Point2D java2DPoint), because dataset can use different axes
    //now the density datasets use the same, but it may change in the future

    public Map<String, String> getValueLabels(Point2D dataPoint)
    {
        Map<String, String> valueLabels = new LinkedHashMap<>();

        int layerCount = getLayerCount();               

        for(int i = 0; i<layerCount; i++)
        {
            XYDataset dataset = getDataset(i);
            ChannelRenderer renderer = getRenderer(i);
            if(!(renderer instanceof Channel2DRenderer))
            {
                continue;
            }

            Channel2DRenderer renderer2D = (Channel2DRenderer) renderer;

            boolean processable = dataset instanceof ProcessableXYZDataset;
            ProcessableXYZDataset processableDataset = (ProcessableXYZDataset)dataset;

            if(processable)
            {
                UnitExpression toolTipExpression = processableDataset.getToolTipExpression(dataPoint);

                if(toolTipExpression == null)
                {
                    continue;
                }

                PrefixedUnit unit = renderer2D.getDisplayedUnit();

                String label = (unit != null) ? toolTipExpression.derive(unit).toString(NumberFormat.getInstance(Locale.US))
                        :toolTipExpression.toString(NumberFormat.getInstance(Locale.US));

                valueLabels.put(processableDataset.getKey().toString(), label);
            }
        }

        return valueLabels;
    }

    public PrefixedUnit getZAxisDisplayedUnit()
    {
        PrefixedUnit unit = null;

        ChannelRenderer renderer = getRenderer();
        if(renderer instanceof Channel2DRenderer)
        {
            unit = ((Channel2DRenderer)renderer).getDisplayedUnit();
        }
        return unit;
    }

    private String concatenateTooltips(List<String> tooltips)
    {
        StringBuffer tooltipText = new StringBuffer();

        for(String t : tooltips)
        {
            tooltipText = tooltipText.append(t);
        }

        String concatenated = tooltipText.toString();
        String tooltip = concatenated.isEmpty() ? null : concatenated;

        return tooltip;
    }

    private String getDomainAxesTooltips(Point2D java2DPoint, PlotRenderingInfo info)
    {
        String tooltipText = "";

        int domainAxisCount = getDomainAxisCount();

        for(int i = 0; i<domainAxisCount; i++)
        {
            TooltipStyleManager manager = getDomainTooltipManager(i);

            tooltipText += manager.getTooltip(getDomainAxisUnitExpression(java2DPoint, info, i));
        }

        return tooltipText;
    }

    private String getRangeAxesTooltips(Point2D java2DPoint, PlotRenderingInfo info)
    {
        String tooltipText = "";

        int rangeAxisCount = getRangeAxisCount();

        for(int i = 0; i<rangeAxisCount; i++)
        {
            TooltipStyleManager manager = getRangeTooltipManager(i);
            tooltipText += manager.getTooltip(getRangeAxisUnitExpression(java2DPoint, info, i));
        }

        return tooltipText;
    }

    public String getDatasetTooltip(Point2D java2DPoint, LightweightXYItemEntity entity, PlotRenderingInfo plotInfo)
    {
        String regularDatasetTooltips = getRegularDatasetTooltip(java2DPoint, plotInfo);

        String tooltipText = (regularDatasetTooltips != null) ? regularDatasetTooltips: "";

        String tooltip = tooltipText.isEmpty() ? null : tooltipText;

        return tooltip;
    }

    @Override
    public void setDataset(int index, XYDataset dataset)
    {	
        //Concomittant renderers renderer raster data, and only for plotting this type of data it is reasonable
        //to set the range inside this method

        XYItemRenderer renderer = getRenderer(index);
        if(renderer instanceof Channel2DRenderer && dataset != null)
        {                
            Channel2DRenderer renderer2D = (Channel2DRenderer)renderer;
            renderer2D.fitAndRegisterDataset(dataset);

            fitAxesToDataset(index, dataset, renderer2D);
        }

        refresh = true;
        super.setDataset(index, dataset);
    }

    private void fitAxesToDataset(int index, XYDataset dataset, Channel2DRenderer renderer2D)
    {
        ValueAxis domainAxis = getDomainAxisForDataset(index);
        ValueAxis rangeAxis = getRangeAxisForDataset(index);

        boolean axesOk = domainAxis instanceof CustomizableNumberAxis && rangeAxis instanceof CustomizableNumberAxis;

        if(!axesOk)
        {
            return;
        }

        CustomizableNumberAxis domainCustomizableAxis = (CustomizableNumberAxis) domainAxis;
        CustomizableNumberAxis rangeCustomizableAxis = (CustomizableNumberAxis) rangeAxis;

        Range finalDomainBounds =  renderer2D.findDomainBounds(dataset);
        Range finalRangeBounds = renderer2D.findRangeBounds(dataset);

        domainCustomizableAxis.setRange(finalDomainBounds);
        rangeCustomizableAxis.setRange(finalRangeBounds);       

        domainCustomizableAxis.setDefaultAutoRange(finalDomainBounds);
        rangeCustomizableAxis.setDefaultAutoRange(finalRangeBounds);      

        rangeCustomizableAxis.setAutoRange(true);
        domainCustomizableAxis.setAutoRange(true);
    }

    public PrefixedUnit getDomainPreferredUnit()
    {
        XYDataset dataset = getDataset();
        PrefixedUnit unit = SimplePrefixedUnit.getNullInstance();
        Range domainBounds;
        if(dataset instanceof ProcessableXYZDataset)
        {
            domainBounds = ((ProcessableXYZDataset) dataset).getXRange();
            unit = ((ProcessableXYZDataset) dataset).getXQuantity().getUnit();
        }
        else
        {
            domainBounds = DatasetUtilities.findDomainBounds(dataset, false);
        }

        double step = domainBounds.getLength();

        PrefixedUnit preferredUnit = unit.getPreferredCompatibleUnit(step);      

        return preferredUnit;
    }

    public PrefixedUnit getRangePreferredUnit()
    {
        XYDataset dataset = getDataset();
        PrefixedUnit unit = SimplePrefixedUnit.getNullInstance();
        Range rangeBounds;
        if(dataset instanceof ProcessableXYZDataset)
        {
            rangeBounds = ((ProcessableXYZDataset) dataset).getYRange();
            unit = ((ProcessableXYZDataset) dataset).getYQuantity().getUnit();
        }
        else
        {
            rangeBounds = DatasetUtilities.findRangeBounds(dataset, false);
        }

        double step = rangeBounds.getLength();

        PrefixedUnit preferredUnit = unit.getPreferredCompatibleUnit(step);      

        return preferredUnit;
    }

    public Quantity getDepthQuantity()
    {
        Quantity depthQuantity = null;

        XYDataset dataset = getDataset() ;
        if(dataset instanceof ProcessableXYZDataset)
        {
            depthQuantity = ((ProcessableXYZDataset)dataset).getZQuantity();
        }
        return  depthQuantity;
    }

    @Override
    public void setOrientation(PlotOrientation orientation)
    {
        refresh = true;
        super.setOrientation(orientation);
    }

    public void addOrReplaceLayer(ProcessableXYZDataset dataset, Channel2DRenderer renderer)
    {
        addOrReplaceLayer(renderer.getStyleKey(), dataset, renderer);
    }

    public void addOrReplaceLayer(Object key, ProcessableXYZDataset dataset, Channel2DRenderer renderer)
    {

        super.addOrReplaceLayer(key,  dataset, renderer);
        refresh = true;
    }

    @Override
    public void replaceLayerDataset(Object key, XYDataset dataset)
    {
        super.replaceLayerDataset(key, dataset);
        refresh = true;
    }

    protected void insertLayer(ProcessableXYZDataset dataset, Channel2DRenderer renderer, int index)
    {
        super.insertLayer(dataset.getKey(), index, dataset, renderer);

        int activeLayerOld = getActiveLayerIndex();
        int activeLayerNew = index <= activeLayerOld ? activeLayerOld + 1 : activeLayerOld;

        setActiveLayerIndex(activeLayerNew);

        refresh = true; 
    }

    public Area getDatasetArea()
    {
        Area allDatasetsArea = new Area();

        int n = getDatasetCount();

        for(int i = 0; i<n; i++)
        {
            XYDataset dataset = getDataset(i);

            if(dataset instanceof ProcessableXYZDataset)
            {
                Shape currentDatasetArea = ((ProcessableXYZDataset) dataset).getDataArea();

                allDatasetsArea.add(new Area(currentDatasetArea));
            }
        }

        return allDatasetsArea;
    }

    /////////////////////////ROIS///////////////////////////////////////////

    public void removeROI(ROIDrawable roi)
    {
        removeROI(roi, true);
    }

    public void removeROI(ROIDrawable roi, boolean notify)
    {
        roi.removeChangeListener(this);
        rois.remove(roi.getKey());

        if(notify)
        {
            fireChangeEvent();
        }
    }

    public void addOrReplaceROI(ROIDrawable roi)
    {
        addOrReplaceROI(roi, true);
    }

    public void addOrReplaceROI(ROIDrawable roi, boolean notify)
    {        
        ROIDrawable previousROI = rois.get(roi.getKey());
        if(previousROI != null)
        {
            previousROI.removeChangeListener(this);
        }

        roi.addChangeListener(this);
        rois.put(roi.getKey(), roi);

        if(notify)
        {
            fireChangeEvent();
        }
    }

    ////////////////////PROFILES///////////////////////////////////

    public void removeProfile(Profile profile)
    {
        removeProfile(profile, true);
    }

    public void removeProfile(Profile profile, boolean notify)
    {
        profile.removeChangeListener(this);
        profiles.remove(profile.getKey());

        if(notify)
        {
            fireChangeEvent();
        }
    }

    public void addOrReplaceProfile(Profile profile)
    {
        addOrReplaceProfile(profile, true);
    }

    public void addOrReplaceProfile(Profile profile, boolean notify)
    {
        Profile previousProfile = profiles.get(profile.getKey());
        if(previousProfile != null)
        {
            previousProfile.removeChangeListener(this);
        }

        profile.addChangeListener(this);
        profiles.put(profile.getKey(), profile);		

        if(notify)
        {
            fireChangeEvent();
        }
    }
    ////////////////////MAP MARKERS///////////////////////////////////

    public void removeMapMarker(MapMarker mapMarker)
    {
        removeMapMarker(mapMarker, true);
    }

    public void removeMapMarker(MapMarker mapMarker, boolean notify)
    {
        mapMarker.removeChangeListener(this);
        mapMarkers.remove(mapMarker.getKey());

        if(notify)
        {
            fireChangeEvent();
        }
    }

    public void addOrReplaceMapMarker(MapMarker mapMarker)
    {
        addOrReplaceMapMarker(mapMarker, true);
    }

    public void addOrReplaceMapMarker(MapMarker mapMarker, boolean notify)
    {
        MapMarker previousMapMarker = mapMarkers.get(mapMarker.getKey());
        if(previousMapMarker != null)
        {
            previousMapMarker.removeChangeListener(this);
        }

        mapMarker.addChangeListener(this);
        mapMarkers.put(mapMarker.getKey(), mapMarker);        

        if(notify)
        {
            fireChangeEvent();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    //code of this method comes from XYPlot class, part of JFreeChart project, which was released under LGPL license. Original author - David Gilbert.
    //I changed it so that now all items are drawn at once - instead of drawItem(), I called drawItems() method, defined in my ConcomittantRenderer interface. This increases speed of rendering
    @Override
    public boolean render(Graphics2D g2, Rectangle2D dataArea, int index,
            PlotRenderingInfo info, CrosshairState crosshairState) 
    {		

        XYDataset xyDataset = getDataset(index);

        if(xyDataset instanceof XYZDataset)
        {
            XYZDataset dataset = (XYZDataset) xyDataset;
            if(dataset instanceof Movie2DDataset)
            {	            
                dataset = ((Movie2DDataset)dataset).getCurrentFrame();
            }

            return render(g2, dataArea, index, dataset, info, crosshairState);
        }
        else
        {
            g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);

            boolean b = super.render(g2, dataArea, index, info, crosshairState);

            return b;
        }
    }

    public boolean render(Graphics2D g2, Rectangle2D dataArea, int index, XYZDataset dataset,
            PlotRenderingInfo info, CrosshairState crosshairState) 
    {		        
        boolean foundData = false;        

        if (!DatasetUtilities.isEmptyOrNull(dataset)) 
        {        	
            foundData = true;
            ValueAxis xAxis = getDomainAxisForDataset(index);
            ValueAxis yAxis = getRangeAxisForDataset(index);

            if (xAxis == null || yAxis == null) 
            {
                return foundData;  // can't render anything without axes
            }
            Channel2DRenderer renderer = (Channel2DRenderer) getRenderer(index); // I cast renderer to my type ConcomittantRenderer, which allows drawing all items at once (PH)


            if (renderer == null) 
            {
                renderer = (Channel2DRenderer) getRenderer();
                if (renderer == null) 
                { 
                    // no default renderer available
                    return foundData;
                }
            }

            XYItemRendererState state = renderer.initialise(g2, dataArea, this, dataset, info);
            int passCount = renderer.getPassCount();

            lastSavedEntities = new StandardEntityCollection();

            SeriesRenderingOrder seriesOrder = getSeriesRenderingOrder();
            if (seriesOrder == SeriesRenderingOrder.REVERSE) 
            {
                //render series in reverse order
                for (int pass = 0; pass < passCount; pass++) 
                {
                    int seriesCount = dataset.getSeriesCount();
                    for (int series = seriesCount - 1; series >= 0; series--) 
                    {
                        EntityCollection ents = renderer.drawItems(g2, state, dataArea, info,
                                this, xAxis, yAxis, dataset, series, 
                                crosshairState, pass);
                        lastSavedEntities.addAll(ents);                        
                    }
                }
            }
            else 
            {
                //render series in forward order
                for (int pass = 0; pass < passCount; pass++) 
                {
                    int seriesCount = dataset.getSeriesCount();

                    for (int series = 0; series < seriesCount; series++) 
                    {
                        EntityCollection ents = renderer.drawItems(g2, state, dataArea, info,
                                this, xAxis, yAxis, dataset, series, 
                                crosshairState, pass);

                        lastSavedEntities.addAll(ents);
                    }
                }
            }
        }
        return foundData;
    }	

    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) 
    {    	 	

        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) 
        {
            return;
        }

        // record the plot area...
        if (info != null) 
        {
            info.setPlotArea(area);
        }

        // adjust the drawing area for the plot insets (if any)...
        RectangleInsets insets = getInsets();
        insets.trim(area);      

        AxisSpace space = calculateAxisSpace(g2, area);
        Rectangle2D dataArea = space.shrink(area, null);
        this.getAxisOffset().trim(dataArea);

        // int sideLength = (int)Math.min(dataArea.getWidth(), dataArea.getHeight());
        // dataArea = new Rectangle2D.Double(dataArea.getX(), dataArea.getY(), sideLength, sideLength);

        dataArea = integerise(dataArea);
        if (dataArea.isEmpty()) {
            return;
        }
        createAndAddEntity((Rectangle2D) dataArea.clone(), info, null, null);
        if (info != null) {
            info.setDataArea(dataArea);
        }

        // draw the plot background and axes...
        drawBackground(g2, dataArea);
        Map<?,?> axisStateMap = drawAxes(g2, area, dataArea, info);

        PlotOrientation orient = getOrientation();

        // the anchor point is typically the point where the mouse last
        // clicked - the crosshairs will be driven off this point...
        if (anchor != null && !dataArea.contains(anchor)) 
        {
            anchor = null;
        }
        CrosshairState crosshairState = new CrosshairState();
        crosshairState.setCrosshairDistance(Double.POSITIVE_INFINITY);
        crosshairState.setAnchor(anchor);

        crosshairState.setAnchorX(Double.NaN);
        crosshairState.setAnchorY(Double.NaN);
        if (anchor != null) 
        {
            ValueAxis domainAxis = getDomainAxis();
            if (domainAxis != null) 
            {
                double x;
                if (orient == PlotOrientation.VERTICAL) 
                {
                    x = domainAxis.java2DToValue(anchor.getX(), dataArea,
                            getDomainAxisEdge());
                }
                else 
                {
                    x = domainAxis.java2DToValue(anchor.getY(), dataArea, getDomainAxisEdge());
                }
                crosshairState.setAnchorX(x);
            }
            ValueAxis rangeAxis = getRangeAxis();
            if (rangeAxis != null) 
            {
                double y;
                if (orient == PlotOrientation.VERTICAL) {
                    y = rangeAxis.java2DToValue(anchor.getY(), dataArea,
                            getRangeAxisEdge());
                }
                else {
                    y = rangeAxis.java2DToValue(anchor.getX(), dataArea,
                            getRangeAxisEdge());
                }
                crosshairState.setAnchorY(y);
            }
        }
        crosshairState.setCrosshairX(getDomainCrosshairValue());
        crosshairState.setCrosshairY(getRangeCrosshairValue());
        Shape originalClip = g2.getClip();
        Composite originalComposite = g2.getComposite();

        g2.clip(dataArea);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getForegroundAlpha()));

        AxisState domainAxisState = (AxisState) axisStateMap.get(getDomainAxis());
        if (domainAxisState == null) 
        {
            if (parentState != null) 
            {
                domainAxisState = (AxisState) parentState.getSharedAxisStates()
                        .get(getDomainAxis());
            }
        }

        AxisState rangeAxisState = (AxisState) axisStateMap.get(getRangeAxis());
        if (rangeAxisState == null) 
        {
            if (parentState != null) 
            {
                rangeAxisState = (AxisState) parentState.getSharedAxisStates()
                        .get(getRangeAxis());
            }
        }         

        if(useDataImageBuffer)
        {
            refresh = refresh || shouldBeResized(dataArea);

            if(refresh)
            {       		
                refresh = false;
                bufferedImage = new BufferedImage((int)dataArea.getWidth(), (int) dataArea.getHeight(), BufferedImage.TYPE_INT_ARGB);

                Graphics2D bufferGraphics = bufferedImage.createGraphics();

                bufferGraphics.addRenderingHints(g2.getRenderingHints());
                bufferGraphics.translate(-dataArea.getX(), -dataArea.getY());

                DatasetRenderingOrder order = getDatasetRenderingOrder();
                if (order == DatasetRenderingOrder.FORWARD) 
                {
                    // render data items...
                    for (int i = 0; i < getDatasetCount(); i++) 
                    {
                        render(bufferGraphics, dataArea, i, info, crosshairState);
                    }
                }
                else if (order == DatasetRenderingOrder.REVERSE) 
                {
                    for (int i = getDatasetCount() - 1; i >= 0; i--) 
                    {
                        render(bufferGraphics, dataArea, i, info, crosshairState);
                    }       
                }


                bufferGraphics.dispose();


            } 
            else
            {
                if(info != null)
                {
                    ChartRenderingInfo chartInfo = info.getOwner();

                    EntityCollection collection = chartInfo.getEntityCollection();
                    collection.addAll(lastSavedEntities);
                }
            }
            g2.drawImage(this.bufferedImage, (int) dataArea.getX(), (int) dataArea.getY(),  (int) dataArea.getWidth(), (int) dataArea.getHeight(), null);
        }    
        else
        {
            DatasetRenderingOrder order = getDatasetRenderingOrder();
            if (order == DatasetRenderingOrder.FORWARD) 
            {
                // render data items...
                for (int i = 0; i < getDatasetCount(); i++) 
                {
                    render(g2, dataArea, i, info, crosshairState);
                }

            }
            else if (order == DatasetRenderingOrder.REVERSE) 
            {
                for (int i = getDatasetCount() - 1; i >= 0; i--) 
                {
                    render(g2, dataArea, i, info, crosshairState);
                }       
            }	
        }


        if (domainAxisState != null) {
            drawDomainGridlines(g2, dataArea, domainAxisState.getTicks());
            drawZeroDomainBaseline(g2, dataArea);
        }
        if (rangeAxisState != null) {
            drawRangeGridlines(g2, dataArea, rangeAxisState.getTicks());
            drawZeroRangeBaseline(g2, dataArea);
        }

        // draw the markers that are associated with a specific renderer...
        for (int i = 0; i < this.getRendererCount(); i++) {
            drawDomainMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }
        for (int i = 0; i < this.getRendererCount(); i++) {
            drawRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }

        drawAnnotations(g2, dataArea, info);
        drawProfiles(g2, dataArea, 	info); 
        drawMapMarkers(g2, dataArea, info);
        drawROIs(g2, dataArea, info);

        g2.setClip(originalClip);
        g2.setComposite(originalComposite);

        drawOutline(g2, dataArea);
    }


    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
            PlotState parentState, PlotRenderingInfo info, StackModel<?> stackModel, int frame) 
    {    	 	
        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) 
        {
            return;
        }

        // record the plot area...
        if (info != null) 
        {
            info.setPlotArea(area);
        }

        // adjust the drawing area for the plot insets (if any)...
        RectangleInsets insets = getInsets();
        insets.trim(area);      

        AxisSpace space = calculateAxisSpace(g2, area);
        Rectangle2D dataArea = space.shrink(area, null);
        this.getAxisOffset().trim(dataArea);

        // int sideLength = (int)Math.min(dataArea.getWidth(), dataArea.getHeight());
        // dataArea = new Rectangle2D.Double(dataArea.getX(), dataArea.getY(), sideLength, sideLength);

        dataArea = integerise(dataArea);
        if (dataArea.isEmpty()) {
            return;
        }
        createAndAddEntity((Rectangle2D) dataArea.clone(), info, null, null);
        if (info != null) {
            info.setDataArea(dataArea);
        }

        // draw the plot background and axes...
        drawBackground(g2, dataArea);
        Map<?,?> axisStateMap = drawAxes(g2, area, dataArea, info);

        PlotOrientation orient = getOrientation();

        // the anchor point is typically the point where the mouse last
        // clicked - the crosshairs will be driven off this point...
        if (anchor != null && !dataArea.contains(anchor)) 
        {
            anchor = null;
        }
        CrosshairState crosshairState = new CrosshairState();
        crosshairState.setCrosshairDistance(Double.POSITIVE_INFINITY);
        crosshairState.setAnchor(anchor);

        crosshairState.setAnchorX(Double.NaN);
        crosshairState.setAnchorY(Double.NaN);
        if (anchor != null) 
        {
            ValueAxis domainAxis = getDomainAxis();
            if (domainAxis != null) 
            {
                double x;
                if (orient == PlotOrientation.VERTICAL) 
                {
                    x = domainAxis.java2DToValue(anchor.getX(), dataArea,
                            getDomainAxisEdge());
                }
                else 
                {
                    x = domainAxis.java2DToValue(anchor.getY(), dataArea, getDomainAxisEdge());
                }
                crosshairState.setAnchorX(x);
            }
            ValueAxis rangeAxis = getRangeAxis();
            if (rangeAxis != null) 
            {
                double y;
                if (orient == PlotOrientation.VERTICAL) {
                    y = rangeAxis.java2DToValue(anchor.getY(), dataArea,
                            getRangeAxisEdge());
                }
                else {
                    y = rangeAxis.java2DToValue(anchor.getX(), dataArea,
                            getRangeAxisEdge());
                }
                crosshairState.setAnchorY(y);
            }
        }
        crosshairState.setCrosshairX(getDomainCrosshairValue());
        crosshairState.setCrosshairY(getRangeCrosshairValue());
        Shape originalClip = g2.getClip();
        Composite originalComposite = g2.getComposite();

        g2.clip(dataArea);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                getForegroundAlpha()));

        AxisState domainAxisState = (AxisState) axisStateMap.get(getDomainAxis());
        if (domainAxisState == null) 
        {
            if (parentState != null) 
            {
                domainAxisState = (AxisState) parentState.getSharedAxisStates()
                        .get(getDomainAxis());
            }
        }

        AxisState rangeAxisState = (AxisState) axisStateMap.get(getRangeAxis());
        if (rangeAxisState == null) 
        {
            if (parentState != null) 
            {
                rangeAxisState = (AxisState) parentState.getSharedAxisStates()
                        .get(getRangeAxis());
            }
        }         

        if(useDataImageBuffer)
        {
            BufferedImage bufferedImage = new BufferedImage((int)dataArea.getWidth(), (int) dataArea.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D bufferGraphics = bufferedImage.createGraphics();
            bufferGraphics.translate(-dataArea.getX(), -dataArea.getY());


            DatasetRenderingOrder order = getDatasetRenderingOrder();
            if (order == DatasetRenderingOrder.FORWARD) 
            {
                // render data items...
                for (int i = 0; i < getDatasetCount(); i++) 
                {
                    Movie2DDataset movie = (Movie2DDataset)getDataset(i);
                    XYZDataset dataset = movie.getFrame(frame);
                    render(bufferGraphics, dataArea, i, dataset, info, crosshairState);
                }

            }
            else if (order == DatasetRenderingOrder.REVERSE) 
            {
                for (int i = getDatasetCount() - 1; i >= 0; i--) 
                {
                    Movie2DDataset movie = (Movie2DDataset)getDataset(i);
                    XYZDataset dataset = movie.getFrame(frame);
                    render(bufferGraphics, dataArea, i, dataset, info, crosshairState);
                }       
            }


            bufferGraphics.dispose();

            g2.drawImage(bufferedImage, (int) dataArea.getX(), (int) dataArea.getY(),  (int) dataArea.getWidth(), (int) dataArea.getHeight(), null);
        }   

        if (domainAxisState != null) {
            drawDomainGridlines(g2, dataArea, domainAxisState.getTicks());
            drawZeroDomainBaseline(g2, dataArea);
        }
        if (rangeAxisState != null) {
            drawRangeGridlines(g2, dataArea, rangeAxisState.getTicks());
            drawZeroRangeBaseline(g2, dataArea);
        }

        // draw the markers that are associated with a specific renderer...
        for (int i = 0; i < this.getRendererCount(); i++) {
            drawDomainMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }
        for (int i = 0; i < this.getRendererCount(); i++) {
            drawRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }

        drawAnnotations(g2, dataArea, info, stackModel, frame);
        drawProfiles(g2, dataArea, info); 
        drawMapMarkers(g2, dataArea, info);
        drawROIs(g2, dataArea, info);

        g2.setClip(originalClip);
        g2.setComposite(originalComposite);

        drawOutline(g2, dataArea);
    }

    @Override
    public void drawDataArea(Graphics2D g2, Rectangle2D area, Point2D anchor, Map<Axis, List<ValueTick>> ticks,
            PlotState parentState, PlotRenderingInfo info) 
    {
        // if the plot area is too small, just return...

        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }


        Rectangle2D dataArea = integerise(area);
        if (dataArea.isEmpty()) {
            return;
        }

        // draw the plot background and axes...
        drawBackground(g2, dataArea);

        PlotOrientation orient = getOrientation();

        // the anchor point is typically the point where the mouse last
        // clicked - the crosshairs will be driven off this point...
        if (anchor != null && !dataArea.contains(anchor)) {
            anchor = null;
        }
        CrosshairState crosshairState = new CrosshairState();
        crosshairState.setCrosshairDistance(Double.POSITIVE_INFINITY);
        crosshairState.setAnchor(anchor);

        crosshairState.setAnchorX(Double.NaN);
        crosshairState.setAnchorY(Double.NaN);
        if (anchor != null) {
            ValueAxis domainAxis = getDomainAxis();
            if (domainAxis != null) {
                double x;
                if (orient == PlotOrientation.VERTICAL) {
                    x = domainAxis.java2DToValue(anchor.getX(), dataArea,
                            getDomainAxisEdge());
                }
                else {
                    x = domainAxis.java2DToValue(anchor.getY(), dataArea,
                            getDomainAxisEdge());
                }
                crosshairState.setAnchorX(x);
            }
            ValueAxis rangeAxis = getRangeAxis();
            if (rangeAxis != null) {
                double y;
                if (orient == PlotOrientation.VERTICAL) {
                    y = rangeAxis.java2DToValue(anchor.getY(), dataArea,
                            getRangeAxisEdge());
                }
                else {
                    y = rangeAxis.java2DToValue(anchor.getX(), dataArea,
                            getRangeAxisEdge());
                }
                crosshairState.setAnchorY(y);
            }
        }
        crosshairState.setCrosshairX(getDomainCrosshairValue());
        crosshairState.setCrosshairY(getRangeCrosshairValue());

        Shape originalClip = g2.getClip();
        Composite originalComposite = g2.getComposite();

        g2.clip(dataArea);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                getForegroundAlpha()));

        if(!getGridlinesOnTop())
        {
            drawDomainGridlines(g2, dataArea, ticks.get(getDomainAxis()));
            drawZeroDomainBaseline(g2, dataArea);

            drawRangeGridlines(g2, dataArea, ticks.get(getRangeAxis()));
            drawZeroRangeBaseline(g2, dataArea);

        }


        if(useDataImageBuffer)
        {
            refresh = refresh || shouldBeResized(dataArea);

            if(refresh)
            {  
                refresh = false;

                bufferedImage = new BufferedImage((int)dataArea.getWidth(), (int) dataArea.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D bufferGraphics = bufferedImage.createGraphics();
                bufferGraphics.translate(-dataArea.getX(), -dataArea.getY());

                DatasetRenderingOrder order = getDatasetRenderingOrder();
                if (order == DatasetRenderingOrder.FORWARD) 
                {
                    // render data items...
                    for (int i = 0; i < getDatasetCount(); i++) 
                    {
                        render(bufferGraphics, dataArea, i, info, crosshairState);
                    }

                }
                else if (order == DatasetRenderingOrder.REVERSE) 
                {
                    for (int i = getDatasetCount() - 1; i >= 0; i--) 
                    {
                        render(bufferGraphics, dataArea, i, info, crosshairState);
                    }       
                }


                bufferGraphics.dispose();
            } 
            else
            {
                if(info != null)
                {
                    ChartRenderingInfo chartInfo = info.getOwner();

                    EntityCollection collection = chartInfo.getEntityCollection();
                    collection.addAll(lastSavedEntities);
                }
            }
            g2.drawImage(this.bufferedImage, (int) dataArea.getX(), (int) dataArea.getY(),  (int) dataArea.getWidth(), (int) dataArea.getHeight(), null);
        }    
        else
        {
            DatasetRenderingOrder order = getDatasetRenderingOrder();
            if (order == DatasetRenderingOrder.FORWARD) 
            {
                // render data items...
                for (int i = 0; i < getDatasetCount(); i++) 
                {
                    render(g2, dataArea, i, info, crosshairState);
                }

            }
            else if (order == DatasetRenderingOrder.REVERSE) 
            {
                for (int i = getDatasetCount() - 1; i >= 0; i--) 
                {
                    render(g2, dataArea, i, info, crosshairState);
                }       
            }	
        }

        // draw the markers that are associated with a specific renderer...
        for (int i = 0; i < this.getRendererCount(); i++) {
            drawDomainMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }
        for (int i = 0; i < this.getRendererCount(); i++) {
            drawRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }


        drawAnnotations(g2, dataArea, info);
        drawProfiles(g2, dataArea, info); 
        drawMapMarkers(g2, dataArea, info);
        drawROIs(g2, dataArea, info);

        if(getGridlinesOnTop())
        {
            drawDomainGridlines(g2, dataArea, ticks.get(getDomainAxis()));
            drawZeroDomainBaseline(g2, dataArea);

            drawRangeGridlines(g2, dataArea, ticks.get(getRangeAxis()));
            drawZeroRangeBaseline(g2, dataArea);

        }

        g2.setClip(originalClip);
        g2.setComposite(originalComposite);

        drawOutline(g2, dataArea);


        ///////////////////////////////////////////////////////////////////////////////////////
    }    


    public void drawDataArea(Graphics2D g2, Rectangle2D area, Point2D anchor, Map<Axis, List<ValueTick>> ticks,
            PlotState parentState, PlotRenderingInfo info, StackModel<?> stackModel, int frame) 
    {

        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) 
        {
            return;
        }

        Rectangle2D dataArea = integerise(area);
        if (dataArea.isEmpty()) {
            return;
        }

        // draw the plot background and axes...
        drawBackground(g2, dataArea);

        PlotOrientation orient = getOrientation();

        // the anchor point is typically the point where the mouse last
        // clicked - the crosshairs will be driven off this point...
        if (anchor != null && !dataArea.contains(anchor)) {
            anchor = null;
        }
        CrosshairState crosshairState = new CrosshairState();
        crosshairState.setCrosshairDistance(Double.POSITIVE_INFINITY);
        crosshairState.setAnchor(anchor);

        crosshairState.setAnchorX(Double.NaN);
        crosshairState.setAnchorY(Double.NaN);
        if (anchor != null) {
            ValueAxis domainAxis = getDomainAxis();
            if (domainAxis != null) 
            {
                double x;
                if (orient == PlotOrientation.VERTICAL) 
                {
                    x = domainAxis.java2DToValue(anchor.getX(), dataArea,
                            getDomainAxisEdge());
                }
                else {
                    x = domainAxis.java2DToValue(anchor.getY(), dataArea,
                            getDomainAxisEdge());
                }
                crosshairState.setAnchorX(x);
            }
            ValueAxis rangeAxis = getRangeAxis();
            if (rangeAxis != null) {
                double y;
                if (orient == PlotOrientation.VERTICAL) {
                    y = rangeAxis.java2DToValue(anchor.getY(), dataArea,
                            getRangeAxisEdge());
                }
                else {
                    y = rangeAxis.java2DToValue(anchor.getX(), dataArea,
                            getRangeAxisEdge());
                }
                crosshairState.setAnchorY(y);
            }
        }
        crosshairState.setCrosshairX(getDomainCrosshairValue());
        crosshairState.setCrosshairY(getRangeCrosshairValue());

        Shape originalClip = g2.getClip();
        Composite originalComposite = g2.getComposite();

        g2.clip(dataArea);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                getForegroundAlpha()));

        if(!getGridlinesOnTop())
        {
            drawDomainGridlines(g2, dataArea, ticks.get(getDomainAxis()));
            drawZeroDomainBaseline(g2, dataArea);

            drawRangeGridlines(g2, dataArea, ticks.get(getRangeAxis()));
            drawZeroRangeBaseline(g2, dataArea);

        }


        if(useDataImageBuffer)
        {         

            BufferedImage bufferedImage = new BufferedImage((int)dataArea.getWidth(), (int) dataArea.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bufferGraphics = bufferedImage.createGraphics();
            bufferGraphics.translate(-dataArea.getX(), -dataArea.getY());

            DatasetRenderingOrder order = getDatasetRenderingOrder();
            if (order == DatasetRenderingOrder.FORWARD) 
            {
                // render data items...
                for (int i = 0; i < getDatasetCount(); i++) 
                {
                    Movie2DDataset movie = (Movie2DDataset)getDataset(i);
                    XYZDataset dataset = movie.getFrame(frame);
                    render(g2, dataArea, i, dataset, info, crosshairState);                     
                }

            }
            else if (order == DatasetRenderingOrder.REVERSE) 
            {
                for (int i = getDatasetCount() - 1; i >= 0; i--) 
                {
                    Movie2DDataset movie = (Movie2DDataset)getDataset(i);
                    XYZDataset dataset = movie.getFrame(frame);
                    render(g2, dataArea, i, dataset, info, crosshairState);                     
                }       
            }


            bufferGraphics.dispose();

            g2.drawImage(bufferedImage, (int) dataArea.getX(), (int) dataArea.getY(),  (int) dataArea.getWidth(), (int) dataArea.getHeight(), null);
        }    
        else
        {
            DatasetRenderingOrder order = getDatasetRenderingOrder();
            if (order == DatasetRenderingOrder.FORWARD) 
            {
                // render data items...
                for (int i = 0; i < getDatasetCount(); i++) 
                {
                    Movie2DDataset movie = (Movie2DDataset)getDataset(i);
                    XYZDataset dataset = movie.getFrame(frame);
                    render(g2, dataArea, i, dataset, info, crosshairState);
                }

            }
            else if (order == DatasetRenderingOrder.REVERSE) 
            {
                for (int i = getDatasetCount() - 1; i >= 0; i--) 
                {
                    Movie2DDataset movie = (Movie2DDataset)getDataset(i);
                    XYZDataset dataset = movie.getFrame(frame);
                    render(g2, dataArea, i, dataset, info, crosshairState);
                }       
            }	
        }

        // draw the markers that are associated with a specific renderer...
        for (int i = 0; i < this.getRendererCount(); i++) {
            drawDomainMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }
        for (int i = 0; i < this.getRendererCount(); i++) {
            drawRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }        

        drawAnnotations(g2, dataArea, info, stackModel, frame);
        drawProfiles(g2, dataArea, info);
        drawMapMarkers(g2, dataArea, info);
        drawROIs(g2, dataArea, info);

        if(getGridlinesOnTop())
        {
            drawDomainGridlines(g2, dataArea, ticks.get(getDomainAxis()));
            drawZeroDomainBaseline(g2, dataArea);

            drawRangeGridlines(g2, dataArea, ticks.get(getRangeAxis()));
            drawZeroRangeBaseline(g2, dataArea);

        }

        g2.setClip(originalClip);
        g2.setComposite(originalComposite);

        drawOutline(g2, dataArea);


        ///////////////////////////////////////////////////////////////////////////////////////
    }    

    public void drawRoamingTextTitle(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info, StackModel<?> stackModel, int frame)
    {  
        RoamingTextTitle originalTitle = getRoamingTitle();
        if(originalTitle != null)
        {
            boolean draw = originalTitle.isVisible() && originalTitle.isInside();

            if(draw)
            {
                RoamingTextTitle titleCopy = originalTitle.copy();

                if(titleCopy instanceof RoamingStackTextTitle)
                {
                    double currentValue = stackModel.getStackingValue(frame);
                    PrefixedUnit unit = stackModel.getStackingQuantity().getUnit();

                    ((RoamingStackTextTitle) titleCopy).setFrameTitleText(currentValue, unit);
                }
                drawRoamingTitle(titleCopy, g2, dataArea, info);
            }		
        }		
    }

    public void drawAnnotations(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info, StackModel<?> stackModel, int frame) 
    {
        Iterator<?> iterator = this.getAnnotations().iterator();
        while (iterator.hasNext()) 
        {
            XYAnnotation annotation = (XYAnnotation) iterator.next();	

            ValueAxis xAxis = getDomainAxis();
            ValueAxis yAxis = getRangeAxis();
            annotation.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
        }

        drawScaleBars(g2, dataArea, info);
        drawRoamingLegend(g2, dataArea, info);
        drawRoamingSublegends(g2, dataArea, info);
        drawRoamingTextTitle(g2, dataArea, info, stackModel, frame);
    }

    public void drawProfiles(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) 
    {
        for(Profile profile : profiles.values())
        {
            ValueAxis xAxis = getDomainAxis();
            ValueAxis yAxis = getRangeAxis();
            profile.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
        }
    }

    public void drawMapMarkers(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) 
    {
        for(MapMarker mapMarker : mapMarkers.values())
        {
            ValueAxis xAxis = getDomainAxis();
            ValueAxis yAxis = getRangeAxis();
            mapMarker.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
        }
    }

    public void drawROIs(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) 
    {
        for(ROIDrawable roi : rois.values())
        {
            ValueAxis xAxis = getDomainAxis();
            ValueAxis yAxis = getRangeAxis();
            roi.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
        }
    }


    private boolean shouldBeResized(Rectangle2D dataArea)
    {
        boolean widthUnequal  = bufferedImage.getWidth() != (int)dataArea.getWidth();
        boolean heightUnequal = bufferedImage.getHeight() != (int)dataArea.getHeight();

        return (widthUnequal||heightUnequal);
    }

    @Override
    public Channel2DPlot clone()
    {       
        Channel2DPlot clone = (Channel2DPlot) super.clone();

        clone.rois = new LinkedHashMap<>();
        clone.profiles = new LinkedHashMap<>();
        clone.mapMarkers = new LinkedHashMap<>();

        return clone;
    }

    private Rectangle integerise(Rectangle2D rect) 
    {
        int x0 = (int) Math.ceil(rect.getMinX());
        int y0 = (int) Math.ceil(rect.getMinY());
        int x1 = (int) Math.floor(rect.getMaxX());
        int y1 = (int) Math.floor(rect.getMaxY());
        return new Rectangle(x0, y0, (x1 - x0), (y1 - y0));
    }

    @Override
    public void datasetChanged(DatasetChangeEvent ev)
    {        
        this.refresh = true;	
        super.datasetChanged(ev);
    }

    @Override
    public void rendererChanged(RendererChangeEvent evt)
    {
        refresh = true;
        super.rendererChanged(evt);
    }

    @Override
    public void axisChanged(AxisChangeEvent evt)
    {        
        refresh = true;
        super.axisChanged(evt);
    }

    public boolean isUseDataImageBuffer()
    {
        return useDataImageBuffer;
    }

    public void setUseDataImageBuffer(boolean use)
    {
        this.useDataImageBuffer = use;
    }

    @Override
    public Paint getDefaultAnnotationPaint()
    {
        return Color.white;
    }

    @Override
    public int getDepthAxisCount()
    {
        int depthAxisCount = 0;

        List<ChannelRenderer> renderers = getRenderers();
        for(ChannelRenderer renderer : renderers)
        {
            if(renderer instanceof PaintScaleSource && ((PaintScaleSource) renderer).hasDepthAxis())
            {
                depthAxisCount++;
            }
        }
        return depthAxisCount;
    }

    @Override
    public Axis getDepthAxis(int index)
    {
        int depthAxisCount = 0;

        List<ChannelRenderer> renderers = getRenderers();    

        Axis depthAxis = null;

        for(ChannelRenderer renderer : renderers)
        {
            if(renderer instanceof PaintScaleSource && ((PaintScaleSource) renderer).hasDepthAxis())
            {                      
                if(depthAxisCount == index)
                {
                    depthAxis = ((PaintScaleSource) renderer).getDepthAxis();
                    break;
                }

                depthAxisCount++;
            }
        }

        return depthAxis;
    }

    @Override
    public boolean hasDomainTooltipManagers() {
        return !domainTooltipStyleManagers.isEmpty();
    }

    @Override
    public int getDomainTooltipManagerCount() {
        return domainTooltipStyleManagers.size();
    }

    @Override
    public TooltipStyleManager getDomainTooltipManager(int index) {
        return domainTooltipStyleManagers.get(index);
    }

    @Override
    public boolean hasRangeTooltipManagers() {
        return !rangeTooltipStyleManagers.isEmpty();
    }

    @Override
    public int getRangeTooltipManagerCount() {
        return rangeTooltipStyleManagers.size();
    }

    @Override
    public TooltipStyleManager getRangeTooltipManager(int index) {
        return rangeTooltipStyleManagers.get(index);
    }

    @Override
    public boolean hasDepthTooltipManagers() 
    {
        int depthTooltipCount = getDepthTooltipManagerCount();
        boolean hasTooltips = depthTooltipCount>0;
        return hasTooltips;
    }

    @Override
    public int getDepthTooltipManagerCount() 
    {
        int depthTooltipCount = 0;

        List<ChannelRenderer> renderers = getRenderers();
        for(ChannelRenderer renderer : renderers)
        {
            if(renderer instanceof Channel2DRenderer)
            {
                depthTooltipCount++;
            }
        }
        return depthTooltipCount;
    }

    @Override
    public TooltipStyleManager getDepthTooltipManager(int index) {

        int depthAxisCount = 0;

        List<ChannelRenderer> renderers = getRenderers();    

        TooltipStyleManager tooltipManager = null;

        for(ChannelRenderer renderer : renderers)
        {
            if(renderer instanceof Channel2DRenderer)
            {                      
                if(depthAxisCount == index)
                {
                    tooltipManager = ((Channel2DRenderer) renderer).getTooltipStyleManager();
                    break;
                }

                depthAxisCount++;
            }
        }

        return tooltipManager;
    }

    @Override
    public void setRenderer(int index, XYItemRenderer renderer)
    {
        XYItemRenderer rendererOld = getRenderer(index);

        super.setRenderer(index, renderer);

        XYItemRenderer rendererNew = getRenderer(index);

        if(!Objects.equals(rendererNew, rendererOld))
        {
            if(rendererNew instanceof GradientPaintReceiver)
            {
                ((GradientPaintReceiver) rendererNew).addPropertyChangeListener(listener);
            }
            if(rendererOld instanceof GradientPaintReceiver)
            {
                ((GradientPaintReceiver) rendererOld).removePropertyChangeListener(listener);
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private class LocalPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            String property = evt.getPropertyName();

            if(Channel2DSupervisor.ROI_SAMPLES_NEEDED.equals(property))
            {
                propertyChangeSupport.firePropertyChange(evt);
            }
        }
    }

    public boolean isAspectRatioLockedByDefault()
    {
        boolean fixedByDefault = false;

        ValueAxis domainAxis = getDomainAxis();
        ValueAxis rangeAxis = getRangeAxis();

        if(domainAxis instanceof CustomizableNumberAxis && rangeAxis instanceof CustomizableNumberAxis)
        {
            PrefixedUnit domainUnit = ((CustomizableNumberAxis)domainAxis).getDataUnit();
            PrefixedUnit rangeUnit = ((CustomizableNumberAxis)rangeAxis).getDataUnit();

            fixedByDefault = domainUnit.isCompatible(rangeUnit);
        }

        return fixedByDefault;
    }
}
