package atomicJ.gui.annotations;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import atomicJ.gui.AbstractWizardPage;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardPage;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionPanel;
import atomicJ.sources.IdentityTag;

public class ExportAnnotationPage extends AbstractWizardPage implements WizardPage, PropertyChangeListener
{   
    private static final String IDENTIFIER = "ROI exporting";
    private final Preferences pref;

    private final JLabel labelObjects = new JLabel();
    private final JTextField fieldDestination = new JTextField("", 20);
    private final ExtensionFileChooser chooser;
    private final JPanel view;

    private final MultipleSelectionPanel<IdentityTag, MultipleSelectionModel<IdentityTag>> selectionPanel = new MultipleSelectionPanel<>();

    private ExportAnnotationModel<?> model;

    public ExportAnnotationPage()
    {
        this.pref  = Preferences.userRoot().node(getClass().getName());
        this.chooser = new ExtensionFileChooser(pref, true);

        this.view = buildView();
    }

    private SubPanel buildView()
    {
        SubPanel view = new SubPanel();

        view.addComponent(labelObjects, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        view.addComponent(selectionPanel.getView(), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);
        view.addComponent(selectionPanel.getControls(), 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.5, 0);

        SubPanel panelSettings = new SubPanel();

        JLabel labelDestDirectory = new JLabel("Destination");

        panelSettings.addComponent(labelDestDirectory, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSettings.addComponent(fieldDestination, 1, 0, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));

        JButton buttonBrowse = new JButton(new BrowseAction());

        view.addComponent(panelSettings, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        view.addComponent(buttonBrowse, 1, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.5, 0, new Insets(5, 3, 5, 3));

        return view;
    }

    public void setModel(ExportAnnotationModel<?> modelNew)
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        modelNew.addPropertyChangeListener(this);

        selectionPanel.setModel(modelNew.getMergeObjectsModel());

        pullModelProperties();
    }


    private void browse()
    {
        Window ancestor = SwingUtilities.getWindowAncestor(view);
        int op = chooser.showDialog(ancestor, "Select");

        if (op == JFileChooser.APPROVE_OPTION) 
        {
            File directoryNew = chooser.getSelectedFile();
            String path = directoryNew.getPath();
            fieldDestination.setText(path);
            model.setFilePath(directoryNew);
        }
    }

    private void pullModelProperties()
    {
        File saveFile = model.getFilePath();
        String pathNew = (saveFile != null) ? saveFile.getAbsolutePath() : "";
        fieldDestination.setText(pathNew);

        labelObjects.setText(model.getObjectListLabel());
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

        if(ExportAnnotationModel.FILE_PATH.equals(property))
        {
            File newFile = (File)evt.getNewValue();
            String pathNew = (newFile != null) ? newFile.getAbsolutePath() : "";

            fieldDestination.setText(pathNew);
        }
    }

    private class BrowseAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BrowseAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Browse");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            browse();
        }
    }
}
