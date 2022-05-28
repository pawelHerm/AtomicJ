package atomicJ.gui.rois;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.WizardPageModel;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionAdapter;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.sources.IdentityTag;

public class ComplementROIModel <E extends ROI> extends AbstractModel implements WizardPageModel
{    
    private static final String TASK_NAME = "ROI complement";
    private static final String TASK_DESCRIPTION = "Select which ROI's complement to calculate";

    public static final String DELETE_ORIGINAL_ROIS = "DeleteOriginalROIs";

    //all rois as values, both main rois, and the rois that can be subtracted
    private final Map<IdentityTag, E> roiIdMap = new LinkedHashMap<>();

    private final MultipleSelectionModel<IdentityTag> complementROIsModel;
    private boolean deleteOriginalROIs = true;

    private boolean necessaryInputProvided;
    private boolean nextEnabled;
    private boolean finishEnabled;

    private final boolean isFirst;
    private final boolean isLast;

    public ComplementROIModel(Set<E> rois, boolean isFirst, boolean isLast)
    {
        this.isFirst = isFirst;
        this.isLast = isLast;

        this.complementROIsModel = new BasicMultipleSelectionModel<>(ROIUtilities.getIds(rois), "");

        initSelectionListener();
        populateROIMap(rois);

        checkIfFinishEnabled();
    }

    public boolean isDeleteOriginalROIs()
    {
        return deleteOriginalROIs;
    }

    public void setDeleteOriginalROIs(boolean deleteOriginalROIsNew)
    {
        boolean deleteOriginalROIsOld = this.deleteOriginalROIs;
        this.deleteOriginalROIs = deleteOriginalROIsNew;

        firePropertyChange(DELETE_ORIGINAL_ROIS, deleteOriginalROIsOld, deleteOriginalROIsNew);
    }

    private void populateROIMap(Set<E> rois)
    {
        for(E roi : rois)
        {
            roiIdMap.put(roi.getIdentityTag(), roi);
        }
    }

    public Set<E> getROIsToComplement()
    {
        Set<E> subtractedROIs = new LinkedHashSet<>();

        for(IdentityTag id : complementROIsModel.getSelectedKeys())
        {
            subtractedROIs.add(roiIdMap.get(id));
        }

        return subtractedROIs;
    }

    private void initSelectionListener()
    {
        this.complementROIsModel.addSelectionChangeListener(new MultipleSelectionAdapter<IdentityTag>()
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
        this.necessaryInputProvided = this.complementROIsModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, this.necessaryInputProvided);
    }

    private void checkIfNextEnabled()
    {
        boolean nextEnabledOld = this.nextEnabled;
        this.nextEnabled = !isLast && !this.complementROIsModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, nextEnabledOld, this.nextEnabled);
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledOld = this.finishEnabled;
        this.finishEnabled = isLast && !this.complementROIsModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, finishEnabledOld, this.finishEnabled);
    }

    public MultipleSelectionModel<IdentityTag> getROIsToComplementModel()
    {
        return complementROIsModel;
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
