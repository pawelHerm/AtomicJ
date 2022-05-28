package atomicJ.gui.imageProcessing;

import java.util.Objects;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.imageProcessing.ImageLineRegressionFunction;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.LineFitCorrection;
import atomicJ.imageProcessing.LineFitRegressionStrategy;
import atomicJ.imageProcessing.SampleFunctional;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.Validation;


public class LineFitCorrectionModel extends ImageBatchROIProcessingModel
{
    public static final String REGRESSION_STRATEGY = "RegressionStrategy";
    public static final String FIT_DEGREE = "FitDegree";
    public static final String IMAGE_LINE_ORIENTATION = "ImageLineOrientation";
    public static final String MINIMAL_LINE_LENGTH_PERCENT = "MinimalLineWidthPercent";

    private double minimalLineLengthPercent = 5;

    private int deg = 1;
    private LineFitRegressionStrategy regressionStrategy = LineFitRegressionStrategy.CLASSICAL_L2;
    private ImageLineOrientation lineOrientation = ImageLineOrientation.HORIZONTAL;

    public LineFitCorrectionModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter)
    {
        super(manager, channelFilter, true, false);
    }

    public LineFitRegressionStrategy getRegressionStrategy()
    {
        return regressionStrategy;
    }

    public void setRegessionStrategy(LineFitRegressionStrategy strategyNew)
    {
        if(!Objects.equals(regressionStrategy, strategyNew))
        {
            LineFitRegressionStrategy strategyOld = this.regressionStrategy;
            this.regressionStrategy = strategyNew;

            firePropertyChange(REGRESSION_STRATEGY, strategyOld, strategyNew);

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public int getFitDegree()
    {
        return deg;
    }

    public void setFitDegree(int degreeNew)
    {
        Validation.requireNonNegativeParameterName(degreeNew, "degreeNew");

        if(this.deg != degreeNew)
        {
            int degreeOld = this.deg;
            this.deg = degreeNew;

            firePropertyChange(FIT_DEGREE, degreeOld, degreeNew);
            updatePreview();
        }
    }

    public ImageLineOrientation getLineOrientation()
    {
        return lineOrientation;
    }

    public void setLineOrientation(ImageLineOrientation orientationNew)
    {
        if(!Objects.equals(this.lineOrientation, orientationNew))
        {
            ImageLineOrientation orientationOld = this.lineOrientation;
            this.lineOrientation = orientationNew;

            firePropertyChange(IMAGE_LINE_ORIENTATION, orientationOld, orientationNew);

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public double getMinimalLineLengthPercent()
    {
        return minimalLineLengthPercent;
    }

    public void setMinimalLineLengthPercent(double minimalLineLengthPercentNew)
    {
        if(minimalLineLengthPercentNew < 0 || minimalLineLengthPercentNew >100)
        {
            throw new IllegalArgumentException("minimalLineLengthPercentNew' must be a real number between 0 and 100 inclusive");
        }

        if(Double.compare(this.minimalLineLengthPercent, minimalLineLengthPercentNew) != 0)
        {
            double minimalLineLengthPercentOld = this.minimalLineLengthPercent;
            this.minimalLineLengthPercent = minimalLineLengthPercentNew;

            firePropertyChange(MINIMAL_LINE_LENGTH_PERCENT, minimalLineLengthPercentOld, minimalLineLengthPercentNew);

            updatePreview();
        }
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        boolean columnWiseCorrection = ImageLineOrientation.VERTICAL.equals(lineOrientation);

        SampleFunctional f = new ImageLineRegressionFunction(regressionStrategy, deg);
        Channel2DDataInROITransformation tr = new LineFitCorrection(f, 0.01*minimalLineLengthPercent, columnWiseCorrection);

        return tr;
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() &&((regressionStrategy != null) && (lineOrientation != null)
                && !Double.isNaN(minimalLineLengthPercent));

        return applyEnabled;
    }
}
