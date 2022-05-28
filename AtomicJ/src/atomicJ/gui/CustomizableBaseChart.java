
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.JFreeChartEntity;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.entity.TitleEntity;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.ui.Align;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.Size2D;

import atomicJ.gui.measurements.DistanceLineMeasurement;


public abstract class CustomizableBaseChart<E extends CustomizableCategoryPlot> extends CustomizableChart implements PreferencesSource, RoamingTitleChangeListener
{	
    private static final long serialVersionUID = 1L;

    private MouseInputMode mode = NORMAL;

    private DistanceLineMeasurement caughtDistanceMeasurement;

    private Point2D caughtPoint;
    private RoamingTitle caughtRoamingTitle;

    private RoamingLegend roamingLegend;
    private RoamingTextTitle roamingTextTitle;

    private ChartSupervisor chartSupervisor;

    public static final String CHART_EDITION = "CHART_EDITION";

    public CustomizableBaseChart(CustomizableXYPlot plot) 
    {
        super(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        setUseFixedChartAreaSize(false);//the chart should be created using this fixed size
        setUseFixedPlotAreaSize(true);//the fixed chart size should be calculated based on a desired plot area size instead of the "static" preferred size of the chart	
    }

    public LegendTitle createDefaultLegend()
    {       
        LegendTitle legend = new LegendTitle(getPlot());
        legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        legend.setFrame(new LineBorder());
        legend.setBackgroundPaint(Color.white);
        legend.setPosition(RectangleEdge.BOTTOM);

        return legend;
    }

    public boolean isDistanceMeasurementMode()
    {
        return mode.isMeasurement();    
    }

    public boolean isNormalMode()
    {
        return mode.equals(NORMAL);
    }

    public boolean isROIMode()
    {
        return mode.isROI();
    }

    public boolean isFreeROIMode()
    {
        return mode.equals(FREE_HAND_ROI);
    }

    public boolean isProfileMode()
    {
        return getMode().isProfile();
    }

    public boolean isInsertMapMarkerMode()
    {
        return getMode().equals(INSERT_MAP_MARKER);
    }

    public boolean isInsertDomainValueMarkerMode()
    {
        return getMode().equals(INSERT_DOMAIN_MARKER);
    }

    public boolean isInsertRangeMarkerMode()
    {
        return getMode().equals(INSERT_RANGE_MARKER);
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



    //// INSERT HORIZONTAL MARKER

    protected Range getRangeRange(Point2D  dataPoint, double axisFraction)
    {
        CategoryPlot plot = getCustomizablePlot();
        ValueAxis domainAxis = plot.getRangeAxis();

        double yRangeLength = domainAxis.getRange().getLength();
        double yRadius = axisFraction*yRangeLength;

        double center = dataPoint.getY();
        Range range = new Range(center - yRadius, center + yRadius);

        return range;
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



    /*This method is taken directly from the source code of JFreeChart, written by David Gilbert and
     * collaborators. I just added a call to a method drawRoamingLegend()
     */
    @Override
    public void draw(Graphics2D g2,
            Rectangle2D chartArea, Point2D anchor,
            ChartRenderingInfo info) {

        notifyListeners(new ChartProgressEvent(this, this,
                ChartProgressEvent.DRAWING_STARTED, 0));

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

        // draw the plot (axes and data visualisation)
        PlotRenderingInfo plotInfo = null;
        if (info != null) {
            plotInfo = info.getPlotInfo();
        }
        getPlot().draw(g2, plotArea, anchor, null, plotInfo);

        g2.setClip(savedClip);

        notifyListeners(new ChartProgressEvent(this, this,
                ChartProgressEvent.DRAWING_FINISHED, 100));
    }

    public EntityCollection drawRoamingTextTitle(Graphics2D g2, Rectangle2D nonTitleArea, boolean entities)
    {   	
        return drawRoamingTitle(roamingTextTitle, g2, nonTitleArea, entities);	
    }

    public EntityCollection drawRoamingLegends(Graphics2D g2, Rectangle2D nonTitleArea, boolean entities)
    {   
        return drawRoamingTitle(roamingLegend, g2, nonTitleArea, entities);	
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
                    collection = collectionForTitle;
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
        CustomizableCategoryPlot plot = getCustomizablePlot();
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

    protected void expandWithRoamingLegends(Graphics2D g2, Size2D chartAreaSize)
    {
        expandWithRoamingTitleArea(roamingLegend, g2, chartAreaSize);
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
    public boolean equals(Object other)
    {
        if(this == other)
        {
            return true;
        }
        return false;
    }	

    @Override
    public CustomizableCategoryPlot getCustomizablePlot()
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

    public abstract ChartStyleSupplier getSupplier();
    public abstract String getKey();

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
        CategoryPlot plot = getCustomizablePlot();
        Rectangle2D dataArea = info.getDataArea();			        
        ValueAxis rangeAxis = plot.getRangeAxis();

        PlotOrientation orientation = plot.getOrientation();
        boolean isVertical = (orientation == PlotOrientation.VERTICAL);

        RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
        double dataX = Double.NaN;
        double dataY = isVertical ? rangeAxis.java2DToValue(java2Dpoint.getY(), dataArea, rangeAxisEdge)
                : rangeAxis.java2DToValue(java2Dpoint.getX(), dataArea, rangeAxisEdge);

        Point2D.Double point = new Point2D.Double(dataX, dataY);
        return point;
    }	

    @Override
    public void chartMouseDragged(CustomChartMouseEvent event) 
    {
        if(event.getChart() == this)
        {
            Point2D java2DPoint = event.getJava2DPoint();
            PlotRenderingInfo info = event.getRenderingInfo().getPlotInfo();

            Point2D dataPoint = getDataPoint(java2DPoint, info);

            if(caughtPoint != null)
            {			
                this.caughtPoint = dataPoint;
            }			
        }	
    }

    @Override
    public void chartMousePressed(CustomChartMouseEvent event)
    {
        if(event.getChart() == this)
        {
            Point2D java2DPoint = event.getJava2DPoint();
            PlotRenderingInfo info = event.getRenderingInfo().getPlotInfo();
            Point2D dataPoint = getDataPoint(java2DPoint, info);

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


        if(entity instanceof AxisEntity)
        {
            handleAxisClickEvent(event, (AxisEntity) entity);
        }   
        else if(entity instanceof RoamingTitleEntity )
        { 
            handleRoamingTitleClickEvent(event, (RoamingTitleEntity) entity);
        }
        else if(entity instanceof TitleEntity)
        {
            handleTitleClickEvent(event, (TitleEntity) entity);
        }	      
    }

    private void handleTitleClickEvent(CustomChartMouseEvent event, TitleEntity entity)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        Title title = entity.getTitle();
        if(title instanceof TextTitle)
        {
            chartSupervisor.doEditTitleProperties();
            event.setConsumed(CHART_EDITION, true);
        }
    }

    private void handleRoamingTitleClickEvent(CustomChartMouseEvent event, RoamingTitleEntity entity)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        RoamingTitle title = entity.getRoamingTitle();
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


    private void handleAxisClickEvent(CustomChartMouseEvent event, AxisEntity entity)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        Axis axis = entity.getAxis();
        CustomizableCategoryPlot plot = getCustomizablePlot();

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

    @Override
    public void chartMouseMoved(CustomChartMouseEvent event) 
    {
        if(event.getChart() == this)
        {
            Point2D java2DPoint = event.getJava2DPoint();
            PlotRenderingInfo info = event.getRenderingInfo().getPlotInfo();
            Rectangle2D dataArea = info.getDataArea();          

            Point2D dataPoint = getDataPoint(java2DPoint, info);

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
        return false;
    }

    @Override
    public boolean isRangeZoomable()
    {
        return false;
    }

    protected boolean isChartElementCaught()
    {
        boolean isRoamingTitleCaught = (caughtRoamingTitle != null);
        boolean isDistanceMeasurementCaught = (caughtDistanceMeasurement != null);

        return isRoamingTitleCaught || isDistanceMeasurementCaught;
    }  

    @Override
    public Preferences getPlotSpecificPreferences()
    {
        return getCustomizablePlot().getPreferences();
    }
}

