
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

package atomicJ.gui.generalProcessing;

import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import atomicJ.curveProcessing.TransformationBatchType;
import atomicJ.gui.SubPanel;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionPanel;


public class BatchProcessingDialog <E extends BatchProcessingModel<?,?,I>, I> extends OperationDialog<E> implements
PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private boolean initPreview;
    private TransformationBatchType initBatchType;

    private final JCheckBox boxPreview = new JCheckBox("Preview");

    private final JRadioButton buttonAll = new JRadioButton(TransformationBatchType.ALL.toString());
    private final JRadioButton buttonOnlySelected = new JRadioButton(TransformationBatchType.ONLY_SELECTED.toString());
    private final ButtonGroup buttonGroup = new ButtonGroup();

    private final MultipleSelectionPanel<I, MultipleSelectionModel<I>> identifierSelectionPanel = new MultipleSelectionPanel<>(true);

    public BatchProcessingDialog(Window parent, String title, boolean temporary)
    {
        this(parent, title, temporary, ModalityType.APPLICATION_MODAL);
    }

    public BatchProcessingDialog(Window parent, String title, boolean temporary, ModalityType modalityType)
    {
        super(parent, title, temporary, modalityType);

        initializeButtonGroup();
        initItemListener();
    }

    private void initializeButtonGroup()
    {
        buttonGroup.add(buttonAll);
        buttonGroup.add(buttonOnlySelected);
    }

    private void initItemListener()
    {        
        boxPreview.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                boolean selectedNew = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setPreviewEnabled(selectedNew);
            }
        });

        //we respond only to selection, because we don't want to respond to temporary situation
        //when no button is selected
        buttonAll.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    TransformationBatchType batchTypeNew = getBatchType();
                    getModel().setBatchType(batchTypeNew);    
                }
            }
        });
        buttonOnlySelected.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    TransformationBatchType batchTypeNew = getBatchType();
                    getModel().setBatchType(batchTypeNew);    
                }
            }
        });      
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(BatchProcessingModel.BATCH_TYPE.equals(property))
        {
            TransformationBatchType positionOld = getBatchType();
            TransformationBatchType positionNew = (TransformationBatchType)evt.getNewValue();

            if(!Objects.equals(positionOld, positionNew))
            {
                setBatchType(positionNew);
            }
        }
        else if(BatchProcessingModel.PREVIEW_ENABLED.equals(property))
        {
            boolean previewEnabledNew = (boolean) evt.getNewValue();
            boolean previewEnabledOld = boxPreview.isSelected();

            if(previewEnabledOld != previewEnabledNew)
            {
                boxPreview.setSelected(previewEnabledNew);
            }
        }
    }

    private TransformationBatchType getBatchType()
    {
        TransformationBatchType type = null;

        if(buttonAll.isSelected())
        {
            type = TransformationBatchType.ALL;
        }
        else if(buttonOnlySelected.isSelected())
        {
            type = TransformationBatchType.ONLY_SELECTED;
        }

        return type;
    }

    private TransformationBatchType setBatchType(TransformationBatchType batchType)
    {        
        if(TransformationBatchType.ALL.equals(batchType))
        {
            buttonGroup.setSelected(buttonAll.getModel(), true);
        }
        else if(TransformationBatchType.ONLY_SELECTED.equals(batchType))
        {
            buttonGroup.setSelected(buttonOnlySelected.getModel(), true);
        }

        return batchType;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        E model = getModel();

        buttonAll.setEnabled(model.isMultipleResourcesAvailable());
        this.initBatchType = model.getBatchType();
        this.initPreview = model.isPreviewEnabled();
    }

    @Override
    protected void clearOldModel()
    {
        super.clearOldModel();
        identifierSelectionPanel.cleanUp();
    }

    @Override
    protected void resetEditor()
    {        
        super.resetEditor();
        if(getModel() == null)
        {
            return;
        }

        setBatchType(initBatchType);
        identifierSelectionPanel.setModel(getModel().getIdentifierSelectionModel());
        boxPreview.setSelected(initPreview);
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        E model = getModel();

        model.setBatchType(initBatchType);
        model.setPreviewEnabled(initPreview);
    }


    //I made this method final, because it is meant to be called from the constructors of the descendant class;
    //methods called in the constructor should not be overriden, because this may cause problems whwn
    //non-static fields are used in the method body
    protected final JPanel buildPanelBatchType()
    {
        SubPanel panelOperationRange = new SubPanel();

        panelOperationRange.addComponent(buttonAll, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1); 
        panelOperationRange.addComponent(buttonOnlySelected, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        return panelOperationRange;
    }

    protected final JPanel buildPanelPreview()
    {
        SubPanel panelPreview = new SubPanel();

        panelPreview.addComponent(boxPreview, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        return panelPreview;
    }

    public MultipleSelectionPanel<I, MultipleSelectionModel<I>> getIdentifierSelectionPanel()
    {
        return identifierSelectionPanel;
    }
}
