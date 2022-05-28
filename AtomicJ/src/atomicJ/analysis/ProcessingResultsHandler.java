package atomicJ.analysis;

import java.awt.Component;
import java.util.List;

public interface ProcessingResultsHandler<Y>
{
    public void acceptAndSegregateResults(List<Y> results);
    public void sendResultsToDestination();
    public void reactToFailures(int failuresCount);
    public void reactToCancellation();
    public void endProcessing();
    public Component getAssociatedComponent();
}