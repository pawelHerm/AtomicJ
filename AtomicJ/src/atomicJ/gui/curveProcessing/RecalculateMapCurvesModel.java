package atomicJ.gui.curveProcessing;

import java.util.Map;

import atomicJ.gui.generalProcessing.OperationModel;
import atomicJ.gui.rois.ROI;

public class RecalculateMapCurvesModel extends OperationModel
{
    public static final String MODIFY_MAPS_IN_PLACE = "ModifyMapsInPlace";
    public static final String DELETE_OLD_CURVE_CHARTS = "DeleteOldCurveCharts";
    public static final String DELETE_OLD_NUMERICAL_RESULTS = "DeleteOldNumericalResults";

    private boolean modifyMapsInPlace = true;
    private boolean deleteOldCurveCharts = true;
    private boolean deleteOldNumericalResults = true;

    public RecalculateMapCurvesModel(Map<Object, ROI> rois, ROI roiUnion) {
        super(rois, roiUnion);
    }

    public boolean isModifyMapsInPlace()
    {
        return modifyMapsInPlace;
    }

    public void setModifyMapsInPlace(boolean modifyMapsInPlaceNew)
    {
        boolean modifyMapsInPlaceOld = this.modifyMapsInPlace;
        this.modifyMapsInPlace = modifyMapsInPlaceNew;

        firePropertyChange(MODIFY_MAPS_IN_PLACE, modifyMapsInPlaceOld, modifyMapsInPlaceNew);
    }

    public boolean isDeleteOldCurveCharts()
    {
        return deleteOldCurveCharts;
    }

    public void setDeleteOldCurveCharts(boolean deleteOldCurveChartsNew)
    {
        boolean deleteOldCurveChartsOld = this.deleteOldCurveCharts;
        this.deleteOldCurveCharts = deleteOldCurveChartsNew;

        firePropertyChange(DELETE_OLD_CURVE_CHARTS, deleteOldCurveChartsOld, deleteOldCurveChartsNew);
    }

    public boolean isDeleteOldNumericalResults()
    {
        return deleteOldNumericalResults;
    }

    public void setDeleteOldNumericalResults(boolean deleteOldNumericalResultsNew)
    {
        boolean deleteOldNumericalResultsOld = this.deleteOldNumericalResults;
        this.deleteOldNumericalResults = deleteOldNumericalResultsNew;

        firePropertyChange(DELETE_OLD_NUMERICAL_RESULTS, deleteOldNumericalResultsOld, deleteOldNumericalResultsNew);

    }

    @Override
    public void reset() {        
    }

}
