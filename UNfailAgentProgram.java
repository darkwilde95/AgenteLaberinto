package unalcol.agents.examples.labyrinth.multeseo.eater.sis20181.UNfail;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.text.AbstractDocument.LeafElement;

import unalcol.agents.*;
import unalcol.agents.simulate.util.SimpleLanguage;

//TODO 470: Arreglar el Empty Stack en toExplore


public class UNfailAgentProgram implements AgentProgram {
	
	private final int HUNGRY = 0;
	private final int GIVE_WAY = 1;
	private final int EXPLORING = 2;
	private final int CHANGING_SPACE = 3;
	private final int MAX_WAIT = 4;
		
	private int direction, currentEnergy, lastEnergy, status, wait,ID;
	private boolean hasEaten;
	private long current, next; 
	private AStarSearch router;
	private LinkedList<Action> actions;
	private Stack<Long> toExplore; 
	private HashSet<Long> foodSpace;
	private HashSet<Long> badFoodSpace;
	private HashMap<Long, MapNode> map;
	private SimpleLanguage language;
	
	public UNfailAgentProgram(int ID, SimpleLanguage language) {
		this.ID = ID;
		this.language = language;
		this.wait = this.MAX_WAIT;
		this.direction = 0;
		this.lastEnergy = 20;
		this.hasEaten = false;
		this.currentEnergy = 0;
		this.map = new HashMap();
		this.next = this.current;
		this.toExplore = new Stack();
		this.status = this.EXPLORING;
		this.foodSpace = new HashSet();
		this.actions = new LinkedList();
		this.badFoodSpace = new HashSet();
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
	
	private void drawMap(boolean[] relative){		
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
		Stack<Long> path = this.router.search(orig, dest, this.map);
		int auxDirection = this.direction;
		long auxKeyCurrent = 0L, auxKeyNext = 0L;
		MapNode auxSpace = null;
		
		if(!path.isEmpty()){
			auxKeyCurrent = path.pop();								
		}
		
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
		
		// Revisar las posibles direcciones que puede tomar y que no han sido exploradas
		for (int i = 0; i < 4; i++) {
			// Elegir primero la direccion que requiere menos rotaciones
			auxDirection = (this.direction+i) % 4;	
			if(currentSpace.valid[auxDirection] && !this.map.containsKey(currentSpace.children[auxDirection])){
				scheduleActions(auxDirection);
				
				// Revisar si hay mas espacios por explorar en la posicion actual
				for(int j = 1; j < 4; j++){ // j = i, j < 4
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
		
		// Si ya acabo de revisar un camino y tiene caminos pendientes por revisar
		if(!this.toExplore.isEmpty()){
			
			// Eliminar caminos pendientes que haya encontrado por explorar otro camino anteriormente
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
			
			
			// Si hay un camino sin explorar desde un espacio pendiente de exploracion
			if(flag){
				this.buildPath(this.current, auxKeyCurrent);
				this.status = this.CHANGING_SPACE;
				flag = false;
			}else{
				
				// Se verifica que se revisaron todos los caminos
				System.out.println("Recorrí todo! (x) :3");
			}
		}else{
			
			// No habian caminos pendientes por revisar
			System.out.println("Recorrí todo! (" + this.ID + ") :3");
		}
	}
	
	private void energyActions(){
		
		int minPath = Integer.MAX_VALUE;
		double auxDistance = 0.0;
		Stack<Long> foodPath = null, aux = null;
		LinkedList<Long> reachables = new LinkedList();
		int[] currCoords = Space.decode(this.current), auxCoords = null;
	
		if (!this.foodSpace.isEmpty()){
			
			// Separar los puntos de comida por distancia
			for (long foodSpace : this.foodSpace) {
				auxCoords = Space.decode(foodSpace);
				auxDistance = Math.pow(auxCoords[0]-currCoords[0], 2)+Math.pow(auxCoords[1]-currCoords[1], 2);		
				if(Math.sqrt(auxDistance) <= this.currentEnergy){
					reachables.add(foodSpace);
				}
			}
			
			// Calcula la distancia y path mas cortos para los puntos posibles de alcanzar
			for (long i : reachables) {
				aux = this.router.search(this.current, i, this.map);
				
				if (aux.size() < minPath){
					minPath = aux.size();
					foodPath = (Stack<Long>) aux.clone();
				}
			}
			
			// Minimo de energia para llegar al punto mas cercano
			if ((this.currentEnergy - minPath) == 3){
				
				this.actions.clear();
				if(this.status == this.EXPLORING){
					this.toExplore.push(this.current);
				}
				this.buildPath(foodPath);
				this.status = this.HUNGRY;
			}
		}else{
			System.out.println(this.ID + ": No tengo donde comer ramen :v");
		}
	}
	
	private void giveWayActions(){
		
		MapNode currentSpace = this.map.get(this.current);
		int auxDirection = 0;
		
		// Buscar la direccion que requiere menos rotaciones a la que me pueda mover
		// y que este dentro de lo explorado
		for (int i = 1; i < 4; i++) {
			auxDirection = (this.direction+i) % 4;
			if(currentSpace.valid[auxDirection] && this.map.containsKey(currentSpace.children[auxDirection])){
				scheduleActions(auxDirection);
				return;
			}
		}
		
		// No hay direccion libre para moverse
		this.actions.addFirst(new Action("no_op"));
	}
	
	private void changeActions(){
		
		
		long auxKey = 0;
		
		// Esperar que el otro agente de el paso	
		if(this.wait > 0){
			this.wait--;
			this.actions.addFirst(new Action("no_op"));
			
		}else {
			// Si ya esperé lo suficiente
			this.actions.clear();
			
			// Si estaba explorando
			if(this.status == this.EXPLORING){
				
				
				if(!this.toExplore.isEmpty()){
					auxKey = this.toExplore.peek();
					
					// Evitar elegir el mismo camino en el que esta mirando
					
					// Si el ultimo pendiente es el mismo en donde esta ubicado
					if(auxKey == this.current){
						this.actions.addFirst(new Action("rotate"));
					}else{
					// Si no es el mismo, debo guardar la posicion actual para 
					// volver mas tarde
						this.status = this.CHANGING_SPACE;
						this.toExplore.pop();
						this.toExplore.push(this.current);
						this.toExplore.push(auxKey);
						this.buildPath(this.current, auxKey);
					}
				}else{
					this.actions.addFirst(new Action("no_op")); 
				}
				
				
			// Si estaba cambiando de espacio
			}else if(this.status == this.CHANGING_SPACE){
				this.giveWayActions();
				this.status = this.GIVE_WAY;
			}
		}
	}
	

	
	@Override
	public Action compute(Percept p) {			
		
		int recharge = 0;
		MapNode aux = null;
		boolean repeat = false;
		boolean fail = (boolean) p.getAttribute("fail");
		boolean[] walls = new boolean[4];
		walls[Direction.N] = (boolean) p.getAttribute("front");
		walls[Direction.W] = (boolean) p.getAttribute("left");
		walls[Direction.E] = (boolean) p.getAttribute("right");
		walls[Direction.S] = (boolean) p.getAttribute("back");
		boolean afront = (boolean) p.getAttribute("afront");
		boolean resource = (boolean) p.getAttribute("resource");
		boolean treasure = (boolean) p.getAttribute("treasure");
		int energy_level = (int) p.getAttribute("energy_level");		
		
		if(energy_level == 0){
			System.out.println(this.ID + ": Comi veneno y mori O:)");
		}else{
			System.out.println(this.ID + ": energy level: " + energy_level);
		}
		
		// Verificar que se llegó al tesoro
		if(treasure){
			return new Action("no_op");
		}
		
		this.currentEnergy = energy_level;
		
		// Verificar el movimiento
		if(fail){	
						
			if (this.status == this.HUNGRY){
				//this.actions.addFirst(new Action("no_op"));
				this.actions.addFirst(new Action("advance"));
			}else{
				//this.actions.addFirst(new Action("advance"));
				this.changeActions();
			}
			
			// Verificar energia
			if(this.currentEnergy <= 15 && this.status != this.HUNGRY){
				this.energyActions();
			}
					
		}else{
			
			// Actualizar la posicion
			this.current = this.next;
						
			// Dibujar el mapa
			if(!this.map.containsKey(this.current)){
				this.drawMap( walls ); 
			}
			
			// Verificar energia
			if(this.currentEnergy <= 15 && this.status != this.HUNGRY){
				this.energyActions();
			}
			
			// Comer si no tiene la energia al maximo, no conoce el espacio o es un espacio de mala comida
			if(resource){
				if(this.hasEaten){
					// Si hay un cambio bueno despues de comer				
					if(this.lastEnergy < this.currentEnergy){
						recharge = (int) Math.ceil((double)(40 - this.currentEnergy)/10.0);
						while(recharge > 0){
							this.actions.addFirst(new Action("eat"));
							recharge--;
						}
						
						//Agrega el espacio de comida buena si no esta en la lista
						if(!this.foodSpace.contains(this.current)){
							this.foodSpace.add(this.current);
						}
					}else if(this.lastEnergy > this.currentEnergy) {
										
						//Si hubo un cambio malo despues de comer
						if(!this.badFoodSpace.contains(this.current)){
							this.badFoodSpace.add(this.current);
						}
					}
				}else {
					// Probar la comida
					if(!this.badFoodSpace.contains(this.current)){
						this.actions.addFirst(new Action("eat"));
					}
				}			
			}
			
			// Preparar siguientes acciones
			// Estaba explorando
			if(this.status == this.EXPLORING){
				
				// Si hay un agente en frente
				if(afront){
					if(this.actions.isEmpty()){
						this.exploreActions();
					}
					this.changeActions();
				}else{
					
				// Si no hay un agente en frente
					if(this.actions.isEmpty()){
						this.exploreActions();
					}
				}	
			}else if(this.status == this.CHANGING_SPACE){
				
				// Si hay un agente en frente
				if(afront){
					this.changeActions();
					
				// Si no hay un agente en frente
				}else{
							
					// Llegó al destino
					if(this.actions.isEmpty()){
						this.toExplore.pop();
						this.exploreActions();
						this.status = this.EXPLORING;					
					}
				}
			}else if(this.status == this.HUNGRY){
				
				// Si hay un agente en frente
				if(afront){
					this.actions.addFirst(new Action("no_op"));
					
				//Si no hay un agente en frente
				}else{
					
					// Si ya llegó al espacio de recarga
					if(this.actions.isEmpty()){
						
						// Si ya recargo la energia al maximo
						if(this.currentEnergy == 40){
							this.buildPath(this.current, this.toExplore.peek());
							this.status = this.CHANGING_SPACE;
						}
					}
				}
			}else if(this.status == this.GIVE_WAY){
				
				// Si hay un agente en frente
				if(afront){
					this.giveWayActions();
					
				// Si no hay un agente en frente
				}else{
					
					if(this.actions.isEmpty()){
						this.buildPath(this.current, this.toExplore.peek());  //PILAS FUE AQUI
						this.status = this.CHANGING_SPACE;
					}
				}
				
			}		
		}

		Action action = (this.actions.isEmpty()) ? new Action("no_op") : this.actions.poll();
		
		switch(action.getCode()){
		
			case "advance":
				aux = this.map.get(this.current);			
				this.next = (fail) ? this.current : aux.children[this.direction];
				if(this.wait < this.MAX_WAIT){
					this.wait = this.MAX_WAIT;
				}
				this.lastEnergy = this.currentEnergy;
				this.hasEaten = false;
				break;
			
			case "rotate":
				this.next = this.current;
				if(this.wait < this.MAX_WAIT){
					this.wait = this.MAX_WAIT;
				}
				this.direction++;
				this.hasEaten = false;
				this.direction %= 4;
				break;
			
			case "eat":
				if(this.wait < this.MAX_WAIT){
					this.wait = this.MAX_WAIT;
				}
				this.next = this.current;
				this.hasEaten = true;
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
		this.badFoodSpace.clear();
		this.map.clear();
		this.current = 0;
		this.next = 0;
		this.direction = 0;
	}

	public static void main(String[] Args){
		new UNfailAgentProgram(3, null);
	}
	
}
