
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class SimpleDialog extends JDialog
{
    private static final long serialVersionUID = 1L;


    public SimpleDialog(Window parent, String title, ModalityType modalityType)
    {
        super(parent, title, modalityType);
        JPanel buttonPanel = buildButtonPanel();
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel buildButtonPanel()
    {	
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 5, 5));	
        JPanel buttonPanelOuter = new JPanel();

        JButton buttonClose = new JButton(new CloseAction());

        buttonPanel.add(buttonClose);

        buttonPanelOuter.add(buttonPanel);
        buttonPanelOuter.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanelOuter;
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
            setVisible(false);
        };
    }

}
