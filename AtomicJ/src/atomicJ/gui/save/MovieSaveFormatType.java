package atomicJ.gui.save;

public interface MovieSaveFormatType extends ChartSaveFormatType
{
    public void specifyInitialMovieParameters(int firstFrame, int lastFrame, int frameCount, int frameRate, boolean playedBackwards);

    @Override
    public default boolean supportsArchives()
    {
        return true;
    }
}
