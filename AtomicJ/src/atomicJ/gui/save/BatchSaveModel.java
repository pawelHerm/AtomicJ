
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

import static atomicJ.gui.save.SaveModelProperties.*;


import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;


import org.jfree.chart.JFreeChart;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.NameComponent;
import atomicJ.utilities.IOUtilities;

public class BatchSaveModel extends AbstractModel implements PropertyChangeListener
{
    private File directory;
    private String archiveName;

    private boolean saveInArchive;
    private ArchiveType archiveType;

    private ChartSaveFormatType currentFormatType;
    private final ChartSaveFormatType[] formatTypes = new ChartSaveFormatType[] 
            {new EPSFormatType(), new PSFormatType(), new PDFFormatType(), 
                    new SVGFormatType(), new TIFFFormatType(), new EMFFormatType(),
                    new JPEGFormatType(), new JPEG2000FormatType(), new PNGFormatType(),
                    new GIFFormatType(), new PPMFormatType(), new BMPFormatType(),
                    new CSVFormatType(), new TSVFormatType()};

    private Integer initSerial;
    private boolean extensionsAppended;

    private final Map<String, ChannelSpecificSaveSettingsModel> seriesSpecificModels = new Hashtable<>();

    private boolean formatParametersSpecified;
    private boolean inputSpecified;
    private boolean multiplePagesSupported;

    public BatchSaveModel()
    {
        initDefaults();
        checkIfNecessaryInputProvided();
    }

    private void initDefaults()
    {
        this.directory = null;
        this.archiveName = "";
        this.archiveType = ArchiveType.ZIP;
        this.currentFormatType = formatTypes[0];
        this.initSerial = Integer.valueOf(1);
        this.extensionsAppended = true;
        this.formatParametersSpecified = currentFormatType.isNecessaryIputProvided();
        currentFormatType.addPropertyChangeListener(this);
        updateMultiplePagesSupported();
        checkIfNecessaryInputProvided();
    }

    public ChartSaveFormatType getFormatType(String description)
    {
        ChartSaveFormatType formatType = null;

        if(description == null)
        {
            return formatType;
        }

        for(ChartSaveFormatType type : formatTypes)
        {
            String typeDescription = type.getDescription();

            if(typeDescription.equals(description))
            {
                formatType = type;
                break;
            }
        }

        return formatType;
    }

    public void addKey(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = new ChannelSpecificSaveSettingsModel(key);
        seriesModel.addPropertyChangeListener(this);
        seriesSpecificModels.put(key,seriesModel);
    }

    public boolean isNecessaryInputProvided()
    {
        return inputSpecified;
    }

    public ChartSaveFormatType[] getFormatTypes()
    {
        return formatTypes;
    }

    public File getDirectory()
    {
        return directory;
    }

    public void setDirectory(File directoryNew)
    {		
        if(directoryNew != null && !directoryNew.isDirectory())
        {
            throw new IllegalArgumentException("The File object passed as the 'file' argument should be a directory");
        }

        File directoryOld = directoryNew;
        this.directory = directoryNew;

        firePropertyChange(DIRECTORY, directoryOld, directoryNew);
    }

    public boolean isSeriesToBeSaved(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = seriesSpecificModels.get(key);
        return seriesModel.getSave();	
    }

    public void setSaveSeries(boolean saveNew, String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = seriesSpecificModels.get(key);
        seriesModel.setSave(saveNew);
    }

    public String getArchiveName()
    {
        return archiveName;
    }

    public void setArchiveName(String archiveNameNew)
    {
        String archiveNameOld = archiveName;
        this.archiveName = archiveNameNew;

        firePropertyChange(ARCHIVE_NAME, archiveNameOld, archiveNameNew);

        checkIfNecessaryInputProvided();
    }

    public ArchiveType getArchiveType()
    {
        return archiveType;
    }

    public void setArchiveType(ArchiveType archiveTypeNew)
    {
        if(archiveTypeNew == null)
        {
            throw new IllegalArgumentException("Null 'archiveTypeNew' argument");
        }

        ArchiveType archiveTypeOld = this.archiveType;

        if(!archiveTypeOld.equals(archiveTypeNew))
        {
            this.archiveType = archiveTypeNew;
            firePropertyChange(ARCHIVE_TYPE, archiveTypeOld, archiveTypeNew);
        }
    }

    public boolean getSaveInArchive()
    {
        return saveInArchive;
    }

    public void setSaveInArchive(boolean saveInArchiveNew)
    {
        boolean saveInArchiveOld = saveInArchive;
        this.saveInArchive = saveInArchiveNew;

        firePropertyChange(SAVE_IN_ARCHIVE, saveInArchiveOld, saveInArchive);

        checkIfNecessaryInputProvided();
    }

    public ChartSaveFormatType getSaveFormat()
    {
        return currentFormatType;
    }

    public void setSaveFormat(String descriptionSaveFormatNew)
    {
        ChartSaveFormatType saveFormatNew = getFormatType(descriptionSaveFormatNew);
        if(saveFormatNew != null)
        {
            setSaveFormat(saveFormatNew);
        }
    }

    public void setSaveFormat(ChartSaveFormatType saveFormatNew)
    {
        if(saveFormatNew == null)
        {
            throw new IllegalArgumentException("Null 'saveFormatNew' argument");
        }

        ChartSaveFormatType saveFormatOld = currentFormatType;
        this.currentFormatType = saveFormatNew;

        saveFormatOld.removePropertyChangeListener(this);
        saveFormatNew.addPropertyChangeListener(this);

        firePropertyChange(SAVE_FORMAT, saveFormatOld, saveFormatNew);

        updateMultiplePagesSupported();
        checkIfNecessaryInputProvided();
    }

    public boolean areExtensionsAppended()
    {
        return extensionsAppended;
    }

    public Integer getInitialSerialNumber()
    {
        return initSerial;
    }

    public Object getPrefix(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = seriesSpecificModels.get(key);

        return seriesModel.getPrefix();	
    }

    public void setPrefix(Object prefixNew, String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = seriesSpecificModels.get(key);

        seriesModel.setPrefix(prefixNew);
    }

    public Object getRoot(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = seriesSpecificModels.get(key);
        return seriesModel.getRoot();
    }

    public void setRoot(Object rootNew, String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = seriesSpecificModels.get(key);
        seriesModel.setRoot(rootNew);
    }

    public Object getSuffix(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = seriesSpecificModels.get(key);
        return seriesModel.getSuffix();	
    }

    public void setSuffix(Object suffixNew, String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = seriesSpecificModels.get(key);
        seriesModel.setSuffix(suffixNew);
    }

    public void setInitialSerialNumber(Integer initSerialNew)
    {
        Integer initSerialOld = initSerial;
        this.initSerial = initSerialNew;

        firePropertyChange(INITIAL_SERIAL, initSerialOld, initSerialNew);
    }

    public void setAppendExtensions(boolean extensionsAppendedNew)
    {
        boolean extensionsAppendedOld = extensionsAppended;
        this.extensionsAppended = extensionsAppendedNew;

        firePropertyChange(ASPECT_CONSTANT, extensionsAppendedOld, extensionsAppendedNew);
    }

    public Map<String, List<String>> getNames(Map<String, List<String>> allDefaultNames)
    {
        Map<String, List<String>> allNames = new Hashtable<>();

        for(String key: allDefaultNames.keySet())
        {
            List<String> defaultNames = allDefaultNames.get(key);
            List<String> names = getNames(key, defaultNames);
            allNames.put(key, names);
        }
        return allNames;
    }

    private List<String> getNames(String key, List<String> defaultNames)
    {
        int n = defaultNames.size();
        List<String> names = new ArrayList<>();
        ChartSaver saver = currentFormatType.getChartSaver();

        String extension = saver.getExtension();

        String prefixString = getPrefix(key).toString();
        String rootString = getRoot(key).toString();
        String sufixString = getSuffix(key).toString();

        for(int i = 0;i<n;i++)
        {
            int serial = i + initSerial;

            String name;
            if(prefixString.equals(NameComponent.NAME.toString())){name = defaultNames.get(i);}
            else if(prefixString.equals(NameComponent.SERIAL_NUMBER.toString())){name = Integer.toString(serial);}
            else if(prefixString.equals(NameComponent.PREFIX.toString())){name = key + "_";}
            else {name = prefixString;}

            if(rootString.equals(NameComponent.NAME.toString())){name = name + defaultNames.get(i);}
            else if(rootString.equals(NameComponent.SERIAL_NUMBER.toString())){name = name + Integer.toString(serial);}
            else if(rootString.equals(NameComponent.ROOT.toString())){name = name + defaultNames.get(i);}
            else {name = name + rootString;}

            if(sufixString.equals(NameComponent.NAME.toString())){name = name + defaultNames.get(i);}
            else if(sufixString.equals(NameComponent.SERIAL_NUMBER.toString())){name = name + Integer.toString(serial);}
            else if(sufixString.equals(NameComponent.SUFFIX.toString())){name = name + "_" + Integer.toString(serial);}
            else {name = name + sufixString;}

            if(extensionsAppended)
            {
                name = name + extension;
            }

            names.add(name);
        }	
        return names;
    }

    public Map<String, List<File>> getPaths(Map<String, List<String>> allDefaultNames, Map<String, List<File>> allDefaultLocations)
    {
        Map<String, List<File>> allPaths = new Hashtable<>();

        for(String key: allDefaultNames.keySet())
        {
            List<String> defaultNames = allDefaultNames.get(key);

            List<File> defaultLocations = allDefaultLocations.get(key);
            List<File> paths = getPaths(key, defaultNames, defaultLocations);

            allPaths.put(key, paths);
        }
        return allPaths;
    }

    private List<File> getPaths(String key, List<String> defaultNames, List<File> defaultLocations)
    {
        List<File> paths = new ArrayList<>();
        List<String> names = getNames(key, defaultNames);

        int n = names.size();
        for(int i = 0;i<n;i++)
        {
            File par = (directory == null) ? defaultLocations.get(i) : directory;

            File path = new File(par, names.get(i));
            paths.add(path);
        }
        return paths;
    }

    public void save(Component parent, Map<String, List<? extends JFreeChart>> allCharts,  Map<String, List<String>> allDefaultNames, Map<String, List<File>> allDefaultLocations)
    {
        if(saveInArchive)
        {
            saveInArchive(parent, allCharts, allDefaultNames, allDefaultLocations);
        }
        else
        {
            saveWithoutArchive(parent, allCharts, allDefaultNames, allDefaultLocations);
        }
    }

    private void saveWithoutArchive(Component parent, Map<String, List<? extends JFreeChart>> allCharts,  Map<String, List<String>> allDefaultNames, Map<String, List<File>> allDefaultLocations)
    {
        Map<String, List<File>> allPathsMap = getPaths(allDefaultNames, allDefaultLocations);

        List<File> allPathsList = new ArrayList<>();
        List<JFreeChart> allChartsList = new ArrayList<>();

        for(String key: allCharts.keySet())
        {
            List<File> paths = allPathsMap.get(key);
            List<? extends JFreeChart> charts = allCharts.get(key);

            allPathsList.addAll(paths);
            allChartsList.addAll(charts);
        }

        if((allChartsList.size() !=  allPathsList.size()))
        {
            throw new IllegalArgumentException("Lists 'charts', 'defaultNames' and 'defaultLocations' should have the same length");
        }

        List<Saveable> packs = new ArrayList<>();

        int n = allPathsList.size();

        ChartSaver saver = currentFormatType.getChartSaver();

        for(int i = 0;i<n;i++)
        {
            JFreeChart chart = allChartsList.get(i);
            File path = allPathsList.get(i);

            Saveable pack = new SimpleSavablePack(chart, path, saver, null);
            packs.add(pack);
        }

        ConcurrentSavingTask task = new ConcurrentSavingTask(packs, parent);
        task.execute();
    }

    private String getFullArchiveName()
    {
        String ext = "." + archiveType.getExtension();
        String fullArchiveName = (archiveName.trim().endsWith(ext)) ? archiveName : archiveName.replaceFirst("[.][^.]+$", "") + ext;

        return fullArchiveName;
    }

    private void saveInArchive(Component parent, Map<String, List<? extends JFreeChart>> allCharts,  Map<String, List<String>> allDefaultNames, Map<String, List<File>> allDefaultLocations)
    {
        ChartSaver saver = currentFormatType.getChartSaver();

        String fullArchiveName = getFullArchiveName();
        Map<String, List<String>> allNamesMap = getNames(allDefaultNames);

        List<String> allNamesList = new ArrayList<>();
        List<File> allPathsList = new ArrayList<>();
        List<JFreeChart> allChartsList = new ArrayList<>();

        for(String key: allCharts.keySet())
        {
            List<String> names = allNamesMap.get(key);
            List<File> paths = allDefaultLocations.get(key);
            List<? extends JFreeChart> charts = allCharts.get(key);

            allNamesList.addAll(names);
            allPathsList.addAll(paths);
            allChartsList.addAll(charts);
        }

        File path = (directory == null) ? new File(IOUtilities.findLastCommonDirectory(allPathsList),fullArchiveName) :
            new File(directory, fullArchiveName);

        if(path.exists())
        {
            int result = JOptionPane.showConfirmDialog(parent,"The file exists, overwrite?",AtomicJ.APPLICATION_NAME,JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result)
            {
            case JOptionPane.NO_OPTION:
                return;
            case JOptionPane.CANCEL_OPTION:
                parent.setVisible(false);
                return;
            }
        }

        int  n = allNamesList.size();

        List<StreamSavable> savables = new ArrayList<>();

        for(int i = 0; i<n; i++)
        {
            String name = allNamesList.get(i);
            JFreeChart chart = allChartsList.get(i);

            SimpleStreamSavablePack pack = new SimpleStreamSavablePack(chart, name, saver);
            savables.add(pack);
        }

        SwingWorker<?,?> task = archiveType.getSavingTask(savables, path, parent);
        task.execute();
    }

    private void checkIfNecessaryInputProvided()
    {
        boolean inputSpecifiedNew = checkIfArchiveSettingsSpecified() && checkIfFormatParametersProvided();		
        boolean inputSpecifiedOld = inputSpecified;
        this.inputSpecified = inputSpecifiedNew;

        firePropertyChange(INPUT_PROVIDED, inputSpecifiedOld, inputSpecifiedNew);
    }

    private boolean checkIfFormatParametersProvided()
    {
        boolean formatParametersSpecifiedNew = currentFormatType.isNecessaryIputProvided();		
        this.formatParametersSpecified = formatParametersSpecifiedNew;
        return formatParametersSpecified;
    }

    private boolean checkIfArchiveSettingsSpecified()
    {
        if(!saveInArchive)
        {
            return true;
        }
        else if(archiveName.length() == 0)
        {
            return false;
        }
        else
        {
            String fullArchiveName = getFullArchiveName();
            boolean isValidFileName = IOUtilities.isFilenameValid(fullArchiveName);
            return isValidFileName;
        }	
    }

    public boolean multiplePagesSupported()
    {
        return multiplePagesSupported;
    }

    private void updateMultiplePagesSupported()
    {
        boolean multiplePagesSupportedNew = currentFormatType.supportMultiplePages();

        if(this.multiplePagesSupported != multiplePagesSupportedNew)
        {
            boolean multiplePagesSupportedOld = this.multiplePagesSupported;
            this.multiplePagesSupported = multiplePagesSupportedNew;

            firePropertyChange(MULTIPLE_PAGES_SUPPORTED, multiplePagesSupportedOld,
                    multiplePagesSupportedNew);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();
        if(FORMAT_PARAMETERS_PROVIDED.equals(property))
        {
            checkIfNecessaryInputProvided();
        }

        firePropertyChange(evt);
    }
}
