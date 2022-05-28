package atomicJ.gui.rois;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import atomicJ.gui.AbstractWizardPage;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardPage;
import atomicJ.gui.selection.single.SingleSelectionModel;
import atomicJ.gui.selection.single.SingleSelectionPanel;
import atomicJ.sources.IdentityTag;

public class DifferenceROIPage extends AbstractWizardPage implements WizardPage, PropertyChangeListener
{   
    private static final String IDENTIFIER = "ROI difference";

    private final SingleSelectionPanel<IdentityTag, SingleSelectionModel<IdentityTag>> mainROIPanel = new SingleSelectionPanel<>();
    private final DifferenceROISubtractedROIInterfaceManager<IdentityTag, IdentityTag> subtractedROIsPanel = new DifferenceROISubtractedROIInterfaceManager<>();

    private final JCheckBox boxDeleteSubtractedROIs = new JCheckBox("Delete subtracted ROIs");
    private final JPanel view;
    private DifferenceROIModel<?> model;

    public DifferenceROIPage()
    {
        this.view = buildView();
        initItemListener();
    }

    private JPanel buildView()
    {
        SubPanel view = new SubPanel();

        SubPanel panelSubtractedROIsFull = new SubPanel();

        panelSubtractedROIsFull.addComponent(subtractedROIsPanel.getView(), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);
        panelSubtractedROIsFull.addComponent(subtractedROIsPanel.getControls(), 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.5, 0);

        JLabel labelMainROI = new JLabel("Subtract from ROI");
        JLabel labelSubtractedROIs = new JLabel("ROI to subtract");

        view.addComponent(labelMainROI, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        view.addComponent(mainROIPanel, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        view.addComponent(labelSubtractedROIs, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        view.addComponent(panelSubtractedROIsFull, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        SubPanel panelSettings = new SubPanel();
        panelSettings.addComponent(boxDeleteSubtractedROIs, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        view.addComponent(panelSettings, 0, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        return view;
    }

    public void setModel(DifferenceROIModel<?> modelNew)
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        modelNew.addPropertyChangeListener(this);

        mainROIPanel.setModel(modelNew.getMainROIModel());
        subtractedROIsPanel.setModel(modelNew.getSubtractedROIsModel());

        pullModelProperties();
    }

    private void pullModelProperties()
    {
        boolean deleteSubtractedROIs = model.isDeleteSubtractedROIs();
        boxDeleteSubtractedROIs.setSelected(deleteSubtractedROIs);
    }

    private void initItemListener()
    {
        boxDeleteSubtractedROIs.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setDeleteSubtractedROIs(selected);
            }
        });
    }

    @Override
    public String getTaskName() 
    {
        return model.getTaskName();
    }

    @Override
    public String getTaskDescription() 
    {
        return model.getTaskDescription();
    }

    @Override
    public String getIdentifier() 
    {
        return IDENTIFIER;
    }

    @Override
    public boolean isFirst() 
    {
        return model.isFirst();
    }

    @Override
    public boolean isLast() 
    {
        return model.isLast();
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return model.isNecessaryInputProvided();
    }

    @Override
    public Component getView() 
    {
        return view;
    }

    @Override
    public Component getControls() {
        return new JPanel();
    }

    @Override
    public boolean isBackEnabled() 
    {
        return model.isBackEnabled();
    }

    @Override
    public boolean isNextEnabled() 
    {
        return model.isNextEnabled();
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return model.isBackEnabled();
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return model.isFinishEnabled();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();

        if(DifferenceROIModel.DELETE_SUBTRACTED_ROIS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxDeleteSubtractedROIs.isSelected();

            if(valOld != valNew)
            {
                boxDeleteSubtractedROIs.setSelected(valNew);
            }
        }
    }
}
