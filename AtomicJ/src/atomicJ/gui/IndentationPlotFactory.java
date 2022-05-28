package atomicJ.gui;

import java.util.prefs.Preferences;

import atomicJ.data.Data1D;
import atomicJ.data.units.Quantity;
import atomicJ.gui.Channel1DPlot.Channel1DPlotFactory;

public class IndentationPlotFactory implements Channel1DPlotFactory
{
    private static IndentationPlotFactory INSTANCE = new IndentationPlotFactory();

    private IndentationPlotFactory() {};

    public static IndentationPlotFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Channel1DPlot getPlot(Data1D dataset) 
    {
        return new IndentationPlot(dataset);
    }       

    private static class IndentationPlot extends Channel1DPlot
    {
        private static final long serialVersionUID = 1L;

        private static final Preferences PREF = Preferences.userNodeForPackage(IndentationPlot.class).node("Indentation plot");

        private static final Preferences PREF_DOMAIN_SCALE_BAR = PREF.node("DomainScaleBar");
        private static final Preferences PREF_RANGE_SCALE_BAR = PREF.node("RangeScaleBar");

        private static final PreferredScaleBarStyle PREFERRED_DOMAIN_SCALE_BAR = new PreferredScaleBarStyle(PREF_DOMAIN_SCALE_BAR);
        private static final PreferredScaleBarStyle PREFERRED_RANGE_SCALE_BAR = new PreferredScaleBarStyle(PREF_RANGE_SCALE_BAR);

        public IndentationPlot(Data1D curve)
        {
            super(curve, PREF, PREFERRED_DOMAIN_SCALE_BAR, PREFERRED_RANGE_SCALE_BAR);
        }

        @Override
        protected CustomizableNumberAxis buildNewRangeAxis(Preferences pref, Quantity quantity)
        {
            CustomizableNumberAxis rangeAxis = new CustomizableNumberAxis(quantity, pref);
            rangeAxis.setAutoRangeIncludesZero(true);

            return rangeAxis;
        }
    }
}