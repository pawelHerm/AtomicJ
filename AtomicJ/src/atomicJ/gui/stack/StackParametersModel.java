package atomicJ.gui.stack;

import java.util.Map;

import atomicJ.data.units.Quantity;
import atomicJ.gui.generalProcessing.OperationModel;
import atomicJ.gui.rois.ROI;

public class StackParametersModel extends OperationModel
{
    public static final String FIX_CONTACT = "FixContact";
    public static final String STACK_MINIMUM = "StackMinimum";
    public static final String STACK_MAXIMUM = "StackMaximum";
    public static final String FRAME_COUNT = "FrameCount";

    private boolean fixContact = true;
    private double stackMinimum = 0;
    private double stackMaximum = 1;
    private int frameCount = 100;

    private final String stackName;
    private final Quantity dataQuantity;

    public StackParametersModel(Map<Object, ROI> rois, ROI roiUnion, String stackName, Quantity dataQuantity)
    {
        super(rois, roiUnion);
        this.stackName = stackName;
        this.dataQuantity = dataQuantity;
    }

    public String getStackName()
    {
        return stackName;
    }

    public Quantity getDataQuantity()
    {
        return dataQuantity;
    }

    public boolean isFixContact()
    {
        return fixContact;
    }

    public void setFixContact(boolean fixContactNew)
    {
        boolean fixContactOld = this.fixContact;
        this.fixContact = fixContactNew;

        firePropertyChange(FIX_CONTACT, fixContactOld, fixContactNew);
    }

    public double getStackMinimum()
    {
        return stackMinimum;
    }

    public void setStackRange(double stackMinimumNew, double stackMaximumNew)
    {
        boolean legalValues = (stackMaximumNew >= stackMinimumNew);

        if(legalValues)
        {
            double stackMinimumOld = this.stackMinimum;
            this.stackMinimum = stackMinimumNew;

            double stackMaximumOld = this.stackMaximum;
            this.stackMaximum = stackMaximumNew;

            firePropertyChange(STACK_MAXIMUM, stackMaximumOld, stackMaximumNew);
            firePropertyChange(STACK_MINIMUM, stackMinimumOld, stackMinimumNew);
        }
        else
        {
            firePropertyChange(STACK_MINIMUM, stackMinimumNew, this.stackMinimum);
            firePropertyChange(STACK_MAXIMUM, stackMaximumNew, this.stackMaximum);
        }
    }

    public void setStackMinimum(double stackMinimumNew)
    {
        boolean legalValue = stackMinimumNew <= stackMaximum;

        if(legalValue)
        {
            double stackMinimumOld = this.stackMinimum;
            this.stackMinimum = stackMinimumNew;


            firePropertyChange(STACK_MINIMUM, stackMinimumOld, stackMinimumNew);
        }

        //this may be necessary, because the GUI components changes their values before notifying
        //listeners. By firing this property change event, we can reverse the state of GUI components
        //to the proper values
        else 
        {
            firePropertyChange(STACK_MINIMUM, stackMinimumNew, this.stackMinimum);
        }
    }

    public double getStackMaximum()
    {
        return stackMaximum;
    }

    public void setStackMaximum(double stackMaximumNew)
    {
        boolean legalValue = stackMaximumNew >= stackMinimum;

        if(legalValue)
        {
            double stackMaximumOld = this.stackMaximum;
            this.stackMaximum = stackMaximumNew;

            firePropertyChange(STACK_MAXIMUM, stackMaximumOld, stackMaximumNew);
        }

        //this may be necessary, because the GUI components changes their values before notifying
        //listeners. By firing this property change event, we can reverse the state of GUI components
        //to the proper values
        else 
        {
            firePropertyChange(STACK_MAXIMUM, stackMaximumNew, this.stackMaximum);
        }
    }

    public int getFrameCount()
    {
        return frameCount;
    }

    public void setFrameCount(int frameCountNew)
    {
        boolean legalValue = frameCountNew > 0;

        if(legalValue)
        {
            double frameCountOld = this.frameCount;
            this.frameCount = frameCountNew;

            firePropertyChange(FRAME_COUNT, frameCountOld, frameCountNew);
        }

        //this may be necessary, because the GUI components changes their values before notifying
        //listeners. By firing this property change event, we can reverse the state of GUI components
        //to the proper values
        else 
        {
            firePropertyChange(FRAME_COUNT, frameCountNew, this.frameCount);
        }
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }
}
