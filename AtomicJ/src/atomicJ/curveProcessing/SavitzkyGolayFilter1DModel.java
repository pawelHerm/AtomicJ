package atomicJ.curveProcessing;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DMinimumSizeFilter;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;

public class SavitzkyGolayFilter1DModel<R extends SpectroscopyResource> extends CurveBatchProcessingModel<R>
{
    public static final String LEFT_HALF_WIDTH = "LeftHalfWidth";
    public static final String RIGHT_HALF_WIDTH = "RightHalfWidth";
    public static final String ENFORCE_EQUAL_HALF_WIDTHS = "EnforceEqualHalfWidths";
    public static final String POLYNOMIAL_DEGREE = "PolynomialDegree";
    public static final String DERIVATIVE = "Derivative";

    private int leftHalfWidth = 2;
    private int rightHalfWidth = 2;
    private boolean enforceEqualHalfWidths = true;
    private int polynomialDegree = 2;
    private int derivative = 0;

    public SavitzkyGolayFilter1DModel(ResourceView<R, Channel1D, String> manager)
    {
        super(manager, new Channel1DMinimumSizeFilter(3), false, false);
    }

    public int getPolynomialDegree()
    {
        return polynomialDegree;
    }

    public void specifyPolynomialDegree(int polynomialDegreeNew)
    {
        if(polynomialDegreeNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'polynomialDegreeNew' must be grater or equal 0");
        }

        List<PropertyChangeEvent> changeEvents = new ArrayList<>();
        if(polynomialDegreeNew > leftHalfWidth + rightHalfWidth)
        {
            changeEvents.add(new PropertyChangeEvent(this, POLYNOMIAL_DEGREE, polynomialDegreeNew, this.polynomialDegree));
        }
        else
        {
            changeEvents.addAll(setPolynomialDegree(polynomialDegreeNew));
            changeEvents.addAll(ensureConsistencyWithPolynomialDegree());
        }

        firePropertyChange(changeEvents);
    }

    private List<PropertyChangeEvent> setPolynomialDegree(int polynomialDegreeNew)
    {
        List<PropertyChangeEvent> changeEvents = new ArrayList<>();

        if(this.polynomialDegree != polynomialDegreeNew)
        {          
            int polynomialDegreeOld = this.polynomialDegree;
            this.polynomialDegree = polynomialDegreeNew;

            changeEvents.add(new PropertyChangeEvent(this,POLYNOMIAL_DEGREE, polynomialDegreeOld, polynomialDegreeNew));
            updatePreview();
        }

        return changeEvents;
    }

    public int getDerivative()
    {
        return derivative;
    }

    public void specifyDerivative(int derivativeNew)
    {
        if(derivativeNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'polynomialDegreeNew' must be grater or equal 0");
        }

        List<PropertyChangeEvent> changeEvents = new ArrayList<>();

        if(derivativeNew > polynomialDegree)
        {
            changeEvents.add(new PropertyChangeEvent(this, DERIVATIVE, derivativeNew, this.derivative));
        }
        else
        {
            changeEvents.addAll(setDerivative(derivativeNew));
        }

        firePropertyChange(changeEvents);
    }

    private List<PropertyChangeEvent> setDerivative(int derivativeNew)
    {
        List<PropertyChangeEvent> changeEvents = new ArrayList<>();

        if(this.derivative != derivativeNew)
        {
            int derivativeOld = this.derivative;
            this.derivative = derivativeNew;

            changeEvents.add(new PropertyChangeEvent(this, DERIVATIVE, derivativeOld, derivativeNew));
            updatePreview();
        }

        return changeEvents;
    }

    public int getLeftHalfWidth()
    {
        return leftHalfWidth;
    }

    private List<PropertyChangeEvent> setLeftHalfWidth(int leftHalfWidthNew)
    {       
        List<PropertyChangeEvent> propertyChangeEvents = new ArrayList<>();

        if(this.leftHalfWidth != leftHalfWidthNew)
        {
            int leftHalfWidthOld = this.leftHalfWidth;
            this.leftHalfWidth = leftHalfWidthNew;

            propertyChangeEvents.add(new PropertyChangeEvent(this, LEFT_HALF_WIDTH, leftHalfWidthOld, leftHalfWidthNew));
            updatePreview();
        }

        return propertyChangeEvents;
    }

    public void specifyLeftHalfWidth(int leftHalfWidthNew)
    {       
        if(leftHalfWidthNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'leftHalfWidthNew' must be grater or equal 0");
        }

        List<PropertyChangeEvent> events = new ArrayList<>();        

        if(this.leftHalfWidth != leftHalfWidthNew)
        {            
            events.addAll(setLeftHalfWidth(leftHalfWidthNew));
            if(enforceEqualHalfWidths)
            {
                events.addAll(setRightHalfWidth(leftHalfWidthNew));
            }
        }

        events.addAll(ensureConsistencyWithKernelWidth());        
        firePropertyChange(events);
    }

    public int getRightHalfWidth()
    {
        return rightHalfWidth;
    }

    public void specifyRightHalfWidth(int rightHalfWidthNew)
    {       
        if(rightHalfWidthNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'rightHalfWidthNew' must be grater or equal 0");
        }

        List<PropertyChangeEvent> events = new ArrayList<>();

        if(this.rightHalfWidth != rightHalfWidthNew)
        {     
            events.addAll(setRightHalfWidth(rightHalfWidthNew));
            if(enforceEqualHalfWidths)
            {
                events.addAll(setLeftHalfWidth(rightHalfWidthNew));
            }
        }
        events.addAll(ensureConsistencyWithKernelWidth());

        firePropertyChange(events);
    }

    private  List<PropertyChangeEvent> setRightHalfWidth(int rightHalfWidthNew)
    {  
        List<PropertyChangeEvent> propertyChangeEvents = new ArrayList<>();

        if(this.rightHalfWidth != rightHalfWidthNew)
        {
            int rightHalfWidthOld = this.rightHalfWidth;
            this.rightHalfWidth = rightHalfWidthNew;

            propertyChangeEvents.add(new PropertyChangeEvent(this, RIGHT_HALF_WIDTH, rightHalfWidthOld, rightHalfWidthNew));          
            updatePreview();
        }

        return propertyChangeEvents;
    }

    public boolean getEnforceEqualHalfWidths()
    {
        return enforceEqualHalfWidths;
    }

    public void specifyEnforceEqualHalfWidths(boolean enforceEqualHalfWidthsNew)
    {
        List<PropertyChangeEvent> changeEvents = new ArrayList<>();
        changeEvents.addAll(setEnforceEqualHalfWidths(enforceEqualHalfWidthsNew));

        firePropertyChange(changeEvents);
    }

    private List<PropertyChangeEvent> setEnforceEqualHalfWidths(boolean enforceEqualHalfWidthsNew)
    {
        List<PropertyChangeEvent> propertyChangeEvents = new ArrayList<>();

        if(this.enforceEqualHalfWidths != enforceEqualHalfWidthsNew)
        {
            boolean enforceEqualHalfWidthsOld = this.enforceEqualHalfWidths;
            this.enforceEqualHalfWidths = enforceEqualHalfWidthsNew;

            propertyChangeEvents.add(new PropertyChangeEvent(this,ENFORCE_EQUAL_HALF_WIDTHS, enforceEqualHalfWidthsOld, enforceEqualHalfWidthsNew));
            updatePreview();
        }

        return propertyChangeEvents;
    }

    private List<PropertyChangeEvent> ensureConsistencyWithKernelWidth()
    {
        int polynomialDegreeNew = Math.min(this.polynomialDegree, (this.leftHalfWidth + this.rightHalfWidth));
        List<PropertyChangeEvent> events = setPolynomialDegree(polynomialDegreeNew);
        events.addAll(ensureConsistencyWithPolynomialDegree());

        return events;
    }


    private List<PropertyChangeEvent> ensureConsistencyWithPolynomialDegree()
    {
        List<PropertyChangeEvent> changeEvents = new ArrayList<>();

        int derivativeNew = Math.min(this.derivative, this.polynomialDegree);
        changeEvents.addAll(setDerivative(derivativeNew));

        return changeEvents;
    }

    @Override
    public Channel1DDataInROITransformation buildTransformation()
    {
        Kernel1DSet<SavitzkyGolay1DKernel> kernelSet = SavitzkyGolay1DKernel.buildKernelSet(-leftHalfWidth, rightHalfWidth + leftHalfWidth + 1, polynomialDegree, derivative);
        Channel1DDataInROITransformation tr = new SavitzkyGolay1DConvolution(kernelSet);

        return tr;
    }
}
