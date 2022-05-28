package atomicJ.gui;

import atomicJ.analysis.PreviewDestination;
import atomicJ.data.MetricChannelFilter;
import atomicJ.gui.experimental.ExperimentalWizardModel;
import atomicJ.gui.rois.ROIWizardReceiver;

public class TopographySelectionWizardModel extends ExperimentalWizardModel
{  
    private final static WizardPageDescriptor sourceSelectionDescriptor = new WizardPageDescriptor("Image selection",
            "<html>Choose the image of the force map area<br>from the file system or from the already read-in maps and images</html>", true, false, false, "<<   Back", "Next   >>");
    private final static WizardPageDescriptor channelSelectionDescriptor = new WizardPageDescriptor("Channel selection", "Select topography channel", false, false, false, "<<   Back", "Next   >>");
    private final static WizardPageDescriptor roiSelectionDescriptor = new WizardPageDescriptor("Correct topography", 
            "Select sample as ROI(s). Use context menu actions to correct topography", false, true, false, "<<   Back", "Next   >>");

    public TopographySelectionWizardModel(PreviewDestination previewDestination,
            ROIWizardReceiver roiImageReceiver) 
    {
        super(previewDestination, roiImageReceiver, MetricChannelFilter.getInstance(),
                sourceSelectionDescriptor, channelSelectionDescriptor,
                roiSelectionDescriptor, true);
    }
}
