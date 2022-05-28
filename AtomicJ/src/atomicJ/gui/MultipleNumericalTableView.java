
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_X;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_Y;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import atomicJ.gui.units.UnitMultiSelectionDialog;
import atomicJ.gui.units.UnitSelectionPanel;
import atomicJ.utilities.GUIUtilities;

public class MultipleNumericalTableView implements PropertyChangeListener
{
    private static final int DEFAULT_HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().height/2);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);
    private static final int DEFAULT_LOCATION_X = Math.round(2*Toolkit.getDefaultToolkit().getScreenSize().width/3);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private final SaveAction saveAction = new SaveAction();
    private final PrintAction printAction = new PrintAction();
    private final CustomizeFormatAction customizeFormatAction = new CustomizeFormatAction();
    private final Action selectUnitsAction = new SelectUnitsAction();
    private final ChangeVisibilityAction visibilityAction = new ChangeVisibilityAction();

    private final TextFileChooser fileChooser = new TextFileChooser();

    private final Map<String, StandardNumericalTable> tables;

    private final Map<String, NumericalFormatDialog> customizeDialogs = new Hashtable<>();
    private final Map<String, ColumnVisibilityDialog> hideColumnsDialogs = new Hashtable<>();
    private final JTabbedPane tablesPane = new JTabbedPane();

    private final boolean temporary;
    private String currentKey;

    private final UnitMultiSelectionDialog unitSelectionDialog;
    private final JDialog viewDialog;

    public MultipleNumericalTableView(Window parent, Map<String,StandardNumericalTable> tables, String title, boolean temporary)
    {
        viewDialog = new JDialog(parent,title,Dialog.ModalityType.MODELESS);
        viewDialog.setLayout(new BorderLayout(1,5));

        this.temporary = temporary;
        if(temporary)
        {
            viewDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        this.tables = tables;
        Map<String, UnitSelectionPanel> unitPanels = new LinkedHashMap<>();

        for(String key: tables.keySet())
        {
            StandardNumericalTable table = tables.get(key);
            table.addPropertyChangeListener(this);
            NumericalFormatDialog customizeDialog = new NumericalFormatDialog(viewDialog, table, "Customize number format");
            ColumnVisibilityDialog hideColumnsDialog = ColumnVisibilityDialog.getDialog(viewDialog, table, table.getColumnShortNames());
            customizeDialogs.put(key, customizeDialog);
            hideColumnsDialogs.put(key, hideColumnsDialog);
            JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

            tablesPane.add(scrollPane, key);
            unitPanels.put(key, table.getUnitSelectionPanel());
        }

        this.unitSelectionDialog = new UnitMultiSelectionDialog(viewDialog, unitPanels);

        int index = tablesPane.getSelectedIndex();
        this.currentKey = tablesPane.getTitleAt(index);

        tablesPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int index = tablesPane.getSelectedIndex();
                currentKey = tablesPane.getTitleAt(index);
                updateActionsState();                
            }
        });
        tablesPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,12,10,12),tablesPane.getBorder()));

        updateActionsState();

        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);

        JMenuItem itemSave = new JMenuItem(saveAction);
        JMenuItem itemPrint = new JMenuItem(printAction);
        JMenuItem itemClose = new JMenuItem(new CloseAction());

        menuFile.add(itemSave);
        menuFile.add(itemPrint);
        menuFile.addSeparator();
        menuFile.add(itemClose);

        JMenu menuCustomize = new JMenu("Customize");
        menuCustomize.setMnemonic(KeyEvent.VK_U);

        JMenuItem itemCustomizeFormat = new JMenuItem(customizeFormatAction);
        JMenuItem itemSelectUnits = new JMenuItem(selectUnitsAction);
        JMenuItem itemColumnVsibility = new JMenuItem(visibilityAction);

        menuCustomize.add(itemCustomizeFormat);
        menuCustomize.add(itemSelectUnits);
        menuCustomize.add(itemColumnVsibility);

        menuBar.add(menuFile);
        menuBar.add(menuCustomize);	
        menuBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredSoftBevelBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));
        viewDialog.setJMenuBar(menuBar);	

        viewDialog.add(tablesPane,BorderLayout.CENTER);

        JPanel buttonGroupResults = new JPanel(new GridLayout(1, 0, 5, 5));
        JPanel buttonGroupResultsContainer = new JPanel();

        JButton buttonClose = new JButton(new CloseAction());
        JButton buttonShowAll = new JButton(saveAction);
        JButton buttonPrint = new JButton(printAction);

        buttonGroupResults.add(buttonShowAll);
        buttonGroupResults.add(buttonPrint);
        buttonGroupResults.add(buttonClose);
        buttonGroupResultsContainer.add(buttonGroupResults);
        buttonGroupResultsContainer.setBorder(BorderFactory.createRaisedBevelBorder());
        viewDialog.add(buttonGroupResultsContainer,BorderLayout.SOUTH);

        viewDialog.pack();
        viewDialog.setLocationRelativeTo(parent);
        initComponentListener();

        int height = pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width = pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        int locationX = pref.getInt(WINDOW_LOCATION_X, DEFAULT_LOCATION_X);
        int locationY = pref.getInt(WINDOW_LOCATION_Y, DEFAULT_HEIGHT);

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        {
            viewDialog.setSize(width,height);
            viewDialog.setLocation(locationX,locationY);
        }
        else
        {
            viewDialog.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            viewDialog.setLocation(DEFAULT_LOCATION_X, DEFAULT_HEIGHT);
        } 
    }

    public boolean isVisible()
    {
        return viewDialog.isVisible();
    }

    public void setVisible(boolean visible)
    {
        viewDialog.setVisible(visible);
    }

    private void initComponentListener()
    {
        viewDialog.addWindowListener(new WindowAdapter()
        {

            @Override
            public void windowClosing(WindowEvent evt)
            {                               
                pref.putInt(WINDOW_HEIGHT, viewDialog.getHeight());
                pref.putInt(WINDOW_WIDTH, viewDialog.getWidth());
                pref.putInt(WINDOW_LOCATION_X, (int) viewDialog.getLocation().getX());         
                pref.putInt(WINDOW_LOCATION_Y, (int) viewDialog.getLocation().getY());
            }
        });
    }

    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            MinimalNumericalTable table = tables.get(currentKey);

            if(table.getModel().getRowCount() > 0)
            {
                File path = table.getDefaultOutputDirectory();

                fileChooser.setCurrentDirectory(path);
                fileChooser.setSelectedFile(new File(currentKey));
                int op = fileChooser.showSaveDialog(viewDialog.getParent());

                if(op == JFileChooser.APPROVE_OPTION)
                {	
                    try 
                    {                
                        NumericalTableExporter exporter = new NumericalTableExporter(); 
                        File selectedFile = fileChooser.getSelectedFile();

                        String selectedExt = fileChooser.getSelectedExtension();

                        if(TextFileChooser.TSV_EXTENSION.equals(selectedExt))
                        {                          
                            exporter.exportTableAsTSV(table, selectedFile, table.getDecimalFormat());
                        }
                        else
                        {                           
                            exporter.exportTableAsCSV(table, selectedFile, table.getDecimalFormat());
                        }
                    } 
                    catch (IOException ex) 
                    {
                        JOptionPane.showMessageDialog(viewDialog, "Error encountered while saving", "", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }	
        }
    }

    private class PrintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PrintAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME, "Print");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            try 
            {
                MinimalNumericalTable table = tables.get(currentKey);
                table.print();
            } 
            catch (PrinterException pe) 
            {
                JOptionPane.showMessageDialog(viewDialog, pe.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CustomizeFormatAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CustomizeFormatAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(NAME,"Format data");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            NumericalFormatDialog customizeDialog = customizeDialogs.get(currentKey);
            customizeDialog.setVisible(true);
        }
    }

    private class ChangeVisibilityAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ChangeVisibilityAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Column visibility");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            ColumnVisibilityDialog hideColumnsDialog = hideColumnsDialogs.get(currentKey);
            hideColumnsDialog.setVisible(true);
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(temporary)
            {
                viewDialog.dispose();
            }
            else
            {
                viewDialog.setVisible(false);
            }
        }
    }

    private class SelectUnitsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SelectUnitsAction() {
            putValue(NAME, "Select units");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            unitSelectionDialog.setSelectedType(currentKey);
            unitSelectionDialog.setVisible(true);
        }
    }

    private void updateActionsState()
    {
        MinimalNumericalTable table = tables.get(currentKey);

        boolean tableNonEmpty = !table.isEmpty();

        saveAction.setEnabled(tableNonEmpty);
        printAction.setEnabled(tableNonEmpty);
        customizeFormatAction.setEnabled(tableNonEmpty);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {		
        String name = evt.getPropertyName();

        if(MinimalNumericalTable.TABLE_EMPTY.equals(name))
        {
            updateActionsState();
        }
    }
}
