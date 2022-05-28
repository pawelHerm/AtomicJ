
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

package atomicJ.gui.selection.multiple;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.Border;

import atomicJ.utilities.Validation;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;


public class MultipleSelectionPanel<E, V extends MultipleSelectionModel<E>> extends JPanel implements MultipleSelectionListener<E>
{
    private static final long serialVersionUID = 1L;

    private V model;
    private final Map<E, JCheckBox> boxes = new LinkedHashMap<>();

    private final Action clearAction = new ClearAction();
    private final Action selectAllAction = new SelectAllAction();

    private final JButton buttonClear = new JButton(clearAction);
    private final JButton buttonSelectAll = new JButton(selectAllAction);
    private final JPanel panelControls;

    private final boolean horizontal;
    private boolean fullWizardLayout;

    //copied
    public MultipleSelectionPanel()
    {
        this(false);
    }

    public MultipleSelectionPanel(boolean horizontal)
    {
        this.horizontal = horizontal;
        this.panelControls = buildControlPanel();
        setLayout(new BorderLayout());
    }

    public MultipleSelectionPanel(V model, boolean fullWizardLayout)
    {       
        this(model, fullWizardLayout, false);
    }

    //copied
    public MultipleSelectionPanel(V model, boolean fullWizardLayout, boolean horizontal)
    {		
        this.horizontal = horizontal;
        this.panelControls = buildControlPanel();
        this.fullWizardLayout = fullWizardLayout;
        setLayout(new BorderLayout());
        setModel(model);
    }


    public V getModel()
    {
        return model;
    }

    //copied
    public void setModel(V modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(model != null)
        {
            model.removeSelectionChangeListener(this);
        }

        this.model = modelNew;
        model.addSelectionChangeListener(this);

        pullModelProperties();
    }

    //copied
    public void cleanUp()
    {
        if(model != null)
        {
            model.removeSelectionChangeListener(this);
        }

        this.model = null;        
    }

    //copied
    private void pullModelProperties()
    {
        boolean allSelected = model.areAllKeysSelected();
        boolean allDeselected = model.areAllKeysDeselected();

        clearAction.setEnabled(!allDeselected);
        selectAllAction.setEnabled(!allSelected);

        updatePanelBoxes();
    }

    //copied
    private JPanel buildControlPanel()
    {
        JPanel panelControl = new JPanel();	

        GroupLayout layout = new GroupLayout(panelControl);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonSelectAll).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClear).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(buttonSelectAll)
                .addComponent(buttonClear));

        layout.linkSize(buttonSelectAll, buttonClear);

        return panelControl;
    }

    //copied
    private void updatePanelBoxes()
    {		        
        updateBoxes();
        JPanel panelBoxes = new JPanel();

        JLabel labelDatasets = new JLabel(model.getSelectionName());
        GroupLayout layout = new GroupLayout(panelBoxes);
        panelBoxes.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        //builds and sets horizontal group
        SequentialGroup horizontalGroup = layout.createSequentialGroup();
        ParallelGroup innerHorizontalGroup = layout.createParallelGroup();
        innerHorizontalGroup.addComponent(labelDatasets);

        SequentialGroup tableGroupH = layout.createSequentialGroup();
        innerHorizontalGroup.addGroup(tableGroupH);

        int boxCount = boxes.values().size();
        int rowCount;
        int columnCount;

        if(horizontal)
        {
            columnCount = 3;
            rowCount = boxCount/columnCount + 1;
        }
        else
        {
            rowCount = 20;
            columnCount = boxCount/rowCount + 1;
        }

        List<JCheckBox> boxList = new ArrayList<>(boxes.values());

        for(int i = 0; i<columnCount; i++)
        {
            ParallelGroup columnGroup = layout.createParallelGroup();
            tableGroupH.addGroup(columnGroup);

            int minIndex = i*rowCount;
            int maxIndex = Math.min(minIndex + rowCount, boxCount);

            for(int j = minIndex; j < maxIndex; j++)
            {                
                columnGroup.addComponent(boxList.get(j));
            }
        }

        horizontalGroup
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(innerHorizontalGroup)
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setHorizontalGroup(horizontalGroup);

        //builds and sets vertical group

        SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(labelDatasets).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);

        ParallelGroup tableGroupV = layout.createParallelGroup();
        verticalGroup.addGroup(tableGroupV);

        for(int i = 0; i<columnCount; i++)
        {
            SequentialGroup columnGroup = layout.createSequentialGroup();
            tableGroupV.addGroup(columnGroup);

            int minIndex = i*rowCount;
            int maxIndex = Math.min(minIndex + rowCount, boxCount);

            for(int j = minIndex; j < maxIndex; j++)
            {                
                columnGroup.addComponent(boxList.get(j)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            }
        }

        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setVerticalGroup(verticalGroup);

        if(!boxes.isEmpty())
        {
            layout.linkSize(boxes.values().toArray(new JCheckBox[] {}));
        }

        removeAll();
        if(fullWizardLayout)
        {
            Border border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createEmptyBorder(8,8,8,8));
            panelBoxes.setBorder(border);
        }
        add(panelBoxes, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    //copied
    private void updateBoxes()
    {	
        boxes.clear();

        for(final E key: model.getKeys())
        {
            final JCheckBox box = new JCheckBox(key.toString());
            boxes.put(key, box);

            box.setSelected(model.isSelected(key));

            box.addItemListener(new ItemListener() {              
                @Override
                public void itemStateChanged(ItemEvent e) {
                    model.setSelected(key, box.isSelected());                    
                }
            });
        }		
    }

    @Override
    public void keySelectionChanged(E key, boolean selectedOld,
            boolean selectedNew) 
    {
        JCheckBox box = boxes.get(key);
        box.setSelected(selectedNew);  
    }

    @Override
    public void allKeysSelectedChanged(boolean allSelectedOld, boolean allSelectedNew) 
    {
        selectAllAction.setEnabled(!allSelectedNew);        
    }

    @Override
    public void allKeysDeselectedChanged(boolean allDeselectedOld, boolean allDeselectedNew)
    {
        clearAction.setEnabled(!allDeselectedNew);    
    }

    @Override
    public void keySetChanged(Set<E> keysOld, Set<E> keysNew)
    {
        updatePanelBoxes();        
    }

    private void clear()
    {
        model.setAllSelected(false);
    }

    private void selectAll()
    {
        model.setAllSelected(true);
    }

    //copied
    public Component getControls() 
    {
        return panelControls;
    }


    public Component getView() {
        return this;
    }

    private class ClearAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ClearAction()
        {			
            putValue(NAME, "Clear");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            clear();
        }
    }

    private class SelectAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SelectAllAction()
        {			
            putValue(NAME, "Select all");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            selectAll();
        }
    }
}
