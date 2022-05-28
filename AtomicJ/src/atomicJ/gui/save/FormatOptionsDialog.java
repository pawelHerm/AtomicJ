
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

package atomicJ.gui.save;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class FormatOptionsDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    public FormatOptionsDialog(JDialog parent, SimpleSaveModel<? extends SaveFormatType> model)
    {
        super(parent, "", true);
        List<? extends SaveFormatType> formatTypes = model.getFormatTypes();

        for(SaveFormatType formatType: formatTypes)
        {
            JPanel inputPanel = formatType.getParametersInputPanel();
            cardPanel.add(inputPanel, formatType.toString());
        }

        JPanel buttonPanel = buildPanelButtons();
        add(cardPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(parent);
    }

    public void showInputPanel(SaveFormatType type)
    {
        setTitle("Options for " + type.getDescription());
        cardLayout.show(cardPanel, type.toString());
        pack();
    }

    private JPanel buildPanelButtons()
    {
        JPanel buttonPanel = new JPanel();
        JButton buttonClose = new JButton(new CloseAction());
        buttonPanel.add(buttonClose);
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
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
