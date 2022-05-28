/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanosurf;

import java.util.Map;

public class NIDLineDimension 
{
    static final String LINE_DIM_PREFIX = "LineDim";

    private static final String MIN_SUFFIX = "Min";
    private static final String RANGE_SUFFIX = "Range";
    private static final String POINTS_SUFFIX = "Points";

    private final int dimensionIndex;

    private final int points;
    private final double range;
    private final double min;

    private NIDLineDimension(int dimensionIndex, int points, double range, double min)
    {
        this.dimensionIndex = dimensionIndex;
        this.points = points;
        this.range = range;
        this.min = min;
    }

    public int getPointCount()
    {
        return points;
    }

    public double getRange()
    {
        return range;
    }

    public double getMinimum()
    {
        return min;
    }

    public static NIDLineDimension build(int dimensionIndex, Map<String, String> keyValuePairs)
    {
        String dimensionIndexString = Integer.toString(dimensionIndex);

        String pointsKey = LINE_DIM_PREFIX + dimensionIndexString + POINTS_SUFFIX;
        String rangeKey = LINE_DIM_PREFIX + dimensionIndexString + RANGE_SUFFIX;
        String minKey = LINE_DIM_PREFIX + dimensionIndexString + MIN_SUFFIX;

        int points = Integer.parseInt(keyValuePairs.get(pointsKey));
        double range = Double.parseDouble(keyValuePairs.get(rangeKey));
        double min = Double.parseDouble(keyValuePairs.get(minKey));

        NIDLineDimension channelDimension = new NIDLineDimension(dimensionIndex, points, range, min);

        return channelDimension;
    }
}