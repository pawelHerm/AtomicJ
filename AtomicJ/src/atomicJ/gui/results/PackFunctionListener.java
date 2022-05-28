package atomicJ.gui.results;

import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;

public interface PackFunctionListener <E extends Processed1DPack<E,?>>
{
    public void packFunctionAdded(ProcessedPackFunction<? super E> f);
}
