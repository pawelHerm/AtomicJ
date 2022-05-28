package atomicJ.utilities;

import java.util.Objects;

public class GeneralObjectUtilities 
{
    /**This method is the same as java.util.Objects.requireNonNullElse availale in Java 9 or later
     * Returns the first argument if it is non-{@code null} and
     * otherwise returns the non-{@code null} second argument.
     *
     * @param obj an object
     * @param defaultObj a non-{@code null} object to return if the first argument
     *                   is {@code null}
     * @param <T> the type of the reference
     * @return the first argument if it is non-{@code null} and
     *        otherwise the second argument if it is non-{@code null}
     * @throws NullPointerException if both {@code obj} is null and
     *        {@code defaultObj} is {@code null}
     */
    public static <T> T requireNonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : Objects.requireNonNull(defaultObj, "defaultObj");}
}
