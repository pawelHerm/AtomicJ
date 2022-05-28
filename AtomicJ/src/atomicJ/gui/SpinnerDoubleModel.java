package atomicJ.gui;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.utilities.Validation;

public class SpinnerDoubleModel implements SpinnerModel
{
    private double minValue;
    private double maxValue;
    private double increment;
    private double value;

    private final String illegalValueWarning;

    private final Set<ChangeListener> listeners = new LinkedHashSet<>();

    public SpinnerDoubleModel(double currentValue, double min, double max, double increment)
    {
        this("", currentValue, min, max, increment);
    }

    public SpinnerDoubleModel(String illegalValueWarning, double currentValue, double min, double max, double increment)
    {
        Validation.requireNonNullParameterName(illegalValueWarning, "illegalValueWarning");

        this.illegalValueWarning = illegalValueWarning;
        this.value = currentValue;

        this.minValue = min;
        this.maxValue = max;
        this.increment = increment;
    }

    public String getIllegalValueWarning()
    {
        return illegalValueWarning;
    }

    @Override
    public void addChangeListener(ChangeListener l)
    {
        Validation.requireNonNullParameterName(l, "l");
        this.listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) 
    {
        this.listeners.remove(l);
    }

    public double getDoubleValue()
    {
        return value;
    }

    public double getIncrement()
    {
        return increment;
    }

    public void setIncrement(double incrementNew)
    {
        Validation.requireNotNaNParameterName(incrementNew, "incrementNew");
        Validation.requireNotInfiniteParameterName(incrementNew, "incrementNew");

        if(Double.compare(this.increment, incrementNew) != 0)
        {
            this.increment = incrementNew;
            fireChangeEvent();
        }
    }

    public double getMinimum()
    {
        return minValue;
    }

    public void setMinimum(double minimumNew)
    {
        Validation.requireNotNaNParameterName(minimumNew, "minimumNew");

        if(Double.compare(this.minValue, minimumNew) != 0)
        {
            this.minValue = minimumNew;
            fireChangeEvent();
        }
    }

    public double getMaximum()
    {
        return maxValue;
    }

    public void setMaximum(double maximumNew)
    {
        Validation.requireNotNaNParameterName(maximumNew, "maximumNew");

        if(Double.compare(this.maxValue, maximumNew) != 0)
        {
            this.maxValue = maximumNew;
            fireChangeEvent();
        }
    }

    @Override
    public Double getNextValue() 
    {
        if(Double.isNaN(this.value))
        {
            return Double.NaN;
        }

        double next = Math.min(this.value + this.increment, this.maxValue);

        return Double.valueOf(next);
    }

    @Override
    public Double getPreviousValue() 
    {
        if(Double.isNaN(this.value))
        {
            return Double.NaN;
        }

        double previous = Math.max(this.value - this.increment, this.minValue);

        return Double.valueOf(previous);
    }

    @Override
    public Double getValue() 
    {       
        return Double.valueOf(value);
    }

    @Override
    public void setValue(Object value)
    {
        Validation.requireNonNullParameterName(value, "value");
        Number numericValue = Validation.requireInstanceOfParameterName(value, Number.class, "value");

        double valueNew = numericValue.doubleValue();
        if(Double.compare(this.value, valueNew) != 0)
        {
            this.value = valueNew;
            fireChangeEvent();
        }        
    }

    private void fireChangeEvent()
    {
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener listener : listeners)
        {
            listener.stateChanged(ev);
        }
    }
}
