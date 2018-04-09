package unalcol.agents.UNfail;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 *
 * @author darkwilde95
 */
public class AStarSearch {
    
    public Stack<Long> path;
    public PriorityQueue<SearchNode> pq;
    public HashMap<Long, SearchNode> forSearch;

    public AStarSearch() {
        this.path = new Stack();
        this.pq = new PriorityQueue();
        this.forSearch = new HashMap();
    }
    
    public Stack<Long> search(long orig, long dest, HashMap<Long, MapNode> original){
        
        this.pq.clear();
        this.path.clear();
        this.forSearch.clear();
        
        int[] destPoint = Space.decode(dest);
        long childKey = 0L;
        SearchNode aux = null, auxChild = null;
        MapNode auxOriginal = null;

        //Ingreso el primer node de busqueda al grafo de busqueda
        double h = this.heuristic(orig, dest);
        this.forSearch.put(orig, new SearchNode(null, orig, h, 0));
        
        //Ingreso el mismo nodo a la cola
        this.pq.add(this.forSearch.get(orig));
        
        while(!this.pq.isEmpty()){
            
            aux = this.pq.poll();
            aux.wasVisited = true;
            
            if( aux.key == dest ){
                
                while(aux != null){
                    this.path.push(aux.key);
                    aux = this.forSearch.get(aux.parent);
                }
                return this.path;
                
            }else{
                for(int i = 0; i < 4; i++) {
                	auxOriginal = original.get(aux.key);
                    if( auxOriginal.valid[i] ){
                        childKey = auxOriginal.children[i];
                        if( !this.forSearch.containsKey(childKey) ){
                            h = this.heuristic(childKey, dest);
                            auxChild = new SearchNode(aux.key,childKey,h,aux.distance+1);
                            this.forSearch.put(childKey, auxChild);
                        }
                        
                        auxChild = this.forSearch.get(childKey);
                        if( !auxChild.wasVisited ){
                            this.pq.offer(auxChild);
                        }
                    } 
                }
            }
        }
        return null; //Validar algun posible error
    }
    
    public double heuristic(long cur, long dest) {
        int[] pc = Space.decode(cur);
        int[] pd = Space.decode(dest);
        return Math.pow(((double)pc[0]-(double)pd[0]), 2) + 
            Math.pow(((double)pc[1]-(double)pd[1]), 2);
    }
}
