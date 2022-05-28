
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import atomicJ.gui.SubPanel;


public class TIFFFormatPanel extends SubPanel implements PropertyChangeListener, ItemListener 
{
    private static final long serialVersionUID = 1L;

    private BasicImageFormatPanel basicPanel;

    private final JComboBox<TIFFCompressionMethod> comboCompression = new JComboBox<>(TIFFCompressionMethod.values());

    private TIFFFormatModel model;

    public TIFFFormatPanel(TIFFFormatModel model) 
    {
        setModel(model);
        buildLayout();
        initItemListener();
    }

    public TIFFFormatModel getModel() 
    {
        return model;
    }

    public void setModel(TIFFFormatModel modelNew) 
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
        TIFFCompressionMethod compression = model.getCompressionMethod();

        comboCompression.setSelectedItem(compression);
    }

    private void buildLayout() 
    {
        JPanel panelJPEG = buildTIFFPanel();

        JPanel basicInternalPanel = basicPanel.getInternalPanel();

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(panelJPEG)
                .addComponent(basicInternalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(panelJPEG).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(basicInternalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));

        layout.linkSize(SwingConstants.HORIZONTAL,panelJPEG, basicInternalPanel);
    }

    private JPanel buildTIFFPanel()
    {
        SubPanel tiffPanel = new SubPanel();
        tiffPanel.addComponent(new JLabel("Compression"), 0, 0, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 1);
        tiffPanel.addComponent(comboCompression, 1, 0, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 1);

        return tiffPanel;
    }

    private void initItemListener() 
    {
        comboCompression.addItemListener(this);
    }

    @Override
    public void itemStateChanged(ItemEvent event) 
    {
        Object source = event.getSource();
        if (source == comboCompression) 
        {
            TIFFCompressionMethod compressionNew = (TIFFCompressionMethod) comboCompression.getSelectedItem();
            model.setCompressionMethod(compressionNew);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if (name.equals(COMPRESSION)) 
        {
            TIFFCompressionMethod newVal = (TIFFCompressionMethod) evt.getNewValue();
            TIFFCompressionMethod oldVal = (TIFFCompressionMethod) comboCompression.getSelectedItem();
            if (!oldVal.equals(newVal)) 
            {
                comboCompression.setSelectedItem(newVal);
            }
        } 
    }

}
