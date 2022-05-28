package atomicJ.gui;

import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

import org.jfree.chart.plot.PlotOrientation;

import atomicJ.gui.annotations.AnnotationAnchorCore;

class MapMarkerManager implements MouseInputResponse
{
    private final Channel2DChart<?> densityChart;

    private final Map<Object, MapMarker> mapMarkers = new LinkedHashMap<>();
    private int currentMapMarkerIndex = 1;
    private MapMarker caughtMapMarker;
    private AnnotationAnchorCore caughtMapMarkerAnchor;

    MapMarkerManager(Channel2DChart<?> densityChart) {
        this.densityChart = densityChart;
    }

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        Rectangle2D rectangle = this.densityChart.getDataSquare(dataPoint, 0.005);

        this.caughtMapMarker = null;

        ListIterator<MapMarker> it = new ArrayList<>(mapMarkers.values()).listIterator(mapMarkers.size());
        while(it.hasPrevious())
        {           
            MapMarker mapMarker = it.previous();
            AnnotationAnchorCore anchor = mapMarker.getCaughtAnchor(java2DPoint, rectangle);
            if(anchor != null)
            {
                caughtMapMarker = mapMarker;
                caughtMapMarkerAnchor = anchor;

                PlotOrientation orientation = this.densityChart.getCustomizablePlot().getOrientation();
                boolean isVertical = (orientation == PlotOrientation.VERTICAL);
                Cursor cursor = caughtMapMarkerAnchor.getCursor(isVertical);

                this.densityChart.supervisor.requestCursorChange(cursor);
                event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);

                break;
            }           
        }            
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        this.caughtMapMarkerAnchor = null;
        this.caughtMapMarker = null;
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event) 
    {
        if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
        {
            return;
        }

        if(caughtMapMarker != null)
        {
            Point2D java2DPoint = event.getJava2DPoint();
            Point2D dataPoint = event.getDataPoint();

            boolean anchorChanged = caughtMapMarker.setPosition(caughtMapMarkerAnchor, this.densityChart.caughtPoint, dataPoint);

            //currently, the anchor never changes
            if(anchorChanged)
            {
                Rectangle2D rectangle = this.densityChart.getDataSquare(dataPoint, 0.005);
                caughtMapMarkerAnchor = caughtMapMarker.getCaughtAnchor(java2DPoint, rectangle);
            }

            this.densityChart.supervisor.addOrReplaceMapMarker(caughtMapMarker);
            event.setConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED, true);
        }            
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        Rectangle2D rectangle = this.densityChart.getDataSquare(dataPoint, 0.005);

        ListIterator<MapMarker> it = new ArrayList<>(mapMarkers.values()).listIterator(mapMarkers.size());

        boolean mapMarkerCaught = false;

        while(it.hasPrevious())
        {
            MapMarker mapMarker = it.previous();
            AnnotationAnchorCore anchor = mapMarker.getCaughtAnchor(java2DPoint, rectangle);
            if(anchor != null)
            {
                mapMarkerCaught = true;
                PlotOrientation orientation = this.densityChart.getCustomizablePlot().getOrientation();
                boolean isVertical = (orientation == PlotOrientation.VERTICAL);

                Cursor cursor = anchor.getCursor(isVertical);
                this.densityChart.supervisor.requestCursorChange(cursor);               
                event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);

                break;
            }           
        }
        if(!mapMarkerCaught && this.densityChart.isInsertMapMarkerMode(MouseInputType.MOVED))
        {
            this.densityChart.supervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));        
            event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
        }            
    }

    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        if(this.densityChart.isInsertMapMarkerMode(MouseInputType.CLICKED))
        {
            MapMarker clickedMapMarker = getMapMarkerForPoint(java2DPoint);

            if(clickedMapMarker != null)
            {
                if(event.isMultiple() && event.isLeft())
                {
                    this.densityChart.supervisor.removeMapMarker(clickedMapMarker);
                    event.setConsumed(MapChart.MOUSE_TRIGGERED_JUMP_TO_FIGURES, true);
                }
            }    
            else if(event.isLeft())
            {
                drawNewMapMarker(dataPoint);
            }
        }            
        else if(event.isMultiple() && event.isLeft())
        {
            removeMapMarker(event, java2DPoint);           
        }                
    }

    @Override
    public boolean isChartElementCaught() 
    {
        boolean caught = (caughtMapMarker != null);
        return caught;
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
    {
        return false;
    }

    private MapMarker getMapMarkerForPoint(Point2D java2Dpoint)
    {
        for(MapMarker mapMarker: mapMarkers.values())
        {  
            boolean isClicked = mapMarker.isClicked(java2Dpoint);
            if(isClicked)
            {
                return mapMarker;
            }
        }

        return null;        
    }

    private void removeMapMarker(CustomChartMouseEvent event, Point2D java2Dpoint)
    {
        MapMarker clickedMapMarker = getMapMarkerForPoint(java2Dpoint);

        if(clickedMapMarker != null)
        {
            this.densityChart.supervisor.removeMapMarker(clickedMapMarker);
            event.setConsumed(MapChart.MOUSE_TRIGGERED_JUMP_TO_FIGURES, true);     
        }       
    }

    public void removeMapMarker(MapMarker mapMarker)
    {
        Object key = mapMarker.getKey();
        MapMarker oldMapMaker = mapMarkers.remove(key);

        if(oldMapMaker != null)
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.removeMapMarker(oldMapMaker);
        }
    }


    public void setMapMarker(Map<Object, MapMarker> mapMarkersNew)
    {       
        if(!this.mapMarkers.equals(mapMarkersNew))
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();

            for(MapMarker oldMapMarker: mapMarkers.values())
            {
                plot.removeMapMarker(oldMapMarker, false);
            }

            mapMarkers.clear();

            for(MapMarker newMapMarker: mapMarkersNew.values())
            {
                MapMarker mapMarkerCopy = newMapMarker.copy(this.densityChart.mapMarkerStyle);
                mapMarkers.put(mapMarkerCopy.getKey(), mapMarkerCopy);

                plot.addOrReplaceMapMarker(mapMarkerCopy);

                currentMapMarkerIndex = Math.max(currentMapMarkerIndex, newMapMarker.getKey());
                currentMapMarkerIndex++;
            }

            this.densityChart.fireChartChanged();
        }
    }

    public void addOrReplaceMapMarker(MapMarker mapMarker)
    {
        Object key = mapMarker.getKey();
        MapMarker oldMapMarker = mapMarkers.get(key);

        if(oldMapMarker != null)
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.removeMapMarker(oldMapMarker);
        }
        else
        {
            currentMapMarkerIndex = Math.max(currentMapMarkerIndex, mapMarker.getKey());
            currentMapMarkerIndex++;
        }
        MapMarker mapMarkerCopy = mapMarker.copy(this.densityChart.mapMarkerStyle);
        mapMarkers.put(key, mapMarkerCopy);

        Channel2DPlot plot = this.densityChart.getCustomizablePlot();

        plot.addOrReplaceMapMarker(mapMarkerCopy);
    }

    public int getCurrentMapMarkerIndex()
    {
        return currentMapMarkerIndex;
    }

    public int getMapMarkerCount()
    {
        return mapMarkers.size();
    }

    private void drawNewMapMarker(Point2D dataPoint)
    {
        MapMarker marker = new MapMarker(dataPoint, currentMapMarkerIndex, this.densityChart.mapMarkerStyle);
        this.densityChart.supervisor.addOrReplaceMapMarker(marker);       
    }
}