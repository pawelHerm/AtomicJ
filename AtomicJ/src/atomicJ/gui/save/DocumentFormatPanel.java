
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

import static atomicJ.gui.save.SaveModelProperties.*;


import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import atomicJ.gui.SubPanel;


public class DocumentFormatPanel extends SubPanel implements PropertyChangeListener, ItemListener 
{
    private static final long serialVersionUID = 1L;

    private BasicImageFormatPanel basicPanel;
    private final JCheckBox boxFitToPage = new JCheckBox("Fit to page");
    private final JComboBox<PageSize> comboPageSize = new JComboBox<>(PageSize.values());
    private final JComboBox<PageMargins> comboPageMargins = new JComboBox<>(PageMargins.values());
    private final JComboBox<PageOrientation> comboPageOrientation = new JComboBox<>(PageOrientation.values());

    private DocumentFormatModel model;

    public DocumentFormatPanel(DocumentFormatModel model) 
    {
        setModel(model);
        buildLayout();
        initItemListener();
    }

    public DocumentFormatModel getModel() 
    {
        return model;
    }

    public void setModel(DocumentFormatModel modelNew) 
    {
        if(basicPanel == null)
        {
            this.basicPanel = new BasicImageFormatPanel(modelNew);
        }
        else
        {
            basicPanel.setModel(modelNew);
        }		
        if (model != null) 
        {
            model.removePropertyChangeListener(this);
        }
        this.model = modelNew;
        pullModelProperties();
        modelNew.addPropertyChangeListener(this);
    }

    public void specifyDimensions(Number widthNew, Number heightNew) 
    {
        model.specifyDimensions(widthNew, heightNew);
    }

    private void pullModelProperties() 
    {
        boolean fitToPage = model.isFitToPage();
        PageSize pageSize = model.getPageSize();
        PageOrientation pageOrientation = model.getPageOrientation();
        PageMargins pageMargins = model.getPageMargins();

        boolean isCustomPage = pageSize.equals(PageSize.CUSTOM);
        boolean basicPanelEnabled = !fitToPage|| isCustomPage;

        boxFitToPage.setSelected(fitToPage);
        comboPageSize.setSelectedItem(pageSize);
        comboPageOrientation.setSelectedItem(pageOrientation);
        comboPageMargins.setSelectedItem(pageMargins);

        boxFitToPage.setEnabled(!isCustomPage);
        basicPanel.setEnabled(basicPanelEnabled);

        setConsistentWithCustomPageSize();
    }

    private void buildLayout() 
    {
        JPanel panelPS = builPSPanel();
        JPanel basicInternalPanel = basicPanel.getInternalPanel();

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(panelPS)
                .addComponent(basicInternalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE));

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(panelPS).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(basicInternalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE));

        layout.linkSize(SwingConstants.HORIZONTAL,panelPS, basicInternalPanel);
    }

    private JPanel builPSPanel()
    {
        SubPanel panelPS = new SubPanel();
        panelPS.addComponent(new JLabel("Size"), 0, 0, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 1);
        panelPS.addComponent(new JLabel("Margins"), 0, 1, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 1);
        panelPS.addComponent(new JLabel("Orientation"), 0, 2, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 1);
        panelPS.addComponent(comboPageSize, 1, 0, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 1);
        panelPS.addComponent(comboPageMargins, 1, 1, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 1);
        panelPS.addComponent(comboPageOrientation, 1, 2, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 1);
        panelPS.addComponent(boxFitToPage, 1, 3, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 1);

        return panelPS;
    }

    private void initItemListener() 
    {
        boxFitToPage.addItemListener(this);
        comboPageSize.addItemListener(this);
        comboPageMargins.addItemListener(this);
        comboPageOrientation.addItemListener(this);
    }

    private void setConsistentWithCustomPageSize()
    {
        boolean isCustomPage = comboPageSize.getSelectedItem().equals(PageSize.CUSTOM);
        boolean fitToPage = boxFitToPage.isSelected();
        boolean basicPanelEnabled = !fitToPage|| isCustomPage;

        boxFitToPage.setEnabled(!isCustomPage);
        if(isCustomPage)
        {
            boxFitToPage.setSelected(true);
        }
        basicPanel.setEnabled(basicPanelEnabled);
    }

    @Override
    public void itemStateChanged(ItemEvent event) 
    {
        Object source = event.getSource();

        if(source == boxFitToPage)
        {
            boolean fitToPageNew = boxFitToPage.isSelected();
            model.setFitToPage(fitToPageNew);
            basicPanel.setEnabled(!fitToPageNew);
        }
        else if (source == comboPageSize) 
        {
            PageSize pageSizeNew = (PageSize) comboPageSize.getSelectedItem();
            model.setPageSize(pageSizeNew);
            setConsistentWithCustomPageSize();
        }
        else if (source == comboPageMargins) 
        {
            PageMargins pageMarginsNew = (PageMargins) comboPageMargins.getSelectedItem();
            model.setPageMargins(pageMarginsNew);
        }
        else if (source == comboPageOrientation) 
        {
            PageOrientation pageOrientationNew = (PageOrientation) comboPageOrientation.getSelectedItem();
            model.setPageOrientation(pageOrientationNew);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if (name.equals(FIT_TO_PAGE)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            boolean oldVal = boxFitToPage.isSelected();
            if (oldVal != newVal) 
            {
                boxFitToPage.setSelected(newVal);
                basicPanel.setEnabled(!newVal);
            }
        } 
        else if (name.equals(PAGE_SIZE)) 
        {
            PageSize newVal = (PageSize) evt.getNewValue();
            PageSize oldVal = (PageSize) comboPageSize.getSelectedItem();
            if (!oldVal.equals(newVal)) 
            {
                comboPageSize.setSelectedItem(newVal);
                setConsistentWithCustomPageSize();
            }
        } 
        else if (name.equals(PAGE_MARGINS)) 
        {
            PageMargins newVal = (PageMargins) evt.getNewValue();
            PageMargins oldVal = (PageMargins) comboPageMargins.getSelectedItem();
            if (!oldVal.equals(newVal)) 
            {
                comboPageMargins.setSelectedItem(newVal);
            }
        } 
        else if (name.equals(PAGE_ORIENTATION)) 
        {
            PageOrientation newVal = (PageOrientation) evt.getNewValue();
            PageOrientation oldVal = (PageOrientation) comboPageOrientation.getSelectedItem();
            if (!oldVal.equals(newVal)) 
            {
                comboPageOrientation.setSelectedItem(newVal);
            }
        } 
    }

}
