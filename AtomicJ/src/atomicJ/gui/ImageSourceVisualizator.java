package atomicJ.gui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import atomicJ.data.Channel2D;
import atomicJ.data.Datasets;
import atomicJ.sources.Channel2DSource;

public class ImageSourceVisualizator implements Channel2DSourceVisualizator
{
    private static ImageSourceVisualizator INSTANCE = new ImageSourceVisualizator();

    private ImageSourceVisualizator()
    {}

    public ImageSourceVisualizator getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Map<String,Channel2DChart<?>> getCharts(Channel2DSource<? extends Channel2D> source)
    {
        Map<String,Channel2DChart<?>> charts = new LinkedHashMap<>();

        List<? extends Channel2D> channels = source.getChannels();

        for(Channel2D channel: channels)
        {
            String channelIdentifier = channel.getIdentifier();

            Preferences pref = Preferences.userNodeForPackage(ImagePlot.class).node(ImagePlot.class.getName()).node(channelIdentifier);
            ProcessableXYZDataset dataset = Channel2DDataset.getDataset(channel, source.getShortName());

            ImagePlot plot = new ImagePlot(dataset, channelIdentifier /*plotName*/, Datasets.DENSITY_PLOT /*styleKey*/,  pref, channelIdentifier /*rendererKey*/);
            ImageChart<ImagePlot> chart = new ImageChart<>(plot, Datasets.DENSITY_PLOT);

            charts.put(channel.getIdentifier(),chart);
        }

        return charts;
    }
}
