
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

import java.awt.Insets;
import java.awt.geom.Rectangle2D;

import javax.swing.filechooser.FileNameExtensionFilter;


public class PDFFormatType extends AbstractDocumentFormatType
{
    private static final String DESCRIPTION = "Portable Document Format (.pdf)";
    private static final String EXTENSION = "pdf";
    private static final FileNameExtensionFilter FILTER = new FileNameExtensionFilter(DESCRIPTION,EXTENSION);

    @Override
    public ChartSaver getChartSaver() 
    {
        DocumentFormatModel model = getModel();

        boolean saveDataArea = model.getSaveDataArea();
        Rectangle2D chartInitialArea = model.getChartInitialArea();

        int width = (int) model.getWidth();
        int height = (int) model.getHeight();
        Insets margins = model.getPageMargins().getInsets();
        com.lowagie.text.Rectangle pageSize = model.getPageSize().getSize();

        if(pageSize == null)
        {
            pageSize = new com.lowagie.text.Rectangle(width + margins.left + margins.right, height + margins.bottom + margins.top);
        }
        com.lowagie.text.Rectangle pageSizeFinal;
        PageOrientation pageOrientation = model.getPageOrientation();

        if(pageOrientation.equals(PageOrientation.LANDSCAPE))
        {
            pageSizeFinal = pageSize.rotate();
        }
        else
        {
            pageSizeFinal = pageSize;
        }

        int widthFinal;
        int heightFinal;

        boolean fitToPage = model.isFitToPage();

        if(fitToPage)
        {
            widthFinal = (int)(pageSizeFinal.getRight() - margins.right - margins.left);
            heightFinal = (int)(pageSizeFinal.getTop() - margins.top - margins.bottom);
        }
        else
        {
            widthFinal = width;
            heightFinal = height;
        }


        PDFFormatSaver saver = new PDFFormatSaver(chartInitialArea, widthFinal, heightFinal, pageSizeFinal, margins, saveDataArea);
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
    public FileNameExtensionFilter getFileNameExtensionFilter() 
    {
        return FILTER;
    }
}
