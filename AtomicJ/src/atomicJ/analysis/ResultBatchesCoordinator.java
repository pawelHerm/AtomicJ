package atomicJ.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import atomicJ.gui.curveProcessing.ProcessingBatchMemento;
import atomicJ.sources.IdentityTag;

public class ResultBatchesCoordinator 
{
    private int currentBatchNumber = 1;
    private final List<IdentityTag> analysedBatches = new ArrayList<>();
    private final Map<Integer, ProcessingBatchMemento> processingBatches = new LinkedHashMap<>();

    public ProcessingBatchMemento getProcessingBatchMemento(int processingBatchId)
    {
        return processingBatches.get(processingBatchId);
    }

    public void registerProcessingBatchMemento(int processingBatchId, ProcessingBatchMemento memento)
    {
        processingBatches.put(processingBatchId, memento);
    }

    public int getPublishedBatchCount() 
    {
        return currentBatchNumber;
    }

    public void countNewBatches(Collection<IdentityTag> batchIds) 
    {
        for(IdentityTag id: batchIds)
        {
            if(!analysedBatches.contains(id))
            {
                analysedBatches.add(id);
                this.currentBatchNumber++;
            }
        }
    }
}
