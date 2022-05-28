package atomicJ.gui.rois;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.WizardPageModel;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionModel;
import atomicJ.gui.selection.multiple.CompositeSelectionModelB;
import atomicJ.gui.selection.multiple.MultipleSelectionAdapter;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.gui.selection.single.BasicSingleSelectionModel;
import atomicJ.gui.selection.single.SingleSelectionModel;
import atomicJ.sources.IdentityTag;

public class DifferenceROIModel <E extends ROI> extends AbstractModel implements WizardPageModel
{    
    private static final String TASK_NAME = "ROI difference";
    private static final String TASK_DESCRIPTION = "Select which ROIs should be subtracted";

    public static final String DELETE_SUBTRACTED_ROIS = "DeleteSubtractedROIs";

    //all rois as values, both main rois, and the rois that can be subtracted
    private final Map<IdentityTag, E> rois = new LinkedHashMap<>();

    private final SingleSelectionModel<IdentityTag> mainROIModel;
    private final CompositeSelectionModelB<IdentityTag, IdentityTag> subtractedROIsModel;

    private boolean deleteSubtractedROIs = true;

    private boolean necessaryInputProvided;
    private boolean nextEnabled;
    private boolean finishEnabled;

    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    private final boolean isFirst;
    private final boolean isLast;

    public DifferenceROIModel(Map<E, Set<E>> possibleDifferences, boolean isFirst, boolean isLast)
    {
        this.isFirst = isFirst;
        this.isLast = isLast;

        this.mainROIModel = buildMainROIModel(possibleDifferences);
        this.subtractedROIsModel = buildSubtractedROIsModel(possibleDifferences);

        initSelectionListener();
        initPropertyChangeListeners();
        populateROIMap(possibleDifferences);

        checkIfFinishEnabled();
    }

    private void populateROIMap(Map<E, Set<E>> possibleDifferences)
    {
        for(Entry<E, Set<E>> entry : possibleDifferences.entrySet())
        {
            rois.put(entry.getKey().getIdentityTag(), entry.getKey());

            for(E roi : entry.getValue())
            {
                rois.put(roi.getIdentityTag(), roi);
            }
        }
    }

    public boolean isDeleteSubtractedROIs()
    {
        return deleteSubtractedROIs;
    }

    public void setDeleteSubtractedROIs(boolean deleteSubtractedROIsNew)
    {
        boolean deleteOriginalROIsOld = this.deleteSubtractedROIs;
        this.deleteSubtractedROIs = deleteSubtractedROIsNew;

        firePropertyChange(DELETE_SUBTRACTED_ROIS, deleteOriginalROIsOld, deleteSubtractedROIsNew);
    }

    public E getMainROI()
    {
        return rois.get(mainROIModel.getSelectedKey());
    }

    public Set<E> getSubtractedROIs()
    {
        Set<E> subtractedROIs = new LinkedHashSet<>();

        for(IdentityTag id : subtractedROIsModel.getSelectedKeys())
        {
            subtractedROIs.add(rois.get(id));
        }

        return subtractedROIs;
    }

    private SingleSelectionModel<IdentityTag> buildMainROIModel(Map<E, Set<E>> possibleDifferences)
    {        
        List<IdentityTag> firstROIs = ROIUtilities.getIds(possibleDifferences.keySet());

        String selectionName = "";
        SingleSelectionModel<IdentityTag> mainROIModel = new BasicSingleSelectionModel<>(firstROIs, selectionName, selectionName, selectionName, true, true);

        return mainROIModel;
    }

    private CompositeSelectionModelB<IdentityTag, IdentityTag> buildSubtractedROIsModel(Map<E, Set<E>> possibleDifferences)
    {
        Map<IdentityTag, MultipleSelectionModel<IdentityTag>> subtractedROIsModels = new LinkedHashMap<>();

        for(Entry<E, Set<E>> entry : possibleDifferences.entrySet())
        {
            IdentityTag id = entry.getKey().getIdentityTag();            
            subtractedROIsModels.put(id, new BasicMultipleSelectionModel<>(ROIUtilities.getIds(entry.getValue()), ""));
        }

        CompositeSelectionModelB<IdentityTag, IdentityTag> subtractedROIsModel = new CompositeSelectionModelB<>(subtractedROIsModels);

        return subtractedROIsModel;
    }

    private void initPropertyChangeListeners()
    {
        this.mainROIModel.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String property = evt.getPropertyName();

                if(SingleSelectionModel.SELECTED_KEY.equals(property))
                {
                    IdentityTag id = (IdentityTag)evt.getNewValue();                                        
                    subtractedROIsModel.setModel(id);
                }                
            }
        });

    }

    private void initSelectionListener()
    {
        this.subtractedROIsModel.addSelectionChangeListener(new MultipleSelectionAdapter<IdentityTag>()
        {    
            @Override
            public void allKeysDeselectedChanged(boolean allDeselectedOld, boolean allDeselectedNew)
            {
                checkIfNecessaryInputProvided();
                checkIfNextEnabled();
                checkIfFinishEnabled();
            }
        });

    }

    private void checkIfNecessaryInputProvided()
    {
        boolean necessaryInputProvidedOld = this.necessaryInputProvided;
        this.necessaryInputProvided = this.subtractedROIsModel.areAllKeysDeselected();

        propertySupport.firePropertyChange(WizardModelProperties.NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, this.necessaryInputProvided);
    }

    private void checkIfNextEnabled()
    {
        boolean nextEnabledOld = this.nextEnabled;
        this.nextEnabled = !isLast && !this.subtractedROIsModel.areAllKeysDeselected();

        propertySupport.firePropertyChange(WizardModelProperties.NEXT_ENABLED, nextEnabledOld, this.nextEnabled);
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledOld = this.finishEnabled;
        this.finishEnabled = isLast && !this.subtractedROIsModel.areAllKeysDeselected();

        propertySupport.firePropertyChange(WizardModelProperties.FINISH_ENABLED, finishEnabledOld, this.finishEnabled);
    }

    public SingleSelectionModel<IdentityTag> getMainROIModel()
    {
        return mainROIModel;
    }

    public CompositeSelectionModelB<IdentityTag, IdentityTag> getSubtractedROIsModel()
    {
        return subtractedROIsModel;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public String getTaskDescription() {
        return TASK_DESCRIPTION;
    }

    @Override
    public boolean isFirst() {
        return isFirst;
    }

    @Override
    public boolean isLast() {
        return isLast;
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
    public void cancel() {        
    }

    @Override
    public boolean isBackEnabled() {
        return !isFirst;
    }

    @Override
    public boolean isNextEnabled() {
        return nextEnabled;
    }

    @Override
    public boolean isSkipEnabled() {
        return false;
    }

    @Override
    public boolean isFinishEnabled() {
        return finishEnabled;
    }

    @Override
    public boolean isNecessaryInputProvided() {
        return false;
    }
}
