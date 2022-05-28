
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
import java.util.*;
import javax.swing.JOptionPane;

import atomicJ.analysis.*;
import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;
import atomicJ.sources.ChannelSource;


public class OpeningModelStandard <E extends ChannelSource> extends AbstractModel implements ResourceSelectionModel<E>
{
    private List<E> sources;
    private final PreviewDestination parent;	
    private boolean currentBatchNonempty;

    public OpeningModelStandard(PreviewDestination parent)
    {
        this(parent, new ArrayList<E>());
    }

    public OpeningModelStandard(PreviewDestination parent, List<E> sources)
    {
        this.parent = parent;
        this.sources = sources;
        this.currentBatchNonempty = !sources.isEmpty();
    }

    @Override
    public boolean isSourceFilteringPossible()
    {
        return false;
    }

    @Override
    public Window getParent()
    {
        return parent.getPublicationSite();
    }

    @Override
    public boolean isRestricted()
    {
        return false;
    }

    @Override
    public boolean areSourcesSelected()
    {
        return currentBatchNonempty;
    }

    @Override
    public void setSources(List<E> newSources)
    {
        List<E> oldSources = sources;
        this.sources = newSources;

        firePropertyChange(ProcessingBatchModelInterface.SOURCES, oldSources, newSources);

        checkIfBatchNonempty();
    }

    @Override
    public void addSources(List<E> newSources)
    {
        List<E> sources = new ArrayList<>(getSources());
        sources.addAll(newSources);

        setSources(sources);
    }

    @Override
    public void removeSources(List<E> removedSources)
    {
        List<E> sources = new ArrayList<>(getSources());
        sources.remove(removedSources);

        setSources(sources);
    }

    @Override
    public List<E> getSources()
    {
        return sources;
    }

    private void checkIfBatchNonempty()
    {
        boolean batchNonEmptyNew = sources.size()>0;;
        boolean batchNonEmptyOld = currentBatchNonempty;

        if(batchNonEmptyNew != currentBatchNonempty)
        {
            currentBatchNonempty = batchNonEmptyNew;
            firePropertyChange(ProcessingBatchModelInterface.SOURCES_SELECTED, batchNonEmptyOld, batchNonEmptyNew);
        }
    }

    @Override
    public void showPreview()
    {
        showPreview(sources);
    }

    @Override
    public void showPreview(List<E> sources)
    {
        if(currentBatchNonempty)
        {	
            parent.requestPreviewEnd();
            ConcurrentPreviewTask task = new ConcurrentPreviewTask(sources, parent);			
            task.execute();
        }		
        else
        {
            JOptionPane.showMessageDialog(parent.getPublicationSite(), "No file to preview", "", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public String getIdentifier() 
    {
        return "";
    }

    public void clear()
    {
        setSources(new ArrayList<E>());
    }

    @Override
    public String getTaskName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTaskDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFirst() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLast() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void back() {
        // TODO Auto-generated method stub

    }

    @Override
    public void next() {
        // TODO Auto-generated method stub

    }

    @Override
    public void skip() {
        // TODO Auto-generated method stub

    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isBackEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNextEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSkipEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFinishEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNecessaryInputProvided() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub

    }
}
