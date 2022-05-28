package atomicJ.gui.imageProcessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.ArrayTextExporter;
import atomicJ.gui.Array2DTextReader;
import atomicJ.gui.imageProcessing.KernelElementValueEvent.KernelElementChange;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.MedianWeightedFilter2D;
import atomicJ.resources.Channel2DResourceView;
import atomicJ.utilities.ArrayUtilities;


public class MedianWeighted2DModel extends Kernel2DModel
{
    public static final String KERNEL_X_RADIUS = "KernelXRadius";
    public static final String KERNEL_Y_RADIUS = "KernelYRadius";

    public static final String KERNEL = "Kernel";

    public static final String EXPORT_ENABLED = "ExportEnabled";

    private int kernelXRadius = 1;
    private int kernelYRadius = 1;

    private final double divisor = 1;

    private double[][] kernel = new double[][] {{1,1,1},{1,1,1},{1,1,1}};

    private final Set<KernelChangeListener> kernelChangeListeners = new LinkedHashSet<>();

    private boolean kernelFilled = true;
    private boolean saveEnabled = true;

    public MedianWeighted2DModel(Channel2DResourceView manager)
    {
        super(manager, PermissiveChannel2DFilter.getInstance(), true);
    }

    @Override
    public int getKernelXRadius()
    {
        return kernelXRadius;
    }

    @Override
    public void setKernelXRadius(int kernelXRadiusNew)
    {       
        if(kernelXRadiusNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'kernelXRadiusNew' must be grater or equal 0");
        }

        if(this.kernelXRadius != kernelXRadiusNew)
        {
            int kernelWidthOld = this.kernelXRadius;
            this.kernelXRadius = kernelXRadiusNew;

            int diff = kernelXRadiusNew - kernelWidthOld;
            double[][] kernelOld = ArrayUtilities.deepCopy(kernel);

            this.kernel = (diff > 0) ?ArrayUtilities.addColumns(kernel, 0, diff, diff) : ArrayUtilities.removeColumns(kernel, -diff, -diff);

            KernelStructuralEvent rowChangeEvent = new KernelStructuralEvent(kernelOld, ArrayUtilities.deepCopy(kernel));
            fireKernelRowChangeEvent(rowChangeEvent);

            firePropertyChange(KERNEL_X_RADIUS, kernelWidthOld, kernelXRadiusNew);
        }
    }

    @Override
    public int getKernelYRadius()
    {
        return kernelYRadius;
    }

    @Override
    public void setKernelYRadius(int kernelYRadiusNew)
    {
        if(kernelYRadiusNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'kernelYRadiusNew' must be grater or equal 0");
        }

        if(this.kernelYRadius != kernelYRadiusNew)
        {
            int kernelYRadiusOld = this.kernelYRadius;
            this.kernelYRadius = kernelYRadiusNew;

            int diff = kernelYRadiusNew - kernelYRadiusOld;

            double[][] kernelOld = ArrayUtilities.deepCopy(kernel);

            if(diff > 0)
            {
                this.kernel = ArrayUtilities.addRows(kernel, 0, diff, diff);
            }
            else
            {
                this.kernel = ArrayUtilities.removeRows(kernel, -diff, -diff);
            }

            KernelStructuralEvent rowChangeEvent = new KernelStructuralEvent(kernelOld, ArrayUtilities.deepCopy(kernel));
            fireKernelRowChangeEvent(rowChangeEvent);

            firePropertyChange(KERNEL_Y_RADIUS, kernelYRadiusOld, kernelYRadiusNew);
        }
    }




    @Override
    public double[][] getKernel()
    {
        return ArrayUtilities.deepCopy(kernel);
    }

    @Override
    public void setKernelElement(int row, int column, double valueNew)
    {  
        double valueOld = kernel[row][column];

        //we use compare(), because comparing two Double.NaN always returns false
        if(Double.compare(valueOld, valueNew) != 0)
        {
            kernel[row][column] = valueNew;
            fireKernelChangeEvent(row, column, valueOld, valueNew);

            checkIfInputSpecified();

            updatePreview();
        }
    }

    @Override
    public void setKernelElements(double[][] kernelElements)
    {
        int rows = Math.min(kernelElements.length,  2*kernelXRadius + 1);

        List<KernelElementChange> kernelChanges = new ArrayList<>();

        for(int i = 0; i<rows; i++)
        {
            int columns = Math.min(kernelElements[i].length, 2*kernelXRadius + 1);

            for(int j = 0; j<columns; j++)
            {
                double valueOld = kernel[i][j];
                double valueNew = kernelElements[i][j];

                //we use compare(), because comparing two Double.NaN always returns false
                if(Double.compare(valueNew, valueOld) != 0)
                {
                    kernel[i][j] = valueNew;

                    KernelElementChange elementChange = new KernelElementChange(i, j, valueOld, valueNew);
                    kernelChanges.add(elementChange);
                }
            }
        }

        KernelElementValueEvent event = new KernelElementValueEvent(this, kernelChanges);
        fireKernelElementChangeEvent(event);

        updatePreview();
    }

    public void fireKernelChangeEvent(int row, int column, double valueOld, double valueNew)
    {
        KernelElementChange elementChange = new KernelElementChange(row, column, valueOld, valueNew);
        KernelElementValueEvent event = new KernelElementValueEvent(this, elementChange);

        fireKernelElementChangeEvent(event);
    }

    public void fireKernelElementChangeEvent(KernelElementValueEvent event)
    {        
        for(KernelChangeListener listener : kernelChangeListeners)
        {
            listener.kernelElementValueChanged(event);
        }
    }

    public void fireKernelRowChangeEvent(KernelStructuralEvent event)
    {
        for(KernelChangeListener listener : kernelChangeListeners)
        {
            listener.kernelStructureChanged(event);
        }
    }

    @Override
    public void addKernelChangeListener(KernelChangeListener listener)
    {
        kernelChangeListeners.add(listener);
    }

    @Override
    public void removeKernelChangeListener(KernelChangeListener listener)
    {
        kernelChangeListeners.remove(listener);
    }

    @Override
    public boolean isExportEnabled()
    {
        return kernelFilled;
    }

    @Override
    public boolean calculateApplyEnabled()
    {
        return super.calculateApplyEnabled() &&(kernelFilled && !Double.isNaN(divisor));
    }

    private void checkIfInputSpecified()
    {
        this.kernelFilled  = checkIfKernelFilled();

        boolean kernelFullySpecified = kernelFilled && !Double.isNaN(divisor);

        if(this.saveEnabled != kernelFullySpecified)
        {
            boolean saveEnabledOld = this.saveEnabled;
            this.saveEnabled = kernelFullySpecified;

            firePropertyChange(EXPORT_ENABLED, saveEnabledOld, this.saveEnabled);
        }

        checkIfApplyEnabled();
    }

    private boolean checkIfKernelFilled()
    {
        boolean kernelFilled = true;

        for(double[] row : kernel)
        {
            for(double el : row)
            {
                kernelFilled = kernelFilled && !Double.isNaN(el);
            }
        }

        return kernelFilled;
    }

    @Override
    public void importKernel(File file, String[] selectedExtensions) throws IOException
    {
        Array2DTextReader reader = new Array2DTextReader();
        double[][] kernel = reader.read(file, selectedExtensions);
        int rowCount = kernel.length;
        int columnCount = kernel[0].length;

        setKernelXRadius(columnCount/2);
        setKernelYRadius(rowCount/2);
        setKernelElements(kernel);
    }

    @Override
    public void exportKernel(File file, String[] selectedExtensions) throws IOException 
    {
        ArrayTextExporter exporter = new ArrayTextExporter(); 
        exporter.export(kernel, file, selectedExtensions);                         
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        Channel2DDataInROITransformation tr = new MedianWeightedFilter2D(kernel);
        return tr;
    }
}
