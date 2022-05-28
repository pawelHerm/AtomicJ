package atomicJ.curveProcessing;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DMinimumSizeFilter;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;

public class LocalRegression1DModel<R extends SpectroscopyResource> extends CurveBatchProcessingModel<R>
{
    public static final String SPAN = "Span";
    public static final String SPAN_TYPE = "SpanType";
    public static final String ROBUSTNESS_ITERATIONS_COUNT = "RobustnessIterationsCount";
    public static final String POLYNOMIAL_DEGREE = "PolynomialDegree";
    public static final String DERIVATIVE = "Derivative";
    public static final String WEIGHT_FUNCTION = "WeigtFunction";

    private double span = 5;
    private SpanType spanType = SpanType.POINT_FRACTION;
    private int robustnessIterations = 0;
    private int polynomialDegree = 2;
    private int derivative = 0;
    private final LocalRegressionWeightFunction weightFunction = LocalRegressionWeightFunction.TRICUBE;

    public LocalRegression1DModel(ResourceView<R, Channel1D, String> manager)
    {
        super(manager, new Channel1DMinimumSizeFilter(3), false, false);
    }

    public LocalRegressionWeightFunction getWeightFunction()
    {
        return weightFunction;
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
        if(!spanType.isPolynomialDegreeAceptable(this.span, polynomialDegreeNew))
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

            changeEvents.add(new PropertyChangeEvent(this, POLYNOMIAL_DEGREE, polynomialDegreeOld, polynomialDegreeNew));

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

    public double getSpan()
    {
        return span;
    }

    private List<PropertyChangeEvent> setSpan(double spanNew)
    {       
        List<PropertyChangeEvent> propertyChangeEvents = new ArrayList<>();

        if(this.span != spanNew)
        {
            double spanOld = this.span;
            this.span = spanNew;

            propertyChangeEvents.add(new PropertyChangeEvent(this, SPAN, spanOld, spanNew));
            updatePreview();
        }

        return propertyChangeEvents;
    }

    public void specifySpan(double spanNew)
    {       
        if(spanNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'spanNew' must be grater than 0");
        }

        List<PropertyChangeEvent> events = new ArrayList<>();        

        if(!spanType.isSpanValueAcceptable(spanNew))
        {
            events.add(new PropertyChangeEvent(this, SPAN, spanNew, this.span));
        }
        else
        {
            events.addAll(setSpan(spanNew));       
            events.addAll(ensureConsistencyWithSpan()); 
        }

        firePropertyChange(events);
    }

    public SpanType getSpanType()
    {
        return spanType;
    }

    private List<PropertyChangeEvent> setSpanType(SpanType spanTypeNew)
    {
        List<PropertyChangeEvent> events = new ArrayList<>();        

        if(!this.spanType.equals(spanTypeNew))
        {
            SpanType spanTypeOld = this.spanType;
            this.spanType = spanTypeNew;

            events.add(new PropertyChangeEvent(this, SPAN_TYPE, spanTypeOld, spanTypeNew));
            updatePreview();
        }

        return events;
    }

    public void specifySpanType(SpanType spanTypeNew)
    {
        if(spanTypeNew == null)
        {
            throw new IllegalArgumentException("Parameter 'spanTypeNew' cannot be null");
        }

        List<PropertyChangeEvent> events = new ArrayList<>(); 

        events.addAll(setSpanType(spanTypeNew));
        events.addAll(ensureConsistencyWithSpanType());

        firePropertyChange(events);
    }

    public int getRobustnessIterationCount()
    {
        return robustnessIterations;
    }

    private List<PropertyChangeEvent> setRobustnessIterationCount(int robustnessIterationsNew)
    {
        List<PropertyChangeEvent> events = new ArrayList<>();        

        if(this.robustnessIterations != robustnessIterationsNew)
        {
            int robustnessIterationsOld = this.robustnessIterations;
            this.robustnessIterations = robustnessIterationsNew;

            events.add(new PropertyChangeEvent(this, ROBUSTNESS_ITERATIONS_COUNT, robustnessIterationsOld, robustnessIterationsNew));
            updatePreview();
        }

        return events;
    }

    public void specifyRobustnessIterationCount(int robustnessIerationsNew)
    {
        if(robustnessIerationsNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'robustnessIerationsNew' must be grater or equal 0");
        }

        List<PropertyChangeEvent> events = new ArrayList<>();        
        events.addAll(setRobustnessIterationCount(robustnessIerationsNew));

        firePropertyChange(events);
    }

    private List<PropertyChangeEvent> ensureConsistencyWithSpanType()
    {
        double spanNew = this.spanType.correctSpanValue(this.span);
        List<PropertyChangeEvent> events = new ArrayList<>();

        events.addAll(setSpan(spanNew));
        events.addAll(ensureConsistencyWithSpan());

        return events;
    }

    private List<PropertyChangeEvent> ensureConsistencyWithSpan()
    {
        int polynomialDegreeNew = this.spanType.correctPolynomialDegree(this.span, this.polynomialDegree);
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
        Channel1DDataInROITransformation tr = new LocalRegressionTransformation(span, SpanGeometry.NEAREST_NEIGHBOUR, spanType, robustnessIterations, polynomialDegree, derivative, weightFunction);
        return tr;
    }
}
