package atomicJ.gui;

import java.util.prefs.Preferences;

import atomicJ.data.Data1D;
import atomicJ.gui.Channel1DPlot.Channel1DPlotFactory;

public class ForceCurvePlotFactory implements Channel1DPlotFactory
{    
    private static final Preferences PREF = Preferences.userNodeForPackage(ForceCurvePlotFactory.class).node(ForceCurvePlotFactory.class.getName());

    private static final PreferredScaleBarStyle PREFERRED_DOMAIN_SCALE_BAR = new PreferredScaleBarStyle(PREF.node("DomainScaleBar"));
    private static final PreferredScaleBarStyle PREFERRED_RANGE_SCALE_BAR = new PreferredScaleBarStyle(PREF.node("RangeScaleBar"));

    private static final ForceCurvePlotFactory INSTANCE = new ForceCurvePlotFactory();

    private ForceCurvePlotFactory()
    {}

    public static ForceCurvePlotFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Channel1DPlot getPlot(Data1D dataset) 
    {
        return new Channel1DPlot(dataset, PREF, PREFERRED_DOMAIN_SCALE_BAR, PREFERRED_RANGE_SCALE_BAR);
    }       
}