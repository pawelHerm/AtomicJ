package atomicJ.gui.curveProcessing;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ThinSampleModel;
import atomicJ.analysis.indentation.Cone;
import atomicJ.analysis.indentation.ContactModel;
import atomicJ.analysis.indentation.HertzianThinSampleConeChadwick;
import atomicJ.analysis.indentation.HertzianThinSampleConeLebedevWithoutAdhesion;
import atomicJ.analysis.indentation.HertzianThinSampleParaboloidChadwick;
import atomicJ.analysis.indentation.HertzianThinSampleParaboloidLebedevWithoutAdhesion;
import atomicJ.analysis.indentation.HertzianThinSampleSphereLebedevChebyshev;
import atomicJ.analysis.indentation.Sphere;

public enum ThicknessCorrectionMethod 
{
    LEBEDEV_CHEBYSHEV("Lebedev-Chebyshev")
    {
        @Override
        public ContactModel getConicalContactModel(Cone indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel) 
        {
            return new HertzianThinSampleConeLebedevWithoutAdhesion(indenter, sampleModel, precontactModel);
        }

        @Override
        public ContactModel getParaboloidalContactModel(Sphere indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel) 
        {
            return new HertzianThinSampleParaboloidLebedevWithoutAdhesion(indenter, sampleModel, precontactModel);
        }

        @Override
        public ContactModel getSphericalContactModel(Sphere indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel) 
        {
            return new HertzianThinSampleSphereLebedevChebyshev(indenter, sampleModel, precontactModel);
        }

        @Override
        public boolean isSphericalCorrectionKnown()
        {
            return true;
        }
    }, 

    CHADWICK("Chadwick") 
    {
        @Override
        public ContactModel getConicalContactModel(Cone indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel) 
        {
            return new HertzianThinSampleConeChadwick(indenter, sampleModel, precontactModel);
        }

        @Override
        public ContactModel getParaboloidalContactModel(Sphere indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel) 
        {
            return new HertzianThinSampleParaboloidChadwick(indenter, sampleModel, precontactModel);
        }

        @Override
        public ContactModel getSphericalContactModel(Sphere indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel) 
        {
            throw new UnsupportedOperationException("Thin sample model of Chadwick type is not implemented for spherical tip");
        }

        @Override
        public boolean isSphericalCorrectionKnown() 
        {
            return false;
        }
    };

    private final String prettyName;

    ThicknessCorrectionMethod(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public abstract ContactModel getConicalContactModel(Cone indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel);
    public abstract ContactModel getParaboloidalContactModel(Sphere indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel);
    public abstract ContactModel getSphericalContactModel(Sphere indenter, ThinSampleModel sampleModel, PrecontactInteractionsModel precontactModel);

    public boolean isConicalCorrectionKnown()
    {
        return true;
    }

    public boolean isParaboloidalCorrectionKnown()
    {
        return true;
    }

    public abstract boolean isSphericalCorrectionKnown();

    public static ThicknessCorrectionMethod getValue(String identifier, ThicknessCorrectionMethod fallBackValue)
    {
        ThicknessCorrectionMethod method = fallBackValue;

        if(identifier != null)
        {
            for(ThicknessCorrectionMethod br : ThicknessCorrectionMethod.values())
            {
                String estIdentifier =  br.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    method = br;
                    break;
                }
            }
        }
        return method;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public String getIdentifier()
    {
        return name();
    }
}
