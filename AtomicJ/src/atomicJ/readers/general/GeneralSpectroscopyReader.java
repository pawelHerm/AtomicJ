
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

package atomicJ.readers.general;

import java.io.*;
import java.util.*;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReader;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.afmworkshop.AFMWorkshopSpectroscopyReader;
import atomicJ.readers.anasys.AnasysSpectroscopyReader;
import atomicJ.readers.asylum.AsylumSpectroscopyReader;
import atomicJ.readers.csinstruments.CSISpectroscopyReader;
import atomicJ.readers.gwyddionGwy.GwyddionGwySpectroscopyReader;
import atomicJ.readers.innova.InnovaSpectroscopyReader;
import atomicJ.readers.jpk.JPKSpectroscopyReader;
import atomicJ.readers.mdt.MDTSpectroscopyReader;
import atomicJ.readers.mi.MISpectroscopyReader;
import atomicJ.readers.nanopuller.NanopullerSpectroscopyReader;
import atomicJ.readers.nanoscope.NanoscopeGeneralSpectroscopyReader;
import atomicJ.readers.nanosurf.NIDSpectroscopyReader;
import atomicJ.readers.park.ParkSpectroscopyReader;
import atomicJ.readers.park.ParkSpectroscopyTextReader;
import atomicJ.readers.shimadzu.ShimadzuSpectroscopyReader;
import atomicJ.readers.text.CSVSpectroscopyReader;
import atomicJ.readers.text.TSVSpectroscopyReader;
import atomicJ.readers.wsxm.WSXMSpectroscopyReader;
import atomicJ.sources.SimpleSpectroscopySource;


public class GeneralSpectroscopyReader implements SourceReader<SimpleSpectroscopySource>
{	
    private static final String DESCRIPTION = "All supported AFM spectroscopy files";

    private final List<SourceReader<SimpleSpectroscopySource>> readers = new ArrayList<>();

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public GeneralSpectroscopyReader()
    {
        readers.add(new MISpectroscopyReader());
        readers.add(new JPKSpectroscopyReader());
        readers.add(new NanoscopeGeneralSpectroscopyReader());
        readers.add(new InnovaSpectroscopyReader());
        readers.add(new AsylumSpectroscopyReader());
        readers.add(new MDTSpectroscopyReader());
        readers.add(new ParkSpectroscopyReader());
        readers.add(new ParkSpectroscopyTextReader());
        readers.add(new CSISpectroscopyReader());
        readers.add(new AFMWorkshopSpectroscopyReader());
        readers.add(new NIDSpectroscopyReader());
        readers.add(new NanopullerSpectroscopyReader());
        readers.add(new AnasysSpectroscopyReader());
        readers.add(new ShimadzuSpectroscopyReader());
        readers.add(new GwyddionGwySpectroscopyReader());
        readers.add(new WSXMSpectroscopyReader());
        readers.add(new CSVSpectroscopyReader());
        readers.add(new TSVSpectroscopyReader());
    }

    public static String[] getAcceptedExtensions()
    {
        List<String> allAcceptedExtensions = new ArrayList<>();
        allAcceptedExtensions.addAll(Arrays.asList(MISpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(JPKSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(NanoscopeGeneralSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(InnovaSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AsylumSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(MDTSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ParkSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ParkSpectroscopyTextReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(CSISpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AFMWorkshopSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(NIDSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(NanopullerSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AnasysSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ShimadzuSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(GwyddionGwySpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(WSXMSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(CSVSpectroscopyReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(TSVSpectroscopyReader.getAcceptedExtensions()));

        return allAcceptedExtensions.toArray(new String[] {});
    }

    @Override
    public boolean accept(File f) 
    {
        for(SourceReader<SimpleSpectroscopySource> reader : readers)
        {            
            if(reader.accept(f))
            {
                return true;
            }
        }     

        return false;
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingState)  throws UserCommunicableException, IllegalImageException, IllegalSpectroscopySourceException
    {    		
        for(SourceReader<SimpleSpectroscopySource> reader : readers)
        {
            if(reader.accept(f))
            {
                List<SimpleSpectroscopySource> sources = reader.readSources(f, readingState);

                if(!sources.isEmpty())
                {
                    return sources;
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean prepareSourceReader(List<File> files) throws UserCommunicableException 
    {
        boolean canceled = false;
        for(SourceReader<SimpleSpectroscopySource> reader : readers)
        {
            boolean readerCanceled = reader.prepareSourceReader(files);
            canceled = canceled || readerCanceled;
            if(canceled)
            {
                break;
            }
        }
        return canceled;
    }
}

