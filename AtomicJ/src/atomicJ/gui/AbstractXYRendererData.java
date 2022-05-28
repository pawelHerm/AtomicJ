package atomicJ.gui;

import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.urls.XYURLGenerator;

public interface AbstractXYRendererData extends AbstractRendererData
{
    public static final XYSeriesLabelGenerator DEFAULT_LEGEND_ITEM_LABEL_GENERATOR = new StandardXYSeriesLabelGenerator("{0}");//the instances are immutable

    public XYItemLabelGenerator getBaseItemLabelGenerator();
    public XYToolTipGenerator getBaseToolTipGenerator();
    public XYURLGenerator getURLGenerator();
    public XYSeriesLabelGenerator getLegendItemLabelGenerator();   
    public XYSeriesLabelGenerator getLegendItemToolTipGenerator();
    public XYSeriesLabelGenerator getLegendItemURLGenerator();
}
