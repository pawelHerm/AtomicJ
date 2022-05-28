package atomicJ.gui.imageProcessing;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public abstract class GaussianBasedFilter2DModel extends ImageBatchROIProcessingModel
{
    public static final String SIGMA_X = "Sigma_X";
    public static final String SIGMA_Y = "Sigma_Y";

    private double sigmaX = 2;
    private double sigmaY = 2;

    public GaussianBasedFilter2DModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter, boolean modifyAllTypes)
    {
        super(manager, channelFilter, modifyAllTypes, false);
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

    public double getSigmaY()
    {
        return sigmaY;
    }

    public void setSigmaY(double sigmaYNew)
    {
        if(sigmaYNew <= 0)
        {
            throw new IllegalArgumentException("Parameter 'sigmaYNew' must be grater than 0");
        }

        if(Double.compare(this.sigmaY, sigmaYNew) != 0)
        {
            double sigmaYOld = this.sigmaY;
            this.sigmaY = sigmaYNew;

            firePropertyChange(SIGMA_Y, sigmaYOld, sigmaYNew);

            checkIfApplyEnabled();

            updatePreview();
        }
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        return super.calculateApplyEnabled() && (!Double.isNaN(sigmaX) && !Double.isNaN(sigmaY));
    }
}
