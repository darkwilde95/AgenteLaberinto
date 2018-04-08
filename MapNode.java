package unalcol.agents.UNfail;

/**
 *
 * @author darkwilde95
 */
public class MapNode{
    
    public Long[] children;
    public Space space;  //Pendiente por configurar
    public Long key;

    public MapNode(Long key, boolean[] perceptions) {  //Pendiente para percepciones
        this.key = key;
        this.children = new Long[4];
        int[] s = Space.decode(key);
       
        //Validar cuando no tenga algun hijo
        this.children[Direction.N] = (!perceptions[Direction.N]) ? Space.encode(s[0], s[1]+1) : null;  // up
        this.children[Direction.E] = (!perceptions[Direction.E]) ? Space.encode(s[0]+1, s[1]) : null;  // right
        this.children[Direction.S] = (!perceptions[Direction.S]) ? Space.encode(s[0], s[1]-1) : null;  // down
        this.children[Direction.W] = (!perceptions[Direction.W]) ? Space.encode(s[0]-1, s[1]) : null;  // left
        
    }    
}
