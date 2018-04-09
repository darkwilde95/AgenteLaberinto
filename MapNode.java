package unalcol.agents.UNfail;

/**
 *
 * @author darkwilde95
 */
public class MapNode{
    
    public long[] children;
    public Space space;  //Pendiente por configurar
    public long key;
    public boolean[] valid;

    public MapNode(long key, boolean[] perceptions){
        this.key = key;
        this.children = new long[4];
        this.valid = new boolean[4];
        int[] s = Space.decode(key);
        
        this.valid[Direction.N] = !perceptions[Direction.N];  // up
        this.valid[Direction.E] = !perceptions[Direction.E];  // right
        this.valid[Direction.S] = !perceptions[Direction.S];  // down
        this.valid[Direction.W] = !perceptions[Direction.W];  // left
        
        this.children[Direction.N] = Space.encode(s[0], s[1]+1);
        this.children[Direction.E] = Space.encode(s[0]+1, s[1]);
        this.children[Direction.S] = Space.encode(s[0], s[1]-1);
        this.children[Direction.W] = Space.encode(s[0]-1, s[1]);
    }    
}
