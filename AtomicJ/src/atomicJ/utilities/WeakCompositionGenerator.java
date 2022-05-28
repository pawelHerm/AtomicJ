package atomicJ.utilities;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class WeakCompositionGenerator 
{
    public static int[][] generateCompositions(int[][] allRestrictions, int n, int k)
    {
        if((k <= 0 || n < 0 ||allRestrictions.length == 0))
        {
            return new int[][] {};
        }

        if(k == 1)
        {
            return ArrayUtilities.contains(allRestrictions[0], n) ? new int[][] {{n}} : new int[][] {};
        }

        boolean[][] restrictionsValues = buildRestrictionValues(allRestrictions, n, k);     

        if(k == 2)
        {
            List<int[]> compositions = new ArrayList<>();
            for(int i = 0; i<= n; i++)
            {
                if(restrictionsValues[0][i] && restrictionsValues[1][n - i])
                {
                    compositions.add(new int[] {i, n - i});
                }
            }

            return compositions.toArray(new int[][] {});
        }


        List<Queue<int[]>> queues = new ArrayList<>();

        for(int i = 0; i < n+1; i++)
        {
            queues.add(new ArrayDeque<int[]>());
        }

        int carryTill = -1; //following this main round, zeroes are allowed to be placed int n-tuples

        for(int i = k - 2; i > 0; i--)
        {
            carryTill = (carryTill == -1 && !restrictionsValues[i][0]) ? i - 1 : carryTill;               
        }

        TIntList[][] linkEdges = buildLinkedListOfEdges(n, k);

        fillLinkedListOfEdges(linkEdges, restrictionsValues, carryTill, n, k);


        //Base step : Concatenate the first position

        for(int i = 0; i<= n; i++)
        {            
            if(restrictionsValues[0][i])
            {
                int[] element = new int[k];
                element[0] = i;
                queues.get(i).add(element);
            }
        }

        //Induction step : perform n - 2 rounds of generation
        performInductionStep(linkEdges, restrictionsValues, queues, n, k);


        return queues.get(n).toArray(new int[][] {});
    }

    private static void performInductionStep(TIntList[][] edges, boolean[][] restrictionsValues, List<Queue<int[]>> queues, int s, int k)
    {
        for(int round = 1; round <= k - 2; round++)
        {
            TIntList[] currentEdgesArray = edges[round - 1];

            for(int i = s - 1; i>= 0; i--)
            {
                TIntList currentEdges = currentEdgesArray[i];
                int currentEdgesCount = currentEdges.size();

                //check if queues.get(i)
                if(round == k - 2)
                {
                    boolean reachable = false;
                    for(int j = 0; j < currentEdgesCount; j++)
                    {
                        reachable = restrictionsValues[k - 1][s - currentEdges.get(j)];
                        if(reachable)
                        {
                            break;
                        }
                    }
                    if(!reachable)
                    {
                        queues.get(i).clear();
                    }
                }

                int countDown = queues.get(i).size();
                for(int elem = 0; elem < countDown; elem ++)
                {
                    int[] element = queues.get(i).remove();

                    if(round < k - 2)
                    {
                        for(int j = 0; j < currentEdgesCount; j++)
                        {
                            int value = currentEdges.get(j);
                            int[] copy = Arrays.copyOf(element, element.length);
                            copy[round] = value - i;
                            queues.get(value).add(copy);
                        }
                    }
                    else
                    {
                        for(int j = 0; j < currentEdgesCount; j++)
                        {
                            int value = currentEdges.get(j);
                            if(restrictionsValues[k - 1][s - value])
                            {
                                int[] copy = Arrays.copyOf(element, element.length);
                                copy[round] = value - i;
                                copy[k - 1] = s - value;
                                queues.get(s).add(copy);
                            }
                        }
                    }
                }
            }
        }


    }

    private static boolean[][] buildRestrictionValues(int[][] allRestrictions, int s, int k)
    {
        boolean[][] restrictionValues = new boolean[k][s + 1];

        for(int i = 0; i<k; i++)
        {
            int[] restrictions = allRestrictions[i];

            for(int j = 0; j < restrictions.length; j++)
            {
                if(restrictions[j] >= 0 && restrictions[j] <= s)
                {
                    restrictionValues[i][restrictions[j]] = true;             
                }
            }
        }

        return restrictionValues;
    }

    private static TIntList[][] buildLinkedListOfEdges(int s, int k)
    {
        TIntList[][] linkEdges = new TIntList[k - 2][];
        for(int i = 0; i<k - 2; i++)
        {
            TIntList[] linkEdgesRow = new TIntList[s + 1];
            for(int j = 0; j < s + 1; j++)
            {
                linkEdgesRow[j] = new TIntArrayList();
            }

            linkEdges[i] = linkEdgesRow;
        }

        return linkEdges;
    }

    private static void fillLinkedListOfEdges(TIntList[][] linkEdges, boolean[][] restrictionArrays, int carryTill, int s, int k)
    {
        for(int m = 1; m < k - 1; m++)
        {
            boolean[] restrictionArray = restrictionArrays[m];
            for(int i = 0; i < s; i++)
            {
                for(int j = s - i; j>= 0; j--)
                {
                    if(restrictionArray[j] && (carryTill <= m - 1 || i + j != s))
                    {
                        linkEdges[m - 1][i].add(i + j);
                    }
                }
            }
        }
    }
}
