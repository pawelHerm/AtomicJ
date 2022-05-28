package atomicJ.gui.annotations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.JOptionPane;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.WizardPageModel;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionAdapter;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.sources.IdentityTag;

public class ExportAnnotationModel <E extends AbstractCustomizableAnnotation> extends AbstractModel implements WizardPageModel
{    
    public static final String FILE_PATH = "FilePath";

    private final String taskName;
    private final String taskDecription;
    private final String objectListLabel;

    private final Map<IdentityTag, E> idObjectMap = new LinkedHashMap<>();

    private final MultipleSelectionModel<IdentityTag> availableObjectsSelectionModel;

    private File filePath;

    private final String objectTypeName;
    private final String fileExtension;

    private boolean necessaryInputProvided;
    private boolean nextEnabled;
    private boolean finishEnabled;

    private final boolean isFirst;
    private final boolean isLast;

    public ExportAnnotationModel(Set<E> availableObjects, String objectTypeName, String fileExtension, boolean isFirst, boolean isLast)
    {
        this.isFirst = isFirst;
        this.isLast = isLast;

        this.objectTypeName = objectTypeName;
        this.fileExtension = fileExtension;
        this.availableObjectsSelectionModel = new BasicMultipleSelectionModel<>(ROIUtilities.getIds(availableObjects), "");

        this.taskName = buildTaskName();
        this.taskDecription = buildTaskDescription();
        this.objectListLabel = buildObjectListLabel();

        initSelectionListener();
        populateObjectMap(availableObjects);

        checkIfFinishEnabled();
    }

    public File getFilePath()
    {
        return filePath;
    }

    public void setFilePath(File filePathNew)
    {
        if(filePathNew != null && filePathNew.isDirectory())
        {
            throw new IllegalArgumentException("File object passed as the 'file' argument should not be a directory");
        }

        if(!Objects.equals(this.filePath, filePathNew))
        {
            File filePathOld = this.filePath;
            this.filePath = filePathNew;

            firePropertyChange(FILE_PATH, filePathOld, filePathNew);

            checkPathExtensionCorrectness();
            checkIfNecessaryInputProvided();
            checkIfNextEnabled();
            checkIfFinishEnabled();
        }       
    }

    public boolean isExportPathFree()
    {
        boolean free = filePath == null || !filePath.exists();

        return free;
    }

    private void checkPathExtensionCorrectness()
    {
        if(filePath != null)
        {
            String[] expectedExtensions =  new String[] {fileExtension};
            File correctExtFile = ExtensionFileChooser.ensureCorrectExtension(filePath, expectedExtensions, fileExtension);
            setFilePath(correctExtFile);
        }
    }

    private String buildTaskName()
    {
        String taskName = objectTypeName + " export";

        return taskName;
    }

    private String buildTaskDescription()
    {
        String taskDescription = "Select which " + objectTypeName + "s are to be exported";

        return taskDescription;
    }

    private String buildObjectListLabel()
    {
        String listLabel = objectTypeName + "s to export";

        return listLabel;
    }

    public String getObjectListLabel()
    {
        return objectListLabel;
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

        for(IdentityTag id : availableObjectsSelectionModel.getSelectedKeys())
        {
            selectedObjects.add(idObjectMap.get(id));
        }

        return selectedObjects;
    }

    private void initSelectionListener()
    {
        this.availableObjectsSelectionModel.addSelectionChangeListener(new MultipleSelectionAdapter<IdentityTag>()
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
        this.necessaryInputProvided = filePath != null && this.availableObjectsSelectionModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, this.necessaryInputProvided);
    }

    private void checkIfNextEnabled()
    {
        boolean nextEnabledOld = this.nextEnabled;
        this.nextEnabled = !isLast && filePath != null && !this.availableObjectsSelectionModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, nextEnabledOld, this.nextEnabled);
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledOld = this.finishEnabled;
        this.finishEnabled = isLast && filePath != null && !this.availableObjectsSelectionModel.areAllKeysDeselected();

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, finishEnabledOld, this.finishEnabled);
    }

    public MultipleSelectionModel<IdentityTag> getMergeObjectsModel()
    {
        return availableObjectsSelectionModel;
    }

    @Override
    public String getTaskName()
    {
        return taskName;
    }

    @Override
    public String getTaskDescription() 
    {
        return taskDecription;
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
    public void back() 
    {        
    }

    @Override
    public void next() {        
    }

    @Override
    public void skip() {        
    }

    @Override
    public void finish() 
    {        
        if(!finishEnabled)
        {
            return;
        }   

        if(!isExportPathFree())
        {
            int result = JOptionPane.showConfirmDialog(null,"The file exists, overwrite?",AtomicJ.APPLICATION_NAME,JOptionPane.YES_NO_CANCEL_OPTION);
            if(result != JOptionPane.YES_OPTION)
            {
                return;
            }
        }

        Set<E> selectedAnnotations = getSelectedObjects();

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath)))
        {
            out.writeInt(selectedAnnotations.size());

            for(AbstractCustomizableAnnotation annotation : selectedAnnotations)
            {                    
                out.writeObject(annotation.getProxy());

            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }          
    }

    @Override
    public void cancel() 
    {}

    @Override
    public boolean isBackEnabled()
    {
        return !isFirst;
    }

    @Override
    public boolean isNextEnabled() 
    {
        return nextEnabled;
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return false;
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return finishEnabled;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return necessaryInputProvided;
    }
}
