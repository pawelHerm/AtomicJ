
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

import static atomicJ.gui.PreferenceKeys.INITIAL_PATH;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.filechooser.FileSystemView;

public class RootedFileChooser extends SafeFileChooser
{
    private static final String VIEW_TYPE_DETAILS = "viewTypeDetails";
    private static final String VIEW_TYPE_LIST = "viewTypeList";
    private static final long serialVersionUID = 1L;

    private final Preferences pref;

    public RootedFileChooser(Preferences directoryPreferences)
    {
        super();
        this.pref = directoryPreferences;
        useCurrentDirectoryFromPreferences();
    }

    public RootedFileChooser(FileSystemView fsv, Preferences directoryPreferences)
    {
        super(fsv);
        this.pref = directoryPreferences;
        useCurrentDirectoryFromPreferences();
    }

    protected Preferences getDirectoryPreferences()
    {
        return pref;
    }

    private void useCurrentDirectoryFromPreferences()
    {
        String currentDirPath = pref.get(INITIAL_PATH, System.getProperty("user.dir"));
        File currentDir = new File(currentDirPath);
        super.setCurrentDirectory(currentDir);
    }

    public File getCurrentDirectoryFromPreferences()
    {
        String currentDirPath = pref.get(INITIAL_PATH, System.getProperty("user.dir"));
        File currentDir = new File(currentDirPath);
        return currentDir;
    }

    //https://stackoverflow.com/questions/16292502/how-can-i-start-the-jfilechooser-in-the-details-view
    public void setToDetailsView() {
        Action viewTypeDetailsAction = getActionMap().get(VIEW_TYPE_DETAILS);
        if(viewTypeDetailsAction != null)
        {
            viewTypeDetailsAction.actionPerformed(null);
        }
    }

    public void setToListView() {
        Action viewTypeListAction = getActionMap().get(VIEW_TYPE_LIST);
        if(viewTypeListAction != null)
        {
            viewTypeListAction.actionPerformed(null);
        }
    }

    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException
    {
        useCurrentDirectoryFromPreferences();
        return super.showDialog(parent, approveButtonText);
    }

    @Override
    public void setCurrentDirectory(File dirNew)
    {
        File dirOld = getCurrentDirectory();
        super.setCurrentDirectory(dirNew);

        if(dirNew != null && !dirNew.equals(dirOld))
        {
            String path = dirNew.getAbsolutePath();
            pref.put(INITIAL_PATH, path);           
        }
    }	
}