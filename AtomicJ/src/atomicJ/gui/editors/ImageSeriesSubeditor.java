
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

package atomicJ.gui.editors;

import static atomicJ.gui.PreferenceKeys.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


import org.jfree.ui.PaintSample;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.ColorGradient;
import atomicJ.gui.ColorSupplier;
import atomicJ.gui.CustomizableImageRenderer;
import atomicJ.gui.GradientColorsBuiltIn;
import atomicJ.gui.GradientMaskSelector;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.GradientPaintReceiver;
import atomicJ.gui.GradientRangeSelector;
import atomicJ.gui.RangeGradientChooser;
import atomicJ.gui.StyleTag;
import atomicJ.gui.SubPanel;
import atomicJ.gui.rois.ROI;
import atomicJ.utilities.SerializationUtilities;


public class ImageSeriesSubeditor implements Subeditor, GradientPaintReceiver
{
    private ColorGradient lutTable;
    private Color gradientUnderflowColor;
    private Color gradientOverflowColor;
    private Color maskColor;	

    private boolean show;

    private double lowerGradientBound;
    private double upperGradientBound;
    private GradientMaskSelector gradientMaskSelector;
    private GradientRangeSelector gradientRangeSelector;
    private boolean useOutsideRangeColors;

    private final double lowerFullGradientBound;
    private final double upperFullGradientBound;

    private final double lowerAutomaticGradientBound;
    private final double upperAutomaticGradientBound;    

    private final boolean initShow;

    private final ColorGradient initLUTTable;
    private final Color initGradientUnderflowColor;
    private final Color initGradientOverflowColor;

    private final Color initMaskColor;

    private final double initLowerGradientBound;
    private final double initUpperGradientBound;
    private final GradientMaskSelector initGradientMaskSelector;
    private final GradientRangeSelector initGradientRangeSelector;
    private final boolean initUseEndcolors;

    private final PrefixedUnit dataUnit;
    private final PrefixedUnit displayedUnit;

    private final JCheckBox showCheckBox = new JCheckBox();

    private final PaintSample gradientSample;	
    private final SelectColorGradientAction colorGradientAction = new SelectColorGradientAction();
    private RangeGradientChooser gradientChooser;

    private final Preferences pref;       
    private final String seriesName;

    private final CustomizableImageRenderer renderer;
    private final List<CustomizableImageRenderer> boundededRenderers;

    private final SubPanel editorPanel = new SubPanel();

    public ImageSeriesSubeditor(CustomizableImageRenderer renderer, List<CustomizableImageRenderer> boundedRenderers)
    {
        this.renderer = renderer;
        this.boundededRenderers = boundedRenderers;

        this.initShow = renderer.getBaseSeriesVisible();

        this.initLUTTable = renderer.getColorGradient();
        this.initGradientUnderflowColor = renderer.getGradientUnderflowColor();
        this.initGradientOverflowColor = renderer.getGradientOverflowColor();
        this.initUseEndcolors = renderer.getUseOutsideRangeColors();

        this.initMaskColor = renderer.getMaskColor();

        this.initLowerGradientBound = renderer.getLowerBound();
        this.initUpperGradientBound = renderer.getUpperBound();
        this.initGradientMaskSelector = renderer.getGradientMaskSelector();
        this.initGradientRangeSelector = renderer.getGradientRangeSelector();

        this.dataUnit = renderer.getDataUnit();
        this.displayedUnit = renderer.getDisplayedUnit();

        this.lowerFullGradientBound = renderer.getLowerFullBound();
        this.upperFullGradientBound = renderer.getUpperFullBound();

        this.lowerAutomaticGradientBound = renderer.getLowerAutomaticBound();
        this.upperAutomaticGradientBound = renderer.getUpperAutomaticBound();

        setSubeditorParametersToInitial();

        this.seriesName = renderer.getName();	
        this.pref = renderer.getPreferences();    

        showCheckBox.setSelected(initShow);

        Paint gradientPaint = new GradientPaint(lutTable);
        gradientSample = new PaintSample(gradientPaint);

        editorPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        editorPanel.addComponent(showCheckBox, 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        editorPanel.addComponent(new JLabel("Gradient color"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        editorPanel.addComponent(gradientSample, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        editorPanel.addComponent(new JButton(colorGradientAction), 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName)));

        initItemListener();
    }

    private void initItemListener()
    {
        showCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                show = selected;
                renderer.setBaseSeriesVisible(show);
            }
        });
    }

    @Override
    public void setNameBorder(boolean b)
    {
        if(b)
        {
            editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName)));
        }
        else
        {

            editorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),BorderFactory.createEmptyBorder(0, 5, 0, 5))));
        }
    }

    @Override
    public String getSubeditorName()
    {
        return seriesName;
    }


    private void setSubeditorParametersToInitial()
    {
        this.show = initShow;
        this.lutTable = initLUTTable;

        this.gradientUnderflowColor = initGradientUnderflowColor;
        this.gradientOverflowColor = initGradientOverflowColor;
        this.useOutsideRangeColors = initUseEndcolors;

        this.maskColor = initMaskColor;

        this.lowerGradientBound = initLowerGradientBound;
        this.upperGradientBound = initUpperGradientBound;
        this.gradientMaskSelector = initGradientMaskSelector;
        this.gradientRangeSelector = initGradientRangeSelector;
    }

    @Override
    public void resetToDefaults() 
    {
        StyleTag style = renderer.getStyleKey();
        ColorSupplier supplier = renderer.getSupplier();
        ColorGradient defaultLUTTable = GradientColorsBuiltIn.getGradients().get("Golden");
        Paint defaultUnderflowColor = supplier.getGradientUnderflow(style);
        Paint defaultOverflowColor = supplier.getGradientOverflow(style);

        this.lutTable = (ColorGradient)SerializationUtilities.getSerializableObject(pref, RANGE_COLOR_GRADIENT, defaultLUTTable);
        this.gradientUnderflowColor = (Color)SerializationUtilities.getSerializableObject(pref, PAINT_GRADIENT_UNDERFLOW, defaultUnderflowColor);
        this.gradientOverflowColor = (Color)SerializationUtilities.getSerializableObject(pref, PAINT_GRADIENT_OVERFLOW, defaultOverflowColor);				

        this.maskColor = (Color)SerializationUtilities.getSerializableObject(pref, MASK_COLOR, Color.black);				

        resetRenderer(renderer);		
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {
        try 
        {
            SerializationUtilities.putSerializableObject(pref, RANGE_COLOR_GRADIENT, lutTable);
            SerializationUtilities.putSerializableObject(pref, PAINT_GRADIENT_UNDERFLOW, gradientUnderflowColor);
            SerializationUtilities.putSerializableObject(pref, PAINT_GRADIENT_OVERFLOW, gradientOverflowColor);
            SerializationUtilities.putSerializableObject(pref, MASK_COLOR, maskColor);
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }

        try 
        {
            pref.flush();
        } 
        catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public void applyChangesToAll() 
    {
        for(CustomizableImageRenderer r : boundededRenderers)
        {
            resetRenderer(r);
        }
    }

    @Override
    public void undoChanges() 
    {
        setSubeditorParametersToInitial();
        resetRenderer(renderer);
        resetEditor();
    }

    @Override
    public Component getEditionComponent() 
    {
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundededRenderers.size() > 1;
    }

    private void resetRenderer(CustomizableImageRenderer renderer)
    {
        renderer.setBaseSeriesVisible(show);
        renderer.setUseOutsideRangeColors(useOutsideRangeColors);
        renderer.setColorGradient(lutTable);
        renderer.setGradientUnderflowColor(gradientUnderflowColor);
        renderer.setGradientOverflowColor(gradientOverflowColor);
        renderer.setMaskColor(maskColor);
        renderer.setGradientRangeSelector(gradientRangeSelector);
    }

    private void resetEditor()
    {
        updateGradientPaintSample();
        showCheckBox.setSelected(show);
    }

    @Override
    public boolean isFullRange() 
    {
        return GradientRangeSelector.FULL.equals(gradientRangeSelector);
    }

    @Override
    public boolean isAutomaticRange() 
    {
        return GradientRangeSelector.AUTOMATIC.equals(gradientRangeSelector);
    }

    @Override
    public boolean isColorROIFullRange()
    {
        return GradientRangeSelector.ROI_FULL.equals(gradientRangeSelector);
    }

    @Override
    public double getLowerROIBound()
    {
        return renderer.getLowerROIBound();
    }

    @Override
    public void setLowerROIBound(double lowerLensBoundNew)
    {
        renderer.setLowerROIBound(lowerLensBoundNew);
    }

    @Override
    public double getUpperROIBound()
    {
        return renderer.getUpperROIBound();
    }

    @Override
    public void setUpperROIBound(double upperLensBoundNew)
    {
        renderer.setUpperROIBound(upperLensBoundNew);
    }

    @Override
    public void setLensToFull()
    {
        renderer.setLensToFull();
    }

    @Override
    public double getLowerFullBound() 
    {
        return lowerFullGradientBound;
    }

    @Override
    public double getUpperFullBound() 
    {
        return upperFullGradientBound;
    }

    @Override
    public double getLowerBound() 
    {
        return lowerGradientBound;
    }

    @Override
    public void setLowerBound(double lowerBound) 
    {
        this.lowerGradientBound = lowerBound;
        renderer.setLowerBound(lowerBound);	
    }

    @Override
    public double getUpperBound() 
    {
        return upperGradientBound;
    }

    @Override
    public void setUpperBound(double upperBound) 
    {
        this.upperGradientBound = upperBound;
        renderer.setUpperBound(upperBound);		 
    }

    @Override
    public void setGradientBounds(double lowerGradientBound, double upperGradientBound)
    {
        this.lowerGradientBound = lowerGradientBound;
        this.upperGradientBound = upperGradientBound;
        renderer.setGradientBounds(lowerGradientBound, upperGradientBound);
    }

    @Override
    public PrefixedUnit getDataUnit()
    {
        return dataUnit;
    }

    @Override
    public PrefixedUnit getDisplayedUnit()
    {
        return displayedUnit;
    }

    @Override
    public ColorGradient getColorGradient() 
    {
        return lutTable;
    }

    @Override
    public void setColorGradient(ColorGradient lc) 
    {
        this.lutTable = lc;
        renderer.setColorGradient(lutTable);	
        updateGradientPaintSample();	
    }

    private void updateGradientPaintSample()
    {
        Paint gradientPaint = new GradientPaint(lutTable);
        gradientSample.setPaint(gradientPaint);
    }

    @Override
    public Color getGradientUnderflowColor() 
    {
        return gradientUnderflowColor;
    }

    @Override
    public void setGradientUnderflowColor(Color ufc) 
    {
        this.gradientUnderflowColor = ufc;
        renderer.setGradientUnderflowColor(ufc);	
    }

    @Override
    public Color getGradientOverflowColor() 
    {
        return gradientOverflowColor;
    }

    @Override
    public void setGradientOverflowColor(Color ofc) 
    {
        this.gradientOverflowColor = ofc;
        renderer.setGradientOverflowColor(ofc);			
    }

    @Override
    public boolean getUseOutsideRangeColors() 
    {
        return useOutsideRangeColors;
    }

    @Override
    public void setUseOutsideRangeColors(boolean useEndcolors) 
    {
        this.useOutsideRangeColors = useEndcolors;
        renderer.setUseOutsideRangeColors(useEndcolors);
    }

    @Override
    public GradientRangeSelector getGradientRangeSelector() 
    {
        return gradientRangeSelector;
    }

    @Override
    public void setGradientRangeSelector(GradientRangeSelector selector) 
    {
        this.gradientRangeSelector = selector;
        renderer.setGradientRangeSelector(selector);
    }

    @Override
    public boolean areROISamplesNeeded()
    {
        return renderer.areROISamplesNeeded();
    }

    @Override
    public double getLowerAutomaticBound() 
    {
        return lowerAutomaticGradientBound;
    }

    @Override
    public double getUpperAutomaticBound() 
    {
        return upperAutomaticGradientBound;
    }

    @Override
    public QuantitativeSample getPaintedSample() 
    {
        return renderer.getPaintedSample();
    }

    @Override
    public void setMaskedRegion(ROI roi)
    {
        renderer.setMaskedRegion(roi);
    }

    @Override
    public Color getMaskColor() 
    {
        return maskColor;
    }

    @Override
    public void setMaskColor(Color maskColor) 
    {
        this.maskColor = maskColor;
        renderer.setMaskColor(maskColor);
    } 


    @Override
    public GradientMaskSelector getGradientMaskSelector() 
    {
        return gradientMaskSelector;
    }

    @Override
    public void setGradientMaskSelector(GradientMaskSelector selector) 
    {
        this.gradientMaskSelector = selector;
        renderer.setGradientMaskSelector(selector);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    private class SelectColorGradientAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SelectColorGradientAction() 
        {
            putValue(NAME, "Select");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            if(gradientChooser == null)
            {
                gradientChooser = new RangeGradientChooser(SwingUtilities.getWindowAncestor(editorPanel), ImageSeriesSubeditor.this);
                gradientChooser.setVisible(true);
            }
            else
            {
                gradientChooser.showDialog(ImageSeriesSubeditor.this);
            }
        }
    }
}
