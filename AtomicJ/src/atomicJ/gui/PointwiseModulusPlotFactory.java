package atomicJ.gui;

import java.util.prefs.Preferences;

import atomicJ.data.Data1D;
import atomicJ.gui.Channel1DPlot.Channel1DPlotFactory;

public class PointwiseModulusPlotFactory implements Channel1DPlotFactory
{
    private static final Preferences PREF = Preferences.userNodeForPackage(PointwiseModulusPlotFactory.class).node("Pointwise plot");
    private static final Preferences PREF_DOMAIN_SCALE_BAR = PREF.node("DomainScaleBar");
    private static final Preferences PREF_RANGE_SCALE_BAR = PREF.node("RangeScaleBar");

    private static final PreferredScaleBarStyle PREFERRED_DOMAIN_SCALE_BAR = new PreferredScaleBarStyle(PREF_DOMAIN_SCALE_BAR);
    private static final PreferredScaleBarStyle PREFERRED_RANGE_SCALE_BAR = new PreferredScaleBarStyle(PREF_RANGE_SCALE_BAR);

    private static PointwiseModulusPlotFactory INSTANCE = new PointwiseModulusPlotFactory();

    private PointwiseModulusPlotFactory() {};

    public static PointwiseModulusPlotFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Channel1DPlot getPlot(Data1D dataset) 
    {
        return new Channel1DPlot(dataset, PREF, PREFERRED_DOMAIN_SCALE_BAR, PREFERRED_RANGE_SCALE_BAR);
    }

}