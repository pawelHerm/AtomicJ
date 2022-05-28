package atomicJ.gui.imageProcessing;

public interface KernelChangeListener 
{
    public void kernelElementValueChanged(KernelElementValueEvent evt);

    public void kernelStructureChanged(KernelStructuralEvent evt);    
}
