
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

import java.awt.Shape;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.util.ObjectUtilities;


public class LightweightXYItemEntity extends ChartEntity {

    private static final long serialVersionUID = 1L;


    private final Object datasetKey;
    private final long seriesItem;

    public LightweightXYItemEntity(Shape area, Object datasetKey, int series, int item,
            String urlText) 
    {
        super(area, null, urlText);
        this.datasetKey = datasetKey;
        this.seriesItem = ((series & 0XFFFFFFFFL) << 32) | (item & 0XFFFFFFFFL);
    }

    public Object getDatasetKey()
    {
        return datasetKey;
    }

    public int getSeriesIndex() 
    {
        return (int)(seriesItem>>32);
    }


    public int getItem() 
    {
        return (int)seriesItem;
    }

    @Override
    public boolean equals(Object that) 
    {
        if (that == this) {
            return true;
        }

        if (that instanceof LightweightXYItemEntity && super.equals(that)) 
        {
            LightweightXYItemEntity ie = (LightweightXYItemEntity) that;
            if (this.seriesItem != ie.seriesItem) 
            {
                return false;
            }

            if(!ObjectUtilities.equal(this.datasetKey, ie.datasetKey))
            {
                return false;
            }

            return true;
        }
        return false;
    }
}