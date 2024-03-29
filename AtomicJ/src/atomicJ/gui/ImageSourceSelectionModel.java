
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe� Hermanowicz
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
import java.io.File;
import java.util.*;
import javax.swing.JOptionPane;

import atomicJ.analysis.*;
import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter;
import atomicJ.data.ImageChannel;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.ImageSource;


public class ImageSourceSelectionModel extends AbstractModel implements ResourceSelectionModel<ImageSource>, WizardPageModel
{
    public static final String CHANNEL_IDENTIFIERS = "CHANNEL_IDENTIFIERS";

    private final String taskName;
    private final String taskDescription;

    private List<ImageSource> sources = new ArrayList<>();
    private final PreviewDestination previewDestination;
    private File sourceDirectory = new File(System.getProperty("user.home"));	

    private Set<String> channelIdentifiers = new LinkedHashSet<>();
    private ChannelFilter channelFilter = PermissiveChannelFilter.getInstance();
    private String channelFilterMessage = "";

    private final boolean isFirst;
    private final boolean isLast;

    private final boolean backEnabled;
    private boolean nextEnabled;
    private final boolean skipEnabled;
    private boolean finishEnabled;

    private boolean necessaryInputProvided;

    public ImageSourceSelectionModel(PreviewDestination parent,String taskName, String taskDescription, boolean isFirst, boolean isLast)
    {
        this.previewDestination = parent;

        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.isFirst = isFirst;
        this.isLast = isLast;

        this.skipEnabled = false;
        this.backEnabled = !isFirst;

        updateChannelIdentifiers();
        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }	

    public void setTypeOfData(ChannelFilter channelFilter, String channelFilterMessage)
    {
        this.channelFilter = channelFilter;
        this.channelFilterMessage = channelFilterMessage;
    }

    public PreviewDestination getPreviewDestination()
    {
        return previewDestination;
    }

    public Window getPublicationSite()
    {
        Window site = previewDestination.getPublicationSite();
        return site;
    }

    @Override
    public boolean isRestricted()
    {
        return false;
    }

    @Override
    public boolean areSourcesSelected()
    {
        return !sources.isEmpty();
    }

    @Override
    public String getIdentifier()
    {
        return "";
    }

    @Override
    public void setSources(List<ImageSource> sourcesNew)
    {        
        List<ImageSource> oldSources = sources;
        sourcesNew = filterChannels(new ArrayList<>(sourcesNew));
        this.sources = sourcesNew;

        File oldParentDir = this.sourceDirectory;
        this.sourceDirectory = BatchUtilities.findLastCommonSourceDirectory(sourcesNew);

        firePropertyChange(ProcessingBatchModelInterface.SOURCES, oldSources, sourcesNew);
        firePropertyChange(ProcessingBatchModelInterface.SOURCES_SELECTED, !oldSources.isEmpty(), !sourcesNew.isEmpty());
        firePropertyChange(ProcessingBatchModelInterface.PARENT_DIRECTORY, oldParentDir, this.sourceDirectory);

        updateChannelIdentifiers();
        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }

    @Override
    public boolean isSourceFilteringPossible()
    {
        return false;
    }

    private List<ImageSource> filterChannels(List<ImageSource> sources)
    {
        int filteredOut = 0;

        Iterator<ImageSource> it = sources.iterator();
        while(it.hasNext())
        {
            List<ImageChannel> identifiers = it.next().getChannels();

            boolean containsIdentifier = false;

            for(Channel2D ch : identifiers)
            {
                if(channelFilter.accepts(ch.getIdentifier(), ch.getZQuantity()))
                {
                    containsIdentifier = true;
                    break;
                }
            }

            if(!containsIdentifier)
            {
                it.remove();
                filteredOut++;
            }
        }

        if(filteredOut>0)
        {
            JOptionPane.showMessageDialog(previewDestination.getPublicationSite(), channelFilterMessage, "", JOptionPane.ERROR_MESSAGE);
        }

        return sources;
    }

    @Override
    public void addSources(List<ImageSource> newSources)
    {
        List<ImageSource> sources = new ArrayList<>(getSources());
        sources.addAll(newSources);

        setSources(sources);
    }

    @Override
    public void removeSources(List<ImageSource> removedSources)
    {
        List<ImageSource> sourcesNew = new ArrayList<>(sources);
        sourcesNew.removeAll(removedSources);

        setSources(sources);
    }

    @Override
    public List<ImageSource> getSources()
    {
        return sources;
    }

    public File getCommonSourceDirectory()
    {
        return sourceDirectory;
    }

    private void updateChannelIdentifiers()
    {
        Set<String> identifiersOld = new LinkedHashSet<>(this.channelIdentifiers);

        this.channelIdentifiers = new LinkedHashSet<>();

        for(Channel2DSource<?> source: sources)
        {
            List<? extends Channel2D> identifiersForSource = source.getChannels();

            for(Channel2D ch : identifiersForSource)
            {
                if(channelFilter.accepts(ch.getIdentifier(), ch.getZQuantity()))
                {
                    this.channelIdentifiers.add(ch.getIdentifier());
                }
            }
        }

        firePropertyChange(CHANNEL_IDENTIFIERS, identifiersOld, new LinkedHashSet<>(this.channelIdentifiers));
    }

    @Override
    public void showPreview()
    {
        showPreview(sources);
    }

    @Override
    public void showPreview(List<ImageSource> sources)
    {
        if(!sources.isEmpty())
        {				
            ConcurrentPreviewTask task = new ConcurrentPreviewTask(sources, previewDestination);			
            task.execute();
        }		
        else
        {
            JOptionPane.showMessageDialog(previewDestination.getPublicationSite(), "No file to preview", "", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void cancel()
    {
    }

    @Override
    public Window getParent() 
    {
        return getPreviewDestination().getPublicationSite();
    }

    private void checkIfNecessaryInputProvided()
    {
        boolean necessaryInputProvidedNew = !sources.isEmpty();
        boolean necessaryInputProvidedOld = this.necessaryInputProvided;

        this.necessaryInputProvided = necessaryInputProvidedNew;

        firePropertyChange(WizardModelProperties.NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, necessaryInputProvidedNew);
    }

    private void checkIfNextEnabled()
    {
        boolean nextEnabledNew = !(sources.isEmpty() || isLast);
        boolean enabledOld = nextEnabled;
        this.nextEnabled = nextEnabledNew;

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, nextEnabledNew);	
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledNew = !sources.isEmpty() && isLast;
        boolean finihEnabledOld = finishEnabled;
        this.finishEnabled = finishEnabledNew;

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, finihEnabledOld, finishEnabledNew);	
    }

    @Override
    public void back() {

    }

    @Override
    public void next() {

    }

    @Override
    public void skip() {

    }

    @Override
    public void finish() {

    }


    @Override
    public boolean isFirst() 
    {
        return isFirst;
    }

    @Override
    public boolean isLast() 
    {
        return isLast;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return necessaryInputProvided;
    }
    @Override
    public boolean isBackEnabled() 
    {
        return backEnabled;
    }

    @Override
    public boolean isNextEnabled() 
    {
        return nextEnabled;
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return skipEnabled;
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return finishEnabled;
    }


    @Override
    public String getTaskName() 
    {
        return taskName;
    }

    @Override
    public String getTaskDescription() 
    {
        return taskDescription;
    }
}
