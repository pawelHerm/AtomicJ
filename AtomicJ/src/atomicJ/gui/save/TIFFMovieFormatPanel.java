
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.SubPanel;


public class TIFFMovieFormatPanel extends SubPanel implements PropertyChangeListener, ItemListener, ChangeListener
{
    private static final long serialVersionUID = 1L;

    private BasicImageFormatPanel basicPanel;

    private final JLabel labelFrameCount = new JLabel();

    private final JSpinner spinnerFirstFrame = new JSpinner();
    private final JSpinner spinnerLastFrame = new JSpinner();

    private final JCheckBox boxBackwards = new JCheckBox();
    private final JComboBox<TIFFMovieCompressionMethod> comboCompression = new JComboBox<>(TIFFMovieCompressionMethod.values());

    private TIFFMovieFormatModel model;

    public TIFFMovieFormatPanel(TIFFMovieFormatModel model) 
    {		
        setModel(model);
        buildLayout();
        initChangeListener();
        initItemListener();
    }

    public TIFFMovieFormatModel getModel() 
    {
        return model;
    }

    public void setModel(TIFFMovieFormatModel modelNew) 
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

        boolean backwards = model.isPlayedBackwards();
        boxBackwards.setSelected(backwards);

        int frameCount = model.getFrameCount();
        int firstFrame = model.getFirstFrame() + 1;
        int lastFrame = model.getLastFrame() + 1;

        labelFrameCount.setText(Integer.toString(frameCount));

        SpinnerNumberModel spinnerFirstFrameModel = new SpinnerNumberModel(firstFrame, 1, lastFrame, 1);	
        SpinnerNumberModel spinnerLastFrameModel = new SpinnerNumberModel(lastFrame, firstFrame, frameCount, 1);

        spinnerFirstFrame.setModel(spinnerFirstFrameModel);
        spinnerLastFrame.setModel(spinnerLastFrameModel);
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
        panelMovie.addComponent(new JLabel("Backwards"), 0, 2, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 0);
        panelMovie.addComponent(boxBackwards, 1, 2, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 0);
        panelMovie.addComponent(new JLabel("Frame count"), 0, 3, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 0);
        panelMovie.addComponent(labelFrameCount, 1, 3, 1, 1, GridBagConstraints.WEST,GridBagConstraints.NONE, 0, 0);

        panelMovie.addComponent(new JLabel("First frame"), 0, 4, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 0);
        panelMovie.addComponent(spinnerFirstFrame, 1, 4, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 0);
        panelMovie.addComponent(new JLabel("Last frame"), 0, 5, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 0);
        panelMovie.addComponent(spinnerLastFrame, 1, 5, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 0);

        panelMovie.addComponent(Box.createVerticalGlue(), 0, 6, 2, 1, GridBagConstraints.WEST,GridBagConstraints.BOTH, 0, 1);

        return panelMovie;
    }

    private void initChangeListener()
    {
        spinnerFirstFrame.addChangeListener(this);
        spinnerLastFrame.addChangeListener(this);
    }

    private void initItemListener() 
    {
        boxBackwards.addItemListener(this);
        comboCompression.addItemListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();
        if(source == spinnerFirstFrame)
        {
            int firstFrame = ((SpinnerNumberModel)spinnerFirstFrame.getModel()).getNumber().intValue();

            ((SpinnerNumberModel)spinnerLastFrame.getModel()).setMinimum(firstFrame);	
            model.setFirstFrame(firstFrame - 1);
        }
        else if(source == spinnerLastFrame)
        {
            int lastFrame = ((SpinnerNumberModel)spinnerLastFrame.getModel()).getNumber().intValue();
            ((SpinnerNumberModel)spinnerFirstFrame.getModel()).setMaximum(lastFrame);
            model.setLastFrame(lastFrame - 1);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent event) 
    {
        Object source = event.getSource();
        if(source == boxBackwards)
        {
            boolean selected = (event.getStateChange()== ItemEvent.SELECTED);
            model.setPlayedBackwards(selected);
        }
        else if (source == comboCompression) 
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
        else if(FIRST_FRAME.equals(name))
        {
            int newVal = ((Number)evt.getNewValue()).intValue();

            spinnerFirstFrame.setValue(newVal + 1);			
            ((SpinnerNumberModel)spinnerLastFrame.getModel()).setMinimum(newVal + 1);
        }
        else if(LAST_FRAME.equals(name))
        {
            int newVal = ((Number)evt.getNewValue()).intValue();

            spinnerLastFrame.setValue(newVal + 1);
            ((SpinnerNumberModel)spinnerFirstFrame.getModel()).setMaximum(newVal + 1);
        }
        else if(FRAME_COUNT.equals(name))
        {
            int newVal = ((Number)evt.getNewValue()).intValue();
            labelFrameCount.setText(Integer.toString(newVal));
            ((SpinnerNumberModel)spinnerLastFrame.getModel()).setMaximum(newVal);
        }
        else if(PLAYED_BACKWARDS.equals(name))
        {
            boolean oldValue = boxBackwards.isSelected();
            boolean newVal = (boolean)evt.getNewValue();

            if(oldValue != newVal)
            {
                boxBackwards.setSelected(newVal);
            }

        }
    }
}
