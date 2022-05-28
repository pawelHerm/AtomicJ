
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


public class TIFFFormatType implements ChartSaveFormatType
{
    private static final String DESCRIPTION = "TIFF image (.tiff)";
    private static final String EXTENSION = "tiff";

    private final TIFFFormatModel model = new TIFFFormatModel();
    private final TIFFFormatPanel panel = new TIFFFormatPanel(model);
    private static final FileNameExtensionFilter FILTER = new FileNameExtensionFilter(DESCRIPTION,EXTENSION);

    @Override
    public boolean supportMultiplePages()
    {
        return false;
    }

    @Override
    public ChartSaver getChartSaver() 
    {
        boolean saveDataArea = model.getSaveDataArea();

        Rectangle2D chartInitialArea = model.getChartInitialArea();

        int width = (int) model.getWidth();
        int height = (int) model.getHeight();
        TIFFCompressionMethod compression = model.getCompressionMethod();
        TIFFFormatSaver saver = new TIFFFormatSaver(compression, chartInitialArea, width, height, saveDataArea);
        return saver;
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
