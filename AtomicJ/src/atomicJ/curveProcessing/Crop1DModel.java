package atomicJ.curveProcessing;

import atomicJ.analysis.CropSettings;
import atomicJ.data.Channel1D;
import atomicJ.data.PermissiveChannel1DFilter;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.utilities.MathUtilities;

public class Crop1DModel<R extends SpectroscopyResource> extends CurveBatchProcessingModel<R>
{
    private static final double TOLERANCE = 1e-12;

    public static final String CROP_LEFT = "CropLeft";
    public static final String CROP_RIGHT = "CropRight";

    public static final String CROP_TOP = "CropTop";
    public static final String CROP_BOTTOM = "CropBottom";

    private double cropLeft = 0;
    private double cropRight = 0;
    private double cropBottom = 0;
    private double cropTop = 0;

    private final PrefixedUnit domainCroppingUnit;
    private final PrefixedUnit rangeCroppingUnit;
    private final PrefixedUnit rangeDataUnit;
    private final PrefixedUnit domainDataUnit;

    public Crop1DModel(ResourceView<R, Channel1D, String> manager)
    {
        super(manager, PermissiveChannel1DFilter.getInstance(), false, true);

        this.rangeDataUnit = manager.getDataUnit();
        this.domainDataUnit = manager.getDomainDataUnits().get(0);
        this.domainCroppingUnit = getDomainXAxisDisplayedUnit();
        this.rangeCroppingUnit = getValueAxisDisplayedUnit(); //the displayed units may change while the cropping dialog is displayed
        //so we must get the initial values
    }

    public PrefixedUnit getDomainCroppingUnit()
    {
        return domainCroppingUnit;
    }

    public PrefixedUnit getRangeCroppingUnit()
    {
        return rangeCroppingUnit;
    }

    public double getCropLeft()
    {
        return cropLeft;
    }

    public void setCropLeft(double cropLeftNew)
    {       
        if(cropLeftNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'cropLeftNew' must be grater or equal 0");
        }

        if(!MathUtilities.equalWithinTolerance(this.cropLeft, cropLeftNew, TOLERANCE))
        {
            double cropLeftOld = this.cropLeft;
            this.cropLeft = cropLeftNew;

            firePropertyChange(CROP_LEFT, cropLeftOld, cropLeftNew);

            updatePreview();
        }
    } 

    public double getCropRight()
    {
        return cropRight;
    }

    public void setCropRight(double cropRightNew)
    {               
        if(cropRightNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'cropRightNew' must be grater or equal 0");
        }

        if(!MathUtilities.equalWithinTolerance(this.cropRight, cropRightNew, TOLERANCE))
        {
            double cropRightOld = this.cropRight;
            this.cropRight = cropRightNew;

            firePropertyChange(CROP_RIGHT, cropRightOld, cropRightNew);

            updatePreview();
        }
    }

    public double getCropBottom()
    {
        return cropBottom;
    }

    public void setCropBottom(double cropBottomNew)
    {       
        if(cropBottomNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'cropBottomNew' must be grater or equal 0");
        }

        if(!MathUtilities.equalWithinTolerance(this.cropBottom, cropBottomNew, TOLERANCE))
        {
            double cropBottomOld = this.cropBottom;
            this.cropBottom = cropBottomNew;

            firePropertyChange(CROP_BOTTOM, cropBottomOld, cropBottomNew);

            updatePreview();
        }
    }

    public double getCropTop()
    {
        return cropTop;
    }

    public void setCropTop(double cropTopNew)
    {       
        if(cropTopNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'cropTopNew' must be grater or equal 0");
        }

        if(!MathUtilities.equalWithinTolerance(this.cropTop, cropTopNew, TOLERANCE))
        {
            double cropTopOld = this.cropTop;
            this.cropTop = cropTopNew;

            firePropertyChange(CROP_TOP, cropTopOld, cropTopNew);

            updatePreview();
        }
    }

    private CropSettings buildCropSettings()
    {
        double domainUnitFactor = domainCroppingUnit.getConversionFactorTo(domainDataUnit);
        double rangeUnitFactor = rangeCroppingUnit.getConversionFactorTo(rangeDataUnit);

        CropSettings cropSettings = new CropSettings(rangeUnitFactor*cropTop, domainUnitFactor*cropLeft, rangeUnitFactor*cropBottom, domainUnitFactor*cropRight);

        return cropSettings;
    }

    @Override
    protected Channel1DDataInROITransformation buildTransformation()
    {
        Channel1DDataInROITransformation tr = new Crop1DTransformation(buildCropSettings());
        return tr;
    }
}
