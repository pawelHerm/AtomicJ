package atomicJ.gui;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.entity.ChartEntity;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ObjectUtilities;

public class XYLinePathEntity extends ChartEntity
{
    private static final long serialVersionUID = 1L;

    private final XYDataset dataset;
    private final int series;

    public XYLinePathEntity(Shape area, XYDataset dataset, int series, String toolTipText, String urlText) 
    {
        super(area, toolTipText, urlText);
        this.dataset = dataset;
        this.series = series;
    }

    public Object getDatasetKey()
    {
        return dataset;
    }

    public int getSeriesIndex() 
    {
        return series;
    }

    public XYDataset getDataset()
    {
        return dataset;
    }

    public boolean isPolylineInteresected(Rectangle2D dataArea)
    {
        Path2D polyline = new GeneralPath(getArea());

        return DistanceShapeFactors.intersects(polyline, dataArea);
    }

    @Override
    public boolean equals(Object that) 
    {
        if (that == this) {
            return true;
        }

        if (that instanceof XYLinePathEntity && super.equals(that)) 
        {
            XYLinePathEntity ie = (XYLinePathEntity) that;

            if(!ObjectUtilities.equal(this.dataset, ie.dataset))
            {
                return false;
            }

            if(this.series != ie.series)
            {
                return false;
            }

            return true;
        }
        return false;
    }
}
