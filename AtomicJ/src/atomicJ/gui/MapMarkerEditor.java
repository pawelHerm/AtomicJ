
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

package atomicJ.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.*;

import atomicJ.gui.editors.FontChooserDialog;
import atomicJ.gui.editors.MarkerStyleReceiver;
import atomicJ.gui.editors.ShapeAndSizeChooser;



public class MapMarkerEditor extends JDialog implements ActionListener, ItemListener, MarkerStyleReceiver
{
    private static final long serialVersionUID = 1L;

    private static final String SELECT_MARKER = "SELECT_MARKER";
    private static final String SELECT_LABEL_FONT = "SELECT_LABEL_FONT";
    private static final String EDIT_STROKE = "EDIT_STROKE";
    private static final String SELECT_PAINT_LABEL = "SELECT_PAINT_LABEL";
    private static final String SELECT_PAINT_FILL = "SELECT_PAINT_FILL";

    private static final Shape[] shapes = PlotStyleUtilities.getAllShapes(); 

    //initial settings

    private boolean initOutlineVisible;

    private int initMarkerIndex;
    private float initMarkerSize;

    private float initLabelOffset;
    private float initLabelLengthwisePosition;
    private MapMarkerLabelType initLabelType;

    private boolean initIsFilled;
    private boolean initLabelVisible;     
    private Font initLabelFont;
    private Paint initLabelPaint;
    private Stroke initStroke;
    private Paint initStrokePaint;
    private Paint initFillPaint; 

    //current settings

    private boolean outlineVisible;

    private int markerIndex;        
    private float markerSize;

    private float labelOffset;
    private float labelLengthwisePosition;
    private MapMarkerLabelType labelType;
    private boolean isFilled;
    private boolean labelVisible;     
    private Font labelFont;
    private Paint paintLabel;
    private Stroke stroke;
    private Paint strokePaint;
    private Paint fillPaint;


    //GUI components

    private final StraightStrokeSample strokeSample = new StraightStrokeSample();
    private final PaintSampleFlexible paintLabelSample = new PaintSampleFlexible();
    private final PaintSampleFlexible paintFillSample = new PaintSampleFlexible();

    private final JComboBox<MapMarkerLabelType> comboLabelType = new JComboBox<>();

    private final FontField fieldLabelFont = new FontField();
    private final JButton buttonSelectLabelFont = new JButton("Select");
    private final JButton buttonEditStroke = new JButton("Edit");
    private final JButton buttonSelectLabelPaint = new JButton("Select");
    private final JButton buttonSelectFillPaint = new JButton("Select");
    private final JCheckBox boxFilled = new JCheckBox();
    private final JCheckBox boxLabelVisible = new JCheckBox();
    private StrokeChooser chooserStrokeFinishedStandard;
    private FontChooserDialog fontChooserDialog;

    private final JLabel shapeLabel;    
    private ShapeAndSizeChooser shapeChooser;
    private final JButton buttonSelectShape = new JButton("Select");

    private final ApplyToAllAction applyToAllAction = new ApplyToAllAction();

    private MapMarkerStyle model;
    private final List<MapMarkerStyle> boundedModels = new ArrayList<>();

    private final boolean modelsFullyBounded;


    public MapMarkerEditor(Window parent) 
    {   
        this(parent, true);
    }

    public MapMarkerEditor(Window parent, boolean modelsFullyBounded) 
    {   
        super(parent, "Marker style", ModalityType.APPLICATION_MODAL);

        this.modelsFullyBounded = modelsFullyBounded;

        this.shapeLabel = buildShapeLabel();

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JPanel finishedPanel = buildFinishedPanel();       

        tabbedPane.add("Finished", finishedPanel);

        add(tabbedPane, BorderLayout.NORTH);

        JPanel buttonPanel = buildButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        updateActionsEnability();

        initActionListener();
        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void updateActionsEnability()
    {
        boolean applyToAllEnabled = isApplyToAllEnabled();
        applyToAllAction.setEnabled(applyToAllEnabled);
    }

    public void setModel(MapMarkerStyle model)
    {
        this.model = model;   
        this.initOutlineVisible = model.isStrokeVisible();

        this.initMarkerIndex = model.getMarkerIndex();
        this.initMarkerSize = model.getMarkerSize();

        this.initLabelOffset = model.getLabelOffset();
        this.initLabelLengthwisePosition = model.getLabelLengthwisePosition();
        this.initLabelType = model.getLabelType();
        this.initIsFilled = model.isFilled();
        this.initLabelVisible = model.isLabelVisible();     
        this.initLabelFont = model.getLabelFont();
        this.initLabelPaint = model.getLabelPaint();
        this.initStroke = model.getStroke();
        this.initStrokePaint = model.getPaint();
        this.initFillPaint = model.getFillPaint(); 

        List<MapMarkerLabelType> labelTypes = model.getSupportedLabelTypes();
        ComboBoxModel<MapMarkerLabelType> comboModel = new DefaultComboBoxModel<>(labelTypes.toArray(new MapMarkerLabelType[] {}));
        comboLabelType.setModel(comboModel);

        setParametersToInitial();        
        resetEditor();
    }

    protected List<MapMarkerStyle> getBoundedModels()
    {
        return boundedModels;
    }   

    public void setBoundedModels(List<MapMarkerStyle> models)
    {
        boundedModels.clear();
        boundedModels.addAll(models);

        updateActionsEnability();
    }


    private void setParametersToInitial()
    {
        this.outlineVisible = initOutlineVisible;

        this.markerIndex = initMarkerIndex;
        this.markerSize = initMarkerSize;

        this.labelOffset = initLabelOffset;
        this.labelLengthwisePosition = initLabelLengthwisePosition;
        this.labelType = initLabelType;
        this.isFilled = initIsFilled;
        this.labelVisible = initLabelVisible;     
        this.labelFont = initLabelFont;
        this.paintLabel = initLabelPaint;
        this.stroke = initStroke;
        this.strokePaint = initStrokePaint;
        this.fillPaint = initFillPaint; 
    }

    private void initActionListener()
    {
        buttonSelectLabelFont.setActionCommand(SELECT_LABEL_FONT);
        buttonEditStroke.setActionCommand(EDIT_STROKE);
        buttonSelectLabelPaint.setActionCommand(SELECT_PAINT_LABEL);
        buttonSelectFillPaint.setActionCommand(SELECT_PAINT_FILL);
        buttonSelectShape.setActionCommand(SELECT_MARKER);
        buttonSelectShape.addActionListener(this);
        buttonSelectLabelFont.addActionListener(this);
        buttonEditStroke.addActionListener(this);
        buttonSelectLabelPaint.addActionListener(this);
        buttonSelectFillPaint.addActionListener(this);
    }

    private void initItemListener()
    {
        comboLabelType.addItemListener(this);
        boxFilled.addItemListener(this);
        boxLabelVisible.addItemListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if(SELECT_LABEL_FONT.equals(command))
        {
            if( fontChooserDialog == null)
            {
                this.fontChooserDialog = new FontChooserDialog(this, "Font selection");
            }

            this.fontChooserDialog.showDialog(new FontReceiver() 
            {

                @Override
                public void setFont(Font newFont) 
                { 
                    labelFont = newFont;
                    fieldLabelFont.setDisplayFont(labelFont);
                    model.setLabelFont(labelFont);
                }

                @Override
                public Font getFont()
                {
                    return labelFont;
                }
            });
        }
        else if(EDIT_STROKE.equals(command))
        {
            attemptStrokeFinishedStandardSelection();
        }

        else if(SELECT_PAINT_LABEL.equals(command))
        {
            Paint paintLabelFinishedStandardNew = JColorChooser.showDialog(this, "Label color", (Color)paintLabel);	        
            if (paintLabelFinishedStandardNew != null) 
            {
                paintLabel = paintLabelFinishedStandardNew;
                paintLabelSample.setPaint(paintLabelFinishedStandardNew);
                model.setPaintLabel(paintLabelFinishedStandardNew)	;		
            }
        }

        else if(SELECT_PAINT_FILL.equals(command))
        {
            Paint paintFillFinishedStandardNew = JColorChooser.showDialog(this, "Fill color", (Color)fillPaint);	        
            if (paintFillFinishedStandardNew != null) 
            {
                fillPaint = paintFillFinishedStandardNew;
                paintFillSample.setPaint(paintFillFinishedStandardNew);
                model.setFillPaint(paintFillFinishedStandardNew);		

                updateShapeLabel();
            }
        }    
        else if(SELECT_MARKER.equals(command))
        {
            attemptShapeSelection();
        }
    }

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(this);
        root.setVisible(false);
    }

    public void attemptShapeSelection() 
    {
        if(shapeChooser == null)
        {
            shapeChooser = new ShapeAndSizeChooser(this, this, shapes);
        }
        shapeChooser.setVisible(true);
    }

    private void attemptStrokeFinishedStandardSelection() 
    {
        if(chooserStrokeFinishedStandard == null)
        {
            chooserStrokeFinishedStandard = new StrokeChooser(this, new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke) stroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    MapMarkerEditor.this.stroke = stroke;
                    strokeSample.setStroke(stroke);
                    model.setStroke(stroke);

                    updateShapeLabel();
                }
                @Override
                public void setStrokePaint(Paint paint)
                {
                    strokePaint = paint;
                    strokeSample.setStrokePaint(paint);
                    model.setPaint(paint);

                    updateShapeLabel();
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return strokePaint;
                }     	
            }
                    );
        }        
        chooserStrokeFinishedStandard.showDialog();
    }


    private void resetEditor()
    {        
        this.comboLabelType.setSelectedItem(labelType);
        this.fieldLabelFont.setDisplayFont(labelFont);
        this.paintLabelSample.setPaint(paintLabel);
        this.paintFillSample.setPaint(fillPaint);
        this.strokeSample.setStroke(stroke);
        this.strokeSample.setStrokePaint(strokePaint);
        this.boxLabelVisible.setSelected(labelVisible);      
        this.boxFilled.setSelected(isFilled);

        updateShapeLabel();
    }

    public void resetPartiallyModel(MapMarkerStyle model)
    {
        model.setStrokeVisible(outlineVisible);
        model.setLabelOffset(labelOffset);
        model.setLabelType(labelType);
        model.setLabelLengthwisePosition(labelLengthwisePosition);
        model.setFilled(isFilled);
        model.setLabelVisible(labelVisible);     
        model.setLabelFont(labelFont);
        model.setStroke(stroke);
    }

    public void resetFullyModel(MapMarkerStyle model)
    {    	
        model.setStrokeVisible(outlineVisible);

        model.setLabelOffset(labelOffset);
        model.setLabelLengthwisePosition(labelLengthwisePosition);
        model.setLabelType(labelType);
        model.setFilled(isFilled);
        model.setLabelVisible(labelVisible);     
        model.setLabelFont(labelFont);
        model.setPaintLabel(paintLabel);
        model.setStroke(stroke);
        model.setPaint(strokePaint);
        model.setFillPaint(fillPaint);
    }

    public void resetToDefaults() 
    {		
        this.outlineVisible = model.isStrokeVisible();

        this.labelOffset = model.getLabelOffset();
        this.labelLengthwisePosition = model.getLabelLengthwisePosition();
        model.setDefaultStyle(labelOffset, labelLengthwisePosition);

        this.labelType = model.getLabelType();

        this.isFilled = model.isFilled();
        this.labelVisible = model.isLabelVisible();     
        this.labelFont = model.getLabelFont();
        this.paintLabel = model.getLabelPaint();
        this.stroke = model.getStroke();
        this.strokePaint = model.getPaint();
        this.fillPaint = model.getFillPaint(); 

        resetEditor();
    }

    public void saveAsDefaults() 
    {	    
        model.saveStyleAsDefault();
    }

    public void applyChangesToAll() 
    {
        if(modelsFullyBounded)
        {
            for(MapMarkerStyle style : boundedModels)
            {
                resetFullyModel(style);
            }
        }
        else
        {
            for(MapMarkerStyle style : boundedModels)
            {
                resetPartiallyModel(style);
            }
        }
    }

    public void undoChanges() 
    {
        setParametersToInitial();
        resetFullyModel(model);
        resetEditor();
    }

    public Component getEditionSite()
    {
        return this;
    }

    public boolean isApplyToAllEnabled()
    {
        return boundedModels.size()>0;
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();

        if(source == comboLabelType)
        {
            MapMarkerLabelType selectedItem = (MapMarkerLabelType) comboLabelType.getSelectedItem();        
            this.labelType = selectedItem;
            model.setLabelType(labelType);
        }
        else
        {
            boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);

            if(source == boxFilled)
            {                       
                this.isFilled = selected;
                model.setFilled(isFilled);
            }
            else if(source == boxLabelVisible)
            {
                this.labelVisible = selected;
                model.setLabelVisible(labelVisible);        
            }
        }     
    }

    private JLabel buildShapeLabel()
    {
        BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);     
        Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Shape shape = shapes[markerIndex];

        g2.setPaint(fillPaint);
        g2.fill(shape);
        g2.setPaint(strokePaint);
        g2.draw(shape);

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        JLabel shapeLabel = new JLabel(shapeString, new ImageIcon(img), SwingConstants.LEFT);
        shapeLabel.setBorder(BorderFactory.createLineBorder(Color.black));

        return shapeLabel;
    }

    private void updateShapeLabel()
    {
        BufferedImage img = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);   

        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Shape shape = shapes[markerIndex];

        g2.setPaint(fillPaint);
        g2.fill(shape);
        g2.setPaint(strokePaint);
        g2.draw(shape);

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        shapeLabel.setText(shapeString);
        shapeLabel.setIcon(new ImageIcon(img));
        shapeLabel.repaint();
    }

    private JPanel buildFinishedPanel()
    {
        JPanel finishedPanel = new JPanel(new BorderLayout());


        SubPanel regularPanel = new SubPanel();

        regularPanel.addComponent(new JLabel("Label"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(boxLabelVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("Label type"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(comboLabelType, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);        

        regularPanel.addComponent(new JLabel("Font"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(fieldLabelFont, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectLabelFont, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .05, 0);

        regularPanel.addComponent(new JLabel("Label color"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(paintLabelSample, 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectLabelPaint, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .05, 0);

        regularPanel.addComponent(new JLabel("Filled"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(boxFilled, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("Fill color"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(paintFillSample, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectFillPaint, 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .05, 0);

        regularPanel.addComponent(new JLabel("Stroke"), 0, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(strokeSample, 1, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonEditStroke, 2, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .05, 0);

        regularPanel.addComponent(new JLabel("Marker style"), 0, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        regularPanel.addComponent(shapeLabel, 1, 7, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        regularPanel.addComponent(buttonSelectShape, 2, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .05, 1);

        finishedPanel.add(regularPanel, BorderLayout.NORTH);
        finishedPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 10));

        return finishedPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonClose = new JButton(new CloseAction());

        JButton buttonApplyToAll = new JButton(applyToAllAction);

        JButton buttonSave = new JButton(new SaveAction());
        JButton buttonReset = new JButton(new ResetAction());
        JButton buttonUndo = new JButton(new UndoAction());

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonApplyToAll).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClose).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonUndo));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonApplyToAll)
                .addComponent(buttonSave)
                .addComponent(buttonReset)
                .addComponent(buttonClose)
                .addComponent(buttonUndo));

        layout.linkSize(buttonClose,buttonApplyToAll, buttonSave, buttonReset, buttonUndo);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanel;
    }

    private class ApplyToAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyToAllAction()
        {
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME,"Batch apply");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            applyChangesToAll();
            JOptionPane.showMessageDialog(MapMarkerEditor.this, "ROI style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            hideRoot();
        }
    }

    private class UndoAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public UndoAction()
        {
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME,"Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            undoChanges();
        }
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            resetToDefaults();
        }
    }

    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            saveAsDefaults();
            JOptionPane.showMessageDialog(MapMarkerEditor.this, "ROI style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);        
        };
    }

    @Override
    public Paint getMarkerFillPaint()
    {
        return fillPaint;
    }

    public void setPaint(Paint paint) 
    {
        this.fillPaint = paint;
        model.setFillPaint(paint);
    }

    @Override
    public int getMarkerIndex() {
        return markerIndex;
    }

    @Override
    public void setMarkerIndex(int markerIndexNew) 
    {
        this.markerIndex = markerIndexNew;                   
        model.setMarkerIndex(markerIndexNew);

        updateShapeLabel();
    }

    @Override
    public float getMarkerSize()
    {
        return markerSize;
    }

    @Override
    public void setMarkerSize(float markerSize) 
    {
        this.markerSize = markerSize;                   
        model.setMarkerSize(markerSize);

        updateShapeLabel();
    }

    @Override
    public boolean getDrawMarkerOutline() 
    {
        return true;
    }

    @Override
    public Stroke getMarkerOutlineStroke() 
    {
        return stroke;
    }

    @Override
    public Paint getMarkerOutlinePaint() {
        return strokePaint;
    }
}
