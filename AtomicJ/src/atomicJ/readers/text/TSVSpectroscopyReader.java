
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

package atomicJ.readers.text;

import java.io.File;

import atomicJ.utilities.FileExtensionPatternFilter;



public class TSVSpectroscopyReader extends TextDelimitedSpectroscopyReader
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"tsv"};
    private static final String DESCRIPTION = "Tab separated values file (.tsv)";

    private static final String DELIMITER = "((?:\\p{javaWhitespace}*)[\\n\\t]+(?:\\p{javaWhitespace}*))";

    @Override
    protected String getDelimiter()
    {
        return DELIMITER;
    } 

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);       
    }
}