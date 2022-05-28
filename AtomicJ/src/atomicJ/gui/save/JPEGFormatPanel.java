
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.SubPanel;


public class JPEGFormatPanel extends SubPanel implements PropertyChangeListener, ChangeListener 
{
    private static final long serialVersionUID = 1L;

    private BasicImageFormatPanel basicPanel;

    private final JSpinner spinnerQuality = new JSpinner(new SpinnerNumberModel(0.5, 0., 1., 0.1));

    private JPEGFormatModel model;

    public JPEGFormatPanel(JPEGFormatModel model) 
    {
        setModel(model);
        buildLayout();
        initChangeListener();
    }

    public JPEGFormatModel getModel() 
    {
        return model;
    }

    public void setModel(JPEGFormatModel modelNew) 
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
        //we need to convert float value returned by model.getQuality() to double, otherwise there are problems on Linux OpenJDK
        double saveQuality = model.getQuality();
        spinnerQuality.setValue(saveQuality);
    }

    private void buildLayout() 
    {
        JPanel panelJPEG = builJPEGPanel();
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

    private JPanel builJPEGPanel()
    {
        SubPanel panelJPEG = new SubPanel();
        panelJPEG.addComponent(new JLabel("Quality"), 0, 0, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 1);
        panelJPEG.addComponent(spinnerQuality, 1, 0, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 1, 1);

        return panelJPEG;
    }

    private void initChangeListener() 
    {
        spinnerQuality.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent event) 
    {
        Object source = event.getSource();
        if (source == spinnerQuality) 
        {
            float encodingRateNew = ((SpinnerNumberModel) spinnerQuality.getModel()).getNumber().floatValue();
            model.setQuality(encodingRateNew);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if (name.equals(QUALITY)) 
        {
            float newVal = (float) evt.getNewValue();
            float oldVal = ((SpinnerNumberModel)spinnerQuality.getModel()).getNumber().floatValue();
            if (oldVal != newVal) 
            {
                spinnerQuality.setValue(newVal);
            }
        } 
    }

}
