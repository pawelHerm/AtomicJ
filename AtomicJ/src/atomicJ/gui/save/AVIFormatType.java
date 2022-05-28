
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

import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;


public class AVIFormatType implements MovieSaveFormatType
{
    private static final String DESCRIPTION = "AVI movie (.avi)           ";
    private static final String EXTENSION = "avi";

    private final AVIFormatModel model = new AVIFormatModel();
    private final AVIFormatPanel panel = new AVIFormatPanel(model);
    private static final FileNameExtensionFilter FILTER = new FileNameExtensionFilter(DESCRIPTION,EXTENSION);

    @Override
    public ChartSaver getChartSaver() 
    {
        return model.getChartSaver(panel);
    }

    @Override
    public boolean supportMultiplePages()
    {
        return false;
    }

    @Override
    public String getDescription() 
    {
        return DESCRIPTION;
    }

    @Override
    public String getExtension() 
    {
        return EXTENSION;
    }

    @Override 
    public String toString()
    {
        return DESCRIPTION;
    }

    @Override
    public void specifyInitialDimensions(Rectangle2D chartArea, Number dataWidthNew, Number dataHeightNew)
    {
        model.specifyInitialDimensions(chartArea, dataWidthNew.doubleValue(), dataHeightNew.doubleValue());
    }

    @Override
    public void specifyInitialMovieParameters(int firstFrame, int lastFrame, int frameCount, int frameRate, boolean playedBackwards)
    {
        model.specifyInitialMovieParameters(firstFrame, lastFrame, frameCount, frameRate, playedBackwards);
    }

    @Override
    public boolean isNecessaryIputProvided()
    {
        return model.areDimensionsSpecified();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        model.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        model.removePropertyChangeListener(listener);
    }

    @Override
    public JPanel getParametersInputPanel()
    {
        return panel;
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() 
    {
        return FILTER;
    }
}
