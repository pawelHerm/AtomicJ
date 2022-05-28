package atomicJ.gui;

import java.util.Objects;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.urls.XYURLGenerator;

public abstract class AbstractXYRendererDataImmutable extends AbstractRendererDataImmutable implements AbstractXYRendererData
{    
    private XYItemLabelGenerator baseItemLabelGenerator;
    private XYToolTipGenerator baseToolTipGenerator;
    private XYURLGenerator urlGenerator;

    private final XYSeriesLabelGenerator legendItemLabelGenerator;
    private XYSeriesLabelGenerator legendItemToolTipGenerator;
    private XYSeriesLabelGenerator legendItemURLGenerator;

    public AbstractXYRendererDataImmutable()
    {
        this.legendItemLabelGenerator = AbstractXYRendererData.DEFAULT_LEGEND_ITEM_LABEL_GENERATOR;
    }

    public AbstractXYRendererDataImmutable(AbstractXYRendererData data)
    {
        super(data);       
        this.legendItemLabelGenerator = data.getLegendItemLabelGenerator();
    }

    @Override
    public XYItemLabelGenerator getBaseItemLabelGenerator() {
        return this.baseItemLabelGenerator;
    }

    @Override
    public XYToolTipGenerator getBaseToolTipGenerator() {
        return this.baseToolTipGenerator;
    }

    @Override
    public XYURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemLabelGenerator() {
        return this.legendItemLabelGenerator;
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemToolTipGenerator() {
        return this.legendItemToolTipGenerator;
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemURLGenerator() {
        return this.legendItemURLGenerator;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        hashCode = 31*hashCode + Objects.hashCode(baseItemLabelGenerator);
        hashCode = 31*hashCode + Objects.hashCode(baseToolTipGenerator);
        hashCode = 31*hashCode + Objects.hashCode(urlGenerator);
        hashCode = 31*hashCode + Objects.hashCode(legendItemLabelGenerator);
        hashCode = 31*hashCode + Objects.hashCode(legendItemToolTipGenerator);
        hashCode = 31*hashCode + Objects.hashCode(legendItemURLGenerator);

        return hashCode;
    }


    @Override
    public boolean equals(Object other)
    {
        if(other instanceof AbstractXYRendererDataImmutable)
        {
            AbstractXYRendererDataImmutable that = (AbstractXYRendererDataImmutable)other;

            if(!Objects.equals(this.baseItemLabelGenerator, that.baseItemLabelGenerator))
            {
                return false;
            };
            if(!Objects.equals(this.baseToolTipGenerator, that.baseToolTipGenerator))
            {
                return false;
            };
            if(!Objects.equals(this.urlGenerator, that.urlGenerator))
            {
                return false;
            };
            if(!Objects.equals(this.legendItemLabelGenerator, that.legendItemLabelGenerator))
            {
                return false;
            };
            if(!Objects.equals(this.legendItemToolTipGenerator, that.legendItemToolTipGenerator))
            {
                return false;
            };
            if(!Objects.equals(this.legendItemURLGenerator, that.legendItemURLGenerator))
            {
                return false;
            };

            return true;
        }
        return false;
    }
}
