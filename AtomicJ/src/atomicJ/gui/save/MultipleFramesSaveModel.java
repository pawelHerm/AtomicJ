
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

import atomicJ.gui.AbstractModel;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.NameComponent;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.Validation;



public class MultipleFramesSaveModel extends AbstractModel implements PropertyChangeListener
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

    private int firstFrame;
    private int lastFrame;
    private int frameCount;
    private Integer initSerial;
    private boolean extensionsAppended;

    private final Map<String, ChannelSpecificSaveSettingsModel> channelSpecificModels = new Hashtable<>();

    private boolean formatParametersSpecified;
    private boolean inputSpecified;

    public MultipleFramesSaveModel(int frameCount)
    {
        this.frameCount = frameCount;
        initDefaults();
        checkIfNecessaryInputProvided();
    }

    public void setFrameCount(int frameCount)
    {
        this.frameCount = frameCount;
        setLastFrame(frameCount);
    }

    private void initDefaults()
    {
        this.firstFrame = 1;
        this.lastFrame = frameCount;

        this.directory = null;
        this.archiveName = "";
        this.archiveType = ArchiveType.ZIP;

        this.currentFormatType = formatTypes[0];
        this.initSerial = Integer.valueOf(1);
        this.extensionsAppended = true;
        this.formatParametersSpecified = currentFormatType.isNecessaryIputProvided();
        currentFormatType.addPropertyChangeListener(this);
        checkIfNecessaryInputProvided();
    }

    public void addKey(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = new ChannelSpecificSaveSettingsModel(key);

        seriesModel.addPropertyChangeListener(this);
        channelSpecificModels.put(key,seriesModel);
    }

    public int getFrameCount()
    {
        return frameCount;
    }

    public int getFirstFrame()
    {
        return firstFrame;
    }

    public void setFirstFrame(int firstFrameNew)
    {
        if(firstFrameNew >  this.lastFrame)
        {
            throw new IllegalArgumentException("The value of the 'firstFrameNew' parameter is greater than the index of the last frame");
        }
        if(firstFrameNew >  this.frameCount || firstFrameNew <1)
        {
            throw new IllegalArgumentException("The value of the 'firstFrameNew' parameter is outside the range of frame indices");
        }
        int firstFrameOld = this.firstFrame;
        this.firstFrame = firstFrameNew;

        firePropertyChange(FIRST_FRAME, firstFrameOld, firstFrameNew);	
        checkIfNecessaryInputProvided();
    }

    public int getLastFrame()
    {
        return lastFrame;
    }

    public void setLastFrame(int lastFrameNew)
    {
        if(lastFrameNew < this.firstFrame)
        {
            throw new IllegalArgumentException("The value of the 'lastFrameNew' parameter is smaller than the index of the first frame");
        }
        if(lastFrameNew >  this.frameCount || lastFrameNew <1)
        {
            throw new IllegalArgumentException("The value of the 'lastFrameNew' parameter is outside the range of frame indices");
        }

        int lastFrameOld = this.lastFrame;
        this.lastFrame = lastFrameNew;

        firePropertyChange(LAST_FRAME, lastFrameOld, lastFrameNew);		
        checkIfNecessaryInputProvided();
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
        if(!directoryNew.isDirectory())
        {
            throw new IllegalArgumentException("The File object passed as the 'file' argument should be a directory");
        }

        File directoryOld = directoryNew;
        this.directory = directoryNew;

        firePropertyChange(DIRECTORY, directoryOld, directoryNew);
    }

    public boolean getSaveSeries(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = channelSpecificModels.get(key);
        return seriesModel.getSave();	
    }

    public void setSaveSeries(boolean saveNew, String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = channelSpecificModels.get(key);
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
        Validation.requireNonNullParameterName(archiveTypeNew, "archiveTypeNew");

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

    public void setSaveFormat(String descriptionSaveFormatNew)
    {
        ChartSaveFormatType saveFormatNew = getFormatType(descriptionSaveFormatNew);
        if(saveFormatNew != null)
        {
            setSaveFormat(saveFormatNew);
        }
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

    public void setSaveFormat(ChartSaveFormatType saveFormatNew)
    {
        Validation.requireNonNullParameterName(saveFormatNew, "saveFormatNew");

        ChartSaveFormatType saveFormatOld = currentFormatType;
        this.currentFormatType = saveFormatNew;

        saveFormatOld.removePropertyChangeListener(this);
        saveFormatNew.addPropertyChangeListener(this);

        firePropertyChange(SAVE_FORMAT, saveFormatOld, saveFormatNew);

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
        ChannelSpecificSaveSettingsModel seriesModel = channelSpecificModels.get(key);
        return seriesModel.getPrefix();	
    }

    public void setPrefix(Object prefixNew, String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = channelSpecificModels.get(key);
        seriesModel.setPrefix(prefixNew);
    }

    public Object getRoot(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = channelSpecificModels.get(key);
        return seriesModel.getRoot();
    }

    public void setRoot(Object rootNew, String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = channelSpecificModels.get(key);
        seriesModel.setRoot(rootNew);
    }

    public Object getSuffix(String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = channelSpecificModels.get(key);
        return seriesModel.getSuffix();	
    }

    public void setSuffix(Object suffixNew, String key)
    {
        ChannelSpecificSaveSettingsModel seriesModel = channelSpecificModels.get(key);
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
            File par;
            if(directory == null) 
            {
                par = defaultLocations.get(i);
            }
            else
            {
                par = directory;
            }			
            File path = new File(par, names.get(i));
            paths.add(path);
        }
        return paths;
    }

    public void save(Component parent, Map<String, Channel2DChart<?>> allCharts,  Map<String, List<String>> allDefaultNames, Map<String, List<File>> allDefaultLocations)
    {
        if(saveInArchive)
        {          
            saveInArchive(parent, allCharts, allDefaultNames, allDefaultLocations);
        }
        else
        {
            saveAsSeparateFiles(parent, allCharts, allDefaultNames, allDefaultLocations);
        }
    }

    private void saveAsSeparateFiles(Component parent, Map<String, Channel2DChart<?>> allCharts,  Map<String, List<String>> allDefaultNames, Map<String, List<File>> allDefaultLocations)
    {
        Map<String, List<File>> allPathsMap = getPaths(allDefaultNames, allDefaultLocations);

        ChartSaver saver = currentFormatType.getChartSaver();

        List<Saveable> saveablePacks = new ArrayList<>();
        for(String key: allCharts.keySet())
        {
            Channel2DChart<?> chart = allCharts.get(key);
            List<File> paths = allPathsMap.get(key);

            int frameToSaveCount = (lastFrame - firstFrame + 1);

            for(int i = 0; i<frameToSaveCount; i++)
            {
                int frame = i + firstFrame - 1;
                File path = paths.get(i);

                Saveable pack = new MovieSavablePack(chart, frame, path, saver, null);
                saveablePacks.add(pack);
            }						
        }

        SavingTask task = new SavingTask(parent, saveablePacks);
        task.execute();
    }

    private String getFullArchiveName()
    {
        String ext = "." +  archiveType.getExtension();
        String fullArchiveName;
        if(archiveName.trim().endsWith(ext))
        {
            fullArchiveName = archiveName;
        }
        else
        {
            fullArchiveName = archiveName.replaceFirst("[.][^.]+$", "") + ext;
        }

        return fullArchiveName;
    }

    private void saveInArchive(Component parent, Map<String, Channel2DChart<?>> allCharts,  Map<String, List<String>> allDefaultNames, Map<String, List<File>> allDefaultLocations)
    {
        ChartSaver saver = currentFormatType.getChartSaver();

        String fullArchiveName = getFullArchiveName();
        Map<String, List<String>> allNamesMap = getNames(allDefaultNames);

        List<File> allPathsList = new ArrayList<>();

        List<StreamSavable> savables = new ArrayList<>();

        for(String key: allCharts.keySet())
        {
            List<String> names = allNamesMap.get(key);
            List<File> paths = allDefaultLocations.get(key);

            allPathsList.addAll(paths);            

            Channel2DChart<?> chart = allCharts.get(key);

            int frameToSaveCount = (lastFrame - firstFrame + 1);

            for(int i = 0; i<frameToSaveCount; i++)
            {
                int frame = i + firstFrame - 1;
                StreamSavable pack = new MovieStreamSavablePack(chart, frame, names.get(i), saver);
                savables.add(pack);
            }       
        }

        File path;
        if(directory == null)
        {
            path = new File(IOUtilities.findLastCommonDirectory(allPathsList),fullArchiveName);
        }
        else
        {
            path = new File(directory, fullArchiveName);
        }

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

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        if(name.equals(FORMAT_PARAMETERS_PROVIDED))
        {
            checkIfNecessaryInputProvided();
        }

        firePropertyChange(evt);
    }
}
