
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
import java.beans.PropertyChangeListener;

public interface WizardPage
{
    public String getTaskName();
    public String getTaskDescription();
    public String getIdentifier();

    public boolean isFirst();
    public boolean isLast();

    public boolean isBackEnabled();
    public boolean isNextEnabled();
    public boolean isSkipEnabled();
    public boolean isFinishEnabled();

    public boolean isNecessaryInputProvided();
    public Component getView();
    public Component getControls();

    public void addPropertyChangeListener(PropertyChangeListener listener) ;    
    public void removePropertyChangeListener(PropertyChangeListener listener) ;
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
