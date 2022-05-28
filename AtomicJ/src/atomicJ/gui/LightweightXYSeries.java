
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

package atomicJ.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.data.general.Series;
import org.jfree.util.PublicCloneable;

import atomicJ.utilities.Validation;

public class LightweightXYSeries extends Series implements PublicCloneable, Serializable 
{
    static final long serialVersionUID = 1L;

    private List<double[]> data = new ArrayList<>(); 

    public LightweightXYSeries(String key) 
    {
        super(key);   
    }

    public LightweightXYSeries(String key, List<double[]> data) 
    {
        super(key);
        this.data.addAll(data);
    }

    @Override
    public int getItemCount() 
    {
        return this.data.size();
    }

    public List<double[]> getItems() 
    {
        return new ArrayList<>(data);
    }

    public void addItem(double x, double y)
    {
        double[] p = new double[] {x, y};
        addItem(p);
    }

    public void addItem(double[] item) 
    {
        addItem(item, true);
    }

    public void addItem(double[] item, boolean notify) 
    {
        Validation.requireNonNullParameterName(item, "item");

        this.data.add(item);

        if (notify) 
        {
            fireSeriesChanged();
        }
    }

    public double[][] getData()
    {
        return this.data.toArray(new double[][] {});
    }

    public double[][] getDataCopy()
    {
        int n = data.size();
        double[][] dataCopy = new double[n][];
        for(int i = 0; i<n; i++)
        {
            double[] item = data.get(i);
            dataCopy[i] = Arrays.copyOf(item, item.length);
        }

        return dataCopy;
    }

    public void setData(double[][] items)
    {
        setData(items, true);
    }  

    public void setData(double[][] items, boolean notify)
    {
        this.data = new ArrayList<>(Arrays.asList(items));

        if (notify) 
        {
            fireSeriesChanged();
        }
    }

    public void delete(int start, int end) 
    {
        this.data.subList(start, end + 1).clear();
        fireSeriesChanged();
    }

    public void setItemTo(int itemIndex, double[] item) 
    {
        this.data.set(itemIndex, item);
        fireSeriesChanged();
    }

    public void clear() 
    {
        clear(true);
    }

    public void clear(boolean notify) 
    {
        if(!this.data.isEmpty()) 
        {
            this.data.clear();

            if(notify)
            {
                fireSeriesChanged();
            }
        }
    }

    public void translate(double x, double y, boolean notify)
    {
        if(!this.data.isEmpty()) 
        {
            for(double[] p : data)
            {
                p[0] = p[0] + x;
                p[1] = p[1] + y;
            }

            if(notify)
            {
                fireSeriesChanged();
            }
        }
    }

    public double getX(int index) 
    {
        double[] item = data.get(index);
        return item[0];
    }

    public double getY(int index) 
    {
        double[] item = data.get(index);
        return item[1];    
    }
}
