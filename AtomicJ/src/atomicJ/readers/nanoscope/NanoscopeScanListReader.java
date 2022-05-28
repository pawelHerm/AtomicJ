
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.readers.nanoscope;

import java.io.*;
import atomicJ.gui.UserCommunicableException;

public class NanoscopeScanListReader
{
    private static final String FILE_LIST_END = "\\*File list end";

    public NanoscopeScanList read(File f) throws UserCommunicableException 
    {	        
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));) 
        {
            NanoscopeScanList scanList = new NanoscopeScanList();

            String line;
            while((line = reader.readLine()) != null)
            {               
                if(scanList.isSectionBeginning(line))
                {                    
                    line = scanList.readInFieldsToHeaderSection(reader);
                    break;
                }   

                if(FILE_LIST_END.equals(line))
                {
                    break;
                }

            }
            return scanList;	
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);		
        } 
    }
}

