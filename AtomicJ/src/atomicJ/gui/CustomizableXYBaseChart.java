
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
 * The method draw() is a modification of a corresponding method from JFreeChart class, part of JFreeChart library, copyright by Object Refiner Limited and other contributors.
 * The source code of JFreeChart can be found through the API doc at the page http://www.jfree.org/jfreechart/api/javadoc/index.html 
 */

package atomicJ.gui;

import static atomicJ.gui.MouseInputModeStandard.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.JFreeChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.entity.TitleEntity;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.ui.Align;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.Size2D;

import atomicJ.data.Channel1D;
import atomicJ.data.units.UnitPoint2D;
import atomicJ.utilities.Validation;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;


public class CustomizableXYBaseChart<E extends CustomizableXYBasePlot> extends CustomizableChart implements PreferencesSource, RoamingTitleChangeListener
{   
    private static final long serialVersionUID = 1L;

    public static final String CHART_EDITION = "CHART_EDITION";

    private static final ChartStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultChartStyleSupplier.getSupplier();
    private static final RectangleInsets DEFAULT_LEGEND_INSETS = new RectangleInsets(1.0, 1.0, 1.0, 1.0);//this class is immutable
    private static final BlockFrame DEFAULT_LEGEND_FRAME = new LineBorder();//this instance cannot be modified, so we can share it (although the instances created by other constructor of LineBorder class are not immutable, because they can hold references to mutable types)

    private MouseInputMode mode = NORMAL;
    private Map<MouseInputType, MouseInputMode> accessoryModes = new HashMap<>();

    private Point2D caughtPoint;
    private RoamingTitle caughtRoamingTitle;

    private RoamingLegend roamingLegend;
    private final List<RoamingLegend> roamingSublegends = new ArrayList<>();
    private RoamingTextTitle roamingTextTitle;

    private ChartSupervisor chartSupervisor;

    private final Preferences pref;
    private final String styleKey;

    public CustomizableXYBaseChart(CustomizableXYBasePlot plot, String styleKey) 
    {
        super(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        this.pref = plot.getPreferences().node("Chart");    
        this.styleKey = styleKey;

        setUseFixedChartAreaSize(false);//the chart should be created using this fixed size
        setUseFixedPlotAreaSize(true);//the fixed chart size should be calculated based on a desired plot area size instead of the "static" preferred size of the chart 
        setPrefferedChartStyle(pref);   
    }

    public LegendTitle createDefaultLegend()
    {       
        LegendTitle legend = new LegendTitle(getPlot());
        legend.setMargin(DEFAULT_LEGEND_INSETS);
        legend.setFrame(DEFAULT_LEGEND_FRAME);
        legend.setBackgroundPaint(Color.white);
        legend.setPosition(RectangleEdge.BOTTOM);

        return legend;
    }

    @Override
    public Preferences getPreferences()
    {
        return pref;
    }

    @Override
    public PreferredChartStyle getPreferredChartStyle()
    {
        return PreferredChartStyle.getInstance(pref, isAspectRatioLockedByDefault());
    }

    public boolean isNormalMode()
    {
        return mode.equals(NORMAL);
    }

    public boolean isNormalMode(MouseInputType inputType)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.equals(NORMAL);
    }

    public boolean isDistanceMeasurementMode()
    {
        return mode.isMeasurement();
    }

    public boolean isDistanceMeasurementMode(MouseInputType inputType)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.isMeasurement();
    }

    public boolean isMoveDataItems(Comparable<?> key)
    {
        return mode.isMoveDataItems(key);
    }

    public boolean isMoveDataItems(MouseInputType inputType, Object movableDatasetKey)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.isMoveDataItems(movableDatasetKey);
    }

    public boolean isMoveDataItems(MouseInputType inputType, Object movableDatasetKey, DataModificationType movementType)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.isMoveDataItems(movableDatasetKey, movementType);
    }

    public boolean isDrawDatasetMode(Object datasetGroupTag)
    {
        return getMode().isDrawDataset(datasetGroupTag);
    }

    public boolean isDrawDatasetMode(MouseInputType inputType, Object datasetGroupTag)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.isDrawDataset(datasetGroupTag);
    }

    public boolean isROIMode()
    {
        return mode.isROI();
    }

    public boolean isROIMode(MouseInputType inputType)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.isROI();
    }

    public boolean isFreeRoiMode()
    {
        return mode.equals(FREE_HAND_ROI);
    }

    public boolean isProfileMode()
    {
        return getMode().isProfile();
    }

    public boolean isProfileMode(MouseInputType inputType)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.isProfile();
    }

    public boolean isInsertMapMarkerMode()
    {
        return getMode().equals(INSERT_MAP_MARKER);
    }

    public boolean isInsertMapMarkerMode(MouseInputType inputType)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.equals(INSERT_MAP_MARKER);
    }

    public boolean isInsertDomainValueMarkerMode()
    {
        return getMode().equals(INSERT_DOMAIN_MARKER);
    }

    public boolean isInsertDomainValueMarkerMode(MouseInputType inputType)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.equals(INSERT_DOMAIN_MARKER);
    }

    public boolean isInsertRangeMarkerMode()
    {
        return getMode().equals(INSERT_RANGE_MARKER);
    }

    public boolean isInsertRangeMarkerMode(MouseInputType inputType)
    {
        MouseInputMode mode = getMode(inputType);
        return mode.equals(INSERT_RANGE_MARKER);
    }

    @Override
    public MouseInputMode getMode()
    {
        return this.mode;
    }

    @Override
    public void setMode(MouseInputMode mode)
    {
        this.mode = mode;
    }

    public MouseInputMode getMode(MouseInputType inputType)
    {
        MouseInputMode accessoryMode = this.accessoryModes.get(inputType);

        if(accessoryMode != null)
        {
            return accessoryMode;
        }

        return this.mode;
    }

    public void setAccessoryMode(MouseInputType inputType, MouseInputMode modeNew)
    {                
        this.accessoryModes.put(inputType, modeNew);        
    }

    public void setAccessoryModes(Map<MouseInputType, MouseInputMode> modes)
    {
        this.accessoryModes = new HashMap<>(modes);
    }

    //// INSERT HORIZONTAL MARKER


    protected Range getDomainRange(Point2D  dataPoint, double axisFraction)
    {
        XYPlot plot = getCustomizablePlot();
        ValueAxis domainAxis = plot.getDomainAxis();

        double xRangeLength = domainAxis.getRange().getLength();
        double xRadius = axisFraction*xRangeLength;

        double center = dataPoint.getX();
        Range range = new Range(center - xRadius, center + xRadius);

        return range;
    }

    protected Range getRangeRange(Point2D  dataPoint, double axisFraction)
    {
        XYPlot plot = getCustomizablePlot();
        ValueAxis domainAxis = plot.getRangeAxis();

        double yRangeLength = domainAxis.getRange().getLength();
        double yRadius = axisFraction*yRangeLength;

        double center = dataPoint.getY();
        Range range = new Range(center - yRadius, center + yRadius);

        return range;
    }


    public Rectangle2D getDataSquare(Point2D  dataPoint, double axisFraction)
    {
        XYPlot plot = getCustomizablePlot();
        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = plot.getRangeAxis();

        double xRangeLength = domainAxis.getRange().getLength();
        double yRangeLength = rangeAxis.getRange().getLength();

        double xRadius = axisFraction*xRangeLength;
        double yRadius = axisFraction*yRangeLength;

        Rectangle2D square = new Rectangle2D.Double(dataPoint.getX() - xRadius, dataPoint.getY() - yRadius, 2*xRadius, 2*yRadius);
        return square;
    }

    @Override
    public void setChartSupervisor(ChartSupervisor chartSupervisor)
    {
        this.chartSupervisor = chartSupervisor;
    }

    @Override
    public ChartSupervisor getChartSupervisor()
    {
        return chartSupervisor;
    }   

    public void setRangeOfDomainAxisForDatasetIfPossible(Object datasetKey, double rangeMin, double rangeMax)
    {
        E plot = getCustomizablePlot();
        plot.setRangeOfDomainAxisForDatasetIfPossible(datasetKey, rangeMin, rangeMax);
    }

    public void setRangeOfRangeAxisForDatasetIfPossible(Object datasetKey, double rangeMin, double rangeMax)
    {
        E plot = getCustomizablePlot();
        plot.setRangeOfRangeAxisForDatasetIfPossible(datasetKey, rangeMin, rangeMax);
    }

    //DOMAIN UNITS

    public PrefixedUnit getDomainDataUnit(int index)
    {
        E plot = getCustomizablePlot();
        return plot.getDomainDataUnit(index);
    }

    public PrefixedUnit getDomainDataUnit()
    {
        E plot = getCustomizablePlot();
        return plot.getDomainDataUnit();
    }

    public PrefixedUnit getDomainDisplayedUnit(int index)
    {
        E plot = getCustomizablePlot();
        return plot.getDomainDisplayedUnit(index);
    }

    public PrefixedUnit getDomainDisplayedUnit()
    {
        E plot = getCustomizablePlot();
        return plot.getDomainDisplayedUnit();
    }

    //DOMAIN QUANTITIES

    public Quantity getDomainDataQuantity(int index)
    {
        E plot = getCustomizablePlot();
        return plot.getDomainDataQuantity(index);
    }

    public Quantity getDomainDataQuantity()
    {
        E plot = getCustomizablePlot();
        return plot.getDomainDataQuantity();
    }

    public Quantity getDomainDisplayedQuantity(int index)
    {
        E plot = getCustomizablePlot();
        return plot.getDomainDisplayedQuantity(index);
    }

    public Quantity getDomainDisplayedQuantity()
    {
        E plot = getCustomizablePlot();
        return plot.getDomainDisplayedQuantity();
    }


    //RANGE UNITS

    public PrefixedUnit getRangeDataUnit(int index)
    {
        E plot = getCustomizablePlot();
        return plot.getRangeDataUnit(index);
    }

    public PrefixedUnit getRangeDataUnit()
    {
        E plot = getCustomizablePlot();
        return plot.getRangeDataUnit();
    }

    public PrefixedUnit getRangeDisplayedUnit(int index)
    {
        E plot = getCustomizablePlot();
        return plot.getRangeDisplayedUnit(index);
    }

    public PrefixedUnit getRangeDisplayedUnit()
    {
        E plot = getCustomizablePlot();
        return plot.getRangeDisplayedUnit();
    }

    //RANGE QUANTITIES


    public Quantity getRangeDataQuantity(int index)
    {
        E plot = getCustomizablePlot();
        return plot.getRangeDataQuantity(index);
    }

    public Quantity getRangeDataQuantity()
    {
        E plot = getCustomizablePlot();
        return plot.getRangeDataQuantity();
    }

    public Quantity getRangeDisplayedQuantity(int index)
    {
        E plot = getCustomizablePlot();
        return plot.getRangeDisplayedQuantity(index);
    }

    public Quantity getRangeDisplayedQuantity()
    {
        E plot = getCustomizablePlot();
        return plot.getRangeDisplayedQuantity();
    }

    public void clearAllLayerData()
    {
        E plot = getCustomizablePlot();
        plot.clearAllLayerData();
    }

    public void clearLayerData(Object key)
    {
        E plot = getCustomizablePlot();
        plot.clearLayerData(key);
    }

    public void clearLayerData(Object key, boolean removeAxis)
    {
        E plot = getCustomizablePlot();
        plot.clearLayerData(key, removeAxis);
    }

    public void removeLayer(Object key, boolean removeAxis)
    {
        E plot = getCustomizablePlot();
        plot.removeLayer(key, removeAxis);
    }

    public void simplifyLayerIndices()
    {
        E plot = getCustomizablePlot();
        plot.simplifyLayerIndices();
    }

    public void resetData(Channel1D channel)
    {
        E plot = getCustomizablePlot();
        plot.resetData(channel);
    }

    public void resetData(Collection<? extends Channel1D> channels)
    {
        E plot = getCustomizablePlot();
        plot.resetData(channels);
    }  

    public void addOrReplaceData(Channel1D channel)
    {
        E plot = getCustomizablePlot();
        plot.addOrReplaceData(channel);
    }

    public void notifyOfDataChange(Object key)
    {
        E plot = getCustomizablePlot();
        plot.notifyOfDataChange(key);
    }

    public void setDataTo(Collection<? extends Channel1D> channels)
    {
        E plot = getCustomizablePlot();
        plot.setDataTo(channels);
    }  

    /*This method is taken directly from the source code of JFreeChart, written by David Gilbert and
     * collaborators. I just added a call to a method drawRoamingLegends()
     */
    @Override
    public void draw(Graphics2D g2, Rectangle2D chartArea, Point2D anchor, ChartRenderingInfo info) 
    {
        notifyListeners(new ChartProgressEvent(this, this, ChartProgressEvent.DRAWING_STARTED, 0));

        EntityCollection entities = null;
        // record the chart area, if info is requested...
        if (info != null) {
            info.clear();
            info.setChartArea(chartArea);
            entities = info.getEntityCollection();
        }
        if (entities != null) {
            entities.add(new JFreeChartEntity((Rectangle2D) chartArea.clone(),
                    this));
        }

        // ensure no drawing occurs outside chart area...
        Shape savedClip = g2.getClip();
        g2.clip(chartArea);

        g2.addRenderingHints(this.getRenderingHints());

        Paint backgroundPaint = getBackgroundPaint();
        // draw the chart background...
        if (backgroundPaint != null) {
            g2.setPaint(backgroundPaint);
            g2.fill(chartArea);
        }

        Image backgroundImage = getBackgroundImage();
        if (backgroundImage != null) {
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    this.getBackgroundImageAlpha()));
            Rectangle2D dest = new Rectangle2D.Double(0.0, 0.0,
                    backgroundImage.getWidth(null),
                    backgroundImage.getHeight(null));
            Align.align(dest, chartArea, getBackgroundImageAlignment());
            g2.drawImage(backgroundImage, (int) dest.getX(),
                    (int) dest.getY(), (int) dest.getWidth(),
                    (int) dest.getHeight(), null);
            g2.setComposite(originalComposite);
        }

        if (isBorderVisible()) {
            Paint paint = getBorderPaint();
            Stroke stroke = getBorderStroke();
            if (paint != null && stroke != null) {
                Rectangle2D borderArea = new Rectangle2D.Double(
                        chartArea.getX(), chartArea.getY(),
                        chartArea.getWidth() - 1.0, chartArea.getHeight()
                        - 1.0);
                g2.setPaint(paint);
                g2.setStroke(stroke);
                g2.draw(borderArea);
            }
        }

        // draw the title and subtitles...
        Rectangle2D nonTitleArea = new Rectangle2D.Double();
        nonTitleArea.setRect(chartArea);
        this.getPadding().trim(nonTitleArea);

        EntityCollection ett = drawRoamingTextTitle(g2, nonTitleArea, (entities != null));
        EntityCollection etl = drawRoamingLegends(g2, nonTitleArea,  (entities != null));

        if (ett != null && entities != null) {
            entities.addAll(ett);
        }

        if (etl != null &&  entities != null) {
            entities.addAll(etl);
        }

        Title title = getTitle();
        if (title != null && title.isVisible()) {
            EntityCollection e = drawTitle(title, g2, nonTitleArea,
                    (entities != null));
            if (e != null) {
                entities.addAll(e);
            }
        }

        Iterator<?> iterator = this.getSubtitles().iterator();
        while (iterator.hasNext()) {
            Title currentTitle = (Title) iterator.next();
            if (currentTitle.isVisible()) {
                EntityCollection e = drawTitle(currentTitle, g2, nonTitleArea,
                        (entities != null));
                if (e != null) {
                    entities.addAll(e);
                }
            }
        } 

        Rectangle2D plotArea = nonTitleArea;

        // draw the plot (axes and data visualization)
        PlotRenderingInfo plotInfo = null;
        if (info != null) {
            plotInfo = info.getPlotInfo();
        }

        drawPlot(g2, plotArea, anchor, null, plotInfo);

        g2.setClip(savedClip);

        notifyListeners(new ChartProgressEvent(this, this, ChartProgressEvent.DRAWING_FINISHED, 100));
    }

    protected void drawPlot(Graphics2D g2, Rectangle2D plotArea, Point2D anchor, PlotState parentState, PlotRenderingInfo plotInfo) 
    {
        getPlot().draw(g2, plotArea, anchor, null, plotInfo);
    }

    public EntityCollection drawRoamingTextTitle(Graphics2D g2, Rectangle2D nonTitleArea, boolean entities)
    {       
        return drawRoamingTitle(roamingTextTitle, g2, nonTitleArea, entities);  
    }

    public EntityCollection drawRoamingLegends(Graphics2D g2, Rectangle2D nonTitleArea, boolean entities)
    {       
        EntityCollection collection = new StandardEntityCollection();

        for(RoamingLegend sublegend : getAllRoamingLegends())
        {
            collection.addAll(drawRoamingTitle(sublegend, g2, nonTitleArea, entities));
        }

        return collection;
    }

    protected void expandWithRoamingLegends(Graphics2D g2, Size2D chartAreaSize)
    {
        for(RoamingLegend sublegend : getAllRoamingLegends())
        {
            expandWithRoamingTitleArea(sublegend, g2, chartAreaSize);
        }
    }

    public void subtractRoamingLegends(Graphics2D g2, Rectangle2D nonTitleArea)
    {       
        for(RoamingLegend sublegend : getAllRoamingLegends())
        {
            subtractRoamingTitle(sublegend, g2, nonTitleArea);
        }
    }


    public RoamingLegend getRoamingLegend()
    {
        return roamingLegend;
    }

    public void setRoamingLegend(RoamingLegend legend)
    {
        this.roamingLegend = Validation.requireNonNullParameterName(legend, "legend");
        this.roamingLegend.addRoamingTitleChangeListener(this);

        CustomizableXYBasePlot plot = getCustomizablePlot();
        plot.setRoamingLegend(roamingLegend);

        fireChartChanged();
    }


    public List<RoamingLegend> getRoamingSublegends()
    {
        return roamingSublegends;
    }

    public List<RoamingLegend> getAllRoamingLegends()
    {
        List<RoamingLegend> allLegends = new ArrayList<>();
        allLegends.add(roamingLegend);
        allLegends.addAll(roamingSublegends);

        return allLegends;
    }

    public void addRoamingSubLegend(RoamingLegend legend)
    {
        Validation.requireNonNullParameterName(legend, "legend");

        this.roamingSublegends.add(legend);
        legend.addRoamingTitleChangeListener(this);

        CustomizableXYBasePlot plot = getCustomizablePlot();
        plot.addRoamingSublegend(legend);

        fireChartChanged();
    }

    protected EntityCollection drawRoamingTitle(RoamingTitle title, Graphics2D g2, Rectangle2D nonTitleArea, boolean entities)
    {
        EntityCollection collection = new StandardEntityCollection();
        if(title != null)
        {
            boolean visible = title.isVisible();
            boolean inside = title.isInside();

            if(visible && !inside)
            {         

                Title roamingTitleOutside = title.getOutsideTitle();

                EntityCollection collectionForTitle = drawTitle(roamingTitleOutside, g2, nonTitleArea, entities);
                if(collectionForTitle != null)
                {
                    for(Object o : collectionForTitle.getEntities())
                    {
                        if(o instanceof TitleEntity)
                        {
                            RoamingTitleEntity roamingEntity = 
                                    new RoamingTitleEntity(((TitleEntity) o).getArea(), 
                                            title, ((TitleEntity) o).getToolTipText(),
                                            ((TitleEntity) o).getURLText());

                            collection.add(roamingEntity);
                        }
                        else if(o instanceof ChartEntity)
                        {
                            collection.add((ChartEntity)o);
                        }
                    }
                }
            }
        }   
        return collection;
    }

    protected void subtractRoamingTitle(RoamingTitle title, Graphics2D g2, Rectangle2D nonTitleArea)
    {
        if(title != null)
        {
            boolean visible = title.isVisible();
            boolean inside = title.isInside();

            if(visible && !inside)
            {
                Title roamingLegendOutside = title.getOutsideTitle();

                subtractTitleArea(roamingLegendOutside, g2, nonTitleArea);
            }
        }       
    }

    @Override
    public void updateFixedDataAreaSize(Graphics2D g2, Rectangle2D chartArea)
    {
        CustomizableXYBasePlot plot = getCustomizablePlot();
        if(plot != null)
        {
            Rectangle2D plotArea = getPlotArea(g2, chartArea);          
            plot.updateFixedDataAreaSize(g2, plotArea);
        }
    }


    @Override
    public Rectangle2D getPlotArea(Graphics2D g2, Rectangle2D chartArea) 
    {       
        Rectangle2D nonTitleArea = new Rectangle2D.Double();
        nonTitleArea.setRect(chartArea);
        this.getPadding().trim(nonTitleArea);

        return subtractAreaOfTitles(g2, nonTitleArea);

    }

    @Override
    protected Rectangle2D subtractAreaOfTitles(Graphics2D g2, Rectangle2D nonTitleArea)
    {
        subtractRoamingTitle(roamingTextTitle, g2, nonTitleArea);
        subtractRoamingTitle(roamingLegend, g2, nonTitleArea);

        Title title = getTitle();
        if (title != null && title.isVisible()) 
        {
            subtractTitleArea(title, g2, nonTitleArea);
        }

        Iterator<?> iterator = this.getSubtitles().iterator();
        while (iterator.hasNext()) 
        {
            Title currentTitle = (Title) iterator.next();
            if (currentTitle.isVisible()) 
            {
                subtractTitleArea(currentTitle, g2, nonTitleArea);
            }
        }
        return nonTitleArea;
    }

    @Override
    protected void expandWidthAreasOfTitles(Graphics2D g2, Size2D chartAreaSize)
    {
        expandWithRoamingTitleArea(roamingTextTitle, g2, chartAreaSize);
        expandWithRoamingLegends(g2, chartAreaSize);

        List<?> subtitles = getSubtitles();
        for(int ti = subtitles.size()-1 ; ti>=0 ; ti--)
        {
            Title currentTitle = (Title)(subtitles.get(ti));
            if(currentTitle.isVisible()){
                expandWithTitleArea(g2,chartAreaSize,currentTitle); 
            }
        } 
        Title title = getTitle();
        if (title != null && title.isVisible()) {
            expandWithTitleArea(g2,chartAreaSize, title);   
        }
    }

    protected void expandWithRoamingTitleArea(RoamingTitle title, Graphics2D g2, Size2D chartAreaSize)
    {
        if(title != null)
        {
            boolean visible = title.isVisible();
            boolean inside = title.isInside();

            if(visible && !inside)
            {
                Title roamingLegendOutside = title.getOutsideTitle();

                expandWithTitleArea(g2, chartAreaSize, roamingLegendOutside);   
            }
        }       
    }

    @Override
    public double getBottomPadding()
    {
        RectangleInsets padding = getPadding();
        return padding.getBottom();
    }

    @Override
    public void setBottomPadding(double bottomPadding)
    {
        RectangleInsets paddingOld = getPadding();
        RectangleInsets padingNew = new RectangleInsets(paddingOld.getTop(), paddingOld.getLeft(), bottomPadding, paddingOld.getRight());
        setPadding(padingNew);
    }

    @Override
    public double getTopPadding()
    {
        RectangleInsets padding = getPadding();
        return padding.getTop();
    }   

    @Override
    public void setTopPadding(double topPadding)
    {
        RectangleInsets paddingOld = getPadding();
        RectangleInsets padingNew = new RectangleInsets(topPadding, paddingOld.getLeft(), paddingOld.getBottom(), paddingOld.getRight());
        setPadding(padingNew);
    }

    @Override
    public double getLeftPadding()
    {
        RectangleInsets padding = getPadding();
        return padding.getLeft();
    }

    @Override
    public void setLeftPadding(double leftPadding)
    {
        RectangleInsets paddingOld = getPadding();
        RectangleInsets padingNew = new RectangleInsets(paddingOld.getTop(), leftPadding, paddingOld.getBottom(), paddingOld.getRight());
        setPadding(padingNew);
    }

    @Override
    public double getRightPadding()
    {
        RectangleInsets padding = getPadding();
        return padding.getRight();
    }

    @Override
    public void setRightPadding(double rightPadding)
    {
        RectangleInsets paddingOld = getPadding();
        RectangleInsets padingNew = new RectangleInsets(paddingOld.getTop(), paddingOld.getLeft(), paddingOld.getBottom(), rightPadding);
        setPadding(padingNew);
    }

    @Override
    public void setPadding(double top, double left, double bottom, double right)
    {
        RectangleInsets padingNew = new RectangleInsets(top, left, bottom, right);
        setPadding(padingNew);
    }

    @Override
    public int hashCode()
    {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object o)
    {
        boolean equals = (this == o);
        return equals;
    }   

    @Override
    public E getCustomizablePlot()
    {
        @SuppressWarnings("unchecked")
        E plot = (E)getPlot();
        return plot;
    }

    protected void setPrefferedChartStyle(Preferences pref)
    { 
        PreferredChartStyle preferredStyle = PreferredChartStyle.getInstance(pref, isAspectRatioLockedByDefault());
        RectangleInsets paddingInsets = preferredStyle.paddingInsets();
        if(paddingInsets !=null)
        {
            setPadding(paddingInsets); 
        }       

        Paint backgroundPaint = preferredStyle.getBackgroundPaint();
        boolean antialias = preferredStyle.isAntialias();

        boolean lockAspectRatio = preferredStyle.isLockAspectRatio();

        setUseFixedChartAreaSize(lockAspectRatio);
        setAntiAlias(antialias);
        setBackgroundPaint(backgroundPaint);
    } 

    protected boolean isAspectRatioLockedByDefault()
    {
        return false;
    }

    public ChartStyleSupplier getSupplier()
    {
        return DEFAULT_STYLE_SUPPLIER;
    }

    public String getKey()
    {
        return styleKey;
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

        CustomizableXYBasePlot plot = getCustomizablePlot();
        plot.setRoamingTitle(roamingTextTitle);

        title.setAutomaticTitles(getAutomaticTitles());

        fireChartChanged();
    }

    public void setRoamingTitleText(String text)
    {        
        if(this.roamingTextTitle == null)
        {
            this.roamingTextTitle = buildNewTitle(text);
            CustomizableXYBasePlot plot = getCustomizablePlot();
            plot.setRoamingTitle(roamingTextTitle);
        }
        else
        {
            roamingTextTitle.setText(text);
        }       

        fireChartChanged();
    }

    @Override
    public RoamingTextTitle buildNewTitle(String text)
    {
        RoamingTextTitle title = new RoamingStandardTextTitle(new TextTitle(text),  "", getPreferences());
        title.setAutomaticTitles(getAutomaticTitles());

        return title;
    }

    @Override
    public void setAutomaticTitles(Map<String, String> automaticTitlesNew)
    {
        super.setAutomaticTitles(automaticTitlesNew);

        if(roamingTextTitle != null)
        {
            roamingTextTitle.setAutomaticTitles(automaticTitlesNew);
        }
    }

    protected boolean renderedDataToRefresh()
    {
        return true;
    }

    @Override
    public Point2D getDataPoint(Point2D java2Dpoint, PlotRenderingInfo info)
    {  
        E plot = getCustomizablePlot();
        return plot.getDataPoint(java2Dpoint, info);
    }   

    public UnitPoint2D getUnitDataPoint(Point2D java2Dpoint, PlotRenderingInfo info)
    {
        E plot = getCustomizablePlot();
        return plot.getDataPointWithUnits(java2Dpoint, info);
    }

    Point2D getCaughtPoint()
    {
        return caughtPoint;
    }

    @Override
    public void chartMouseDragged(CustomChartMouseEvent event) 
    {        
        if(event.getChart() != this)
        {
            return;
        }

        PlotRenderingInfo info = event.getRenderingInfo().getPlotInfo();
        Rectangle2D dataArea = info.getDataArea();          

        Point2D dataPoint = event.getDataPoint();

        if(caughtPoint != null)
        {      
            if(caughtRoamingTitle != null)
            {
                CustomizableXYBasePlot plot = getCustomizablePlot();
                ValueAxis domainAxis = plot.getDomainAxis();
                ValueAxis rangeAxis = plot.getRangeAxis();
                caughtRoamingTitle.move(caughtPoint, dataPoint, getCustomizablePlot(), dataArea, domainAxis, rangeAxis);    
            }

            this.caughtPoint = dataPoint;
        }           
    }

    @Override
    public void chartMousePressed(CustomChartMouseEvent event)
    {
        if(event.getChart() != this)
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();
        this.caughtPoint = dataPoint;

        ChartEntity entity = event.getEntity();

        if(entity instanceof RoamingTitleEntity)
        { 
            RoamingTitleEntity roamingTitleEntity = (RoamingTitleEntity)entity;
            this.caughtRoamingTitle = roamingTitleEntity.getRoamingTitle();
        }
        else
        {
            this.caughtRoamingTitle = null;
        }
    }

    @Override
    public void chartMouseReleased(CustomChartMouseEvent event)
    {
        if(event.getChart() == this)
        {
            this.caughtPoint = null;
            this.caughtRoamingTitle = null;
        }
    }

    @Override
    public void chartMouseClicked(CustomChartMouseEvent event) 
    {
        if(chartSupervisor == null)
        {
            return;
        }

        ChartEntity entity = event.getEntity();

        handleAxisClickEvent(event, entity);
        handleAxisCategoryEntity(event, entity);
        handleLegendItemClickEvent(event, entity);
        handleRoamingLegendItemClickEvent(event, entity);
        handleRoamingTitleClickEvent(event, entity);
        handleTitleClickEvent(event, entity);
    }


    private void handleTitleClickEvent(CustomChartMouseEvent event, ChartEntity entity)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        boolean isTitleEntity = entity instanceof TitleEntity;
        if(!isTitleEntity)
        {
            return;
        }

        Title title = ((TitleEntity)entity).getTitle();
        if(title instanceof TextTitle)
        {
            chartSupervisor.doEditTitleProperties();
            event.setConsumed(CHART_EDITION, true);
        }

    }

    private void handleRoamingTitleClickEvent(CustomChartMouseEvent event, ChartEntity entity)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        boolean isRoamingTitleEntity = entity instanceof RoamingTitleEntity;

        if(!isRoamingTitleEntity)
        {
            return;
        }

        RoamingTitle title = ((RoamingTitleEntity)entity).getRoamingTitle();
        if(title instanceof RoamingTextTitle)
        {
            chartSupervisor.doEditTitleProperties();
            event.setConsumed(CHART_EDITION, true);
        }
        else if(title instanceof RoamingStandardTitleLegend)
        {
            chartSupervisor.doEditLegendProperties(((RoamingStandardTitleLegend) title).getName());
            event.setConsumed(CHART_EDITION, true);
        }
    }

    private void handleAxisCategoryEntity(CustomChartMouseEvent event, ChartEntity entity)
    {
        boolean isCategoryEntity = entity instanceof NamedAxisCategoryEntity;
        if(!isCategoryEntity)
        {
            return;
        }

        NamedAxisCategoryEntity categoryEntity = (NamedAxisCategoryEntity)entity;
        String label = categoryEntity.getLabel();
        String category = categoryEntity.getCategory();
        CustomizableNamedNumberAxis axis = categoryEntity.getAxis();

        attemptChangeCategoryLabel(category, label, axis);
    }

    private void handleRoamingLegendItemClickEvent(CustomChartMouseEvent event,
            ChartEntity entity)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        boolean isRoamingLegendItemEntity = entity instanceof RoamingLegendItemEntity;

        if(!isRoamingLegendItemEntity)
        {
            return;
        }

        LegendItemEntity itemEntity = ((RoamingLegendItemEntity)entity).getOriginalEntity();
        Comparable<?> seriesKey = itemEntity.getSeriesKey();
        attemptChangeSeriesKey(seriesKey);

        event.setConsumed(CHART_EDITION, true);
    }

    private void handleLegendItemClickEvent(CustomChartMouseEvent event, ChartEntity entity)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        boolean isLegendItemEntity = entity instanceof LegendItemEntity;

        if(!isLegendItemEntity)
        {
            return;
        }

        Comparable<?> seriesKey = ((LegendItemEntity)entity).getSeriesKey();
        attemptChangeSeriesKey(seriesKey);

        event.setConsumed(CHART_EDITION, true);
    }

    private void handleAxisClickEvent(CustomChartMouseEvent event, ChartEntity entity)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        boolean isAxisEntity = entity instanceof AxisEntity;

        if(!isAxisEntity)
        {
            return;
        }

        Axis axis = ((AxisEntity)entity).getAxis();
        CustomizableXYBasePlot plot = getCustomizablePlot();

        if(!event.isConsumed(CHART_EDITION))
        {
            for(int i = 0; i<plot.getDomainAxisCount(); i++)
            {
                Axis domainAxis = plot.getDomainAxis(i);
                if(axis == domainAxis)
                {
                    chartSupervisor.doEditDomainAxisProperties(i);
                    event.setConsumed(CHART_EDITION, true);
                    break;
                }
            }
        }

        if(!event.isConsumed(CHART_EDITION))
        {
            for(int i = 0; i<plot.getRangeAxisCount(); i++)
            {
                Axis domainAxis = plot.getRangeAxis(i);
                if(axis == domainAxis)
                {
                    chartSupervisor.doEditRangeAxisProperties(i);
                    event.setConsumed(CHART_EDITION, true);
                    break;
                }
            }
        }

        if(!event.isConsumed(CHART_EDITION))
        {
            for(int i = 0; i<plot.getDepthAxisCount(); i++)
            {
                Axis domainAxis = plot.getDepthAxis(i);
                if(axis == domainAxis)
                {
                    chartSupervisor.doEditDepthAxisProperties(i);
                    event.setConsumed(CHART_EDITION, true);
                    break;
                }
            }
        }
    }

    private void attemptChangeCategoryLabel(String category, String label, CustomizableNamedNumberAxis axis)
    {   
        CustomizableXYBasePlot plot = getCustomizablePlot();
        plot.attemptChangeCategoryLabel(category, label, axis);
    } 

    private void attemptChangeSeriesKey(Comparable<?> seriesKey)
    {   
        CustomizableXYBasePlot plot = getCustomizablePlot();
        plot.attemptChangeSeriesKey(seriesKey);
    }

    @Override
    public void chartMouseMoved(CustomChartMouseEvent event) 
    {
        if(event.getChart() == this)
        {
            Point2D java2DPoint = event.getJava2DPoint();
            PlotRenderingInfo info = event.getRenderingInfo().getPlotInfo();
            Rectangle2D dataArea = info.getDataArea();          

            Point2D dataPoint = event.getDataPoint();

            if(dataPoint == null)
            {
                return;
            }
            boolean insideDataArea = dataArea.contains(java2DPoint);

            if(!insideDataArea)
            {
                chartSupervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                return;
            }            
        }   
    }

    @Override
    public void roamingTitleChanged(RoamingTitleChangeEvent event)
    {
        fireChartChanged();
    }

    @Override
    public boolean isDomainZoomable()
    {
        CustomizableXYBasePlot plot = getCustomizablePlot();
        boolean zoomable = (!isChartElementCaught()) && (!isComplexChartElementUnderConstruction()) && plot.isDomainZoomable();

        return zoomable;
    }

    @Override
    public boolean isDomainPannable()
    {
        CustomizableXYBasePlot plot = getCustomizablePlot();
        boolean zoomable = (!isChartElementCaught()) && plot.isDomainPannable();

        return zoomable;
    }

    @Override
    public boolean isRangeZoomable()
    {
        CustomizableXYBasePlot plot = getCustomizablePlot();
        boolean zoomable =  (!isChartElementCaught()) && (!isComplexChartElementUnderConstruction()) && plot.isRangeZoomable();

        return zoomable;
    }

    @Override
    public boolean isRangePannable()
    {
        CustomizableXYBasePlot plot = getCustomizablePlot();
        boolean zoomable =  (!isChartElementCaught()) && plot.isRangePannable();

        return zoomable;
    }


    protected boolean isChartElementCaught()
    {
        boolean isRoamingTitleCaught = (caughtRoamingTitle != null);

        return isRoamingTitleCaught;
    }  

    protected boolean isComplexChartElementUnderConstruction()
    {
        return false;
    }

    @Override
    public Preferences getPlotSpecificPreferences()
    {
        return getCustomizablePlot().getPreferences();
    }
}

