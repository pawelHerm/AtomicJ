
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.statistics;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.functions.AbstractFittedUnivariateFunction;
import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.functions.PowerFunctionCombination;
import atomicJ.utilities.RegressionUtilities;


public class L1Regression implements LinearRegressionEsimator
{
    private static final double TOLERANCE = 1e-12;

    public static L1Regression getL1RegressionForFittedFunction(double[][] data, FittedLinearUnivariateFunction function)
    {      
        int n = data.length;

        double[] residualsData = new double[n];
        for(int i = 0;i<n;i++)
        {
            residualsData[i] = function.residual(data[i]);
        }

        L1Regression l2Regression = new L1Regression(function, residualsData);

        return l2Regression;
    }

    public static double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction f)
    {
        return getObjectiveFunctionValue(data, 0, data.length, f);
    }

    public static double getObjectiveFunctionValue(double[][] points, int from, int to, FittedUnivariateFunction f) 
    {
        double objective = f.absoluteDeviationsSum(points, from, to);
        return objective;
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, UnivariateFunction f) 
    {
        return getObjectiveFunctionValue(ys, xs, 0, ys.length, f);
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, UnivariateFunction f) 
    {
        return AbstractFittedUnivariateFunction.absoluteDeviationsSum(ys, xs, from, to, f);
    }

    public static L1Regression findFit(double[][] data, int deg, boolean constant)
    {
        return findFit(data, RegressionUtilities.getModel(deg, constant));
    };

    public static L1Regression findFit(double[][] data, int from, int to, int deg, boolean constant)
    {
        return findFit(data, from, to, RegressionUtilities.getModel(deg, constant));
    };

    public static L1Regression findFit(double[][] data, double[] model)
    {
        return findFit(data, 0, data.length, model);
    }; 

    public static L1Regression findFit(double[][] data, int from, int to, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, from, to, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        L1Regression regression = new L1Regression(design, obs, model);
        return regression;
    }; 

    public static L1Regression findFit(double[] ys, double[] xs, int from, int to, int deg, boolean constant)
    {
        return findFit(ys, xs, from, to, RegressionUtilities.getModel(deg, constant));
    };

    public static L1Regression findFit(double[] ys, double[] xs, double[] model)
    {
        return findFit(ys, xs, 0, ys.length, model);
    }; 

    public static L1Regression findFit(double[] ys, double[] xs, int from, int to, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(ys, xs, from, to, model);
        L1Regression regression = new L1Regression(regModel, model);
        return regression;
    }; 

    public static L1Regression findFitForSingleExponent(double[][] data, int exponent)
    {
        RegressionModel regModel = RegressionModel.getRegressionModelForSingleExponent(data, exponent);
        L1Regression regression = new L1Regression(regModel, new double[] {exponent});
        return regression;
    }

    public static L1Regression findFitForSingleExponent(double[] ys, double[] xs, int exponent)
    {
        RegressionModel regModel = RegressionModel.getRegressionModelForSingleExponent(ys, xs, exponent);
        L1Regression regression = new L1Regression(regModel, new double[] {exponent});
        return regression;
    }

    public static L1Regression findFitForSingleExponent(double[][] data, double exponent)
    {
        RegressionModel regModel = RegressionModel.getRegressionModelForSingleExponent(data, exponent);
        L1Regression regression = new L1Regression(regModel, new double[] {exponent});
        return regression;
    }

    public static L1Regression findFitForSingleExponent(double[] ys, double[] xs, double exponent)
    {
        RegressionModel regModel = RegressionModel.getRegressionModelForSingleExponent(ys, xs, exponent);
        L1Regression regression = new L1Regression(regModel, new double[] {exponent});
        return regression;
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[] data, int deg, boolean constant)
    {
        return findFitFunction(data, RegressionUtilities.getModel(deg, constant));
    };

    public static FittedLinearUnivariateFunction findFitFunction(double[] data, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        L1Regression regression = new L1Regression(regModel, model);
        return regression.getBestFit();
    }; 

    public static FittedLinearUnivariateFunction findFitFunction(double[][] data, int deg, boolean constant)
    {
        return findFitFunction(data, RegressionUtilities.getModel(deg, constant));
    }

    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent)
    {
        return findFitForSingleExponent(ys, xs, exponent).getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent)
    {
        return findFitForSingleExponent(ys, xs, exponent).getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int exponent)
    {
        return findFitForSingleExponent(data, exponent).getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, double exponent)
    {
        return findFitForSingleExponent(data, exponent).getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[][] data, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        L1Regression regression = new L1Regression(regModel, model);
        return regression.getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int deg, boolean constant)
    {
        return findFitFunction(ys, xs, RegressionUtilities.getModel(deg, constant));
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(ys, xs, model);
        L1Regression regression = new L1Regression(regModel, model);
        return regression.getBestFit();
    }

    public static double[] findParameters(double[][] design, double[] obs)
    {       
        int n = design.length; //Number of points
        int p = design[0].length;    //Number of parameters

        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;

        double[] responses = new double[n+1];
        double[] parameters = new double[p+1];
        for(int i = 1;i<=n;i++)
        {                   
            responses[i] = obs[i - 1];  
        }
        double[][] tableaux = buildTableauxStatic(design, obs, n, p); 

        int[] S = new int[n+1];     

        //STAGE 1

        boolean stage = true; //czy jesteœmy wci¹¿ w stage1?
        int count = 0; // number of simplex operations performed
        int kr = 1;
        int kl = 1;
        int in = 1; //vector to eneter the basis
        int out = 0; //vector to leave the basis

        int k;


        loopA:
            do
            {
                //determine the vector to enter the basis

                in = firstStageVectorToEnterBasisStatic(tableaux, n, p, kr);

                //determine the vector to leave the basis

                loopB:

                    do
                    {

                        /*B*/ k = 0;

                        boolean test = true;

                        for(int i = kl;i<=n;i++) //zlicza k, czyli liczbê wyrazów w kolumnie in (pomiedzy kl a m1) wiêkszych istotnie (powy¿ej tolerancji) od zera 
                        {
                            double d = tableaux[i][in];
                            if(d>TOLERANCE)
                            {
                                k++; 
                                responses[k] = tableaux[i][p1]/d;
                                S[k] = i;
                                test = true;
                            }
                        }


                        loopC:
                            do
                            {
                                /*C*/ if(k > 0)
                                {
                                    int j = 1;
                                    double min = Double.POSITIVE_INFINITY;
                                    for(int i = 1; i<=k; i++)
                                    {
                                        if(responses[i]<min)
                                        {
                                            j = i;
                                            min = responses[i];
                                            out = S[i];
                                        }// j - indeks obserwacji o najmniejszej wartoœci               
                                    }

                                    responses[j] = responses[k];
                                    S[j] = S[k];
                                    k--;
                                }
                                else 
                                {test = false;}

                                //check for linear dependence in stage I
                                if(test||(!stage))
                                {
                                    if(test)
                                    {
                                        double pivot = tableaux[out][in];

                                        if((tableaux[n1][in] - 2*pivot)<= TOLERANCE)
                                        {                               
                                            pivotTableauxStatic(tableaux, n, p, out, in, kr);  //pivot on a[out][in]
                                            count++;
                                            if(stage)
                                            {
                                                //interchange rows in stage I
                                                kl++;
                                                for(int r = kr;r<=p2;r++)
                                                {
                                                    double d = tableaux[out][r];
                                                    tableaux[out][r] = tableaux[count][r];
                                                    tableaux[count][r] = d;
                                                }
                                                /*GT 70*/                   if((count + kr) != p1){continue loopA;/*GO TO 70*/}
                                                else{stage = false;}
                                            }

                                            //STAGE II


                                            //determine the vector to enter the basis
                                            double max = Double.NEGATIVE_INFINITY;
                                            for(int i = kr;i<=p;i++)
                                            {
                                                double d = tableaux[n1][i];
                                                if(d<0)
                                                {
                                                    if(d<=-2)
                                                    {
                                                        d = -d -2;
                                                        if(d>max)
                                                        {
                                                            max = d;
                                                            in = i;
                                                        }
                                                    }
                                                }
                                                else
                                                {
                                                    if(d>max)
                                                    {
                                                        max = d;
                                                        in = i;
                                                    }
                                                }
                                            }
                                            if(max>TOLERANCE) 
                                            {
                                                if(tableaux[n1][in] <= 0)
                                                {
                                                    for(int i = 1;i<=n2;i++)
                                                    {
                                                        tableaux[i][in] = - tableaux[i][in];
                                                    }
                                                    tableaux[n1][in] = tableaux[n1][in] - 2;
                                                }

                                                continue loopB;

                                            }
                                            //No suitable vector to enter the basis. PREPARE THE OUTPUT

                                            prepareOutput1Static(tableaux, parameters, n, p, k, kl, kr, count);

                                            return parameters; 
                                        }
                                        else
                                        {
                                            for(int r = kr; r<=p1;r++)
                                            {
                                                double d = tableaux[out][r];
                                                tableaux[n1][r] = tableaux[n1][r] - d - d;
                                                tableaux[out][r] = -d;
                                            }
                                            tableaux[out][p2] = - tableaux[out][p2];

                                            continue loopC;

                                        }
                                    }
                                    else 
                                    {
                                        tableaux[n2][p1] = 2;

                                        prepareOutput2Static(tableaux, parameters, n,p, k, kl, kr, count);

                                        return parameters; 
                                    }
                                }
                                else
                                {
                                    for(int i = 1; i<=n2; i++)
                                    {
                                        double d = tableaux[i][kr];
                                        tableaux[i][kr] = tableaux[i][in];
                                        tableaux[i][in] = d;
                                    }
                                    kr++; 
                                    if((count + kr) != p1)
                                    {
                                        continue loopA;
                                    }
                                    else
                                    {
                                        stage = false;
                                        //STAGE II


                                        //determine the vector to enter the basis
                                        double max = Double.NEGATIVE_INFINITY;
                                        for(int i = kr;i<=p;i++)
                                        {
                                            double d = tableaux[n1][i];
                                            if(d <= -2)
                                            {
                                                d = -d -2;
                                            }

                                            if(!(d<0||d<=max))
                                            {
                                                max = d;
                                                in = i;
                                            }
                                            if(d>max&&(d>= 0||d<=-2))
                                            {
                                                max = d;
                                                in = i;
                                            }                   
                                        }
                                        if(max>TOLERANCE) //no 
                                        {
                                            if(tableaux[n1][in] <= 0)
                                            {
                                                for(int i = 1;i<=n2;i++)
                                                {
                                                    tableaux[i][in] = - tableaux[i][in];
                                                }
                                                tableaux[n1][in] = tableaux[n1][in] - 2;
                                            }

                                            continue loopB;

                                        }

                                        prepareOutput1Static(tableaux, parameters, n, p, k, kl, kr, count);

                                        return parameters; 

                                    }
                                }
                            }while(true);

                    }while(true);

            } while(true); 

    };

    private L1Regression(RegressionModel regModel, double[] modelExponents)
    {
        this(regModel.getDesign(), regModel.getObservations(), modelExponents);
    }

    private L1Regression(double[][] design, double[] obs, double[] modelExponents)
    {		
        int n = design.length; //Number of points
        int p = design[0].length;    //Number of parameters

        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;

        this.n = n;
        this.p = p;
        this.design = design;
        this.modelExponents = modelExponents;
        this.predictions = new double[n];
        this.responses = new double[n+1];
        this.parameters = new double[p+1];
        this.residuals = new double[n+1];
        for(int i = 1;i<=n;i++)
        {					
            responses[i] = obs[i - 1];	
        }
        this.tableaux = buildTableaux(); 

        int[] S = new int[n+1];		

        //STAGE 1

        boolean stage = true; //czy jesteœmy wci¹¿ w stage1?
        int count = 0; // number of simplex operations performed
        int kr = 1;
        int kl = 1;
        int in = 1; //vector to eneter the basis
        int out = 0; //vector to leave the basis

        int k;


        loopA:
            do
            {
                //determine the vector to enter the basis

                in = firstStageVectorToEnterBasis(kr);

                //determine the vector to leave the basis

                loopB:

                    do
                    {

                        /*B*/	k = 0;

                        boolean test = true;

                        for(int i = kl;i<=n;i++) //zlicza k, czyli liczbê wyrazów w kolumnie in (pomiedzy kl a m1) wiêkszych istotnie (powy¿ej tolerancji) od zera 
                        {
                            double d = tableaux[i][in];
                            if(d>TOLERANCE)
                            {
                                k++; 
                                responses[k] = tableaux[i][p1]/d;
                                S[k] = i;
                                test = true;
                            }
                        }


                        loopC:
                            do
                            {
                                /*C*/	if(k > 0)
                                {
                                    int j = 1;
                                    double min = Double.POSITIVE_INFINITY;
                                    for(int i = 1; i<=k; i++)
                                    {
                                        if(responses[i]<min)
                                        {
                                            j = i;
                                            min = responses[i];
                                            out = S[i];
                                        }// j - indeks obserwacji o najmniejszej wartoœci 				
                                    }

                                    responses[j] = responses[k];
                                    S[j] = S[k];
                                    k--;
                                }
                                else 
                                {test = false;}

                                //check for linear dependence in stage I
                                if(test||(!stage))
                                {
                                    if(test)
                                    {
                                        double pivot = tableaux[out][in];

                                        if((tableaux[n1][in] - 2*pivot)<= TOLERANCE)
                                        {								
                                            pivotTableaux(out, in, kr);  //pivot on a[out][in]
                                            count++;
                                            if(stage)
                                            {
                                                //interchange rows in stage I
                                                kl++;
                                                for(int r = kr;r<=p2;r++)
                                                {
                                                    double d = tableaux[out][r];
                                                    tableaux[out][r] = tableaux[count][r];
                                                    tableaux[count][r] = d;
                                                }
                                                /*GT 70*/					if((count + kr) != p1){continue loopA;/*GO TO 70*/}
                                                else{stage = false;}
                                            }

                                            //STAGE II


                                            //determine the vector to enter the basis
                                            double max = Double.NEGATIVE_INFINITY;
                                            for(int i = kr;i<=p;i++)
                                            {
                                                double d = tableaux[n1][i];
                                                if(d<0)
                                                {
                                                    if(d<=-2)
                                                    {
                                                        d = -d -2;
                                                        if(d>max)
                                                        {
                                                            max = d;
                                                            in = i;
                                                        }
                                                    }
                                                }
                                                else
                                                {
                                                    if(d>max)
                                                    {
                                                        max = d;
                                                        in = i;
                                                    }
                                                }
                                            }
                                            if(max>TOLERANCE) 
                                            {
                                                if(tableaux[n1][in] <= 0)
                                                {
                                                    for(int i = 1;i<=n2;i++)
                                                    {
                                                        tableaux[i][in] = - tableaux[i][in];
                                                    }
                                                    tableaux[n1][in] = tableaux[n1][in] - 2;
                                                }

                                                continue loopB;

                                            }
                                            //No suitable vector to enter the basis. PREPARE THE OUTPUT

                                            prepareOutput1(k, kl, kr, count);

                                            return; 
                                        }
                                        else
                                        {
                                            for(int r = kr; r<=p1;r++)
                                            {
                                                double d = tableaux[out][r];
                                                tableaux[n1][r] = tableaux[n1][r] - d - d;
                                                tableaux[out][r] = -d;
                                            }
                                            tableaux[out][p2] = - tableaux[out][p2];

                                            continue loopC;

                                        }
                                    }
                                    else 
                                    {
                                        tableaux[n2][p1] = 2;

                                        prepareOutput2(k, kl, kr, count);

                                        return; 
                                    }
                                }
                                else
                                {
                                    for(int i = 1; i<=n2; i++)
                                    {
                                        double d = tableaux[i][kr];
                                        tableaux[i][kr] = tableaux[i][in];
                                        tableaux[i][in] = d;
                                    }
                                    kr++; 
                                    if((count + kr) != p1)
                                    {
                                        continue loopA;
                                    }
                                    else
                                    {
                                        stage = false;
                                        //STAGE II


                                        //determine the vector to enter the basis
                                        double max = Double.NEGATIVE_INFINITY;
                                        for(int i = kr;i<=p;i++)
                                        {
                                            double d = tableaux[n1][i];
                                            if(d <= -2)
                                            {
                                                d = -d -2;
                                            }

                                            if(!(d<0||d<=max))
                                            {
                                                max = d;
                                                in = i;
                                            }
                                            if(d>max&&(d>= 0||d<=-2))
                                            {
                                                max = d;
                                                in = i;
                                            }					
                                        }
                                        if(max>TOLERANCE) //no 
                                        {
                                            if(tableaux[n1][in] <= 0)
                                            {
                                                for(int i = 1;i<=n2;i++)
                                                {
                                                    tableaux[i][in] = - tableaux[i][in];
                                                }
                                                tableaux[n1][in] = tableaux[n1][in] - 2;
                                            }

                                            continue loopB;

                                        }

                                        prepareOutput1(k, kl, kr, count);

                                        return ; 

                                    }
                                }
                            }while(true);

                    }while(true);

            } while(true); 

    };

    private double[][] buildTableaux()
    {
        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;

        double[][] tableaux = new double[n2+1][p2+1];

        for(int j = 1;j<=p;j++)
        {
            tableaux[n2][j] = j; // fills the last row of the tableaux with the labels if the nonbasic variables
        }
        for(int i = 1;i<=n;i++)
        {		

            for(int j = 1;j<=p;j++)
            {
                tableaux[i][j] = design[i-1][j-1];
            }

            double f = responses[i];

            tableaux[i][p2] = p + i; // fills the last column of the tableaux with the labels of the basic variables
            tableaux[i][p1] = f; //fills the second - to - last column of the tableaux with the constants, i.e. observed responses

            //deals with any negative constants

            if(f<0)
            {
                for(int j = 1; j<=p2;j++)
                {
                    tableaux[i][j] = -tableaux[i][j];
                }
            }
        }

        //computes and stores the marginal costs in the second - to - last row of the tableaux 
        for(int j = 1;j<=p1;j++)
        {
            double sum = 0;
            for(int i = 1;i<=n;i++)
            {
                sum = sum + tableaux[i][j];
            }
            tableaux[n1][j]= sum;
        }

        return tableaux;
    }

    private static double[][] buildTableauxStatic(double[][] design, double[] responses, int n, int p)
    {
        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;

        double[][] tableaux = new double[n2+1][p2+1];

        for(int j = 1;j<=p;j++)
        {
            tableaux[n2][j] = j; // fills the last row of the tableaux with the labels if the nonbasic variables
        }
        for(int i = 1;i<=n;i++)
        {       

            for(int j = 1;j<=p;j++)
            {
                tableaux[i][j] = design[i-1][j-1];
            }

            double f = responses[i];

            tableaux[i][p2] = p + i; // fills the last column of the tableaux with the labels of the basic variables
            tableaux[i][p1] = f; //fills the second - to - last column of the tableaux with the constants, i.e. observed responses

            //deals with any negative constants

            if(f<0)
            {
                for(int j = 1; j<=p2;j++)
                {
                    tableaux[i][j] = -tableaux[i][j];
                }
            }
        }

        //computes and stores the marginal costs in the second - to - last row of the tableaux 
        for(int j = 1;j<=p1;j++)
        {
            double sum = 0;
            for(int i = 1;i<=n;i++)
            {
                sum = sum + tableaux[i][j];
            }
            tableaux[n1][j]= sum;
        }

        return tableaux;
    }

    private void prepareOutput1(int k, int kl, int kr, int count)
    {
        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;
        for(int i = 1;i<=kl - 1;i++)
        {
            if(tableaux[i][p1]<0)
            {
                for(int r = kr;r<=p2;r++)
                {
                    tableaux[i][r] = - tableaux[i][r];
                }
            }
        }
        tableaux[n2][p1] = 0;
        if(kr == 1)
        {
            for(int i = 1;i<=p;i++)
            {
                double de = Math.abs(tableaux[n1][i]);
                if(de<= TOLERANCE||(2 - de)<= TOLERANCE)
                {
                    break;
                }
                tableaux[n2][p1] = 1; //!!!! DO POPRAWY
            }

        }
        prepareOutput2(k, kl, kr, count);
    }

    private static void prepareOutput1Static(double[][] tableaux, double[] parameters, int n, int p, int k, int kl, int kr, int count)
    {
        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;
        for(int i = 1;i<=kl - 1;i++)
        {
            if(tableaux[i][p1]<0)
            {
                for(int r = kr;r<=p2;r++)
                {
                    tableaux[i][r] = - tableaux[i][r];
                }
            }
        }
        tableaux[n2][p1] = 0;
        if(kr == 1)
        {
            for(int i = 1;i<=p;i++)
            {
                double de = Math.abs(tableaux[n1][i]);
                if(de<= TOLERANCE||(2 - de)<= TOLERANCE)
                {
                    break;
                }
                tableaux[n2][p1] = 1; //!!!! DO POPRAWY
            }

        }
        prepareOutput2Static(tableaux, parameters, n,p, k, kl, kr, count);
    }

    private void prepareOutput2(int k, int kl, int kr, int count)
    {
        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;

        for(int i = 1; i<=n;i++)
        {
            k = (int)Math.round(tableaux[i][p2]);
            double d = tableaux[i][p1];
            if(k<=0)
            {
                k = -k;
                d = -d;
            }
            if(i>=kl)
            {

                k = k - p;
                residuals[k] = d;
            }
            else
            {
                parameters[k] = d;
            }
        }
        tableaux[n2][p2] = count;
        tableaux[n1][p2] = p1 - kr;
        double sum = 0;
        for(int i = kl; i<=n;i++)
        {
            sum = sum + tableaux[i][p1];
        }
        tableaux[n1][p1] = sum;

        lowestCriterion = sum;

        for(int i = 1;i<=n;i++)
        {
            double pred = 0;
            for(int j = 1;j<=p;j++)
            {
                pred = pred + design[i - 1][j - 1]*parameters[j];
            }
            predictions[i - 1] = pred;
        }

        double[] modelCopy = new double[p];
        double[] coeffs = new double[p];

        for(int i = 0;i<p;i++)
        {
            modelCopy[i] = modelExponents[i];
            coeffs[i] = parameters[i + 1];
        }

        bestFit = new PowerFunctionCombination(modelCopy, coeffs);
    }

    private static void prepareOutput2Static(double[][] tableaux, double[] parameters, int n, int p, int k, int kl, int kr, int count)
    {
        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;

        for(int i = 1; i<=n;i++)
        {
            k = (int)Math.round(tableaux[i][p2]);
            double d = tableaux[i][p1];
            if(k<=0)
            {
                k = -k;
                d = -d;
            }
            if(i>=kl)
            {

                k = k - p;
            }
            else
            {
                parameters[k] = d;
            }
        }
        tableaux[n2][p2] = count;
        tableaux[n1][p2] = p1 - kr;
        double sum = 0;
        for(int i = kl; i<=n;i++)
        {
            sum = sum + tableaux[i][p1];
        }
        tableaux[n1][p1] = sum;

    }

    private void pivotTableaux(int out, int in, int kr)
    {
        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;

        double pivot = tableaux[out][in];

        for(int i = kr;i<=p1;i++)
        {
            if(i != in)
            {
                tableaux[out][i] = tableaux[out][i]/pivot;
            }
        }

        for(int i = 1;i<=n1;i++)
        {
            if(i != out)
            {
                double d = tableaux[i][in];
                for(int r = kr;r<=p1;r++)
                {
                    if(r != in)
                    {
                        tableaux[i][r] = tableaux[i][r] - d*tableaux[out][r];
                    }
                }
            }
        }
        for(int i = 1; i<=n1;i++)
        {
            if(i != out)
            {
                tableaux[i][in] = -tableaux[i][in]/pivot;
            }
        }
        tableaux[out][in] = 1/pivot;

        double d = tableaux[out][p2];
        tableaux[out][p2] = tableaux[n2][in];	
        tableaux[n2][in] = d;
    }

    private static void pivotTableauxStatic(double[][] tableaux, int n, int p, int out, int in, int kr)
    {
        int n1 = n + 1; 
        int p1 = p + 1;
        int n2 = n + 2; 
        int p2 = p + 2;

        double pivot = tableaux[out][in];

        for(int i = kr;i<=p1;i++)
        {
            if(i != in)
            {
                tableaux[out][i] = tableaux[out][i]/pivot;
            }
        }

        for(int i = 1;i<=n1;i++)
        {
            if(i != out)
            {
                double d = tableaux[i][in];
                for(int r = kr;r<=p1;r++)
                {
                    if(r != in)
                    {
                        tableaux[i][r] = tableaux[i][r] - d*tableaux[out][r];
                    }
                }
            }
        }
        for(int i = 1; i<=n1;i++)
        {
            if(i != out)
            {
                tableaux[i][in] = -tableaux[i][in]/pivot;
            }
        }
        tableaux[out][in] = 1/pivot;

        double d = tableaux[out][p2];
        tableaux[out][p2] = tableaux[n2][in];   
        tableaux[n2][in] = d;
    }

    //In the first stage, the vector to enter the basis is chosen as that with the largest nonnegative marginal cost
    private int firstStageVectorToEnterBasis(int kr)
    {		
        int in = 1;
        int n1 = n + 1; 
        int n2 = n + 2; 
        double max = -1;

        for(int j = kr;j<=p;j++) // finds vector with largest marginal cost
        {
            if(Math.abs(tableaux[n2][j]) <= p)
            {
                double d = Math.abs(tableaux[n1][j]); // suma wyrazów w j-tej kolumnie
                if(d > max)
                {
                    max = d;
                    in = j;	//indeks wektora to enter the basis			 
                }				
            }						
        }

        if(tableaux[n1][in]<0) // Je¿eli in - ty wyraz m1 rzedu jest mniejszy od zera, to zmieñ wartoœc wszystkich wyrazów z in - tej kolumny na przeciwn¹
        {
            for(int i = 1;i<=n2;i++)
            {
                tableaux[i][in] = - tableaux[i][in];  
            }
        }

        return in;
    }

    private static int firstStageVectorToEnterBasisStatic(double[][] tableaux, int n, int p, int kr)
    {       
        int in = 1;
        int n1 = n + 1; 
        int n2 = n + 2; 
        double max = -1;

        for(int j = kr;j<=p;j++) // finds vector with largest marginal cost
        {
            if(Math.abs(tableaux[n2][j]) <= p)
            {
                double d = Math.abs(tableaux[n1][j]); // suma wyrazów w j-tej kolumnie
                if(d > max)
                {
                    max = d;
                    in = j; //indeks wektora to enter the basis          
                }               
            }                       
        }

        if(tableaux[n1][in]<0) // Je¿eli in - ty wyraz m1 rzedu jest mniejszy od zera, to zmieñ wartoœc wszystkich wyrazów z in - tej kolumny na przeciwn¹
        {
            for(int i = 1;i<=n2;i++)
            {
                tableaux[i][in] = - tableaux[i][in];  
            }
        }

        return in;
    }



    private int n; //number of points
    private int p; //number of predicates
    private double lowestCriterion;
    private FittedLinearUnivariateFunction bestFit;

    private double[] responses;
    private double[] predictions;
    private final double[] residuals;
    private double[] parameters;
    private double[] modelExponents;

    private double[][] design;
    private double[][] tableaux;

    private L1Regression(FittedLinearUnivariateFunction bestFit, double[] residuals)
    {
        this.bestFit = bestFit; 
        this.residuals = residuals;
        this.lowestCriterion = ResidualVector.getAbsoluteValuesSum(residuals);
    }   

    @Override
    public ResidualVector getResiduals()
    {
        return new ResidualVector(residuals);
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit() 
    {
        return bestFit;
    }

    @Override
    public double getObjectiveFunctionMinimum() 
    {
        return lowestCriterion;
    }
}
