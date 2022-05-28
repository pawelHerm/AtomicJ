package atomicJ.utilities;

import java.util.Collection;
import java.util.Map;

public class Validation
{
    public static <T> T requireInstanceOfParameterName(Object val, Class<T> requiredClass, String parameterName)
    {
        if(val == null)
        {
            throw new NullPointerException("The parameter '" + parameterName + "' cannot be null");
        }

        if(!requiredClass.isInstance(val))
        {
            throw new IllegalArgumentException("The class  of the parameter '"+parameterName + "' should be "+requiredClass.getCanonicalName());
        }

        T valueCast = requiredClass.cast(val);

        return valueCast;
    }

    public static <T> T requireNonNullParameterName(T val, String parameterName)
    {
        if(val == null)
        {
            throw new NullPointerException("The argument " + parameterName + " cannot be null");
        }

        return val;
    }

    /**
     * Checks that the specified collection reference is not {@code null} and, if so, whether the collection is not empty. This
     * method is designed primarily for doing parameter validation in methods
     * and constructors.
     * @param coll the object reference to check for nullity and emptiness
     * @param <T extends Collection<?>> the type of the reference
     * @return {@code coll} if not {@code null} and not empty
     * @throws NullPointerException if {@code coll} is {@code null}
     * * @throws IllegalArgumentException if {@code coll} is empty
     */
    public static <T extends Collection<?>> T requireNonNullAndNonEmpty(T coll) {
        if (coll == null)
        {
            throw new NullPointerException();
        }
        if(coll.isEmpty())
        {
            throw new IllegalArgumentException();
        }

        return coll;
    }


    /**
     * Checks that the specified collection reference is not {@code null} and, if so, whether the collection is not empty. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param coll     the collection reference to check for nullity and emptiness
     * @param messageNull detail message to be used in the event that a {@code
     *                NullPointerException} is thrown
     * @param messageEmpty detail message to be used in the event that a {@code
     *                IllegalArgumentException} is thrown
     * @param <T extends Collection<?>> the type of the reference
     * @return {@code coll} if not {@code null} and not empty
     * @throws NullPointerException if {@code coll} is {@code null}
     * @throws IllegalArgumentException if {@code coll} is empty
     */
    public static <T extends Collection<?>> T requireNonNullAndNonEmpty(T coll, String messageNull, String messageEmpty) 
    {
        if (coll == null)
        {
            throw new NullPointerException(messageNull);
        }
        if(coll.isEmpty())
        {
            throw new IllegalArgumentException(messageEmpty);
        }

        return coll;
    }


    /**
     * Checks that the specified collection reference is not {@code null} and, if so, whether the collection is not empty. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param coll     the collection reference to check for nullity and emptiness
     * @param parameterName the name of the parameter to be used in the error messages
     * @param <T extends Collection<?>> the type of the reference
     * @return {@code coll} if not {@code null} and not empty
     * @throws NullPointerException if {@code coll} is {@code null}
     * @throws IllegalArgumentException if {@code coll} is empty
     */
    public static <M extends Collection<?>> M requireNonNullAndNonEmptyParameterName(M coll, String parameterName) 
    {
        if (coll == null)
        {
            throw new NullPointerException("The argument " + parameterName + " cannot be null");
        }
        if(coll.isEmpty())
        {
            throw new NullPointerException("The argument " + parameterName + " cannot be empty");
        }

        return coll;
    }

    /**
     * Checks that the specified map reference is not {@code null} and, if so, whether the map is not empty. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param map     the map reference to check for nullity and emptiness
     * @param parameterName the name of the parameter to be used in the error messages
     * @param <M extends Map<?,?>> the type of the reference
     * @return {@code map} if not {@code null} and not empty
     * @throws NullPointerException if {@code map} is {@code null}
     * @throws IllegalArgumentException if {@code map} is empty
     */
    public static <M extends Map<?,?>> M requireNonNullAndNonEmptyParameterName(M map, String parameterName) 
    {
        if (map == null)
        {
            throw new NullPointerException("The argument " + parameterName + " cannot be null");
        }
        if(map.isEmpty())
        {
            throw new NullPointerException("The argument " + parameterName + " cannot be empty");
        }

        return map;
    }


    /**
     * Checks that the specified MultiMap reference is not {@code null} and, if so, whether the MultiMap is not empty. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param multiMap     the MultiMap reference to check for nullity and emptiness
     * @param parameterName the name of the parameter to be used in the error messages
     * @param <M extends MultiMap<?,?>> the type of the reference
     * @return {@code multiMap} if not {@code null} and not empty
     * @throws NullPointerException if {@code multiMap} is {@code null}
     * @throws IllegalArgumentException if {@code multiMap} is empty
     */
    public static <M extends MultiMap<?, ?>> M requireNonNullAndNonEmptyParameterName(M multiMap, String parameterName) 
    {
        if (multiMap == null)
        {
            throw new NullPointerException("The argument " + parameterName + " cannot be null");
        }
        if(multiMap.isEmpty())
        {
            throw new NullPointerException("The argument " + parameterName + " cannot be empty");
        }

        return multiMap;
    }


    /**
     * Checks that the specified double[] array reference is not {@code null} and, if so, whether the array is not empty. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param array     the array reference to check for nullity and emptiness
     * @param parameterName the name of the parameter to be used in the error messages
     * @return {@code array} if not {@code null} and not of length greater than 0
     * @throws NullPointerException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static double[] requireNonNullAndNonEmptyParameterName(double[] array, String parameterName) 
    {
        if (array == null)
        {
            throw new NullPointerException("The array " + parameterName + " cannot be null");
        }
        if(array.length == 0)
        {
            throw new NullPointerException("The array " + parameterName + " cannot be of length 0");
        }

        return array;
    }


    /**
     * Checks that the specified double[] array reference is not {@code null} and, if so, whether the array is not empty. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param array     the array reference to check for nullity and emptiness
     * @param parameterName the name of the parameter to be used in the error messages
     * @return {@code array} if not {@code null} and not of length greater than 0
     * @throws NullPointerException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static int[] requireNonNullAndNonEmptyParameterName(int[] array, String parameterName) 
    {
        if (array == null)
        {
            throw new NullPointerException("The array " + parameterName + " cannot be null");
        }
        if(array.length == 0)
        {
            throw new NullPointerException("The array " + parameterName + " cannot be of length 0");
        }

        return array;
    }

    /**
     * Checks that the specified double[] array reference is not {@code null} and, if so, whether the array is not empty. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param array     the array reference to check for nullity and emptiness
     * @param parameterName the name of the parameter to be used in the error messages
     * @return {@code array} if not {@code null} and not of length greater than 0
     * @throws NullPointerException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static <T> T[] requireNonNullAndNonEmptyParameterName(T[] array, String parameterName) 
    {
        if (array == null)
        {
            throw new NullPointerException("The array " + parameterName + " cannot be null");
        }
        if(array.length == 0)
        {
            throw new NullPointerException("The array " + parameterName + " cannot be of length 0");
        }

        return array;
    }


    /**
     * Checks that the specified collection reference is not {@code null} and, if so, whether the collection is not empty. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param coll     the collection reference to check for nullity and emptiness
     * @param messageNull detail message to be used in the event that a {@code
     *                NullPointerException} is thrown
     * @param messageEmpty detail message to be used in the event that a {@code
     *                IllegalArgumentException} is thrown
     * @param <T extends Collection<?>> the type of the reference
     * @return {@code coll} if not {@code null} and not empty
     * @throws NullPointerException if {@code coll} is {@code null}
     * @throws IllegalArgumentException if {@code coll} is empty
     */
    public static double[] requireNonNullAndNonEmpty(double[] array, String messageNull, String messageEmpty) 
    {
        if (array == null)
        {
            throw new NullPointerException(messageNull);
        }
        if(array.length == 0)
        {
            throw new IllegalArgumentException(messageEmpty);
        }

        return array;
    }


    public static void requireTwoArraysNonNullAndOfEqualLengthParameterName(Object[] arrayA, Object[] arrayB, String parameterAName, String parameterBName) 
    {
        if (arrayA == null)
        {
            throw new NullPointerException("The array " + parameterAName + " cannot be null");
        }
        if (arrayB == null)
        {
            throw new NullPointerException("The array " + parameterBName + " cannot be null");
        }
        if(arrayA.length != arrayB.length)
        {
            throw new IllegalArgumentException("The arrays " + parameterAName + " and " + parameterBName +" should be of the same length");
        }
    }

    public static void requireTwoArraysNonNullAndOfEqualLengthParameterName(double[] arrayA, double[] arrayB, String parameterAName, String parameterBName) 
    {
        if (arrayA == null)
        {
            throw new NullPointerException("The array " + parameterAName + " cannot be null");
        }
        if (arrayB == null)
        {
            throw new NullPointerException("The array " + parameterBName + " cannot be null");
        }
        if(arrayA.length != arrayB.length)
        {
            throw new IllegalArgumentException("The arrays " + parameterAName + " and " + parameterBName +" should be of the same length");
        }
    }

    public static void requireTwoArraysNonNullAndOfEqualLengthParameterName(int[] arrayA, int[] arrayB, String parameterAName, String parameterBName) 
    {
        if (arrayA == null)
        {
            throw new NullPointerException("The array " + parameterAName + " cannot be null");
        }
        if (arrayB == null)
        {
            throw new NullPointerException("The array " + parameterBName + " cannot be null");
        }
        if(arrayA.length != arrayB.length)
        {
            throw new IllegalArgumentException("The arrays " + parameterAName + " and " + parameterBName +" should be of the same length");
        }
    }

    /**
     * Checks that the specified reference is not {@code null}. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters.
     * @param obj     the collection reference to check for nullity 
     * @param messageNull detail message to be used in the event that a {@code
     *                NullPointerException} is thrown
     * @param <T> the type of the reference
     * @return {@code coll} if not {@code null} and not empty
     * @throws NullPointerException if {@code coll} is {@code null}
     */
    public static <T> T requireNonNull(T obj, String messageNull) 
    {
        if (obj == null)
        {
            throw new NullPointerException(messageNull);
        }

        return obj;
    }

    public static double requireNotNaNParameterName(double val, String parameterName)
    {
        if(Double.isNaN(val))
        {
            throw new IllegalArgumentException("NaN value of the parameter '"+parameterName + "' encountered");
        }

        return val;
    }

    public static double requireNotInfiniteParameterName(double val, String parameterName)
    {
        if(Double.isInfinite(val))
        {
            throw new IllegalArgumentException("Infinite in magnitude value of the parameter '"+parameterName + "' encountered");
        }

        return val;
    }

    public static double requireNonZeroParameterName(double val, String parameterName)
    {
        if(val == 0)
        {
            throw new IllegalArgumentException("Equal to zero value of the parameter '"+parameterName + "' encountered");
        }

        return val;
    }


    public static int requireNonNegative(int val)
    {
        return requireNonNegative(val, "Negative integer value encountered " + val);
    }

    public static int requireNonNegativeParameterName(int val, String parameterName)
    {
        return requireNonNegative(val, "Negative integer value " + val + " of the parameter '" + parameterName + "' encountered");
    }

    public static int requireNonNegative(int val, String message)
    {
        if(val < 0)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }


    public static double requireNonNegative(double val)
    {
        return requireNonNegative(val, "Negative integer value encountered " + val);
    }

    public static double requireNonNegativeParameterName(double val, String parameterName)
    {
        return requireNonNegative(val, "Negative integer value " + val + " of the parameter '" + parameterName + "' encountered");
    }

    public static double requireNonNegative(double val, String message)
    {
        if(val < 0)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static int requireValueSmallerThan(int val, int upperBound)
    {
        return requireValueSmallerThan(val, upperBound, "An illegal parameter value " + val + " encountered. A value smaller than " + upperBound + " expected.");
    }

    public static int requireValueSmallerThanParameterName(int val, int upperBound, String parameterName)
    {
        return requireValueSmallerThan(val, upperBound, "An illegal value " + val + " encountered of the '" + parameterName + "'. A value smaller than " + upperBound + " expected.");
    }

    public static int requireValueSmallerThan(int val, int upperBound, String message)
    {
        if(val >= upperBound)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static double requireValueSmallerThan(double val, double upperBound)
    {
        return requireValueSmallerThan(val, upperBound, "An illegal parameter value " + val + " encountered. A value smaller than " + upperBound + " expected.");
    }

    public static double requireValueSmallerThanParameterName(double val, double upperBound, String parameterName)
    {
        return requireValueSmallerThan(val, upperBound, "An illegal value " + val + " encountered of the '" + parameterName + "'. A value smaller than " + upperBound + " expected.");
    }

    public static double requireValueSmallerThan(double val, double upperBound, String message)
    {
        if(val >= upperBound)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static int requireValueSmallerOrEqualTo(int val, int upperBound)
    {
        return requireValueSmallerOrEqualTo(val, upperBound, "An illegal parameter value " + val + " encountered. A value smaller or equal to " + upperBound + " expected.");
    }

    public static int requireValueSmallerOrEqualToParameterName(int val, int upperBound, String parameterName)
    {
        return requireValueSmallerOrEqualTo(val, upperBound, "An illegal parameter value " + val + " encountered. A value smaller or equal to " + upperBound + " expected.");
    }

    public static int requireValueSmallerOrEqualTo(int val, int upperBound, String message)
    {
        if(val > upperBound)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static double requireValueSmallerOrEqualTo(double val, int upperBound)
    {
        return requireValueSmallerOrEqualTo(val, upperBound, "An illegal parameter value " + val + " encountered. A value smaller or equal to " + upperBound + " expected.");
    }

    public static double requireValueSmallerOrEqualToParameterName(double val, double upperBound, String parameterName)
    {
        return requireValueSmallerOrEqualTo(val, upperBound, "An illegal parameter value " + val + " encountered. A value smaller or equal to " + upperBound + " expected.");
    }

    public static double requireValueSmallerOrEqualTo(double val, double upperBound, String message)
    {
        if(val > upperBound)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static int requireValueGreaterThan(int val, int lowerBound)
    {
        return requireValueGreaterThan(val, lowerBound, "An illegal parameter value " + val + " encountered. A value greater than " + lowerBound + " expected.");
    }

    public static int requireValueGreaterThanParameterName(int val, int lowerBound, String parameterName)
    {
        return requireValueGreaterOrEqualTo(val, lowerBound, "An illegal value " + val + "of the parameter '" + parameterName +"' encountered. A value greater than " + lowerBound + " expected.");
    }

    public static int requireValueGreaterThan(int val, int lowerBound, String message)
    {
        if(val <= lowerBound)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static double requireValueGreaterThan(double val, double lowerBound)
    {
        return requireValueGreaterThan(val, lowerBound, "An illegal parameter value " + val + " encountered. A value greater than " + lowerBound + " expected.");
    }

    public static double requireValueGreaterThanParameterName(double val, double lowerBound, String parameterName)
    {
        return requireValueGreaterOrEqualTo(val, lowerBound, "An illegal value " + val + "of the parameter '" + parameterName +"' encountered. A value greater than " + lowerBound + " expected.");
    }

    public static double requireValueGreaterThan(double val, double lowerBound, String message)
    {
        if(val <= lowerBound)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static int requireValueGreaterOrEqualTo(int val, int lowerBound)
    {
        return requireValueGreaterOrEqualTo(val, lowerBound, "An illegal parameter value " + val + " encountered. A value greater or equal to " + lowerBound + " expected.");
    }

    public static int requireValueGreaterOrEqualToParameterName(int val, int lowerBound, String parameterName)
    {
        return requireValueGreaterOrEqualTo(val, lowerBound, "An illegal value " + val + "of the parameter '" + parameterName +"' encountered. A value greater or equal to " + lowerBound + " expected.");
    }  

    public static int requireValueGreaterOrEqualTo(int val, int lowerBound, String message)
    {
        if(val < lowerBound)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static double requireValueGreaterOrEqualTo(double val, double lowerBound)
    {
        return requireValueGreaterOrEqualTo(val, lowerBound, "An illegal parameter value " + val + " encountered. A value greater or equal to " + lowerBound + " expected.");
    }

    public static double requireValueGreaterOrEqualToParameterName(double val, double lowerBound, String parameterName)
    {
        return requireValueGreaterOrEqualTo(val, lowerBound, "An illegal value " + val + "of the parameter '" + parameterName +"' encountered. A value greater or equal to " + lowerBound + " expected.");
    }    

    public static double requireValueGreaterOrEqualTo(double val, double lowerBound, String message)
    {
        if(val < lowerBound)
        {
            throw new IllegalArgumentException(message);
        }

        return val;
    }

    public static int requireValueEqualToOrBetweenBounds(int val, int lowerBound, int upperBound, String parameterName)
    {
        if(val < lowerBound)
        {
            throw new IllegalArgumentException("An illegal parameter '" + parameterName + "' value " + val + " encountered. A value greater or equal to " + lowerBound + " expected.");
        }

        if(val > upperBound)
        {
            throw new IllegalArgumentException("An illegal parameter '" + parameterName + "' value " + val + " encountered. A value smaller or equal to " + upperBound + " expected.");
        }

        return val;
    }

    public static int requireValueEqualToOrBetweenButExcludingBounds(int val, int lowerBound, int upperBound, String parameterName)
    {
        if(val <= lowerBound)
        {
            throw new IllegalArgumentException("An illegal parameter '" + parameterName + "' value " + val + " encountered. A value greater than" + lowerBound + " expected.");
        }

        if(val >= upperBound)
        {
            throw new IllegalArgumentException("An illegal parameter '" + parameterName + "' value " + val + " encountered. A value smaller than " + upperBound + " expected.");
        }

        return val;
    } 

    public static double requireValueEqualToOrBetweenBounds(double val, double lowerBound, double upperBound, String parameterName)
    {
        if(val < lowerBound)
        {
            throw new IllegalArgumentException("An illegal parameter '" + parameterName + "' value " + val + " encountered. A value greater or equal to " + lowerBound + " expected.");
        }

        if(val > upperBound)
        {
            throw new IllegalArgumentException("An illegal parameter '" + parameterName + "' value " + val + " encountered. A value smaller or equal to " + upperBound + " expected.");
        }

        return val;
    }

    public static double requireValueEqualToOrBetweenButExcludingBounds(double val, double lowerBound, double upperBound, String parameterName)
    {
        if(val <= lowerBound)
        {
            throw new IllegalArgumentException("An illegal parameter '" + parameterName + "' value " + val + " encountered. A value greater than" + lowerBound + " expected.");
        }

        if(val >= upperBound)
        {
            throw new IllegalArgumentException("An illegal parameter '" + parameterName + "' value " + val + " encountered. A value smaller than " + upperBound + " expected.");
        }

        return val;
    }

    public static void requireFirstValueSmallerThanSecond(double firstValue, double secondValue, String firstParameterName, String secondParameterName)
    {
        if(firstValue >= secondValue)
        {
            throw new IllegalArgumentException("The value of '" + firstParameterName + "', which is " + Double.toString(firstValue) + " should be smaller than the value " + Double.toString(secondValue) + " of the parameter '" + secondParameterName +"'");
        }
    }   

    public static void requireFirstValueSmallerOrEqualToSecond(double firstValue, double secondValue, String firstParameterName, String secondParameterName)
    {
        if(firstValue > secondValue)
        {
            throw new IllegalArgumentException("The value of '" + firstParameterName + "', which is " + Double.toString(firstValue) + " should be smaller or equal to the value " + Double.toString(secondValue) + " of the parameter '" + secondParameterName +"'");
        }
    }   
}
