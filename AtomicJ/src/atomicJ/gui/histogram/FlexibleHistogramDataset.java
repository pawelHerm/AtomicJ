
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui.histogram;

//GILBERT AND OTHER CONTRIBUTORS, BELOW THE ORIGINAL COPYRIGHT STATEMENT
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
 * ---------------------
 * HistogramDataset.java
 * ---------------------
 * (C) Copyright 2003-2009, by Jelai Wang and Contributors.
 *
 * Original Author:  Jelai Wang (jelaiw AT mindspring.com);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Cameron Hayne;
 *                   Rikard Bj?rklind;
 *                   Thomas A Caswell (patch 2902842);
 *
 * Changes
 * -------
 * 06-Jul-2003 : Version 1, contributed by Jelai Wang (DG);
 * 07-Jul-2003 : Changed package and added Javadocs (DG);
 * 15-Oct-2003 : Updated Javadocs and removed array sorting (JW);
 * 09-Jan-2004 : Added fix by "Z." posted in the JFreeChart forum (DG);
 * 01-Mar-2004 : Added equals() and clone() methods and implemented
 *               Serializable.  Also added new addSeries() method (DG);
 * 06-May-2004 : Now extends AbstractIntervalXYDataset (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 20-May-2005 : Speed up binning - see patch 1026151 contributed by Cameron
 *               Hayne (DG);
 * 08-Jun-2005 : Fixed bug in getSeriesKey() method (DG);
 * 22-Nov-2005 : Fixed cast in getSeriesKey() method - see patch 1329287 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 03-Aug-2006 : Improved precision of bin boundary calculation (DG);
 * 07-Sep-2006 : Fixed bug 1553088 (DG);
 * 22-May-2008 : Implemented clone() method override (DG);
 * 08-Dec-2009 : Fire change event in addSeries() - see patch 2902842
 *               contributed by Thomas A Caswell (DG);
 *
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jfree.data.statistics.HistogramBin;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.util.PublicCloneable;

import atomicJ.statistics.HistogramType;



public class FlexibleHistogramDataset extends AbstractIntervalXYDataset
implements IntervalXYDataset, Cloneable, PublicCloneable,
Serializable 
{

    private static final long serialVersionUID = 1L;

    private static final String BIN_WIDTH = "binWidth";
    private static final String VALUES_LENGTH = "valuesLength";
    private static final String BINS = "bins";
    private static final String KEY = "key";


    private List<Map<String, Object>> list = new ArrayList<>();

    private HistogramType type;

    public FlexibleHistogramDataset() 
    {
        this(HistogramType.COUNT);
    }

    public FlexibleHistogramDataset(HistogramType type) 
    {
        this.type = type;
    }

    public HistogramType getType()
    {
        return this.type;
    }


    public void setType(HistogramType type) 
    {
        if (type == null) 
        {
            throw new IllegalArgumentException("Null 'type' argument");
        }
        this.type = type;
        fireDatasetChanged();
    }


    public void addSeries(Comparable<?> key, List<HistogramBin> bins, Double binWidth, Integer observationCount) 
    {
        if (key == null) 
        {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        if (bins == null) {
            throw new IllegalArgumentException("Null 'bins' argument.");
        }
        if (binWidth == null) 
        {
            throw new IllegalArgumentException("Null 'binWidth' argument.");
        }
        if (observationCount == null) 
        {
            throw new IllegalArgumentException("Null 'dataCount' argument.");
        }

        Map<String, Object> map = new HashMap<>();

        map.put(KEY, key);
        map.put(BINS, bins);
        map.put(VALUES_LENGTH, observationCount);
        map.put(BIN_WIDTH, binWidth);

        this.list.add(map);

        fireDatasetChanged();
    }



    private List<HistogramBin> getBins(int series) 
    {
        Map<?, ?> map = this.list.get(series);
        return (List<HistogramBin>) map.get(BINS);
    }


    private int getTotal(int series) 
    {
        Map<?, ?> map = this.list.get(series);
        return ((Integer) map.get(VALUES_LENGTH)).intValue();
    }

    private double getBinWidth(int series) 
    {
        Map<?, ?> map = this.list.get(series);
        return ((Double) map.get(BIN_WIDTH)).doubleValue();
    }

    @Override
    public int getSeriesCount() 
    {
        return this.list.size();
    }

    public boolean containsSeries(Comparable<?> key)
    {
        int n = this.list.size();

        boolean contains = false;

        for(int i = 0; i<n; i++)
        {
            Map<String, Object> mapForSeries = list.get(i);
            Object currentKey = mapForSeries.get(KEY);

            if(key.equals(currentKey))
            {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public int getSeriesIndex(Comparable<?> key)
    {
        int n = this.list.size();

        int index = -1;

        for(int i = 0; i<n; i++)
        {
            Map<String, Object> mapForSeries = list.get(i);
            Object currentKey = mapForSeries.get(KEY);

            if(key.equals(currentKey))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    public void setSeriesKey(Comparable<?> keyOld, Comparable<?> keyNew)
    {
        int seriesIndex = getSeriesIndex(keyOld);
        setSeriesKey(seriesIndex, keyNew);
    }

    public void setSeriesKey(int seriesIndex, Comparable<?> keyNew)
    {
        if (keyNew == null) 
        {
            throw new IllegalArgumentException("Null 'keyew' argument.");
        } 

        if(seriesIndex >= 0 && seriesIndex < this.list.size())
        {
            Map<String, Object> mapForSeries = list.get(seriesIndex);
            mapForSeries.put(KEY, keyNew);          
        }
    }

    @Override
    public Comparable<?> getSeriesKey(int series) 
    {
        Map<?, ?> map = this.list.get(series);
        return (Comparable<?>) map.get(KEY);
    }


    @Override
    public int getItemCount(int series) 
    {
        return getBins(series).size();
    }

    @Override
    public Number getX(int series, int item) 
    {
        List<HistogramBin> bins = getBins(series);
        HistogramBin bin = bins.get(item);
        double x = (bin.getStartBoundary() + bin.getEndBoundary()) / 2.;
        return new Double(x);
    }


    @Override
    public Number getY(int series, int item) 
    {
        List<HistogramBin> bins = getBins(series);
        HistogramBin bin = bins.get(item);
        double total = getTotal(series);
        double binWidth = getBinWidth(series);

        double y = 0;

        if (this.type == HistogramType.COUNT) 
        {
            y =  bin.getCount();
        }
        else if (this.type == HistogramType.PROBABILITY) 
        {
            y = bin.getCount() / total;
        }
        else if (this.type == HistogramType.PROBABILITY_DENSITY) 
        {
            y = bin.getCount() / (binWidth * total);
        }
        else if(HistogramType.LOG_COUNT.equals(this.type))
        {
            double binCount = bin.getCount();

            y = (binCount>0) ?  Math.log10(binCount) : 0;        	
        }
        else 
        { 
            throw new IllegalStateException();
        }


        return Double.valueOf(y);
    }


    @Override
    public Number getStartX(int series, int item) 
    {
        List<HistogramBin> bins = getBins(series);
        HistogramBin bin = bins.get(item);
        Double start = Double.valueOf(bin.getStartBoundary());
        return start;
    }

    @Override
    public Number getEndX(int series, int item)
    {
        List<HistogramBin> bins = getBins(series);
        HistogramBin bin = bins.get(item);
        Double end = Double.valueOf(bin.getEndBoundary());
        return end;
    }


    @Override
    public Number getStartY(int series, int item) 
    {
        return getY(series, item);
    }


    @Override
    public Number getEndY(int series, int item) 
    {
        return getY(series, item);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FlexibleHistogramDataset)) 
        {
            return false;
        }
        FlexibleHistogramDataset that = (FlexibleHistogramDataset) obj;
        if (!Objects.equals(this.type, that.type)) 
        {
            return false;
        }
        if (!Objects.equals(this.list, that.list)) 
        {
            return false;
        }
        return true;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        FlexibleHistogramDataset clone = (FlexibleHistogramDataset) super.clone();
        int seriesCount = getSeriesCount();
        clone.list = new java.util.ArrayList<>(seriesCount);
        for (int i = 0; i < seriesCount; i++) {
            clone.list.add(new HashMap<>(this.list.get(i)));
        }
        return clone;
    }

}
