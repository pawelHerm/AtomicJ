package atomicJ.curveProcessing;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import atomicJ.gui.AbstractModel;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.statistics.JICPenalty;
import atomicJ.statistics.LocalRegressionWeightFunction;

public class JumpPointEstimationModel<R extends SpectroscopyResource> extends AbstractModel
{
    public static final String SPAN = "Span";
    public static final String SPAN_TYPE = "SpanType";
    public static final String POLYNOMIAL_DEGREE = "PolynomialDegree";
    public static final String WEIGHT_FUNCTION = "WeigtFunction";
    public static final String JIC_PENALTY = "JICPenalty";

    private int polynomialDegree = 2;
    private double span = 5;
    private SpanType spanType = SpanType.POINT_FRACTION;
    private LocalRegressionWeightFunction weightFunction = LocalRegressionWeightFunction.TRICUBE;
    private JICPenalty jicPenalty = JICPenalty.MODERATE;


    public JICPenalty getJICPenalty()
    {
        return jicPenalty;
    }

    public void setJICPenalty(JICPenalty jicPenaltyNew)
    {
        if(!Objects.equals(this.jicPenalty, jicPenaltyNew))
        {
            JICPenalty jicPenaltyOld = this.jicPenalty;
            this.jicPenalty = jicPenaltyNew;

            firePropertyChange(JIC_PENALTY, jicPenaltyOld, jicPenaltyNew);
        }
    }

    public LocalRegressionWeightFunction getWeightFunction()
    {
        return weightFunction;
    }

    public void setWeightFunction(LocalRegressionWeightFunction weightFunctionNew)
    {
        if(!Objects.equals(this.weightFunction, weightFunctionNew))
        {
            LocalRegressionWeightFunction weightFunctionOld = this.weightFunction;
            this.weightFunction = weightFunctionNew;

            firePropertyChange(WEIGHT_FUNCTION, weightFunctionOld, weightFunctionNew);
        }
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

        return events;
    }
}
