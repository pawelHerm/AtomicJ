package atomicJ.utilities;

public class NumberUtilities
{

    //http://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
    // ignores the higher 16 bits
    public static float halfFloatToFloat(int hbits)
    {
        int mant = hbits & 0x03ff;            // 10 bits mantissa
        int exp =  hbits & 0x7c00;            // 5 bits exponent
        if(exp == 0x7c00)                   // NaN/Inf //exponent sk³ada siê z samych 5 jedynek
            exp = 0x3fc00;                    // -> NaN/Inf //zmieniamy sobie na specjalny exponent w float 32, sk³adaj¹cy sie z samych 8 jedynaek
        else if(exp != 0 )                   // normalized value
        {
            exp += 0x1c000;                   // exp - 15 + 127
            if( mant == 0 && exp > 0x1c400 )  // smooth transition
                return Float.intBitsToFloat(( hbits & 0x8000 ) << 16
                        | exp << 13 | 0x3ff );
        }
        else if( mant != 0 )                  // && exp==0 -> subnormal
        {
            exp = 0x1c400;                    // make it normal
            do {
                mant <<= 1;                   // mantissa * 2
                exp -= 0x400;                 // decrease exp by 1
            } while( ( mant & 0x400 ) == 0 ); // while not normal
            mant &= 0x3ff;                    // discard subnormal bit
        }                                     // else +/-0 -> +/-0
        return Float.intBitsToFloat(          // combine all parts
                ( hbits & 0x8000 ) << 16          // sign  << ( 31 - 15 )
                | ( exp | mant ) << 13 );         // value << ( 23 - 10 )
    }

    public static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }
}
