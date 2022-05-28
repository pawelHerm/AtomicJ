
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

import javax.swing.*;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.SubPanel;
import atomicJ.gui.stack.StackModel;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

import static atomicJ.gui.save.SaveModelProperties.*;

public class MovieSaveDialog extends SimpleSaveDialog<MovieSaveFormatType, MovieSaveModel> 
{
    private static final long serialVersionUID = 1L;

    private static final String DIALOG_TITLE = "Save frames";

    private final JCheckBox boxSaveInArchive;

    private MovieSaveDialog(JCheckBox boxSaveInArchive, GridBagConstraints boxSaveInArchiveLayoutConstraints, Window parent, JPanel parentPanel) 
    {
        super(new MovieSaveModel(Arrays.asList(new AVIFormatType(), new TIFFMovieFormatType())), DIALOG_TITLE, 
                Arrays.<Map.Entry<JComponent, GridBagConstraints>>asList(new AbstractMap.SimpleEntry<JComponent, GridBagConstraints>(boxSaveInArchive, boxSaveInArchiveLayoutConstraints)),
                parent, parentPanel, "Movie");

        this.boxSaveInArchive = boxSaveInArchive;
        pullModelProperties();
        initItemListener();
    }

    public static MovieSaveDialog getInstance(Window parent, JPanel parentPanel)
    {
        JCheckBox boxSaveInArchive = new JCheckBox("Compress in ZIP archive");
        GridBagConstraints boxSaveInArchiveLayoutConstraints = SubPanel.buildConstraints(1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));
        MovieSaveDialog dialog = new MovieSaveDialog(boxSaveInArchive, boxSaveInArchiveLayoutConstraints, parent, parentPanel) ;

        return dialog;
    }

    public void showDialog(Channel2DChart<?> chart, StackModel<?> stackModel, Rectangle2D chartArea, Rectangle2D dataArea) 
    {
        getModel().specifyChartToSave(chart, chartArea, dataArea, stackModel);
        showDialog();
    }

    private void pullModelProperties() 
    {
        MovieSaveModel model = getModel();

        boolean saveInArchive = model.isSaveInArchive();
        boolean saveInArchiveEnabled = model.isSaveInArchiveEnabled();

        boxSaveInArchive.setSelected(saveInArchive);
        boxSaveInArchive.setEnabled(saveInArchiveEnabled);
    }

    private void initItemListener() 
    {
        boxSaveInArchive.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setSaveInArchive(selected);
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(SAVE_IN_ARCHIVE.equals(property))
        {
            boolean saveInArchiveNew = (boolean)evt.getNewValue();
            boolean saveInArchiveOld = boxSaveInArchive.isSelected();

            if(saveInArchiveOld != saveInArchiveNew)
            {
                boxSaveInArchive.setSelected(saveInArchiveNew);
            }            
        }
        else if(SAVE_IN_ARCHIVE_ENABLED.equals(property))
        {
            boolean saveInArchiveEnabledNew = (boolean)evt.getNewValue();
            boxSaveInArchive.setEnabled(saveInArchiveEnabledNew);
        }          
    }
}
