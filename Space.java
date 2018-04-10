
package unalcol.agents.UNfail;

/**
 *
 * @author darkwilde95
 */
public class Space {
    
    public static final long OP_X = -4294967296L;
    public static final long OP_Y = 4294967295L;
        
    public static int[] decode(Long key){
        return new int[] { (int)((key & OP_X) >>> 32), (int)(key & OP_Y) };
    }
    
    public static long encode(int x, int y){
        return ((x | 0L) << 32) | (y & OP_Y);
    }    
}
