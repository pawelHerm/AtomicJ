package atomicJ.gui.curveProcessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.results.ResultView;
import atomicJ.sources.Channel1DSource;

public class NumericalResultsSourceReplaceHandler<S extends Channel1DSource<?>, E extends Processed1DPack<E,S>> implements NumericalResultsHandler<E>
{
    private final List<S> sourcesToRemove;
    private final ResultView<S,E> resultDialog;

    public NumericalResultsSourceReplaceHandler(ResultView<S,E> resultDialog, Collection<S> sourcesToRemove)
    {
        this.sourcesToRemove = new ArrayList<>(sourcesToRemove);
        this.resultDialog = resultDialog;
    }

    @Override
    public void handlePublicationRequest(Collection<Batch<E>> results)
    {
        resultDialog.publishResults(results);
        resultDialog.setVisible(true);

        resultDialog.removeSources(sourcesToRemove);
    }
}