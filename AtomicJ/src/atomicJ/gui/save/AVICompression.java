
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

public enum AVICompression
{
    NO_COMPRESSION("Uncompressed", 0), PNG_COMPRESSION("PNG", 0x20676e70), JPEG_COMPRESSION("JPEG", 0x47504a4d);

    AVICompression(String name, int compressionCode)
    {
        this.prettyName = name;
        this.compressionCode = compressionCode;
    }

    private final String prettyName;
    private final int compressionCode;

    public int getCompressionCode()
    {
        return compressionCode;
    }

    public String getName()
    {
        return prettyName;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }
}