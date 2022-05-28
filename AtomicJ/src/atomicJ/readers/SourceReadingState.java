package atomicJ.readers;


public interface SourceReadingState
{ 
    static final String OUT_OF_JOB = "canceled";
    static final String PROGRESS = "progress";

    public int getProblemSize();
    public int getProgress();
    public void setProgress(final int progressNew, int absoluteProgress);
    public int getAbsoluteProgress();
    public boolean isOutOfJob();
    public void setOutOfJob();
    public void incrementAbsoluteProgress();
}