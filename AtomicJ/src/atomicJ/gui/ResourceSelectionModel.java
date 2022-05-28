
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

import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.util.List;

import atomicJ.resources.Resource;


public interface ResourceSelectionModel <E extends Resource> extends WizardPageModel
{
    public void setSources(List<E> newSources);
    public void addSources(List<E> newSources);
    public void removeSources(List<E> sources);
    public List<E> getSources();
    public boolean isSourceFilteringPossible();

    public boolean areSourcesSelected();

    public boolean isRestricted();
    public String getIdentifier();

    public void showPreview();
    public void showPreview(List<E> sources);

    public Window getParent();

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener);
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener);
}
