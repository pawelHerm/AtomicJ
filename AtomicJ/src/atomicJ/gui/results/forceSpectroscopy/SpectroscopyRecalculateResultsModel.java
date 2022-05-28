package atomicJ.gui.results.forceSpectroscopy;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.analysis.BatchUtilities;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.SpectroscopyResultDestination;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.gui.results.RecalculateResultsModel;
import atomicJ.gui.results.ResultDataEvent;
import atomicJ.sources.IdentityTag;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.MultiMap;


public class SpectroscopyRecalculateResultsModel extends RecalculateResultsModel<SimpleSpectroscopySource,ProcessedSpectroscopyPack>
{
    public static final String MODIFY_MAPS_IN_PLACE = "ModifyMapsInPlace";
    public static final String INCLUDED_IN_MAPS = "IncludedInMaps";

    private boolean modifyMapsInPlace = true;
    private boolean includedInMaps;

    public SpectroscopyRecalculateResultsModel(SpectroscopyResultDataModel dataModel)
    {
        super(dataModel);

        this.includedInMaps = dataModel.containsPacksFromMap(isRestrictToSelection());
    }

    public boolean isIncludedInMaps()
    {
        return includedInMaps;
    }

    private void checkIfIncludedInMapsChanged()
    {
        boolean includedInMapsNew = ((SpectroscopyResultDataModel)getDataModel()).containsPacksFromMap(isRestrictToSelection());

        boolean includedInMapsOld = this.includedInMaps;
        this.includedInMaps = includedInMapsNew;

        firePropertyChange(INCLUDED_IN_MAPS, includedInMapsOld, includedInMapsNew);
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

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void handleChangeSelectionRestriction()
    {
        super.handleChangeSelectionRestriction();
        checkIfIncludedInMapsChanged();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();
        if(SpectroscopyResultSelectionModel.SELECTED_PACKS_FROM_MAPS.equals(property))
        {
            checkIfIncludedInMapsChanged();
        }
    }

    @Override
    public void batchesCleared(ResultDataEvent<SimpleSpectroscopySource,ProcessedSpectroscopyPack> event)
    {
        super.batchesCleared(event);
        checkIfIncludedInMapsChanged();
    }

    @Override
    public void batchesAdded(ResultDataEvent<SimpleSpectroscopySource,ProcessedSpectroscopyPack> event) 
    {
        super.batchesAdded(event);
        checkIfIncludedInMapsChanged();
    }

    @Override
    public void batchesRemoved(ResultDataEvent<SimpleSpectroscopySource,ProcessedSpectroscopyPack> event) 
    {
        super.batchesRemoved(event);
        checkIfIncludedInMapsChanged();

    }

    @Override
    public void packsAdded(ResultDataEvent<SimpleSpectroscopySource,ProcessedSpectroscopyPack> event)
    {
        super.packsAdded(event);
        checkIfIncludedInMapsChanged();

    }

    @Override
    public void packsRemoved(ResultDataEvent<SimpleSpectroscopySource,ProcessedSpectroscopyPack> event) 
    {
        super.packsRemoved(event);
        checkIfIncludedInMapsChanged();
    }


    @Override
    public void apply()
    {
        super.apply();
        recalculatePacks(AtomicJ.getResultDestination(), getPacks());
    }

    private void recalculatePacks(SpectroscopyResultDestination resultDestination, List<ProcessedSpectroscopyPack> packs)
    {
        MultiMap<IdentityTag, ProcessedSpectroscopyPack> batches = BatchUtilities.segregateIntoBatches(packs);
        List<ProcessingBatchModel> batchModels = new ArrayList<>();
        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> allSourcesNewVsOld = new LinkedHashMap<>();

        for(Entry<IdentityTag, List<ProcessedSpectroscopyPack>> entry : batches.entrySet())
        {
            IdentityTag batchId = entry.getKey();
            String name = batchId.getLabel();
            int index = (int)batchId.getKey();

            List<SimpleSpectroscopySource> sourcesToCopy = new ArrayList<>();
            for(ProcessedSpectroscopyPack pack : entry.getValue())
            {
                sourcesToCopy.add(pack.getSource());
            }

            Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = modifyMapsInPlace ? StandardSimpleSpectroscopySource.copySourcesInPlace(sourcesToCopy) : StandardSimpleSpectroscopySource.copySources(sourcesToCopy);
            allSourcesNewVsOld.putAll(sourcesNewVsOld);

            ProcessingBatchModel model = new ProcessingBatchModel(resultDestination, new ArrayList<>(sourcesNewVsOld.keySet()), name, index);
            batchModels.add(model);
        }

        resultDestination.recalculate(batchModels, allSourcesNewVsOld, packs, isDeleteOldNumericalResults(), isDeleteOldCurveCharts(), modifyMapsInPlace);      
    }

}
