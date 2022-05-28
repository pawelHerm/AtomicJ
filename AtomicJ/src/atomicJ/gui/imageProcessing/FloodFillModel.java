package atomicJ.gui.imageProcessing;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

import atomicJ.data.units.UnitExpression;
import atomicJ.gui.CustomChartMouseEvent;
import atomicJ.gui.MouseInputType;
import atomicJ.gui.MouseInteractiveTool;
import atomicJ.gui.generalProcessing.ProcessingModel;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.imageProcessing.FloodFillSolidTransformation;
import atomicJ.imageProcessing.FloodFillTransformation;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.Channel2DResourceView;
import atomicJ.utilities.GeometryUtilities;


public class FloodFillModel extends ProcessingModel
{
    private static final double TOLERANCE = 1e-10;

    public static final String FILL_VALUE = "FillValue";
    public static final String MAX_DIFFERENCE = "MaxDiffernce";
    public static final String MIN_DIFFERENCE = "MinDifference";
    public static final String INIT_X = "InitX";
    public static final String INIT_Y = "InitY";
    public static final String INTERRUPT = "Interrupt";
    public static final String FILL_HOLES = "FillHoles";

    private UnitExpression fillValue;
    private UnitExpression minDifference;
    private UnitExpression maxDifference;

    private double initY = Double.NaN;
    private double initX = Double.NaN;

    private boolean fillHoles = false;

    private UndoableCommand previewCommand;

    private final Channel2DResourceView manager;
    private final FloodFillInteractiveTool floodFillTool = new FloodFillInteractiveTool();

    public FloodFillModel(Channel2DResourceView manager)
    {
        super(manager.getDrawableROIs(), manager.getROIUnion(), manager.getUnitManager());
        this.manager = manager;

        this.fillValue = new UnitExpression(0, getValueAxisDisplayedUnit());
        this.minDifference = new UnitExpression(-1, getValueAxisDisplayedUnit());
        this.maxDifference = new UnitExpression(1, getValueAxisDisplayedUnit());

        this.manager.useMouseInteractiveTool(floodFillTool);
    }     

    public double getInitY()
    {
        return initY;
    }

    public void setInitY(double initYNew)
    {        
        if(!GeometryUtilities.almostEqual(this.initY, initYNew, TOLERANCE))
        {
            double initYOld = this.initY;
            this.initY = initYNew;

            firePropertyChange(INIT_Y, initYOld, initYNew);

            checkIfApplyEnabled();
        }
    }

    public double getInitX()
    {
        return initX;
    }

    public void setInitX(double initXNew)
    {
        if(!GeometryUtilities.almostEqual(this.initX, initXNew, TOLERANCE))
        {
            double initXOld = this.initX;
            this.initX = initXNew;

            firePropertyChange(INIT_X, initXOld, initXNew);

            checkIfApplyEnabled();
        }
    }

    public UnitExpression getFillValue()
    {
        return fillValue;
    }

    public void setFillValue(UnitExpression fillValueNew)
    {
        if(!GeometryUtilities.almostEqual(this.fillValue, fillValueNew, TOLERANCE))
        {
            UnitExpression fillValueOld  = this.fillValue;
            this.fillValue = fillValueNew;

            firePropertyChange(FILL_VALUE, fillValueOld, fillValueNew);

            checkIfApplyEnabled();
        }
    }

    public UnitExpression getMinDifference()
    {
        return minDifference;
    }

    public void setMinDifference(UnitExpression minDifferenceNew)
    {      
        if(!GeometryUtilities.almostEqual(this.minDifference, minDifferenceNew, TOLERANCE))
        {
            UnitExpression minDifferenceOld = this.minDifference;
            this.minDifference = minDifferenceNew;

            firePropertyChange(MIN_DIFFERENCE, minDifferenceOld, minDifferenceNew);

            checkIfApplyEnabled();
        }
    }

    public UnitExpression getMaxDifference()
    {
        return maxDifference;
    }

    public void setMaxDifference(UnitExpression maxDifferenceNew)
    {
        if(!GeometryUtilities.almostEqual(this.maxDifference, maxDifferenceNew, TOLERANCE))
        {
            UnitExpression maxDifferenceOld = this.maxDifference;
            this.maxDifference = maxDifferenceNew;

            firePropertyChange(MAX_DIFFERENCE, maxDifferenceOld, maxDifferenceNew);

            checkIfApplyEnabled();
        }
    }

    public boolean isFillHoles()
    {
        return fillHoles;
    }

    public void setFillHoles(boolean fillHolesNew)
    {
        if(this.fillHoles != fillHolesNew)
        {
            boolean fillHolesOld = this.fillHoles;
            this.fillHoles = fillHolesNew;

            firePropertyChange(FILL_HOLES, fillHolesOld, fillHolesNew);
        }
    }

    protected Channel2DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        double minDifferenceDataUnits = minDifference.derive(getDataUnit()).getValue();
        double maxDifferenceDataUnits = maxDifference.derive(getDataUnit()).getValue();
        double fillValueDataUnits = fillValue.derive(getDataUnit()).getValue();

        Channel2DDataInROITransformation tr = fillHoles ?  new FloodFillSolidTransformation(initX, initY, minDifferenceDataUnits,
                maxDifferenceDataUnits, fillValueDataUnits):
                    new FloodFillTransformation(initX, initY, minDifferenceDataUnits, maxDifferenceDataUnits,fillValueDataUnits);

        return tr;
    }

    @Override
    public void apply()
    {        
        super.apply();     

        if(isApplyEnabled())
        {

            Channel2DResource resource = manager.getSelectedResource();
            String type = manager.getSelectedType();
            ROIRelativePosition position = getROIPosition();
            ROI roi = getSelectedROI();

            Channel2DDataInROITransformation tr = buildTransformation();

            UndoableCommand command = new UndoableImageROICommand(manager, type, null, resource, tr, position, roi);
            command.execute(); 

            if(command != null)
            {
                manager.pushCommand(resource, type, command);
            }
        }
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && (!Double.isNaN(initX) && !Double.isNaN(initY));

        return applyEnabled;
    }

    @Override
    public void operationFinished()
    {
        super.operationFinished();
        manager.stopUsingMouseInteractiveTool(floodFillTool);
    }

    @Override
    public void reset()
    {
        super.reset();
        resetPreview();
    }

    private void resetPreview()
    {        
        if(previewCommand != null)
        {
            previewCommand.undo();
        }
    }

    private class FloodFillInteractiveTool implements MouseInteractiveTool
    {
        private final Set<MouseInputType> usedMouseInputTypes = new LinkedHashSet<>(Arrays.asList(MouseInputType.CLICKED, MouseInputType.MOVED));
        private final MouseInteractiveToolListenerSupport listenerSupport = new MouseInteractiveToolListenerSupport();

        @Override
        public void mousePressed(CustomChartMouseEvent event) {            
        }

        @Override
        public void mouseReleased(CustomChartMouseEvent event) {

        }

        @Override
        public void mouseDragged(CustomChartMouseEvent event)
        {}

        @Override
        public void mouseMoved(CustomChartMouseEvent event)
        {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            manager.requestCursorChange(cursor, cursor);
        }

        @Override
        public void mouseClicked(CustomChartMouseEvent evt)
        {                   
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            manager.requestCursorChange(cursor, cursor);

            if(evt.isLeft())
            {
                Point2D dataPoint = evt.getDataPoint();

                setInitX(dataPoint.getX());
                setInitY(dataPoint.getY());
                apply();
            }                  
        }

        @Override
        public boolean isChartElementCaught() 
        {
            return false;
        }

        @Override
        public boolean isComplexElementUnderConstruction()
        {
            return false;
        }

        @Override
        public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
        {
            return false;
        }

        @Override
        public void notifyOfToolModeLoss()
        {
            operationFinished();
        }

        @Override
        public Set<MouseInputType> getUsedMouseInputTypes()
        {
            return usedMouseInputTypes;
        }

        @Override
        public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex,
                PlotRenderingInfo info) {            
        }        

        @Override
        public void addMouseToolListener(MouseInteractiveToolListener listener)
        {
            listenerSupport.addMouseToolListener(listener);
        }

        @Override
        public void removeMouseToolListerner(MouseInteractiveToolListener listener) 
        {
            listenerSupport.removeMouseListener(listener);
        }       
    }
}
