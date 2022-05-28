package atomicJ.gui.curveProcessing;

import java.util.Collection;
import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;

public interface NumericalResultsHandler <E extends Processed1DPack<E,?>>
{
    public void handlePublicationRequest(Collection<Batch<E>> results);
}
