package atomicJ.gui;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.google.common.base.Objects;

import atomicJ.utilities.Validation;

import java.beans.*;
import javax.accessibility.*;

public class JSpinnerNumeric extends JSpinner implements Accessible
{    
    private static final long serialVersionUID = 1L;

    private static final Action DISABLED_ACTION = new DisabledAction();

    public JSpinnerNumeric(SpinnerDoubleModel model) {
        super(model);
    }

    @Override
    public SpinnerDoubleModel getModel()
    {
        return (SpinnerDoubleModel)super.getModel();
    }


    @Override
    public void setModel(SpinnerModel model)
    {
        if(!(model instanceof SpinnerDoubleModel))
        {
            throw new IllegalArgumentException("The 'model ' parameter must be an instance of the SpinnerDoubleModel class");
        }
        super.setModel(model);
    }

    @Override
    protected JComponent createEditor(SpinnerModel model) 
    {
        if(!(model instanceof SpinnerDoubleModel))
        {
            throw new IllegalArgumentException("The 'model ' parameter must be an instance of the SpinnerDoubleModel class");
        }
        return new NumericSpinnerEditor(this);
    }

    @Override
    public void setEditor(JComponent editor) 
    {
        Validation.requireNonNullParameterName(editor, "editor");

        if (!Objects.equal(this.getEditor(), editor))
        {
            JComponent oldEditor = this.getEditor();
            if (oldEditor instanceof NumericSpinnerEditor) {
                ((NumericSpinnerEditor)oldEditor).dismiss(this);
            }

            super.setEditor(editor);
        }
    }

    public double getDoubleValue()
    {
        double val = getModel().getDoubleValue();
        return val;
    }

    public static class NumericSpinnerEditor extends JPanel implements LayoutManager
    {      
        private static final long serialVersionUID = 1L;

        private final NumericalField field;
        private final ChangeListener spinnerListener;

        public NumericSpinnerEditor(JSpinnerNumeric spinner)
        {
            super(null);

            SpinnerDoubleModel model = spinner.getModel();

            String illegalValueWarning = model.getIllegalValueWarning();
            double val = model.getDoubleValue();
            double min = model.getMinimum();
            double max = model.getMaximum();

            this.field = new NumericalField(illegalValueWarning, min, max);
            field.setValue(val);

            field.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() 
            {            
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {
                    Object valNew = evt.getNewValue();
                    spinner.setValue(valNew);
                }
            }); 

            field.setInheritsPopupMenu(true);

            String toolTipText = spinner.getToolTipText();
            if (toolTipText != null) {
                field.setToolTipText(toolTipText);
            }

            add(field);

            setLayout(this);
            this.spinnerListener = new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e)
                {
                    double valNew = spinner.getModel().getDoubleValue();
                    double valOld = field.getValue().doubleValue();

                    if(Double.compare(valNew, valOld) != 0)
                    {
                        field.setValue(valNew);             
                    }                                
                }
            };
            spinner.addChangeListener(spinnerListener);

            // We want the spinner's increment/decrement actions to be
            // active vs those of the JFormattedTextField. As such we
            // put disabled actions in the JFormattedTextField's actionmap.
            // A binding to a disabled action is treated as a nonexistant
            // binding.
            ActionMap ftfMap = field.getActionMap();

            if (ftfMap != null) {
                ftfMap.put("increment", DISABLED_ACTION);
                ftfMap.put("decrement", DISABLED_ACTION);
            }
        }


        public void dismiss(JSpinnerNumeric spinner) {
            spinner.removeChangeListener(spinnerListener);
        }

        /**
         * This <code>LayoutManager</code> method does nothing.  We're
         * only managing a single child and there's no support
         * for layout constraints.
         *
         * @param name ignored
         * @param child ignored
         */
        @Override
        public void addLayoutComponent(String name, Component child) {
        }


        /**
         * This <code>LayoutManager</code> method does nothing.  There
         * isn't any per-child state.
         *
         * @param child ignored
         */
        @Override
        public void removeLayoutComponent(Component child) {
        }


        /**
         * Returns the size of the parents insets.
         */
        private Dimension insetSize(Container parent) {
            Insets insets = parent.getInsets();
            int w = insets.left + insets.right;
            int h = insets.top + insets.bottom;
            return new Dimension(w, h);
        }


        /**
         * Returns the preferred size of first (and only) child plus the
         * size of the parents insets.
         *
         * @param parent the Container that's managing the layout
         * @return the preferred dimensions to lay out the subcomponents
         *          of the specified container.
         */
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension preferredSize = insetSize(parent);
            if (parent.getComponentCount() > 0) {
                Dimension childSize = getComponent(0).getPreferredSize();
                preferredSize.width += childSize.width;
                preferredSize.height += childSize.height;
            }
            return preferredSize;
        }


        /**
         * Returns the minimum size of first (and only) child plus the
         * size of the parents insets.
         *
         * @param parent the Container that's managing the layout
         * @return  the minimum dimensions needed to lay out the subcomponents
         *          of the specified container.
         */
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            Dimension minimumSize = insetSize(parent);
            if (parent.getComponentCount() > 0) {
                Dimension childSize = getComponent(0).getMinimumSize();
                minimumSize.width += childSize.width;
                minimumSize.height += childSize.height;
            }
            return minimumSize;
        }


        /**
         * Resize the one (and only) child to completely fill the area
         * within the parents insets.
         */
        @Override
        public void layoutContainer(Container parent) {
            if (parent.getComponentCount() > 0) {
                Insets insets = parent.getInsets();
                int w = parent.getWidth() - (insets.left + insets.right);
                int h = parent.getHeight() - (insets.top + insets.bottom);
                getComponent(0).setBounds(insets.left, insets.top, w, h);
            }
        }



        /**
         * Returns the baseline.
         *
         * @throws IllegalArgumentException {@inheritDoc}
         * @see javax.swing.JComponent#getBaseline(int,int)
         * @see javax.swing.JComponent#getBaselineResizeBehavior()
         * @since 1.6
         */
        @Override
        public int getBaseline(int width, int height) {
            // check size.
            super.getBaseline(width, height);
            Insets insets = getInsets();
            width = width - insets.left - insets.right;
            height = height - insets.top - insets.bottom;
            int baseline = getComponent(0).getBaseline(width, height);
            if (baseline >= 0) {
                return baseline + insets.top;
            }
            return -1;
        }

        @Override
        public BaselineResizeBehavior getBaselineResizeBehavior() {
            return getComponent(0).getBaselineResizeBehavior();
        }
    }

    /**
     * An Action implementation that is always disabled.
     */
    private static class DisabledAction implements Action {
        @Override
        public Object getValue(String key) {
            return null;
        }
        @Override
        public void putValue(String key, Object value) {
        }
        @Override
        public void setEnabled(boolean b) {
        }
        @Override
        public boolean isEnabled() {
            return false;
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
        }
    }
}