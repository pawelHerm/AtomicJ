
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class SubPanel extends JPanel 
{
    private static final long serialVersionUID = 1L;

    public SubPanel()
    {
        setLayout(new GridBagLayout());
    };

    public void addComponent(Component c, int x, int y, int w, int h, int anchor, int fill, double weightx, double weighty, Insets insets,int ipadx, int ipady)
    {	
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.insets = insets;
        constraints.ipadx = ipadx;
        constraints.ipady = ipady;
        add(c, constraints);		
    }

    public void addComponent(Component c, int x, int y, int w, int h, int anchor, int fill, double weightx, double weighty, int ipadx, int ipady)
    {	
        addComponent(c, x, y, w, h, anchor, fill, weightx, weighty, new Insets(0,0,0,0), ipadx, ipady);
    }

    public void addComponent(Component c, int x, int y, int w, int h, int anchor, int fill, double weightx, double weighty, Insets insets)
    {	
        addComponent(c, x, y, w, h, anchor, fill, weightx, weighty, insets, 0, 0);
    }

    public void addComponent(Component c, int x, int y, int w, int h, int anchor, int fill, double weightx, double weighty)
    {
        addComponent(c, x, y, w, h, anchor, fill, weightx, weighty, new Insets(3,3,3,3));
    }

    public int getMaxRow()
    {
        LayoutManager layout = getLayout();

        int maxRow = -1;

        if(layout instanceof GridBagLayout)
        {
            for (Component comp : getComponents()) 
            {
                GridBagConstraints gbc = ((GridBagLayout)layout).getConstraints(comp);
                int currentRow = gbc.gridy;

                if(currentRow > maxRow)
                {
                    maxRow = currentRow;
                }
            }
        }

        return maxRow;
    }

    public static GridBagConstraints buildConstraints(int x, int y, int w, int h, int anchor, int fill, double weightx, double weighty, Insets insets)
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.insets = insets;
        constraints.ipadx = 0;
        constraints.ipady = 0;

        return constraints;
    }
}
