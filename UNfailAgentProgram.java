package unalcol.agents.UNfail;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.text.AbstractDocument.LeafElement;

import unalcol.agents.*;

//TODO giveWayActions: 294: podemos completar el path sin llamar a la busqueda
//TODO 298: Arreglar el orden de las percepciones


public class UNfailAgentProgram implements AgentProgram {
	
	private final int HUNGRY = 0;
	private final int GIVE_WAY = 1;
	private final int EXPLORING = 2;
	private final int CHANGING_SPACE = 3;
	private final int MAX_WAIT = 2;
		
	private int direction, currentEnergy, lastEnergy, status, wait,ID;
	private long current, next; 
	private AStarSearch router;
	private LinkedList<Action> actions;
	private Stack<Long> toExplore; 
	private HashSet<Long> foodSpace;
	private HashMap<Long, MapNode> map;
	
	public UNfailAgentProgram(int ID) {
		this.ID = ID;
		this.wait = this.MAX_WAIT;
		this.direction = 0;
		this.lastEnergy = 20;
		this.currentEnergy = 0;
		this.map = new HashMap();
		this.next = this.current;
		this.toExplore = new Stack();
		this.status = this.EXPLORING;
		this.foodSpace = new HashSet();
		this.actions = new LinkedList();
		this.router = new AStarSearch();
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
	
	private void buildPath(long orig, long dest){
		
		System.out.print(this.ID + ": Estoy en ");
		printKey(orig);
		System.out.print(this.ID + ": Quiero ir a ");
		printKey(dest);
		Stack<Long> path = this.router.search(orig, dest, this.map);
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
				
				auxKeyCurrent = this.toExplore.peek();
				auxSpace = this.map.get(auxKeyCurrent);		
				
				for(int i = 0; i < 4; i++){						
					if(auxSpace.valid[i] && !this.map.containsKey(auxSpace.children[i])){
						flag = true;
						break;
					}
				}
				
				if(!flag){
					this.toExplore.pop();
				}
			}
			
			if(flag){
				this.buildPath(this.current, auxKeyCurrent);
				this.status = this.CHANGING_SPACE;
				flag = false;
			}else{
				System.out.println("Recorrí todo! (x) :3");
			}
		}else{
			System.out.println("Recorrí todo! (" + this.ID + ") :3");
		}
	}
	
	private void energyActions(){
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
				aux = this.router.search(this.current, i, this.map);
				
				if (aux.size() < minPath){
					minPath = aux.size();
					foodPath = (Stack<Long>) aux.clone();
				}
			}
			
			if ((this.currentEnergy - minPath) == 0){
				this.actions.clear();
				
				if(this.status == this.EXPLORING){
					this.toExplore.push(this.current);
					
				}
				this.buildPath(foodPath);
				this.status = this.HUNGRY;
			}
		}		
		
	}
	
	private void changeActions(){
		long auxKey = 0;
		boolean flag = false;
		MapNode auxSpace = null;
		
		if(this.toExplore.isEmpty()){
			this.actions.addFirst(new Action("no_op")); //Tal vez despues miramos
		}else{
			if(this.wait > 0){
				this.wait--;
				this.actions.addFirst(new Action("no_op")); 
				System.out.println(this.ID + ": Esperando");
			}else{
				this.actions.clear();
				System.out.println(this.ID + ": No me dio paso, debo dar yo el paso");
				System.out.print(this.ID + ": Guardando que debo volver aqui despues");				
				
				while(!this.toExplore.isEmpty() && !flag){
					
					auxKey = this.toExplore.peek();
					auxSpace = this.map.get(auxKey);		
					
					for(int i = 0; i < 4; i++){						
						if(auxSpace.valid[i] && !this.map.containsKey(auxSpace.children[i])){
							flag = true;
							break;
						}
					}
					
					if(!flag){
						this.toExplore.pop();
					}
				}
				
				if(flag){
					this.actions.clear();
					if(this.status == this.EXPLORING){
						this.toExplore.push(this.current);
						this.status = this.GIVE_WAY;
						
					}else if(this.status == this.CHANGING_SPACE || this.status == this.GIVE_WAY){
						this.status = this.GIVE_WAY;
					}
					
					this.buildPath(this.current, auxKey);
					this.status = this.CHANGING_SPACE;
					flag = false;
				}
			}
		}
	}
	
	private void giveWayActions(Percept p){
		
		MapNode currentSpace = this.map.get(this.current);
		int auxDirection = 0;
		boolean[] perception = new boolean[4];
		
		Boolean aux = (Boolean) p.getAttribute("afront");
		perception[Direction.N] = (aux == null) ? false : aux;
		
		aux = (Boolean) p.getAttribute("aright");
		perception[Direction.E] = (aux == null) ? false : aux;
		
		aux = (Boolean) p.getAttribute("aback");
		perception[Direction.S] = (aux == null) ? false : aux;
		
		aux = (Boolean) p.getAttribute("aleft");
		perception[Direction.W] = (aux == null) ? false : aux;
		
		System.out.println(this.ID + ": afront: " + this.ID + ": " + perception[Direction.N]);
		System.out.println(this.ID + ": aright: " + this.ID + ": " + perception[Direction.E]);
		System.out.println(this.ID + ": aback: " + this.ID + ": " + perception[Direction.S]);
		System.out.println(this.ID + ": aleft: " + this.ID + ": " + perception[Direction.W]);
		
		perception = this.absolutePerceptions(perception);
		
		for (int i = 0; i < 4; i++) {
			auxDirection = (this.direction+i) % 4;
			if(currentSpace.valid[auxDirection] && this.map.containsKey(currentSpace.children[auxDirection])){
				System.out.println("Hay camino en la direccion " + auxDirection + ": " + this.ID);
				if(!perception[auxDirection]){
					switch(auxDirection){
						case 0:
							System.out.println(this.ID + ": Direccion valida para dar permiso N");
						break;
						case 1:
							System.out.println(this.ID + ": Direccion valida para dar permiso E");
						break;
						case 2:
							System.out.println(this.ID + ": Direccion valida para dar permiso S");
						break;
						case 3:
							System.out.println(this.ID + ": Direccion valida para dar permiso W");
						break;
					}
					
					scheduleActions(auxDirection);
					return;
				}
			}
		}
		
		//cuando ya no me puedo mover
		System.out.println(this.ID + ": No me puedo mover");
		this.actions.addFirst(new Action("no_op"));
	}
	
	@Override
	public Action compute(Percept p) {	
		
		int recharge = 0;
		MapNode aux = null;
		boolean repeat = false;
		
		
		this.current = this.next;
		this.currentEnergy = (Integer) p.getAttribute("energy_level");
		
		if(!this.map.containsKey(this.current)){
			this.drawMap(p); 
		}
		
		if(this.goalAchieved(p)){
			return new Action("no_op");
		}
		
		if(this.currentEnergy <= 15 && this.status != this.HUNGRY){
			this.energyActions();
		}
		
		if((Boolean) p.getAttribute("resource") && this.currentEnergy < 40){
			
			this.actions.addFirst(new Action("eat"));
			
			if(this.lastEnergy < this.currentEnergy){
				recharge = (int) Math.ceil((40 - this.currentEnergy)/10);
				recharge--;
				while(recharge > 0){
					this.actions.addFirst(new Action("eat"));
					recharge--;
				}
				
				if(!this.foodSpace.contains(this.current)){
					this.foodSpace.add(this.current);
				}
			}
		}
		
		
		do{
			if(this.status == this.EXPLORING){
				
				repeat = false;
				
				if((boolean) p.getAttribute("afront")){
					this.changeActions();
				}else{
					if(this.actions.isEmpty()){
						this.exploreActions();
					}
				}
				
			}else if(this.status == this.CHANGING_SPACE){
				
				repeat = false;
				
				if((boolean) p.getAttribute("afront")){
					this.changeActions();
				}else{
					if(this.actions.isEmpty()){
						this.status = this.EXPLORING;
						repeat = true;
					}
				}
				
				
			}else if(this.status == this.HUNGRY){
				
				repeat = false;
				
				if((boolean) p.getAttribute("afront")){
					this.actions.addFirst(new Action("no_op"));
				}else{
					if(this.actions.isEmpty()){
						
						if(this.currentEnergy == 40){
							this.buildPath(this.current, this.toExplore.peek());
							this.status = this.CHANGING_SPACE;
						}
					}
				}
				
			}else if(this.status == this.GIVE_WAY){
				
				repeat = false;
				
				if((boolean) p.getAttribute("afront")){
					this.giveWayActions(p);
				}else{
					if(this.actions.isEmpty()){
						this.buildPath(this.current, this.toExplore.peek());
						this.status = this.CHANGING_SPACE;
					}
				}
				
			}
		}while(repeat);
		
		Action action = (this.actions.isEmpty()) ? new Action("no_op") : this.actions.poll();
		
		
		switch(action.getCode()){
		
			case "advance":
				aux = this.map.get(this.current);
				this.next = aux.children[this.direction];
				if(this.wait < this.MAX_WAIT){
					this.wait = this.MAX_WAIT;
				}
			break;
			
			case "rotate":
				this.next = this.current;
				if(this.wait < this.MAX_WAIT){
					this.wait = this.MAX_WAIT;
				}
				this.direction++;
				this.direction %= 4;
			break;
			
			case "eat":
				if(this.wait < this.MAX_WAIT){
					this.wait = this.MAX_WAIT;
				}
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
		new UNfailAgentProgram(3);
	}
	
}
