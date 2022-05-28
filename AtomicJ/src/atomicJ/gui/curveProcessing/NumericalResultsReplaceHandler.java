package atomicJ.gui.curveProcessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.results.ResultView;

public class NumericalResultsReplaceHandler<E extends Processed1DPack<E,?>> implements NumericalResultsHandler<E>
{
    private final List<E> packsToRemove;
    private final ResultView<?,E> resultsDialog;

    public NumericalResultsReplaceHandler(ResultView<?,E> resultsDialog, Collection<E> packsToRemove)
    {
        this.packsToRemove = new ArrayList<>(packsToRemove);
        this.resultsDialog = resultsDialog;
    }

    @Override
    public void handlePublicationRequest(Collection<Batch<E>> results)
    {
        resultsDialog.publishResults(results);
        resultsDialog.setVisible(true);

        resultsDialog.removeProcessedPacks(packsToRemove);
    }
}