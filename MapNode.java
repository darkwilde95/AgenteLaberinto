package UNfail;

/**
 *
 * @author darkwilde95
 */
public class MapNode{
    
    public Long[] children;
    public Space space;  //Pendiente por configurar
    public Long key;

    public MapNode(Long key, double w) {  //Pendiente para percepciones
        this.key = key;
        this.children = new Long[4];
        int[] s = Space.decode(key);
        
        //Validar cuando no tenga algun hijo
        this.children[0] = Space.encode(s[0], s[1]+1);  // up
        this.children[1] = Space.encode(s[0], s[1]-1);  // down
        this.children[2] = Space.encode(s[0]-1, s[1]);  // left
        this.children[3] = Space.encode(s[0]+1, s[1]);  // right
    }
}
