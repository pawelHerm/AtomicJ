
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
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.JFreeChart;
import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.IOUtilities;

public class SimpleChartSaveModel extends SimpleSaveModel<ChartSaveFormatType>
{
    private JFreeChart chartToSave;

    public SimpleChartSaveModel()
    {
        this(Arrays.asList(new EPSFormatType(), new PSFormatType(), new PDFFormatType(), 
                new SVGFormatType(), new TIFFFormatType(), new EMFFormatType(),
                new JPEGFormatType(), new JPEG2000FormatType(), new PNGFormatType(),
                new GIFFormatType(), new PPMFormatType(),new BMPFormatType(),
                new CSVFormatType(), new TSVFormatType()));
    }

    public SimpleChartSaveModel(List<ChartSaveFormatType> formatTypes)
    {
        super(formatTypes);
    }

    @Override
    public void save()  throws UserCommunicableException
    {
        File outputFile = getOutputFile();
        ChartSaveFormatType currentFormatType = getSaveFormat();
        ChartSaver saver = currentFormatType.getChartSaver();

        try 
        {
            boolean inArchive = isSaveInArchive() && saver instanceof ZippableFrameFormatSaver;
            if(inArchive)
            {
                String entryName = IOUtilities.getBareName(outputFile) + "." + currentFormatType.getExtension();

                ((ZippableFrameFormatSaver)saver).saveAsZip(this.chartToSave, outputFile, entryName, null);
            }
            else
            {
                saver.saveChart(this.chartToSave, outputFile, null);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured during saving the file");
        }
    }

    public void specifyChartToSave(JFreeChart chartToSave, Rectangle2D chartArea, Rectangle2D dataArea)
    {
        this.chartToSave = chartToSave;
        double dataWidth = dataArea.getWidth();
        double dataHeight = dataArea.getHeight();

        List<ChartSaveFormatType> formatTypes = getFormatTypes();
        for(ChartSaveFormatType type: formatTypes)
        {
            type.specifyInitialDimensions(chartArea, dataWidth, dataHeight);
        }
    }
}
