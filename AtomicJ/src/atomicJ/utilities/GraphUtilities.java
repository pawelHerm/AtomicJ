package atomicJ.utilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class GraphUtilities
{
    public static <E> Set<Set<E>> findConnectedComponents(Collection<? extends E> vertices, Collection<? extends Pair<E>> edges)
    {
        Set<Set<E>> components = new LinkedHashSet<>();

        for(E vertex : vertices)
        {
            Set<E> partitionElement = new HashSet<>();
            partitionElement.add(vertex);

            components.add(partitionElement);
        }

        for(Pair<E> pair : edges)
        {
            Set<E> partitionFirstElement = findSetContainingElements(components, pair.getFirst());
            Set<E> partitionSecondElement = findSetContainingElements(components, pair.getSecond());

            if(partitionFirstElement != partitionSecondElement)
            {
                partitionFirstElement.addAll(partitionSecondElement);
                components.remove(partitionSecondElement);
            }          
        }

        return components;
    }

    private static <E> Set<E> findSetContainingElements(Collection<? extends Set<E>> partition, E vertex)
    {
        for(Set<E> el : partition)
        {
            if(el.contains(vertex))
            {
                return el;
            }
        }

        throw new IllegalArgumentException("Partition does not contain the vertex");
    }
}
