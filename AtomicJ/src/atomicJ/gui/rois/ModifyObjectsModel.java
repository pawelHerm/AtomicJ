package atomicJ.gui.rois;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.Identifiable;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.WizardPageModel;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionAdapter;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.sources.IdentityTag;

public class ModifyObjectsModel <E extends Identifiable> extends AbstractModel implements WizardPageModel
{    
    public static final String DELETE_ORIGINAL_OBJECTS = "DeleteOriginalObjects";

    private final String taskName;
    private final String taskDescription;
    private final String selectedObjectsDescription;
    private final String objectOriginalDeleteLabel;

    private final Map<IdentityTag, E> idObjectMap = new LinkedHashMap<>();

    private final MultipleSelectionModel<IdentityTag> mergeObjectsSelectionModel;
    private boolean deleteOriginalObjects = true;

    private final String objectTypeName;    

    private boolean necessaryInputProvided;
    private boolean nextEnabled;
    private boolean finishEnabled;

    private final boolean isFirst;
    private final boolean isLast;

    public ModifyObjectsModel(Set<E> mergeableObjects, String objectTypeName, String taskName, String taskDescription, String selectedObjectsDescription, boolean isFirst, boolean isLast)
    {
        this.isFirst = isFirst;
        this.isLast = isLast;

        this.objectTypeName = objectTypeName;


        this.mergeObjectsSelectionModel = new BasicMultipleSelectionModel<>(ROIUtilities.getIds(mergeableObjects), "");

        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.selectedObjectsDescription = selectedObjectsDescription;
        this.objectOriginalDeleteLabel = buildObjectDeleteLabel();

        initSelectionListener();
        populateObjectMap(mergeableObjects);

        checkIfFinishEnabled();
    }

    //taskDescription - "Select which " + objectTypeName + "s are to be merged"
    //task name - objectTypeName + " " + merging

    //selectedObjectsDescription - objectTypeName + "s to merge"

    private String buildObjectDeleteLabel()
    {
        String label = "Delete original " + objectTypeName + "s";
        return label;
    }

    public String getObjectListLabel()
    {
        return selectedObjectsDescription;
    }

    public String getObjectOriginalDeleteLabel()
    {
        return objectOriginalDeleteLabel;
    }

    public boolean isDeleteOriginalObjects()
    {
        return deleteOriginalObjects;
    }

    public void setDeleteOriginalObjects(boolean deleteOriginalObjectsNew)
    {
        boolean deleteOriginalObjectsOld = this.deleteOriginalObjects;
        this.deleteOriginalObjects = deleteOriginalObjectsNew;

        firePropertyChange(DELETE_ORIGINAL_OBJECTS, deleteOriginalObjectsOld, deleteOriginalObjectsNew);
    }

    private void populateObjectMap(Set<E> mergableObjects)
    {
        for(E ob : mergableObjects)
        {
            idObjectMap.put(ob.getIdentityTag(), ob);
        }
    }

    public Set<E> getSelectedObjects()
    {
        Set<E> selectedObjects = new LinkedHashSet<>();

        for(IdentityTag id : mergeObjectsSelectionModel.getSelectedKeys())
        {
            selectedObjects.add(idObjectMap.get(id));
        }

        return selectedObjects;
    }

    private void initSelectionListener()
    {
        this.mergeObjectsSelectionModel.addSelectionChangeListener(new MultipleSelectionAdapter<IdentityTag>()
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
        this.necessaryInputProvided = this.mergeObjectsSelectionModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, this.necessaryInputProvided);
    }

    private void checkIfNextEnabled()
    {
        boolean nextEnabledOld = this.nextEnabled;
        this.nextEnabled = !isLast && !this.mergeObjectsSelectionModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, nextEnabledOld, this.nextEnabled);
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledOld = this.finishEnabled;
        this.finishEnabled = isLast && !this.mergeObjectsSelectionModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, finishEnabledOld, this.finishEnabled);
    }

    public MultipleSelectionModel<IdentityTag> getMergeObjectsModel()
    {
        return mergeObjectsSelectionModel;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public String getTaskDescription() {
        return taskDescription;
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
    public boolean isNecessaryInputProvided() 
    {
        return necessaryInputProvided;
    }
}
