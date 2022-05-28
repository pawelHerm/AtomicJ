package atomicJ.gui.save;

import java.io.File;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

public interface ZippableFrameFormatSaver 
{
    public void saveAsZip(JFreeChart freeChart, File path, String entryName, ChartRenderingInfo info);
}
