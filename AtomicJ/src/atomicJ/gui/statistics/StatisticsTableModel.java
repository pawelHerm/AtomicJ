
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

package atomicJ.gui.statistics;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.table.DefaultTableModel;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.NumericalTableModel;
import atomicJ.gui.MinimalNumericalTable;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.MultiMap;


public abstract class StatisticsTableModel extends DefaultTableModel implements NumericalTableModel
{
    private static final long serialVersionUID = 1L;

    private static final List<IdentityTag> COLUMN_IDS = Arrays.asList(
            new IdentityTag("Sample"),new IdentityTag("Count"), new IdentityTag("Mean"),new IdentityTag("Trim 5%"),new IdentityTag("Median"),new IdentityTag("Q1"),
            new IdentityTag("Q3"),new IdentityTag("SD"), new IdentityTag("SE"), new IdentityTag("IQR"), new IdentityTag("Skewness"),new IdentityTag("Kurtosis"));


    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final List<PrefixedUnit> dataUnits = new ArrayList<>();

    private final PrefixedUnit dataUnit;
    private final File defaultOutputFile;
    private boolean empty = isEmpty();

    private final StandardUnitSource unitSource;

    public StatisticsTableModel(File defaultOutputFile, PrefixedUnit unit)
    {  
        setColumnCount(COLUMN_IDS.size());
        this.defaultOutputFile = defaultOutputFile;
        this.dataUnit = unit;

        dataUnits.addAll(Collections.<PrefixedUnit>nCopies(COLUMN_IDS.size(), null));
        for(int i = 2; i<10; i++)
        {
            dataUnits.set(i, unit);
        }

        this.unitSource = buildUnitSource();
        this.unitSource.setUseDefaultUnits(true);
    }

    @Override
    public StandardUnitSource getUnitSource()
    {
        return unitSource;
    }

    @Override
    public String getColumnName(int index)
    {
        IdentityTag id = COLUMN_IDS.get(index);
        PrefixedUnit unit = unitSource.getSelectedUnit(id);
        String name = (unit != null) ? id.getLabel() + " (" + unit.getFullName() + ")" : id.getLabel();
        return name;
    }

    @Override
    public int getColumnIndex(IdentityTag columnId)
    {      
        int index = COLUMN_IDS.indexOf(columnId);
        return index;
    }

    @Override
    public PrefixedUnit getDataUnit(IdentityTag columnId)
    {
        int index = COLUMN_IDS.indexOf(columnId);
        return dataUnits.get(index);
    }

    protected void updateDefaultPrefix()
    {
        PrefixedUnit defaultUnit = getDefultUnit();

        for(int i = 2; i<10; i++)
        {
            unitSource.setDefaultUnit(COLUMN_IDS.get(i), defaultUnit);
        }
    }

    protected PrefixedUnit getDefultUnit()
    {
        int rowCount = getRowCount();
        int unitValuesCount = 8*rowCount;
        double[] values = new double[unitValuesCount];

        int index = 0;
        for(int i = 0; i<rowCount; i++)
        {
            for(int j = 2; j<10;j++)
            {
                values[index++] = ((Number)getValueAt(i, j)).doubleValue();   
            }
        }

        return UnitUtilities.getPreferredPrefix(values, dataUnit);
    }

    private StandardUnitSource buildUnitSource()
    {
        Map<IdentityTag, PrefixedUnit> selectedUnits = new LinkedHashMap<>();
        for(int i = 2; i<10;i++)
        {
            selectedUnits.put(COLUMN_IDS.get(i), dataUnit);
        }


        MultiMap<IdentityTag, PrefixedUnit> unitGroups = new MultiMap<>();

        List<PrefixedUnit> units = dataUnit.deriveUnits();
        for(int i = 2; i<10;i++)
        {
            unitGroups.putAll(COLUMN_IDS.get(i), units);
        }

        StandardUnitSource unitSource = new StandardUnitSource(unitGroups, selectedUnits);

        return unitSource;
    }

    @Override
    public File getDefaultOutputDirectory()
    {
        return defaultOutputFile;
    }

    protected PrefixedUnit getUnit()
    {
        return dataUnit;
    }

    @Override
    public List<PrefixedUnit> getDataUnits()
    {
        return new ArrayList<>(dataUnits);
    }

    @Override
    public List<PrefixedUnit> getDisplayedUnits()
    {
        List<PrefixedUnit> units = new ArrayList<>(Collections.<PrefixedUnit>nCopies(COLUMN_IDS.size(), null));

        for(Entry<IdentityTag, PrefixedUnit> entry : unitSource.getSelectedUnits().entrySet())
        {
            int index = COLUMN_IDS.indexOf(entry.getKey());
            units.set(index, entry.getValue());
        }

        return units;
    }

    protected abstract Set<Object> getAvailableSamples();

    @Override
    public boolean isEmpty()
    {
        Vector<?> data = getDataVector();
        return data.isEmpty();
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int n)
    {
        if(n == 0)
        {
            return String.class;
        }
        else if(n == 1)
        {
            return Integer.class;
        }	
        return Double.class;
    }

    @Override
    public List<IdentityTag> getColumnShortNames()
    {
        return new ArrayList<>(COLUMN_IDS);
    }

    protected void clearData()
    {
        getDataVector().removeAllElements();
        fireTableDataChanged();
    }

    protected Object[] convertDatasetToRows(Object sampleKey, String sampleName, DescriptiveStatistics stats)
    {			
        Object[] row = new Object[12];

        row[0] = new IdentityTag(sampleKey, sampleName);

        row[1] = stats.getSize();
        row[2] = stats.getArithmeticMean();	
        row[3] = stats.getTrimmedArithmeticMean();
        row[4] = stats.getMedian();
        row[5] = stats.getLowerQuartile();
        row[6] = stats.getUpperQuartile();
        row[7] = stats.getStandardDeviation();
        row[8] = stats.getStandardError();
        row[9] = stats.getInterquartileLength();
        row[10] = stats.getSkewness();
        row[11] = stats.getKurtosis();

        return row;	
    }

    protected void checkIfEmpty()
    {
        boolean emptyOld = empty;
        boolean emptyNew = isEmpty();
        this.empty = emptyNew;

        firePropertyChange(MinimalNumericalTable.TABLE_EMPTY, emptyOld, emptyNew);
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
