package atomicJ.gui.imageProcessing;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.PolynomialFitCorrection;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.ArrayUtilities;

public class PolynomialFit2DModel extends ImageBatchROIProcessingModel
{
    public static final String DEGREE_X = "DegreeX";
    public static final String DEGREE_Y = "DegreeY";

    private int degX = 0;
    private int degY = 0;

    private int[][] appliedModel = null;

    public PolynomialFit2DModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter) 
    {
        super(manager, channelFilter, true, true);
    }

    public int getDegreeX()
    {
        return degX;
    }

    public void setDegreeX(int degXNew)
    {
        if(this.degX != degXNew)
        {
            int degXOld = this.degX;
            this.degX = degXNew;

            updatePolynomialCoefficients();

            firePropertyChange(DEGREE_X, degXOld, degXNew);

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public void setDegrees(int degXNew, int degYNew)
    {
        boolean degXDiffer = this.degX != degXNew;
        boolean degYDiffer = this.degY != degYNew;

        int degXOld = this.degX;
        this.degX = degXNew;

        int degYOld = this.degY;
        this.degY = degYNew;

        if(degXDiffer || degYDiffer)
        {
            updatePolynomialCoefficients();

            firePropertyChange(DEGREE_X, degXOld, degXNew);
            firePropertyChange(DEGREE_Y, degYOld, degYNew); 

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public int getDegreeY()
    {
        return degY;
    }

    public void setDegreeY(int degYNew)
    {
        if(this.degY != degYNew)
        {
            int degYOld = this.degY;
            this.degY = degYNew;

            updatePolynomialCoefficients();

            firePropertyChange(DEGREE_Y, degYOld, degYNew);      

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    private int[][] buildPolynomialCoefficients()
    {
        int[] powersX = new int[degX + 1];
        int[] powersY = new int[degY];

        for(int i = 0; i<degX + 1; i++)
        {
            powersX[i] = i;
        }

        for(int i = 1; i<degY + 1; i++)
        {
            powersY[i - 1] = i;
        }

        int[][] model = new int[][] {powersX, powersY};

        return model;
    }

    private void updatePolynomialCoefficients()
    {   
        int[][] model = buildPolynomialCoefficients();

        if(!ArrayUtilities.equal(this.appliedModel, model))
        {
            this.appliedModel = model;
        }              
    }

    public int[][] getPolynomialCoefficients() 
    {
        return appliedModel;
    }

    protected boolean calculateIfApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && degX >= 0 && degY >= 0;

        return applyEnabled;
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }
        Channel2DDataInROITransformation tr = new PolynomialFitCorrection(this.appliedModel);
        return tr;
    }
}