package atomicJ.gui;


import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.profile.ProfileStyle;
import atomicJ.gui.rois.line.ROICurve;
import atomicJ.gui.rois.line.ROICurveType;

public abstract class ROICurveManager implements MouseInputResponse
{
    private final Map<Object, ROICurve> roiCurves = new LinkedHashMap<>();
    private ROICurve curveUnderConstruction;   
    private int currentCurveIndex = 1;

    private AnnotationAnchorSigned caughtROICurveAnchor;
    private ROICurve caughtROICurve;
    private boolean profilesVisible = false;

    private Point2D caughtPoint;

    protected ROICurve getCurveUnderConstruction()
    {
        return curveUnderConstruction;
    }

    public List<ROICurve> getROICurves()
    {
        List<ROICurve> curves = new ArrayList<>(roiCurves.values());       
        return curves;
    }

    public int getROICurveCount()
    {
        return roiCurves.size();
    }

    protected abstract ROICurveType getROICurveType();
    protected abstract ProfileStyle getStyle();
    protected abstract boolean isAppropriateMode(MouseInputType inputType);
    protected abstract void requestCursorChange(Cursor horizontalCursor, Cursor verticalCursor);
    protected abstract void handleChangeOfDrawing();
    protected abstract void handleFinishedCurveCountChange(int countOld, int countNew);

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {
        if(!isAppropriateMode(MouseInputType.PRESSED))
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();
        Rectangle2D dataArea = event.getDataRectangle(0.005);  

        this.caughtPoint = dataPoint;  

        ROICurve clickedCurve = getCurveForPoint(dataArea);
        boolean isEmptySpace = (clickedCurve == null); 

        if(curveUnderConstruction != null)
        {
            if(isEmptySpace)
            {
                curveUnderConstruction.mousePressedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys());
                handleChangeOfDrawing();
            }
        }
        else
        {           
            this.caughtROICurveAnchor = null;
            this.caughtROICurve = null;

            ListIterator<ROICurve> it = new ArrayList<>(roiCurves.values()).listIterator(roiCurves.size());
            while(it.hasPrevious())
            {           
                ROICurve curve = it.previous();
                AnnotationAnchorSigned anchor = curve.getCaughtAnchor(java2DPoint, dataPoint, dataArea);
                if(anchor != null)
                {
                    this.caughtROICurve = curve;
                    this.caughtROICurveAnchor = anchor;

                    Cursor horizontalCursor = caughtROICurveAnchor.getCoreAnchor().getCursor(false);
                    Cursor verticalCursor = caughtROICurveAnchor.getCoreAnchor().getCursor(true);

                    requestCursorChange(horizontalCursor, verticalCursor);
                    break;
                }           
            }     
        }        
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        if(!isAppropriateMode(MouseInputType.RELEASED))
        {
            return;
        }

        this.caughtPoint = null;
        this.caughtROICurveAnchor = null;
        this.caughtROICurve = null;
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event) 
    {
        if(!isAppropriateMode(MouseInputType.DRAGGED))
        {
            return;
        }

        if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();

        if(caughtROICurve != null)
        {       
            AnnotationAnchorSigned anchorChanged = caughtROICurve.setPosition(caughtROICurveAnchor, caughtPoint, dataPoint);

            if(anchorChanged != null)
            {
                caughtROICurveAnchor = anchorChanged;
            }

            addOrReplaceCurve(caughtROICurve);
        }    

        this.caughtPoint = dataPoint;
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {        
        if(!isAppropriateMode(MouseInputType.MOVED))
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();


        if(isCurveUnderConstruction())
        {
            if(!event.isConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED))
            {
                requestCursorChange(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
            }

            curveUnderConstruction.mouseMovedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys());
            handleChangeOfDrawing();  
        }
        else 
        {       
            if(event.isConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED))
            {
                return;
            }

            Rectangle2D rectangle = event.getDataRectangle(0.005);
            Cursor horizontalCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            Cursor verticalCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

            ListIterator<ROICurve> it = new ArrayList<>(roiCurves.values()).listIterator(roiCurves.size());
            while(it.hasPrevious())
            {
                ROICurve curve = it.previous();
                AnnotationAnchorSigned anchor = curve.getCaughtAnchor(java2DPoint, dataPoint, rectangle);
                if(anchor != null)
                {                    
                    horizontalCursor = anchor.getCoreAnchor().getCursor(false);
                    verticalCursor = anchor.getCoreAnchor().getCursor(true);
                    break;
                }           
            }

            requestCursorChange(horizontalCursor, verticalCursor);
            event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
        }
    }

    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {
        if(!isAppropriateMode(MouseInputType.CLICKED))
        {
            return;
        }

        if(event.isMultiple())
        {
            removeCurve(event.getDataRectangle(0.005));
        }
        else if(event.isLeft())
        {
            handleLeftSingleClick(event);
        }
        else 
        {    
            handleRightSingleClick(event);  
        }       
    }

    private void handleLeftSingleClick(CustomChartMouseEvent event)
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();
        Rectangle2D dataArea = event.getDataRectangle(0.005);  

        ROICurve clickedCurve = getCurveForPoint(dataArea);
        boolean isEmptySpace = (clickedCurve == null);  

        if(isEmptySpace)
        {
            if(!isComplexCurveUnderConstruction())
            {
                if(isCurveUnderConstruction())
                {
                    finishCurve();
                }
                else 
                {
                    beginNewCurve(dataPoint);
                } 
            }
        }
        else
        {
            boolean isHighlighted = clickedCurve.isHighlighted();

            Set<ModifierKey> modifierKeys = event.getModifierKeys();
            Rectangle2D dataRectangle = event.getDataRectangle(0.005);

            boolean reshaped = clickedCurve.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);

            if(reshaped)
            {
                addOrReplaceCurve(clickedCurve);
            }
            else
            {
                clickedCurve.setHighlighted(!isHighlighted);
                handleChangeOfDrawing();
            }           
        }
    }

    private void handleRightSingleClick(CustomChartMouseEvent event)
    {
        if(isCurveUnderConstruction())
        {
            finishCurve();
        }
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
    {
        boolean reserved = false;
        if(isCurveUnderConstruction())
        {
            reserved = curveUnderConstruction.isBoundaryClicked(dataArea);
        }

        return reserved;
    }

    @Override
    public boolean isChartElementCaught() 
    {
        boolean caught = (caughtROICurve != null);
        return caught;
    }

    public boolean removeCurve(ROICurve curve)
    {
        Object key = curve.getKey();
        int countOd = roiCurves.size();
        ROICurve removedCurve = roiCurves.remove(key);
        boolean curveWasPresent = removedCurve != null;

        if(curveWasPresent)
        {
            handleFinishedCurveCountChange(countOd, roiCurves.size());
            handleChangeOfDrawing();
        }

        return curveWasPresent;
    }

    private void beginNewCurve(Point2D anchor)
    {      
        ROICurveType curveType = getROICurveType();
        curveUnderConstruction = curveType.buildCurve(anchor, currentCurveIndex, getStyle());  
        handleChangeOfDrawing();
    }

    private void finishCurve()
    {   
        if(isCurveUnderConstruction())
        {
            curveUnderConstruction.setFinished(true);                         
            addOrReplaceCurve(curveUnderConstruction);           
            curveUnderConstruction = null;
        }   
    }

    public ROICurve getCurveForPoint(Rectangle2D dataArea)
    {
        for(ROICurve profile: roiCurves.values())
        {       
            boolean isClicked = profile.isClicked(dataArea);
            if(isClicked)
            {
                return profile;
            }
        }

        return null;        
    }

    public boolean isCurveUnderConstruction()
    {
        return curveUnderConstruction != null;
    }

    private boolean isComplexCurveUnderConstruction()
    {
        return curveUnderConstruction != null && curveUnderConstruction.isComplex();
    }

    public boolean isComplexElementUnderConstruction()
    {
        return isComplexCurveUnderConstruction();
    }

    private boolean removeCurve(Rectangle2D dataArea)
    {
        ROICurve clickedCurve = getCurveForPoint(dataArea);
        boolean curveRemoved = false;
        if(clickedCurve != null)
        {
            curveRemoved = removeCurve(clickedCurve);
        }     

        return curveRemoved;
    }

    public void addOrReplaceCurve(ROICurve curve)
    {
        Object key = curve.getKey();
        ROICurve oldCurve = roiCurves.get(key);

        if(oldCurve == null)
        {
            currentCurveIndex = Math.max(currentCurveIndex, curve.getKey());
            currentCurveIndex++;
        }

        int countOld = roiCurves.size();

        ROICurve curveCopy = curve.copy(getStyle());
        roiCurves.put(key, curveCopy);

        int countNew = roiCurves.size();

        handleFinishedCurveCountChange(countOld, countNew);
        handleChangeOfDrawing();
    }

    public void setVisible(boolean visibleNew)
    {
        if(profilesVisible != visibleNew)
        {
            profilesVisible = visibleNew;           
            for(ROICurve curve: roiCurves.values())
            {
                curve.setVisible(profilesVisible);
            }

            handleChangeOfDrawing();
        }
    }
}