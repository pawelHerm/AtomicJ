package atomicJ.gui.save;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.JFreeChart;

import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.stack.StackModel;
import atomicJ.utilities.IOUtilities;

public class MovieSaveModel extends SimpleSaveModel<MovieSaveFormatType>
{
    private JFreeChart chartToSave;

    public MovieSaveModel(List<MovieSaveFormatType> formatTypes)
    {
        super(formatTypes);
    }

    public void specifyChartToSave(JFreeChart chartToSave, Rectangle2D chartArea, Rectangle2D dataArea, StackModel<?> stackModel)
    {
        this.chartToSave = chartToSave;
        double dataWidth = dataArea.getWidth();
        double dataHeight = dataArea.getHeight();

        List<MovieSaveFormatType> types = getFormatTypes();
        for(MovieSaveFormatType type: types)
        {
            setFormatTypeConsistentWithStackModel(chartArea, dataWidth, dataHeight, type, stackModel);
        }
    }  

    private void setFormatTypeConsistentWithStackModel(Rectangle2D chartArea, double dataWidth, double dataHeight,
            MovieSaveFormatType type, StackModel<?> stackModel)
    {
        int firstFrame = 0;
        int frameCount = stackModel.getFrameCount();
        int lastFrame = frameCount - 1;
        int frameRate = (int)Math.rint(stackModel.getFrameRate());
        boolean playedBackwards = !stackModel.isPlayedForward();

        type.specifyInitialDimensions(chartArea, dataWidth, dataHeight);
        type.specifyInitialMovieParameters(firstFrame, lastFrame, frameCount, frameRate, playedBackwards);
    }

    @Override
    public void save() throws UserCommunicableException 
    {
        File outputFile = getOutputFile();
        MovieSaveFormatType currentFormatType = getSaveFormat();
        ChartSaver saver = currentFormatType.getChartSaver();

        try 
        {
            boolean inArchive = isSaveInArchive() && saver instanceof ZippableFrameFormatSaver;
            if(inArchive)
            {
                String entryName = IOUtilities.getBareName(outputFile) + "." + currentFormatType.getExtension();
                ((ZippableFrameFormatSaver)saver).saveAsZip(this.chartToSave, outputFile, entryName, null);
            }
            else
            {
                saver.saveChart(this.chartToSave, outputFile, null);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured during saving the file");
        }        
    }
}
