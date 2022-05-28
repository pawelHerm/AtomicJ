
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

import java.awt.geom.Line2D;
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


public class LineGeometryTableModel extends DefaultTableModel implements NumericalTableModel
{
    private static final long serialVersionUID = 1L;

    private final PrefixedUnit unitX;
    private final PrefixedUnit unitY;

    private final File defaultOutputFile;	

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean empty = isEmpty();

    private final Map<Object, Line2D> lines = new Hashtable<>();

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

    public LineGeometryTableModel()
    {
        this(new File(System.getProperty("user.home")), SimplePrefixedUnit.getNullInstance());
    }

    public LineGeometryTableModel(File defaultOutputFile, PrefixedUnit unit)
    {
        this(defaultOutputFile, unit, unit);
    }

    public LineGeometryTableModel(File defaultOutputFile, PrefixedUnit unitX, PrefixedUnit unitY)
    {	
        super(0, COLUMN_IDS.size());
        this.unitX = unitX;
        this.unitY = unitY;

        boolean unitsIdentical = unitX.equals(unitY);
        PrefixedUnit lengthUnit = unitsIdentical ? unitX : null;

        dataUnits.add(null);
        dataUnits.add(lengthUnit);
        dataUnits.add(unitX);
        dataUnits.add(unitY);
        dataUnits.add(Units.DEGREE_UNIT);
        dataUnits.add(unitX);
        dataUnits.add(unitY);
        dataUnits.add(unitX);
        dataUnits.add(unitY);

        this.defaultOutputFile = defaultOutputFile;	
        this.unitSource = buildUnitSource();
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
        unitSource.setUseDefaultUnits(true);

        for(int i = 1; i<COLUMN_IDS.size();i++)
        {
            PrefixedUnit dataUnit = dataUnits.get(i);
            if(dataUnit != null)
            {
                unitSource.setDefaultUnit(COLUMN_IDS.get(i), dataUnits.get(i));
            }
        }

        return unitSource;
    }

    public void addProfile(Object key, Line2D profile)
    {
        lines.put(key, profile);

        Object[] row = convertToRow(key, profile);
        addRow(row);

    }

    public void addOrUpdateLine(Object key, Line2D profile)
    {		
        boolean alreadyPresent = lines.containsKey(key);			
        lines.put(key, profile);

        Object[] row = convertToRow(key, profile);

        if(alreadyPresent)
        {
            replaceRow(row, key);
        }
        else
        {
            addRow(row);
        }

    }

    public void addLines(Map<Object, Line2D> profiles2)
    {		   
        for(Entry<Object, Line2D> entry: profiles2.entrySet())
        {
            Object key = entry.getKey();
            Line2D line = entry.getValue();

            boolean alreadyPresent = lines.containsKey(key);			
            this.lines.put(key, line);

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

    public void setLine(Map<Object, Line2D> profiles2)
    {
        setRowCount(0);
        addLines(profiles2);
    }

    public boolean replaceLine(Object key, Line2D profile)
    {
        boolean tableChanged = false;

        boolean presentBefore = lines.containsKey(key);			
        if(presentBefore)
        {
            Object[] row = convertToRow(key, profile);
            replaceRow(row, key);
            tableChanged = true;
        }

        return tableChanged;
    }

    public void removeLine(Object key, Line2D profile)
    {
        int i = getSampleIndex(key);
        if(i>-1)
        {
            removeRow(i);
        }

        lines.remove(key);
        setRowCount(lines.size());
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
        lines.clear();
        setRowCount(0);
    }

    protected Object[] convertToRow(Object key, Line2D line)
    {		
        Object[] row = new Object[9];

        double x0 = line.getX1();
        double y0 = line.getY1();

        double x1 = line.getX2();
        double y1 = line.getY2();

        double dx = (x1 - x0);
        double dy = (y1 - y0);

        double length = Math.sqrt(dx*dx + dy*dy);

        double angle = 180*Math.atan2(dy, dx)/Math.PI;

        row[0] = key;
        row[1] = length;
        row[2] = Math.abs(dx);
        row[3] = Math.abs(dy);
        row[4] = angle;
        row[5] = x0;	
        row[6] = y0;
        row[7] = x1;
        row[8] = y1;

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
