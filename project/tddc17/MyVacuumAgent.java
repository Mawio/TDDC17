package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

class MyAgentState {
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN = 0;
	final int WALL = 1;
	final int CLEAR = 2;
	final int DIRT = 3;
	final int HOME = 4;
	final int ACTION_NONE = 0;
	final int ACTION_MOVE_FORWARD = 1;
	final int ACTION_TURN_RIGHT = 2;
	final int ACTION_TURN_LEFT = 3;
	final int ACTION_SUCK = 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	public Node target_node;
	public Stack<Node> nodes_to_visit = new Stack<Node>();
	public Stack<Node> tail = new Stack<Node>();
	public boolean firstTurn;
	public boolean walking_home;

	MyAgentState() {
		for (int i = 0; i < world.length; i++)
			for (int j = 0; j < world[i].length; j++)
				world[i][j] = UNKNOWN;
		agent_last_action = ACTION_NONE;
	}

	// Based on the last action and the received percept updates the x & y agent
	// position
	public void updatePosition(DynamicPercept p) {
		Boolean bump = (Boolean) p.getAttribute("bump");

		if (agent_last_action == ACTION_MOVE_FORWARD && !bump) {
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

	public void updateWorld(int x_position, int y_position, int info) {
		world[x_position][y_position] = info;
	}

	public void printWorldDebug() {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				if (world[j][i] == UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i] == WALL)
					System.out.print(" # ");
				if (world[j][i] == CLEAR)
					System.out.print(" _ ");
				if (world[j][i] == DIRT)
					System.out.print(" D ");
				if (world[j][i] == HOME)
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
	// uses percepts to update the Agent position - only the position, other
	// percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if (action == 0) {
			state.agent_direction = ((state.agent_direction - 1) % 4);
			if (state.agent_direction < 0)
				state.agent_direction += 4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action == 1) {
			state.agent_direction = ((state.agent_direction + 1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
		state.agent_last_action = state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}

	@Override
	public Action execute(Percept percept) {
		// DO NOT REMOVE this if condition!!!
		if (initnialRandomActions > 0) {
			return moveToRandomStartPosition((DynamicPercept) percept);
		} else if (initnialRandomActions == 0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.HOME);
			state.firstTurn = true;
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only
		// moving forward.
		// START HERE - code below should be modified!

		iterationCounter--;

		if (iterationCounter == 0)
			return NoOpAction.NO_OP;

		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean) p.getAttribute("bump");
		Boolean dirt = (Boolean) p.getAttribute("dirt");
		Boolean home = (Boolean) p.getAttribute("home");
		System.out.println("percept: " + p);

		if (state.agent_last_action == state.ACTION_MOVE_FORWARD && !bump && !backtrack) {
			System.out.println("Added to the stack: " + state.agent_x_position + " " + state.agent_y_position);
			stack.add(new Node(state.agent_x_position, state.agent_y_position));
		}

		// State update based on the percept value and the last action
		state.updatePosition((DynamicPercept) percept);
		if (bump) {
			switch (state.agent_direction) {
				case MyAgentState.NORTH:
					state.updateWorld(state.agent_x_position, state.agent_y_position - 1, state.WALL);
					break;
				case MyAgentState.EAST:
					state.updateWorld(state.agent_x_position + 1, state.agent_y_position, state.WALL);
					break;
				case MyAgentState.SOUTH:
					state.updateWorld(state.agent_x_position, state.agent_y_position + 1, state.WALL);
					break;
				case MyAgentState.WEST:
					state.updateWorld(state.agent_x_position - 1, state.agent_y_position, state.WALL);
					break;
			}
		}
		if (dirt)
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.DIRT);
		else
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);

		System.out.println("x=" + state.agent_x_position);
		System.out.println("y=" + state.agent_y_position);
		System.out.println("dir=" + state.agent_direction);

		state.printWorldDebug();

		if (dirt) {
			System.out.println("DIRT -> choosing SUCK action!");
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// Ours
		var cur = new Node(state.agent_x_position, state.agent_y_position);
		// Special behavior for first turn
		if (state.firstTurn) {
			pushNewNodes(cur);
			state.target_node = state.nodes_to_visit.pop();
			state.firstTurn = false;
		}

		// Special behavior for reaching home square
		if (state.walking_home && home) {
			return NoOpAction.NO_OP;
		}

		// Whenever we moved to a different node, there are new nodes to be discovered
		if (cur.equals(state.target_node)) {
			pushNewNodes(cur);
		}

		// Whenever we bump or move, we have discovered a new node
		// Therefore select a new node from stack
		if (bump || cur.equals(state.target_node)) {
			// Pop nodes from stack until we find an undiscovered one
			// Some nodes on the stack will already be discovered, since nodes are pushed whenever a new square is visited
			do {
				state.target_node = state.nodes_to_visit.pop();
			} while (state.target_node.isDiscovered(state.world) && !state.nodes_to_visit.empty());

			// If everything is discovered, move to home
			if (state.nodes_to_visit.empty()) {
				state.walking_home = true;
				state.target_node = new Node(1, 1);
			}
			System.out.println("Selected new target node: " + state.target_node);
		}

		// Determine action
		var action = getAction(cur, state.target_node);
		if (action == LIUVacuumEnvironment.ACTION_TURN_LEFT) {
			System.out.println("Old agent direction: " + state.agent_direction);
			state.agent_direction = ((state.agent_direction - 1) % 4);
			if (state.agent_direction == -1) {
				state.agent_direction = 3;
			}
			System.out.println("New agent direction: " + state.agent_direction);
		} else if (action == LIUVacuumEnvironment.ACTION_TURN_RIGHT) {
			System.out.println("Old agent direction: " + state.agent_direction);
			state.agent_direction = ((state.agent_direction + 1) % 4);
			System.out.println("New agent direction: " + state.agent_direction);
		}
		return action;
	}

	private void pushNewNodes(Node cur) {
		var north = new Node(cur, MyAgentState.NORTH);
		var east = new Node(cur, MyAgentState.EAST);
		var south = new Node(cur, MyAgentState.SOUTH);
		var west = new Node(cur, MyAgentState.WEST);

		var nodes = new Node[] { west, south, east, north };
		for (var node : nodes) {
			if (!node.isDiscovered(state.world) && node.x > 0 && node.y > 0) {
				System.out.println("Pushed Node " + node);
				state.nodes_to_visit.push(node);
			}
		}
	}

	private Action getAction(Node cur, Node target) {
		var directionToNode = directionToNode(cur, target);
		if (directionToNode == state.agent_direction) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		} else {
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
		// TODO
	}

	private int directionToNode(Node currentNode, Node desiredNode) {
		if (currentNode.x == desiredNode.x) {
			if (currentNode.y > desiredNode.y) {
				return MyAgentState.NORTH;
			} else {
				return MyAgentState.SOUTH;
			}
		} else {
			if (currentNode.x > desiredNode.x) {
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

	public Node(Node other, int direction) {
		this.x = other.x;
		this.y = other.y;
		switch (direction) {
			case 0:
				this.y--;
				break;
			case 2:
				this.y++;
				break;
			case 1:
				this.x++;
				break;
			case 3:
				this.x--;
				break;
		}
	}

	public boolean isDiscovered(int[][] world) {
		// Isn't unknown
		return world[x][y] != 0;
	}

	public int get(int[][] world) {
		return world[this.x][this.y];
	}

	@Override
	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}

	@Override
	public boolean equals(Object obj) {
		var other = (Node)obj;
		return other.x == this.x && other.y == this.y;
	}

}