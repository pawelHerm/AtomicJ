package atomicJ.curveProcessing;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JPanel;


import atomicJ.gui.NumericalField;
import atomicJ.gui.imageProcessing.KernelChangeListener;
import atomicJ.gui.imageProcessing.KernelElementValueEvent;
import atomicJ.gui.imageProcessing.KernelElementValueEvent.KernelElementChange;
import atomicJ.gui.imageProcessing.KernelStructuralEvent;
import atomicJ.utilities.ArrayIndex;

public class Kernel1DPanel extends JPanel implements KernelChangeListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final List<NumericalField> numericalFields = new ArrayList<>();

    private Kernel1DModel<?> model;


    public Kernel1DPanel()
    {            
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    public void setModel(Kernel1DModel<?> model)
    {
        this.model = model;
    }

    private void clearNumericalFields()
    {
        for(NumericalField field : numericalFields)
        {
            field.removePropertyChangeListener(this);
            remove(field);
        }

        numericalFields.clear();
    }

    private NumericalField getNumericalField(int column)
    {
        return numericalFields.get(column);              
    }

    public void setKernelElements(double[] kernel)
    {
        clearNumericalFields();

        int columnCount = kernel.length;

        setLayout(new GridLayout(1, columnCount, 5, 5));

        for(double el : kernel)
        {
            NumericalField field = createNumericalField();
            numericalFields.add(field);

            field.setValue(el);

            field.addPropertyChangeListener(NumericalField.VALUE_EDITED, this);

            add(field);
        }

        revalidate();
    }

    protected NumericalField createNumericalField()
    {
        NumericalField field = new NumericalField();
        return field;
    }

    private ArrayIndex getFieldIndex(Object o)
    {
        int n = numericalFields.size();

        for(int i = 0; i<n; i++)
        {
            NumericalField f = numericalFields.get(i);

            if(f.equals(o))
            {
                return new ArrayIndex(1, i);
            }
        }

        return null;
    }

    @Override
    public void kernelElementValueChanged(KernelElementValueEvent evt)
    {
        for(KernelElementChange change : evt.getElementChanges())
        {
            int column = change.getColumn();

            NumericalField field = getNumericalField(column);
            if(field != null)
            {
                Double valueOld = field.getValue().doubleValue(); 
                Double valueNew = change.getValueNew();

                if(!Objects.equals(valueNew, valueOld))
                {
                    field.setValue(valueNew);
                }
            }
        }
    }

    @Override
    public void kernelStructureChanged(KernelStructuralEvent evt) 
    {            
        double[][] kernelNew = evt.getKernelNew();

        setKernelElements(kernelNew[0]);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();

        ArrayIndex index = getFieldIndex(source);

        if(model != null && index != null)
        {
            Double valueNew = ((Number)evt.getNewValue()).doubleValue();
            model.setKernelElement(index.getColumn(), valueNew);
        }          
    }      
}