
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
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;
import javax.swing.*;

import atomicJ.gui.StandardNumericalFormatStyle.FormattableNumericalDataState;

public class NumericalFormatDialog
{
    private final NumericFormatSelectionPanel formatSelectionPanel;

    private FormattableNumericalDataState memento;

    private final JDialog viewDialog;

    public NumericalFormatDialog(JDialog parent, String name)
    {       
        this(parent, new StandardNumericalFormatStyle(), name);
    }

    public NumericalFormatDialog(JDialog parent, NumericalFormatStyle data, String name)
    {
        this.viewDialog = new JDialog(parent,name,false);
        this.formatSelectionPanel = new NumericFormatSelectionPanel(data);
        this.memento = formatSelectionPanel.getStateMemento();

        JPanel buttonPanel = buildButtonPanel();

        viewDialog.add(formatSelectionPanel, BorderLayout.CENTER);
        viewDialog.add(buttonPanel, BorderLayout.SOUTH);

        initComponentListener();
        viewDialog.pack();
        viewDialog.setLocationRelativeTo(parent);

        formatSelectionPanel.setBorder(BorderFactory.createEmptyBorder(10,5,7,5));
    }

    protected void packAndSetLocation() 
    {
        viewDialog.pack();
        viewDialog.setLocationRelativeTo(viewDialog.getOwner());        
    }

    public boolean isVisible()
    {
        boolean visible = viewDialog.isVisible();
        return visible;
    }

    public void setVisible(boolean visible)
    {
        viewDialog.setVisible(visible);
    }

    private void initComponentListener()
    {
        viewDialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent evt)
            {
                formatSelectionPanel.saveToPreferences();
            }
        });
    }

    public void setFormattableData(NumericalFormatStyle data, Preferences preferences)
    {
        formatSelectionPanel.setFormattableData(data);
        this.memento = formatSelectionPanel.getStateMemento();
    }

    protected void doReset()
    {
        formatSelectionPanel.reset(memento);    
    }

    private JPanel buildButtonPanel()
    {
        JButton buttonReset = new JButton(new ResetAction());
        JButton buttonClose = new JButton(new CloseAction());

        JPanel outerPanel = new JPanel();
        JPanel innerPanel = new JPanel(new GridLayout(1,0,5,5));
        innerPanel.add(buttonReset);
        innerPanel.add(buttonClose);

        innerPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        outerPanel.add(innerPanel);
        outerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        return outerPanel;
    }

    protected SubPanel getMainPanel()
    {
        return formatSelectionPanel;
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            viewDialog.setVisible(false);
        }
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            doReset();
        }
    }
}

