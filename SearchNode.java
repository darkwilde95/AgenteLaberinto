package unalcol.agents.examples.labyrinth.multeseo.eater.sis20181.UNfail;

/**
 *
 * @author darkwilde95
 */
public class SearchNode implements Comparable<SearchNode>{
    
    public double heuristic;
    public long distance;
    public double w;
    public boolean wasVisited;
    public long key;
    public Long parent;

    public SearchNode(Long parent, long key, double heuristic, long distance){
        this.parent = parent;
        this.key = key;
        this.wasVisited = false;
        this.heuristic = heuristic;
        this.distance = distance;
        this.w = heuristic + distance;
    }

    @Override
    public int compareTo(SearchNode o) {
        return (this.w < o.w) ? -1 :
               (this.w > o.w) ? 1 : 0;
    }
    
}
