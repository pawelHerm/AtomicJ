package atomicJ.gui;


import java.awt.Cursor;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.plot.PlotOrientation;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.measurements.DistanceFreeMeasurement;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistancePolyMeasurement;
import atomicJ.gui.measurements.DistanceLineMeasurement;
import atomicJ.gui.measurements.DistanceMeasurementSupervisor;


class DistanceMeasurementManager implements MouseInputResponse
{
    private final ChannelChart<?> chart;

    private boolean distanceMeasurementsVisible = false;

    private final Map<Object, DistanceMeasurementDrawable> distanceMeasurements = new LinkedHashMap<>();
    private DistanceMeasurementDrawable measurementUnderConstruction;   
    private int currentMeasurementIndex = 1;

    private AnnotationAnchorSigned caughtDistanceMeasurementAchor;
    private DistanceMeasurementDrawable caughtDistanceMeasurement;

    private DistanceMeasurementSupervisor measurementSupervisor;

    DistanceMeasurementManager(ChannelChart<?> chart) 
    {
        this.chart = chart;
    }

    public void setDistanceMeasurementSupervisor(DistanceMeasurementSupervisor measurementSupervisor)
    {
        this.measurementSupervisor = measurementSupervisor;
    }

    public DistanceMeasurementSupervisor getDistanceMeasurementSupervisor()
    {
        return measurementSupervisor;
    } 

    public int getCurrentMeasurementIndex()
    {
        return currentMeasurementIndex;
    }

    public int getDistanceMeasurementCount()
    {
        return distanceMeasurements.size();
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
    {
        boolean reserved = false;

        if(isDistanceMeasurementUnderConstruction())
        {
            reserved = measurementUnderConstruction.isBoundaryClicked(dataArea);
        }

        return reserved;
    }

    public List<DistanceShapeFactors> getDistanceMeasurementShapeFactors()
    {
        List<DistanceShapeFactors> measurementLines = new ArrayList<>();
        for(DistanceMeasurementDrawable profile : distanceMeasurements.values())
        {
            measurementLines.add(profile.getDistanceShapeFactors());
        }
        return measurementLines;
    }

    public void setDistanceMeasurementsVisible(boolean visibleNew)
    {
        if(distanceMeasurementsVisible != visibleNew)
        {
            distanceMeasurementsVisible = visibleNew;           
            for(DistanceMeasurementDrawable measurement: distanceMeasurements.values())
            {
                measurement.setVisible(distanceMeasurementsVisible);
            }
        }
    }

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {
        if(!this.chart.isDistanceMeasurementMode(MouseInputType.PRESSED))
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        if(isComplexMeasurementUnderConstruction())
        {
            measurementUnderConstruction.mousePressedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys()); 
        }       
        else if(!isDistanceMeasurementUnderConstruction())
        {
            Rectangle2D rectangle = this.chart.getDataSquare(dataPoint, 0.01);

            this.caughtDistanceMeasurementAchor = null;
            this.caughtDistanceMeasurement = null;

            ListIterator<DistanceMeasurementDrawable> it = new ArrayList<>(distanceMeasurements.values()).listIterator(distanceMeasurements.size());
            while(it.hasPrevious())
            {           
                DistanceMeasurementDrawable measurement = it.previous();
                AnnotationAnchorSigned anchor = measurement.getCaughtAnchor(java2DPoint, dataPoint, rectangle);
                if(anchor != null)
                {
                    caughtDistanceMeasurement= measurement;
                    caughtDistanceMeasurementAchor = anchor;

                    PlotOrientation orientation = chart.getCustomizablePlot().getOrientation();
                    boolean isVertical = (orientation == PlotOrientation.VERTICAL);
                    Cursor cursor = caughtDistanceMeasurementAchor.getCoreAnchor().getCursor(isVertical);
                    measurementSupervisor.requestCursorChange(cursor);

                    break;
                }           
            }
        }      
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        if(!this.chart.isDistanceMeasurementMode(MouseInputType.RELEASED))
        {
            return;
        }

        this.caughtDistanceMeasurementAchor = null;
        this.caughtDistanceMeasurement = null;
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event) 
    {
        if(!this.chart.isDistanceMeasurementMode(MouseInputType.DRAGGED))
        {
            return;
        }

        if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
        {
            return;
        }

        if(caughtDistanceMeasurement != null)
        {
            Point2D dataPoint = event.getDataPoint();
            Set<ModifierKey> modifierKeys = event.getModifierKeys();

            AnnotationAnchorSigned anchorChanged = caughtDistanceMeasurement.setPosition(caughtDistanceMeasurementAchor, modifierKeys, this.chart.getCaughtPoint(), dataPoint);

            if(anchorChanged != null)
            {
                caughtDistanceMeasurementAchor = anchorChanged;
            }

            measurementSupervisor.addOrReplaceDistanceMeasurement(caughtDistanceMeasurement);
            event.setConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED, true);
        }
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {
        if(!this.chart.isDistanceMeasurementMode(MouseInputType.MOVED))
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();

        if(dataPoint == null)
        {
            return;
        }

        if(isDistanceMeasurementUnderConstruction())
        {
            measurementUnderConstruction.mouseMovedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys());
        }    

        if(!event.isConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED))
        {
            requestCursorChange(event);
        }
    }

    private void requestCursorChange(CustomChartMouseEvent event)
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        Rectangle2D rectangle = this.chart.getDataSquare(dataPoint, 0.01);

        Cursor cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

        if(!isDistanceMeasurementUnderConstruction())
        {
            ListIterator<DistanceMeasurementDrawable> it = new ArrayList<>(distanceMeasurements.values()).listIterator(distanceMeasurements.size());
            while(it.hasPrevious())
            {
                DistanceMeasurementDrawable distanceMeasurement = it.previous();
                AnnotationAnchorSigned anchor = distanceMeasurement.getCaughtAnchor(java2DPoint, dataPoint, rectangle);

                if(anchor != null)
                {
                    PlotOrientation orientation = this.chart.getCustomizablePlot().getOrientation();
                    boolean isVertical = (orientation == PlotOrientation.VERTICAL);

                    cursor = anchor.getCoreAnchor().getCursor(isVertical);

                    break;
                }           
            }
        }     

        measurementSupervisor.requestCursorChange(cursor);  
        event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
    }

    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {
        if(event.isConsumed(CustomizableXYBaseChart.CHART_EDITION))
        {
            return;
        }

        if(!this.chart.isDistanceMeasurementMode(MouseInputType.CLICKED))
        {
            return;                      
        }


        Point2D dataPoint = event.getDataPoint();
        boolean isLeft = event.isLeft();

        if(event.isMultiple())
        {
            removeDistanceMeasurement(dataPoint);
        }
        else if(isLeft)
        {
            handleLeftSingleClick(event);
        } 
        else 
        {
            handleRightSingleClick(event);
        }
    }

    private void handleRightSingleClick(CustomChartMouseEvent event)
    {
        if(isDistanceMeasurementUnderConstruction())
        {
            finishDistanceMeasurement();
        }
    }

    private void handleLeftSingleClick(CustomChartMouseEvent event)
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        DistanceMeasurementDrawable clickedMeasurement = getDistanceMeasurementForPoint(dataPoint);
        boolean isEmptySpace = (clickedMeasurement == null);                                                 

        if(isSimpleMeasurementUnderConstruction())
        {
            finishDistanceMeasurement();
        }
        else if(!isDistanceMeasurementUnderConstruction())
        {
            if(isEmptySpace)
            {
                beginNewDistanceMeasurement(dataPoint);
            }
            else
            {
                boolean isHighlighted = clickedMeasurement.isHighlighted();

                Set<ModifierKey> modifierKeys = event.getModifierKeys();
                Rectangle2D dataRectangle = this.chart.getDataSquare(dataPoint, 0.005);

                boolean reshaped = clickedMeasurement.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);

                if(reshaped)
                {
                    this.measurementSupervisor.addOrReplaceDistanceMeasurement(clickedMeasurement);
                }
                else
                {
                    clickedMeasurement.setHighlighted(!isHighlighted);
                }           
            }
        }         
    }

    @Override
    public boolean isChartElementCaught() 
    {
        boolean caught = (caughtDistanceMeasurement != null);
        return caught;
    }

    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        Object key = measurement.getKey();
        DistanceMeasurementDrawable oldMeasurement = distanceMeasurements.remove(key);

        if(oldMeasurement != null)
        {
            CustomizableXYPlot plot = this.chart.getCustomizablePlot();
            plot.removeDistanceMeasurement(oldMeasurement);
        }
    }

    private void removeDistanceMeasurement(Point2D p)
    {
        DistanceMeasurementDrawable clickedMeasurement = getDistanceMeasurementForPoint(p);
        if(clickedMeasurement != null)
        {
            measurementSupervisor.removeDistanceMeasurement(clickedMeasurement);
        }       
    }

    private boolean isDistanceMeasurementUnderConstruction()
    {
        return measurementUnderConstruction != null;
    }

    private boolean isSimpleMeasurementUnderConstruction()
    {
        return measurementUnderConstruction != null && !measurementUnderConstruction.isComplex();
    }

    private boolean isComplexMeasurementUnderConstruction()
    {
        return measurementUnderConstruction != null && measurementUnderConstruction.isComplex();
    }


    public boolean isComplexElementUnderConstruction()
    {
        return isComplexMeasurementUnderConstruction();
    }

    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        Object key = measurement.getKey();
        DistanceMeasurementDrawable oldMeasurement = distanceMeasurements.get(key);

        CustomizableXYPlot plot = this.chart.getCustomizablePlot();

        if(oldMeasurement != null)
        {
            plot.removeDistanceMeasurement(oldMeasurement);
        }
        else
        {
            currentMeasurementIndex = Math.max(currentMeasurementIndex, measurement.getKey());
            currentMeasurementIndex++;
        }
        DistanceMeasurementDrawable measurementCopy = measurement.copy(this.chart.getDistanceMeasurementStyle());
        distanceMeasurements.put(key, measurementCopy);

        plot.addOrReplaceDistanceMeasurement(measurementCopy);
    }

    public void setDistanceMeasurements(Map<Object, DistanceMeasurementDrawable> measurements2)
    {       
        if(!distanceMeasurements.equals(measurements2))
        {
            CustomizableXYPlot plot = this.chart.getCustomizablePlot();

            for(DistanceMeasurementDrawable oldMeasurement: distanceMeasurements.values())
            {
                plot.removeDistanceMeasurement(oldMeasurement, false);
            }

            distanceMeasurements.clear();

            for(DistanceMeasurementDrawable newMeasurement: measurements2.values())
            {
                DistanceMeasurementDrawable measurementCopy
                = newMeasurement.copy(this.chart.getDistanceMeasurementStyle());
                distanceMeasurements.put(measurementCopy.getKey(), measurementCopy);

                plot.addOrReplaceDistanceMeasurement(measurementCopy);

                currentMeasurementIndex = Math.max(currentMeasurementIndex, newMeasurement.getKey());
                currentMeasurementIndex++;
            }

            this.chart.fireChartChanged();
        }
    }

    private void beginNewDistanceMeasurement(Point2D dataPoint)
    {
        if(this.chart.getMode().equals(MouseInputModeStandard.DISTANCE_MEASUREMENT_POLYLINE))
        {
            Path2D shape = new GeneralPath();
            shape.moveTo(dataPoint.getX(), dataPoint.getY());

            measurementUnderConstruction = new DistancePolyMeasurement(dataPoint, currentMeasurementIndex, this.chart.getDistanceMeasurementStyle());
            CustomizableXYPlot plot = this.chart.getCustomizablePlot();
            plot.addOrReplaceDistanceMeasurement(measurementUnderConstruction);

        }
        else if(this.chart.getMode().equals(MouseInputModeStandard.DISTANCE_MEASUREMENT_FREEHAND))
        {
            Path2D shape = new GeneralPath();
            shape.moveTo(dataPoint.getX(), dataPoint.getY());

            measurementUnderConstruction = new DistanceFreeMeasurement(dataPoint, currentMeasurementIndex, this.chart.getDistanceMeasurementStyle());
            CustomizableXYPlot plot = this.chart.getCustomizablePlot();
            plot.addOrReplaceDistanceMeasurement(measurementUnderConstruction);

        }
        else if(this.chart.getMode().equals(MouseInputModeStandard.DISTANCE_MEASUREMENT_LINE))
        {
            measurementUnderConstruction = new DistanceLineMeasurement(dataPoint, dataPoint, currentMeasurementIndex, this.chart.getDistanceMeasurementStyle());

            CustomizableXYPlot plot = this.chart.getCustomizablePlot();
            plot.addOrReplaceDistanceMeasurement(measurementUnderConstruction);
        }
    }

    private void finishDistanceMeasurement()
    {   
        if(isDistanceMeasurementUnderConstruction())
        {
            measurementUnderConstruction.setFinished(true);                         
            measurementSupervisor.addOrReplaceDistanceMeasurement(measurementUnderConstruction);           
            measurementUnderConstruction = null;
        }   
    }

    private DistanceMeasurementDrawable getDistanceMeasurementForPoint(Point2D dataPoint)
    {
        Rectangle2D dataRectangle = this.chart.getDataSquare(dataPoint, 0.01);  

        for(DistanceMeasurementDrawable measurement: distanceMeasurements.values())
        {       
            boolean isClicked = measurement.isClicked(dataRectangle);
            if(isClicked)
            {
                return measurement;
            }
        }

        return null;        
    }
}