package atomicJ.gui;

import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

public class Layer 
{
    private final Object key;

    private final XYItemRenderer renderer;
    private final XYDataset dataset;

    public Layer(Object key, XYDataset dataset, XYItemRenderer renderer)
    {
        this.key = key;
        this.renderer = renderer;
        this.dataset = dataset;
    }

    public Object getKey()
    {
        return key;
    }

    public XYItemRenderer getRenderer()
    {
        return renderer;
    }

    public XYDataset getDataset()
    {
        return dataset;
    }
}
