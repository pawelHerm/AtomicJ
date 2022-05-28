
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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.JFreeChartEntity;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.ui.Align;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleInsets;

import atomicJ.data.Datasets;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.profile.ProfileStyle;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIStyle;
import atomicJ.gui.stack.StackModel;


public class Channel2DChart <E  extends Channel2DPlot> extends ChannelChart<E> implements CustomChartMouseListener, CustomChartMouseWheelListener
{
    private static final long serialVersionUID = 1L;

    public static final String ROIS_AVAILABLE = "ROIS_AVAILABLE";
    public static final String PROFILES_AVAILABLE = "PROFILES_AVAILABLE";
    public static final String CURRENT_PROFILE = "CURRENT_PROFILE";
    public static final String NEW_PROFILE_ADDED = "NEW_PROFILE_ADDED";

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final PropertyChangeListener listener = new LocalPropertyChangeListener();

    private final BasicMouseInputResponse basicResponse = new BasicMouseInputResponse();

    //////////////////////PROFILES/////////////////////////////////////////

    final ProfileStyle profileStyle;
    private final ProfileManager profileManager = new ProfileManager(this);

    /////////////////////ROIS/////////////////////////////////////////////

    boolean roiHoleMode;
    final ROIStyle roisStyle;
    private final ROIManager roiResponse = new ROIManager(this);

    ///////////////////////MAP MARKERS/////////////////////////////////////

    final MapMarkerStyle mapMarkerStyle;
    private final MapMarkerManager mapMarkerManager = new MapMarkerManager(this);

    ///////////////////////VALUE MARKERS/////////////////////////////////////


    private final ValueMarkerManager valueMarkerResponse = new ValueMarkerManager(this);

    ///////////////////// TOOLS ////////////////////////////////////////

    private final ToolManager toolResponse = new ToolManager(this);

    ////////////////////////////////////////////////////////

    Point2D caughtPoint;

    Channel2DSupervisor supervisor;

    //movie chart
    private StackModel<?> stackModel;
    ////////////


    public Channel2DChart(E plot, String key)
    {
        this(plot, key, true);
    }	

    public Channel2DChart(E plot, String key, boolean addLegend)
    {
        super(plot, key);

        plot.addPropertyChangeListener(listener);

        Preferences pref = getPreferences();
        this.roisStyle = new ROIStyle(pref.node("Rois"), plot.getDefaultAnnotationPaint());		
        this.profileStyle = new ProfileStyle(pref.node("Profiles"), plot.getDefaultAnnotationPaint());
        this.mapMarkerStyle = new MapMarkerStyle(pref.node("MapMarkers"), plot.getDefaultAnnotationPaint());

        if(addLegend)
        {
            Preferences depthPref = plot.getPreferences().node(AxisType.DEPTH.toString());
            CustomizableNumberAxis depthAxis =  new CustomizableNumberAxis(((ProcessableXYZDataset)plot.getDataset(0)).getZQuantity(), depthPref);        

            PaintScaleSource source = (PaintScaleSource)plot.getRenderer(0);
            RoamingLegend legend = buildRoamingLegend(source, depthAxis);
            setRoamingLegend(legend);
        }
    }	

    @Override
    public void setUseFixedChartAreaSize(boolean flag)
    {		
        super.setUseFixedChartAreaSize(flag);

        if(supervisor != null)
        {
            supervisor.notifyAboutAspectRatioLock();
        }
    }

    public boolean areROISamplesNeeded()
    {
        Channel2DPlot plot = getCustomizablePlot();
        boolean needed = plot.areROISamplesNeeded();

        return needed;
    }

    public GradientPaintReceiver getGradientPaintReceiver()
    {
        Channel2DPlot plot = getCustomizablePlot();
        GradientPaintReceiver receiver = plot.getGradientPaintReceiver();

        return receiver;
    }

    @Override
    protected boolean isAspectRatioLockedByDefault()
    {
        Channel2DPlot plot = getCustomizablePlot();
        boolean locked = (plot != null) ? plot.isAspectRatioLockedByDefault() : false;

        return locked;
    }

    @Override
    public boolean isPopupDisplayable(Point2D dataPoint)
    {
        Rectangle2D dataArea = getDataSquare(dataPoint, 0.005);  

        boolean clearOfRoiUnderConstruction = !roiResponse.isRightClickReserved(dataArea, dataPoint);
        boolean clearOfFinishedProfile =  !profileManager.isRightClickReserved(dataArea, dataPoint);
        boolean clearOfMarkers = !valueMarkerResponse.isRightClickReserved(dataArea, dataPoint);
        boolean clearOfTools = !toolResponse.isRightClickReserved(dataArea, dataPoint);

        boolean displayable = super.isPopupDisplayable(dataPoint) && clearOfRoiUnderConstruction && clearOfFinishedProfile && clearOfMarkers && clearOfTools;
        return displayable;
    }


    public StackModel<?> getStackModel()
    {
        return stackModel;
    }

    public void setStackModel(StackModel<?> stackModel)
    {
        this.stackModel = stackModel;
    }

    @Override
    public String getDataTooltipText(Point2D java2DPoint, ChartRenderingInfo info)
    {
        PlotRenderingInfo plotInfo = info.getPlotInfo();
        Rectangle2D dataArea = plotInfo.getDataArea();

        String tooltipText = null;
        ChartEntity entity = getChartEntity(java2DPoint, info);

        if(entity != null)
        {
            tooltipText = entity.getToolTipText();
        }

        boolean lightwightEntity = (entity instanceof LightweightXYItemEntity);

        if(tooltipText == null && dataArea.contains(java2DPoint))
        {            
            E plot = getCustomizablePlot();
            tooltipText = lightwightEntity ? plot.getDatasetTooltip(java2DPoint, (LightweightXYItemEntity) entity, plotInfo) : plot.getRegularDatasetTooltip(java2DPoint, plotInfo);
        }

        return tooltipText;
    }

    public List<String> getValueLabelTypes()
    {
        E plot = getCustomizablePlot();
        return plot.getValueLabelTypes();
    }

    public Map<String, String> getValueLabels(Point2D dataPoint)
    {
        E plot = getCustomizablePlot();
        return plot.getValueLabels(dataPoint);
    }

    public PrefixedUnit getZDisplayedUnit()
    {
        E plot = getCustomizablePlot();
        return plot.getZAxisDisplayedUnit();
    }

    public ProfileStyle getProfileStyle()
    {
        return profileStyle;
    }

    public ROIStyle getROIStyle()
    {
        return roisStyle;
    }

    public MapMarkerStyle getMapMarkerStyle()
    {
        return mapMarkerStyle;
    }

    public void setDensitySupervisor(Channel2DSupervisor supervisor)
    {
        this.supervisor = supervisor;
    }

    public RoamingLegend buildRoamingLegend(PaintScaleSource paintScaleScource, CustomizableNumberAxis depthAxis)
    {
        GradientPaintScale ps = paintScaleScource.getPaintScale();              
        ColorGradientLegend paintScaleLegend = new ColorGradientLegend(ps, depthAxis);       
        paintScaleScource.setColorGradientLegend(paintScaleLegend);

        PreferredRoamingPaintScaleLegendStyle legendStyle = PreferredRoamingPaintScaleLegendStyle.getInstance(getPreferences(), getKey());
        RoamingLegend legend = new RoamingColorGradientLegend("Gradient strip", paintScaleLegend, legendStyle);

        return legend;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChange(String property, Object oldValue, Object newValue)
    {
        propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
    }

    public void setROIHoleMode(boolean roiHoleMode)
    {
        this.roiHoleMode = roiHoleMode;
    }

    @Override
    public void setMode(MouseInputMode mode)
    {
        super.setMode(mode);

        if(!isROIMode())
        {
            roiResponse.cancelRoiConstruction();
        }

        setProfilesVisible(isProfileMode());
        setRoisVisible(isROIMode());
    }



    ///////////////////////////////////////////////////////////////////
    ///           PROFILES     ////
    //////////////////////////////////////////////////////////////////


    public void removeProfile(Profile profile)
    {
        profileManager.removeProfile(profile);
    }

    public void setProfiles(Map<Object, Profile> profilesNew)
    {		
        profileManager.setProfiles(profilesNew);
    }

    public void addOrReplaceProfile(Profile profile)
    {
        profileManager.addOrReplaceProfile(profile);
    }

    public void setProfilesVisible(boolean visibleNew)
    {
        profileManager.setProfilesVisible(visibleNew);
    }

    public void setProfileKnobPositions(Object profileKey, List<Double> knobrPositions)
    {
        profileManager.setProfileKnobPositions(profileKey, knobrPositions);
    }

    public void moveProfileKnob(Object profileKey, int knobIndex, double knobNewPosition)
    {
        profileManager.moveProfileKnob(profileKey, knobIndex, knobNewPosition);
    }

    public void addProfileKnob(Object profileKey, double knobPosition)
    {
        profileManager.addProfileKnob(profileKey, knobPosition);
    }

    public void removeProfileKnob(Object profileKey, double knobPosition)
    {
        profileManager.removeProfileKnob(profileKey, knobPosition);
    }   

    public int getProfileCount()
    {
        return profileManager.getProfileCount();
    }

    public int getCurrentProfileIndex()
    {
        return profileManager.getCurrentProfileIndex();
    }

    //////////////////////VALUE MARKERS/////////////////////////////////////////////////////////////////

    public void addDomainValueMarker(CustomizableValueMarker marker, Layer layer)
    {
        valueMarkerResponse.addDomainValueMarker(marker, layer);
    }

    public void removeDomainValueMarker(CustomizableValueMarker marker)
    {
        valueMarkerResponse.removeDomainValueMarker(marker);
    }

    public void addRangeValueMarker(CustomizableValueMarker marker, Layer layer)
    {
        valueMarkerResponse.addRangeValueMarker(marker, layer);
    }

    public void removeRangeValueMarker(CustomizableValueMarker marker)
    {
        valueMarkerResponse.removeRangeValueMarker(marker);
    }

    public void addValueMarkerKnob(Object markerKey, double knobPosition)
    {
        valueMarkerResponse.addValueMarkerKnob(markerKey, knobPosition);
    }

    public void moveValueMarkerKnob(Object markerKey, int knobIndex, double knobNewPosition)
    {
        valueMarkerResponse.moveValueMarkerKnob(markerKey, knobIndex, knobNewPosition);
    }

    public void removeValueMarkerKnob(Object markerKey, int markerIndex)
    {
        valueMarkerResponse.removeValueMarkerKnob(markerKey, markerIndex);
    }

    ///////////////////////////////////////////////////////////////////
    ///           MAP MARKERS     ////
    //////////////////////////////////////////////////////////////////

    public void removeMapMarker(MapMarker mapMarker)
    {
        mapMarkerManager.removeMapMarker(mapMarker);
    }

    public void setMapMarkers(Map<Object, MapMarker> mapMarkersNew)
    {       
        mapMarkerManager.setMapMarker(mapMarkersNew);
    }

    public void addOrReplaceMapMarker(MapMarker mapMarker)
    {
        mapMarkerManager.addOrReplaceMapMarker(mapMarker);
    }

    public int getCurrentMapMarkerIndex()
    {
        return mapMarkerManager.getCurrentMapMarkerIndex();
    }

    public int getMapMarkerCount()
    {
        return mapMarkerManager.getMapMarkerCount();
    }

    ///////////////////BASIC RESPONSES TO MOUSE EVENTS ///////////////////////


    private class BasicMouseInputResponse implements MouseInputResponse
    {
        @Override
        public void mousePressed(CustomChartMouseEvent event) 
        {
            Point2D dataPoint = event.getDataPoint();
            caughtPoint = dataPoint;    
        }

        @Override
        public void mouseReleased(CustomChartMouseEvent event) 
        {
            caughtPoint = null;  
        }

        @Override
        public void mouseDragged(CustomChartMouseEvent event) 
        {
            caughtPoint = event.getDataPoint();           
        }

        @Override
        public void mouseMoved(CustomChartMouseEvent event) 
        {
            boolean cursorNotChanged = !event.isConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED);

            if(cursorNotChanged && isNormalMode(MouseInputType.MOVED))
            {
                supervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); 
            }
        }

        @Override
        public void mouseClicked(CustomChartMouseEvent event) {            
        }

        @Override
        public boolean isChartElementCaught() 
        {
            return false;
        }

        @Override
        public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint) 
        {
            return false;
        }      
    }

    ////////////////////////ROI RESPONSES TO MOUSE EVENTS////////////////////////////

    public void setRoisVisible(boolean visibleNew)
    {
        roiResponse.setRoisVisible(visibleNew);
    }	

    public void addOrReplaceROI(ROIDrawable roi)
    {
        addOrReplaceROI(roi, roisStyle);
    }

    public void addOrReplaceROI(ROIDrawable roi, ROIStyle customStyle)
    {
        roiResponse.addOrReplaceROI(roi, customStyle);
    }

    public void removeROI(ROIDrawable roi)
    {
        roiResponse.removeROI(roi);
    }

    public void setROIs(Map<Object, ROIDrawable> roisNew)
    {
        setROIs(roisNew, roisStyle);
    }

    public void setROIs(Map<Object, ROIDrawable> roisNew, ROIStyle customStyle)
    {
        roiResponse.setROIs(roisNew, customStyle);
    }

    public void changeROILabel(Object roiKey, String labelOld, String labelNew)
    {       
        roiResponse.changeROILabel(roiKey, labelOld, labelNew);          
    }

    public int getCurrentROIIndex()
    {
        return roiResponse.getCurrentROIIndex();
    }

    ////////////////////////////PAINTS SCALE LEGEND RESPONSES TO MOUSE EEVENTS /////////////////////////////////////



    private void paintScaleLegendResponseToMouseClick(CustomChartMouseEvent event)
    {        
        if(!event.isConsumed(CustomizableXYBaseChart.CHART_EDITION) && event.isLeft())
        {          
            Point2D java2DPoint = event.getJava2DPoint();
            CustomizableXYPlot plot = getCustomizablePlot();
            int n = plot.getRendererCount();

            List<ColorGradientReceiver> clickedGradientReceivers = new ArrayList<>();

            for(int i = 0; i<n; i++)
            {
                ChannelRenderer renderer = plot.getRenderer(i);

                if(renderer instanceof PaintScaleSource)
                {
                    PaintScaleSource paintScaleSource = (PaintScaleSource)renderer;
                    ColorGradientLegend legend = paintScaleSource.getColorGradientLegend();

                    if(legend != null)
                    {
                        if(legend.isStripClicked(java2DPoint))
                        {                           
                            clickedGradientReceivers.add(paintScaleSource);
                        }
                    }
                }
            }

            if(clickedGradientReceivers.size()>0 && (!event.isConsumed(CustomizableXYBaseChart.CHART_EDITION)))
            {
                showGradientChooser(clickedGradientReceivers);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected boolean isChartElementCaught()
    {
        boolean superCaught = super.isChartElementCaught();

        boolean isBasicElementCaught = basicResponse.isChartElementCaught();
        boolean isRoiCaught = roiResponse.isChartElementCaught();
        boolean isProfileCaught = profileManager.isChartElementCaught();
        boolean isValueMarkerCaught = valueMarkerResponse.isChartElementCaught();
        boolean isMapMarkerCaught = mapMarkerManager.isChartElementCaught();
        boolean isSomethingCaughtByTool = toolResponse.isChartElementCaught();

        boolean chartElementCaught = superCaught || isBasicElementCaught|| isRoiCaught || isProfileCaught
                || isValueMarkerCaught || isMapMarkerCaught || isSomethingCaughtByTool;

        return chartElementCaught;
    }

    @Override
    protected boolean isComplexChartElementUnderConstruction()
    {
        boolean complexUnderConstruction = super.isComplexChartElementUnderConstruction();

        complexUnderConstruction = complexUnderConstruction || roiResponse.isComplexElementUnderConstruction()
                || profileManager.isComplexElementUnderConstruction() || toolResponse.isComplexElementUnderConstruction();


        return complexUnderConstruction;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////// 
    ///////////////////////////// RESPONSES TO CHARTMOUSEEVENTS //////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void chartMousePressed(CustomChartMouseEvent event)
    {
        super.chartMousePressed(event);

        boolean isInside = event.isInsideDataArea();
        if(!isInside)
        {   
            return;
        }	

        valueMarkerResponse.mousePressed(event);
        profileManager.mousePressed(event);
        roiResponse.mousePressed(event);       
        mapMarkerManager.mousePressed(event);
        toolResponse.mousePressed(event);
        basicResponse.mousePressed(event);
    }

    @Override
    public void chartMouseReleased(CustomChartMouseEvent event)
    {
        super.chartMouseReleased(event);

        valueMarkerResponse.mouseReleased(event);
        roiResponse.mouseReleased(event);
        profileManager.mouseReleased(event);		       
        mapMarkerManager.mouseReleased(event);
        toolResponse.mouseReleased(event);
        basicResponse.mouseReleased(event);
    }

    @Override
    public void chartMouseDragged(CustomChartMouseEvent event)
    {	
        super.chartMouseDragged(event);

        if(event.getChart() != this)
        {
            return;
        }

        boolean inside = event.isInsideDataArea();

        if(!inside)
        {
            return;
        }

        //basicResponse must be the last called, because of caughtPoint issues

        valueMarkerResponse.mouseDragged(event);
        roiResponse.mouseDragged(event);      
        profileManager.mouseDragged(event);           
        mapMarkerManager.mouseDragged(event);
        toolResponse.mouseDragged(event);
        basicResponse.mouseDragged(event); 
    }

    @Override
    public void chartMouseClicked(CustomChartMouseEvent event) 
    {	
        super.chartMouseClicked(event);

        if(event.getChart() != this)
        {
            return;
        }

        paintScaleLegendResponseToMouseClick(event);                   

        boolean isInsideDataArea = event.isInsideDataArea();

        if(!isInsideDataArea)
        {
            return;
        }

        valueMarkerResponse.mouseClicked(event);
        roiResponse.mouseClicked(event);                                        
        profileManager.mouseClicked(event);
        mapMarkerManager.mouseClicked(event);
        toolResponse.mouseClicked(event);
        basicResponse.mouseClicked(event);
    }

    @Override
    public void chartMouseMoved(CustomChartMouseEvent event) 
    {
        super.chartMouseMoved(event);

        if(event.getChart() != this)
        {
            return;
        }

        boolean insideDataArea = event.isInsideDataArea();

        if(!insideDataArea)
        {
            supervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        }

        valueMarkerResponse.mouseMoved(event);
        mapMarkerManager.mouseMoved(event);
        profileManager.mouseMoved(event);                                    
        roiResponse.mouseMoved(event);                     
        toolResponse.mouseMoved(event);
        basicResponse.mouseMoved(event);
    }

    @Override
    public void chartMouseWheelMoved(CustomChartMouseWheelEvent event)
    {
        if(event.getChart() != this)
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();

        MouseWheelEvent trigger = event.getTrigger();                   

        CustomizableXYPlot plot = getCustomizablePlot();

        int n = plot.getRendererCount();

        for(int i = 0; i<n; i++)
        {
            ChannelRenderer renderer = plot.getRenderer(i);

            if(renderer instanceof PaintScaleSource)
            {
                PaintScaleSource paintScaleSource = (PaintScaleSource)renderer;
                paintScaleSource.requestPaintScaleChange(java2DPoint, trigger.getWheelRotation()/200.);
            }
        }                           							            
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    private void showGradientChooser(List<ColorGradientReceiver> gradientReceivers)
    {
        supervisor.showGradientChooser(gradientReceivers);
    }

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! MULTIPLE DATASET PLOT

    public boolean isMovie()
    {
        E plot = getCustomizablePlot();
        return plot.isMovie();
    }

    public void setFrameTitle(double value, PrefixedUnit unit)
    {     
        if(isMovie())
        {
            RoamingTextTitle title = getRoamingTitle();

            if(title == null)
            {
                title = buildNewTitle("");
            }
            if(title instanceof RoamingStackTextTitle)
            {
                RoamingStackTextTitle stackTitle = (RoamingStackTextTitle)title;
                stackTitle.setFrameTitleText(value, unit);
            }

            setRoamingTitle(title);
        }
    }

    @Override
    public RoamingTextTitle buildNewTitle(String text)
    {        
        if(isMovie())
        {
            RoamingTextTitle title = new RoamingStandardStackTextTitle(new TextTitle(text),  "", getPreferences());
            return title;
        }
        else
        {
            return super.buildNewTitle(text);
        }
    }

    public int getFrameCount()
    {
        E plot = getCustomizablePlot();
        return plot.getFrameCount();
    }

    public void showNextFrame()
    {
        E plot = getCustomizablePlot();
        plot.showNextFrame();
    }

    public void showPreviousFrame()
    {
        E plot = getCustomizablePlot();
        plot.showPreviousFrame();
    }

    public void showFrame(int index)
    {
        E plot = getCustomizablePlot();
        plot.showFrame(index);
    }

    /**
     * Draws the chart on a Java 2D graphics device (such as the screen or a
     * printer).
     * <P>
     * This method is the focus of the entire JFreeChart library.
     *
     * @param g2  the graphics device.
     * @param chartArea  the area within which the chart should be drawn.
     * @param anchor  the anchor point (in Java2D space) for the chart
     *                (<code>null</code> permitted).
     * @param info  records info about the drawing (null means collect no info).
     */

    //This method is taken from the source code of JFreeChart, written by D. Gilbert and other contributors.
    //I changed it, so that now it is possible to draw one frame of a movie
    public void draw(Graphics2D g2, Rectangle2D chartArea, Point2D anchor,
            ChartRenderingInfo info, int frame) 
    {
        notifyListeners(new ChartProgressEvent(this, this,
                ChartProgressEvent.DRAWING_STARTED, 0));

        EntityCollection entities = null;
        // record the chart area, if info is requested...
        if (info != null) {
            info.clear();
            info.setChartArea(chartArea);
            entities = info.getEntityCollection();
        }
        if (entities != null) 
        {
            entities.add(new JFreeChartEntity((Rectangle2D) chartArea.clone(),
                    this));
        }

        // ensure no drawing occurs outside chart area...
        Shape savedClip = g2.getClip();
        g2.clip(chartArea);

        g2.addRenderingHints(this.getRenderingHints());

        if (this.getBackgroundPaint() != null) {
            g2.setPaint(this.getBackgroundPaint());
            g2.fill(chartArea);
        }

        Image backgroundImage = this.getBackgroundImage();
        if (backgroundImage != null) 
        {
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.getBackgroundImageAlpha()));
            Rectangle2D dest = new Rectangle2D.Double(0.0, 0.0,
                    backgroundImage.getWidth(null),
                    backgroundImage.getHeight(null));
            Align.align(dest, chartArea, this.getBackgroundImageAlignment());
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
        RectangleInsets padding = getPadding();

        Rectangle2D nonTitleArea = new Rectangle2D.Double();
        nonTitleArea.setRect(chartArea);
        padding.trim(nonTitleArea);

        drawRoamingTextTitle(g2, nonTitleArea, (entities != null), stackModel, frame);

        drawRoamingLegends(g2, nonTitleArea,  (entities != null));

        List<?> subtitles = this.getSubtitles();
        Iterator<?> iterator = subtitles.iterator();
        while (iterator.hasNext()) 
        {
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
        Channel2DPlot plot = getCustomizablePlot();
        plot.draw(g2, plotArea, anchor, null, plotInfo, stackModel, frame);

        g2.setClip(savedClip);

        notifyListeners(new ChartProgressEvent(this, this,ChartProgressEvent.DRAWING_FINISHED, 100));        
    }

    ///!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    public void drawRoamingTextTitle(Graphics2D g2, Rectangle2D dataArea, boolean entities, StackModel<?> stackModel, int frame)
    {  
        RoamingTextTitle originalTitle = getRoamingTitle();

        if(originalTitle == null)
        {
            return;
        }

        boolean draw = originalTitle.isVisible() && !originalTitle.isInside();

        if(draw)
        {
            RoamingTextTitle titleCopy = originalTitle.copy();
            if(titleCopy instanceof RoamingStackTextTitle)
            {
                double currentValue = stackModel.getStackingValue(frame);
                PrefixedUnit unit = stackModel.getStackingQuantity().getUnit();

                ((RoamingStackTextTitle) titleCopy).setFrameTitleText(currentValue, unit);
            }   
            drawRoamingTitle(titleCopy, g2, dataArea, entities);
        }                 		
    }


    @Override
    protected Rectangle2D subtractAreaOfTitles(Graphics2D g2, Rectangle2D nonTitleArea)
    {
        subtractRoamingTitle(getRoamingTitle(), g2, nonTitleArea);
        subtractRoamingLegends(g2, nonTitleArea);

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

    public Channel2DChart<E> createChart()
    {
        E originalPlot = getCustomizablePlot();                 
        E plot = (E) originalPlot.clone(); 

        return new Channel2DChart<>(plot, Datasets.DENSITY_PLOT, false);
    }

    public Channel2DChart<E> getCopy()
    {
        Channel2DChart<E> chartNew = createChart();

        //COPIES THE TITLE 
        RoamingTextTitle originalTitle = getRoamingTitle();
        if(originalTitle != null)
        {
            RoamingTextTitle titleNew = originalTitle.copy();
            chartNew.setRoamingTitle(titleNew);
        }

        RoamingLegend legendOriginal = getRoamingLegend();
        if(legendOriginal instanceof RoamingColorGradientLegend)
        {
            RoamingColorGradientLegend legendCopy = ((RoamingColorGradientLegend) legendOriginal).copy();

            ChannelRenderer renderer = chartNew.getCustomizablePlot().getRenderer();

            if(renderer instanceof PaintScaleSource)
            {                
                ((PaintScaleSource)renderer).setColorGradientLegend(legendCopy.getOutsideTitle());
            }

            chartNew.setRoamingLegend(legendCopy);
        }

        //COPIES THE SUBLEGENDS OF THE ORIGINAL CHART
        List<RoamingLegend> originalSublegends = getRoamingSublegends();

        int n = originalSublegends.size();

        for(int i = 0; i<n; i++)
        {
            RoamingLegend originalSublegend = originalSublegends.get(i);
            if(originalSublegend instanceof RoamingColorGradientLegend)
            {
                RoamingColorGradientLegend legendPaintScaleCopy = ((RoamingColorGradientLegend) legendOriginal).copy();

                ChannelRenderer renderer = chartNew.getCustomizablePlot().getRenderer(i + 2);

                if(renderer instanceof PaintScaleSource)
                {                
                    ((PaintScaleSource)renderer).setColorGradientLegend(legendPaintScaleCopy.getOutsideTitle());
                }

                chartNew.addRoamingSubLegend(legendPaintScaleCopy);
            }
        }

        chartNew.setMode(getMode());

        return chartNew;
    }

    public void overlay(String type, ProcessableXYZDataset dataset)
    {
        Quantity zQuantity = dataset.getZQuantity();
        Preferences pref = Preferences.userNodeForPackage(getClass()).node(getClass().getName()).node(type);

        CustomizableNumberAxis depthAxisNew = new CustomizableNumberAxis(zQuantity, pref);                    

        CustomizableImageRenderer rendererOverlay = new CustomizableImageRenderer(new StandardStyleTag(type), "Overlay " + type);
        rendererOverlay.fitAndRegisterDataset(dataset);

        getCustomizablePlot().insertLayer(dataset, rendererOverlay, 0);

        RoamingLegend legend = buildRoamingLegend(rendererOverlay, depthAxisNew);        
        setRoamingLegend(legend);
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

    public PrefixedUnit getDomainPreferredUnit()
    {
        Channel2DPlot plot = getCustomizablePlot();
        return plot.getDomainPreferredUnit();
    }

    public PrefixedUnit getRangePreferredUnit()
    {
        Channel2DPlot plot = getCustomizablePlot();
        return plot.getRangePreferredUnit();
    }

    @Override
    protected void drawPlot(Graphics2D g2, Rectangle2D plotArea, Point2D anchor, PlotState parentState, PlotRenderingInfo plotInfo) 
    {
        super.drawPlot(g2, plotArea, anchor, parentState, plotInfo);

        Channel2DPlot plot = getCustomizablePlot();
        MouseInteractiveTool currentTool = this.supervisor.getCurrentlyUsedInteractiveTool();
        if(currentTool != null)
        {
            Shape originalClip = g2.getClip();
            Composite originalComposite = g2.getComposite();

            Rectangle2D dataArea = plotInfo.getDataArea();
            g2.clip(dataArea);


            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, plot.getForegroundAlpha()));

            currentTool.draw(g2, plot, plotInfo.getDataArea(), plot.getDomainAxis(), plot.getRangeAxis(), 0, plotInfo);

            g2.clip(originalClip);
            g2.setComposite(originalComposite);
        }
    }
}
