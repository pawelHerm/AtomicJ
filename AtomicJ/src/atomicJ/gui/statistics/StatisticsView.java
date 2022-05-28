
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

package atomicJ.gui.statistics;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import atomicJ.gui.ColumnVisibilityDialog;
import atomicJ.gui.DataViewListener;
import atomicJ.gui.DataViewSupport;
import atomicJ.gui.NumericalFormatDialog;
import atomicJ.gui.NumericalTableExporter;
import atomicJ.gui.MinimalNumericalTable;
import atomicJ.gui.TextFileChooser;
import atomicJ.gui.units.UnitMultiSelectionDialog;
import atomicJ.gui.units.UnitSelectionPanel;
import atomicJ.utilities.GUIUtilities;


public class StatisticsView implements PropertyChangeListener
{
    private static final int DEFAULT_HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().height/2);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);
    private static final int DEFAULT_LOCATION_X = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/4);
    private static final int DEFAULT_LOCATION_Y = Math.round(Toolkit.getDefaultToolkit().getScreenSize().height/4);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private final Action saveAction = new SaveAction();
    private final Action printAction = new PrintAction();

    private final Action customizeFormatAction = new CustomizeFormatAction();
    private final Action selectUnitsAction = new SelectUnitsAction();
    private final Action visibilityAction = new ChangeVisibilityAction();

    private final TextFileChooser fileChooser = new TextFileChooser();

    private final Map<String,StatisticsTable> tables = new LinkedHashMap<>();
    private final Map<String, JScrollPane> tableScrollPanes = new LinkedHashMap<>();

    private final JTabbedPane tablesPane = new JTabbedPane();

    private final boolean temporary;
    private String currentKey;

    private final UnitMultiSelectionDialog unitSelectionDialog;
    private final DataViewSupport dataViewSupport = new DataViewSupport();

    private final JDialog mainDialog;

    public StatisticsView(Window parent, String title, boolean temporary)
    {
        this(parent, Collections.emptyMap(), title, temporary);
    }

    public StatisticsView(Window parent, Map<String, StatisticsTable> tables, String title, boolean temporary)
    {
        mainDialog = new JDialog(parent,title,Dialog.ModalityType.MODELESS);
        mainDialog.setLayout(new BorderLayout(1,5));

        this.temporary = temporary;
        if(temporary)
        {
            mainDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        Map<String, UnitSelectionPanel> unitPanels = new LinkedHashMap<>();
        for(String key: tables.keySet())
        {
            StatisticsTable table = tables.get(key);
            unitPanels.put(key, table.getUnitSelectionPanel());
            initTable(table, key);
        }

        this.unitSelectionDialog = new UnitMultiSelectionDialog(mainDialog, unitPanels);

        int index = tablesPane.getSelectedIndex();
        this.currentKey = index > -1 ? tablesPane.getTitleAt(index) : null;

        initChangeListeners();
        tablesPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,12,10,12),tablesPane.getBorder()));

        updateActionsState();

        JMenuBar menuBar = buildMenuBar();
        mainDialog.setJMenuBar(menuBar);	

        mainDialog.add(tablesPane,BorderLayout.CENTER);

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
        mainDialog.add(buttonGroupResultsContainer,  BorderLayout.SOUTH);

        initListeners();

        int height = pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width = pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        int locationX = pref.getInt(WINDOW_LOCATION_X, DEFAULT_LOCATION_X);
        int locationY = pref.getInt(WINDOW_LOCATION_Y, DEFAULT_LOCATION_Y);

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        { 
            mainDialog.setSize(width,height);
            mainDialog.setLocation(locationX,locationY);
        }
        else
        {
            mainDialog.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            mainDialog.setLocation(DEFAULT_LOCATION_X, DEFAULT_LOCATION_Y);
        } 
    }


    private void initListeners()
    {
        mainDialog.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {               
                pref.putInt(WINDOW_HEIGHT, mainDialog.getHeight());
                pref.putInt(WINDOW_WIDTH, mainDialog.getWidth());
                pref.putInt(WINDOW_LOCATION_X, (int) mainDialog.getLocation().getX());         
                pref.putInt(WINDOW_LOCATION_Y, (int) mainDialog.getLocation().getY());

                dataViewSupport.fireDataViewVisiblityChanged(false);
            }

            @Override
            public void componentShown(ComponentEvent evt)
            {
                dataViewSupport.fireDataViewVisiblityChanged(true);
            }
        });
    }

    public boolean isVisible()
    {
        return mainDialog.isVisible();
    }

    public void setVisible(boolean visible)
    {
        mainDialog.setVisible(visible);
    }

    private void initChangeListeners()
    {
        tablesPane.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent e)
            {
                int index = tablesPane.getSelectedIndex();
                currentKey = tablesPane.getTitleAt(index);
                updateActionsState();                
            }
        });
    }

    private void initTable(StatisticsTable table, String key)
    {
        table.addPropertyChangeListener(this);      

        this.tables.put(key, table);

        JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        tableScrollPanes.put(key, scrollPane);
        tablesPane.add(scrollPane, key);

    }

    private JMenuBar buildMenuBar()
    {
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

        return menuBar;
    }

    public void addDataViewListener(DataViewListener listener)
    {
        dataViewSupport.addDataViewListener(listener);
    }

    public void removeDataViewListener(DataViewListener listener)
    {
        dataViewSupport.removeDataViewListener(listener);
    }

    protected void fireDataAvailabilityChanged(boolean dataAvailableNew)
    {
        dataViewSupport.fireDataAvailabilityChanged(dataAvailableNew);
    }    



    public StatisticsTableModel getTableModel(String key)
    {
        StatisticsTable table = tables.get(key);
        StatisticsTableModel model = (table != null) ? table.getModel(): null;
        return model;
    }

    public void addOrReplaceTableModel(StatisticsTableModel model, String key)
    {
        if(model == null)
        {
            return;
        }

        StatisticsTable table = tables.get(key);
        if(table == null)
        {
            table = new StatisticsTable(model);
            initTable(table, key);
        }
        else
        {
            table.setModel(model);
        }

        unitSelectionDialog.addUnitSource(key, table.getModel().getUnitSource());
    }


    public void setTableModels(Map<String, StatisticsTableModel> models)
    {
        for(Entry<String, StatisticsTableModel> entry : models.entrySet())
        {
            String key = entry.getKey();
            StatisticsTableModel model = entry.getValue();
            addOrReplaceTableModel(model, key);
        }

        setVisibleTables(models.keySet());
    }

    public void setSelectedType(String type)
    {
        int tabCount = tablesPane.getTabCount();
        for(int i = 0; i <tabCount; i++) 
        {
            String title = tablesPane.getTitleAt(i);
            if(title.equals(type)) 
            {
                tablesPane.setSelectedIndex(i);
                return;
            }
        }
    }

    private void ensureThatTableIsVisible(String type)
    {
        boolean visible = false;
        int tabCount = tablesPane.getTabCount();
        for(int i = tabCount - 1; i >= 0; i--) 
        {
            String title = tablesPane.getTitleAt(i);
            if(title.equals(type)) 
            {
                visible = true;
                break;
            }
        }
        if(!visible)
        {
            JScrollPane pane = tableScrollPanes.get(type);
            tablesPane.addTab(type, pane);
        }
    }

    private void ensureThatTableIsHidden(String type)
    {
        int tabCount = tablesPane.getTabCount();

        for(int i = tabCount - 1; i >= 0; i--) 
        {
            String title = tablesPane.getTitleAt(i);
            if(title.equals(type)) 
            {
                tablesPane.removeTabAt(i);
                break;
            }
        }
    }	

    public void setVisibleTables(Set<String> visibleTypes)
    {
        List<String> typesStillToAdd = new ArrayList<>(visibleTypes);

        int tabCount = tablesPane.getTabCount();
        for(int i = tabCount - 1; i >= 0; i--) 
        {
            String title = tablesPane.getTitleAt(i);
            if(!visibleTypes.contains(title)) 
            {
                tablesPane.removeTabAt(i);
            }
            else
            {
                typesStillToAdd.remove(title);
            }
        }

        for(String type: typesStillToAdd)
        {
            JScrollPane pane = tableScrollPanes.get(type);
            tablesPane.addTab(type, pane);
        }
    }


    private void customizeFormat()
    {
        StatisticsTable table = tables.get(currentKey);
        NumericalFormatDialog customizeDialog = new NumericalFormatDialog(mainDialog, table, "Customize statistics table");
        customizeDialog.setVisible(true);
    }

    private void changeColumnVisibility()
    {
        StatisticsTable table = tables.get(currentKey);
        ColumnVisibilityDialog hideColumnsDialog = ColumnVisibilityDialog.getDialog(mainDialog, table, table.getColumnShortNames());
        hideColumnsDialog.setVisible(true);
    }

    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            StatisticsTable table = tables.get(currentKey);

            if(table.getModel().getRowCount() > 0)
            {
                File path = table.getDefaultOutputDirectory();

                fileChooser.setCurrentDirectory(path);
                fileChooser.setSelectedFile(new File(currentKey));
                int op = fileChooser.showSaveDialog(mainDialog.getParent());

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
                        JOptionPane.showMessageDialog(mainDialog, "Error encountered while saving", "", JOptionPane.ERROR_MESSAGE);
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
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME, "Print");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            try 
            {
                StatisticsTable table = tables.get(currentKey);
                table.print();
            } 
            catch (PrinterException pe) 
            {
                JOptionPane.showMessageDialog(mainDialog, pe.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class InferenceAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public InferenceAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
            putValue(NAME,"Inference");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {

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
            customizeFormat();
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
            changeColumnVisibility();
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
                mainDialog.dispose();
            }
            else
            {
                mainDialog.setVisible(false);
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
        boolean enabled = false;
        if(currentKey != null)
        {
            StatisticsTable table = tables.get(currentKey);
            enabled = (table == null) ? false : !table.isEmpty();
        }

        saveAction.setEnabled(enabled);
        printAction.setEnabled(enabled);
        customizeFormatAction.setEnabled(enabled);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {		
        String name = evt.getPropertyName();

        if(MinimalNumericalTable.TABLE_EMPTY.equals(name))
        {
            updateActionsState();
            dataViewSupport.fireDataViewVisiblityChanged(!(Boolean)evt.getNewValue());
        }
    }
}
