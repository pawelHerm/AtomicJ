
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.NumericalTableModel;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.MultiMap;


public class InferencesTableModel extends DefaultTableModel implements NumericalTableModel
{
    private static final long serialVersionUID = 1L;

    private final File defaultOutputDirectory;
    private final Object[][] allData;
    private final int sampleCount;

    private boolean saved = false;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public InferencesTableModel(Map<String, Object[]> sampleData, Map<String, Object> testData, int sampleCount,  File defaultOutputDirectory)
    {
        super(3,2);
        this.sampleCount = sampleCount;
        this.defaultOutputDirectory = defaultOutputDirectory;
        setColumnCount(2);
        setColumnIdentifiers(new Object[] {"",""});

        int n1 = sampleData.size();
        int n2 = testData.size();
        int n = n1 + n2;

        Object[][] sampleValues = new Object[n1][];

        int p = 0;
        for(Object[] value: sampleData.values())
        {
            sampleValues[p++] = value;
        }


        setValueAt(sampleData.keySet().toArray(), 0, 0);
        setValueAt(sampleValues, 0, 1);
        setValueAt("",1,0);
        setValueAt("",1,1);
        setValueAt(testData.keySet().toArray(), 2, 0);
        setValueAt(testData.values().toArray(), 2, 1);


        allData = new Object[n + 1][];

        int i = 0;
        for(Entry<String, Object[]> entry: sampleData.entrySet())
        {
            String key = entry.getKey();
            Object[] values = entry.getValue();
            int k = values.length + 1;
            Object[] row = new Object[k];
            row[0] = key;

            for(int j = 0; j<values.length; j++)
            {
                Object v = values[j];
                row[j + 1] = v;
            }

            allData[i++] = row;
        }

        //adds empty Line
        allData[i++] = new String [] {};

        for(Entry<String, Object> entry: testData.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            Object[] row = new Object[2];
            row[0] = key;
            row[1] = value;
            allData[i++] = row;
        }		
    }

    public int getSampleCount()
    {
        return sampleCount;
    }

    public Object[][] getSavableData()
    {
        return allData;
    }

    public int getDimensions(int row, int column)
    {
        int dim = (row == 0 && column == 1) ? 2 : 1;
        return dim;
    }

    @Override
    public File getDefaultOutputDirectory()
    {
        return defaultOutputDirectory;
    }

    public boolean areChangesSaved()
    {
        return saved;
    }

    public void setSaved(boolean savedNew)
    {
        this.saved = savedNew;
    }

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

    public void clearData()
    {
        getDataVector().removeAllElements();
        fireTableDataChanged();
    }

    @Override
    public List<IdentityTag> getColumnShortNames() 
    {
        return Collections.emptyList();
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

    @Override
    public StandardUnitSource getUnitSource() {
        return new StandardUnitSource(new MultiMap<IdentityTag, PrefixedUnit>(), new LinkedHashMap<IdentityTag, PrefixedUnit>());
    }

    @Override
    public int getColumnIndex(IdentityTag columnId) {
        return 0;
    }

    @Override
    public PrefixedUnit getDataUnit(IdentityTag columnId) {
        return null;
    }

    @Override
    public List<PrefixedUnit> getDisplayedUnits()
    {
        return Collections.emptyList();
    }

    @Override
    public List<PrefixedUnit> getDataUnits() {
        return Collections.emptyList();
    }
}
