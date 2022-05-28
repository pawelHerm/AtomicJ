
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

import javax.swing.*;

import atomicJ.analysis.SpectroscopyPreferencesModel;
import atomicJ.analysis.SpectroscopyProcessingOrigin;
import atomicJ.analysis.SpectroscopyResultDestination;

import java.awt.*;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AtomicJ
{   
    public static final String APPLICATION_NAME = "AtomicJ";
    public static final String APPLICATION_VERSION = "2.4.0";
    public static final String MANUAL_FILE_NAME = "AtomicJ_Users_Manual.pdf";
    public static final String CONTACT_MAIL = "pawel.hermanowicz@uj.edu.pl";

    public static final String COPYRRIGHT_NOTICE = "Copyright 2013 - 2022 by Pawel Hermanowicz \n\nThis program is free software; you can redistribute it and/or modify " +
            "it under the terms of the GNU General Public License as published by " +
            "the Free Software Foundation version 2 of the License. "+
            "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without " +
            "even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU " +
            "General Public License for more details."+
            "You should have received a copy of the GNU General Public License along with this program; if not, " +
            "see http://www.gnu.org/licenses/";

    private static MainView CURRENT_FRAME;

    @SuppressWarnings("unused")
    private static byte[] LAST_RESORT = new byte[256*1024];

    private static final Logger ROOT_LOGGER = Logger.getLogger("");
    private static final String LOGGING_FILE_NAME = "atomicJ.log";

    public static void main(String[] args)
    {
        initializeLogger();
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {

                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    Locale.setDefault(Locale.US);			
                }
                catch(OutOfMemoryError oome)
                {
                    handleOOME(oome);
                }
                catch(Exception e) 
                {
                    ROOT_LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    e.printStackTrace();
                }

                CURRENT_FRAME = new MainView();
                CURRENT_FRAME.setVisible(true);
            }
        });	
    }

    private static void initializeLogger()
    {
        try {
            FileHandler fh = new FileHandler(LOGGING_FILE_NAME);
            ROOT_LOGGER.addHandler(fh);
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    public static SpectroscopyResultDestination getResultDestination()
    {
        return CURRENT_FRAME;
    }

    public static SpectroscopyProcessingOrigin getProcessingOrigin()
    {
        return CURRENT_FRAME;
    }

    public static SpectroscopyPreferencesModel getPreferencesModel()
    {
        return CURRENT_FRAME;
    }

    public static JFrame getApplicationFrame()
    {
        return CURRENT_FRAME.getPublicationSite();
    }

    public static void handleOOME(OutOfMemoryError oome)
    {
        LAST_RESORT = null;

        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(CURRENT_FRAME.getPublicationSite(), "AtomicJ has run out of memory. The application will probably not work properly now. Do you want to close it?","Out of memory error", JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,
                null,  options, options[0]); 

        if(n == 0)
        {
            System.exit(-1);
        }
    }
}

