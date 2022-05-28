
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

import static atomicJ.gui.save.SaveModelProperties.QUALITY;

import java.awt.geom.Rectangle2D;

public class JPEGFormatModel extends BasicFormatModel 
{
    private float quality = 1.f;

    public JPEGFormatSaver getChartSaver()
    {
        boolean saveDataArea = getSaveDataArea();
        Rectangle2D chartInitialArea = getChartInitialArea();

        int width = (int) getWidth();
        int height = (int)getHeight();

        JPEGFormatSaver saver = new JPEGFormatSaver(quality, chartInitialArea, width, height, saveDataArea);
        return saver;
    }

    public float getQuality() 
    {
        return quality;
    }

    private float getLegalQuality(float quality)
    {
        float legalValue = Math.max(0.f, Math.min(1.f,quality));
        return legalValue;
    }

    public void setQuality(float qualityNew) 
    {
        float qualityOld = quality;
        this.quality = getLegalQuality(qualityNew);

        firePropertyChange(QUALITY, qualityOld, this.quality);
    }
}