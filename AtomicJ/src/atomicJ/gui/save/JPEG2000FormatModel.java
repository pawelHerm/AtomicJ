
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

package atomicJ.gui.save;

import static atomicJ.gui.save.SaveModelProperties.ENCODING_RATE;
import static atomicJ.gui.save.SaveModelProperties.ENCODING_RATE_ENABLED;
import static atomicJ.gui.save.SaveModelProperties.LOSSLESS;

import java.awt.geom.Rectangle2D;

public class JPEG2000FormatModel extends BasicFormatModel 
{
    private boolean lossless = true;
    private double encodingRate = 1.0;

    public JPEG2000FormatSaver getChartSaver()
    {
        boolean saveDataArea = getSaveDataArea();
        Rectangle2D chartInitialArea = getChartInitialArea();

        int width = (int) getWidth();
        int height = (int)getHeight();

        JPEG2000FormatSaver saver = new JPEG2000FormatSaver(encodingRate, lossless, chartInitialArea, width, height, saveDataArea);
        return saver;
    }

    public boolean isLossless()
    {
        return lossless;
    }

    public void setLossless(boolean losslessNew)
    {
        boolean losslessOld = this.lossless;
        this.lossless = losslessNew;

        boolean encodingRateEnabledOld = !losslessOld;
        boolean encodingRateEnabledNew = !losslessNew;

        firePropertyChange(LOSSLESS, losslessOld, losslessNew);
        firePropertyChange(ENCODING_RATE_ENABLED, encodingRateEnabledOld, encodingRateEnabledNew);
    }

    public boolean isEncodingRateEnabled()
    {
        boolean enabled = !lossless;        
        return enabled;
    }

    public double getEncodingRate() 
    {
        return encodingRate;
    }

    public void setEncodingRate(double encodingRateNew) 
    {
        double encodingRateOld = encodingRate;
        this.encodingRate = encodingRateNew;

        firePropertyChange(ENCODING_RATE, encodingRateOld, encodingRateNew);

    }
}