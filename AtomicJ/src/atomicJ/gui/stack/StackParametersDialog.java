
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

package atomicJ.gui.stack;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.data.units.Quantity;
import atomicJ.gui.SubPanel;


public class StackParametersDialog extends JDialog implements ChangeListener
{
    private static final long serialVersionUID = 1L;

    private final JCheckBox boxFixContactPoint = new JCheckBox("Fix contact");

    private final JButton buttonOK = new JButton(new OKAction());
    private final JButton buttonCancel = new JButton(new CancelAction());

    private final JSpinner spinnerMinimum = new JSpinner(new SpinnerNumberModel(0, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.));
    private final JSpinner spinnerMaximum = new JSpinner(new SpinnerNumberModel(1, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.));
    private final JSpinner spinnerFrameCount = new JSpinner(new SpinnerNumberModel(100, 0, Integer.MAX_VALUE, 1));

    private final Quantity quantity;

    private boolean approved = false;

    public StackParametersDialog(Window parent, String title, Quantity quantity)
    {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        this.quantity = quantity;
        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initChangeListener()
    {
        spinnerMinimum.addChangeListener(this);
        spinnerMaximum.addChangeListener(this);
        spinnerFrameCount.addChangeListener(this);
    }	

    //I dont really know why it is necessary to remove and add changeListeners, but it is necessary, otherwise
    //spinners would not increment 
    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == spinnerMinimum)
        {
            double minimum = ((SpinnerNumberModel)spinnerMinimum.getModel()).getNumber().doubleValue();

            spinnerMaximum.removeChangeListener(this);
            ((SpinnerNumberModel)spinnerMaximum.getModel()).setMinimum(minimum);
            spinnerMaximum.addChangeListener(this);
        }
        else if(source == spinnerMaximum)
        {
            double maximum = ((SpinnerNumberModel)spinnerMaximum.getModel()).getNumber().doubleValue();

            spinnerMinimum.removeChangeListener(this);
            ((SpinnerNumberModel)spinnerMinimum.getModel()).setMaximum(maximum);
            spinnerMinimum.addChangeListener(this);
        }
    }	

    public boolean showDialog()
    {
        approved = false;

        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
        return approved;
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();    

        boxFixContactPoint.setSelected(true);

        mainPanel.addComponent(boxFixContactPoint, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        mainPanel.addComponent(new JLabel("Minimum"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerMinimum, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        mainPanel.addComponent(new JLabel(quantity.getFullUnitName()), 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Maximum"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerMaximum, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        mainPanel.addComponent(new JLabel(quantity.getFullUnitName()), 2, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Frame no"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerFrameCount, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    public boolean isContactFixed()
    {
        boolean fixed = boxFixContactPoint.isSelected();
        return fixed;
    }

    public int getFrameCount()
    {
        return ((SpinnerNumberModel)spinnerFrameCount.getModel()).getNumber().intValue();	
    }

    public double getLowerThreshold()
    {
        return ((SpinnerNumberModel)spinnerMinimum.getModel()).getNumber().doubleValue();	
    }

    public double getUpperThreshold()
    {
        return ((SpinnerNumberModel)spinnerMaximum.getModel()).getNumber().doubleValue();	
    }

    private void doOK()
    {
        approved = true;
        setVisible(false);
    }

    private void doCancel()
    {
        approved = false;
        setVisible(false);
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK,  buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    private class OKAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OKAction()
        {			
            putValue(MNEMONIC_KEY,KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            doOK();
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            doCancel();
        }
    }
}
