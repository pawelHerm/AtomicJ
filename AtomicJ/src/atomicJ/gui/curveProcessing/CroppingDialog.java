
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

package atomicJ.gui.curveProcessing;

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_X;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_Y;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;
import javax.swing.*;

import atomicJ.data.Data1D;
import atomicJ.gui.Channel1DPlot.Channel1DPlotFactory;
import atomicJ.gui.curveProcessing.CroppingReceiver.DummyCroppingReceiver;
import atomicJ.utilities.GUIUtilities;;


public class CroppingDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);

    private static final Preferences PREF = Preferences.userRoot().node(CroppingDialog.class.getName());

    private final CroppingPanel croppingPanel  = new CroppingPanel();

    private CroppingReceiver croppingModel = DummyCroppingReceiver.getInstance();

    public CroppingDialog(Window parentDialog)
    {
        super(parentDialog, "Specify cropping", ModalityType.APPLICATION_MODAL);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        JPanel buttonPanel = buildButtonPanel();

        croppingPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        content.add(croppingPanel, BorderLayout.CENTER);		
        content.add(buttonPanel,BorderLayout.SOUTH);

        int height = PREF.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width = PREF.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        int locationX = PREF.getInt(WINDOW_LOCATION_X, 0);
        int locationY = PREF.getInt(WINDOW_LOCATION_Y, 0);

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        {
            setSize(width,height);
            setLocation(locationX,locationY);
        }
        else
        {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLocation(0, 0);
        }

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {
                PREF.putInt(WINDOW_HEIGHT, getHeight());
                PREF.putInt(WINDOW_WIDTH, getWidth());
                PREF.putInt(WINDOW_LOCATION_X, (int) getLocation().getX());			
                PREF.putInt(WINDOW_LOCATION_Y, (int) getLocation().getY());
            }
        });
    }	

    public <E extends Data1D> void showDialog(Channel1DPlotFactory plotFactory, Dataset1DCroppingModel<E> datasetCroppingModel, CroppingReceiver model)
    {
        this.croppingModel = model;
        CroppingChart chart = new CroppingChart(plotFactory, datasetCroppingModel);
        croppingPanel.setSelectedChart(chart);
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();
        JButton buttonOK = new JButton(new OKAction());
        JButton buttonCancel = new JButton(new CancelAction());;			
        JPanel innerPanel = new JPanel(new GridLayout(1, 0, 10, 10));

        innerPanel.add(buttonOK);
        innerPanel.add(buttonCancel);		
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        buttonPanel.add(innerPanel);		
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        return buttonPanel;
    }

    private class CancelAction extends AbstractAction
    {

        private static final long serialVersionUID = 1L;
        public CancelAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        };
    }	

    private class OKAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OKAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            croppingModel.setLeftCropping(croppingPanel.getLeftCropping());
            croppingModel.setRightCropping(croppingPanel.getRightCropping());
            croppingModel.setLowerCropping(croppingPanel.getLowerCropping());
            croppingModel.setUpperCropping(croppingPanel.getUpperCropping());

            setVisible(false);
        };		
    }
}

