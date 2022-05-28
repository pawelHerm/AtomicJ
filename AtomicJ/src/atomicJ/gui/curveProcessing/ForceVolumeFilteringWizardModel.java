package atomicJ.gui.curveProcessing;

import atomicJ.analysis.PreviewDestination;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.gui.WizardPageDescriptor;
import atomicJ.gui.experimental.ExperimentalWizardModel;
import atomicJ.gui.rois.ROIWizardReceiver;

public class ForceVolumeFilteringWizardModel extends ExperimentalWizardModel
{ 
    private final static  WizardPageDescriptor sourceSelectionDescriptor = 
            new WizardPageDescriptor("Image selection",
                    "<html>Choose the image of the force map area<br>" +
                            "from the file system or from the already read-in maps and images</html>", true, false, false, "<<   Back", "Next   >>");
    private final static WizardPageDescriptor channelSelectionDescriptor = 
            new WizardPageDescriptor("Channel selection", "Select image channel", false, false, false, "<<   Back", "Next   >>");
    private final static WizardPageDescriptor roiSelectionDescriptor = new WizardPageDescriptor("Select patches", 
            "Select patches that includes curves to process. Curves outside will be removed from this batch.", false, true, false, "<< Previous patch", "Next patch >>");

    public ForceVolumeFilteringWizardModel(PreviewDestination previewDestination,
            ROIWizardReceiver roiImageReceiver) 
    {
        super(previewDestination, roiImageReceiver, PermissiveChannelFilter.getInstance(),
                sourceSelectionDescriptor, channelSelectionDescriptor,
                roiSelectionDescriptor, false);
    }
}
