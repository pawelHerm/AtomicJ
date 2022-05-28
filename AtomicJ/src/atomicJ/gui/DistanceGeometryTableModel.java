
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.table.DefaultTableModel;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.MultiMap;


public class DistanceGeometryTableModel extends DefaultTableModel implements NumericalTableModel
{
    private static final long serialVersionUID = 1L;
    private final File defaultOutputFile;	

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean empty;

    private final Map<Object, DistanceShapeFactors> distances = new Hashtable<>();

    private static final List<IdentityTag> COLUMN_IDS = Arrays.asList(new IdentityTag("Profile"),
            new IdentityTag("Length"),
            new IdentityTag("Length X"),
            new IdentityTag("Length Y"),
            new IdentityTag("Angle"),
            new IdentityTag("Start X"),
            new IdentityTag("Start Y"),
            new IdentityTag("End X"),
            new IdentityTag("End Y")
            );
    private final List<PrefixedUnit> dataUnits = new ArrayList<>();
    private final StandardUnitSource unitSource;

    public DistanceGeometryTableModel()
    {
        this(new File(System.getProperty("user.home")), SimplePrefixedUnit.getNullInstance());
    }

    public DistanceGeometryTableModel(File defaultOutputFile, PrefixedUnit unit)
    {
        this(defaultOutputFile, unit, unit, unit, unit);
    }

    public DistanceGeometryTableModel(File defaultOutputFile, PrefixedUnit dataUnitX, PrefixedUnit dataUnitY)
    { 
        this(defaultOutputFile, dataUnitX, dataUnitY, dataUnitX, dataUnitY);
    }

    public DistanceGeometryTableModel(File defaultOutputFile, PrefixedUnit dataUnitX, PrefixedUnit dataUnitY, 
            PrefixedUnit displayedUnitX, PrefixedUnit displayedUnitY)
    {	
        super(0, COLUMN_IDS.size());

        boolean unitsIdentical = dataUnitX.equals(dataUnitY);
        PrefixedUnit lengthUnit = unitsIdentical ? dataUnitX : null;
        PrefixedUnit lengthDisplayedUnit = unitsIdentical ? displayedUnitX : null;

        dataUnits.add(null);
        dataUnits.add(lengthUnit);
        dataUnits.add(dataUnitX);
        dataUnits.add(dataUnitY);
        dataUnits.add(Units.DEGREE_UNIT);
        dataUnits.add(dataUnitX);
        dataUnits.add(dataUnitY);
        dataUnits.add(dataUnitX);
        dataUnits.add(dataUnitY);

        this.defaultOutputFile = defaultOutputFile;	
        this.unitSource = buildUnitSource();
        unitSource.setUseDefaultUnits(true);

        unitSource.setDefaultUnit(COLUMN_IDS.get(1), lengthDisplayedUnit);
        unitSource.setDefaultUnit(COLUMN_IDS.get(2), displayedUnitX);
        unitSource.setDefaultUnit(COLUMN_IDS.get(2), displayedUnitY);
        unitSource.setDefaultUnit(COLUMN_IDS.get(4), Units.DEGREE_UNIT);
        unitSource.setDefaultUnit(COLUMN_IDS.get(5), displayedUnitX);
        unitSource.setDefaultUnit(COLUMN_IDS.get(6), displayedUnitY);
        unitSource.setDefaultUnit(COLUMN_IDS.get(7), displayedUnitX);
        unitSource.setDefaultUnit(COLUMN_IDS.get(8), displayedUnitY);

        this.empty = isEmpty();
    }

    private StandardUnitSource buildUnitSource()
    {
        Map<IdentityTag, PrefixedUnit> selectedUnits = new LinkedHashMap<>();
        MultiMap<IdentityTag, PrefixedUnit> unitGroups = new MultiMap<>();

        for(int i = 1; i<COLUMN_IDS.size();i++)
        {
            PrefixedUnit dataUnit = dataUnits.get(i);
            if(dataUnit != null)
            {
                selectedUnits.put(COLUMN_IDS.get(i), dataUnit);
                unitGroups.putAll(COLUMN_IDS.get(i), dataUnit.deriveUnits());
            }
        }


        StandardUnitSource unitSource = new StandardUnitSource(unitGroups, selectedUnits);

        return unitSource;
    }

    public void addDistance(Object key, DistanceShapeFactors distance)
    {
        distances.put(key, distance);

        Object[] row = convertToRow(key, distance);
        addRow(row);

    }

    public void addOrUpdateDistance(Object key, DistanceShapeFactors distance)
    {		
        boolean alreadyPresent = distances.containsKey(key);			
        distances.put(key, distance);

        Object[] row = convertToRow(key, distance);

        if(alreadyPresent)
        {
            replaceRow(row, key);
        }
        else
        {
            addRow(row);
        }

    }

    public void addDistances(Map<Object, DistanceShapeFactors> distances)
    {		   
        for(Entry<Object, DistanceShapeFactors> entry: distances.entrySet())
        {
            Object key = entry.getKey();
            DistanceShapeFactors line = entry.getValue();

            boolean alreadyPresent = this.distances.containsKey(key);			
            this.distances.put(key, line);

            Object[] row = convertToRow(key, line);

            if(alreadyPresent)
            {
                replaceRow(row, key);
            }
            else
            {
                addRow(row);
            }


        }		
        checkIfEmpty();
    }

    public void setDistances(Map<Object, DistanceShapeFactors> distances)
    {
        setRowCount(0);
        addDistances(distances);
    }

    public boolean replaceDistance(Object key, DistanceShapeFactors distance)
    {
        boolean tableChanged = false;

        boolean presentBefore = distances.containsKey(key);			
        if(presentBefore)
        {
            Object[] row = convertToRow(key, distance);
            replaceRow(row, key);
            tableChanged = true;
        }

        return tableChanged;
    }

    public void removeDistance(Object key, DistanceShapeFactors distance)
    {
        int i = getSampleIndex(key);
        if(i>-1)
        {
            removeRow(i);
        }

        distances.remove(key);
        setRowCount(distances.size());
        checkIfEmpty();
    }

    private void replaceRow(Object[] newRow, Object name)
    {
        int i = getSampleIndex(name);

        if(i>-1)
        {		
            removeRow(i);
            insertRow(i, newRow);
        }
        checkIfEmpty();
    }

    protected int getSampleIndex(Object sampleName)
    {
        int result = -1;
        Vector<?> data = getDataVector();
        int r = getRowCount();
        for(int i = 0;i<r;i++)
        {
            Vector<?> row = (Vector<?>)data.elementAt(i);
            Object p = row.elementAt(0);
            if(p.equals(sampleName))
            {
                result = i;
                break;
            }
        }
        return result;
    }


    @Override
    public File getDefaultOutputDirectory() 
    {
        return defaultOutputFile;
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

    @Override
    public Class<?> getColumnClass(int n)
    {
        Class<?> c = (n == 0) ? Integer.class : Double.class;
        return c;
    }

    @Override
    public List<IdentityTag> getColumnShortNames()
    {
        return new ArrayList<>(COLUMN_IDS);
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

    public void clearData()
    {
        distances.clear();
        setRowCount(0);
    }

    protected Object[] convertToRow(Object key, DistanceShapeFactors line)
    {		
        Object[] row = new Object[9];

        row[0] = key;
        row[1] = line.getLength();
        row[2] = line.getLengthX();
        row[3] = line.getLengthY();
        row[4] = line.getAngle();
        row[5] = line.getStartX();	
        row[6] = line.getStartY();
        row[7] = line.getEndX();
        row[8] = line.getEndY();

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
