
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorChooserDialog extends JDialog implements ChangeListener
{
    private static final long serialVersionUID = 1L;

    public static final String SELECTED_COLOR = "SELECTED_COLOR";

    private Color initialColor;
    private Color selectedColor;

    private boolean approved = true;

    private final JColorChooser chooser = new JColorChooser();

    private final JButton buttonOk = new JButton(new OKAction());
    private final JButton buttonCancel = new JButton(new CancelAction());
    private final JButton buttonReset = new JButton(new ResetAction());

    public ColorChooserDialog(Window parent, String title)
    {
        super(parent, title, ModalityType.APPLICATION_MODAL);

        JPanel panelButtons = buildButtonPanel();
        add(chooser, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.SOUTH);

        initChangeListener();

        pack();
        setLocationRelativeTo(parent);
    }

    public boolean showDialog(Color initialColor)
    {
        this.initialColor = initialColor;
        this.selectedColor = initialColor;
        chooser.setColor(initialColor);

        this.approved = true;
        setVisible(true);
        return approved;
    }

    private void initChangeListener()
    {
        chooser.getSelectionModel().addChangeListener(this);
    }

    public Color getSelectedColor()
    {
        return chooser.getSelectionModel().getSelectedColor();
    }

    public Color getInitialColor()
    {
        return initialColor;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOk).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonCancel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOk)
                .addComponent(buttonCancel)
                .addComponent(buttonReset)
                );

        layout.linkSize(buttonOk,buttonCancel, buttonReset);		
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        return buttonPanel;
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
        }
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
            approved = false;
            setVisible(false);
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
            chooser.setColor(initialColor);
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Color selectedColorNew = chooser.getSelectionModel().getSelectedColor();
        Color selectedColorOld = selectedColor;
        this.selectedColor = selectedColorNew;

        firePropertyChange(SELECTED_COLOR, selectedColorOld, selectedColorNew);
    }
}
