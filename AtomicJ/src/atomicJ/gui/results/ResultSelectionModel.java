package atomicJ.gui.results;

import java.util.ArrayList;
import java.util.List;


import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.AbstractModel;
import atomicJ.utilities.MultiMap;

public class ResultSelectionModel <E extends Processed1DPack<E,?>> extends AbstractModel 
{
    public static final String SELECTED_PACKS = "SelectedProcessedPacks";
    public static final String BATCHES_WITH_SELECTED_PACKS = "BatchesWithSelectedPacks";
    public static final String SELECTED_PACKS_AVAILABLE = "SelectedPacksAvailable";

    private boolean selectedPacksFromMap;
    private MultiMap<Batch<E>, E> selected = new MultiMap<>();

    public List<E> getSelectedProcessedPacks()
    {
        return selected.allValues();
    }

    public List<E> getSelectedProcessedPacks(Batch<E> batch)
    {
        return new ArrayList<>(selected.get(batch));
    }

    public List<Batch<E>> getBatchesWithSelectedPacks()
    {
        return new ArrayList<>(selected.keySet());
    }

    public boolean isSelectedPacksAvailable()
    {
        boolean packsSelected = !selected.isEmpty();
        return packsSelected;
    }

    public void setSelectedPacks(MultiMap<Batch<E>, E> selectedNew)
    {
        List<E> packsSelectedOld = selected.allValues();
        List<Batch<E>> batchesOld = new ArrayList<>(selected.keySet());
        boolean isPacksSelectedOld = !selected.isEmpty();

        List<E> packsSelectedNew = selectedNew.allValues();
        List<Batch<E>> batchesNew = new ArrayList<>(selectedNew.keySet());
        boolean isPacksSelectedNew = !selectedNew.isEmpty();

        this.selected = selectedNew;

        firePropertyChange(SELECTED_PACKS, packsSelectedOld, packsSelectedNew);
        firePropertyChange(BATCHES_WITH_SELECTED_PACKS, batchesOld, batchesNew);
        firePropertyChange(SELECTED_PACKS_AVAILABLE, isPacksSelectedOld, isPacksSelectedNew);
    }

    public boolean isSelectedPacksFromMap()
    {
        return selectedPacksFromMap;
    } 
}
