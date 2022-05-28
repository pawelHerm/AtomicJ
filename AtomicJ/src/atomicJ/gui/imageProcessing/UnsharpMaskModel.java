package atomicJ.gui.imageProcessing;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.UnsharpMask;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public class UnsharpMaskModel extends GaussianBasedFilter2DModel
{
    public static final String SHARPENING_AMOUNT = "SharpeningAmount";
    private double amount = 0.05;

    public UnsharpMaskModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter)
    {
        super(manager, channelFilter, true);
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean enabled = !Double.isNaN(amount) && super.calculateApplyEnabled();
        return enabled;
    }

    public double getSharpeningAmount()
    {
        return amount;
    }

    public void setSharpeningAmount(double amountNew)
    {       
        if(amountNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'amountNew' must be grater or equal 0");
        }

        if(this.amount != amountNew)
        {
            double sigmaXOld = this.amount;
            this.amount = amountNew;

            firePropertyChange(SHARPENING_AMOUNT, sigmaXOld, amountNew);

            checkIfApplyEnabled();        
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

        Channel2DDataInROITransformation transformation = new UnsharpMask(getSigmaX(), getSigmaY(), amount);
        return transformation;
    }
}
