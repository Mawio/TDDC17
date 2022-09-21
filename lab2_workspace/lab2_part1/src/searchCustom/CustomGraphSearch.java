package searchCustom;

import java.util.ArrayList;
import java.util.HashSet;

import searchShared.NodeQueue;
import searchShared.Problem;
import searchShared.SearchObject;
import searchShared.SearchNode;

import world.GridPos;

public class CustomGraphSearch implements SearchObject {

	private HashSet<SearchNode> explored;
	private NodeQueue frontier;
	protected ArrayList<SearchNode> path;
	private boolean insertFront;

	/**
	 * The constructor tells graph search whether it should insert nodes to front or back of the frontier 
	 */
    public CustomGraphSearch(boolean bInsertFront) {
		insertFront = bInsertFront;
    }

	/**
	 * Implements "graph search", which is the foundation of many search algorithms
	 */
	public ArrayList<SearchNode> search(Problem p) {
		// The frontier is a queue of expanded SearchNodes not processed yet
		frontier = new NodeQueue();
		/// The explored set is a set of nodes that have been processed
		explored = new HashSet<SearchNode>();
		// The start state is given
		GridPos startState = (GridPos) p.getInitialState();
		// Path will be empty until we find the goal.
		path = new ArrayList<SearchNode>();
		
		// Our code begins here
		SearchNode node = new SearchNode(startState);
		if (p.isGoalState(node.getState())) {
			path.add(node);
			return path;
		}

		frontier.addNodeToFront(node);
		explored.add(node);
		
		while(!frontier.isEmpty()) {
			node = frontier.removeFirst();
			for(GridPos s : p.getReachableStatesFrom(node.getState())) {
				SearchNode child = new SearchNode(s, node);

				if(p.isGoalState(s)) {
					path = child.getPathFromRoot();
					return path;
				}
			
				if(!explored.contains(child)) {
					
					explored.add(child);
					if(insertFront) {
						frontier.addNodeToFront(child);
					} else {
						frontier.addNodeToBack(child);
					}
					
				}
			}
		}
		return path;
	}

	/*
	 * Functions below are just getters used externally by the program 
	 */
	public ArrayList<SearchNode> getPath() {
		return path;
	}

	public ArrayList<SearchNode> getFrontierNodes() {
		return new ArrayList<SearchNode>(frontier.toList());
	}
	public ArrayList<SearchNode> getExploredNodes() {
		return new ArrayList<SearchNode>(explored);
	}
	public ArrayList<SearchNode> getAllExpandedNodes() {
		ArrayList<SearchNode> allNodes = new ArrayList<SearchNode>();
		allNodes.addAll(getFrontierNodes());
		allNodes.addAll(getExploredNodes());
		return allNodes;
	}

}
