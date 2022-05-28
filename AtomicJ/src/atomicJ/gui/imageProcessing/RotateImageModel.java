package atomicJ.gui.imageProcessing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.CustomChartMouseEvent;
import atomicJ.gui.MapMarkerStyle;
import atomicJ.gui.MouseInteractiveTool;
import atomicJ.gui.MouseInputType;
import atomicJ.gui.RotationCenterMarker;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.RotateTransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.GeometryUtilities;


public class RotateImageModel extends ImageBatchROIProcessingModel
{
    private static final double TOLERANCE = 1e-10;

    public static final String ROTATION_ANGLE = "RotationAngle";
    public static final String CENTER_X = "CenterX";
    public static final String CENTER_Y = "CenterY";
    public static final String FILL_VALUE = "FillValue";
    public static final String INTERPOLATION_METHOD = "InterpolationMethod";

    private double angle = 0;
    private UnitExpression centerX = new UnitExpression(1, Units.MICRO_METER_UNIT);
    private UnitExpression centerY= new UnitExpression(1, Units.MICRO_METER_UNIT);
    private UnitExpression fillValue = new UnitExpression(0, Units.MICRO_METER_UNIT);
    private InterpolationMethod2D interpolationMethod = InterpolationMethod2D.BILINEAR;

    private final RotationInteractiveTool rotationManager = new RotationInteractiveTool();

    public RotateImageModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter)
    {
        super(manager, channelFilter, true, true);

        pullInitialValues(manager);

        manager.useMouseInteractiveTool(rotationManager);
        rotationManager.setCenter(getCenterXInDataUnits().getValue(), getCenterYInDataUnits().getValue());
    }

    private void pullInitialValues(ResourceView<Channel2DResource, Channel2D, String> manager)
    {
        Channel2DResource selectedResource = manager.getSelectedResource();
        String selectedType = manager.getSelectedType();

        if(selectedResource == null || selectedType == null)
        {
            return;
        }

        Map<String, Channel2D> selectedChannels = selectedResource.getChannels(selectedType);

        Iterator<Channel2D> iterator = selectedChannels.values().iterator();

        if(iterator.hasNext())
        {
            Channel2D firstChannel = iterator.next();

            Range xRange = firstChannel.getXRange();
            Range yRange = firstChannel.getYRange();
            Range zRange = firstChannel.getZRange();

            this.centerX = new UnitExpression(xRange.getCentralValue(), firstChannel.getXQuantity().getUnit());
            this.centerY = new UnitExpression(yRange.getCentralValue(), firstChannel.getYQuantity().getUnit());
            this.fillValue = new UnitExpression(zRange.getLowerBound(), firstChannel.getZQuantity().getUnit());
        }
    }

    public InterpolationMethod2D getInterpolationMethod()
    {
        return interpolationMethod;
    }

    public void setInterpolationMethod(InterpolationMethod2D interpolationMethodNew)
    {
        if(!Objects.equals(interpolationMethod, interpolationMethodNew))
        {
            InterpolationMethod2D interpolationMethodOld = this.interpolationMethod;
            this.interpolationMethod = interpolationMethodNew;

            firePropertyChange(INTERPOLATION_METHOD, interpolationMethodOld, interpolationMethodNew);

            checkIfApplyEnabled();

            updatePreview();
        }
    }

    public UnitExpression getCenterX()
    {
        return centerX;
    }

    public void setCenterX(UnitExpression centerXNew)
    {
        if(!GeometryUtilities.almostEqual(this.centerX, centerXNew, TOLERANCE))
        {        
            UnitExpression centerXOld = this.centerX;
            this.centerX = centerXNew;

            rotationManager.setCenter(getCenterXInDataUnits().getValue(), getCenterYInDataUnits().getValue());

            firePropertyChange(CENTER_X, centerXOld, centerXNew);
            checkIfApplyEnabled();

            updatePreview();

        }
    }

    public UnitExpression getCenterY()
    {
        return centerY;
    }


    private UnitExpression getCenterXInDataUnits()
    {
        return centerX.derive(getDomainXAxisDataUnit());
    }

    private UnitExpression getCenterYInDataUnits()
    {
        return centerY.derive(getDomainYAxisDataUnit());
    }

    public void setCenterY(UnitExpression centerYNew)
    {
        if(!GeometryUtilities.almostEqual(this.centerY, centerYNew, TOLERANCE))
        {        
            UnitExpression centerYOld = this.centerY;
            this.centerY = centerYNew;

            rotationManager.setCenter(getCenterXInDataUnits().getValue(), getCenterYInDataUnits().getValue());

            firePropertyChange(CENTER_Y, centerYOld, centerYNew);
            checkIfApplyEnabled();

            updatePreview();
        }
    }

    public double getRotationAngle()
    {
        return angle;
    }

    public void setRotationAngle(double angleNew)
    {
        if(!GeometryUtilities.almostEqual(this.angle, angleNew, TOLERANCE))
        {        
            double angleOld = this.angle;
            this.angle = angleNew;

            firePropertyChange(ROTATION_ANGLE, angleOld, angleNew);
            checkIfApplyEnabled();

            updatePreview();
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
            UnitExpression fillValueOld = this.fillValue;
            this.fillValue = fillValueNew;

            firePropertyChange(FILL_VALUE, fillValueOld, fillValueNew);
            checkIfApplyEnabled();

            updatePreview();
        }
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        double angleInRadians = angle*Math.PI/180.;
        Channel2DDataInROITransformation tr = new RotateTransformation(interpolationMethod, angleInRadians, centerX, centerY, fillValue);

        return tr;
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && interpolationMethod != null && centerX != null && centerY != null && !Double.isNaN(angle);

        return applyEnabled;
    }

    @Override
    public void operationFinished()
    {
        super.operationFinished();
        getResourceManager().stopUsingMouseInteractiveTool(rotationManager);
    }

    private class RotationInteractiveTool implements MouseInteractiveTool
    {
        private Point2D caughtPoint;
        private boolean isRotationCenterCaught;

        private final RotationCenterMarker rotationCenterMarker = new RotationCenterMarker(0,0, new MapMarkerStyle(Preferences.userNodeForPackage(getClass()).node("tools"), Color.white));
        private final Set<MouseInputType> usedMouseInputTypes = new LinkedHashSet<>(Arrays.asList(MouseInputType.PRESSED, MouseInputType.RELEASED, MouseInputType.DRAGGED));

        private final MouseInteractiveToolListenerSupport listenerSupport = new MouseInteractiveToolListenerSupport();

        @Override
        public Set<MouseInputType> getUsedMouseInputTypes()
        {
            return Collections.unmodifiableSet(usedMouseInputTypes);
        }

        @Override
        public void mousePressed(CustomChartMouseEvent event) 
        {
            this.caughtPoint = event.getDataPoint();  
            this.isRotationCenterCaught = rotationCenterMarker.isClicked(event.getJava2DPoint());
        }

        @Override
        public void mouseReleased(CustomChartMouseEvent event) 
        {
            this.caughtPoint = null;
            this.isRotationCenterCaught = false;
        }

        @Override
        public void mouseDragged(CustomChartMouseEvent event) 
        {
            if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
            {
                return;
            }

            selectIdentifiersToTransform(getResourceManager().getSelectedResourcesChannelIdentifiers(getResourceManager().getSelectedType()), true);

            Point2D dataPoint = event.getDataPoint();
            if(isRotationCenterCaught)
            {
                rotationCenterMarker.setPosition(BasicAnnotationAnchor.CENTER, this.caughtPoint, dataPoint);

                UnitExpression centerXNew = new UnitExpression(rotationCenterMarker.getX(), getDomainXAxisDataUnit());
                UnitExpression centerYNew = new UnitExpression(rotationCenterMarker.getY(), getDomainYAxisDataUnit());

                setCenterX(centerXNew);
                setCenterY(centerYNew);

            }
            else
            {
                double angle = getRotationAngle() + calculateAngle(dataPoint);            
                setRotationAngle(angle);
            }

            this.caughtPoint = dataPoint;
        }

        private double calculateAngle(Point2D pointNew)
        {          
            double centerXValueUnitsUnified = centerX.getValue();
            double centerYValueUnitsUnified = centerY.derive(centerX.getUnit()).getValue();

            double angleInRadians = (this.caughtPoint != null) ? Math.atan2(caughtPoint.getY() - centerYValueUnitsUnified, caughtPoint.getX() - centerXValueUnitsUnified) 
                    - Math.atan2(pointNew.getY() - centerYValueUnitsUnified, pointNew.getX() - centerXValueUnitsUnified) : Double.NaN;

                    double angleInDegrees = angleInRadians*180/Math.PI;        
                    return angleInDegrees;
        }

        @Override
        public void mouseMoved(CustomChartMouseEvent event) 
        {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            getResourceManager().requestCursorChange(cursor, cursor);
        }

        @Override
        public void mouseClicked(CustomChartMouseEvent event) 
        {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            getResourceManager().requestCursorChange(cursor, cursor);
        }

        @Override
        public boolean isChartElementCaught() 
        {
            boolean elementCaught = caughtPoint != null;
            return elementCaught;
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

        public void setCenter(double centerXInDataUnits, double centerYInDataUnits)
        {
            this.rotationCenterMarker.setControlPoint(centerXInDataUnits, centerYInDataUnits);
        }

        @Override
        public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
        {
            rotationCenterMarker.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
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
