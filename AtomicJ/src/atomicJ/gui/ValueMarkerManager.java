package atomicJ.gui;

import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.data.Range;
import org.jfree.ui.Layer;

import atomicJ.gui.profile.KnobSpecification;

class ValueMarkerManager implements MouseInputResponse
{
    private final Channel2DChart<?> chart;

    ValueMarkerManager(Channel2DChart<?> chart) {
        this.chart = chart;
    }

    private CustomizableValueMarker caughtValueMarker;
    private KnobSpecification caughtValueMarkerKnob;

    private final Map<Object, CustomizableValueMarker> domainValueMarkers = new LinkedHashMap<>();
    private final Map<Object, CustomizableValueMarker> rangeValueMarkers = new LinkedHashMap<>();

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        caughtValueMarkerKnob = getValueMarkerKnob(java2DPoint);

        if(caughtValueMarkerKnob == null)
        {
            caughtValueMarker = getCaughtValueMarker(dataPoint);

            if(caughtValueMarker != null && caughtValueMarker.isVisible())
            {
                this.chart.supervisor.requestCursorChange(HandCursors.getGrabbedHand());
            }
        }       
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        caughtValueMarkerKnob = null;            
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event)
    {
        if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();

        if(caughtValueMarkerKnob != null)
        {               
            caughtValueMarkerKnobResponseToMouseDragged(dataPoint);
            event.setConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED, true);
        }
        else if(caughtValueMarker != null)
        {            
            this.chart.supervisor.respondToValueMarkerMovement(dataPoint, caughtValueMarker.getKey());
            event.setConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED, true);
        }
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        KnobSpecification markerKnob = getValueMarkerKnob(java2DPoint);

        if(markerKnob != null)
        {
            this.chart.supervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
        }
        else
        {
            CustomizableValueMarker caughtMarker = getCaughtValueMarker(dataPoint);
            boolean markerCaught = (caughtMarker != null) && caughtMarker.isVisible();

            if(markerCaught)
            {
                this.chart.supervisor.requestCursorChange(HandCursors.getOpenHand());
                event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
            }
            else if(this.chart.isInsertDomainValueMarkerMode(MouseInputType.MOVED))
            {
                Cursor cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                this.chart.supervisor.requestCursorChange(cursor);
                event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
            }
        }               
    }

    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        if(event.isLeft())
        {
            KnobSpecification caughtValueMarkerKnob = getValueMarkerKnob(java2DPoint);
            boolean isKnobClicked = caughtValueMarkerKnob != null;

            if(isKnobClicked)
            {
                if(event.isMultiple())
                {
                    this.chart.supervisor.removeMarkerKnob(caughtValueMarkerKnob.getKey(), caughtValueMarkerKnob.getKnobIndex());
                    event.setConsumed(MapChart.MOUSE_TRIGGERED_JUMP_TO_FIGURES, true);
                }                   
            } 

            if(this.chart.isInsertDomainValueMarkerMode(MouseInputType.CLICKED) )
            {
                double position = dataPoint.getX();

                this.chart.supervisor.requestNewDomainMarker(position);
            }
        }
        else
        {                      
            CustomizableValueMarker caughtMarker = getCaughtValueMarker(dataPoint);

            if(caughtMarker != null && caughtMarker.isVisible())
            {
                boolean isDomainMarker = domainValueMarkers.containsKey(caughtMarker.getKey());

                double position = isDomainMarker ? dataPoint.getY() : dataPoint.getX();

                this.chart.supervisor.addMarkerKnob(caughtMarker.getKey(), position);
                event.setConsumed(MapChart.MOUSE_TRIGGERED_JUMP_TO_FIGURES, true);
            }     
        }        
    }

    @Override
    public boolean isChartElementCaught() 
    {
        boolean caught = (caughtValueMarker != null)||(caughtValueMarkerKnob != null);
        return caught;
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
    {
        boolean reserved = (getCaughtValueMarker(dataPoint) != null);

        return reserved;
    }

    private void caughtValueMarkerKnobResponseToMouseDragged(Point2D dataPoint)
    {
        if(caughtValueMarkerKnob != null)
        {
            int knobIndex = caughtValueMarkerKnob.getKnobIndex();
            Object markerKey = caughtValueMarkerKnob.getKey();

            double knobNewPosition = getCorrespondingKnobMarkerPosition(markerKey, dataPoint);

            this.chart.supervisor.moveMarkerKnob(markerKey, knobIndex, knobNewPosition);
        }
    }

    private double getCorrespondingKnobMarkerPosition(Object key, Point2D p)
    {
        double position = 0;

        CustomizableValueMarker marker = domainValueMarkers.get(key);
        if(marker != null)
        {
            position = p.getY();
        }
        else
        {
            position = p.getX();
        }
        return position;
    }

    CustomizableValueMarker getCaughtValueMarker(Point2D dataPoint)
    {       
        CustomizableValueMarker marker = getCaughtDomainMarker(dataPoint);

        if(marker == null )
        {
            marker = getCaughtRangeMarker(dataPoint);
        }

        return marker;
    }

    private CustomizableValueMarker getCaughtDomainMarker(Point2D dataPoint)
    {
        CustomizableValueMarker markerForPoint = null;

        double reach = 0.02;

        Range range = this.chart.getDomainRange(dataPoint, reach);

        double min = range.getLowerBound();
        double max = range.getUpperBound();

        for(CustomizableValueMarker marker : domainValueMarkers.values())
        {            
            double val = marker.getValue();

            boolean clicked = val >= min && val <= max;

            if(clicked)
            {
                markerForPoint = marker;
                break;
            }
        }

        return markerForPoint;
    }

    private CustomizableValueMarker getCaughtRangeMarker(Point2D dataPoint)
    {
        CustomizableValueMarker markerForPoint = null;

        double reach = 0.02;

        Range range = this.chart.getRangeRange(dataPoint, reach);

        double min = range.getLowerBound();
        double max = range.getUpperBound();

        for(CustomizableValueMarker marker : rangeValueMarkers.values())
        {            
            double val = marker.getValue();

            boolean clicked = val >= min && val <= max;

            if(clicked)
            {
                markerForPoint = marker;
                break;
            }
        }

        return markerForPoint;
    }


    private CustomizableValueMarker getValueMarker(Object markerKey)
    {
        CustomizableValueMarker domainMarker = domainValueMarkers.get(markerKey);

        if(domainMarker != null)
        {
            return domainMarker;
        }

        CustomizableValueMarker rangeMarker = rangeValueMarkers.get(markerKey);

        if(rangeMarker != null)
        {
            return rangeMarker;
        }

        return null;
    }


    private KnobSpecification getValueMarkerKnob(Point2D java2DPoint)
    {
        KnobSpecification knobSpecification = null;
        for(CustomizableValueMarker domainMarker : domainValueMarkers.values())
        {
            if(domainMarker.isVisible())
            {
                knobSpecification = domainMarker.getCaughtKnob(java2DPoint);

                if(knobSpecification != null)
                {
                    return knobSpecification;
                }      
            }           
        }

        for(CustomizableValueMarker rangeMarker : rangeValueMarkers.values())
        {
            if(rangeMarker.isVisible())
            {
                knobSpecification = rangeMarker.getCaughtKnob(java2DPoint);

                if(knobSpecification != null)
                {
                    return knobSpecification;
                }    
            }               
        }
        return  knobSpecification;
    }

    public void addValueMarkerKnob(Object markerKey, double knobPosition)
    {
        CustomizableValueMarker marker = getValueMarker(markerKey);

        if(marker != null)
        {
            marker.addKnob(knobPosition);
            return;
        }
    }

    public void moveValueMarkerKnob(Object markerKey, int knobIndex, double knobNewPosition)
    {
        CustomizableValueMarker marker = getValueMarker(markerKey);

        if(marker != null)
        {
            marker.moveKnob(knobIndex, knobNewPosition);
            return;
        }
    }

    public void removeValueMarkerKnob(Object markerKey, int markerIndex)
    {
        CustomizableValueMarker marker = getValueMarker(markerKey);

        if(marker != null)
        {
            marker.removeKnob(markerIndex);
        }
    }

    public void addDomainValueMarker(CustomizableValueMarker marker, Layer layer)
    {
        CustomizableXYPlot plot = this.chart.getCustomizablePlot();
        plot.addDomainMarker(marker, layer);

        this.domainValueMarkers.put(marker.getKey(), marker);
    }

    public void removeDomainValueMarker(CustomizableValueMarker marker)
    {
        CustomizableXYPlot plot = this.chart.getCustomizablePlot();
        plot.removeDomainMarker(marker);

        this.domainValueMarkers.remove(marker.getKey());

        updateMarkerKeys(marker.getKey());
    }

    private void updateMarkerKeys(Object keyRemoved)
    {
        Map<Object, CustomizableValueMarker> domainValueMarkersNew = new LinkedHashMap<>();
        if(keyRemoved instanceof KnobSpecification)
        {
            for(Entry<Object, CustomizableValueMarker> entry : domainValueMarkers.entrySet())
            {
                Object key = entry.getKey();
                CustomizableValueMarker marker = entry.getValue();

                if(key instanceof KnobSpecification)
                {
                    KnobSpecification numberKey = (KnobSpecification)key;
                    boolean larger = numberKey.getKnobIndex() > ((KnobSpecification)keyRemoved).getKnobIndex();

                    if(larger)
                    {
                        numberKey.decrementIndex();
                    }
                }

                domainValueMarkersNew.put(key, marker);
            }

            domainValueMarkers.clear();
            domainValueMarkers.putAll(domainValueMarkersNew);
        }
    }

    public void addRangeValueMarker(CustomizableValueMarker marker, Layer layer)
    {
        CustomizableXYPlot plot = this.chart.getCustomizablePlot();
        plot.addRangeMarker(marker, layer);

        this.rangeValueMarkers.put(marker.getKey(),marker);
    }

    public void removeRangeValueMarker(CustomizableValueMarker marker)
    {
        CustomizableXYPlot plot = this.chart.getCustomizablePlot();
        plot.removeRangeMarker(marker);

        this.rangeValueMarkers.remove(marker.getKey());
    }
}