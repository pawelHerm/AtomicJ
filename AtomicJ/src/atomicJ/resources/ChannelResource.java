package atomicJ.resources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import atomicJ.data.Channel;
import atomicJ.data.ChannelFilter2;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MultiMap;

public interface ChannelResource<E extends Channel, D, I> extends DataModelResource
{
    public List<String> getAllTypes();
    public Set<I> getIdentifiers(String type);
    public Set<I> getIdentifiers(String type, ChannelFilter2<E> filter);
    public Set<I> getIdentifiersForAllTypes();
    public Set<I> getIdentifiersForAllTypes(ChannelFilter2<E> filter);
    @Override
    public boolean containsChannelsFromSource(ChannelSource source);
    //returns a map with values being those channels whose identifiers are in the set 'identifiers'. The keys are the corresponding identifiers
    //A multimap is returned, because this TypeModelManager may contain two different sources, with channels of the same identifier
    public MultiMap<I, E> getChannelsForIdentifiers(Set<I> identifiers);
    public Map<I, D> getChannelData(String type);
    public Map<I, E> setChannelData(String type, Map<I, D> dataMap);
    public void undo(String type);
    public void redo(String type);
    public CommandIdentifier getCommandToRedoCompundIdentifier(String type);
    public CommandIdentifier getCommandToUndoCompundIdentifier(String type);
    public boolean canBeRedone(String type);
    public boolean canBeUndone(String type);
    public void pushCommand(String type, UndoableCommand command);
    public void setUndoSizeLimit(int sizeLimit);
    public ROI getROIUnion();
}
