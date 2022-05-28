
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Objects;

import javax.swing.filechooser.FileNameExtensionFilter;


import atomicJ.gui.AbstractModel;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.Validation;

public abstract class SimpleSaveModel<E extends SaveFormatType> extends AbstractModel implements PropertyChangeListener
{
    private static final String ZIP_EXTENSION = "zip";

    private File outputFile;

    private E currentFormatType;

    private final List<E> formatTypes;
    private boolean formatParametersSpecified;
    private boolean inputSpecified;

    //ZIPPABLE

    private final FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("ZIP archive", ZIP_EXTENSION);
    private boolean saveInArchiveEnabled;
    private boolean saveInArchive;

    private FileNameExtensionFilter currentFileFilter;

    public SimpleSaveModel(List<E> formatTypes)
    {
        this.formatTypes = Validation.requireNonNullAndNonEmpty(formatTypes, "formatTypes cannot be null", "formatTypes cannot be empty");
        initDefaults();
        checkIfSaveInArchiveEnabled();
        checkIfNecessaryInputProvided();
    }

    private void initDefaults()
    {
        this.outputFile = null;
        this.currentFormatType = formatTypes.get(0);
        this.formatParametersSpecified = currentFormatType.isNecessaryIputProvided();
        currentFormatType.addPropertyChangeListener(this);

        updateFileFilter();
    }

    public boolean isNecessaryInputProvided()
    {
        return inputSpecified;
    }

    public List<E> getFormatTypes()
    {
        return formatTypes;
    }

    public File getOutputFile()
    {
        return outputFile;
    }

    public void setFilePath(File filePathNew)
    {
        if(filePathNew != null && filePathNew.isDirectory())
        {
            throw new IllegalArgumentException("File object passed as the 'file' argument should not be a directory");
        }

        File filePathOld = this.outputFile;
        this.outputFile = filePathNew;

        firePropertyChange(FILE_PATH, filePathOld, filePathNew);

        checkIfNecessaryInputProvided();
    }

    public E getSaveFormat()
    {
        return currentFormatType;
    }


    private E getFormatType(String description)
    {
        E formatType = null;

        if(description == null)
        {
            return formatType;
        }

        for(E type : formatTypes)
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

    public void setSaveFormat(String descriptionSaveFormatNew)
    {
        E saveFormatNew = getFormatType(descriptionSaveFormatNew);
        if(saveFormatNew != null)
        {
            setSaveFormat(saveFormatNew);
        }
    }

    public void setSaveFormat(E saveFormatNew)
    {
        Validation.requireNonNullParameterName(saveFormatNew, "saveFormatNew");

        E saveFormatOld = currentFormatType;
        this.currentFormatType = saveFormatNew;

        saveFormatOld.removePropertyChangeListener(this);
        saveFormatNew.addPropertyChangeListener(this);

        firePropertyChange(SAVE_FORMAT, saveFormatOld, saveFormatNew);

        checkIfSaveInArchiveEnabled();
        checkPathExtensionCorrectness();
        checkIfNecessaryInputProvided();
        updateFileFilter();
    }

    private void checkPathExtensionCorrectness()
    {
        String ext = saveInArchive ?  ZIP_EXTENSION : currentFormatType.getExtension();
        if(outputFile != null)
        {
            String[] expectedExtensions =  saveInArchive ? new String[] {ZIP_EXTENSION} : currentFormatType.getFileNameExtensionFilter().getExtensions();
            File correctExtFile = ExtensionFileChooser.ensureCorrectExtension(outputFile, expectedExtensions, ext);
            setFilePath(correctExtFile);
        }
    }

    private void checkIfNecessaryInputProvided()
    {
        boolean inputSpecifiedNew = checkIfFormatParametersProvided() && checkIfFilePathProvded();		
        boolean inputSpecifiedOld = inputSpecified;
        this.inputSpecified = inputSpecifiedNew;

        firePropertyChange(INPUT_PROVIDED, inputSpecifiedOld, inputSpecifiedNew);
    }

    private boolean checkIfFilePathProvded()
    {
        boolean filePathProvided = outputFile != null;

        return filePathProvided;
    }


    private boolean checkIfFormatParametersProvided()
    {
        boolean formatParametersSpecifiedNew = currentFormatType.isNecessaryIputProvided();		
        this.formatParametersSpecified = formatParametersSpecifiedNew;
        return formatParametersSpecified;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String poperty = evt.getPropertyName();
        if(FORMAT_PARAMETERS_PROVIDED.equals(poperty))
        {
            checkIfNecessaryInputProvided();
        }

        firePropertyChange(evt);
    }

    public boolean isSavePathFree()
    {
        boolean free = outputFile == null || !outputFile.exists();

        return free;
    }

    public abstract void save()  throws UserCommunicableException;

    public boolean isSaveInArchiveEnabled()
    {
        return saveInArchiveEnabled;
    }

    private void checkIfSaveInArchiveEnabled()
    {
        boolean saveInArchiveEnabledOld = this.saveInArchiveEnabled;

        boolean saveInArchiveEnabledNew = currentFormatType.supportsArchives();

        this.saveInArchiveEnabled = saveInArchiveEnabledNew;

        firePropertyChange(SAVE_IN_ARCHIVE_ENABLED, saveInArchiveEnabledOld, saveInArchiveEnabledNew);

        if(!this.saveInArchiveEnabled && saveInArchive)
        {
            setSaveInArchive(false);
        }
    }

    public boolean isSaveInArchive()
    {
        return saveInArchive;
    }

    public void setSaveInArchive(boolean saveInArchiveNew)
    {
        boolean saveInArchiveOld = saveInArchive;
        this.saveInArchive = saveInArchiveNew;

        firePropertyChange(SAVE_IN_ARCHIVE, saveInArchiveOld, saveInArchive);

        checkPathExtensionCorrectness();
        updateFileFilter();
    }

    public FileNameExtensionFilter getFileFilter()
    {
        return currentFileFilter;
    }

    private void updateFileFilter()
    {
        FileNameExtensionFilter fileFilterNew = saveInArchive ? zipFilter : currentFormatType.getFileNameExtensionFilter();

        if(!Objects.equals(fileFilterNew, this.currentFileFilter))
        {
            FileNameExtensionFilter fileFilterOld = this.currentFileFilter;
            this.currentFileFilter = fileFilterNew;

            firePropertyChange(FILE_FILTER, fileFilterOld, fileFilterNew);
        }
    }
}
