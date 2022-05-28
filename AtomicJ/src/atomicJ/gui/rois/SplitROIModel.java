package atomicJ.gui.rois;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.ObjectUtilities;

import atomicJ.gui.Channel2DSupervisor;
import atomicJ.gui.MouseInteractiveTool;
import atomicJ.gui.MouseInputType;
import atomicJ.gui.ROICurveManager;
import atomicJ.gui.generalProcessing.BasicOperationModel;
import atomicJ.gui.profile.ProfileStyle;
import atomicJ.gui.rois.line.ROICurve;
import atomicJ.gui.rois.line.ROICurveType;


public class SplitROIModel extends BasicOperationModel
{
    public static final String DELETE_ORIGINAL_ROIS = "DeleteOriginalROIs";
    public static final String SPLITTING_CURVE_TYPE = "SplittingCurveType";

    private final Channel2DSupervisor supervisor;

    private ROICurveType splittingCurveType = ROICurveType.LINE;

    private boolean deleteOriginalROIs = true;
    private final SplitROIManager splittingTool = new SplitROIManager();

    public SplitROIModel(Channel2DSupervisor supervisor)
    {
        this.supervisor = supervisor;
    }

    public MouseInteractiveTool getMouseTool()
    {
        return splittingTool;
    }

    public boolean isDeleteOriginalROIs()
    {
        return deleteOriginalROIs;
    }

    public void setDeleteOriginalROIs(boolean deleteOriginalROIsNew)
    {
        boolean deleteOriginalROIsOld = this.deleteOriginalROIs;
        this.deleteOriginalROIs = deleteOriginalROIsNew;

        firePropertyChange(DELETE_ORIGINAL_ROIS, deleteOriginalROIsOld, deleteOriginalROIsNew);
    }


    public ROICurveType getSplittingCurveType()
    {
        return splittingCurveType;
    }

    public void setSplittingCurveType(ROICurveType splittingCurveTypeNew)
    {
        if(!ObjectUtilities.equal(splittingCurveType, splittingCurveTypeNew))
        {
            ROICurveType splittingCurveTypeOld = this.splittingCurveType;
            this.splittingCurveType = splittingCurveTypeNew;

            firePropertyChange(SPLITTING_CURVE_TYPE, splittingCurveTypeOld, splittingCurveTypeNew);

            checkIfApplyEnabled();
        }
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && splittingTool.getROICurveCount() > 0 && splittingCurveType != null;
        return applyEnabled;
    }

    @Override
    public void operationFinished()
    {
        super.operationFinished();
        supervisor.stopUsingMouseInteractiveTool(splittingTool);     
    }

    @Override
    public void apply()
    {
        super.apply();

        Map<Object, ROI> roisAll = supervisor.getDrawableROIs();
        Map<Object, ROIDrawable> rois = new LinkedHashMap<>();
        for(Entry<Object, ROI> roiEntry : roisAll.entrySet())
        {
            ROI r = roiEntry.getValue();
            if(r instanceof ROIDrawable)
            {
                rois.put(roiEntry.getKey(), (ROIDrawable)r);
            }
        }

        if(deleteOriginalROIs)
        {
            for(ROIDrawable roi : rois.values())
            {
                supervisor.removeROI(roi);
            }
        }

        for(ROIDrawable roi : rois.values())
        {
            List<ROIDrawable> roiCurrentlyToSplit = new ArrayList<>(Arrays.asList(roi));
            List<ROICurve> splitLines = splittingTool.getROICurves();

            for(ROICurve curve : splitLines)
            {
                List<ROIDrawable> roiCurrentlyToSplitNew = new ArrayList<>();
                double[][] vertices = curve.getVertices();

                for(ROIDrawable path : roiCurrentlyToSplit)
                {
                    roiCurrentlyToSplitNew.addAll(path.split(vertices));

                }

                roiCurrentlyToSplit = roiCurrentlyToSplitNew;
            }

            for(ROIDrawable path : roiCurrentlyToSplit)
            {
                int keyOld = path.getKey();
                ROIDrawable r;
                if(keyOld < 0)
                {
                    int keyNew =  supervisor.getCurrentROIIndex();
                    r = path.copy(roi.getStyle(), keyNew, Integer.toString(keyNew));
                    r.setFinished(true);
                }
                else
                {
                    r = path;
                }

                supervisor.addOrReplaceROI(r);
            }

        }        
    }

    private class SplitROIManager extends ROICurveManager implements MouseInteractiveTool
    {           
        private final Preferences pref = Preferences.userNodeForPackage(SplitROIManager.class).node("SplitROIManager");

        private final List<MouseInputType> mouseInputTypes = Arrays.asList(MouseInputType.CLICKED,MouseInputType.DRAGGED, MouseInputType.MOVED, MouseInputType.PRESSED,MouseInputType.RELEASED);
        private final ProfileStyle style = new ProfileStyle(pref, Color.white);
        {
            style.setLabelVisibleFinishedHighlighted(false);
            style.setLabelVisibleFinishedStandard(false);
            style.setLabelVisibleUnfinishedHighlighted(false);
            style.setLabelVisibleUnfinishedStandard(false);
        }

        private final MouseInteractiveToolListenerSupport listenerSupport = new MouseInteractiveToolListenerSupport();

        @Override
        public void handleChangeOfDrawing() 
        { 
            listenerSupport.fireToolToRedraw();
        }

        @Override
        public void handleFinishedCurveCountChange(int countOld, int countNew)
        {            
            checkIfApplyEnabled();
        }

        @Override
        public Set<MouseInputType> getUsedMouseInputTypes()
        {
            Set<MouseInputType> inputTypes = new LinkedHashSet<>(mouseInputTypes);

            return inputTypes;
        }

        @Override
        public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
        {
            List<ROICurve> roiCurves = getROICurves();
            for(ROICurve roiCurve : roiCurves)
            {
                roiCurve.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
            }

            ROICurve curveUnderConstruction = getCurveUnderConstruction();

            if(curveUnderConstruction!= null)
            {
                curveUnderConstruction.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
            }
        }

        @Override
        public void notifyOfToolModeLoss()
        {
            operationFinished();
        }

        @Override
        public ROICurveType getROICurveType()
        {
            return splittingCurveType;
        }

        @Override
        public ProfileStyle getStyle() 
        {
            return style;
        }

        @Override
        public boolean isAppropriateMode(MouseInputType inputType) 
        {
            return mouseInputTypes.contains(inputType);
        }

        @Override
        public void requestCursorChange(Cursor horizontalCursor, Cursor verticalCursor)
        {
            supervisor.requestCursorChange(horizontalCursor, verticalCursor);
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
