
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
import atomicJ.readers.afmworkshop.AFMWorkshopSourceReader;
import atomicJ.readers.anasys.AnasysSourceReader;
import atomicJ.readers.asylum.AsylumSourceReader;
import atomicJ.readers.csinstruments.CSISourceReader;
import atomicJ.readers.gwyddionGwy.GwyddionGwySourceReader;
import atomicJ.readers.gwyddionSimple.GwyddionGsfSourceReader;
import atomicJ.readers.innova.InnovaSourceReader;
import atomicJ.readers.jpk.JPKSourceReader;
import atomicJ.readers.mdt.MDTSourceReader;
import atomicJ.readers.mi.MISourceReader;
import atomicJ.readers.nanopuller.NanopullerSourceReader;
import atomicJ.readers.nanoscope.NanoscopeSourceReader;
import atomicJ.readers.nanosurf.NIDSourceReader;
import atomicJ.readers.park.ParkSourceReader;
import atomicJ.readers.park.ParkTextSourceReader;
import atomicJ.readers.regularImage.BMPSourceReader;
import atomicJ.readers.regularImage.BioRadImageReader;
import atomicJ.readers.regularImage.BioRadSourceReader;
import atomicJ.readers.regularImage.GIFSourceReader;
import atomicJ.readers.regularImage.JPEGSourceReader;
import atomicJ.readers.regularImage.PNGSourceReader;
import atomicJ.readers.regularImage.TIFFSourceReader;
import atomicJ.readers.regularImage.ZeissImageReader;
import atomicJ.readers.regularImage.ZeissSourceReader;
import atomicJ.readers.shimadzu.ShimadzuSourceReader;
import atomicJ.readers.text.CSVSourceReader;
import atomicJ.readers.text.TSVSourceReader;
import atomicJ.readers.wsxm.WSxMSourceReader;
import atomicJ.sources.ChannelSource;


public class GeneralSourceReader implements SourceReader<ChannelSource>
{	
    private static final String DESCRIPTION = "All supported files";

    private final List<SourceReader<ChannelSource>> readers = new ArrayList<>();

    public GeneralSourceReader()
    {
        readers.add(new MISourceReader());
        readers.add(new JPKSourceReader());
        readers.add(new NanoscopeSourceReader());
        readers.add(new InnovaSourceReader());
        readers.add(new AsylumSourceReader());
        readers.add(new MDTSourceReader());
        readers.add(new ParkSourceReader());
        readers.add(new ParkTextSourceReader());
        readers.add(new CSISourceReader());
        readers.add(new AFMWorkshopSourceReader());
        readers.add(new NIDSourceReader());
        readers.add(new NanopullerSourceReader());
        readers.add(new AnasysSourceReader());
        readers.add(new ShimadzuSourceReader());
        readers.add(new GwyddionGwySourceReader());
        readers.add(new GwyddionGsfSourceReader());
        readers.add(new WSxMSourceReader());
        readers.add(new CSVSourceReader());
        readers.add(new TSVSourceReader());
        readers.add(new BioRadSourceReader());
        readers.add(new ZeissSourceReader());
        readers.add(new TIFFSourceReader());
        readers.add(new JPEGSourceReader());
        readers.add(new PNGSourceReader());
        readers.add(new GIFSourceReader());
        readers.add(new BMPSourceReader());
    }

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        List<String> allAcceptedExtensions = new ArrayList<>();

        allAcceptedExtensions.addAll(Arrays.asList(MISourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(JPKSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(NanoscopeSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AsylumSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(MDTSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ParkSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ParkTextSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(CSISourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AFMWorkshopSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(NIDSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AnasysSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ShimadzuSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(InnovaSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(NanopullerSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(GwyddionGwySourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(GwyddionGsfSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(WSxMSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(CSVSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(TSVSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(BioRadImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ZeissImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(TIFFSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(JPEGSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(PNGSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(GIFSourceReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(BMPSourceReader.getAcceptedExtensions()));

        return allAcceptedExtensions.toArray(new String[] {});
    }

    @Override
    public boolean accept(File f) 
    {
        for(SourceReader<ChannelSource> reader : readers)
        {            
            if(reader.accept(f))
            {
                return true;
            }
        }     

        return false;
    }

    @Override
    public List<ChannelSource> readSources(File f, SourceReadingDirectives readingState)  throws UserCommunicableException, IllegalImageException, IllegalSpectroscopySourceException
    {    		
        for(SourceReader<ChannelSource> reader : readers)
        {
            if(reader.accept(f))
            {
                List<ChannelSource> sources = reader.readSources(f, readingState);

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
        for(SourceReader<ChannelSource> reader : readers)
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

