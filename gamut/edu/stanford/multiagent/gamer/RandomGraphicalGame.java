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

/**
 * Generate a Graphical Game with any given structure as long
 * as the structure has been implemented as a graph class. 
 * For example, can be used to create Ring Structured Games
 * and Tree Structured Games.
 *
 * This is a working version of the graphical game with 
 * random payoffs.  Other styles of payoffs are not yet 
 * implemented.
 *
 */ 


public class RandomGraphicalGame extends GraphicalGame
{

    // ------------------------------------------------------

    // Parameters: Takes as parameters the graph to be used
    // with its parameters, and the number of actions (which
    // currently must be the same for all players).
    //

    private static Parameters.ParamInfo pGraph;
    private static Parameters.ParamInfo pGraphParams;
    private static Parameters.ParamInfo[] rggParam;

    static {

	pGraph = new Parameters.ParamInfo("graph", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the graph structure class to use");

	pGraphParams = new Parameters.ParamInfo("graph_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the graph, must be enclosed in [].");

	rggParam = new Parameters.ParamInfo[] {Game.players,
					       Game.symActions, pGraph,
					       pGraphParams};
	Global.registerParams(RandomGraphicalGame.class, rggParam);
    }

    // ----------------------------------
    private boolean randomize;


    public RandomGraphicalGame() 
	throws Exception
    {
	super();
    }



    public void setParameters(ParamParser p, boolean randomize)
	throws Exception
    {
	super.setParameters(p, randomize);
	this.randomize=randomize;
    }



    public void initialize()
	throws Exception
    {
	super.initialize();

	// Get number of players and actions
	parsePlayersSameNumberActions();

	// Initialize the graph
	initGraph();	

	// The number of players is equal to the number of
	// nodes in the graph, and the number of actions 
	// depends on the parameters of the random game
      if( graph.getNNodes() != getNumPlayers() )
	throw new Exception("Number of nodes in the graph must be the same as the number of players!");
      if (graph.reflexEdgesOk())
	throw new Exception("Reflexive edges not allowed in the graph!");
    }



    //
    // This will be done for the subgame matrices and the graph
    // separately
    //
    protected void checkParameters() throws Exception 
    {
      // -- if need to randomize graphs parameters, then must
      // -- be able to set number of nodes
      if(randomize && parameters.isParamSet(pGraph.name))
	if(!Global.isPartOf(Global.GRAPH, getStringParameter(pGraph.name), 
			   "GraphWithNodeParam"))
	  throw new Exception("ERROR: Cannot randomize graph parameters unless graph is an instance of GraphWithNodeParam!");
    }


    //
    // Randomize parameters that were not set by the user
    //
    public void randomizeParameters() {

	try {
	    if(!parameters.setByUser(pGraph.name))
		parameters.setParameter(pGraph.name, 
					Global.getRandomClass(Global.GRAPH, "GraphWithNodeParam"));

	} catch (Exception e) {
	    Global.handleError(e, "Randomizing Graphs");
	}

	// Deal with the CMDLINE_PARAM
	super.randomizeParameters();
    }




    protected String getGameHelp()
    {
	return "Creates a version of any random graphical game. " +
	    "Parameters for the given graph class " +
	    "must be set. If randomization is desired, graph must belong to the class GraphWithNodeParam. Note also that the number of nodes in the graph as implied by graph parameters must match the number of players.";
    }



    /**
     * Initialize a graph with the number of nodes
     * equal to the number of players.
     */ 
    protected void initGraph()
    {
	// Instantiate a graph of the type selected by the user
	String graphName = parameters.getStringParameter(pGraph.name);
	graph = (ALGraph) Global.getObjectOrDie(graphName, Global.GRAPH);
	
	ParamParser graphParams = 
	    parameters.getParserParameter(pGraphParams.name);

	try {
	    if(randomize)
	      {
		// -- then manually set node parameter
		graph.setParameter("nodes", new Long(getNumPlayers()), true);
		// -- try to override reflexivity params
		// -- NOTE:This code assumes that all graphs either have this
		// -- NOTE: flag, or have reflex_ok always False.
		// -- NOTE: This should be reflected in Docs
		try {
		  graph.setParameter("reflex_ok", Boolean.FALSE, true);
		} catch (Exception e) {}
		
	      }
	    graph.setParameters(graphParams, randomize);
	    graph.initialize();
	} catch (Exception e) {
	  System.err.println(getHelp());
	    System.err.println(graph.getHelp());
	    System.err.println(graph.getDescription());
	    Global.handleError(e, "Error initializing graph: ");
	}
    }
	
    

    /** 
     * Generate the matrices for each node of the graph 
     * graph and the graph connecting neighbors to each other.
     */
    public void doGenerate()
    {
	setDescription("Random Graphical Game\n"  + getDescription());
	setName("Random Graphical Game");

	// Generate the graph for the game.
	graph.doGenerate();

	setDescription(getDescription() + "\nGraph Params:\n" + graph.getDescription());

	Vector actionsVector = new Vector(1);
	actionsVector.add(0, String.valueOf(getNumActions(0)));

	// Now that the graph has been generated, initialize the node
	// games to have the correct number of players
 	for (int i = 0; i < getNumPlayers(); i++) {

	    // Count the number of edges attached to the node
	    // representing the player in the graph
	    long numPlayers = 1;
	    Iterator edgeIter = graph.getEdges(i);
	    while (edgeIter.hasNext()) {
		edgeIter.next();
		numPlayers++;
	    }

	    Long numPlayersLong = new Long(numPlayers);
	    Game nodeGame = null;

	    try {
		nodeGame = new RandomGame();
		nodeGame.parameters.setParameter(Game.players.name, 
						     numPlayersLong);
		nodeGame.parameters.setParameter(Game.actions.name,
						     actionsVector);
		nodeGame.initialize();
	    } catch (Exception e) {
		Global.handleError(e, "Unable to initialize node game");
	    }
	    
	    int[] dimensions = new int[(int) numPlayers];
	    for (int j = 0; j < (int) numPlayers; j++)
		dimensions[j] = getNumActions(0);

	    DoubleTensor payMatrix = new DoubleTensor(dimensions);

 	    // Generate the matrix game for the node.  An entire random
	    // game is created although only the payoffs for one player
	    // are used.
	    try {
		nodeGame.generate();
	    } catch (Exception e) {
		Global.handleError(e, "Error generating node game");
	    }
	    
 	    Outcome outcome=new Outcome(nodeGame.getNumPlayers(), 
					nodeGame.getNumActions());
 	    outcome.reset();	
 	    int[] next = new int[(int)numPlayers];
	    
 	    while (outcome.hasMoreOutcomes()) {
 		next = outcome.getOutcome();
 		outcome.nextOutcome();
		
 		// Get payoffs for the first player
		payMatrix.setValue
 		    (nodeGame.getPayoff(next, 0), next);
 	    }
		
 	    // Now set the data for the node on the graph 
	    try {
		setNodeMatrix(i, payMatrix);
	    } catch (Exception e) {
		Global.handleError(e, "Error setting node data");
	    }
	    nodeGame=null;
 	}
    }
}


