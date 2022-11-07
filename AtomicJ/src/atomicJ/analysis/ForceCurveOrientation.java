package atomicJ.analysis;

import java.util.Arrays;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.ArrayUtilities;


public enum ForceCurveOrientation 
{
    LEFT {
        @Override
        public ForceCurveBranch guessBranch(double[][] sortedBranch)
        {
            int n = sortedBranch.length;

            double x0 = sortedBranch[0][0];            
            double xn = sortedBranch[n - 1][0];

            ForceCurveBranch branch = (x0 > xn) ? ForceCurveBranch.APPROACH : ForceCurveBranch.WITHDRAW;

            return branch;
        }

        @Override
        public ForceCurveBranch guessBranch(double[] sortedBranchXs, double[] sortedBranchYs)
        {
            int n = sortedBranchXs.length;

            double x0 = sortedBranchXs[0];            
            double xn = sortedBranchXs[n - 1];

            ForceCurveBranch branch = (x0 > xn) ? ForceCurveBranch.APPROACH : ForceCurveBranch.WITHDRAW;

            return branch;
        }

        //modifies the passed-in sortedBranch
        @Override
        public double[][] correctOrientation(double[][] sortedBranch, ForceCurveBranch branchType)
        {
            if(!ForceCurveBranch.APPROACH.equals(branchType) && !ForceCurveBranch.WITHDRAW.equals(branchType))
            {
                throw new IllegalArgumentException("Unknown branch type");
            }

            correctOrientation(sortedBranch);
            branchType.getDefaultXOrderForLeftHandSideContactOrientation().sortXIfNecessary(sortedBranch);          

            return sortedBranch;
        }

        //modifies the passed-in sortedBranch
        @Override
        public double[][] sortX(double[][] branchPoints, ForceCurveBranch branchType)
        {
            return branchType.getDefaultXOrderForLeftHandSideContactOrientation().sortX(branchPoints); 
        }

        //modifies the passed-in sortedBranch
        @Override
        public double[][] sortXIfNecessary(double[][] branchPoints, ForceCurveBranch branchType)
        {
            return branchType.getDefaultXOrderForLeftHandSideContactOrientation().sortXIfNecessary(branchPoints); 
        }
    }, 

    RIGHT 
    {
        @Override
        public ForceCurveBranch guessBranch(double[][] sortedBranch)
        {
            int n = sortedBranch.length;

            double x0 = sortedBranch[0][0];            
            double xn = sortedBranch[n - 1][0];

            ForceCurveBranch branch = (xn > x0) ? ForceCurveBranch.APPROACH : ForceCurveBranch.WITHDRAW;

            return branch;
        }

        @Override
        public ForceCurveBranch guessBranch(double[] sortedBranchXs, double[] sortedBranchYs)
        {
            int n = sortedBranchXs.length;

            double x0 = sortedBranchXs[0];            
            double xn = sortedBranchXs[n - 1];

            ForceCurveBranch branch = (xn > x0) ? ForceCurveBranch.APPROACH : ForceCurveBranch.WITHDRAW;

            return branch;
        }

        //branch must be sorted, but it does not matter whether it is sorted in ascending or descending order
        //order may be partly broken, but not on array ends 
        //modifies the passed in array
        @Override
        public double[][] correctOrientation(double[][] sortedBranch, ForceCurveBranch branchType)
        {
            if(!ForceCurveBranch.APPROACH.equals(branchType) && !ForceCurveBranch.WITHDRAW.equals(branchType))
            {
                throw new IllegalArgumentException("Unknown branch type");
            }

            correctOrientation(sortedBranch);
            branchType.getDefaultXOrderForRightHandSideContactOrientation().sortXIfNecessary(sortedBranch);

            return sortedBranch;
        }

        //modifies the passed-in sortedBranch
        @Override
        public double[][] sortX(double[][] branchPoints, ForceCurveBranch branchType)
        {
            return branchType.getDefaultXOrderForRightHandSideContactOrientation().sortX(branchPoints); 
        }

        //modifies the passed-in sortedBranch
        @Override
        public double[][] sortXIfNecessary(double[][] branchPoints, ForceCurveBranch branchType)
        {
            return branchType.getDefaultXOrderForRightHandSideContactOrientation().sortXIfNecessary(branchPoints); 
        }
    };

    //branch must be sorted, but it does not matter whether it is sorted in ascending or descending order
    //order may be partly broken, but not on array ends 

    //If we denote the first point in 'sortedBranch' as p1 (p1 = sortedBranch[0]) and the last one
    //as pn (pn = sortedBranch[sortedBranch.length - 1]), then
    //the orientation is RIGHT if p1 has both coordinates larger than pn,
    //or when pn has both coordinates larger than p1
    public static ForceCurveOrientation resolveOrientation(double[][] sortedBranch)
    {
        int n = sortedBranch.length;

        double x0 = sortedBranch[0][0];
        double y0 = sortedBranch[0][1];

        double xn = sortedBranch[n - 1][0];
        double yn = sortedBranch[n - 1][1];

        return resolveOrientation(x0, y0, xn, yn);
    }

    public static ForceCurveOrientation resolveOrientation(double[] sortedBranchXs, double[] sortedBranchYs)
    {
        int n = sortedBranchYs.length;

        double x0 = sortedBranchXs[0];
        double y0 = sortedBranchYs[0];

        double xn = sortedBranchXs[n - 1];
        double yn = sortedBranchYs[n - 1];

        return resolveOrientation(x0, y0, xn, yn);
    }

    public static ForceCurveOrientation resolveOrientation(double x0, double y0, double xn, double yn)
    {                
        ForceCurveOrientation orientation = (x0 > xn != y0 > yn) ? LEFT : RIGHT;  

        return orientation;
    }

    public double[][] correctOrientation(double[][] sortedBranch)
    {
        ForceCurveOrientation originalOrientation = resolveOrientation(sortedBranch);

        if(!this.equals(originalOrientation))
        {
            ArrayUtilities.negateXs(sortedBranch);
        }

        return sortedBranch;
    }

    public void correctOrientation(double[] sortedBranchXs, double[] sortedBranchYs)
    {
        ForceCurveOrientation originalOrientation = resolveOrientation(sortedBranchXs, sortedBranchYs);

        if(!this.equals(originalOrientation))
        {
            ArrayUtilities.negate(sortedBranchXs);
        }
    }


    public abstract double[][] correctOrientation(double[][] sortedBranch, ForceCurveBranch branch);
    public abstract ForceCurveBranch guessBranch(double[][] sortedBranch);
    public abstract ForceCurveBranch guessBranch(double[] sortedBranchXs, double[] sortedBranchYs);
    public abstract double[][] sortX(double[][] branchPoints, ForceCurveBranch branchType);
    public abstract double[][] sortXIfNecessary(double[][] branchPoints, ForceCurveBranch branchType);

    public static ForceCurveSimpleStorage partition(double[][] points)
    {
        int n = points.length;

        if(n == 0)
        {
            ForceCurveSimpleStorage.getEmpty();
        }

        boolean xInitiallyDescending = SortedArrayOrder.DESCENDING.equals(SortedArrayOrder.getInitialXOrder(points));

        int turnIndex = xInitiallyDescending ? SortedArrayOrder.getIndexOfFirstAscendingXResistantToIsolatedPoints(points) : SortedArrayOrder.getIndexOfFirstDescendingXResistantToIsolatedPoints(points);

        if(turnIndex == n)
        {
            ForceCurveOrientation.LEFT.correctOrientation(points);
            ForceCurveBranch firstBranchType = ForceCurveOrientation.LEFT.guessBranch(points);
            boolean firstApproach = ForceCurveBranch.APPROACH.equals(firstBranchType);

            double[][] approachBranch = firstApproach?  points : new double[][] {};
            double[][] withdrawBranch = firstApproach ? new double[][] {} : points;

            return new ForceCurveSimpleStorage(approachBranch, withdrawBranch);
        }
        else
        {
            double[][] firstBranchPoints = Arrays.copyOf(points, turnIndex);
            double[][] secondBranchPoints = Arrays.copyOfRange(points, turnIndex, n);

            ForceCurveOrientation.LEFT.correctOrientation(firstBranchPoints);
            ForceCurveOrientation.LEFT.correctOrientation(secondBranchPoints);

            ForceCurveBranch firstBranchType = ForceCurveOrientation.LEFT.guessBranch(firstBranchPoints);
            boolean firstApproach = ForceCurveBranch.APPROACH.equals(firstBranchType);

            double[][] approachBranch = firstApproach? firstBranchPoints : secondBranchPoints;
            double[][] withdrawBranch = firstApproach ? secondBranchPoints : firstBranchPoints;

            return new ForceCurveSimpleStorage(approachBranch, withdrawBranch);
        }
    }

    public static ForceCurveChannelStorage partition(double[] xs, double[] ys, Quantity xQuantity, Quantity yQuantity)
    {
        int n = ys.length;

        if(n == 0)
        {
            ForceCurveSimpleStorage.getEmpty();
        }

        boolean xInitiallyDescending = SortedArrayOrder.DESCENDING.equals(SortedArrayOrder.getInitialOrder(xs));

        int turnIndex = xInitiallyDescending ? SortedArrayOrder.getIndexOfFirstAscendingValueResistantToIsolatedPoints(xs) : SortedArrayOrder.getIndexOfFirstDescendingValueResistantToIsolatedPoints(xs);

        if(turnIndex == n)
        {
            ForceCurveOrientation.LEFT.correctOrientation(xs, ys);
            ForceCurveBranch firstBranchType = ForceCurveOrientation.LEFT.guessBranch(xs, ys);
            boolean firstApproach = ForceCurveBranch.APPROACH.equals(firstBranchType);

            double[] approachBranchXs = firstApproach ? xs : new double[] {};
            double[] approachBranchYs = firstApproach ? ys : new double[] {};

            double[] withdrawBranchXs = firstApproach ? new double[] {} : xs;
            double[] withdrawBranchYs = firstApproach ? new double[] {} : ys;

            Channel1DData approach = new FlexibleFlatChannel1DData(approachBranchXs, approachBranchYs, xQuantity, yQuantity, SortedArrayOrder.DESCENDING);
            Channel1DData withdraw = new FlexibleFlatChannel1DData(withdrawBranchXs, withdrawBranchYs, xQuantity, yQuantity, SortedArrayOrder.ASCENDING);

            return new ForceCurveChannelStorage(approach, withdraw);
        }
        else
        {
            double[] firstBranchPointsXs = Arrays.copyOf(xs, turnIndex);
            double[] firstBranchPointsYs = Arrays.copyOf(ys, turnIndex);

            double[] secondBranchPointsXs = Arrays.copyOfRange(xs, turnIndex, n);
            double[] secondBranchPointsYs = Arrays.copyOfRange(ys, turnIndex, n);

            ForceCurveOrientation.LEFT.correctOrientation(firstBranchPointsXs, firstBranchPointsYs);
            ForceCurveOrientation.LEFT.correctOrientation(secondBranchPointsXs, secondBranchPointsYs);

            ForceCurveBranch firstBranchType = ForceCurveOrientation.LEFT.guessBranch(firstBranchPointsXs, firstBranchPointsYs);
            boolean firstApproach = ForceCurveBranch.APPROACH.equals(firstBranchType);

            Channel1DData approachBranch = firstApproach? new FlexibleFlatChannel1DData(firstBranchPointsXs,firstBranchPointsYs,xQuantity, yQuantity, SortedArrayOrder.DESCENDING) : 
                new FlexibleFlatChannel1DData(secondBranchPointsXs, secondBranchPointsYs, xQuantity, yQuantity, SortedArrayOrder.DESCENDING);
            Channel1DData withdrawBranch = firstApproach ? new FlexibleFlatChannel1DData(secondBranchPointsXs, secondBranchPointsYs, xQuantity, yQuantity, SortedArrayOrder.ASCENDING) 
                    : new FlexibleFlatChannel1DData(firstBranchPointsXs, firstBranchPointsYs, xQuantity, yQuantity, SortedArrayOrder.ASCENDING);

            return new ForceCurveChannelStorage(approachBranch, withdrawBranch);
        }
    }
}
