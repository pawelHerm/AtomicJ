
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

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import atomicJ.gui.SubPanel;

public class TIFFMultipageFormatPanel extends SubPanel implements PropertyChangeListener, ItemListener
{
    private static final long serialVersionUID = 1L;

    private BasicImageFormatPanel basicPanel;

    private final JComboBox<TIFFMovieCompressionMethod> comboCompression = new JComboBox<>(TIFFMovieCompressionMethod.values());

    private TIFFMultipageFormatModel model;

    public TIFFMultipageFormatPanel(TIFFMultipageFormatModel model) 
    {		
        setModel(model);
        buildLayout();
        initItemListener();
    }

    public TIFFMultipageFormatModel getModel() 
    {
        return model;
    }

    public void setModel(TIFFMultipageFormatModel modelNew) 
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
        TIFFMovieCompressionMethod compression = model.getCompression();
        comboCompression.setSelectedItem(compression);
    }

    private void buildLayout() 
    {
        JPanel panelMovie = builMovieControlPanel();
        JPanel basicInternalPanel = basicPanel.getInternalPanel();

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(panelMovie)
                .addComponent(basicInternalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(panelMovie).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(basicInternalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));

        layout.linkSize(SwingConstants.HORIZONTAL, panelMovie, basicInternalPanel);
    }

    private JPanel builMovieControlPanel()
    {

        SubPanel panelMovie = new SubPanel();

        panelMovie.addComponent(Box.createVerticalGlue(), 0, 0, 2, 1, GridBagConstraints.WEST,GridBagConstraints.BOTH, 0, 1);

        panelMovie.addComponent(new JLabel("Compression"), 0, 1, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 0);
        panelMovie.addComponent(comboCompression, 1, 1, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 0);

        panelMovie.addComponent(Box.createVerticalGlue(), 0, 6, 2, 1, GridBagConstraints.WEST,GridBagConstraints.BOTH, 0, 1);

        return panelMovie;
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
            TIFFMovieCompressionMethod compressionNew = (TIFFMovieCompressionMethod) comboCompression.getSelectedItem();
            model.setCompression(compressionNew);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if (TIFF_MULTIPAGE_COMPRESSION.equals(name)) 
        {
            TIFFMovieCompressionMethod newVal = (TIFFMovieCompressionMethod) evt.getNewValue();
            TIFFMovieCompressionMethod oldVal = (TIFFMovieCompressionMethod) comboCompression.getSelectedItem();
            if (!oldVal.equals(newVal)) 
            {
                comboCompression.setSelectedItem(newVal);
            }
        }    
    }
}
