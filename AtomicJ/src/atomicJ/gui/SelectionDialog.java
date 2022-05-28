
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
import java.awt.event.*;
import java.util.List;
import javax.swing.*;


public class SelectionDialog <E> extends JDialog
{
    private static final long serialVersionUID = 1L;

    private final ItemList<E> fileList = new ItemList<>();

    public SelectionDialog(Window parent)
    {
        super(parent,"Active file selection", ModalityType.APPLICATION_MODAL);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        fileList.setLayoutOrientation(JList.VERTICAL);

        JScrollPane scrollPane  = new JScrollPane(fileList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel scrollPanePanel = new JPanel(new BorderLayout());
        scrollPanePanel.add(scrollPane,BorderLayout.CENTER);
        scrollPanePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton buttonOK = new JButton(new OKAction());
        JButton buttonCancel = new JButton(new CancelAction());
        JPanel outerButtonPanel = new JPanel();
        JPanel innerButtonPanel = new JPanel(new GridLayout(1,0,5,5));
        innerButtonPanel.add(buttonOK);
        innerButtonPanel.add(buttonCancel);
        outerButtonPanel.add(innerButtonPanel);

        content.add(outerButtonPanel,BorderLayout.SOUTH);
        content.add(scrollPanePanel,BorderLayout.CENTER);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                fileList.setSelectedIndex(-1);
            }
        });

        pack();
        setLocationRelativeTo(parent);
    }

    public int getSelectedIndex()
    {
        return fileList.getSelectedIndex();
    }

    public E getSelectedSource()
    {
        return fileList.getSelectedValue();
    }

    public void setListElements(List<E> elements)
    {
        fileList.setItems(elements);	
        validate();
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            fileList.setSelectedIndex(-1);
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
            setVisible(false);
        };
    }
}
