
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

import java.awt.geom.Rectangle2D;

import atomicJ.gui.AbstractModel;
import atomicJ.utilities.Validation;



public class BasicFormatModel extends AbstractModel 
{
    private double initialDataWidth;
    private double initialDataHeight;

    private Rectangle2D chartInitialArea;

    private boolean saveDataArea;
    private Double width;
    private Double height;
    private boolean aspectConstant;
    private double aspectRatio;

    private boolean dimensionsSpecified;

    public BasicFormatModel() 
    {
        initDefaults();
    }

    private void initDefaults() 
    {
        this.saveDataArea = false;
        this.width = Double.NaN;
        this.height = Double.NaN;
        this.aspectConstant = true;
        this.dimensionsSpecified = false;
    }

    public void specifyInitialDimensions(Rectangle2D chartInitialArea, double initialDataWidth, double initialDataHeight)
    {
        this.initialDataWidth = initialDataWidth;
        this.initialDataHeight = initialDataHeight;
        this.chartInitialArea = chartInitialArea;

        double width = saveDataArea ? initialDataWidth : chartInitialArea.getWidth();
        double height = saveDataArea ? initialDataHeight : chartInitialArea.getHeight();

        setWidth(width);
        setHeight(height);
        recalculateAspectRatio();
        checkIfDimensionsSpecified();
    }

    public void specifyDimensions(Number widthNew, Number heightNew) 
    {
        Validation.requireNonNullParameterName(widthNew, "widthNew");
        Validation.requireValueGreaterThanParameterName(widthNew.doubleValue(), 0, "widthNew");

        Validation.requireNonNullParameterName(heightNew, "heightNew");
        Validation.requireValueGreaterThanParameterName(heightNew.doubleValue(), 0, "heightNew");

        setWidth(widthNew);
        setHeight(heightNew);
        recalculateAspectRatio();
        checkIfDimensionsSpecified();
    }

    public double getInitialDataWidth()
    {
        return initialDataWidth;
    }

    public double getInitialDataHeight()
    {
        return initialDataHeight;
    }

    public Rectangle2D getChartInitialArea()
    {
        return chartInitialArea;
    }

    public double getInitialChartWidth()
    {
        return chartInitialArea.getWidth();
    }

    public double getInitialChartHeight()
    {
        return chartInitialArea.getHeight();
    }

    public double getWidth() 
    {
        return width;
    }

    public void specifyWidth(Number widthNew) 
    {
        Validation.requireNonNullParameterName(widthNew, "widthNew");
        Validation.requireValueGreaterThanParameterName(widthNew.doubleValue(), 0, "widthNew");

        setWidth(widthNew);
        setHeightConsistentWithWidth();
        checkIfDimensionsSpecified();
    }

    private void setWidth(Number widthNew) 
    {
        Double widthOld = width;
        this.width = widthNew.doubleValue();

        firePropertyChange(SAVED_AREA_WIDTH, widthOld, widthNew);
    }

    public double getHeight() 
    {
        return height;
    }

    public void specifyHeight(Number heightNew) 
    {
        Validation.requireNonNullParameterName(heightNew, "heightNew");
        Validation.requireValueGreaterThanParameterName(heightNew.doubleValue(), 0, "heightNew");

        setHeight(heightNew);
        setWidthConsistentWithHeight();
        checkIfDimensionsSpecified();
    }

    private void setHeight(Number heightNew) 
    {
        Double heightOld = height;
        this.height = heightNew.doubleValue();

        firePropertyChange(SAVED_AREA_HEIGHT, heightOld, heightNew);
    }

    private void setWidthConsistentWithHeight() 
    {
        if (aspectConstant && !height.isNaN()) 
        {
            double h = height.intValue();
            double w = width.intValue();
            double wNew = Math.round((h * aspectRatio));
            if (Math.abs(wNew - w) > 0.5) 
            {
                setWidth(wNew);
            }
        }
    }

    private void setHeightConsistentWithWidth() 
    {
        if (aspectConstant && !width.isNaN()) 
        {
            double w = width.intValue();
            double h = height.intValue();
            int hNew = (int) Math.round(w / aspectRatio);
            if (Math.abs(hNew - h) > 0.5) 
            {
                setHeight(hNew);
            }
        }
    }

    public void recalculateAspectRatio() 
    {
        aspectRatio = width / height;
    }

    public boolean isAspectRatioConstant() 
    {
        return aspectConstant;
    }

    public void setAspectRatioConstant(boolean aspectConstantNew) 
    {
        boolean aspectConstantOld = aspectConstant;
        this.aspectConstant = aspectConstantNew;

        firePropertyChange(ASPECT_CONSTANT, aspectConstantOld, aspectConstantNew);

        recalculateAspectRatio();
    }

    public boolean getSaveDataArea()
    {
        return saveDataArea;
    }

    public void setSaveDataArea(boolean saveDataAreaNew) 
    {
        if(saveDataAreaNew != saveDataArea)
        {
            boolean saveDataAreaOld = saveDataArea;
            this.saveDataArea = saveDataAreaNew;

            updateSaveAreaSize();

            firePropertyChange(SAVE_DATA_AREA, saveDataAreaOld, saveDataAreaNew);
        }	
    }

    private void updateSaveAreaSize()
    {
        double width = saveDataArea ? initialDataWidth : chartInitialArea.getWidth();
        double height = saveDataArea ? initialDataHeight : chartInitialArea.getHeight();

        setWidth(width);
        setHeight(height);		
        recalculateAspectRatio();
    }

    public boolean areDimensionsSpecified() 
    {
        return dimensionsSpecified;
    }

    private boolean checkIfDimensionsSpecified() 
    {
        boolean dimensionsSpecifiedNew = !width.isNaN() && !height.isNaN();
        boolean dimensionsSpecifiedOld = dimensionsSpecified;
        this.dimensionsSpecified = dimensionsSpecifiedNew;

        firePropertyChange(DIMENSIONS_SPECIFIED, dimensionsSpecifiedOld, dimensionsSpecifiedNew);
        firePropertyChange(FORMAT_PARAMETERS_PROVIDED, dimensionsSpecifiedOld, dimensionsSpecifiedNew);

        return dimensionsSpecifiedNew;
    }
}