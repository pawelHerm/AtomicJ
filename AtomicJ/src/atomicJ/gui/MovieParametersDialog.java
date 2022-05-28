
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

import static atomicJ.gui.stack.StackModel.*;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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

import atomicJ.gui.stack.StackModel;

public class MovieParametersDialog extends JDialog implements ChangeListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;	

    private double initFrameRate;
    private double initMovieLength;

    private int frameCount;	
    private double frameRate;
    private double movieLength;

    private final JButton buttonOK = new JButton(new OKAction());
    private final JButton buttonReset = new JButton(new ResetAction());

    private final JButton buttonCancel = new JButton(new CancelAction());

    private final JLabel labelFrameCount = new JLabel();
    private final JSpinner spinnerMovieLength = new JSpinner(new SpinnerNumberModel(10., 0.01, Integer.MAX_VALUE, 1.));
    private final JSpinner spinnerFrameRate = new JSpinner(new SpinnerNumberModel(10., 1., Integer.MAX_VALUE, 1.));

    private StackModel<?> model;

    public MovieParametersDialog(Window parent,StackModel<?> model)
    {
        super(parent, "Movie parameters", ModalityType.MODELESS);
        setLayout(new BorderLayout());

        setModel(model);
        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListener();

        pack();
        setLocationRelativeTo(parent);
    }

    public void setModel(StackModel<?> model)
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }

        this.model = model;
        model.addPropertyChangeListener(this);

        pullModelParameters();
        setParametersToInitial();
        resetEditor();
    }

    private void initChangeListener()
    {
        spinnerMovieLength.addChangeListener(this);
        spinnerFrameRate.addChangeListener(this);
    }	

    private void pullModelParameters()
    {
        this.frameCount = model.getFrameCount();
        this.initFrameRate = model.getFrameRate();
        this.initMovieLength = model.getMovieLength();
    }

    private void setParametersToInitial()
    {
        this.frameRate = this.initFrameRate;
        this.movieLength = this.initMovieLength;
    }

    private void resetEditor()
    {
        labelFrameCount.setText("Frame count " + Integer.toString(frameCount));
        spinnerFrameRate.setValue(frameRate);
        spinnerMovieLength.setValue(movieLength);
    }

    private void resetModel()
    {
        model.setFrameRate(frameRate);
        model.setMovieLength(movieLength);
    }



    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == spinnerMovieLength)
        {
            double movieLength = ((SpinnerNumberModel)spinnerMovieLength.getModel()).getNumber().doubleValue();
            model.setMovieLength(movieLength);
        }
        else if(source == spinnerFrameRate)
        {
            double frameRate = ((SpinnerNumberModel)spinnerFrameRate.getModel()).getNumber().doubleValue();
            model.setFrameRate(frameRate);
        }
    }	


    public void showDialog()
    {		
        pullModelParameters();
        setParametersToInitial();
        resetEditor();

        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();    

        mainPanel.addComponent(labelFrameCount, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        mainPanel.addComponent(new JLabel("Rate"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerFrameRate, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        mainPanel.addComponent(new JLabel("fps"), 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Length"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerMovieLength, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        mainPanel.addComponent(new JLabel("s"), 2, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    private void doOK()
    {
        setVisible(false);
    }

    public void doReset()
    {
        setParametersToInitial();
        resetModel();
    }

    private void doCancel()
    {
        setParametersToInitial();
        resetModel();
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

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            doReset();
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(FRAME_RATE.equals(name))
        {
            double frameRateNew = (double)evt.getNewValue();
            spinnerFrameRate.setValue(frameRateNew);
        }
        else if(MOVIE_LENGTH.equals(name))
        {
            double movieLength = (double)evt.getNewValue();
            spinnerMovieLength.setValue(movieLength);
        }
    }
}
