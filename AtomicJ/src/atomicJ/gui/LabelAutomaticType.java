package atomicJ.gui;

import atomicJ.gui.annotations.AbstractCustomizableAnnotation;
import atomicJ.utilities.RomanNumeralConverter;

public enum LabelAutomaticType
{
    LETTER_UPPER_CASE("A, B, C ...") 
    {
        @Override
        public String getLabel(AbstractCustomizableAnnotation annotation) 
        {
            return Character.toString((char) (annotation.getKey() + 64));
        }
    }, 
    LETTER_LOWER_CASE("a, b, c ...") {
        @Override
        public String getLabel(AbstractCustomizableAnnotation annotation) 
        {
            return Character.toString((char) (annotation.getKey() + 96));
        }
    }, 
    ROMAN_UPPER_CASE("I, II, III ...") 
    {
        @Override
        public String getLabel(AbstractCustomizableAnnotation annotation) {
            return RomanNumeralConverter.convertToRoman(annotation.getKey());
        }
    },
    ROMAN_LOWER_CASE("i, ii, iii ....") {
        @Override
        public String getLabel(AbstractCustomizableAnnotation annotation) 
        {
            String label = RomanNumeralConverter.convertToRoman(annotation.getKey()).toLowerCase();
            return label;
        }
    }, 
    INTEGERS("1, 2, 3, ....") 
    {
        @Override
        public String getLabel(AbstractCustomizableAnnotation annotation) 
        {
            String label = annotation.getKey().toString();
            return label;
        }
    };

    private final String prettyName;

    private LabelAutomaticType(String prettyName)
    {
        this.prettyName = prettyName;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public abstract String getLabel(AbstractCustomizableAnnotation annotation);
}