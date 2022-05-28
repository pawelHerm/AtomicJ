package atomicJ.readers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ProgressMonitor;

public final class SourceReadingProgressListener implements PropertyChangeListener 
{
    private final ProgressMonitor progressMonitor;
    private final SourceReadingState mapReadingState;

    public SourceReadingProgressListener(ProgressMonitor progressMonitor, SourceReadingState readingState) 
    {
        this.progressMonitor = progressMonitor;
        this.mapReadingState = readingState;
    }

    @Override
    public  void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();
        if (progressMonitor.isCanceled()) 
        {
            mapReadingState.setOutOfJob();
            return;
        } 
        if (SourceReadingState.PROGRESS.equals(property)) 
        {
            int progress = mapReadingState.getProgress();
            int absoluteProgress = mapReadingState.getAbsoluteProgress();
            int problemSize = mapReadingState.getProblemSize();

            String message = "Read in" + " " + progress + "%" + "  (" + absoluteProgress + " of " + problemSize +")";
            progressMonitor.setNote(message);
            progressMonitor.setProgress(progress);                          
        }
        else if(SourceReadingState.OUT_OF_JOB.equals(property))
        {
            boolean canceled = (Boolean)evt.getNewValue();           
            if(canceled)
            {
                progressMonitor.close();
            }
        }
    }
}