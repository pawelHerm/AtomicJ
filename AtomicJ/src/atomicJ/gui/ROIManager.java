package atomicJ.gui;

import static atomicJ.gui.MouseInputModeStandard.ELIPTIC_ROI;
import static atomicJ.gui.MouseInputModeStandard.FREE_HAND_ROI;
import static atomicJ.gui.MouseInputModeStandard.POLYGON_ROI;
import static atomicJ.gui.MouseInputModeStandard.RECTANGULAR_ROI;
import static atomicJ.gui.MouseInputModeStandard.WAND_ROI;

import java.awt.Cursor;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;


import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;

import atomicJ.data.Channel2D;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.rois.WandContourTracer;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIEllipse;
import atomicJ.gui.rois.ROIEllipseHole;
import atomicJ.gui.rois.ROIFreeHand;
import atomicJ.gui.rois.ROIFreeHandHole;
import atomicJ.gui.rois.ROIMagicWandHole;
import atomicJ.gui.rois.ROIMagicWandPath;
import atomicJ.gui.rois.ROIPolygon;
import atomicJ.gui.rois.ROIPolygonHole;
import atomicJ.gui.rois.ROIRectangle;
import atomicJ.gui.rois.ROIRectangleHole;
import atomicJ.gui.rois.ROIStyle;

class ROIManager implements MouseInputResponse
{   
    private final Channel2DChart<?> densityChart;

    private final Map<Object, ROIDrawable> rois = new LinkedHashMap<>();

    private ROIPolygon roiUnderConstruction;   
    private int currentROIIndex = 1;
    private boolean roisVisible = false;

    private AnnotationModificationOperation currentModificationOperation;
    private ROIDrawable caughtROI;

    private Point2D caughtROICenter;
    private Point2D caughtROICompositeCenter;

    ROIManager(Channel2DChart<?> densityChart) 
    {
        this.densityChart = densityChart;
    }

    public int getCurrentROIIndex()
    {
        return currentROIIndex;
    }

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {
        if(!this.densityChart.isROIMode(MouseInputType.PRESSED))
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();

        if(roiUnderConstruction != null)
        {
            roiUnderConstruction.mousePressedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys());
            return;
        }  

        Map.Entry<ROIDrawable, AnnotationAnchorSigned> caughtROIAndAnchorPair = getCaughtAnchor(event);

        AnnotationAnchorSigned caughtAnchor = caughtROIAndAnchorPair.getValue();
        this.caughtROI = caughtROIAndAnchorPair.getKey();
        this.currentModificationOperation = new AnnotationModificationOperation(caughtAnchor,dataPoint, dataPoint);
        this.caughtROICenter = caughtAnchor != null ? caughtROI.getDefaultRotationCenter(caughtAnchor) : null;
        this.caughtROICompositeCenter = caughtAnchor != null ? caughtROI.getDefaultCompositeRotationCenter(caughtAnchor) : null;

        if(caughtAnchor != null )
        {
            this.densityChart.supervisor.requestCursorChange(caughtAnchor.getCoreAnchor().getCursor(false), caughtAnchor.getCoreAnchor().getCursor(true));
        }
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        if(!this.densityChart.isROIMode(MouseInputType.RELEASED))
        {
            return;
        }

        this.currentModificationOperation = null;
        this.caughtROI = null;
        this.caughtROICenter = null;
        this.caughtROICompositeCenter = null;
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event) 
    {
        if(!this.densityChart.isROIMode(MouseInputType.DRAGGED))
        {
            return;
        }

        if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
        {
            return;
        }

        if(caughtROI != null)
        {            
            boolean isRotation = MouseInputModeStandard.ROTATE_ROI.equals(this.densityChart.getMode(MouseInputType.DRAGGED));

            Point2D dataPoint = event.getDataPoint();

            Set<ModifierKey> modifierKeys = event.getModifierKeys();

            AnnotationModificationOperation returnedModificationOperation = isRotation ? caughtROI.rotate(currentModificationOperation.getAnchor(), modifierKeys, caughtROICenter, caughtROICompositeCenter, currentModificationOperation.getPressedPoint(), currentModificationOperation.getEndPoint(), dataPoint) 
                    : caughtROI.setPosition(currentModificationOperation.getAnchor(), modifierKeys, currentModificationOperation.getPressedPoint(), currentModificationOperation.getEndPoint(), dataPoint);

            this.currentModificationOperation = (returnedModificationOperation.getAnchor() != null) ? returnedModificationOperation : new AnnotationModificationOperation(currentModificationOperation.getAnchor(), currentModificationOperation.getPressedPoint(), dataPoint) ;
            this.densityChart.supervisor.addOrReplaceROI(caughtROI);
            event.setConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED, true);
        }
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {
        if(!this.densityChart.isROIMode(MouseInputType.MOVED))
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();

        if(isROIUnderConstruction())
        {
            roiUnderConstruction.mouseMovedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys());           
        }  

        if(!event.isConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED))
        {
            requestCursorChange(event);
        }  
    }

    private void requestCursorChange(CustomChartMouseEvent event)
    {
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

        //we look for anchor cursor only if there is no ROI under construction
        if(!isROIUnderConstruction())
        {
            Map.Entry<ROIDrawable, AnnotationAnchorSigned> caughtROIAndAnchorPair = getCaughtAnchor(event);
            AnnotationAnchorSigned anchor =caughtROIAndAnchorPair.getValue();
            if(anchor != null)
            {
                PlotOrientation orientation = this.densityChart.getCustomizablePlot().getOrientation();
                boolean isVertical = (orientation == PlotOrientation.VERTICAL);

                cursor = anchor.getCoreAnchor().getCursor(isVertical);
            }         
        }

        this.densityChart.supervisor.requestCursorChange(cursor);
        event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
    }


    private Map.Entry<ROIDrawable, AnnotationAnchorSigned> getCaughtAnchor(CustomChartMouseEvent event)
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();
        Rectangle2D rectangle = event.getDataRectangle(0.005);

        ListIterator<ROIDrawable> it = new ArrayList<>(rois.values()).listIterator(rois.size());

        ROIDrawable caughtNewROI = null;
        AnnotationAnchorSigned caughtAnchorNew = null;

        while(it.hasPrevious())
        {           
            ROIDrawable roi = it.previous();
            AnnotationAnchorSigned currentAnchor = roi.getCaughtAnchor(java2DPoint, dataPoint, rectangle);
            if(currentAnchor != null)
            {
                caughtAnchorNew = currentAnchor;
                caughtNewROI = roi;
                return new java.util.AbstractMap.SimpleEntry<>(caughtNewROI, caughtAnchorNew);
            }           
        }

        return new java.util.AbstractMap.SimpleEntry<>(null, null);
    }


    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {        
        if(!this.densityChart.isROIMode(MouseInputType.CLICKED))
        {
            return;
        }

        if(event.isLeft())
        {
            if(event.isMultiple())
            {
                handleLeftMultipleClick(event);            
            }
            else
            {
                handleLeftSingleClick(event);
            }
        }               
        else if(event.isRight())
        {
            handleRightClick(event);
        } 
    }

    private void handleLeftSingleClick(CustomChartMouseEvent event)
    {
        if(isROIUnderConstruction())
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        boolean isEmptySpace = isEmpty(java2DPoint, dataPoint);

        if(isEmptySpace)
        {
            createNewROI(dataPoint);
            return;
        }

        ROIDrawable clickedROI = getROIForPoint(dataPoint);
        if(clickedROI != null)
        {
            Set<ModifierKey> modifierKeys = event.getModifierKeys();
            Rectangle2D dataRectangle = event.getDataRectangle(0.005);
            boolean reshaped = clickedROI.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);

            if(reshaped)
            {
                this.densityChart.supervisor.addOrReplaceROI(clickedROI);
            }
            else
            {
                boolean highlighted = clickedROI.isHighlighted();
                clickedROI.setHighlighted(!highlighted);
            }
        }     
    }

    private void handleLeftMultipleClick(CustomChartMouseEvent event)
    {
        Map.Entry<ROIDrawable, AnnotationAnchorSigned> caughtROIandAnchor = getCaughtAnchor(event);

        ROIDrawable roi = caughtROIandAnchor.getKey();
        AnnotationAnchorSigned anchor = caughtROIandAnchor.getValue();

        if(anchor != null && BasicAnnotationAnchor.LABEL.equals(anchor.getCoreAnchor()))
        {
            requestNewLabelName(roi.getKey(), roi.getLabel());
            return;
        }

        removeROI(event.getDataPoint());    
    }

    public void requestNewLabelName(Object roiKey, final String labelOld)
    {
        NameChangeDialog nameDialog = new NameChangeDialog(this.densityChart.supervisor.getPublicationSite(), "ROI label");
        boolean approved = nameDialog.showDialog(new NameChangeModel() {

            @Override
            public void setName(Comparable<?> keyNew) 
            {}

            @Override
            public Comparable<?> getName() 
            {
                return labelOld;
            }
        });
        if(approved)
        {
            Comparable<?> labelNew = nameDialog.getNewKey();
            this.densityChart.supervisor.changeROILabel(roiKey, labelOld, labelNew.toString());
        }
    }

    @Override
    public boolean isChartElementCaught() 
    {
        boolean caught = (caughtROI != null);
        return caught;
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
    {
        boolean rightClickReserved = isROIUnderConstruction() ? roiUnderConstruction.isBoundaryClicked(dataArea) : false;

        return rightClickReserved;
    }

    private ROIDrawable getROIForPoint(Point2D dataPoint)
    {
        Rectangle2D hotDataArea = this.densityChart.getDataSquare(dataPoint, 0.01);   

        for(ROIDrawable roi: rois.values())
        {           
            boolean isClicked = roi.isBoundaryClicked(hotDataArea);
            if(isClicked)
            {
                return roi;
            }
        }
        return null;    
    }


    private boolean isEmpty(Point2D java2DPoint, Point2D dataPoint)
    {
        boolean empty = true;

        Rectangle2D hotDataArea = this.densityChart.getDataSquare(dataPoint, 0.01);   

        for(ROIDrawable roi: rois.values())
        {           
            boolean boundaryClicked = roi.isBoundaryClicked(hotDataArea);
            boolean labelClicked = roi.isLabelClicked(java2DPoint);
            if(boundaryClicked || labelClicked)
            {
                empty = false;
                break;
            }
        }
        return empty;    
    }

    private boolean isROIUnderConstruction()
    {
        return roiUnderConstruction != null;
    }


    public boolean isComplexElementUnderConstruction()
    {
        return isROIUnderConstruction();
    }

    void cancelRoiConstruction()
    {
        if(isROIUnderConstruction())
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.removeROI(roiUnderConstruction, false);
            roiUnderConstruction = null;
        }
    }

    private void handleRightClick(CustomChartMouseEvent event)
    {
        if(roiUnderConstruction != null)
        {
            Point2D dataPoint = event.getDataPoint();
            boolean constructed = roiUnderConstruction.respondToMouseRightClickedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys());
            if(constructed)
            {
                this.densityChart.supervisor.addOrReplaceROI(roiUnderConstruction);
            }
            else
            {               
                cancelRoiConstruction();
            }

            roiUnderConstruction = null;
        }   
    }

    private void removeROI(Point2D p)
    {
        ROIDrawable clickedRoi = getROIForPoint(p);
        if(clickedRoi != null)
        {
            this.densityChart.supervisor.removeROI(clickedRoi);           
        }       
    }

    private void createNewROI(Point2D dataPoint)
    {
        if(this.densityChart.getMode().equals(FREE_HAND_ROI)||this.densityChart.getMode().equals(POLYGON_ROI))
        {
            Path2D shape = new GeneralPath();
            shape.moveTo(dataPoint.getX(), dataPoint.getY());

            if(this.densityChart.roiHoleMode)
            {
                Area datasetArea = this.densityChart.getCustomizablePlot().getDatasetArea();

                roiUnderConstruction = this.densityChart.isFreeRoiMode() ?
                        new ROIFreeHandHole(datasetArea, shape, currentROIIndex, this.densityChart.roisStyle):
                            new ROIPolygonHole(datasetArea, shape, currentROIIndex, this.densityChart.roisStyle);

            }
            else
            {
                roiUnderConstruction = this.densityChart.isFreeRoiMode() ?
                        new ROIFreeHand(shape, currentROIIndex, this.densityChart.roisStyle):
                            new ROIPolygon(shape, currentROIIndex, this.densityChart.roisStyle);
            }


            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.addOrReplaceROI(roiUnderConstruction);

        }
        else if(this.densityChart.getMode().equals(RECTANGULAR_ROI)||this.densityChart.getMode().equals(ELIPTIC_ROI))
        {
            Rectangle2D rectangle = this.densityChart.getDataSquare(dataPoint, 0.04);

            ROIDrawable roiNew;

            if(this.densityChart.roiHoleMode)
            {
                Area datasetArea = this.densityChart.getCustomizablePlot().getDatasetArea();

                roiNew = (this.densityChart.getMode().equals(RECTANGULAR_ROI) ? 
                        new ROIRectangleHole(datasetArea, rectangle, currentROIIndex, this.densityChart.roisStyle)
                        : new ROIEllipseHole(datasetArea, new Ellipse2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight()) ,  currentROIIndex, this.densityChart.roisStyle));                 
            }
            else
            {                
                roiNew = (this.densityChart.getMode().equals(RECTANGULAR_ROI) ? 
                        new ROIRectangle(rectangle, currentROIIndex, this.densityChart.roisStyle)
                        : new ROIEllipse(new Ellipse2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight()) ,  currentROIIndex, this.densityChart.roisStyle));


            }
            roiNew.setFinished(true);
            this.densityChart.supervisor.addOrReplaceROI(roiNew);
        }
        else if(this.densityChart.getMode().equals(WAND_ROI))
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            XYDataset dataset = plot.getDataset();
            if(dataset instanceof Movie2DDataset)
            {
                dataset = ((Movie2DDataset) dataset).getCurrentFrame();
            }

            if(dataset instanceof Channel2DDataset)
            {
                WandContourTracer tracer = this.densityChart.supervisor.getWandTracer();
                Channel2D data = ((Channel2DDataset) dataset).getDisplayedChannel();

                Path2D shape = tracer.getContour(data.getDefaultGridding(), dataPoint);

                if(new Area(shape).isEmpty())
                {
                    return;
                }

                ROIDrawable roiNew = (this.densityChart.roiHoleMode) ? new ROIMagicWandHole(plot.getDatasetArea(), shape, currentROIIndex, this.densityChart.roisStyle) 
                        : new ROIMagicWandPath(shape, currentROIIndex, this.densityChart.roisStyle);    

                roiNew.setFinished(true);

                this.densityChart.supervisor.addOrReplaceROI(roiNew);
            }        
        }
    }

    public void setRoisVisible(boolean visibleNew)
    {
        if(roisVisible != visibleNew)
        {
            roisVisible = visibleNew;

            for(ROIDrawable roi: rois.values())
            {
                roi.setVisible(roisVisible);
            }
        }   
    }   

    public void addOrReplaceROI(ROIDrawable roi, ROIStyle customStyle)
    {
        Object key = roi.getKey();
        ROIDrawable oldRoi = rois.get(key);

        if(oldRoi != null)
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.removeROI(oldRoi);
        }
        else
        {
            currentROIIndex = Math.max(currentROIIndex, roi.getKey());
            currentROIIndex++;
        }

        ROIDrawable roiCopy = roi.copy(customStyle);
        rois.put(key, roiCopy);

        Channel2DPlot plot = this.densityChart.getCustomizablePlot();
        plot.addOrReplaceROI(roiCopy);
    }

    public void removeROI(ROIDrawable roi)
    {
        Object key = roi.getKey();
        ROIDrawable oldRoi = rois.remove(key);

        if(oldRoi != null)
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.removeROI(oldRoi);
        }
    }

    public void setROIs(Map<Object, ROIDrawable> roisNew, ROIStyle customStyle)
    {
        if(!rois.equals(roisNew))
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();

            for(ROIDrawable oldRoi: rois.values())
            {
                plot.removeROI(oldRoi, false);
            }

            rois.clear();

            for(ROIDrawable newRoi: roisNew.values())
            {
                ROIDrawable roiCopy = newRoi.copy(customStyle);
                rois.put(roiCopy.getKey(), roiCopy);

                plot.addOrReplaceROI(roiCopy);

                currentROIIndex = Math.max(currentROIIndex, newRoi.getKey());
                currentROIIndex++;
            }

            this.densityChart.fireChartChanged();
        }
    }

    public void changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        ROIDrawable roi = rois.get(roiKey);

        if(roi != null)
        {
            roi.setLabel(labelNew);
        }
    }
}