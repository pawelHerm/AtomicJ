package atomicJ.resources;

import java.awt.Cursor;
import java.awt.Window;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atomicJ.data.ChannelFilter2;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.MouseInteractiveToolSupervisor;
import atomicJ.gui.ResourceGroupListener;
import atomicJ.gui.ResourceTypeListener;
import atomicJ.gui.SelectionListener;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.utilities.MetaMap;

public interface ResourceView <R extends Resource, E, I> extends MouseInteractiveToolSupervisor
{    
    public String getSelectedType();

    public R getSelectedResource();
    public List<? extends R> getAllSelectedResources();
    public List<? extends R> getResources();
    public List<? extends R> getAdditionalResources();
    public int getResourceCount();

    public Set<I> getSelectedResourcesChannelIdentifiers();    
    public Set<I> getSelectedResourcesChannelIdentifiers(String type);  
    public Set<I> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<E> filter);  
    public Set<I> getSelectedResourcesChannelIdentifiers(ChannelFilter2<E> filter);  

    public Set<I> getAllResourcesChannelIdentifiers();
    public Set<I> getAllResourcesChannelIdentifiers(String type);
    public Set<I> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<E> filter);
    public Set<I> getAllResourcesChannelIdentifiers(ChannelFilter2<E> filter);

    public void addResourceSelectionListener(SelectionListener<? super R> listener);
    public void removeResourceSelectionListener(SelectionListener<? super R> listener);

    public void addResourceDataListener(ResourceGroupListener<? super R> listener);
    public void removeResourceDataListener(ResourceGroupListener<? super R> listener);

    public void addResourceTypeListener(ResourceTypeListener listener);
    public void removeResourceTypeListener(ResourceTypeListener listener);

    public PrefixedUnit getDataUnit();
    public PrefixedUnit getDisplayedUnit();
    public UnitManager getUnitManager();
    public List<PrefixedUnit> getDomainDisplayedUnits();
    public List<PrefixedUnit> getDomainDataUnits();

    public ROI getROIUnion();
    public Map<Object, ROI> getDrawableROIs();
    public Map<Object, ROI> getAllROIs();

    public void handleChangeOfData(Map<I, E> channelsMap, String type, R resource);
    public void pushCommand(R resource, String type, UndoableCommand command);
    public void pushCommands(MetaMap<R, String, UndoableCommand> commands);
    public void refreshUndoRedoOperations();

    public Window getAssociatedWindow();
    public void requestCursorChange(Cursor horizontalCursor, Cursor verticalCursor);
}
