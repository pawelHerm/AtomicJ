package atomicJ.gui.generalProcessing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIReceiver;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.sources.IdentityTag;

public abstract class OperationModel extends BasicOperationModel implements ROIReceiver
{
    public static final String ROI_RELATIVE_POSITION = "ROIRelativePosition";
    public static final String ROI_SELECTED = "ROISelected";
    public static final String ROI_SELECTION_ENABLED = "ROISelectionEnabled";
    public static final String AVAILABLE_ROI_IDENTITIES = "AvailableROIIdentities";
    public static final String ROIS_AVAILABLE = "ROIsAvailable";

    private ROIRelativePosition position = ROIRelativePosition.EVERYTHING;
    private IdentityTag selectedROIIdentity = null;

    private boolean roisAvailable;
    private boolean roiSelectionEnabled;

    private Map<Object, ROI> rois;
    private ROI roiUnion;

    public OperationModel(Map<Object, ROI> rois, ROI roiUnion)
    {
        this.rois = new LinkedHashMap<>(rois);
        this.roiUnion = roiUnion;
        this.roisAvailable = !rois.isEmpty();

        this.roiSelectionEnabled = calculateROISelectionEnabled();

        this.selectedROIIdentity = (roiUnion != null) ? roiUnion.getIdentityTag(): null;
    }

    public ROIRelativePosition getROIPosition() {
        return position;
    }

    public void setPositionRelativeToROI(ROIRelativePosition positionNew) {

        if(!Objects.equals(this.position, positionNew))
        {
            ROIRelativePosition positionOld = this.position;
            this.position = positionNew;

            handleChangeOfPosition();

            firePropertyChange(OperationModel.ROI_RELATIVE_POSITION, positionOld, positionNew);
            checkIfROISelectionEnabled();
        }
    }

    protected void handleChangeOfPosition() {}

    public IdentityTag getSelectedROIIdentity()
    {
        return selectedROIIdentity;
    }

    public void setSelectedROIIdentity(IdentityTag identityNew) {

        if(!Objects.equals(this.selectedROIIdentity, identityNew))
        {
            IdentityTag identityOld = this.selectedROIIdentity;
            this.selectedROIIdentity = identityNew;

            handleChangeOfSelectedROI();

            firePropertyChange(OperationModel.ROI_SELECTED, identityOld, identityNew);
        }
    }

    private ROI identifySelectedROI(IdentityTag idManager)
    {
        if(idManager == null)
        {
            return null;
        }

        Map<Object, ROI> availableROIs = getAvailableROIs();
        return availableROIs.get(idManager.getKey());
    }

    public ROI getSelectedROI()
    {
        return identifySelectedROI(selectedROIIdentity);
    }

    protected void handleChangeOfSelectedROI(){};

    private boolean calculateROISelectionEnabled()
    {
        boolean enabled = !this.rois.isEmpty() && (this.position != null && this.position.isROIDependent());
        return enabled;
    }

    private void checkIfROISelectionEnabled()
    {
        boolean enabledOld = this.roiSelectionEnabled;
        this.roiSelectionEnabled = calculateROISelectionEnabled();

        if(enabledOld != roiSelectionEnabled)
        {
            firePropertyChange(ROI_SELECTION_ENABLED, enabledOld, roiSelectionEnabled);
        }
    }

    private void checkIfROIsAvailable()
    {
        boolean roisAvailableOld = this.roisAvailable;
        this.roisAvailable = !rois.isEmpty();

        if(roisAvailableOld != this.roisAvailable)
        {
            firePropertyChange(ROIS_AVAILABLE, roisAvailableOld, this.roisAvailable);
        }
    }

    public boolean isROISelectionEnabled()
    {
        return roiSelectionEnabled;
    }

    public boolean areROIsAvailable()
    {
        return roisAvailable;
    }

    public Map<Object, ROI> getAvailableROIs()
    {
        Map<Object, ROI> rois = new LinkedHashMap<>();

        rois.put(roiUnion.getKey(), roiUnion);       
        rois.putAll(this.rois);

        return rois;
    }

    public List<IdentityTag> getAvailableROIIdentities()
    {
        List<IdentityTag> idManagers = new ArrayList<>();

        Map<Object, ROI> rois = getAvailableROIs();

        for(ROI roi : rois.values())
        {
            idManagers.add(roi.getIdentityTag());
        }

        return idManagers;
    }

    @Override
    public void setROIs(Map<Object, ROIDrawable> rois)
    {
        List<IdentityTag> idsOld = getAvailableROIIdentities();

        this.rois = new LinkedHashMap<>();
        this.rois.putAll(rois);

        this.roiUnion = ROIComposite.getROIForRois(rois, "All");

        List<IdentityTag> idsNew = getAvailableROIIdentities();

        if(!Objects.equals(idsOld, idsNew))
        {
            firePropertyChange(AVAILABLE_ROI_IDENTITIES, idsOld, idsOld);
        }

        verifyIdentityTag();

        checkIfROIsAvailable();
        checkIfROISelectionEnabled();
    };

    @Override
    public void addOrReplaceROI(ROIDrawable roi)
    {
        List<IdentityTag> idsOld = getAvailableROIIdentities();

        this.rois.put(roi.getKey(), roi);
        this.roiUnion = ROIComposite.getROIForRois(rois, "All");

        List<IdentityTag> idsNew = getAvailableROIIdentities();

        if(!Objects.equals(idsOld, idsNew))
        {
            firePropertyChange(AVAILABLE_ROI_IDENTITIES, idsOld, idsOld);
        }

        checkIfROIsAvailable();
        checkIfROISelectionEnabled();
    };

    @Override
    public void removeROI(ROIDrawable roi) 
    {
        List<IdentityTag> idsOld = getAvailableROIIdentities();

        this.rois.remove(roi.getKey());
        this.roiUnion = ROIComposite.getROIForRois(rois, "All");

        List<IdentityTag> idsNew = getAvailableROIIdentities();

        if(!Objects.equals(idsOld, idsNew))
        {
            firePropertyChange(AVAILABLE_ROI_IDENTITIES, idsOld, idsOld);
        }

        verifyIdentityTag();

        checkIfROIsAvailable();
        checkIfROISelectionEnabled();
    };

    @Override
    public void changeROILabel(Object roiKey, String labelOld, String labelNew) 
    {
        List<IdentityTag> idsOld = getAvailableROIIdentities();

        ROI roi = this.rois.get(roiKey);
        if(roi != null)
        {
            roi.setLabel(labelNew);
        }

        List<IdentityTag> idsNew = getAvailableROIIdentities();

        if(!Objects.equals(idsOld, idsNew))
        {
            firePropertyChange(AVAILABLE_ROI_IDENTITIES, idsOld, idsOld);
        }

        checkIfROISelectionEnabled();
    };

    private void verifyIdentityTag()
    {
        if(selectedROIIdentity != null)
        {
            Object key = selectedROIIdentity.getKey();
            if(!rois.containsKey(key))
            {
                setSelectedROIIdentity(null);
            }
        }
    }

    //ugly hack, actually it would be better either to introduce a new interface ROIUnionReceiver
    //or somehow parametrize the interface ROIReceiver (i.e. ROIRceiver<E extends ROI>)
}
