
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2014 by Pawe³ Hermanowicz
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

package atomicJ.readers.gwyddionGwy;

import atomicJ.readers.SourceReaderFactory;

public class GwyddionGwySourceReaderFactory extends SourceReaderFactory<GwyddionGwySourceReader>
{
    @Override
    public GwyddionGwySourceReader getReader()
    {
        return new GwyddionGwySourceReader();
    }

    @Override
    public String getDescription()
    {
        return GwyddionGwySourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return GwyddionGwySourceReader.getAcceptedExtensions();
    }
}
