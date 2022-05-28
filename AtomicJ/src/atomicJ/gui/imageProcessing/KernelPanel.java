package atomicJ.gui.imageProcessing;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JPanel;


import atomicJ.gui.NumericalField;
import atomicJ.gui.imageProcessing.KernelElementValueEvent.KernelElementChange;
import atomicJ.utilities.ArrayIndex;

public class KernelPanel extends JPanel implements KernelChangeListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final List<List<NumericalField>> numericalFields = new ArrayList<>();

    private Kernel2DModel model;


    public KernelPanel()
    {            
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    public void setModel(Kernel2DModel model)
    {
        this.model = model;
    }

    private void clearNumericalFields()
    {
        for(List<NumericalField> fields : numericalFields)
        {
            for(NumericalField field : fields)
            {
                field.removePropertyChangeListener(this);
                remove(field);
            }
        }

        numericalFields.clear();
    }

    private NumericalField getNumericalField(int row, int column)
    {
        List<NumericalField> fields = numericalFields.get(row);
        return fields.get(column);              
    }

    public void setKernelElements(double[][] kernel)
    {
        clearNumericalFields();

        int rowCount = kernel.length;
        int columnCount = kernel[0].length;

        setLayout(new GridLayout(rowCount, columnCount, 5, 5));

        for(double[] row : kernel)
        {
            List<NumericalField> fields = new ArrayList<>();
            numericalFields.add(fields);

            for(double el : row)
            {
                NumericalField field = createNumericalField();
                field.setValue(el);

                field.addPropertyChangeListener(NumericalField.VALUE_EDITED, this);

                fields.add(field);
                add(field);
            }
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
            List<NumericalField> row = numericalFields.get(i);

            int m = row.size();

            for(int j = 0; j<m; j++)
            {
                NumericalField f = row.get(j);

                if(f.equals(o))
                {
                    return new ArrayIndex(i, j);
                }
            }
        }

        return null;
    }

    @Override
    public void kernelElementValueChanged(KernelElementValueEvent evt)
    {
        for(KernelElementChange change : evt.getElementChanges())
        {
            int row = change.getRow();
            int column = change.getColumn();

            NumericalField field = getNumericalField(row, column);
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

        setKernelElements(kernelNew);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();

        ArrayIndex index = getFieldIndex(source);

        if(model != null && index != null)
        {
            Double valueNew = ((Number)evt.getNewValue()).doubleValue();
            model.setKernelElement(index.getRow(), index.getColumn(), valueNew);
        }          
    }      
}