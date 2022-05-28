
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
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

public class SafeFileChooser extends JFileChooser
{
    private static final long serialVersionUID = 1L;

    public SafeFileChooser()
    {
        super();
    }

    public SafeFileChooser(File currentDirectory)
    {
        super(currentDirectory);
    }

    public SafeFileChooser(File currentDirectory, FileSystemView fsv)
    {
        super(currentDirectory, fsv);
    }

    public SafeFileChooser(FileSystemView fsv)
    {
        super(fsv);
    }

    public SafeFileChooser(String currentDirectoryPath)
    {
        super(currentDirectoryPath);
    }

    public SafeFileChooser(String currentDirectoryPath, FileSystemView fsv)
    {
        super(currentDirectoryPath,fsv);
    }

    public void addChoosableFileFilters(Collection<? extends FileFilter> filters)
    {
        for(FileFilter filter : filters)
        {
            addChoosableFileFilter(filter);
        }
    }

    @Override
    public void approveSelection()
    {
        File f = getSelectedFile();

        if(f.exists() && getDialogType() == SAVE_DIALOG)
        {
            int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", AtomicJ.APPLICATION_NAME, JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result)
            {
            case JOptionPane.YES_OPTION:
                super.approveSelection();
                return;
            case JOptionPane.NO_OPTION:
                return;
            case JOptionPane.CANCEL_OPTION:
                cancelSelection();
                return;
            }
        }

        super.approveSelection();
    }
}
