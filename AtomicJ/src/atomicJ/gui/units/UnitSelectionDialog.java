
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

package atomicJ.gui.units;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class UnitSelectionDialog extends JDialog 
{
    private static final long serialVersionUID = 1L;

    private final JButton buttonReset = new JButton(new ResetAction());
    private final JButton buttonClose = new JButton(new CloseAction());

    private final UnitSelectionPanel selectionPanel;

    public UnitSelectionDialog(JDialog parent, UnitSelectionPanel selectionPanel)
    {
        super(parent, "Select units", false);

        this.selectionPanel = selectionPanel;
        JPanel buttonPanel = buildButtonPanel();
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(selectionPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void reset()
    {		
        selectionPanel.reset();
    }

    private void close()
    {		
        setVisible(false);
    }

    private JPanel buildButtonPanel()
    {
        JPanel outerPanel = new JPanel();
        JPanel innerPanel = new JPanel(new GridLayout(1,0,5,5));
        innerPanel.add(buttonReset);
        innerPanel.add(buttonClose);

        innerPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        outerPanel.add(innerPanel);
        outerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        return outerPanel;
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
            reset();
        }
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
            close();
        }
    }
}
