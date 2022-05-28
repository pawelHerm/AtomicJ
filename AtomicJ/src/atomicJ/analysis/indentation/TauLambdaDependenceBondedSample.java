package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;

public enum TauLambdaDependenceBondedSample implements BivariateFunction, PoissonRatioDependentFunctionSource
{
    DEGREE_ONE
    {
        @Override
        public double value(double reducedIndent, double poisson) 
        {
            double poisson2 = poisson*poisson;
            double poisson3 = poisson2*poisson;

            double tau = Double.NaN;    

            if(reducedIndent <= 1.15)
            { 
                double redIndent2 = reducedIndent*reducedIndent;
                double redIndent3 = reducedIndent*redIndent2;

                if(poisson <= 0.335)
                {
                    tau = reducedIndent*(2./Math.PI + 
                            reducedIndent*(0.15779672737304437 + 0.06840530354536055*poisson - 0.23585464654588828*poisson2 + 0.7788851924549571*poisson3) +
                            redIndent2*(0.08800725655294439 - 0.033576877177366696*poisson + 0.5588982164949963*poisson2 - 0.27779152618446423*poisson3)+
                            redIndent3*(-0.062282713156367624 - 0.0005388902027916812*poisson - 0.34434846103070804*poisson2 + 0.16275653077746224*poisson3));

                    return tau;
                }
                else if (poisson <= 0.46)
                {
                    tau = reducedIndent*(2./Math.PI + reducedIndent*(0.18762201465696962 - 0.21374482031714526*poisson + 0.5105379675298859*poisson2)+
                            redIndent2*(0.2955368963370133 - 1.0536746824470338*poisson + 2.129460651323444*poisson2)+
                            redIndent3*(-0.14199921269299312 + 0.20192954810911423*poisson - 0.8621961300063662*poisson2)+
                            redIndent2*redIndent2*(0.0024313783776132884 + 0.1032992023004699*poisson - 0.029426913311226034*poisson2));
                }
                else 
                {
                    tau = reducedIndent*(2./Math.PI + reducedIndent*(0.27794606588280407 - 0.616567116749741*poisson + 0.9716215697870365*poisson2)+
                            redIndent2*(0.6607248037733947 - 2.676019051609851*poisson + 3.8697082200225648*poisson2)+
                            redIndent3*(0.09087322569551935 - 0.8298452076713472*poisson + 0.3737258024462021*poisson2)+
                            redIndent2*redIndent2*(-0.022263507916910005 + 0.2293869566289579*poisson - 0.2290038925274933*poisson2));

                }
            }
            else if(reducedIndent <= 9)
            {
                double redIndent2 = reducedIndent*reducedIndent;
                double redIndent3 = reducedIndent*redIndent2;

                if(poisson <= 0.335)
                {
                    tau = -0.174083266081143 + 0.05157215878854209*poisson + 0.15302096940890553*poisson2 - 0.9825036646359249*poisson3 + 
                            reducedIndent*(0.9873566015098587 - 0.016309373116029393*poisson - 0.10721758623454879*poisson2 + 1.6712555718983306*poisson3) +
                            redIndent2*(0.003425076929875005 + 0.0014935862085477625*poisson - 
                                    0.0389587490721189*poisson2 - 0.1059719699279704*poisson3)+
                            redIndent3*(-0.0002478274731100035 + 0.004097730253250978*poisson2);

                    return tau;
                }
                else if(poisson <= 0.464)
                {
                    double poisson4 = poisson2*poisson2;
                    tau =  6.462528077828544 - 69.52209653521459*poisson + 273.08478232070837*poisson2 - 475.83950823916535*poisson3 + 308.9953762782084*poisson4 + 
                            reducedIndent*(-11.106237063559607 + 127.31901166019874*poisson - 502.26503335249186*poisson2 + 880.9828892747998*poisson3 - 576.8277811553316*poisson4) +
                            redIndent2*(6.857899723101601 - 72.74086593434485*poisson + 289.6714557281282*poisson2 - 513.8929560462417*poisson3 + 342.7390648496734*poisson4)+
                            redIndent3*(-0.7040400301120895 + 7.52265545366686*poisson - 30.215560405575754*poisson2 + 54.16288068102308*poisson3 - 36.60320165111102*poisson4)+
                            redIndent2*redIndent2*(0.023538655383046735 - 0.2536617501194275*poisson + 1.0290475800429943*poisson2 - 1.8666579772383198*poisson3 + 1.2799506115036696*poisson4);

                    return tau;
                }
                else if(poisson <= 0.491)
                {
                    tau =  -66.22167923977655 + 434.10560319347934*poisson - 951.5396493302807*poisson2 + 695.1052188806927*poisson3 + 
                            reducedIndent*(77.98807724662706 - 513.6911434964358*poisson + 1143.5282318146888*poisson2 - 848.0667852467984*poisson3) +
                            redIndent2*(-9.46266106638666 + 73.14117448905424*poisson - 185.4799778619998*poisson2 + 154.92298231467137*poisson3)+
                            redIndent3*(-11.488352933190296 + 71.83679846737995*poisson - 149.32424479529854*poisson2 + 103.11357700401928*poisson3)+
                            redIndent2*redIndent2*(0.7991387064822842 - 5.045849054405497*poisson + 10.604955891976866*poisson2 - 7.416134948708973*poisson3);

                    return tau;

                }
                else if(poisson <= 0.4995)
                {
                    tau =  1657.4218127291615 - 10116.592967498716*poisson + 
                            20576.218993812774*poisson2 - 13946.870146049645*poisson3 + 
                            reducedIndent*(-2713.673712489817 + 16583.73030848168*poisson - 33761.01189162837*poisson2 + 22904.810873572056*poisson3) +
                            redIndent2*(1377.1091359580728 - 8430.548888880528*poisson + 17198.678805201285*poisson2 - 11691.322778939495*poisson3)+
                            redIndent3*(-211.4133576121991 + 1304.1667175081138*poisson - 2681.2109832364936*poisson2 + 1836.9930285990185*poisson3)+
                            redIndent2*redIndent2*(-10.094956507618807 + 60.32890583804894*poisson - 120.12365928175441*poisson2 + 79.69390877155158*poisson3);                   
                    return tau;
                }
                //bonded incompressible sample
                else
                {
                    return TauLambdaDependenceBondedIncompressible.DEGREE_ONE.value(reducedIndent);
                }
            }
            else
            {
                double redIndent2 = reducedIndent*reducedIndent;

                if(poisson <= 0.335)
                {
                    tau = -0.17910305896723142 + 1.00008215540866*reducedIndent - 0.34588268670115274*poisson + 1.7930592032563406*poisson2;
                }
                else if(poisson <= 0.464)
                {
                    double poisson4 = poisson2*poisson2;

                    tau =  23.821611042029794 - 259.8577601024882*poisson + 1057.7661134320938*poisson2 - 1922.213577811237*poisson3 + 1325.5739276191935*poisson4 + 
                            reducedIndent*(10.713187321599898 - 101.59928216430521*poisson + 397.8797314692002*poisson2 - 691.7448122934006*poisson3 + 450.88773494421764*poisson4) +
                            redIndent2*(-0.18954018775657105 + 1.9828310602470622*poisson - 7.766146455991301*poisson2 + 13.50392195916428*poisson3 - 8.803179893336676*poisson4);

                    return tau;
                }
                else if(poisson <= 0.49)
                {
                    tau = 4419.480332630096 - 28197.42426540026*poisson + 59966.08637145508*poisson2 - 42504.57824981022*poisson3 + 
                            reducedIndent*(-1091.4777037121314 + 6995.610001507279*poisson - 14937.6710806309*poisson2 + 10636.884108743941*poisson3) +
                            redIndent2*(32.38729449856241 - 207.9192402560032*poisson + 445.1559459472669*poisson2 - 317.88179379632714*poisson3) +
                            redIndent2*reducedIndent*(-0.3470835572726042 + 2.234345689671312*poisson - 4.7975274300319155*poisson2 + 3.4362338400973726*poisson3);

                    return tau;
                }
                else if(poisson <= 0.495)
                {
                    tau = -2280.203539160009 + 9356.713818512962*poisson - 9598.690886845447*poisson2 + 
                            reducedIndent*(454.95066276154205 - 1871.323665843675*poisson + 1929.4481904265078*poisson2) +
                            redIndent2*(-5.320157114491838 + 22.38833922638572*poisson - 23.574165354112093*poisson2) +
                            redIndent2*reducedIndent*(-0.024917914920468867 + 0.09309091039320053*poisson - 0.08563744222380257*poisson2);

                    return tau;

                }
                //bonded incompressible sample
                else
                {
                    return TauLambdaDependenceBondedIncompressible.DEGREE_ONE.value(reducedIndent);
                } 
            }
            return tau;
        }

    },

    DEGREE_TWO  
    {
        @Override
        public double value(double reducedIndent, double poisson)
        {
            double redIndentRoot = Math.sqrt(reducedIndent);
            double redIndent1d5 = reducedIndent*redIndentRoot;

            double poisson2 = poisson*poisson;
            double poisson3 = poisson2*poisson;

            double tau = Double.NaN;    

            if(poisson <= 0.395)
            {                              
                if(reducedIndent <= 3)
                {
                    double lambda2 = reducedIndent*reducedIndent;               
                    double lambda2d5 = lambda2*redIndentRoot;

                    tau = redIndentRoot*(1 + redIndentRoot*(0.24958836915803728 +  0.06251775953518347*poisson - 0.009280333438805568*poisson2 + 0.5858528091990169*poisson3) + 
                            reducedIndent*(0.261319339831859 + 0.162720216367164*poisson - 0.34791720566510365*poisson2 + 2.4985291567690675*poisson3) + 
                            redIndent1d5*(-0.4685304334758228 - 0.3451253667516817*poisson - 0.014410138367969979*poisson2 - 2.7517872763183426*poisson3) + 
                            lambda2*(0.2509661706021398 + 0.22154853065264551*poisson + 0.13789580846768276*poisson2 + 1.1927439462155596*poisson3) + 
                            lambda2d5*(-0.04683288380491873 - 0.04746456462931809*poisson - 0.03939696357929425*poisson2 - 0.1958939404386966*poisson3));

                }
                else if(reducedIndent <= 350)
                {
                    double poisson4 = poisson2*poisson2;

                    tau = -0.172274593335174 + 0.0894019341051992*poisson - 0.6630691043687543*poisson2 + 4.337803282030867*poisson3 - 7.429688380884887*poisson4 + 
                            redIndentRoot*(1.4101815279793168 - 0.09397459328745088*poisson + 1.0274417981226551*poisson2 - 5.17659833335623*poisson3 + 10.449678429948847*poisson4) + 
                            reducedIndent*(0.00047221417302174183 + 0.008108937934876098*poisson - 0.08432843128644175*poisson2 + 0.4173848539036701*poisson3 - 0.8672306486787822*poisson4) + 
                            redIndent1d5*(-0.00001563828453477203 - 0.00022973020633994043*poisson + 0.0023124483216741265*poisson2 - 0.011302783398024333*poisson3 + 0.023952501600832923*poisson4);

                }
                else
                {
                    tau = -0.1872838939400115 + 0.24426704744220898*poisson - 3.142451718976111*poisson2 + 10.069012294811783*poisson3 + 
                            redIndentRoot*(1.414180915265325 + 0.0016912334850354201*poisson - 0.022205097339493617*poisson2 + 0.06126908184467482*poisson3);

                }

                return tau;
            }

            if(reducedIndent <= 3)
            {
                double redIndent2 = reducedIndent*reducedIndent;
                double redIndent2d5 = redIndent2*redIndentRoot;

                tau = redIndentRoot*(1 + redIndentRoot*(0.03232391759288445 + 1.6973409015591232*poisson - 4.11367011296193*poisson2 + 4.038073365734686*poisson3) + 
                        reducedIndent*(-0.18994328880770092 + 3.9032992797257093*poisson - 10.994588429325631*poisson2 + 12.770292806208175*poisson3) + 
                        redIndent1d5*(-1.8369911565645098 + 9.45840454603823*poisson - 23.00153901771617*poisson2 + 14.891716703336009*poisson3) + 
                        redIndent2*(0.04820056422739634 + 1.3881281260317229*poisson - 2.1513406641862667*poisson2 + 2.7230726954033475*poisson3) + 
                        redIndent2d5*(0.1043563077039589 - 1.0599318037112146*poisson + 2.2498420627454716*poisson2 - 1.931314098419292*poisson3));

            }
            else if(reducedIndent <= 65)
            {
                double lambda2 = reducedIndent*reducedIndent;

                double poisson4 = poisson2*poisson2;

                if(poisson <=  0.4865)
                {
                    tau = 265.7501147725815 - 2463.8194458993794*poisson + 8544.467145182054*poisson2 - 13139.251105283041*poisson3 + 7554.060868709229*poisson4 + 
                            redIndentRoot*(-376.28987564750463 + 3513.8158805763087*poisson - 12241.400667672531*poisson2 + 18922.858636779365*poisson3 - 10943.9955423681*poisson4) + 
                            reducedIndent*(172.24686119821985 - 1611.2938262341995*poisson + 5648.541805123831*poisson2 - 8794.80338247426*poisson3 + 5131.072729922444*poisson4) + redIndent1d5*(-18.141399348712085 + 170.2351302767614*poisson - 598.802179283334*poisson2 + 935.8085260193508*poisson3 - 548.2308230396004*poisson4) + 
                            lambda2*(0.6532733242439945 - 6.148131983750188*poisson + 21.694209166418418*poisson2 - 34.019305141617934*poisson3 + 20.003775607489082*poisson4);
                }
                else
                {
                    tau = -546486.6455164192 + 4.446823983831823e6*poisson - 1.3568423598620173e7*poisson2 + 1.8399384934972685e7*poisson3 - 9.355885185627606e6*poisson4 + 
                            redIndentRoot*(662287.7185172221 - 5.39174456824668e6*poisson + 1.6459753214965135e7*poisson2 - 2.2331256519675102e7*poisson3 + 1.1360878376765661e7*poisson4) + 
                            reducedIndent*(-266741.69177095324 + 2.173456065072321e6*poisson - 6.640864393873373e6*poisson2 + 9.017730275788212e6*poisson3 - 4.591783104727585e6*poisson4) + 
                            redIndent1d5*(37411.28815476328 - 305415.6444550781*poisson + 934982.5443190399*poisson2 - 1.272110337752946e6*poisson3 + 649034.9148446621*poisson4) + lambda2*(-146.06018450608866 + 1255.3562105046788*poisson - 4035.7847641894123*poisson2 + 5753.0470096576155*poisson3 - 3068.8368386525244*poisson4);
                }
            }
            else if(reducedIndent <= 350)
            {
                if(poisson <= 0.4865)
                {
                    double lambda2 = reducedIndent*reducedIndent;

                    double poisson4 = poisson2*poisson2;

                    tau = -1699.3965089017893 + 15834.130218666542*poisson - 55274.58654770711*poisson2 + 85671.80420983786*poisson3 - 49740.9831278942*poisson4 + 
                            redIndentRoot*(669.6349915277239 - 6250.322506708639*poisson + 21910.768020368956*poisson2 - 34121.44825534927*poisson3 + 19921.166918958843*poisson4) + reducedIndent*(-41.7133545969465 + 390.96023685379726*poisson - 1373.4800263691147*poisson2 + 2143.8581022548037*poisson3 - 1254.7950559101885*poisson4) + 
                            redIndent1d5*(1.3356478389330153 - 12.544158208157725*poisson + 44.16526091350369*poisson2 - 69.09882017951313*poisson3 + 40.54580498147442*poisson4) + lambda2*(-0.017334165082196762 + 0.16313535659882236*poisson - 0.5756217454089679*poisson2 + 0.9026920244046908*poisson3 - 0.5310130375754248*poisson4);
                }
                else if(poisson <= 0.496)
                {
                    tau = 90261.95882273496 - 554621.5391190115*poisson + 1.1359860693144288e6*poisson2 - 775588.4597657217*poisson3 + 
                            redIndentRoot*(-19088.888028919067 + 117478.31873158322*poisson - 241000.8858858207*poisson2 + 164815.7250291921*poisson3) + reducedIndent*(298.8886067356442 - 1842.660541406232*poisson + 3787.1603279629094*poisson2 - 2594.890262779642*poisson3);
                }
                else if(poisson <= 0.4985)
                {
                    tau = 1.1443628522782866e6 - 6.935053571278696e6*poisson + 1.4009535154932406e7*poisson2 - 9.433772230811147e6*poisson3 + 
                            redIndentRoot*(-112644.72493020512 + 684795.6686834638*poisson - 1.3877279973384675e6*poisson2 + 937448.1479723391*poisson3) + reducedIndent*(-9812.277081457785 + 59223.95876122013*poisson - 119150.47349751742*poisson2 + 79903.24700648164*poisson3);
                }
                else
                {
                    tau = -8.074753239510392e7 + 4.854116606326263e8*poisson - 9.72678325070494e8*poisson2 + 6.496902579713252e8*poisson3 + 
                            redIndentRoot*(1.789014211401744e7 - 1.0755710738465197e8*poisson + 2.15547340685519e8*poisson2 - 1.4398737292561015e8*poisson3) + reducedIndent*(-1.0282525602178238e6 + 6.1831053967653625e6*poisson -  1.2393449527317964e7*poisson2 + 8.280497952327611e6*poisson3);
                }
            }
            else 
            {
                if(poisson <= 0.4865)
                {
                    double poisson4 = poisson2*poisson2;

                    tau = 1894.9200943070302 - 17752.904990433508*poisson + 62342.184263394214*poisson2 - 97283.9178210209*poisson3 + 56942.27797652527*poisson4 + 
                            redIndentRoot*(51.20918147430391 - 464.10964290470554*poisson + 1620.7484215013772*poisson2 - 2513.585207053882*poisson3 + 1460.918724589009*poisson4);
                }
                else if(poisson <= 0.496)
                {
                    tau = -33035.99869744722 + 204565.6388771129*poisson - 422354.5218898251*poisson2 + 290761.856739433*poisson3 + 
                            redIndentRoot*(-6897.618144042798 + 42370.95559115276*poisson - 86744.89769183913*poisson2 + 59199.30025214321*poisson3);
                }
                else if(poisson <= 0.4985)
                {
                    tau = 2.9366757538466095e6 - 1.773699906879868e7*poisson + 3.570907797248634e7*poisson2 - 2.396354635553362e7*poisson3 + 
                            redIndentRoot*(-389281.2299157363 + 2.353617106649313e6*poisson - 4.74338484990181e6*poisson2 + 3.186564866266088e6*poisson3);
                }
                else
                {
                    double poisson4 = poisson2*poisson2;

                    tau = -1.5130017903472034e11 + 1.2129973677108599e12*poisson - 3.6467967051307383e12*poisson2 + 4.872824188024961e12*poisson3 - 2.4416376326779614e12*poisson4 + 
                            redIndentRoot*(1.0125604501790016e10 - 8.117934901496094e10*poisson + 2.4406274922250095e11*poisson2 - 3.2611815676009753e11*poisson3 + 1.634104367531114e11*poisson4);
                }
            }
            return tau;
        }
    }
    ;        

    @Override
    public UnivariateFunction getFunction(final double poissonRatio)
    {
        UnivariateFunction function = new UnivariateFunction() {

            @Override
            public double value(double tau) {
                return TauLambdaDependenceBondedSample.this.value(tau, poissonRatio);
            }
        };
        return function;
    }

}