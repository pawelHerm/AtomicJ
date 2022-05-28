package atomicJ.gui.boxplots;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RobustBoxAndWhiskerItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double mean;
    private final double median;
    private final double q1;
    private final double q3;
    private final double minRegularValue;
    private final double maxRegularValue;
    private final double minValue;
    private final double maxValue;

    private final List<Double> outliers;


    public RobustBoxAndWhiskerItem(double mean, double median,
            double q1, double q3,
            double minRegularValue,double maxRegularValue,
            double minValue, double maxValue,
            List<Double> outliers) {

        this.mean = mean;
        this.median = median;
        this.q1 = q1;
        this.q3 = q3;
        this.minRegularValue = minRegularValue;
        this.maxRegularValue = maxRegularValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.outliers = outliers;
    }

    public double getMean() {
        return this.mean;
    }

    public double getMedian() {
        return this.median;
    }


    public double getQ1() {
        return this.q1;
    }


    public double getQ3() {
        return this.q3;
    }

    public double getMinRegularValue() {
        return this.minRegularValue;
    }

    public double getMaxRegularValue() {
        return this.maxRegularValue;
    }


    public double getMinValue() {
        return this.minValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    public List<Double> getOutliers() {
        if (this.outliers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.outliers);
    }

    @Override
    public int hashCode()
    {
        int result = Double.hashCode(this.mean);
        result = 31*result + Double.hashCode(this.median);
        result = 31*result + Double.hashCode(this.q1);
        result = 31*result + Double.hashCode(this.q3);       
        result = 31*result + Double.hashCode(this.minRegularValue);       
        result = 31*result + Double.hashCode(this.maxRegularValue);
        result = 31*result + Double.hashCode(this.minValue);
        result = 31*result + Double.hashCode(this.maxValue);
        result = 31*result + Objects.hashCode(this.outliers);

        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RobustBoxAndWhiskerItem)) {
            return false;
        }
        RobustBoxAndWhiskerItem that = (RobustBoxAndWhiskerItem) obj;
        if (Double.compare(this.mean, that.mean) != 0) 
        {
            return false;
        }
        if (Double.compare(this.median, that.median) != 0)
        {
            return false;
        }
        if (Double.compare(this.q1, that.q1) != 0) 
        {
            return false;
        }
        if (Double.compare(this.q3, that.q3) != 0) 
        {
            return false;
        }
        if (Double.compare(this.minRegularValue, that.minRegularValue) != 0)
        {
            return false;
        }
        if (Double.compare(this.maxRegularValue, that.maxRegularValue) != 0) 
        {
            return false;
        }
        if (Double.compare(this.minValue, that.minValue) != 0)
        {
            return false;
        }
        if (Double.compare(this.maxValue, that.maxValue) != 0)
        {
            return false;
        }
        if (!Objects.equals(this.outliers, that.outliers))
        {
            return false;
        }
        return true;
    }

}

