package tddc17;
 
 
import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;
import java.util.Random;
import java.util.Stack;
 
class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;
 
	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;
 
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;
 
	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");
 
		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
	    {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
	    }
 
	}
 
	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}
 
	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" _ ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}
 
class MyAgentProgram implements AgentProgram {
 
	private int initnialRandomActions = 10;
	private Random random_generator = new Random();
 
	// Here you can define your variables!
	public int iterationCounter = 10000;
	public MyAgentState state = new MyAgentState();
	public Stack<Node> stack = new Stack<Node>();
	public Node targetNode;
	public boolean backtrack;
 
	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0) 
		    	state.agent_direction +=4;
		    state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
		    state.agent_last_action = state.ACTION_TURN_RIGHT;
		    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}
 
 
	@Override
	public Action execute(Percept percept) {
 
		// DO NOT REMOVE this if condition!!!
    	if (initnialRandomActions>0) {
    		return moveToRandomStartPosition((DynamicPercept) percept);
    	} else if (initnialRandomActions==0) {
    		// process percept for the last step of the initial random actions
    		initnialRandomActions--;
    		state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.HOME);
			state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
    	}
    	
    	
 
    	// This example agent program will update the internal agent state while only moving forward.
    	// START HERE - code below should be modified!
 
	    iterationCounter--;
 
	    if (iterationCounter==0)
	    	return NoOpAction.NO_OP;
 
	    DynamicPercept p = (DynamicPercept) percept;
	    Boolean bump = (Boolean)p.getAttribute("bump");
	    Boolean dirt = (Boolean)p.getAttribute("dirt");
	    Boolean home = (Boolean)p.getAttribute("home");
	    System.out.println("percept: " + p);
 
	    if (state.agent_last_action==state.ACTION_MOVE_FORWARD && !bump && !backtrack) {
	    	System.out.println("Added to the stack: " + state.agent_x_position + " " + state.agent_y_position);
	    	stack.add(new Node(state.agent_x_position,state.agent_y_position));
	    }
	    
	    // State update based on the percept value and the last action
	    state.updatePosition((DynamicPercept)percept);
	    if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
	    }
	    if (dirt)
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
	    else if (state.world[state.agent_x_position][state.agent_y_position] != state.HOME)
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);
 
    	System.out.println("x=" + state.agent_x_position);
    	System.out.println("y=" + state.agent_y_position);
    	System.out.println("dir=" + state.agent_direction);
 
	    state.printWorldDebug();
 
	    System.out.println("Stack size: " + stack.size());
 
	    // Next action selection based on the percept value
	    if (dirt)
	    {
	    	System.out.println("DIRT -> choosing SUCK action!");
	    	state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
	    } 
	    else
	    {
	    	if(targetNode == null) {
		    	int direction = decideDirection(state.agent_direction);
		    	if(direction > -1) { // && iterationCounter > ((stack.size() * 2) + 1)) {
		    	    backtrack = false;
		    		return moveToDirection(direction);
		    	} else if (state.world[state.agent_x_position][state.agent_y_position] != state.HOME) {
		    		backtrack = true;
		    		targetNode = stack.pop();
		    		System.out.println("Removed from stack: " + targetNode.x + " " + targetNode.y);
		    		return moveToNode(targetNode);
		    	} else {
		    		return NoOpAction.NO_OP;
		    	}
	    	} else {
	    		return moveToNode(targetNode);
	    	}
	    }
	}
	
	Action moveToNode(Node target) {
		System.out.println("Target: " + target.x + " " + target.y);
		int directionToTarget = directionToNode(new Node(state.agent_x_position, state.agent_y_position),target);
		if(directionToTarget == state.agent_direction) {
			targetNode = null;
		}
		return moveToDirection(directionToTarget);
	}
 
	Action moveToDirection(int direction) {
		if(state.agent_direction == direction) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		} else if(state.agent_direction-direction == 1) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0) 
		    	state.agent_direction +=4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else {
			state.agent_direction = ((state.agent_direction+1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
	}
 
	int decideDirection(int startingDirection) {
 
		int counter = 0;
		while(counter < 4) {
			System.out.println("Is unknown in direction" + ((startingDirection+counter)%4) + "? " + isUnknownAhead((startingDirection+counter)%4));
			if(isUnknownAhead((startingDirection+counter)%4)) {
				return (startingDirection+counter)%4;
			}
			counter++;
		}
		//then we should backtrack
		return -1;
	}
 
	boolean isUnknownAhead(int direction) {
		switch (direction) {
		case MyAgentState.NORTH:
			return state.world[state.agent_x_position][state.agent_y_position-1] == state.UNKNOWN;
		case MyAgentState.EAST:
			return state.world[state.agent_x_position+1][state.agent_y_position] == state.UNKNOWN;
		case MyAgentState.SOUTH:
			return state.world[state.agent_x_position][state.agent_y_position+1] == state.UNKNOWN;
		case MyAgentState.WEST:
			return state.world[state.agent_x_position-1][state.agent_y_position] == state.UNKNOWN;
		default:
			return false;
		}
	}
 
	int directionToNode(Node currentNode, Node desiredNode) {
		if(currentNode.x == desiredNode.x) {
			if(currentNode.y > desiredNode.y) {
				return MyAgentState.NORTH;
			} else {
				return MyAgentState.SOUTH;
			}
		} else {
			if(currentNode.x > desiredNode.x) {
				return MyAgentState.WEST;
			} else {
				return MyAgentState.EAST;
			}
		}
	}
}
 
public class MyVacuumAgent extends AbstractAgent {
    public MyVacuumAgent() {
    	super(new MyAgentProgram());
	}
}
 
class Node {
 
	public int x;
	public int y;
 
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
	}
 
}