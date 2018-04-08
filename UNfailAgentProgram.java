package unalcol.agents.UNfail;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.text.AbstractDocument.LeafElement;

import unalcol.agents.*;


public class UNfailAgentProgram implements AgentProgram {
		
	private int direction;
	private Long current, last; 
	private HashMap<Long, MapNode> map;
	private Queue<Action> actions;
	
	
	public UNfailAgentProgram() {
		this.map = new HashMap();
		this.actions = new LinkedList();
		this.direction = 0;
		this.current = Space.encode(0, 0);
		this.last = null;
		this.actions.add(new Action("no-op"));
		System.out.println("Soy UNfail");
		
	}

	public void printPerceptions(Percept p){
		//  "front", "right", "back", "left", "treasure",
	    //"resource", "resource-color", "resource-shape", "resource-size", "resource-weight", "resource-type", "energy_level" 
		System.out.println("front " + p.getAttribute("front"));
		System.out.println("right " + p.getAttribute("right"));
		System.out.println("back " + p.getAttribute("back"));
		System.out.println("left "  + p.getAttribute("left"));
		System.out.println("treasure " + p.getAttribute("treasure"));
		System.out.println("resource " + p.getAttribute("resource"));
		//System.out.println("resource-color " + p.getAttribute("resource-color"));
		//System.out.println("resource-shape " + p.getAttribute("resource-shape"));
		//System.out.println("resource-size " + p.getAttribute("resouce-size"));
		//System.out.println("resource-weight " + p.getAttribute("resource-weight"));
		//System.out.println("resource-type " + p.getAttribute("resource-type"));
		//System.out.println("energy_level " + p.getAttribute("energy_level"));
		//System.out.println(this.map.size());
		//System.out.println("___________________________________________________");
	}
	
	
	public boolean[] absolutePerceptions(boolean[] relative){
		boolean[] absolute = new boolean[4];
		
		switch(direction){
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
				absolute[Direction.W] = relative[Direction.S];
				absolute[Direction.E] = relative[Direction.N];
			break;
			case Direction.E:
				absolute[Direction.N] = relative[Direction.W];
				absolute[Direction.S] = relative[Direction.E];
				absolute[Direction.W] = relative[Direction.N];
				absolute[Direction.E] = relative[Direction.S];				
			break;
		}
		
		return absolute;
		
	}
	
	public void drawMap(Percept p){
	
		boolean[] relative = new boolean[4];
		
		relative[Direction.N] = (Boolean) p.getAttribute("front");
		relative[Direction.S] = (Boolean) p.getAttribute("back");
		relative[Direction.W] = (Boolean) p.getAttribute("left");
		relative[Direction.E] = (Boolean) p.getAttribute("right");
		
		MapNode newSpace = new MapNode(this.current, absolutePerceptions(relative));
		this.map.put(this.current, newSpace);
//		this.last = this.current;			
				
	}
	
	public void scheduleActions(int destDirection){
		
		int rotation = (destDirection - this.direction) % 4;
		for(int i = 0; i < rotation; i++){
			this.actions.add(new Action("rotate"));
			this.direction++;
		}
		this.direction %= 4;
		this.actions.add(new Action("advance"));		
	}
	
	public void exploreActions(){
		MapNode currentSpace = this.map.get(this.current);
		Long aux = null;		
		
		if((aux = currentSpace.children[Direction.N]) != null && !this.map.containsKey(aux)){
			scheduleActions(Direction.N);
		}else if((aux = currentSpace.children[Direction.E]) != null && !this.map.containsKey(aux)){ 
			scheduleActions(Direction.E);
		}else if((aux = currentSpace.children[Direction.S]) != null && !this.map.containsKey(aux)){ 
			scheduleActions(Direction.S);
		}else if((aux = currentSpace.children[Direction.W]) != null && !this.map.containsKey(aux)){ 
			scheduleActions(Direction.W);
		}else{
			//Tengo que devolverme
		}		
		
	}
	
	public void returnActions(){
		
	}
	
	
	@Override
	public Action compute(Percept p) {
		printPerceptions(p);
		if (!this.current.equals(this.last)){
			drawMap(p);
		}
		exploreActions();
		
		Action action = (this.actions.isEmpty()) ? new Action("no_op") : this.actions.poll();
		
		switch(action.getCode()){
			case "advance":
			
				this.last = this.current;
				int[] coords = Space.decode(this.last); 
				
				switch(this.direction){
					case Direction.N:
						this.current = Space.encode(coords[0], coords[1] + 1 );
					break;
					case Direction.E:
						this.current = Space.encode(coords[0] + 1, coords[1]);
					break;
					case Direction.S:
						this.current = Space.encode(coords[0], coords[1] - 1);
					break;
					case Direction.W:
						this.current = Space.encode(coords[0] - 1, coords[1]);
					break;
				}
				System.out.println("voy a avanzar");
			break;
			
			default:
				System.out.println(action.getCode());
			break;
		}
		System.out.println(this.map.size());
		System.out.println("___________________________________________________");
	
		return action;
		
	}

	@Override 
	public void init() {
		// TODO Auto-generated method stub
		this.actions.clear();
		this.map.clear();
		
	}
	
	public boolean goalAchieved( Percept p ){
	    return (((Boolean)p.getAttribute("treasure")).booleanValue());
	}

	public static void main(String[] Args){
		new UNfailAgentProgram();
	}
	
}
