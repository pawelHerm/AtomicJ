
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

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TextFileChooser extends SafeFileChooser
{	
    private static final long serialVersionUID = 1L;

    private static final Preferences FILE_FILTER_PREFERENCES = Preferences.userRoot().node("TextFileChooser");

    public static final String TSV_EXTENSION = "tsv";
    public static final String CSV_EXTENSION = "csv";

    private static final String TSV_FILTER_DESCRIPTION = "Tab separated values file (.tsv)";
    private static final String CSV_FILTER_DESCRIPTION = "Comma separated values file (.csv)";

    private final Map<String, FileNameExtensionFilter> filters = new HashMap<>();

    public TextFileChooser()
    {        
        setFileSelectionMode(JFileChooser.FILES_ONLY);	
        setApproveButtonMnemonic(KeyEvent.VK_S);
        setMultiSelectionEnabled(false);	
        setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter tsvFilter = new FileNameExtensionFilter(TSV_FILTER_DESCRIPTION, TSV_EXTENSION);
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(CSV_FILTER_DESCRIPTION, CSV_EXTENSION);
        filters.put(TSV_FILTER_DESCRIPTION, tsvFilter);
        filters.put(CSV_FILTER_DESCRIPTION, csvFilter);

        addChoosableFileFilters(filters.values());

        useCurrentFilterFromPreferences();
    }

    private void useCurrentFilterFromPreferences()
    {
        String currentFilter = FILE_FILTER_PREFERENCES.get(PreferenceKeys.FILE_FILTER, TSV_FILTER_DESCRIPTION);        
        FileNameExtensionFilter filter = filters.containsKey(currentFilter) ? filters.get(currentFilter): filters.get(TSV_FILTER_DESCRIPTION);
        setFileFilter(filter);
    }

    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException
    {
        useCurrentFilterFromPreferences();
        return super.showDialog(parent, approveButtonText);
    }

    public String getSelectedExtension()
    {
        String[] selectedExtensions = getSelectedExtensions();
        String firstExt = selectedExtensions != null & selectedExtensions.length > 0 ? selectedExtensions[0] : "";
        return firstExt;
    }

    public String[] getSelectedExtensions()
    {
        FileNameExtensionFilter filter = (FileNameExtensionFilter)getFileFilter();
        String[] selectedExtensions = (filter != null) ? filter.getExtensions() : new String[] {};

        return selectedExtensions;
    }

    @Override
    public File getSelectedFile()
    {
        File file = super.getSelectedFile();

        if(file != null)
        {
            int n = file.getName().lastIndexOf(".");
            String selectedExt = getSelectedExtension();

            file = (n < 0 && selectedExt.length() > 0) ? new  File(file.getParent(), file.getName() + "." + selectedExt) : file;
        }

        return file;
    }

    @Override
    public void approveSelection()
    {
        String currentFilter = getFileFilter().getDescription();
        FILE_FILTER_PREFERENCES.put(PreferenceKeys.FILE_FILTER, currentFilter); 

        super.approveSelection();
    }
}

