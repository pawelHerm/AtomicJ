package atomicJ.curveProcessing;

import atomicJ.data.Channel1D;
import atomicJ.data.PermissiveChannel1DFilter;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.utilities.MathUtilities;

public class Translate1DModel<R extends SpectroscopyResource> extends CurveBatchProcessingModel<R>
{
    private static final double TOLERANCE = 1e-12;

    public static final String TRANSLATION_X = "TranslationX";
    public static final String TRANSLATION_Y = "TranslationY";

    private double translationX = 0;
    private double translationY = 0;

    private final PrefixedUnit rangeDataUnit;
    private final PrefixedUnit domainDataUnit;

    public Translate1DModel(ResourceView<R, Channel1D, String> manager)
    {
        super(manager, PermissiveChannel1DFilter.getInstance(), false, false);

        this.rangeDataUnit = manager.getDataUnit();
        this.domainDataUnit = Units.MICRO_METER_UNIT;
    }

    public double getTranslationX()
    {
        return translationX;
    }

    public void setTranslationX(double translationXNew)
    {       
        if(!MathUtilities.equalWithinTolerance(this.translationX, translationXNew, TOLERANCE))
        {
            double translationXOld = this.translationX;
            this.translationX = translationXNew;

            firePropertyChange(TRANSLATION_X, translationXOld, translationXNew);
            updatePreview();
        }
    } 

    public double getTranslationY()
    {
        return translationY;
    }

    public void setTranslationY(double translationYNew)
    {  
        if(!MathUtilities.equalWithinTolerance(this.translationY, translationYNew, TOLERANCE))
        {
            double translationYOld = this.translationY;
            this.translationY = translationYNew;

            firePropertyChange(TRANSLATION_Y, translationYOld, translationYNew);
            updatePreview();
        }
    }

    @Override
    public Channel1DDataInROITransformation buildTransformation()
    {
        double domainUnitFactor = getDomainXAxisDisplayedUnit().getConversionFactorTo(domainDataUnit);
        double rangeUnitFactor = getValueAxisDisplayedUnit().getConversionFactorTo(rangeDataUnit);

        Channel1DDataInROITransformation tr = new Translate1DTransformation(domainUnitFactor*translationX, rangeUnitFactor*translationY);

        return tr;
    }
}
