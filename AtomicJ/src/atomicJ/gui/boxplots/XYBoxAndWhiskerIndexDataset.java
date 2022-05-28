package atomicJ.gui.boxplots;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2011, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ----------------------------------
 * DefaultBoxAndWhiskerXYDataset.java
 * ----------------------------------
 * (C) Copyright 2003-2008, by David Browning and Contributors.
 *
 * Original Author:  David Browning (for Australian Institute of Marine
 *                   Science);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 05-Aug-2003 : Version 1, contributed by David Browning (DG);
 * 08-Aug-2003 : Minor changes to comments (DB)
 *               Allow average to be null  - average is a perculiar AIMS
 *               requirement which probably should be stripped out and overlaid
 *               if required...
 *               Added a number of methods to allow the max and min non-outlier
 *               and non-farout values to be calculated
 * 12-Aug-2003   Changed the getYValue to return the highest outlier value
 *               Added getters and setters for outlier and farout coefficients
 * 27-Aug-2003 : Renamed DefaultBoxAndWhiskerDataset
 *               --> DefaultBoxAndWhiskerXYDataset (DG);
 * 06-May-2004 : Now extends AbstractXYDataset (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 18-Nov-2004 : Updated for changes in RangeInfo interface (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 * 12-Nov-2007 : Implemented equals() and clone() (DG);
 *
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.jfree.data.Range;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYRangeInfo;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.Quantity;

/**
 * A simple implementation of the {@link BoxAndWhiskerXYDataset} interface.
 * This dataset implementation can hold only one series.
 */
public class XYBoxAndWhiskerIndexDataset extends AbstractXYDataset
implements BoxAndWhiskerXYDataset, XYRangeInfo {

    private static final long serialVersionUID = 1L;

    /** The series key. */
    private final Comparable<?> seriesKey;

    private List<Integer> itemIndices = new ArrayList<>();

    private final Map<Integer, String> labels = new LinkedHashMap<>();

    /** Storage for the box and whisker statistics. */
    private List<RobustBoxAndWhiskerItem> items = new ArrayList<>();

    /** The minimum range value. */
    private Number minimumRangeValue;

    /** The maximum range value. */
    private Number maximumRangeValue;

    /** The range of values. */
    private Range rangeBounds;

    private Quantity dataQuantity;

    /**
     * The coefficient used to calculate outliers. Tukey's default value is
     * 1.5 (see EDA) Any value which is greater than Q3 + (interquartile range
     * * outlier coefficient) is considered to be an outlier.  Can be altered
     * if the data is particularly skewed.
     */
    private double outlierCoefficient = 1.5;

    /**
     * The coefficient used to calculate farouts. Tukey's default value is 2
     * (see EDA) Any value which is greater than Q3 + (interquartile range *
     * farout coefficient) is considered to be a farout.  Can be altered if the
     * data is particularly skewed.
     */
    private double faroutCoefficient = 2.0;

    /**
     * Constructs a new box and whisker dataset.
     * <p>
     * The current implementation allows only one series in the dataset.
     * This may be extended in a future version.
     *
     * @param seriesKey  the key for the series.
     */
    public XYBoxAndWhiskerIndexDataset(Comparable<?> seriesKey)
    {
        this.seriesKey = seriesKey;
    }

    public static XYBoxAndWhiskerIndexDataset getDataset(String datasetName, Map<Object, QuantitativeSample> samples)
    {
        XYBoxAndWhiskerIndexDataset dataset = new XYBoxAndWhiskerIndexDataset(datasetName);

        int index = 0;

        //we assume that each sample has the same quantity
        Quantity dataQuantity = null;
        for(Entry<Object, QuantitativeSample> entry : samples.entrySet())
        {
            QuantitativeSample sample = entry.getValue();
            String label = sample.getSampleName();

            dataQuantity = sample.getQuantity();

            if(sample.containsNumericValues())
            {
                dataset.add(++index, label, RobustBoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(sample));
            }
        }

        dataset.setDataQuantity(dataQuantity);

        return dataset;
    }

    public boolean isNonEmpty()
    {
        boolean empty = itemIndices.isEmpty();
        return !empty;
    }

    public Quantity getDataQuantity()
    {
        return dataQuantity;
    }

    public void setDataQuantity(Quantity dataQuantity)
    {
        this.dataQuantity = dataQuantity;    
    }

    public List<String> getCategories()
    {
        List<String> categories = new ArrayList<>(labels.values());

        return categories;
    }

    @Override
    public double getOutlierCoefficient() {
        return this.outlierCoefficient;
    }

    public void setOutlierCoefficient(double outlierCoefficient) {
        this.outlierCoefficient = outlierCoefficient;
    }

    @Override
    public double getFaroutCoefficient() {
        return this.faroutCoefficient;
    }

    public void setFaroutCoefficient(double faroutCoefficient) {

        if (faroutCoefficient > getOutlierCoefficient()) {
            this.faroutCoefficient = faroutCoefficient;
        }
        else {
            throw new IllegalArgumentException("Farout value must be greater "
                    + "than the outlier value, which is currently set at: ("
                    + getOutlierCoefficient() + ")");
        }
    }


    @Override
    public int getSeriesCount() {
        return 1;
    }


    @Override
    public int getItemCount(int series) {
        return this.itemIndices.size();
    }


    public void add(Integer index, String label, RobustBoxAndWhiskerItem item)
    {
        this.itemIndices.add(index);
        this.items.add(item);
        this.labels.put(index, label);

        if (this.minimumRangeValue == null) 
        {
            this.minimumRangeValue = item.getMinValue();
        }
        else 
        {
            if (item.getMinValue() < this.minimumRangeValue.doubleValue())
            {
                this.minimumRangeValue = item.getMinValue();
            }
        }
        if (this.maximumRangeValue == null)
        {
            this.maximumRangeValue = item.getMaxValue();
        }
        else 
        {
            if (item.getMaxValue() > this.maximumRangeValue.doubleValue()) {
                this.maximumRangeValue = item.getMaxValue();
            }
        }

        this.rangeBounds = new Range(this.minimumRangeValue.doubleValue(),
                this.maximumRangeValue.doubleValue());
        fireDatasetChanged();
    }

    @Override
    public Comparable<?> getSeriesKey(int i) {
        return this.seriesKey;
    }

    public RobustBoxAndWhiskerItem getItem(int series, int item) {
        return this.items.get(item);
    }


    @Override
    public Number getX(int series, int item) {
        return this.itemIndices.get(item);
    }

    @Override
    public Number getY(int series, int item) {
        return getMeanValue(series, item);
    }

    @Override
    public Number getMeanValue(int series, int item) {
        Number result = null;
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getMean();
        }
        return result;
    }


    @Override
    public Number getMedianValue(int series, int item) {
        Number result = null;
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getMedian();
        }
        return result;
    }


    @Override
    public Number getQ1Value(int series, int item) {
        Number result = null;
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getQ1();
        }
        return result;
    }

    @Override
    public Number getQ3Value(int series, int item) {
        Number result = null;
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getQ3();
        }
        return result;
    }


    @Override
    public Number getMinRegularValue(int series, int item)
    {
        Number result = null;
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getMinRegularValue();
        }

        return result;
    }

    @Override
    public Number getMaxRegularValue(int series, int item) {
        Number result = null;
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getMaxRegularValue();
        }
        return result;
    }

    @Override
    public Number getMinOutlier(int series, int item) {
        Number result = null;
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getMinValue();
        }
        return result;
    }

    /**
     * Returns the maximum value which is not a farout, ie Q3 + (interquartile
     * range * farout coefficient).
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return A <code>Number</code> representing the maximum non-farout value.
     */
    @Override
    public Number getMaxOutlier(int series, int item) {
        Number result = null;
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getMaxValue();
        }
        return result;
    }

    @Override
    public List<Double> getOutliers(int series, int item) {
        List<Double> result = Collections.emptyList();
        RobustBoxAndWhiskerItem stats = this.items.get(item);
        if (stats != null) {
            result = stats.getOutliers();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYBoxAndWhiskerIndexDataset)) {
            return false;
        }
        XYBoxAndWhiskerIndexDataset that
        = (XYBoxAndWhiskerIndexDataset) obj;
        if (!Objects.equals(this.seriesKey, that.seriesKey)) {
            return false;
        }
        if (!this.itemIndices.equals(that.itemIndices)) {
            return false;
        }
        if (!this.items.equals(that.items)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of the plot.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the cloning is not supported.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        XYBoxAndWhiskerIndexDataset clone
        = (XYBoxAndWhiskerIndexDataset) super.clone();
        clone.itemIndices = new java.util.ArrayList<>(this.itemIndices);
        clone.items = new java.util.ArrayList<>(this.items);
        return clone;
    }

    @Override
    public Range getRangeBounds(List visibleSeriesKeys, Range xRange, boolean includeInterval) 
    {
        return rangeBounds;
    }

}

