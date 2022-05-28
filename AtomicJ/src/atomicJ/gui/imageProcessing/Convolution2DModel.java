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
import atomicJ.imageProcessing.Kernel2D;
import atomicJ.imageProcessing.KernelConvolution;
import atomicJ.resources.Channel2DResourceView;
import atomicJ.utilities.ArrayUtilities;


public class Convolution2DModel extends Kernel2DModel
{
    private static final double TOLERANCE = 1e-10;

    public static final String KERNEL_X_RADIUS = "KernelXRadius";
    public static final String KERNEL_Y_RADIUS = "KernelYRadius";

    public static final String DIVISOR = "Divisor";
    public static final String OFFSET = "Offset";
    public static final String KERNEL = "Kernel";

    public static final String NORMALIZE = "Normalize";

    public static final String EXPORT_ENABLED = "ExportEnabled";

    private int kernelXRadius = 1;
    private int kernelYRadius = 1;

    private double divisor = 1;
    private double offset = 0;

    private double[][] kernel = new double[][] {{0,0,0},{0,1,0},{0,0,0}};

    private boolean normalize = false;

    private final Set<KernelChangeListener> kernelChangeListeners = new LinkedHashSet<>();

    private boolean kernelFilled = true;
    private boolean saveEnabled = true;

    public Convolution2DModel(Channel2DResourceView manager)
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

            this.kernel = (diff > 0) ? ArrayUtilities.addColumns(kernel, 0, diff, diff) : ArrayUtilities.removeColumns(kernel, -diff, -diff);            

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

            this.kernel = (diff > 0) ? ArrayUtilities.addRows(kernel, 0, diff, diff) : ArrayUtilities.removeRows(kernel, -diff, -diff);       

            KernelStructuralEvent rowChangeEvent = new KernelStructuralEvent(kernelOld, ArrayUtilities.deepCopy(kernel));
            fireKernelRowChangeEvent(rowChangeEvent);

            firePropertyChange(KERNEL_Y_RADIUS, kernelYRadiusOld, kernelYRadiusNew);
        }
    }

    public double getDivisor()
    {
        return divisor;
    }

    public void setDivisor(double divisorNew)
    {
        if(Math.abs(divisorNew) < TOLERANCE)
        {
            throw new IllegalArgumentException("Parameter 'divisorNew' cannot be equal 0");
        }

        if(Double.compare(this.divisor, divisorNew) != 0)
        {
            double divisorOld = this.divisor;
            this.divisor = divisorNew;

            firePropertyChange(DIVISOR, divisorOld, divisorNew);

            updatePreview();
        }
    }

    public double getNormalizationDivisor()
    {
        double total = ArrayUtilities.total(kernel);
        double divisor = Math.abs(total) < TOLERANCE ? 1 : total;

        return divisor;
    }

    public double getOffset()
    {
        return offset;
    }

    public void setOffset(double offsetNew)
    {
        if(Double.compare(this.offset, offsetNew) != 0)
        {
            double offsetOld = this.offset;
            this.offset = offsetNew;

            firePropertyChange(OFFSET, offsetOld, offsetNew);

            updatePreview();
        }
    }

    public boolean isNormalize()
    {
        return normalize;
    }

    public void setNormalize(boolean normalizeNew)
    {
        if(this.normalize != normalizeNew)
        {
            boolean normalizeOld = this.normalize;
            this.normalize = normalizeNew;

            firePropertyChange(NORMALIZE, normalizeOld, normalizeNew);

            updatePreview();
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

    public double[][] getTransformedKernel()
    {
        double divisor = normalize ? getNormalizationDivisor() : this.divisor;
        double offset =  normalize ? 0 : this.offset;

        int n = kernel.length;

        double[][] transformedKernel = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] row = kernel[i];
            int m = row.length;
            double[] rowTransformed = new double[m];

            for(int j = 0; j< m; j++)
            {
                rowTransformed[j] = row[j]/divisor + offset;
            }

            transformedKernel[i] = rowTransformed;
        }

        return transformedKernel;
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
        return super.calculateApplyEnabled() && (kernelFilled && !Double.isNaN(divisor));
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

        Kernel2D kernel = new Kernel2D(getTransformedKernel());
        Channel2DDataInROITransformation tr = new KernelConvolution(kernel);

        return tr;
    }
}
