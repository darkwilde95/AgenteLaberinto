package unalcol.agents.UNfail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.text.AbstractDocument.LeafElement;

import unalcol.agents.*;


public class UNfailAgentProgram implements AgentProgram {
		
	private int direction, currentEnergy, lastEnergy;
	private long current, next; 
	private AStarSearch search;
	private LinkedList<Action> actions;
	private Stack<Long> toExplore; 
	private HashSet<Long> foodSpace;
	private HashMap<Long, MapNode> map;
	
	public UNfailAgentProgram() {
		this.direction = 0;
		this.lastEnergy = 20;
		this.currentEnergy = 0;
		this.map = new HashMap();
		this.next = this.current;
		this.toExplore = new Stack();
		this.foodSpace = new HashSet();
		this.actions = new LinkedList();
		this.search = new AStarSearch();
		this.current = Space.encode(0, 0);		
	}
	
	
	private boolean[] absolutePerceptions(boolean[] relative){
		boolean[] absolute = new boolean[4];
		
		switch(this.direction){
			case Direction.N:
				absolute = relative;
			break;
			case Direction.S:
				absolute[Direction.N] = relative[Direction.S];
				absolute[Direction.S] = relative[Direction.N];
				absolute[Direction.W] = relative[Direction.E];
				absolute[Direction.E] = relative[Direction.W];
			break;
			case Direction.W:
				absolute[Direction.N] = relative[Direction.E];
				absolute[Direction.S] = relative[Direction.W];
				absolute[Direction.W] = relative[Direction.N];
				absolute[Direction.E] = relative[Direction.S];
			break;
			case Direction.E:
				absolute[Direction.N] = relative[Direction.W];
				absolute[Direction.S] = relative[Direction.E];
				absolute[Direction.W] = relative[Direction.S];
				absolute[Direction.E] = relative[Direction.N];				
			break;
		}
		
		return absolute;
		
	}
	

	private void drawMap(Percept p){
		boolean[] relative = new boolean[4];
		
		relative[Direction.N] = (Boolean) p.getAttribute("front");
		relative[Direction.S] = (Boolean) p.getAttribute("back");
		relative[Direction.W] = (Boolean) p.getAttribute("left");
		relative[Direction.E] = (Boolean) p.getAttribute("right");
		
		MapNode newSpace = new MapNode(this.current, absolutePerceptions(relative));
		this.map.put(this.current, newSpace);				
	}
	

	private void scheduleActions(int destDirection){
		
		int rotation = (destDirection - this.direction) % 4;
		if(rotation < 0){
			rotation += 4;
		}
		for(int i = 0; i < rotation; i++){
			this.actions.add(new Action("rotate"));
		}
		this.actions.add(new Action("advance"));
	}
	

	private int scheduleActions(int destDirection, int direction){
		
		int rotation = (destDirection - direction) % 4;
		if(rotation < 0){
			rotation += 4;
		}
		for(int i = 0; i < rotation; i++){
			this.actions.add(new Action("rotate"));
		}
		this.actions.add(new Action("advance"));
		
		return (direction + rotation) % 4;
	}
	

	private void buildPath(Stack<Long> path){
		
		int auxDirection = this.direction;
		long auxKeyCurrent = path.pop(), auxKeyNext = 0L;
		MapNode auxSpace = null;		
		
		while(!path.isEmpty()){
			
			auxKeyNext = path.pop();
			auxSpace = this.map.get(auxKeyCurrent);
			
			for (int i = 0; i < 4; i++) {
				if(auxKeyNext == auxSpace.children[i]){
					auxDirection = this.scheduleActions(i, auxDirection);
					break;
				}
			}
			
			auxKeyCurrent = auxKeyNext;
		}
	}
	

	private void exploreActions(){
				
		MapNode currentSpace = this.map.get(this.current);
		MapNode auxSpace = null;
		Stack<Long> path = null;
		long auxKeyCurrent = 0;
		int auxDirection = 0, k = 0;
		boolean flag = false;
		
		for (int i = 0; i < 4; i++) {
			auxDirection = (this.direction+i) % 4;
			
			if(currentSpace.valid[auxDirection] && !this.map.containsKey(currentSpace.children[auxDirection])){
				
				scheduleActions(auxDirection);
				
				for(int j = i; j < 4; j++){
					
					k = (auxDirection+j) % 4;
					if(currentSpace.valid[k] && !this.map.containsKey(currentSpace.children[k])){
							
						if(!this.toExplore.contains(this.current)){
						
							this.toExplore.push(this.current);
							break;
						}
					}
				}
				return ;
			}
		}
			
		if(!this.toExplore.isEmpty()){
			while(!this.toExplore.isEmpty() && !flag){
				
				auxKeyCurrent = this.toExplore.pop();
				auxSpace = this.map.get(auxKeyCurrent);		
				
				for(int i = 0; i < 4; i++){						
					if(auxSpace.valid[i] && !this.map.containsKey(auxSpace.children[i])){
						flag = true;
						break;
					}
				}
			}
			
			if(flag){
				path = this.search.search(this.current, auxKeyCurrent, this.map);
				this.buildPath(path);
				flag = false;
			}else{
				System.out.println("Recorrí todo! (1) :3");
			}
		}else{
			System.out.println("Recorrí todo! (2) :3");
		}
	}
	

	private void energyStatus(){
		int minPath = Integer.MAX_VALUE;
		double auxDistance = 0.0;
		Stack<Long> foodPath = null, aux = null;
		LinkedList<Long> reachables = new LinkedList();
		int[] currCoords = Space.decode(this.current), auxCoords = null;
	
		//Calcular mi foodPoint mas cercano teniendo en cuenta cuantos advance necesito para llegar a el;
		if (!this.foodSpace.isEmpty()){
			for (long foodSpace  : this.foodSpace) {
				
				auxCoords = Space.decode(foodSpace);
				auxDistance = Math.pow(auxCoords[0]-currCoords[0], 2)+Math.pow(auxCoords[1]-currCoords[1], 2);		
				if(Math.sqrt(auxDistance) <= this.currentEnergy){
					reachables.add(foodSpace);
				}
			}
			
			for (long i : reachables) {
				aux = this.search.search(this.current, i, this.map);
				
				if (aux.size() < minPath){
					minPath = aux.size();
					foodPath = (Stack<Long>) aux.clone();
				}
			}
			
			if ((this.currentEnergy - minPath) == 0){
				//Quito mis acciones posiblemente planificadas y planeo las necesatias para volver a el punto
				//comida
				this.actions.clear();
				
				//Anexar al to explore el nodo de la cabeza FALTA
				this.toExplore.push(this.current);
				this.buildPath(foodPath);
			}
		}		
		
	}
	
	@Override
	public Action compute(Percept p) {	
		
		MapNode aux = null;
		int recharge = 0;
		
		this.current = this.next;
		
		this.currentEnergy = (Integer) p.getAttribute("energy_level");		
		
		if(this.goalAchieved(p)){
			return new Action("no_op");
		}
		
		if((Boolean) p.getAttribute("resource") && this.currentEnergy < 40){
			
			this.actions.addFirst(new Action("eat"));
			
			//Revisar el nivel de energia para saber si es comida buena o mala
			if(this.lastEnergy < this.currentEnergy){
				recharge = (int) Math.ceil((40 - this.currentEnergy)/10);
				recharge--;
				while(recharge > 0){
					this.actions.addFirst(new Action("eat")); 
					// Creo que toca hacerlo funcionar con comida que indigesta
					recharge--;
				}
				
				if(!this.foodSpace.contains(this.current)){
					this.foodSpace.add(this.current);
				}
			}
		}	
		
		if(!this.map.containsKey(this.current)){
			drawMap(p);
		}
		
		if(this.currentEnergy <= 10){
			this.energyStatus();	
		}
			
		if(this.actions.isEmpty()){
			exploreActions();
		}
			
		
		Action action = (this.actions.isEmpty()) ? new Action("no_op") : this.actions.poll();
		
		switch(action.getCode()){
		
			case "advance":
				aux = this.map.get(this.current);
				this.next = aux.children[this.direction];		
			break;
			
			case "rotate":
				this.next = this.current;
				this.direction++;
				this.direction %= 4;
			break;
			
			case "eat":
				this.next = this.current;
				this.lastEnergy = this.currentEnergy;
			break;
			default:
			break;
		}
		
		return action;
		
	}
	
	public void printQueue(){
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (Iterator iterator = actions.iterator(); iterator.hasNext();) {
			Action action = (Action) iterator.next();
			sb.append(action.getCode() + ", ");
		}
		sb.setCharAt(sb.length()-1, ']');
		System.out.println(sb);		
	}
	
	public void printStack(Stack s){
		for (Iterator iterator = s.iterator(); iterator.hasNext();) {
			Long l = (Long) iterator.next();	
			printKey(l);
		}
	}
	
	public void printKey(long key){
		int[] coor = Space.decode(key);
		System.out.println("(" + coor[0] + ", " + coor[1] + ")");
	}

	
	@Override 
	public void init() {
		// TODO Auto-generated method stub
		this.actions.clear();
		this.toExplore.clear();
		this.foodSpace.clear();
		this.map.clear();
		this.current = 0;
		this.next = 0;
		this.direction = 0;
	}
	
	
	private boolean goalAchieved( Percept p ){
	    return (((Boolean)p.getAttribute("treasure")).booleanValue());
	}

	public static void main(String[] Args){
		new UNfailAgentProgram();
	}
	
}
