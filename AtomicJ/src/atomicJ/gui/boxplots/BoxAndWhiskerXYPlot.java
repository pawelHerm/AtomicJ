package atomicJ.gui.boxplots;

import java.util.List;
import java.util.prefs.Preferences;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYDataset;

import atomicJ.data.Datasets;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.gui.AxisType;
import atomicJ.gui.CustomizableNamedNumberAxis;
import atomicJ.gui.CustomizableNumberAxis;
import atomicJ.gui.CustomizableXYBasePlot;
import atomicJ.gui.PreferredAxisStyle;
import atomicJ.gui.StandardStyleTag;

public class BoxAndWhiskerXYPlot extends CustomizableXYBasePlot
{
    private static final long serialVersionUID = 1L;
    private static final Preferences pref = 
            Preferences.userNodeForPackage(BoxAndWhiskerXYPlot.class).node(BoxAndWhiskerXYPlot.class.getName());
    private static final Preferences prefDomainAxis = pref.node(AxisType.DOMAIN.toString());
    private static final Preferences prefRangeAxis = pref.node(AxisType.RANGE.toString());

    private static final PreferredAxisStyle preferredDomainStyle = new PreferredAxisStyle(prefDomainAxis);
    private static final PreferredAxisStyle preferredRangeStyle = new PreferredAxisStyle(prefRangeAxis);

    public BoxAndWhiskerXYPlot(XYBoxAndWhiskerIndexDataset dataset, String domainAxisName)
    {
        super(pref, Datasets.BOX_AND_WHISKER_PLOT);

        Quantity datasetQuantity = dataset.getDataQuantity();
        Quantity dataQuantity = datasetQuantity == null ? new DimensionlessQuantity("") : datasetQuantity;
        String quantityName = dataQuantity.getName();

        ValueAxis domainAxis = new CustomizableNamedNumberAxis(domainAxisName, dataset.getCategories(), preferredDomainStyle);
        ValueAxis rangeAxis = new CustomizableNumberAxis(dataQuantity, preferredRangeStyle);

        CustomizableXYBoxAndWhiskerRenderer renderer = new CustomizableXYBoxAndWhiskerRenderer(new StandardStyleTag(quantityName), quantityName);

        setDomainAxis(domainAxis);
        setRangeAxis(rangeAxis);

        super.setDataset(0, dataset);
        setRenderer(0, renderer);
    }

    @Override
    public void setDataset(int index, XYDataset dataset)
    {
        super.setDataset(index, dataset);

        if(dataset instanceof XYBoxAndWhiskerIndexDataset)
        {
            ValueAxis domainAxis = getDomainAxis();

            if(domainAxis instanceof CustomizableNamedNumberAxis)
            {
                CustomizableNamedNumberAxis  customizableDomainAxis = (CustomizableNamedNumberAxis) domainAxis;
                List<String> categories = ((XYBoxAndWhiskerIndexDataset) dataset).getCategories();
                customizableDomainAxis.setCategories(categories); 
            }

            ValueAxis rangeAxis = getRangeAxis();


            Quantity datasetQuantity = ((XYBoxAndWhiskerIndexDataset)dataset).getDataQuantity();
            Quantity dataQuantity = datasetQuantity== null ? new DimensionlessQuantity("") : datasetQuantity;

            if(rangeAxis instanceof CustomizableNumberAxis)
            {
                CustomizableNumberAxis  customizableRangeAxis = (CustomizableNumberAxis) rangeAxis;
                customizableRangeAxis.setDataQuantity(dataQuantity);
            }
        }       
    }
}
