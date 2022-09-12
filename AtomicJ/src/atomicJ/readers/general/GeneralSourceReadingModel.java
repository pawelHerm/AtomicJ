
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

package atomicJ.readers.general;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import atomicJ.data.ChannelFilter;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.readers.SourceReader;
import atomicJ.readers.SourceReaderFactory;
import atomicJ.readers.SourceReadingModel;
import atomicJ.readers.afmworkshop.AFMWorkshopSourceReaderFactory;
import atomicJ.readers.anasys.AnasysSourceReaderFactory;
import atomicJ.readers.asylum.AsylumSourceReaderFactory;
import atomicJ.readers.csinstruments.CSISourceReaderFactory;
import atomicJ.readers.gwyddionGwy.GwyddionGwySourceReaderFactory;
import atomicJ.readers.gwyddionSimple.GwyddionGsfSourceReaderFactory;
import atomicJ.readers.innova.InnovaSourceReaderFactory;
import atomicJ.readers.jpk.JPKSourceReaderFactory;
import atomicJ.readers.mdt.MDTSourceReaderFactory;
import atomicJ.readers.mi.MISourceReaderFactory;
import atomicJ.readers.nanopuller.NanopullerSourceReaderFactory;
import atomicJ.readers.nanoscope.NanoscopeSourceReaderFactory;
import atomicJ.readers.nanosurf.NIDSourceReaderFactory;
import atomicJ.readers.park.ParkSourceReaderFactory;
import atomicJ.readers.park.ParkTextSourceReaderFactory;
import atomicJ.readers.regularImage.BMPSourceReaderFactory;
import atomicJ.readers.regularImage.BioRadSourceReaderFactory;
import atomicJ.readers.regularImage.GIFSourceReaderFactory;
import atomicJ.readers.regularImage.JPEGSourceReaderFactory;
import atomicJ.readers.regularImage.PNGSourceReaderFactory;
import atomicJ.readers.regularImage.TIFFSourceReaderFactory;
import atomicJ.readers.regularImage.ZeissSourceReaderFactory;
import atomicJ.readers.shimadzu.ShimadzuSourceReaderFactory;
import atomicJ.readers.shimadzu.ShimadzuSpectroscopyReader;
import atomicJ.readers.text.CSVSourceReaderFactory;
import atomicJ.readers.text.TSVSourceReaderFactory;
import atomicJ.readers.wsxm.WSxMSourceReaderFactory;
import atomicJ.sources.ChannelSource;


public class GeneralSourceReadingModel implements SourceReadingModel<ChannelSource>
{
    private ChannelFilter dataFilter = PermissiveChannelFilter.getInstance();

    private final Map<FileFilter, SourceReaderFactory<? extends SourceReader<ChannelSource>>> readerFilterMap = new LinkedHashMap<>();
    private final Map<String, FileFilter> filterNameMap = new LinkedHashMap<>();

    private final FileFilter defaultFilter;

    private GeneralSourceReadingModel()
    {	
        GeneralSourceReaderFactory general = new GeneralSourceReaderFactory();
        MISourceReaderFactory mi = new MISourceReaderFactory();
        JPKSourceReaderFactory pk = new JPKSourceReaderFactory();
        NanoscopeSourceReaderFactory nanoscope = new NanoscopeSourceReaderFactory();
        InnovaSourceReaderFactory innova = new InnovaSourceReaderFactory();
        AsylumSourceReaderFactory asylum = new AsylumSourceReaderFactory();
        MDTSourceReaderFactory mdt = new MDTSourceReaderFactory();
        ParkSourceReaderFactory park = new ParkSourceReaderFactory();
        ParkTextSourceReaderFactory parkText = new ParkTextSourceReaderFactory();
        CSISourceReaderFactory csi = new CSISourceReaderFactory();
        AFMWorkshopSourceReaderFactory afmWorkshop = new AFMWorkshopSourceReaderFactory();
        NIDSourceReaderFactory nanosurf = new NIDSourceReaderFactory();
        NanopullerSourceReaderFactory nanopuller = new NanopullerSourceReaderFactory();
        AnasysSourceReaderFactory anasys = new AnasysSourceReaderFactory();
        ShimadzuSourceReaderFactory shimadzu = new ShimadzuSourceReaderFactory();
        GwyddionGwySourceReaderFactory gwy = new GwyddionGwySourceReaderFactory();
        GwyddionGsfSourceReaderFactory gsf = new GwyddionGsfSourceReaderFactory();
        WSxMSourceReaderFactory wsxm = new WSxMSourceReaderFactory();
        CSVSourceReaderFactory csv = new CSVSourceReaderFactory();
        TSVSourceReaderFactory tsv = new TSVSourceReaderFactory();
        BioRadSourceReaderFactory bioRad = new BioRadSourceReaderFactory();
        ZeissSourceReaderFactory zeiss = new ZeissSourceReaderFactory();
        TIFFSourceReaderFactory tiff = new TIFFSourceReaderFactory();
        JPEGSourceReaderFactory jpeg = new JPEGSourceReaderFactory();
        PNGSourceReaderFactory png = new PNGSourceReaderFactory();
        GIFSourceReaderFactory gif = new GIFSourceReaderFactory();
        BMPSourceReaderFactory bmp = new  BMPSourceReaderFactory();

        this.defaultFilter = general.getFileFilter();
        readerFilterMap.put(defaultFilter, general);
        readerFilterMap.put(mi.getFileFilter(), mi);
        readerFilterMap.put(pk.getFileFilter(), pk);
        readerFilterMap.put(nanoscope.getFileFilter(), nanoscope);   
        readerFilterMap.put(innova.getFileFilter(), innova);
        readerFilterMap.put(asylum.getFileFilter(), asylum);    
        readerFilterMap.put(mdt.getFileFilter(), mdt);
        readerFilterMap.put(park.getFileFilter(), park);
        readerFilterMap.put(parkText.getFileFilter(), parkText);
        readerFilterMap.put(csi.getFileFilter(), csi);
        readerFilterMap.put(afmWorkshop.getFileFilter(), afmWorkshop);
        readerFilterMap.put(nanosurf.getFileFilter(), nanosurf);
        readerFilterMap.put(nanopuller.getFileFilter(), nanopuller);
        readerFilterMap.put(anasys.getFileFilter(), anasys);
        readerFilterMap.put(shimadzu.getFileFilter(), shimadzu);
        readerFilterMap.put(gwy.getFileFilter(), gwy);
        readerFilterMap.put(gsf.getFileFilter(), gsf);
        readerFilterMap.put(wsxm.getFileFilter(), wsxm);
        readerFilterMap.put(csv.getFileFilter(), csv);
        readerFilterMap.put(tsv.getFileFilter(), tsv);
        readerFilterMap.put(bioRad.getFileFilter(), bioRad);
        readerFilterMap.put(zeiss.getFileFilter(), zeiss);
        readerFilterMap.put(tiff.getFileFilter(), tiff);
        readerFilterMap.put(jpeg.getFileFilter(), jpeg);
        readerFilterMap.put(png.getFileFilter(), png);
        readerFilterMap.put(gif.getFileFilter(), gif);
        readerFilterMap.put(bmp.getFileFilter(), bmp);

        for(FileFilter filter: readerFilterMap.keySet())
        {
            filterNameMap.put(filter.getDescription(), filter);
        }
    }

    public static GeneralSourceReadingModel getInstance()
    {
        return new GeneralSourceReadingModel();
    }

    @Override
    public void setDataFilter(ChannelFilter dataFilter)
    {
        this.dataFilter = dataFilter;
    }

    @Override
    public ChannelFilter getDataFilter()
    {
        return this.dataFilter;
    }

    @Override
    public List<FileFilter> getExtensionFilters() 
    {
        return new ArrayList<>(filterNameMap.values());
    }

    @Override
    public SourceReader<ChannelSource> getSourceReader(FileFilter filter) 
    {
        SourceReaderFactory<? extends SourceReader<ChannelSource>>
        factory = readerFilterMap.get(filter);

        SourceReader<ChannelSource> reader = (factory != null) ? factory.getReader(): null;

        return reader;   
    }

    @Override
    public FileFilter getExtensionFilter(String filterName) 
    {
        FileFilter filter = filterNameMap.get(filterName);
        if(filter == null)
        {
            filter = defaultFilter;
        }
        return filter;
    }
}
