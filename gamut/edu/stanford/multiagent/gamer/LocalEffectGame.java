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

import java.util.*;
import edu.stanford.multiagent.gamer.graphs.*;
import edu.stanford.multiagent.gamer.functions.*;

/**
 * Abstract class which can be used to implement any Local-Effect
 * game.  Can be extended in order generate bi-directional LEGs, 
 * Uniform LEGs, etc.
 */

public abstract class LocalEffectGame extends Game
{

    protected ALGraph graph;

    // -------------------------------------------------


    /**
     * Constructor for a LEG.
     */
    public LocalEffectGame()
	throws Exception
    {
	super();
    }


    /**
     * Sets the number of players based on the command line parameter.
     * Separate initializers should be used for setting up the necessary 
     * functions and graphs.
     */
    public void initialize()
	throws Exception
    {
	super.initialize();
	setNumPlayers((int) getLongParameter(Game.players.name));
    }



    /**
     * Calculates the payoff for a player as the negation of the
     * cost function for the action the player has chosen.
     *
     * @param outcome an array holding the action choices of each player
     * @param player the player whose payoff should be returned
     */
    public double getPayoff(int[] outcome, int player)
    {
	// First figure out how many players have chosen
	// each action.  (This is called the D function in
	// the LEGs literature.)
	int actions = getNumActions(0);
	int[] D = new int[actions];
	for (int i = 0; i < actions; i++) 
	    D[i] = 0;

	for (int i = 0; i < getNumPlayers(); i++) {
	    // Action idices start at 1
	    int node = outcome[i] - 1;
	    D[node]++;
	}

	// Remember that action indices start at 1
	int chosenNode = outcome[player] - 1;
	double cost = 0;
       
	// First add the node function
	Function nodeFunc = (Function) graph.getNodeData(chosenNode);
	cost += nodeFunc.eval(D[chosenNode]);

	// Now add the edge functions for all edges 
	// coming into the chosen node -- Note that because of
	// the implementation of graphs, the effects stored on
	// edge (a, b) are the effects of node b on node a, which
	// is the opposite of how the edge functions are defined
	// in the literature.
	Iterator edgeIter = graph.getEdges(chosenNode);

	try {
	    while (edgeIter.hasNext()) {
		Edge e = (Edge) edgeIter.next();
		Function edgeFunc = (Function) e.getData();
		int neighborNode = e.getDest();
		cost += edgeFunc.eval(D[neighborNode]);
	    }
	} catch (Exception e) {
	    Global.handleError(e, "Error getting LEG payoff");
	}

	// The payoff is the negation of the cost 
	return(-cost);
    }


}

