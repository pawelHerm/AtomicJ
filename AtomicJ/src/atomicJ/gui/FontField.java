
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

package atomicJ.gui;

import java.awt.Font;
import javax.swing.JTextField;


public class FontField extends JTextField 
{
    private static final long serialVersionUID = 1L;
    private Font displayFont;

    public FontField() 
    {
        this(null);
    }

    public FontField(Font font) 
    {
        super();
        setDisplayFont(font);
        setEnabled(false);
    }

    public Font getDisplayFont() 
    {
        return this.displayFont;
    }

    public void setDisplayFont(Font font) 
    {
        this.displayFont = font;
        setText(fontToString(this.displayFont));
    }

    private String fontToString(Font font) 
    {
        if (font != null) {
            return font.getFontName() + ", " + font.getSize();
        }
        else
        {
            return "";
        }
    }

}