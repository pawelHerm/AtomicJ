
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
import atomicJ.readers.afmworkshop.AFMWorkshopSpectroscopyReaderFactory;
import atomicJ.readers.anasys.AnasysSpectroscopyReaderFactory;
import atomicJ.readers.asylum.AsylumSpectroscopyReaderFactory;
import atomicJ.readers.csinstruments.CSISpectroscopyReaderFactory;
import atomicJ.readers.general.GeneralSpectroscopyReaderFactory;
import atomicJ.readers.gwyddionGwy.GwyddionGwySpectroscopyReaderFactory;
import atomicJ.readers.innova.InnovaSpectroscopyReaderFactory;
import atomicJ.readers.jpk.JPKSpectroscopyReaderFactory;
import atomicJ.readers.mdt.MDTSpectroscopyReaderFactory;
import atomicJ.readers.mi.MISpectroscopyReaderFactory;
import atomicJ.readers.nanopuller.NanopullerSpectroscopyReaderFactory;
import atomicJ.readers.nanoscope.NanoscopeGeneralSpectroscopyReaderFactory;
import atomicJ.readers.nanosurf.NIDSpectroscopyReaderFactory;
import atomicJ.readers.park.ParkSpectroscopyReaderFactory;
import atomicJ.readers.park.ParkSpectroscopyTextReaderFactory;
import atomicJ.readers.text.CSVSpectroscopyReaderFactory;
import atomicJ.readers.text.TSVSpectroscopyReaderFactory;
import atomicJ.readers.wsxm.WSxMSpectroscopyReaderFactory;
import atomicJ.sources.SimpleSpectroscopySource;


public class SpectroscopyReadingModel implements SourceReadingModel<SimpleSpectroscopySource>
{
    private ChannelFilter dataFilter = PermissiveChannelFilter.getInstance();

    private final Map<FileFilter, SourceReaderFactory<? extends SourceReader<SimpleSpectroscopySource>>> readerFilterMap = new LinkedHashMap<>();
    private final Map<String, FileFilter> filterNameMap = new LinkedHashMap<>();

    private final FileFilter defaultFilter;

    private SpectroscopyReadingModel()
    {		
        GeneralSpectroscopyReaderFactory general = new GeneralSpectroscopyReaderFactory();
        MISpectroscopyReaderFactory mi = new MISpectroscopyReaderFactory();
        JPKSpectroscopyReaderFactory jpk = new JPKSpectroscopyReaderFactory();
        NanoscopeGeneralSpectroscopyReaderFactory nanoscope = new NanoscopeGeneralSpectroscopyReaderFactory();
        InnovaSpectroscopyReaderFactory innova = new InnovaSpectroscopyReaderFactory();
        AsylumSpectroscopyReaderFactory asylum = new AsylumSpectroscopyReaderFactory();
        MDTSpectroscopyReaderFactory mdt = new MDTSpectroscopyReaderFactory();
        ParkSpectroscopyReaderFactory park = new ParkSpectroscopyReaderFactory();
        ParkSpectroscopyTextReaderFactory parkText = new ParkSpectroscopyTextReaderFactory();
        CSISpectroscopyReaderFactory csi = new CSISpectroscopyReaderFactory();
        AFMWorkshopSpectroscopyReaderFactory afmWorkshop = new AFMWorkshopSpectroscopyReaderFactory();
        NIDSpectroscopyReaderFactory nanosurf = new NIDSpectroscopyReaderFactory();
        NanopullerSpectroscopyReaderFactory nanopuller = new NanopullerSpectroscopyReaderFactory();
        AnasysSpectroscopyReaderFactory anasys = new AnasysSpectroscopyReaderFactory();
        GwyddionGwySpectroscopyReaderFactory gwy = new GwyddionGwySpectroscopyReaderFactory();
        WSxMSpectroscopyReaderFactory wsxm = new WSxMSpectroscopyReaderFactory();
        CSVSpectroscopyReaderFactory csv = new CSVSpectroscopyReaderFactory();
        TSVSpectroscopyReaderFactory tsv = new TSVSpectroscopyReaderFactory();

        defaultFilter = general.getFileFilter();

        readerFilterMap.put(defaultFilter, general);
        readerFilterMap.put(mi.getFileFilter(), mi);
        readerFilterMap.put(jpk.getFileFilter(), jpk);
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
        readerFilterMap.put(gwy.getFileFilter(), gwy);
        readerFilterMap.put(wsxm.getFileFilter(), wsxm);
        readerFilterMap.put(csv.getFileFilter(), csv);
        readerFilterMap.put(tsv.getFileFilter(), tsv);   

        for(FileFilter filter: readerFilterMap.keySet())
        {
            filterNameMap.put(filter.getDescription(), filter);
        }
    }

    public static SpectroscopyReadingModel getInstance()
    {
        return new SpectroscopyReadingModel();
    }

    @Override
    public List<FileFilter> getExtensionFilters() 
    {
        return new ArrayList<>(filterNameMap.values());
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
    public SourceReader<SimpleSpectroscopySource> getSourceReader(FileFilter filter) 
    {
        SourceReaderFactory<? extends SourceReader<SimpleSpectroscopySource>> factory = readerFilterMap.get(filter);

        SourceReader<SimpleSpectroscopySource> reader = (factory != null) ? factory.getReader(): null;
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
