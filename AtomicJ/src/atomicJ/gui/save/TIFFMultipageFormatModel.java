
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
import static atomicJ.gui.save.SaveModelProperties.TIFF_MULTIPAGE_COMPRESSION;

import java.awt.Component;
import java.awt.geom.Rectangle2D;

public class TIFFMultipageFormatModel extends BasicFormatModel 
{
    private SaveQuality quality = SaveQuality.HIGH;
    private TIFFMovieCompressionMethod compression = TIFFMovieCompressionMethod.UNCOMPRESSED;

    public TIFFMultipageFormatSaver getChartSaver(Component parent)
    {
        boolean saveDataArea = getSaveDataArea();
        Rectangle2D chartInitialArea = getChartInitialArea();

        int width =  (int)getWidth();
        int height = (int)getHeight();

        TIFFMultipageFormatSaver saver = new TIFFMultipageFormatSaver(chartInitialArea, width, 
                height, compression, saveDataArea);

        return saver;
    }


    public SaveQuality getQuality() 
    {
        return quality;
    }

    public void setQuality(SaveQuality qualityNew) 
    {
        SaveQuality qualityOld = quality;
        this.quality = qualityNew;

        firePropertyChange(QUALITY, qualityOld, qualityNew);
    }

    public TIFFMovieCompressionMethod getCompression()
    {
        return compression;
    }

    public void setCompression(TIFFMovieCompressionMethod compressionNew) 
    {
        TIFFMovieCompressionMethod compressionOld = compression;
        this.compression = compressionNew;

        firePropertyChange(TIFF_MULTIPAGE_COMPRESSION, compressionOld, compressionNew);
    }
}