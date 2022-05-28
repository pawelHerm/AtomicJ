
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

package atomicJ.gui.profile;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.gui.SubPanel;
import atomicJ.resources.CrossSectionSettings;


public class CrossSectionSettingsView extends JDialog
{
    private static final long serialVersionUID = 1L;

    private final ApplyToAllAction applyToAllAction = new ApplyToAllAction();
    private final JButton buttonOK = new JButton(applyToAllAction);
    private final JButton buttonReset = new JButton(new ResetAction());
    private final JButton buttonCancel = new JButton(new CloseAction());

    private String currentType;
    private Map<String, CrossSectionSettings> initCrossSectionSettings;	

    private Map<String, CrossSectionSettings> crossSectionSettings;

    private final JComboBox<InterpolationMethod2D> comboInterpolationMethod = new JComboBox<>(InterpolationMethod2D.values()); 

    private final JSpinner spinnerPointCount = new JSpinner(new SpinnerNumberModel(100, 2, Integer.MAX_VALUE, 1));

    private CrossSectionSettingsReceiver receiver;

    public CrossSectionSettingsView(Window parent, String title)
    {
        super(parent, title, ModalityType.MODELESS);
        setLayout(new BorderLayout());

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListener();
        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void setParametersToInitial()
    {
        this.crossSectionSettings = new LinkedHashMap<>();

        for(Entry<String, CrossSectionSettings> entry: initCrossSectionSettings.entrySet())
        {
            String type = entry.getKey();
            CrossSectionSettings settings = new CrossSectionSettings(entry.getValue());
            crossSectionSettings.put(type, settings);
        }
    }

    private void pullReceiverParameters()
    {
        this.initCrossSectionSettings = receiver.getCrossSectionSettings();
        this.currentType = receiver.getCurrentSectionType();

        boolean multipleSettings = initCrossSectionSettings.size()>1;		
        applyToAllAction.setEnabled(multipleSettings);
    }

    private void initChangeListener()
    {
        spinnerPointCount.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int pointCountNew = ((SpinnerNumberModel)spinnerPointCount.getModel()).getNumber().intValue();

                CrossSectionSettings settings = crossSectionSettings.get(currentType);
                int pointCountOld = settings.getPointCount();

                if(pointCountOld != pointCountNew)
                {
                    settings.setPointCount(pointCountNew);
                    receiver.setCrossSectionSettings(currentType, settings);
                }                
            }
        });
    }	

    private void initItemListener()
    {
        comboInterpolationMethod.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                CrossSectionSettings settings = crossSectionSettings.get(currentType);

                InterpolationMethod2D interpolationMethodNew = (InterpolationMethod2D)comboInterpolationMethod.getSelectedItem();            
                InterpolationMethod2D interpolationMethodOld = settings.getInterpolationMethod();

                if(!interpolationMethodOld.equals(interpolationMethodNew))
                {
                    settings.setInterpolationMethod(interpolationMethodNew);
                    receiver.setCrossSectionSettings(currentType, settings);
                }                
            }
        });
    }

    public void ensureConsistencyWithReceiver()
    {
        pullReceiverParameters();
        setParametersToInitial();
        resetEditor();
    }

    public void showDialog(CrossSectionSettingsReceiver receiver)
    {
        this.receiver = receiver;

        ensureConsistencyWithReceiver();
        setVisible(true);		
    }

    private void applyToAll()
    {
        CrossSectionSettings settingsForCurrentType = crossSectionSettings.get(currentType);

        Set<String> types = crossSectionSettings.keySet();
        for(String type : types)
        {
            crossSectionSettings.put(type, settingsForCurrentType);
        }

        receiver.setCrossSectionSettings(types, settingsForCurrentType);
    }

    private void resetReceiver()
    {	  
        receiver.setCrossSectionSettings(crossSectionSettings);
    }

    private void resetEditor()
    {		
        CrossSectionSettings settingsForCurrentType = this.crossSectionSettings.get(currentType);

        int pointCount = settingsForCurrentType.getPointCount();
        InterpolationMethod2D interpolationMethod = settingsForCurrentType.getInterpolationMethod();	    

        spinnerPointCount.setValue(pointCount);
        comboInterpolationMethod.setSelectedItem(interpolationMethod);
    }

    private void reset()
    {
        setParametersToInitial();
        resetReceiver();
        resetEditor();
    }

    private void close()
    {
        setVisible(false);
    }

    private JPanel buildMainPanel()
    {	
        JPanel outerPanel = new JPanel();

        SubPanel innerPanel = new SubPanel(); 

        innerPanel.addComponent(new JLabel("Interpolation "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(comboInterpolationMethod, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        innerPanel.addComponent(new JLabel("Point count "), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(spinnerPointCount, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);      

        innerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        outerPanel.add(innerPanel);

        return outerPanel;
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

    private class ApplyToAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyToAllAction()
        {			
            putValue(MNEMONIC_KEY,KeyEvent.VK_A);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Apply to All");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            applyToAll();
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
            reset();
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            close();
        }
    }
}
