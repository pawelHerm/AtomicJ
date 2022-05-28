
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

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class SingleChartPresentationDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private final MultipleChartPanel<CustomizableXYBaseChart<?>> chartPanel = new MultipleChartPanel<>();

    public SingleChartPresentationDialog(Window parent, CustomizableXYBaseChart<?> chart, String title)
    {
        super(parent,title, ModalityType.MODELESS);	
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        chartPanel.setSelectedChart(chart);
        add(chartPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        pack();
        setLocationRelativeTo(parent);
    }

    public SingleChartPresentationDialog(Window parent, CustomizableXYBaseChart<?> chart, JPanel addInfoPanel, String title)
    {
        super(parent,title, ModalityType.MODELESS);	

        JPanel outerPanel = new JPanel(new BorderLayout(1, 5));

        chartPanel.setSelectedChart(chart);
        outerPanel.add(chartPanel,BorderLayout.CENTER);
        outerPanel.add(addInfoPanel,BorderLayout.EAST);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(outerPanel);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        pack();
        setLocationRelativeTo(parent);
    }
}
