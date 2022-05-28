
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

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;
import javax.swing.*;

import atomicJ.gui.results.ResultTable;

import static atomicJ.gui.PreferenceKeys.*;

public class CustomizeResultTableDialog extends NumericalFormatDialog
{
    private boolean initUseLongName;

    private final JRadioButton buttonShortName = new JRadioButton();
    private final JRadioButton buttonLongName = new JRadioButton();
    private final ButtonGroup buttonGroupNames = new ButtonGroup();
    private final ResultTable<?,?,?> table;
    private final Preferences pref;

    public CustomizeResultTableDialog(JDialog parent, ResultTable<?,?,?> table, Preferences pref)
    {
        super(parent, table, "Customize result table");
        this.table = table;
        this.pref = pref;

        modifyMainPanel();	
        pullPreferences();

        initItemListener();

        packAndSetLocation();
    }

    private void pullPreferences()
    {
        initUseLongName = pref.getBoolean(SOURCE_NAME_LONG, true);
        JRadioButton buttonToSelect = initUseLongName ? buttonLongName : buttonShortName;

        buttonToSelect.setSelected(true);
    }

    private void initItemListener()
    {
        buttonLongName.addItemListener(new ItemListener() 
        {
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                table.setUseLongSourceName(selected);
                pref.putBoolean(SOURCE_NAME_LONG, selected);               
            }
        });
    }

    private SubPanel modifyMainPanel()
    {
        SubPanel mainPanel = getMainPanel();

        buttonGroupNames.add(buttonShortName);
        buttonGroupNames.add(buttonLongName);			

        JLabel labelSourceName = new JLabel("Source name: ");

        JLabel labelShort = new JLabel("Short");       
        labelShort.setDisplayedMnemonic(KeyEvent.VK_S);
        buttonShortName.setMnemonic(KeyEvent.VK_S);

        JLabel labelLong = new JLabel("Long");
        labelLong.setDisplayedMnemonic(KeyEvent.VK_L);
        buttonLongName.setMnemonic(KeyEvent.VK_L);

        JPanel panelRadioButtons = new JPanel();
        panelRadioButtons.add(labelShort);
        panelRadioButtons.add(buttonShortName);
        panelRadioButtons.add(labelLong);
        panelRadioButtons.add(buttonLongName);

        mainPanel.addComponent(labelSourceName, 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(panelRadioButtons, 1, 4, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        return mainPanel;
    }



    @Override
    protected void doReset()
    {
        super.doReset();

        buttonGroupNames.clearSelection();
        buttonGroupNames.setSelected(buttonLongName.getModel(), initUseLongName);
        table.setUseLongSourceName(initUseLongName);
        pref.putBoolean(SOURCE_NAME_LONG, initUseLongName);					
    }
}

