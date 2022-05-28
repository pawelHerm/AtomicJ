
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.gui.statistics;


import java.awt.Dimension;
import java.io.File;

import atomicJ.gui.MinimalNumericalTable;


public class InferencesTable extends MinimalNumericalTable
{	
    private static final long serialVersionUID = 1L;

    private final InferenceTableCellRenderer decimalRenderer;

    public InferencesTable(InferencesTableModel model)
    {
        super(model, false, true);

        this.decimalRenderer = new InferenceTableCellRenderer();
        decimalRenderer.setPreferences(getPreferences());
        setDefaultRenderer(Object.class, decimalRenderer);    

        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setIntercellSpacing(new Dimension(0,1));
        getTableHeader().setReorderingAllowed(false);
    }

    /*
	public Dimension getPreferredScrollableViewportSize()
	{
		Dimension superDim = super.getPreferredScrollableViewportSize();
		Dimension tableDim = getPreferredSize();
		int width = superDim.width;
		int height = tableDim.height;

		Dimension preferredDim = new Dimension(width, height);

		return preferredDim;
	}
     */

    @Override
    public InferenceTableCellRenderer getDecimalCellRenderer()
    {
        return decimalRenderer;
    }

    public boolean areChangesSaved()
    {
        InferencesTableModel model = (InferencesTableModel)getModel();
        return model.areChangesSaved();
    }

    @Override
    public void setSaved(boolean saved)
    {
        InferencesTableModel model = (InferencesTableModel)getModel();
        model.setSaved(saved);
    }

    @Override
    public boolean isEmpty()
    {
        InferencesTableModel model = (InferencesTableModel)getModel();
        return model.isEmpty();
    }

    @Override
    public File getDefaultOutputDirectory() 
    {
        InferencesTableModel model = (InferencesTableModel)getModel();
        return model.getDefaultOutputDirectory();
    }
}

