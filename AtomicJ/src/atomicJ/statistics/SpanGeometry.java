package atomicJ.statistics;

import atomicJ.data.IndexRange;

public enum SpanGeometry
{
    NEAREST_NEIGHBOUR("Nearest") 
    {
        @Override
        public IndexRange getNextRange(double[][] data, int n, int i, int iLeftOld, int iRightOld, int windowWidth)
        {
            //updates fitting range

            double x = data[i][0];
            int nextRight = iRightOld + 1;
            if (nextRight < n && Math.abs(data[nextRight][0] - x) <=  Math.abs(x - data[iLeftOld][0])) //it must be <=, not <, because otherwise the window would stack if there are two point with the same x , one the current iright and the next to its right
            {
                iLeftOld = iLeftOld + 1;
                iRightOld = nextRight;
            }

            return new IndexRange(iLeftOld, iRightOld);
        }

        @Override
        public IndexRange getRange(double[] data, int n, int i, int windowWidth)
        {            
            int iLeftNew = Math.max(0, i - windowWidth/2);
            int iRightNew = Math.max(0, i - 1);
            return new IndexRange(iLeftNew, iRightNew);
        }

        @Override
        public IndexRange getNextRange(double[] data, int n, int i, int iLeftOld, int iRightOld, int windowWidth) 
        {
            int nextRight = iRightOld + 1;
            if (nextRight < n && (nextRight - i) < (i - iLeftOld)) 
            {
                iLeftOld = iLeftOld + 1;
                iRightOld = nextRight;
            }
            return new IndexRange(iLeftOld, iRightOld);
        }

        @Override
        public IndexRange getRange(double[][] data, int n, int i, int windowWidth)
        {            
            if(n <= windowWidth)
            {
                return new IndexRange(0, n - 1);
            }

            int currentLength = 1;
            int iLeft = i;
            int iRight = i;

            double x = data[i][0];

            while(currentLength < windowWidth)
            {
                int nextRight = iRight + 1;
                int nextLeft = iLeft - 1;

                double distanceLeft = nextRight < n ? Math.abs(data[nextRight][0] - x) : Double.POSITIVE_INFINITY;
                double distanceRight = nextLeft >= 0 ? Math.abs(data[nextLeft][0] - x) : Double.POSITIVE_INFINITY;

                if(distanceLeft < distanceRight)
                {
                    iLeft = nextLeft;
                }
                else
                {
                    iRight = nextRight;
                }

                currentLength++;
            }

            IndexRange indexRange = new IndexRange(iLeft, iRight);
            return indexRange;
        }

        @Override
        public IndexRange getKernelMinAndLength(int windowWidth)
        {
            return new IndexRange(-windowWidth/2, windowWidth);
        }
    },

    LEFT("Left") 
    {
        @Override
        public IndexRange getNextRange(double[][] data, int n, int i, int iLeftOld, int iRightOld, int windowWidth) 
        {
            return getRange(data, n, i, windowWidth);
        }

        @Override
        public IndexRange getRange(double[][] data, int n, int i, int windowWidth) 
        {
            int iLeftNew = Math.max(0, i - windowWidth/2);
            return new IndexRange(iLeftNew, i);
        }

        @Override
        public IndexRange getNextRange(double[] data, int n, int i, int iLeftOld, int iRightOld, int windowWidth) 
        {       
            return getRange(data, n, i, windowWidth);
        }

        @Override
        public IndexRange getRange(double[] data, int n, int i, int windowWidth)
        {
            int iLeftNew = Math.max(0, i - windowWidth/2);
            return new IndexRange(iLeftNew, i);
        }

        @Override
        public IndexRange getKernelMinAndLength(int windowWidth)
        {
            return new IndexRange(-windowWidth/2, windowWidth/2 + 1);
        }
    }, 

    LEFT_OPEN("Left open") 
    {
        @Override
        public IndexRange getNextRange(double[][] data, int n, int i, int ileftOld, int irightOld, int windowWidth) 
        {
            return getRange(data, n, i, windowWidth);
        }

        @Override
        public IndexRange getRange(double[][] data, int n, int i, int windowWidth) 
        {
            int iLeftNew = Math.max(0, i - windowWidth/2);
            int iRightNew = Math.max(0, i - 1);
            return new IndexRange(iLeftNew, iRightNew);
        }

        @Override
        public IndexRange getNextRange(double[] data, int n, int i, int ileftOld, int irightOld, int windowWidth) 
        {       
            return getRange(data, n, i, windowWidth);
        }

        @Override
        public IndexRange getRange(double[] data, int n, int i, int windowWidth)
        {
            int iLeftNew = Math.max(0, i - windowWidth/2);
            int iRightNew = Math.max(0, i - 1);
            return new IndexRange(iLeftNew, iRightNew);
        }

        @Override
        public IndexRange getKernelMinAndLength(int windowWidth)
        {
            return new IndexRange(-windowWidth/2, windowWidth/2);
        }
    },

    RIGHT("Right") 
    {
        @Override
        public IndexRange getNextRange(double[][] data, int n, int i, int ileftOld, int irightOld, int windowWidth) 
        {
            return getRange(data, n, i, windowWidth);
        }

        @Override
        public IndexRange getRange(double[][] data, int n, int i, int windowWidth) 
        {
            int iRightNew = Math.min(i + windowWidth/2, n - 1);

            return new IndexRange(i, iRightNew);
        }

        @Override
        public IndexRange getNextRange(double[] data, int n, int i, int ileftOld, int irightOld, int windowWidth) 
        {
            return getRange(data, n, i, windowWidth);
        }

        @Override
        public IndexRange getRange(double[] data, int n, int i, int windowWidth)
        {
            int iRightNew = Math.min(i + windowWidth/2, n - 1);

            return new IndexRange(i, iRightNew);
        }

        @Override
        public IndexRange getKernelMinAndLength(int bandwith)
        {
            return new IndexRange(0, bandwith/2  + 1);
        }
    },

    RIGHT_OPEN("Right open") 
    {
        @Override
        public IndexRange getNextRange(double[][] data, int n, int i, int ileftOld, int irightOld, int windowWidth) 
        {
            return getRange(data, n, i, windowWidth);
        }

        @Override
        public IndexRange getRange(double[][] data, int n, int i, int windowWidth) 
        {
            int iLeftNew = Math.min(i + 1, n - 1);
            int iRightNew = Math.min(i + windowWidth/2, n - 1);

            return new IndexRange(iLeftNew, iRightNew);
        }

        @Override
        public IndexRange getNextRange(double[] data, int n, int i, int iLeftOld, int iRightOld, int windowWidth) 
        {
            return getRange(data, n, i, windowWidth);
        }

        @Override
        public IndexRange getRange(double[] data, int n, int i, int windowWidth)
        {
            int iLeftNew = Math.min(i + 1, n - 1);
            int iRightNew = Math.min(i + windowWidth/2, n - 1);

            return new IndexRange(iLeftNew, iRightNew);
        }

        @Override
        public IndexRange getKernelMinAndLength(int bandwith)
        {
            return new IndexRange(1, bandwith/2 + 1);
        }
    };

    private final String label;

    SpanGeometry(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }

    public abstract IndexRange getRange(double[] data, int n, int i, int windowWidth);
    public abstract IndexRange getRange(double[][] data, int n, int i, int windowWidth);

    public abstract IndexRange getNextRange(double[] data, int n, int i, int iLeftOld, int iRightOld, int windowWidth);
    public abstract IndexRange getNextRange(double[][] data, int n, int i, int iLeftOld, int iRightOld, int windowWidth);

    public abstract IndexRange getKernelMinAndLength(int windowWidth);

    public static SpanGeometry getValue(String identifier)
    {
        return getValue(identifier, null);
    }

    public static SpanGeometry getValue(String identifier, SpanGeometry fallBackValue)
    {
        SpanGeometry estimator = fallBackValue;

        if(identifier != null)
        {
            for(SpanGeometry est : SpanGeometry.values())
            {
                String estIdentifier = est.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    estimator = est;
                    break;
                }
            }
        }

        return estimator;
    }

    public static boolean canBeParsed(String identifier)
    {
        boolean canBeParsed = false;

        if(identifier != null)
        {
            for(SpanGeometry est : SpanGeometry.values())
            {
                String estIdentifier = est.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    canBeParsed = true;
                    break;
                }
            }
        }

        return canBeParsed;
    }

    public String getIdentifier()
    {
        return name();
    }
}