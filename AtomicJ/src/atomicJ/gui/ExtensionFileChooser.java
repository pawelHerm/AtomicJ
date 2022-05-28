
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
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ExtensionFileChooser extends RootedFileChooser
{	
    private static final long serialVersionUID = 1L;
    private boolean enforce;
    private String ext;

    public ExtensionFileChooser(Preferences directoryPreferences, boolean enforce)
    {
        super(directoryPreferences);
        this.ext = "";
        this.enforce = enforce;
        setFileSelectionMode(JFileChooser.FILES_ONLY);	
        setMultiSelectionEnabled(false);	
    }

    public ExtensionFileChooser(Preferences directoryPreferences, String name, String ext, boolean enforce)
    {
        super(directoryPreferences);
        this.ext = ext;
        setFileSelectionMode(JFileChooser.FILES_ONLY);	
        setMultiSelectionEnabled(false);

        this.enforce = enforce;	
        setFileFilter(new FileNameExtensionFilter(name, ext));
    }

    public boolean isEnforceExtension()
    {
        return enforce;
    }

    public void setEnforceExtension(boolean enforce)
    {
        this.enforce = enforce;
    }

    public String getEnforcedExtension()
    {
        return ext;
    }

    public void setEnforcedExtension(String name, String ext)
    {
        this.ext = ext;
        setFileFilter(new FileNameExtensionFilter(name, ext));
    }

    public void setEnforcedExtension(FileNameExtensionFilter filter)
    {
        this.ext = filter.getExtensions()[0];
        setFileFilter(filter);
    }

    @Override
    public File getSelectedFile()
    {
        File file = super.getSelectedFile();
        if(enforce && (file != null))
        {
            int n = file.getName().lastIndexOf(".");
            if(n == -1)
            {
                file = new File(file.getParent(), file.getName() + "." + ext);
            }
        }
        return file;
    }

    public static File ensureCorrectExtension(File file, String[] acceptedExts, String ext)
    {
        String fileName = file.getName();
        int n = fileName.lastIndexOf(".");

        if(n == -1)
        {
            file = new File(file.getParent(), fileName + "." + ext);
        }
        else
        {
            String oldExt = fileName.substring(n + 1);

            boolean accepted = false;
            for(String acceptedExt : acceptedExts)
            {
                if(acceptedExt.equals(oldExt))
                {
                    accepted = true;
                    break;
                }
            }

            if(!accepted)
            {
                String newFileName = fileName.substring(0, n + 1) + ext;
                file = new  File(file.getParent(), newFileName);
            }
        }

        return file;
    }
}