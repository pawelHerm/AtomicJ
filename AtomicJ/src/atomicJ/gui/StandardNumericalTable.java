
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

import java.util.List;
import javax.swing.table.TableModel;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.gui.units.UnitSelectionPanel;
import atomicJ.gui.units.UnitSourceAdapter;
import atomicJ.gui.units.UnitSourceListener;
import atomicJ.sources.IdentityTag;

public class StandardNumericalTable extends MinimalNumericalTable implements NumericalFormatStyle
{
    private static final long serialVersionUID = 1L;

    private DecimalUnitTableCellRenderer decimalRenderer;

    private final UnitSelectionPanel selectionPanel;
    private final UnitSourceListener unitSourceListener;

    public StandardNumericalTable(NumericalTableModel model, boolean addDefaultSorter, boolean addPopup)
    {
        this(model, new DecimalUnitTableCellRenderer(model.getDataUnits(), model.getDisplayedUnits()), addDefaultSorter, addPopup);
    }

    public StandardNumericalTable(NumericalTableModel model, DecimalUnitTableCellRenderer decimalRenderer, boolean addDefaultSorter, boolean addPopup)
    {
        super(model, addDefaultSorter, addPopup);

        this.decimalRenderer = decimalRenderer;
        decimalRenderer.setPreferences(getPreferences());
        setDefaultRenderer(Double.class, decimalRenderer);    

        this.unitSourceListener = buildUnitListener();
        StandardUnitSource unitSource = model.getUnitSource();
        unitSource.addUnitSourceListener(unitSourceListener);
        this.selectionPanel = new UnitSelectionPanel(model.getUnitSource());

        setColumnWidthToDefault();
    }


    @Override
    public DecimalUnitTableCellRenderer getDecimalCellRenderer()
    {
        return decimalRenderer;
    }

    public UnitSelectionPanel getUnitSelectionPanel()
    {
        return selectionPanel;
    }

    private UnitSourceListener buildUnitListener()
    {        
        UnitSourceListener listener = new UnitSourceAdapter()
        {

            @Override
            public void unitSelected(IdentityTag group, PrefixedUnit unit) 
            {
                NumericalTableModel model = getModel();
                int modelIndex = model.getColumnIndex(group);

                DecimalUnitTableCellRenderer renderer = getDecimalCellRenderer();
                renderer.setNewDisplayedUnit(modelIndex, unit);

                int viewIndex = convertColumnIndexToView(modelIndex);

                if(viewIndex > -1)
                {
                    getColumnModel().getColumn(viewIndex).setHeaderValue(model.getColumnName(modelIndex));

                    repaint();
                    getTableHeader().repaint();
                }       

            }

            @Override
            public void unitGroupAdded(IdentityTag group, PrefixedUnit selectedUnit, List<PrefixedUnit> units) 
            {
                NumericalTableModel model = getModel();

                PrefixedUnit dataUnit = model.getDataUnit(group);
                DecimalUnitTableCellRenderer renderer = getDecimalCellRenderer();

                renderer.registerNewUnit(dataUnit, selectedUnit);                
            }
        };

        return listener;
    }



    @Override
    public void setModel(TableModel model)
    {
        if(!(model instanceof NumericalTableModel))
        {
            throw new IllegalArgumentException("Standard numerical table requires NumericalTableModel");
        }

        NumericalTableModel modelOld = getModel();
        NumericalTableModel modelNew = (NumericalTableModel)model;
        if(modelOld != null)
        {
            modelOld.getUnitSource().removeUnitSourceListener(unitSourceListener);
        }

        super.setModel(model);

        if(this.selectionPanel != null)
        {
            StandardUnitSource unitSource = modelNew.getUnitSource();
            unitSource.addUnitSourceListener(unitSourceListener);
            this.selectionPanel.setModel(unitSource);

            this.decimalRenderer = new DecimalUnitTableCellRenderer(modelNew.getDataUnits(), modelNew.getDisplayedUnits());
            decimalRenderer.setPreferences(getPreferences());
            setDefaultRenderer(Double.class, decimalRenderer); 
        }
    }  
}
