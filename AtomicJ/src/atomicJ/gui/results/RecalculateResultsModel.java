package atomicJ.gui.results;

import atomicJ.analysis.Processed1DPack;
import atomicJ.sources.Channel1DSource;

public class RecalculateResultsModel<S extends Channel1DSource<?>, E extends Processed1DPack<E,S>> extends BatchBasedModel<S,E>
{
    public static final String DELETE_OLD_CURVE_CHARTS = "DeleteOldCurveCharts";
    public static final String DELETE_OLD_NUMERICAL_RESULTS = "DeleteOldNumericalResults";

    private boolean deleteOldCurveCharts = true;
    private boolean deleteOldNumericalResults = true;

    public RecalculateResultsModel(ResultDataModel<S,E> dataModel) {
        super(dataModel);
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
