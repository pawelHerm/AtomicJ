package atomicJ.gui.curveProcessing;

import java.util.Arrays;
import java.util.List;

import atomicJ.analysis.BasicPrecontactInteractionsModel;
import atomicJ.analysis.BasicSampleModel;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.SampleModel;
import atomicJ.analysis.ThinSampleModel;
import atomicJ.analysis.indentation.BluntConeWithSphericalCap;
import atomicJ.analysis.indentation.BluntPyramid;
import atomicJ.analysis.indentation.Cone;
import atomicJ.analysis.indentation.ContactModel;
import atomicJ.analysis.indentation.DMTIndentation;
import atomicJ.analysis.indentation.HertzianBluntCone;
import atomicJ.analysis.indentation.HertzianBluntPyramid;
import atomicJ.analysis.indentation.HertzianCone;
import atomicJ.analysis.indentation.HertzianHyperboloid;
import atomicJ.analysis.indentation.HertzianPowerShaped;
import atomicJ.analysis.indentation.HertzianPyramid;
import atomicJ.analysis.indentation.HertzianSneddonSphere;
import atomicJ.analysis.indentation.HertzianParaboloid;
import atomicJ.analysis.indentation.HertzianTruncatedCone;
import atomicJ.analysis.indentation.HertzianTruncatedPyramid;
import atomicJ.analysis.indentation.Hyperboloid;
import atomicJ.analysis.indentation.HyperelasticFungSphere;
import atomicJ.analysis.indentation.JKRIndentation;
import atomicJ.analysis.indentation.MaugisIndentation;
import atomicJ.analysis.indentation.OgdenIndentation;
import atomicJ.analysis.indentation.PowerShapedTip;
import atomicJ.analysis.indentation.Pyramid;
import atomicJ.analysis.indentation.Sphere;
import atomicJ.analysis.indentation.SunAkhremitchevWalkerIndentation;
import atomicJ.analysis.indentation.TruncatedCone;
import atomicJ.analysis.indentation.TruncatedPyramid;

import static atomicJ.gui.curveProcessing.TipShapeParameter.*;


public enum BasicIndentationModel 
{
    CONE("Cone", new TipShapeParameter[] {HALF_ANGLE}, false)
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);

            Cone indenter = processingModel.getCone();

            double poissonRatio = processingModel.getPoissonRatio();

            ContactModel indentation = null;

            if(processingModel.isCorrectSubstrateEffect())
            {
                ThinSampleModel sampleModel = processingModel.buildThinSampleModel();     
                ThicknessCorrectionMethod thicknessCorrectionMethod = processingModel.getThicknessCorrectionMethod();
                indentation = thicknessCorrectionMethod.getConicalContactModel(indenter, sampleModel, precontactModel);               
            }
            else
            {
                SampleModel sampleModel = new BasicSampleModel(poissonRatio);             
                indentation = new HertzianCone(indenter, sampleModel, precontactModel);
            }

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) 
        {
            return model.isConeAvailable();
        }

        @Override
        public boolean isSubstrateEffectCorrectionKnown(ThicknessCorrectionMethod correctionMethod, boolean adherentSample)
        {
            return correctionMethod.isConicalCorrectionKnown();
        }
    },

    PYRAMID("Pyramid", new TipShapeParameter[] {HALF_ANGLE}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          
            Pyramid pyramid = processingModel.getPyramid();
            ContactModel indentation = new HertzianPyramid(pyramid, sampleModel, precontactModel);

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isPyramidAvailable();
        }
    },

    POWER_SHAPED("Power shaped", new TipShapeParameter[] {EXPONENT, FACTOR}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          
            PowerShapedTip tip = processingModel.getPowerShapedTip();
            ContactModel indentation = new HertzianPowerShaped(tip, sampleModel, precontactModel);

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) 
        {
            return model.isPowerShapedTipAvailable();
        }
    },

    PARABOLOID("Paraboloid (Hertz)", new TipShapeParameter[] {RADIUS}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);

            double poissonRatio = processingModel.getPoissonRatio();

            Sphere indenter = processingModel.getSphere();

            ContactModel indentation = null;

            if(processingModel.isCorrectSubstrateEffect())
            {
                ThinSampleModel sampleModel = processingModel.buildThinSampleModel();

                ThicknessCorrectionMethod thicknessCorrectionMethod = processingModel.getThicknessCorrectionMethod();
                indentation = thicknessCorrectionMethod.getParaboloidalContactModel(indenter, sampleModel, precontactModel);
            }
            else
            {
                SampleModel sampleModel =  new BasicSampleModel(poissonRatio);             
                indentation = new HertzianParaboloid(indenter, sampleModel, precontactModel);
            }

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) 
        {
            return model.isSphereAvailable();
        }

        @Override
        public boolean isSubstrateEffectCorrectionKnown(ThicknessCorrectionMethod correctionMethod, boolean adherentSample)
        {
            return correctionMethod.isParaboloidalCorrectionKnown();
        }
    },

    SPHERE_SNEDDON("Sphere (Sneddon)", new TipShapeParameter[] {RADIUS}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);

            Sphere indenter = processingModel.getSphere();
            ContactModel indentation = null;              

            if(processingModel.isCorrectSubstrateEffect())
            {
                ThinSampleModel sampleModel = processingModel.buildThinSampleModel();     

                ThicknessCorrectionMethod thicknessCorrectionMethod = processingModel.getThicknessCorrectionMethod();
                indentation = thicknessCorrectionMethod.getParaboloidalContactModel(indenter, sampleModel, precontactModel);
            }
            else
            {
                SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          
                indentation = new HertzianSneddonSphere(indenter, sampleModel, precontactModel); 
            }
            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) 
        {
            return model.isSphereAvailable();
        }

        @Override
        public boolean isSubstrateEffectCorrectionKnown(ThicknessCorrectionMethod correctionMethod, boolean adherentSample)
        {
            return correctionMethod.isSphericalCorrectionKnown();
        }
    },

    SPHERE_FUNG("Sphere (Fung)",  new TipShapeParameter[] {RADIUS}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          

            Sphere indenter = processingModel.getSphere();
            ContactModel indentation = new HyperelasticFungSphere(indenter, sampleModel, precontactModel);              

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isSphereAvailable();
        }
    },
    SPHERE_OGDEN("Sphere (Ogden)", new TipShapeParameter[] {RADIUS}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          

            Sphere indenter = processingModel.getSphere();
            ContactModel indentation = new OgdenIndentation(indenter, sampleModel, precontactModel);              

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isSphereAvailable();
        }
    },
    SPHERE_DMT("Sphere (DMT)", new TipShapeParameter[] {RADIUS}, true) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), true);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          

            Sphere indenter = processingModel.getSphere();
            ContactModel indentation = new DMTIndentation(indenter, sampleModel, precontactModel, processingModel.getAdhesiveEnergyEstimationMethod());              

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isSphereAvailable();
        }
    },
    SPHERE_JKR("Sphere (JKR)", new TipShapeParameter[] {RADIUS}, true)
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel)
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), true);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          

            Sphere indenter = processingModel.getSphere();

            ContactModel indentation = new JKRIndentation(indenter, sampleModel, precontactModel, processingModel.getAdhesiveEnergyEstimationMethod());              

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isSphereAvailable();
        }
    },
    SPHERE_MAUGIS("Sphere (Maugis)", new TipShapeParameter[] {RADIUS}, true) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel)
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), true);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          

            Sphere indenter = processingModel.getSphere();
            ContactModel indentation = new MaugisIndentation(indenter, sampleModel, precontactModel, processingModel.getAdhesiveEnergyEstimationMethod());                         
            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isSphereAvailable();
        }
    },
    HYPERBOLOID("Hyperboloid", new TipShapeParameter[] {RADIUS, HALF_ANGLE}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          

            Hyperboloid hyperboloid = processingModel.getHyperboloid();

            ContactModel indentation = new HertzianHyperboloid(hyperboloid, sampleModel, precontactModel);

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isHyperboloidAvailable();
        }
    },
    HYPERBOLOID_ADHESIVE("Hyperboloid (SAW)", new TipShapeParameter[] {RADIUS, HALF_ANGLE}, true)
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel)
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), true);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          

            Hyperboloid hyperboloid = processingModel.getHyperboloid();
            ContactModel indentation = new SunAkhremitchevWalkerIndentation(hyperboloid, sampleModel, 
                    precontactModel, processingModel.getAdhesiveEnergyEstimationMethod());

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isHyperboloidAvailable();
        }
    },
    BLUNT_CONE("Blunt cone", new TipShapeParameter[] {RADIUS, HALF_ANGLE, TRANSITION_RADIUS}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel)
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          
            BluntConeWithSphericalCap bluntCone = processingModel.getBluntCone();

            ContactModel indentation = new HertzianBluntCone(bluntCone, sampleModel, precontactModel);

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isBluntConeAvailable();
        }
    },
    BLUNT_PYRAMID("Blunt pyramid", new TipShapeParameter[] {RADIUS, HALF_ANGLE, TRANSITION_RADIUS}, false)
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          
            BluntPyramid bluntPyramid = processingModel.getBluntPyramid();

            ContactModel indentation = new HertzianBluntPyramid(bluntPyramid, sampleModel, precontactModel);

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isBluntPyramidAvailable();
        }
    },
    TRUNCATED_CONE("Truncated cone", new TipShapeParameter[] {HALF_ANGLE, TRANSITION_RADIUS}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          
            TruncatedCone truncatedCone = processingModel.getTruncatedCone();
            ContactModel indentation = new HertzianTruncatedCone(truncatedCone, sampleModel, precontactModel);

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isTruncatedConeAvailable();
        }
    },
    TRUNCATED_PYRAMID("Truncated pyramid", new TipShapeParameter[] {HALF_ANGLE, TRANSITION_RADIUS}, false) 
    {
        @Override
        public ContactModel getModel(ProcessingBatchModel processingModel) 
        {
            PrecontactInteractionsModel precontactModel = new BasicPrecontactInteractionsModel(processingModel.getBaselineDegree(), false);
            SampleModel sampleModel =  new BasicSampleModel(processingModel.getPoissonRatio());                          
            TruncatedPyramid truncatedPyramid = processingModel.getTruncatedPyramid();

            ContactModel indentation = new HertzianTruncatedPyramid(truncatedPyramid, sampleModel, precontactModel);

            return indentation;
        }

        @Override
        public boolean isNecessaryInformationProvided(ProcessingBatchModel model) {
            return model.isTruncatedPyramidAvailable();
        }
    };

    private final String prettyName;
    private final List<TipShapeParameter> shapeParameters;
    private final boolean requiresAdhesiveEnergy;

    BasicIndentationModel(String prettyName, TipShapeParameter[] shapeParameters, boolean requiresAdhesiveEnergy)
    {
        this.prettyName = prettyName;
        this.shapeParameters = Arrays.asList(shapeParameters);
        this.requiresAdhesiveEnergy = requiresAdhesiveEnergy;
    }

    public static BasicIndentationModel getValue(String identifier, BasicIndentationModel fallBackValue)
    {
        BasicIndentationModel value = fallBackValue;

        if(identifier != null)
        {
            for(BasicIndentationModel val : BasicIndentationModel.values())
            {
                String estIdentifier =  val.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    value = val;
                    break;
                }
            }
        }

        return value;
    }

    public boolean isTipParameterNeeded(TipShapeParameter parameter)
    {
        return shapeParameters.contains(parameter);
    }

    public boolean requiresAdhesiveEnergy()
    {
        return requiresAdhesiveEnergy;
    }

    public abstract boolean isNecessaryInformationProvided(ProcessingBatchModel model);
    public boolean isSubstrateEffectCorrectionKnown(ThicknessCorrectionMethod correctionMethod, boolean adherentSample)
    {
        return false;
    }

    public abstract ContactModel getModel(ProcessingBatchModel model);

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
