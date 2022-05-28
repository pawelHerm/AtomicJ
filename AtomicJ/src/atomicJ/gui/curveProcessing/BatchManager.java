package atomicJ.gui.curveProcessing;

import java.util.ArrayList;
import java.util.List;

public class BatchManager 
{
    private int currentBatchNumber = 1;
    private final List<String> analysedBatches = new ArrayList<>();

    public int getPublishedBatchCount() 
    {
        return currentBatchNumber;
    }

    public void countNewBatches(List<String> batchNames) 
    {
        for(String batchName: batchNames)
        {
            if(!analysedBatches.contains(batchName))
            {
                analysedBatches.add(batchName);
                this.currentBatchNumber++;
            }
        }
    }
}
