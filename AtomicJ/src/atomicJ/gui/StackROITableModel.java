
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.table.AbstractTableModel;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.QuantityArray1DExpression;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitArray1DExpression;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.MultiMap;


public class StackROITableModel extends AbstractTableModel implements NumericalTableModel
{
    private static final long serialVersionUID = 1L;

    private final List<IdentityTag> columnIds = new ArrayList<>();
    private final List<PrefixedUnit> dataUnits = new ArrayList<>(); 

    private final StandardUnitSource unitSource;

    private final List<double[]> data = new ArrayList<>(); //list of column data
    private final int rowCount;
    private final int columnCount;

    private final File defaultDir;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public StackROITableModel(Map<IdentityTag /*column - i.e. ROI*/, UnitArray1DExpression> samples, QuantityArray1DExpression stackLevels, File defaultDir)
    {
        this.defaultDir = defaultDir;

        columnIds.add(new IdentityTag("Frame"));
        dataUnits.add(null);


        Map<IdentityTag, PrefixedUnit> selectedUnits = new LinkedHashMap<>();
        MultiMap<IdentityTag, PrefixedUnit> unitGroups = new MultiMap<>();

        Quantity stackingQuantity = stackLevels.getQuantity();
        IdentityTag stackLevelColumnId = new IdentityTag(stackingQuantity.getName());
        PrefixedUnit stackingUnit = stackingQuantity.getUnit();

        columnIds.add(stackLevelColumnId);
        dataUnits.add(stackingUnit);
        data.add(stackLevels.getValues()); 

        selectedUnits.put(stackLevelColumnId, stackingUnit);

        if(stackingUnit != null)
        {
            unitGroups.putAll(stackLevelColumnId, stackingUnit.deriveUnits());
        }

        int rowCount = stackLevels.getValueCount();

        for(Entry<IdentityTag,UnitArray1DExpression> entry: samples.entrySet())
        {
            IdentityTag columnId = entry.getKey();
            UnitArray1DExpression columnValues = entry.getValue();
            PrefixedUnit unit = columnValues.getUnit();

            columnIds.add(columnId);
            dataUnits.add(unit);

            selectedUnits.put(columnId, unit);

            if(unit != null)
            {
                unitGroups.putAll(columnId, unit.deriveUnits());
            }

            data.add(columnValues.getValues());	
            rowCount = Math.max(rowCount, columnValues.getValueCount());
        }

        this.rowCount = rowCount;
        this.columnCount = columnIds.size();

        this.unitSource = new StandardUnitSource(unitGroups, selectedUnits);
        this.unitSource.setUseDefaultUnits(true);
        updateDefaultPrefics();
    }

    @Override
    public StandardUnitSource getUnitSource()
    {
        return unitSource;
    }

    private void updateDefaultPrefics()
    {  
        for(int i = 1; i<columnCount; i++)
        {
            PrefixedUnit defaultUnit = getDefultUnit(i);

            unitSource.setDefaultUnit(columnIds.get(i), defaultUnit);
        }
    }

    protected PrefixedUnit getDefultUnit(int columnIndex)
    {
        if(dataUnits.get(columnIndex) == null)
        {
            return null;
        }

        int rowCount = getRowCount();
        double[] values = new double[rowCount];

        for(int i = 0; i<rowCount; i++)
        {   
            Object value = getValueAt(i, columnIndex);
            if(value instanceof Number)
            {
                values[i] = Math.abs(((Number)value).doubleValue());   

            }
        }

        double median = DescriptiveStatistics.median(values);
        return dataUnits.get(columnIndex).getPreferredCompatibleUnit(median);
    }


    @Override
    public String getColumnName(int index)
    {
        if(index > -1 && index<columnCount)
        {
            IdentityTag id = columnIds.get(index);
            PrefixedUnit unit = unitSource.getSelectedUnit(id);
            String name = (unit != null) ? id.getLabel() + " (" + unit.getFullName() + ")" : id.getLabel();
            return name;
        }

        return "";
    }

    @Override
    public int getColumnIndex(IdentityTag columnId)
    {      
        int index = columnIds.indexOf(columnId);
        return index;
    }

    @Override
    public PrefixedUnit getDataUnit(IdentityTag columnId)
    {
        int index = columnIds.indexOf(columnId);
        return dataUnits.get(index);
    }

    @Override
    public List<PrefixedUnit> getDataUnits()
    {
        return new ArrayList<>(dataUnits);
    }

    @Override
    public List<PrefixedUnit> getDisplayedUnits()
    {
        List<PrefixedUnit> units = new ArrayList<>(Collections.<PrefixedUnit>nCopies(columnIds.size(), null));

        for(Entry<IdentityTag, PrefixedUnit> entry : unitSource.getSelectedUnits().entrySet())
        {
            int index = columnIds.indexOf(entry.getKey());
            units.set(index, entry.getValue());
        }

        return units;
    }

    @Override
    public List<IdentityTag> getColumnShortNames()
    {
        return new ArrayList<>(columnIds);
    }

    @Override
    public File getDefaultOutputDirectory()
    {
        return defaultDir;
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = (rowCount < 1 || columnCount <2);
        return empty;
    }


    @Override
    public Class<?> getColumnClass(int n)
    {
        Class<?> columnClass = (n == 0) ? Integer.class : Double.class;
        return columnClass;
    }

    @Override
    public int getColumnCount() 
    {
        return columnCount;
    }

    @Override
    public int getRowCount() 
    {
        return rowCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) 
    {
        if(columnIndex == 0)
        {
            return rowIndex;
        }

        double[] values = data.get(columnIndex - 1);
        int n = values.length;

        Object value = (rowIndex<n) ? values[rowIndex]: null;
        return value;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) 
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) 
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) 
    {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
