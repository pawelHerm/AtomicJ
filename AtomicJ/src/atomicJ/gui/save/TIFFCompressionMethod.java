
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

import org.apache.sanselan.formats.tiff.constants.TiffConstants;

public enum TIFFCompressionMethod 
{
    PACKBITS(TiffConstants.TIFF_COMPRESSION_PACKBITS), LZW(TiffConstants.TIFF_COMPRESSION_LZW), 
    UNCOMPRESSED(TiffConstants.TIFF_COMPRESSION_UNCOMPRESSED);

    private final int compression;

    TIFFCompressionMethod(int compression)
    {
        this.compression = compression;
    }

    public int getCompression()
    {
        return compression;
    }
}
