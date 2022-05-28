package atomicJ.gui;

import java.awt.Shape;

import org.jfree.chart.entity.ChartEntity;

public class NamedAxisCategoryEntity extends ChartEntity
{
    private static final long serialVersionUID = 1L;

    private final String label;
    private final String category;
    private final CustomizableNamedNumberAxis axis;

    public NamedAxisCategoryEntity(Shape area, String label, CustomizableNamedNumberAxis axis) 
    {
        super(area);

        this.label = label;
        this.category = axis.getCategory(label);
        this.axis = axis;
    }

    public String getLabel()
    {
        return label;
    }

    public String getCategory()
    {
        return category;
    }

    public CustomizableNamedNumberAxis getAxis()
    {
        return axis;
    } 
}
