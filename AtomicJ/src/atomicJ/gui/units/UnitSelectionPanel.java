
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
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

package atomicJ.gui.units;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.MultiMap;
import atomicJ.utilities.Validation;

public class UnitSelectionPanel extends JPanel implements UnitSourceListener
{
    private static final long serialVersionUID = 1L;

    private StandardUnitSource model;
    private final Map<IdentityTag, JComboBox<PrefixedUnit>> combos = new LinkedHashMap<>();
    private JCheckBox boxUseDefaultUnits;

    private final JPanel panelControls = new JPanel();
    private JPanel panelBounding;

    private boolean initUseDefaultUnits;
    private Map<IdentityTag, PrefixedUnit> initSelectedUnits = new LinkedHashMap<>();

    private boolean boundUnits = false;
    private boolean unitBoundingPossible;

    public UnitSelectionPanel()
    {
        this.panelBounding = null;
        setLayout(new BorderLayout());
    }

    public UnitSelectionPanel(StandardUnitSource model)
    {		
        this.unitBoundingPossible = model.isUnitsCanBeBound();
        this.panelBounding = unitBoundingPossible ?  buildLinkPanel() : null;

        setLayout(new BorderLayout());
        setModel(model);
    }

    public StandardUnitSource getModel()
    {
        return model;
    }

    public void setModel(StandardUnitSource modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(this.model != null)
        {
            model.removeUnitSourceListener(this);
        }

        this.model = modelNew;
        model.addUnitSourceListener(this);	

        pullModelProperties();

        buildNewCombos();
        buildLayout();
    }

    public void cleanUp()
    {
        if(model != null)
        {
            model.removeUnitSourceListener(this);
        }

        this.model = null;        
    }

    private void pullModelProperties()
    {
        this.initUseDefaultUnits = model.isUseDefaultUnits();
        this.initSelectedUnits = model.getSelectedUnits();
    }

    public void reset()
    {
        if(initUseDefaultUnits)
        {
            model.setUseDefaultUnits(initUseDefaultUnits);
        }
        else
        {
            for(Entry<IdentityTag, PrefixedUnit> entry : initSelectedUnits.entrySet())
            {            
                model.setSelectedUnit(entry.getKey(), entry.getValue());
            }
        }
    }

    private void buildLayout()
    {		
        JPanel panelControls = new JPanel();

        JLabel labelDatasets = new JLabel("Column units");

        GroupLayout layout = new GroupLayout(panelControls);
        panelControls.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);


        boxUseDefaultUnits = new JCheckBox();
        boxUseDefaultUnits.setSelected(model.isUseDefaultUnits());
        setCombosEnabled(!model.isUseDefaultUnits());

        boxUseDefaultUnits.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setUseDefaultUnits(selected);
            }
        });

        JLabel labelDefaultUnits = new JLabel("Use default units");

        //builds and sets horizontal group
        SequentialGroup horizontalGroup = layout.createSequentialGroup();
        ParallelGroup innerHorizontalGroup = layout.createParallelGroup().addComponent(labelDatasets);

        SequentialGroup boxRowH = layout.createSequentialGroup()
                .addComponent(labelDefaultUnits)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(boxUseDefaultUnits);
        innerHorizontalGroup.addGroup(boxRowH);

        Map<IdentityTag, JLabel> comboLabels = buildLabels();

        for(IdentityTag key: combos.keySet())
        {
            JLabel label = comboLabels.get(key);
            JComboBox<PrefixedUnit> combo = combos.get(key);

            SequentialGroup comboRowH = layout.createSequentialGroup().addComponent(label).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(combo);
            innerHorizontalGroup.addGroup(comboRowH);
        }

        horizontalGroup
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(innerHorizontalGroup)
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setHorizontalGroup(horizontalGroup);

        //builds and sets vertical group

        SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(labelDatasets).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);

        //default units box


        ParallelGroup boxRowV = layout.createParallelGroup()
                .addComponent(labelDefaultUnits).addComponent(boxUseDefaultUnits);


        verticalGroup.addGroup(boxRowV).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);

        ///////////////////////////////////

        for(IdentityTag key: combos.keySet())
        {
            JLabel label = comboLabels.get(key);
            JComboBox<PrefixedUnit> combo = combos.get(key);
            ParallelGroup comboRowV = layout.createParallelGroup().addComponent(label).addComponent(combo);

            verticalGroup.addGroup(comboRowV).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        }

        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setVerticalGroup(verticalGroup);

        if(!combos.isEmpty())
        {
            layout.linkSize(combos.values().toArray(new JComboBox[] {}));
        }

        if(!comboLabels.isEmpty())
        {
            layout.linkSize(comboLabels.values().toArray(new JLabel[] {}));
        }

        removeAll();

        add(panelControls, BorderLayout.CENTER);
        if(unitBoundingPossible)
        {
            add(panelBounding, BorderLayout.EAST);
        }

        revalidate();
        repaint();

        Window ancestor = SwingUtilities.getWindowAncestor(this);
        if(ancestor != null)
        {
            ancestor.pack();
        }
    }

    private Map<IdentityTag, JLabel> buildLabels()
    {
        Map<IdentityTag, JLabel> labels = new LinkedHashMap<>();

        for(IdentityTag group : combos.keySet())
        {
            labels.put(group, new JLabel(group.getLabel()));
        }

        return labels;
    }

    private void buildNewCombos()
    {
        combos.clear();

        final MultiMap<IdentityTag, PrefixedUnit> allGroupUnits = model.getAllProposedUnits();

        for(final Entry<IdentityTag, List<PrefixedUnit>> entry: allGroupUnits.entrySet())
        { 
            final IdentityTag group = entry.getKey();
            final JComboBox<PrefixedUnit> button = new JComboBox<>(entry.getValue().toArray(new PrefixedUnit[] {}));

            combos.put(group, button);

            PrefixedUnit selectedUnit = model.getSelectedUnit(group);
            button.setSelectedItem(selectedUnit);

            button.addItemListener(new ItemListener() 
            {              
                @Override
                public void itemStateChanged(ItemEvent e) 
                {
                    PrefixedUnit unit = (PrefixedUnit) button.getSelectedItem();
                    if(boundUnits)
                    {
                        for(IdentityTag group : allGroupUnits.keySet())
                        {
                            model.setSelectedUnit(group, unit);
                        }
                    }
                    else
                    {
                        model.setSelectedUnit(group, unit);                  
                    }
                }
            });
        }		
    }

    private JPanel buildLinkPanel()
    {
        JToggleButton buttonLinkStyle = new JToggleButton(new BoundStyleAction());
        buttonLinkStyle.setHideActionText(true);
        buttonLinkStyle.setMargin(new Insets(3, 5, 3, 5));

        JPanel panelLink = new JPanel();
        panelLink.add(buttonLinkStyle);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(
                toolkit.getImage("Resources/chainLinks.png"));

        buttonLinkStyle.setSelectedIcon(icon);

        panelLink.setLayout(new BoxLayout(panelLink, BoxLayout.PAGE_AXIS));
        panelLink.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelLink.add(Box.createVerticalGlue());
        panelLink.add(buttonLinkStyle);
        panelLink.add(Box.createVerticalGlue());

        return panelLink;
    }


    public Component getControls() 
    {
        return panelControls;
    }

    @Override
    public void unitGroupAdded(IdentityTag group, PrefixedUnit selectedUnit, List<PrefixedUnit> units)

    {
        this.initSelectedUnits.put(group, selectedUnit);

        buildNewCombos();
        buildLayout();
    }

    @Override
    public void unitGroupRemoved(IdentityTag group)
    {
        this.initSelectedUnits.remove(group);

        buildNewCombos();
        buildLayout();
    }

    @Override
    public void unitSelected(IdentityTag group, PrefixedUnit unit) 
    {
        JComboBox<PrefixedUnit> combo = combos.get(group);

        if(combo != null)
        {
            combo.setSelectedItem(unit);
        }        
    }

    public boolean isBoundUnits()
    {
        return boundUnits;
    }

    public void setBoundUnits(boolean boundUnits)
    {
        this.boundUnits = boundUnits;
    }

    private class BoundStyleAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public BoundStyleAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/chainLinksBroken.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Link style");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean bound = (boolean) getValue(SELECTED_KEY);
            setBoundUnits(bound);
        }
    }

    @Override
    public void canBoundUnitsChanged(boolean unitsCanBeBoundOld,
            boolean unitsCanBeBoundNew) 
    {
        if(unitsCanBeBoundNew != unitBoundingPossible)
        {
            this.panelBounding = unitBoundingPossible ?  buildLinkPanel() : null;
            buildLayout();
        }
    }

    private void setCombosEnabled(boolean enabled)
    {
        for(JComboBox<?> combo : combos.values())
        {
            combo.setEnabled(enabled);
        }
    }

    @Override
    public void useDefaultUnitsChanged(boolean useDefaultUnitsOld,
            boolean useDefaultUnitsNew) 
    {
        boxUseDefaultUnits.setSelected(useDefaultUnitsNew);
        setCombosEnabled(!useDefaultUnitsNew);
    }
}
