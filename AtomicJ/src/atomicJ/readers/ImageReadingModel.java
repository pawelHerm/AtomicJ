
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

package atomicJ.readers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import atomicJ.data.ChannelFilter;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.readers.afmworkshop.AFMWorkshopImageReaderFactory;
import atomicJ.readers.anasys.AnasysImageReaderFactory;
import atomicJ.readers.asylum.AsylumImageReaderFactory;
import atomicJ.readers.csinstruments.CSIImageReaderFactory;
import atomicJ.readers.general.GeneralImageReaderFactory;
import atomicJ.readers.gwyddionGwy.GwyddionGwyImageReaderFactory;
import atomicJ.readers.gwyddionSimple.GwyddionGsfImageReaderFactory;
import atomicJ.readers.innova.InnovaImageReaderFactory;
import atomicJ.readers.jpk.JPKImageReaderFactory;
import atomicJ.readers.jpk.JPKQIImageReaderFactory;
import atomicJ.readers.mdt.MDTImageReaderFactory;
import atomicJ.readers.mi.MIImageReaderFactory;
import atomicJ.readers.nanoscope.NanoscopeImageReaderFactory;
import atomicJ.readers.nanosurf.NIDImageReaderFactory;
import atomicJ.readers.regularImage.BMPImageReaderFactory;
import atomicJ.readers.regularImage.BioRadImageReaderFactory;
import atomicJ.readers.regularImage.GIFImageReaderFactory;
import atomicJ.readers.regularImage.JPEGImageReaderFactory;
import atomicJ.readers.regularImage.PNGImageReaderFactory;
import atomicJ.readers.regularImage.TIFFImageReaderFactory;
import atomicJ.readers.regularImage.ZeissImageReaderFactory;
import atomicJ.readers.text.CSVImageReaderFactory;
import atomicJ.readers.text.TSVImageReaderFactory;
import atomicJ.readers.wsxm.WSxMImageReaderFactory;
import atomicJ.sources.ImageSource;

public class ImageReadingModel implements SourceReadingModel<ImageSource>
{
    private ChannelFilter dataFilter = PermissiveChannelFilter.getInstance();

    private final Map<FileFilter, SourceReaderFactory<? extends SourceReader<ImageSource>>> readerFilterMap = new LinkedHashMap<>();
    private final Map<String, FileFilter> filterNameMap = new LinkedHashMap<>();

    private final FileFilter defaultFilter;

    private ImageReadingModel()
    {		
        GeneralImageReaderFactory general = new GeneralImageReaderFactory();
        MIImageReaderFactory mi = new MIImageReaderFactory();
        JPKImageReaderFactory jpk = new JPKImageReaderFactory();
        JPKQIImageReaderFactory jpkQI = new JPKQIImageReaderFactory();
        NanoscopeImageReaderFactory nanoscope = new NanoscopeImageReaderFactory();
        InnovaImageReaderFactory innova = new InnovaImageReaderFactory();
        AsylumImageReaderFactory asylum = new AsylumImageReaderFactory();
        MDTImageReaderFactory mdt = new MDTImageReaderFactory();
        CSIImageReaderFactory csi = new CSIImageReaderFactory();
        AFMWorkshopImageReaderFactory afmWorkshop = new AFMWorkshopImageReaderFactory();
        NIDImageReaderFactory nanosurf = new NIDImageReaderFactory();
        AnasysImageReaderFactory anasys = new AnasysImageReaderFactory();
        GwyddionGwyImageReaderFactory gwy = new GwyddionGwyImageReaderFactory();
        GwyddionGsfImageReaderFactory gsf = new GwyddionGsfImageReaderFactory();
        WSxMImageReaderFactory wsxm = new WSxMImageReaderFactory();
        CSVImageReaderFactory csv = new CSVImageReaderFactory();
        TSVImageReaderFactory tsv = new TSVImageReaderFactory();
        BioRadImageReaderFactory bioRad = new BioRadImageReaderFactory();
        ZeissImageReaderFactory zeiss = new ZeissImageReaderFactory();
        TIFFImageReaderFactory tiff = new TIFFImageReaderFactory();
        JPEGImageReaderFactory jpeg = new JPEGImageReaderFactory();
        PNGImageReaderFactory png = new PNGImageReaderFactory();
        GIFImageReaderFactory gif = new GIFImageReaderFactory();
        BMPImageReaderFactory bmp = new BMPImageReaderFactory();

        defaultFilter = general.getFileFilter();

        readerFilterMap.put(defaultFilter, general);
        readerFilterMap.put(mi.getFileFilter(), mi);
        readerFilterMap.put(jpk.getFileFilter(), jpk);
        readerFilterMap.put(jpkQI.getFileFilter(), jpkQI);
        readerFilterMap.put(nanoscope.getFileFilter(), nanoscope);
        readerFilterMap.put(innova.getFileFilter(), innova);
        readerFilterMap.put(asylum.getFileFilter(), asylum);
        readerFilterMap.put(mdt.getFileFilter(), mdt);
        readerFilterMap.put(csi.getFileFilter(),csi);
        readerFilterMap.put(afmWorkshop.getFileFilter(), afmWorkshop);
        readerFilterMap.put(nanosurf.getFileFilter(), nanosurf);
        readerFilterMap.put(anasys.getFileFilter(), anasys);
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

    public static ImageReadingModel getInstance()
    {
        return new ImageReadingModel();
    }

    @Override
    public void setDataFilter(ChannelFilter filter)
    {
        this.dataFilter = filter;
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
    public SourceReader<ImageSource> getSourceReader(FileFilter filter) 
    {
        SourceReaderFactory<? extends SourceReader<ImageSource>>
        factory = readerFilterMap.get(filter);

        SourceReader<ImageSource> reader = (factory != null) ? factory.getReader() : null;

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
