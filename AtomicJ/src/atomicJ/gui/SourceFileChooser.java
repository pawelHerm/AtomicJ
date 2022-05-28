
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

package atomicJ.gui;

import java.io.File;
import javax.swing.*;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;

import javax.swing.filechooser.FileFilter;

import atomicJ.data.ChannelFilter;
import atomicJ.readers.ConcurrentReadingTask;
import atomicJ.readers.ReaderFileFilter;
import atomicJ.readers.SourceReader;
import atomicJ.readers.SourceReadingModel;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.IOUtilities;

import java.util.List;
import java.util.prefs.Preferences;


public class SourceFileChooser <E extends ChannelSource> extends RootedFileChooser
{   
    private static final long serialVersionUID = 1L;
    private static final Preferences DEFAULT_DIRECTORY_PREFERNCES = Preferences.userRoot().node("SourceFileChooser");

    private final SourceReadingModel<E> model;
    private final Preferences fileFilterPreferences;

    public SourceFileChooser(SourceReadingModel<E> model, Preferences fileFilterPreferences)
    {
        this(model, fileFilterPreferences, DEFAULT_DIRECTORY_PREFERNCES, true, JFileChooser.FILES_AND_DIRECTORIES);
    }

    public SourceFileChooser(SourceReadingModel<E> model,Preferences fileFilterPreferences, Preferences directoryPreferences)
    {
        this(model, fileFilterPreferences,directoryPreferences, true, JFileChooser.FILES_AND_DIRECTORIES);
    }

    public SourceFileChooser(SourceReadingModel<E> model, Preferences fileFilterPreferences, boolean multiSelectionEnabled, int fileSelectionMode)
    {
        this(model, fileFilterPreferences, DEFAULT_DIRECTORY_PREFERNCES, multiSelectionEnabled, fileSelectionMode);
    }

    public SourceFileChooser(SourceReadingModel<E> model, Preferences fileFilterPreferences, Preferences directoryPreferences, boolean multiSelectionEnabled, int fileSelectionMode)
    {
        super(directoryPreferences);

        this.fileFilterPreferences = fileFilterPreferences;
        this.model = model;

        setMultiSelectionEnabled(multiSelectionEnabled);
        setFileSelectionMode(fileSelectionMode);
        setApproveButtonMnemonic(KeyEvent.VK_O);
        setAcceptAllFileFilterUsed(false);

        List<FileFilter> filters = model.getExtensionFilters();
        addChoosableFileFilters(filters);

        useCurrentFilterFromPreferences();
    }

    private void useCurrentFilterFromPreferences()
    {
        String currentFilter = fileFilterPreferences.get(PreferenceKeys.FILE_FILTER, "");
        FileFilter filter = model.getExtensionFilter(currentFilter);
        setFileFilter(filter);
    }

    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException
    {
        useCurrentFilterFromPreferences();
        return super.showDialog(parent, approveButtonText);
    }

    public void setTypeOfData(ChannelFilter dataTypeFilter)
    {
        this.model.setDataFilter(dataTypeFilter);
    }

    @Override
    public File[] getSelectedFiles()
    {
        if(isMultiSelectionEnabled())
        {
            return super.getSelectedFiles();
        }

        File[] selectedFiles = (getSelectedFile() != null) ? new File[] {getSelectedFile()} : new File[] {};
        return selectedFiles;
    }

    public ConcurrentReadingTask<E> chooseSources(Component parent)
    {
        ConcurrentReadingTask<E> readingTask = null;

        int op = showDialog(parent, "Open");

        if(op == JFileChooser.APPROVE_OPTION)
        {
            File[] selectedFiles = getSelectedFiles();

            FileFilter extFilter = getFileFilter();
            SourceReader<E> reader = model.getSourceReader(extFilter);
            ChannelFilter channelFilter = model.getDataFilter();

            List<File> files = IOUtilities.findAcceptableChildrenFiles(selectedFiles, new ReaderFileFilter(reader));

            readingTask = new ConcurrentReadingTask<>(files, parent, reader, channelFilter);

            String currentFilter = getFileFilter().getDescription();
            fileFilterPreferences.put(PreferenceKeys.FILE_FILTER, currentFilter);           
        }

        return readingTask;
    }
}

