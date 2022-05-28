package atomicJ.gui;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import atomicJ.analysis.SpectroscopyResultDestination;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.resources.Channel1DResource;
import atomicJ.resources.SpectroscopyProcessedResource;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;


public class RecalculateSpectroscopyResourcesModel extends ResourceBasedModel<SpectroscopyProcessedResource>
{
    public static final String MODIFY_MAPS_IN_PLACE = "ModifyMapsInPlace";
    public static final String DELETE_OLD_CURVE_CHARTS = "DeleteOldCurveCharts";
    public static final String DELETE_OLD_NUMERICAL_RESULTS = "DeleteOldNumericalResults";
    public static final String INCLUDED_IN_MAPS = "IncludedInMaps";

    private boolean includedInMaps;

    private boolean modifyMapsInPlace = true;
    private boolean deleteOldCurveCharts = true;
    private boolean deleteOldNumericalResults = true;

    public RecalculateSpectroscopyResourcesModel(ResourceGroupModel<SpectroscopyProcessedResource> dataModel, ResourceGroupSelectionModel<SpectroscopyProcessedResource> selectionModel) {
        super(dataModel, selectionModel);

        this.includedInMaps = SpectroscopyResource.containsResourcesFromMap(getData());
    }

    public boolean isIncludedInMaps()
    {
        return includedInMaps;
    }

    private PropertyChangeEvent checkIfIncludedInMapsChanged()
    {
        boolean includedInMapsOld = this.includedInMaps;
        this.includedInMaps = SpectroscopyResource.containsResourcesFromMap(getData());

        PropertyChangeEvent evt = new PropertyChangeEvent(this, INCLUDED_IN_MAPS, includedInMapsOld, this.includedInMaps);
        return evt;
    }

    @Override
    protected List<PropertyChangeEvent> updateProperties(List<SpectroscopyProcessedResource> selectedResourcesNew)
    {
        List<PropertyChangeEvent> events = super.updateProperties(selectedResourcesNew);
        events.add(checkIfIncludedInMapsChanged());

        return events;
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
    public void apply()
    {
        super.apply();
        recalculatePacks(AtomicJ.getResultDestination(), Channel1DResource.getSources(getData()));
    }

    private void recalculatePacks(SpectroscopyResultDestination spectroscopyDestination, List<SimpleSpectroscopySource> sources)
    {

        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = modifyMapsInPlace ? StandardSimpleSpectroscopySource.copySourcesInPlace(sources) : StandardSimpleSpectroscopySource.copySources(sources);

        int batchNumber = spectroscopyDestination.getResultBatchesCoordinator().getPublishedBatchCount();
        String name = Integer.toString(batchNumber);

        List<ProcessingBatchModel> batchModels = Collections.singletonList(new ProcessingBatchModel(spectroscopyDestination, new ArrayList<>(sourcesNewVsOld.keySet()), name, batchNumber));
        spectroscopyDestination.recalculateSources(batchModels, sourcesNewVsOld, sources, deleteOldNumericalResults, deleteOldCurveCharts, modifyMapsInPlace);      
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

}
