
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
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.functions.IncrementationFunction;



public class ConstantFunctionDialog extends JDialog implements ChangeListener
{
    private static final long serialVersionUID = 1L;

    private final JButton buttonOK = new JButton(new OKAction());
    private final JButton buttonReset = new JButton(new ResetAction());
    private final JButton buttonCancel = new JButton(new CancelAction());
    private final JSpinner spinnerConstant = new JSpinner(new SpinnerNumberModel(0, -Short.MAX_VALUE, Short.MAX_VALUE, .05));

    private UnivariateFunctionReceiver receiver;

    public ConstantFunctionDialog(Window parent, String title)
    {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

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

        spinnerConstant.addChangeListener(this);
    }	

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        UnivariateFunction f = getFunction();
        receiver.setFunction(f);
    }

    public void showDialog(UnivariateFunctionReceiver receiver)
    {
        this.receiver = receiver;
        setSpinnersToZeros();
        setVisible(true);		
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();    

        mainPanel.addComponent(new JLabel(" + "), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(spinnerConstant, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    public UnivariateFunction getFunction()
    {

        double constant = ((SpinnerNumberModel)spinnerConstant.getModel()).getNumber().doubleValue();
        UnivariateFunction f = new IncrementationFunction(constant);
        return f;
    }

    private void approve()
    {
        setVisible(false);
    }

    private void setSpinnersToZeros()
    {

        spinnerConstant.setValue(0);
    }

    private void reset()
    {
        setSpinnersToZeros();
        receiver.reset();
    }

    private void cancel()
    {
        setSpinnersToZeros();
        receiver.reset();
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
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonReset)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK, buttonReset, buttonCancel);

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
            approve();
        }
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            reset();
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
            cancel();
        }
    }
}
