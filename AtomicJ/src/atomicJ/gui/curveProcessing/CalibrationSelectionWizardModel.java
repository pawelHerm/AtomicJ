package atomicJ.gui.curveProcessing;

import atomicJ.analysis.PreviewDestination;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.gui.WizardPageDescriptor;
import atomicJ.gui.experimental.ExperimentalWizardModel;
import atomicJ.gui.rois.ROIWizardReceiver;

public class CalibrationSelectionWizardModel extends ExperimentalWizardModel
{  
    private final static  WizardPageDescriptor sourceSelectionDescriptor = new WizardPageDescriptor("Image selection", "Choose an image of the sample", true, false, false, "<<   Back", "Next   >>");
    private final static WizardPageDescriptor channelSelectionDescriptor = new WizardPageDescriptor("Channel selection", "Select image channel", false, false, false, "<<   Back", "Next   >>");
    private final static WizardPageDescriptor roiSelectionDescriptor = new WizardPageDescriptor("Select substrate", 
            "Select substrate as ROI. This curves will be used for calibration.", false, true, false, "<<   Back", "Next   >>");


    public CalibrationSelectionWizardModel(PreviewDestination previewDestination,
            ROIWizardReceiver roiImageReceiver) 
    {
        super(previewDestination, roiImageReceiver, PermissiveChannelFilter.getInstance(),
                sourceSelectionDescriptor, channelSelectionDescriptor,
                roiSelectionDescriptor, true);
    }
}
