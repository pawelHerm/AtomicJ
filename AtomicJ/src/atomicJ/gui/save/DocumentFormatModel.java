
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

import java.awt.Dimension;
import java.util.Properties;
import org.freehep.graphicsio.ps.AbstractPSGraphics2D;
import org.freehep.util.UserProperties;

public class DocumentFormatModel extends BasicFormatModel
{
    private boolean fitToPage = true; 
    private PageOrientation orientation = PageOrientation.PORTRAIT;
    private PageSize size = PageSize.CUSTOM;
    private PageMargins margins = PageMargins.NONE;

    private final UserProperties properties = new UserProperties();

    public DocumentFormatModel()
    {
        updateProperties();
    }

    public boolean isFitToPage()
    {
        return fitToPage;
    }

    public void setFitToPage(boolean fitToPageNew) 
    {
        boolean fitToPageOld = fitToPage;
        this.fitToPage = fitToPageNew;

        properties.setProperty(AbstractPSGraphics2D.FIT_TO_PAGE, fitToPageNew);

        firePropertyChange(FIT_TO_PAGE, fitToPageOld, fitToPageNew);
    }

    public PageOrientation getPageOrientation()
    {
        return orientation;
    }

    public void setPageOrientation(PageOrientation orientationNew) 
    {
        PageOrientation orientationOld = orientation;
        this.orientation = orientationNew;

        firePropertyChange(PAGE_ORIENTATION, orientationOld, orientationNew);
    }

    public PageSize getPageSize()
    {
        return size;
    }

    public void setPageSize(PageSize sizeNew) 
    {
        PageSize sizeOld = size;
        this.size = sizeNew;

        firePropertyChange(PAGE_SIZE, sizeOld, sizeNew);
    }

    public PageMargins getPageMargins()
    {
        return margins;
    }

    public void setPageMargins(PageMargins marginsNew)
    {
        PageMargins marginsOld = margins;
        this.margins = marginsNew;

        firePropertyChange(PAGE_MARGINS, marginsOld, marginsNew);
    }

    private void updateProperties()
    {
        properties.setProperty(AbstractPSGraphics2D.FIT_TO_PAGE, fitToPage);
        properties.setProperty(AbstractPSGraphics2D.ORIENTATION, orientation.toString());
        properties.setProperty(AbstractPSGraphics2D.PAGE_SIZE, AbstractPSGraphics2D.CUSTOM_PAGE_SIZE);

        if(size.equals(PageSize.CUSTOM))
        {
            properties.setProperty(AbstractPSGraphics2D.CUSTOM_PAGE_SIZE, new Dimension((int)getWidth(), (int)getHeight()));
        }
        else
        {
            properties.setProperty(AbstractPSGraphics2D.CUSTOM_PAGE_SIZE, new Dimension((int)size.getSize().getWidth(), (int)size.getSize().getHeight()));		
        }
        properties.setProperty(AbstractPSGraphics2D.PAGE_MARGINS, margins.getInsets());
    }


    public Properties getProperties()
    {
        updateProperties();
        return properties;
    }
}
