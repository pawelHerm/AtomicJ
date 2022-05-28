
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

package atomicJ.gui.editors;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

import atomicJ.gui.GradientMaskSelector;
import atomicJ.gui.GradientPaintReceiver;
import atomicJ.gui.PaintSampleFlexible;
import atomicJ.gui.SubPanel;


public class ROIMaskEditor extends JDialog implements ItemListener, ActionListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final static String MASK_COLOR_COMMAND = "MASK_COLOR_COMMAND";


    private GradientMaskSelector initGradientMaskSelector;	
    private Color initMaskColor;	
    private Color maskColor;	
    private GradientMaskSelector gradientMaskSelector;

    private final PaintSampleFlexible maskColorSample;
    private final JButton buttonSelectMaskColor = new JButton("Select");


    private final JCheckBox boxOutsideMask = new JCheckBox("Outside");
    private final JCheckBox boxInsideMask = new JCheckBox("Inside");


    private final JLabel labelMaskColor = new JLabel("Mask color");

    private GradientPaintReceiver receiver;



    public ROIMaskEditor(Window parent, GradientPaintReceiver receiver)
    {
        this(parent, receiver, true);
    }
    public ROIMaskEditor(Window parent, GradientPaintReceiver receiver, boolean allowForROIs)
    {
        super(parent, "ROI mask", ModalityType.MODELESS);		

        this.receiver = receiver;
        this.receiver.addPropertyChangeListener(this);

        initGradientMaskSelector = receiver.getGradientMaskSelector();

        initMaskColor = receiver.getMaskColor();

        setParametersToInitial();

        maskColorSample = new PaintSampleFlexible(initMaskColor);

        JPanel mainPanel = buildMainPanel();
        JPanel buttonPanel = buildButtonPanel();

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        initActionListener();
        initItemListener();

        setConsistentWithUseMasks();

        pack();
        setLocationRelativeTo(parent);
    }

    private void setParametersToInitial()
    {
        gradientMaskSelector = initGradientMaskSelector;				
        maskColor = initMaskColor;		
    }

    private void pullReceiverParameters()
    {
        initGradientMaskSelector = receiver.getGradientMaskSelector();		
        initMaskColor = receiver.getMaskColor();

        setParametersToInitial();	
    }

    public GradientPaintReceiver getReceiver()
    {
        return receiver;
    }

    public void setReceiver(GradientPaintReceiver receiver)
    {
        if(this.receiver != null)
        {
            this.receiver.removePropertyChangeListener(this);
        }
        this.receiver = receiver;
        this.receiver.addPropertyChangeListener(this);
        ensureConsistencyWithReceiver();
    }

    protected void ensureConsistencyWithReceiver()
    {
        pullReceiverParameters();
        resetInterface();
    }

    public void cleanUp()
    {
        if(this.receiver != null)
        {
            this.receiver.removePropertyChangeListener(this);
        }
        this.receiver = null;
    }

    private void initActionListener()
    {
        buttonSelectMaskColor.setActionCommand(MASK_COLOR_COMMAND);
        buttonSelectMaskColor.addActionListener(this);
    }

    private void initItemListener()
    {
        boxInsideMask.addItemListener(this);
        boxOutsideMask.addItemListener(this);
    }

    private void resetReceiver()
    {
        if(!initMaskColor.equals(maskColor))
        {
            receiver.setMaskColor(maskColor);
        }
        if(!initGradientMaskSelector.equals(gradientMaskSelector))
        {
            receiver.setGradientMaskSelector(initGradientMaskSelector);
        }
    }

    private void resetInterface()
    {		

        boolean insideMask = GradientMaskSelector.MASK_INSIDE.equals(gradientMaskSelector);
        boolean outsideMask = GradientMaskSelector.MASK_OUTSIDE.equals(gradientMaskSelector);

        boxInsideMask.setSelected(insideMask);
        boxOutsideMask.setSelected(outsideMask);

        maskColorSample.setPaint(maskColor);

        setConsistentWithUseMasks();
    }






    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();
        boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);

        if(source == boxInsideMask)
        {
            boolean otherSelected = boxOutsideMask.isSelected();
            if(selected)
            {
                boxOutsideMask.setSelected(false);
                receiver.setGradientMaskSelector(GradientMaskSelector.MASK_INSIDE);
            }
            else if(!otherSelected)
            {
                receiver.setGradientMaskSelector(GradientMaskSelector.NO_MASK);
            }

            setConsistentWithUseMasks();
        }
        else if(source == boxOutsideMask)
        {
            boolean otherSelected = boxInsideMask.isSelected();

            if(selected)
            {
                boxInsideMask.setSelected(false);
                receiver.setGradientMaskSelector(GradientMaskSelector.MASK_OUTSIDE);
            }
            else if(!otherSelected)
            {
                receiver.setGradientMaskSelector(GradientMaskSelector.NO_MASK);
            }

            setConsistentWithUseMasks();
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        String command = evt.getActionCommand();
        if(command.equals(MASK_COLOR_COMMAND))
        {
            attemptMaskColorSelection();
        }
    }

    private void setConsistentWithUseMasks()
    {		
        boolean useMasks = !GradientMaskSelector.NO_MASK.equals(gradientMaskSelector);

        buttonSelectMaskColor.setEnabled(useMasks);
        labelMaskColor.setEnabled(useMasks);
        maskColorSample.setEnabled(useMasks);
    }





    private void attemptMaskColorSelection() 
    {
        Paint p = this.maskColor;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.black);
        Color c = JColorChooser.showDialog(this, "Mask color", defaultColor);
        if (c != null) 
        {
            maskColor = c;
            this.maskColorSample.setPaint(maskColor);
            receiver.setMaskColor(maskColor);
        }
    }



    protected void reset()
    {
        resetReceiver();		
        setParametersToInitial();
        resetInterface();
    }

    private void cancel()
    {
        //its important that resetReceiver() is called before setParametersToInitial()
        resetReceiver();	
        setParametersToInitial();
        resetInterface();

        setVisible(false);
    }

    private JPanel buildMainPanel()
    {
        SubPanel mainPanel = new SubPanel();


        JLabel labelMask = new JLabel("ROI masks");

        labelMask.setFont(labelMask.getFont().deriveFont(Font.BOLD));



        SubPanel panelMasks = new SubPanel();
        panelMasks.addComponent(boxOutsideMask, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        panelMasks.addComponent(boxInsideMask, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);


        mainPanel.addComponent(labelMask, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1, new Insets(9,3,5,5));
        mainPanel.addComponent(panelMasks, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);


        mainPanel.addComponent(labelMaskColor, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(maskColorSample, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(buttonSelectMaskColor, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);



        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return mainPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonOK = new JButton(new OKAction());
        JButton buttonReset = new JButton(new ResetAction());
        JButton buttonCancel = new JButton(new CancelAction());

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
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        };
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
        };
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
            cancel();
        };
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        if(GradientPaintReceiver.GRADIENT_MASK_SELECTOR.equals(name))
        {
            gradientMaskSelector = (GradientMaskSelector)evt.getNewValue();
            if(GradientMaskSelector.MASK_OUTSIDE.equals(gradientMaskSelector))
            {
                boxOutsideMask.setSelected(true);
                boxInsideMask.setSelected(false);
            }
            else if(GradientMaskSelector.MASK_INSIDE.equals(gradientMaskSelector))
            {
                boxOutsideMask.setSelected(false);
                boxInsideMask.setSelected(true);
            }
            else
            {
                boxInsideMask.setSelected(false);
                boxOutsideMask.setSelected(false);
            }

            setConsistentWithUseMasks();
        }
        else if(GradientPaintReceiver.MASK_COLOR.equals(name))
        {
            Color maskColorNew = (Color)evt.getNewValue();
            this.maskColor = maskColorNew;
            maskColorSample.setPaint(maskColorNew);
        }
    }
}
