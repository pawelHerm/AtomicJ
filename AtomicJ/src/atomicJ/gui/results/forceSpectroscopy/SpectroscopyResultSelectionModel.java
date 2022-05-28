package atomicJ.gui.results.forceSpectroscopy;

import atomicJ.analysis.Batch;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.gui.results.ResultSelectionModel;
import atomicJ.utilities.MultiMap;

public class SpectroscopyResultSelectionModel extends ResultSelectionModel <ProcessedSpectroscopyPack> 
{
    public static final String SELECTED_PACKS_FROM_MAPS = "SelectedPacksFromMap";

    private boolean selectedPacksFromMap;

    @Override
    public void setSelectedPacks(MultiMap<Batch<ProcessedSpectroscopyPack>, ProcessedSpectroscopyPack> selectedNew)
    {
        super.setSelectedPacks(selectedNew);

        boolean selectedPacksFromMapOld = this.selectedPacksFromMap;
        this.selectedPacksFromMap = ProcessedSpectroscopyPack.containsPacksOfKnownPositionInMap(selectedNew.allValues());

        firePropertyChange(SELECTED_PACKS_FROM_MAPS, selectedPacksFromMapOld, this.selectedPacksFromMap);
    }

    @Override
    public boolean isSelectedPacksFromMap()
    {
        return selectedPacksFromMap;
    } 
}
