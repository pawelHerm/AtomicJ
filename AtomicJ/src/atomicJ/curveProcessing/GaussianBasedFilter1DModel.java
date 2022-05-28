package atomicJ.curveProcessing;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DMinimumSizeFilter;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;

public abstract class GaussianBasedFilter1DModel<R extends SpectroscopyResource> extends CurveBatchProcessingModel<R>
{
    public static final String SIGMA_X = "Sigma_X";

    private double sigmaX = 2;

    public GaussianBasedFilter1DModel(ResourceView<R, Channel1D, String> manager)
    {
        super(manager, new Channel1DMinimumSizeFilter(3), false, false);
    }

    public double getSigmaX()
    {
        return sigmaX;
    }

    public void setSigmaX(double sigmaXNew)
    {       
        if(sigmaXNew <= 0)
        {
            throw new IllegalArgumentException("Parameter 'sigmaX' must be grater than 0");
        }

        if(Double.compare(this.sigmaX, sigmaXNew) != 0)
        {
            double sigmaXOld = this.sigmaX;
            this.sigmaX = sigmaXNew;

            firePropertyChange(SIGMA_X, sigmaXOld, sigmaXNew);

            checkIfApplyEnabled();

            updatePreview();
        }
    }


    @Override
    protected boolean calculateApplyEnabled()
    {
        return super.calculateApplyEnabled() && (!Double.isNaN(sigmaX));
    }
}
