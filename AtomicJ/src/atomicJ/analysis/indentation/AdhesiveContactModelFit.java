package atomicJ.analysis.indentation;

public interface AdhesiveContactModelFit<E extends ContactModel> extends ContactModelFit<E>
{
    //returns adhesion work in mJ/m^2
    public double getAdhesionWork();
}