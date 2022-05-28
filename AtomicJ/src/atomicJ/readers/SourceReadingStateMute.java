package atomicJ.readers;

public class SourceReadingStateMute implements SourceReadingState
{ 
    private int progress = 0;
    private int absoluteProgress = 0;
    private final int problemSize;
    private boolean outOfJob = false;

    public SourceReadingStateMute(int problemSize)
    {
        this.problemSize = problemSize;
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
        int progress = (int)Math.rint(100.*absoluteProgress/problemSize);
        setProgress(progress, absoluteProgessNew);  
    }

    @Override
    public void setProgress(final int progressNew, int absoluteProgress)
    {
        this.absoluteProgress = absoluteProgress;
        this.progress = progressNew;
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
        this.outOfJob = true;
    }

}