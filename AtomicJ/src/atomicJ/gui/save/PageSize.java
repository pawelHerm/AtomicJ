
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

import org.freehep.graphicsio.PageConstants;

import com.lowagie.text.Rectangle;

public enum PageSize 
{
    CUSTOM("Custom", null),
    A0("A0",com.lowagie.text.PageSize.A0), 
    A1("A1",com.lowagie.text.PageSize.A1), 
    A2("A2",com.lowagie.text.PageSize.A2), 
    A3("A3",com.lowagie.text.PageSize.A3), 
    A4("A4", com.lowagie.text.PageSize.A4), 
    A5("A5",com.lowagie.text.PageSize.A5), 
    A6("A6",com.lowagie.text.PageSize.A6),
    B0("B0",com.lowagie.text.PageSize.B0), 
    B1("B1",com.lowagie.text.PageSize.B1), 
    B2("B2",com.lowagie.text.PageSize.B2), 
    B3("B3",com.lowagie.text.PageSize.B3), 
    B4("B4", com.lowagie.text.PageSize.B4), 
    B5("B5",com.lowagie.text.PageSize.B5), 
    B6("B6",com.lowagie.text.PageSize.B6),
    EXECUTIVE("Executive",com.lowagie.text.PageSize.EXECUTIVE), 
    INTERNATIONAL("International", new Rectangle((float)PageConstants.getSize(PageConstants.INTERNATIONAL).getWidth(),(float)PageConstants.getSize(PageConstants.INTERNATIONAL).getHeight())), 
    LEGAL("Legal",com.lowagie.text.PageSize.LEGAL),
    LETTER("Letter", com.lowagie.text.PageSize.LETTER),
    LEDGER("Ledger",com.lowagie.text.PageSize.LEDGER.rotate()) ;


    private final String name;
    private final Rectangle rectangle;

    PageSize(String name, Rectangle rectangle)
    {
        this.name = name;
        this.rectangle = rectangle;
    }

    public Rectangle getSize()
    {
        return rectangle;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
