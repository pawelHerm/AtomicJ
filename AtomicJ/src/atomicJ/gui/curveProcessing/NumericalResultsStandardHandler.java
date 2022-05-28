package atomicJ.gui.curveProcessing;

import java.util.Collection;
import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.results.ResultView;

public class NumericalResultsStandardHandler<E extends Processed1DPack<E,?>> implements NumericalResultsHandler <E>
{
    private final ResultView<?,E> resultDialog;

    public NumericalResultsStandardHandler(ResultView<?,E> resultDialog)
    {
        this.resultDialog = resultDialog;
    }

    @Override
    public void handlePublicationRequest(Collection<Batch<E>> results) 
    {
        resultDialog.publishResults(results);
        resultDialog.setVisible(true);
    }
}
