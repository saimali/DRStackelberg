/*
 * Copyright (C) 2004 Jennifer Wortman, Eugene Nudelman, Kevin Leyton-Brown, Yoav Shoham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.stanford.multiagent.gamer;

import java.io.*;
import java.util.*;
import edu.stanford.multiagent.gamer.graphs.*;

/**
 * Abstract class implements the basic common features of
 * graphical games.
 *
 * The graphical game may have data stored at either  edges or at the 
 * nodes.  If data is at the nodes, this data would be in the form of 
 * a matrix of payoffs for one players based on actions of all connected
 * players.  If the data is at the edges it would be in the form of 2 
 * person games which are added to the node data to get the payoff.
 */

public abstract class GraphicalGame extends Game
{

    protected ALGraph graph;

    // ----------------------------------------------------

    /**
     * Constructor for new graphical games.
     */
    public GraphicalGame()
	throws Exception
    {
	super();
    }


    /**
     * Adds an edge from player1 to player2 but leaves the data
     * for the edge set to null.  Use this to add edges if the
     * graphical game being created uses matrices at the nodes
     * instead of at the egdes
     * 
     * @param player1 the player node at which the edge should originate
     * @param player2 the player node at which the edge should end
     */
    protected void addEdge(int player1, int player2)
	throws Exception
    {
	// Make sure players are valid and that the edge does
	// not already exist and that the players in question do
	// not already have node data matrices of a certain size
	// set for them
	if ((player1 < 0) || (player1 >= getNumPlayers()) ||
	    (player2 < 0) || (player2 >= getNumPlayers()) ||
	    (player1 == player2) || (graph.getNodeData(player1) != null) ||
	    (graph.getNodeData(player2) != null))
	    throw new Exception("Attempt to add edge with " + 
				"invalid players in GraphicalGame");

	if (graph.areNeighbours(player1, player2))
	    throw new Exception("Attempt to create edge that " +
				"already exists in GraphicalGame");

	// Add the edge to the graph
	graph.addEdge(player1, player2);
    }



    /**
     * Adds an edge from player1 to player2 if the player numbers
     * are valid, and sets the matrix at the edge to the one given.
     *
     * @param player1 the player node at which the edge should originate
     * @param player2 the player node at which the edge should end
     * @param matrix the tensor to store on the edge
     *
     * @throws Exception if the tensor is invalid, if an edge from 
     * player1 to player2 already exists, or if the player numbers are
     * invalid
     */
    protected void addEdge(int player1, int player2, DoubleTensor matrix)
	throws Exception
    {
	// Make sure players are valid and that the edge does
	// not already exist and that the players in question do
	// not already have node data matrices of a certain size
	// set for them
	if ((player1 < 0) || (player1 >= getNumPlayers()) ||
	    (player2 < 0) || (player2 >= getNumPlayers()) ||
	    (player1 == player2) || (graph.getNodeData(player1) != null) ||
	    (graph.getNodeData(player2) != null))
	    throw new Exception("Attempt to add edge with " + 
				"invalid players in GraphicalGame");

	if (graph.areNeighbours(player1, player2))
	    throw new Exception("Attempt to create edge that " +
				"already exists in GraphicalGame");

	// Make sure the DoubleTensor is of the right dimensions
	if ((matrix.getNumDimensions() != 2) ||
	    (matrix.getSizeOfDim(0) != getNumActions(player1)) ||
	    (matrix.getSizeOfDim(1) != getNumActions(player2)))
	    throw new Exception("Invalid DoubleTensor in GraphicalGame");

	// If all is well, add the edge
	graph.addEdge(player1, player2, matrix);
    }



    /**
     * Sets the matrix for the player's node if using the version
     * of the graphical game representation in which the payoffs
     * are stored in matrices at the nodes (as opposed to only
     * on edges).  The appropriate number of edges must have
     * already been added to the graph.  After this, no more
     * edges can be added to or from this player.
     *
     * @param player the player whose node the data should be
     * stored at
     * @param matrix the tensor to store at this node
     */
    public void setNodeMatrix(int player, DoubleTensor matrix)
    {
	// First make sure the number of edges connected to the
	// player in the graph corresponds with the number of
	// dimensions in the matrix.
	int numEdges = matrix.getNumDimensions() - 1;
	Iterator edgeIter = graph.getEdges(player);	
	int count = 0;

	while (edgeIter.hasNext()) {
	    edgeIter.next();
	    count++;
	}
     
	try {
	    if (count != numEdges)
		throw new Exception("Node matrix is of the wrong size");
	} catch (Exception e) {
	    Global.handleError (e, "Error setting node data for " +
				"graphical game");
	}

	// If the Tensor is the right size, set the node data
	graph.setNodeData(player, matrix);
    }



    /**
     * Figures out payoff for a certain outcome by adding up
     *
     *   a) the payoff from the matrix that is stored at the
     *      player's node if one exists
     *
     *   b) payoffs from all of the matrices which are connected 
     *      by edges to the node of a certain player, if any of
     *      those exist
     *
     * @param outcome an array containing the actions chosen by
     * each player
     * @param player the player whose payoff should be returned
     */
    public double getPayoff(int[] outcome, int player)
    {
	Iterator edgeIter;
	double payoff = 0;

	// First see if there is matrix data stored at the
	// node for the player, in which case calculate the
	// payoff for the player based on the actions of all
	// connected players and this payoff matrix
	if (graph.getNodeData(player) != null) {

	    DoubleTensor matrix = (DoubleTensor) graph.getNodeData(player);
	    int numEdges = matrix.getNumDimensions() - 1;

	    // Iterate through the edges getting the actions of
	    // each player involved in the matrix game.
	    edgeIter = graph.getEdges(player);

	    int[] nodeGameOutcome = new int[numEdges + 1];
	    nodeGameOutcome[0] = outcome[player];
	    int count = 1;

	    try {
		while (edgeIter.hasNext()) {
		    Edge e = (Edge) edgeIter.next();

		    if (count > numEdges) {
			throw new Exception("Incorrect node matrix in " +
					    "graphical game: too many edges");
		    
		    }

		    int otherPlayer = e.getDest();
		    
		    nodeGameOutcome[count] = outcome[otherPlayer];
		    count++;
		}

		if (count != numEdges + 1) {
		    throw new Exception("Incorrect node matrix in " +
					"graphical game: not enough edges");
		}

	    } catch (Exception e) {
		Global.handleError(e, "Error getting graphical payoff");
	    }

	    // Finally get the payoff for the player based on
	    // the his actions and the actions of the others
	    payoff += matrix.getValue(nodeGameOutcome);
	}

		
	int[] miniGameOutcome = new int[2];
	miniGameOutcome[0] = outcome[player];
	edgeIter = graph.getEdges(player);

	while (edgeIter.hasNext()) {
	    Edge e = (Edge) edgeIter.next();

	    // See if there is matrix data stored at the edge
	    // and if so add the payoff from this matrix
	    if (e.getData() != null) {
		DoubleTensor matrix = (DoubleTensor) e.getData();
		int otherPlayer = e.getDest();
		miniGameOutcome[1] = outcome[otherPlayer];
		payoff += matrix.getValue(miniGameOutcome);
	    }
	}

	return payoff;
    }


    /** 
     * Creates and initializes a graph of the correct type.  Must
     * be implemented by each subclass and called in the initialize
     * method after all parameters have been set in place.
     */
    protected abstract void initGraph()
	throws Exception;


    /**
     * Writes output as a graphical game.  The format of this output
     * is subject to change with future versions of GAMUT.
     *
     * @param out the PrintWriter to which the output should be written
     */
    public void writeGame(PrintWriter out) 
    {
      out.println("# This is an ad-hoc graphical game output format.");
      out.println("# It may change with future versions of GAMUT.\n");

	out.println("graph {");
	for(int i=0; i<graph.getNNodes(); i++)
	    {
		Iterator it = graph.getNeighbours(i);
		while(it.hasNext())
		    {
			int to = ((Integer)it.next()).intValue();
			if(to > i)
			    out.println("Node_" + i + " -- Node_" + to);
		    }
	    }

	out.println("}\n");

	out.println("#Payoff Section: ");
	for(int i=0; i<getNumPlayers(); i++)
	  {
	    out.println("Player: " + i);
	    out.println("Actions: " + getNumActions(i));

	    DoubleTensor p=(DoubleTensor)graph.getNodeData(i);

	    Outcome outcome=new Outcome(p.getNumDimensions(),
					p.getSizeOfDim());
	    outcome.reset();
	    while(outcome.hasMoreOutcomes())
	      {
		try {
		  out.print(getOutputPayoff(p.getValue(outcome.getOutcome()))
			    + "\t");
		} catch (Exception e) {
		  Global.handleError(e, "Failed to format output!");
		}

		outcome.nextOutcome();
	      }
	    out.println("\n\n");
	      
	  }
    }

}

