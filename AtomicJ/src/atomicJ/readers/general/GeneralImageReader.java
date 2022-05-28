
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
import atomicJ.readers.afmworkshop.AFMWorkshopImageReader;
import atomicJ.readers.anasys.AnasysImageReader;
import atomicJ.readers.asylum.AsylumImageReader;
import atomicJ.readers.csinstruments.CSIImageReader;
import atomicJ.readers.gwyddionGwy.GwyddionGwyImageReader;
import atomicJ.readers.gwyddionSimple.GwyddionGsfImageReader;
import atomicJ.readers.innova.InnovaImageReader;
import atomicJ.readers.jpk.JPKImageReader;
import atomicJ.readers.jpk.JPKQIImageReader;
import atomicJ.readers.mdt.MDTImageReader;
import atomicJ.readers.mi.MIImageReader;
import atomicJ.readers.nanoscope.NanoscopeImageReader;
import atomicJ.readers.nanosurf.NIDImageReader;
import atomicJ.readers.park.ParkImageReader;
import atomicJ.readers.regularImage.BMPImageReader;
import atomicJ.readers.regularImage.BioRadImageReader;
import atomicJ.readers.regularImage.GIFImageReader;
import atomicJ.readers.regularImage.JPEGImageReader;
import atomicJ.readers.regularImage.PNGImageReader;
import atomicJ.readers.regularImage.TIFFImageReader;
import atomicJ.readers.regularImage.ZeissImageReader;
import atomicJ.readers.text.CSVImageReader;
import atomicJ.readers.text.TSVImageReader;
import atomicJ.readers.wsxm.WSxMImageReader;
import atomicJ.sources.ImageSource;

public class GeneralImageReader implements SourceReader<ImageSource>
{	
    private static final String DESCRIPTION = "All supported image files";

    private final List<SourceReader<ImageSource>> readers = new ArrayList<>();

    public GeneralImageReader()
    {
        readers.add(new MIImageReader());
        readers.add(new JPKImageReader());
        readers.add(new JPKQIImageReader());
        readers.add(new NanoscopeImageReader());
        readers.add(new InnovaImageReader());
        readers.add(new AsylumImageReader());
        readers.add(new MDTImageReader());
        readers.add(new ParkImageReader());
        readers.add(new CSIImageReader());
        readers.add(new AFMWorkshopImageReader());
        readers.add(new NIDImageReader());
        readers.add(new AnasysImageReader());
        readers.add(new GwyddionGwyImageReader());
        readers.add(new GwyddionGsfImageReader());
        readers.add(new WSxMImageReader());
        readers.add(new CSVImageReader());
        readers.add(new TSVImageReader()); 
        readers.add(new BioRadImageReader());
        readers.add(new ZeissImageReader());
        readers.add(new TIFFImageReader());
        readers.add(new JPEGImageReader());
        readers.add(new PNGImageReader());
        readers.add(new GIFImageReader());
        readers.add(new BMPImageReader());
    }  

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        List<String> allAcceptedExtensions = new ArrayList<>();
        allAcceptedExtensions.addAll(Arrays.asList(MIImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(JPKImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(JPKQIImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(NanoscopeImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(InnovaImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AsylumImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(MDTImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ParkImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(CSIImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AFMWorkshopImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(NIDImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(AnasysImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(GwyddionGwyImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(GwyddionGsfImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(WSxMImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(CSVImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(TSVImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(BioRadImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(ZeissImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(TIFFImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(JPEGImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(PNGImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(GIFImageReader.getAcceptedExtensions()));
        allAcceptedExtensions.addAll(Arrays.asList(BMPImageReader.getAcceptedExtensions()));

        return allAcceptedExtensions.toArray(new String[] {});
    }

    @Override
    public boolean accept(File f) 
    {
        for(SourceReader<ImageSource> reader : readers)
        {            
            if(reader.accept(f))
            {
                return true;
            }
        }     

        return false;
    }

    @Override
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingState)  throws UserCommunicableException, IllegalImageException, IllegalSpectroscopySourceException
    {    		
        for(SourceReader<ImageSource> reader : readers)
        {
            if(reader.accept(f))
            {
                List<ImageSource> sources = reader.readSources(f, readingState);

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
        for(SourceReader<ImageSource> reader : readers)
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

