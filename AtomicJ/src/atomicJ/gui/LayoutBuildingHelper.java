
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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class LayoutBuildingHelper 
{
    private LayoutBuildingHelper(){};
    public final static void addComponent(Container ct, Component c, int x, int y, int w, int h, int anchor, int fill, double weightx, double weighty, Insets insets)
    {	
        GridBagConstraints directives = new GridBagConstraints();
        directives.gridx = x;
        directives.gridy = y;
        directives.gridwidth = w;
        directives.gridheight = h;
        directives.weightx = weightx;
        directives.weighty = weighty;
        directives.anchor = anchor;
        directives.fill = fill;
        directives.insets = insets;
        ct.add(c, directives);		
    }
    public final static void addComponent(Container ct, Component c, int x, int y, int w, int h, int anchor, int fill, double weightx, double weighty)
    {
        addComponent(ct, c, x, y, w, h, anchor, fill, weightx, weighty, new Insets(3,3,3,3));
    }
}
