package atomicJ.readers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import atomicJ.gui.AtomicJ;


public class SourceReadingStateMonitored implements SourceReadingState
{ 
    public static final String FORCE_VOLUME_PROBLEM = "Reading force volume map";
    public static final String IAMGE_PROBLEM = "Reading images";
    public static final String SOURCE_PROBLEM = "Reading files";

    private int progress = 0;
    private int absoluteProgress = 0;
    private final int problemSize;
    private boolean outOfJob = false;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public SourceReadingStateMonitored(int problemSize, String problemName)
    {
        this.problemSize = problemSize;
        initializeProgressMonitor(problemName);
    }

    private void initializeProgressMonitor(String problemName)
    {
        final ProgressMonitor progressMonitor = new ProgressMonitor(AtomicJ.getApplicationFrame(), problemName, "", 0, 100);
        progressMonitor.setMillisToPopup(500);

        addPropertyChangeListener(new SourceReadingProgressListener(progressMonitor, this));
    }

    @Override
    public int getProblemSize()
    {
        return problemSize;
    }

    @Override
    public int getProgress()
    {
        return progress;
    }

    @Override
    public void incrementAbsoluteProgress()
    {
        int absoluteProgessNew = absoluteProgress + 1;
        int progress = (int)Math.rint(100.*absoluteProgessNew/problemSize);

        setProgress(progress, absoluteProgessNew);  
    }

    @Override
    public void setProgress(final int progressNew, int absoluteProgress)
    {
        this.absoluteProgress = absoluteProgress;

        final int progressOld = this.progress;
        this.progress = progressNew;

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() 
            {
                firePropertyChange(SourceReadingState.PROGRESS, progressOld, progressNew);
            }			
        });
    }

    @Override
    public int getAbsoluteProgress()
    {
        return absoluteProgress;
    }

    @Override
    public boolean isOutOfJob()
    {
        return outOfJob;
    }

    @Override
    public void setOutOfJob()
    {
        final boolean canceledOld = this.outOfJob;
        this.outOfJob = true;

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() 
            {
                firePropertyChange(SourceReadingState.OUT_OF_JOB, canceledOld, true);
            }			
        });
    }

    private void addPropertyChangeListener(PropertyChangeListener listener) 
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) 
    {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

}