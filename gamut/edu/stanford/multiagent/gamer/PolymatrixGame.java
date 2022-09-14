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
 * Generate a Polymatrix Game with any given structure as long
 * as the structure has been implemented as a graph class. 
 * For example, can be used to create polymatrix games with
 * tree structures or ring structures.
 *
 * Similar to StructuredGraphicalGame except the payoff matrices
 * in the graphical games are per node and the payoff matrices in
 * the polymatrix game are per edge.  (This means it is sensible
 * for polymatrix games to have subgames that are not just random,
 * unlike the random graphical games.)
 *
 */ 


public class PolymatrixGame extends GraphicalGame
{

    // ------------------------------------------------------

    // Parameters: The polymatrix games takes in a graph class
    // and a game class and the parameters for each of these.
    //

    private static Parameters.ParamInfo pGraph;
    private static Parameters.ParamInfo pGraphParams;
    private static Parameters.ParamInfo pSubGame;
    private static Parameters.ParamInfo pSubGameParams;
    private static Parameters.ParamInfo[] pgParam;

    static {

	pGraph = new Parameters.ParamInfo("graph", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the graph structure class to use");

	pGraphParams = new Parameters.ParamInfo("graph_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the graph, must be enclosed in [].");

	pSubGame = new Parameters.ParamInfo("subgame", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the game class to use as a subgame.  There will be an error if the subgame does not have two players or if the number of actions for either of the players is different than that supplied by the actions parameter.");

	pSubGameParams = new Parameters.ParamInfo("subgame_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the subgame, must be enclosed in [].  If the \"players\" or \"actions\" parameters are generally required by this subgame, they may be left out.  These will be reset to appropriate values automatically.  All other parameters may be generated randomly and will then be regenerated for each instance of the subgame.");
	
	pgParam = new Parameters.ParamInfo[] {Game.players,
					      Game.symActions, pGraph,
					      pGraphParams, pSubGame,
					      pSubGameParams};
	Global.registerParams(PolymatrixGame.class, pgParam);
	
    }


    // ---------------------------------------------------

    private ParamParser edgeGameParams;
    private String subGameName;

    boolean randomize;

    // ---------------------------------------------------


    public PolymatrixGame() 
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


    /**
     * Initialize this game, the graph, and the two player games.
     */
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
	if (!graph.hasSymEdges())
	  throw new Exception("Graph used by LEG must be undirected!");
	
	// Get the parameters for the edge games.  These will
	// be stored for later when the edge games are created.
	edgeGameParams = parameters.getParserParameter(pSubGameParams.name);
	subGameName = parameters.getStringParameter(pSubGame.name);

    }


    /**
     * Randomize parameters that were not set by the user
     */
    public void randomizeParameters() {
      try {
	
	if(!parameters.setByUser(pGraph.name))
	  parameters.setParameter(pGraph.name, 
				  Global.getRandomClass(Global.GRAPH,
							"GraphWithNodeParam"));
	
	if(!parameters.setByUser(pSubGame.name))
	  parameters.setParameter(pSubGame.name,
				  Global.getRandomClass(Global.GAME,
							"GameWithActionParam"));
      } catch (Exception e) {
	Global.handleError(e, "Randomizing Graphs/SubGames");
      }
      
      // Deal with the CMDLINE_PARAM
      super.randomizeParameters();
    }


    /**
     * This will be done for the subgame matrices and the graph
     * separately
     */
    protected void checkParameters() throws Exception 
    {
      // -- if need to randomize graphs parameters, then must
      // -- be able to set number of nodes
      if(randomize && parameters.isParamSet(pGraph.name))
	if(!Global.isPartOf(Global.GRAPH, getStringParameter(pGraph.name), 
			   "GraphWithNodeParam"))
	  throw new Exception("ERROR: Cannot randomize graph parameters unless graph is an instance of GraphWithNodeParam!");

      if(randomize && parameters.isParamSet(pSubGame.name))
	if(!Global.isPartOf(Global.GAME, getStringParameter(pSubGame.name),
			    "GameWithActionParam"))
	  throw new Exception("ERROR: Cannot randomiza subgame parameters unless can set number of actions. Need an instance of GameWithActionParam!");
    }



    protected String getGameHelp()
    {
	return "Creates a polymatrix game using the given graph and " +
	  "the given subgame type to form two player edge games.\n\n" +
	  "If randomization is desired, graph must belong to the " +
	  "GraphWithNodesParam class, and subgame must support 2 players and belong to the GameWithActionParam class.";
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
		// -- manually set everything
		graph.setParameter("nodes", new Long(getNumPlayers()), true);

		// -- try to override reflexivity/symmetry params
		try {graph.setParameter("reflex_ok", Boolean.FALSE, true);}
		catch (Exception e) {}
		
		try {graph.setParameter("sym_edges", Boolean.TRUE, true);}
		catch (Exception e) {}
	      }

	    graph.setParameters(graphParams, randomize);
	    graph.initialize();
	} catch (Exception e) {
	  System.err.println(getHelp());
	    System.err.println(graph.getHelp());
	    Global.handleError(e, "Error initializing graph: ");
	}
    }
	
    


    /**
     * Generates a new instance of the edge game.  Needs to set
     * parameters each time in case parameters are randomized.
     */
    private Game generateNewEdgeGame(Game edgeGame) 
	throws Exception
    {	
	if (randomize) 
	  {
	    // -- override players/actions
	    try {edgeGame.setParameter("players", new Long(2), true);}
	    catch (Exception e) {
		//System.err.println("Warning: could not set players parameter for subgame.");
	    }

	    try {edgeGame.setParameter("actions", getParameter("actions"), 
				       true);} 
	    catch (Exception e) {
		//System.err.println("Warning: could not set actions parameter for subgame.");
	    }
	  }

	edgeGame.setParameters(edgeGameParams, randomize);
	edgeGame.initialize();
	
	// Check to make sure that the subgame is valid to be
	// used as part of a polymatrix game.
	if (2 != edgeGame.getNumPlayers()) {
	    Global.handleError ("Subgames of polymatrix games " +
				"must have two players: " + subGameName);
	}
	
	if ((getNumActions(0) != edgeGame.getNumActions(0)) ||
	    (getNumActions(0) != edgeGame.getNumActions(1))) {
	    Global.handleError ("Subgames of polymatrix games " +
				"must have the same number of actions as " +
				"the number which each player of the " +
				"polymatrix game has: " + subGameName);
	}	

	edgeGame.generate();
	return edgeGame;
    }



    /** 
     * Generate the matrices for each node of the graph and 
     * the graph connecting neighbors to each other.
     */
    public void doGenerate()
    {
	setDescription("Polymatrix Game\n"  + getDescription());
	setName("Polymatrix Game");

	// Generate the graph for the game.
	graph.doGenerate();

	setDescription(getDescription() + "\nGraph Params:\n" + graph.getDescription());

	for (int i = 0; i < graph.getNNodes(); i++) {
	    Iterator edgeIter = graph.getEdges(i);

	    while (edgeIter.hasNext()) {
		Edge edge = (Edge) edgeIter.next();
		Game edgeGame = null;
	    
		// Make sure the edge data was not set already as this
		// might have happened when the reverse edge was set
		if (edge.getData() == null) {
		    
		    // Now make sure the reversed edge exists
		    Edge reverse = graph.getEdge(edge.getDest(), 
						 edge.getSource());
		    
		    try {
			// Make sure there is a reverse
			if (reverse == null)
			    throw new Exception ("Found edge without " + 
						 "existence of reverse " +
						 "edge.  Polymatrix games " +
						 "must use reflexive graphs.");


			// Create a new instance of the edge game
			// -- this is cleaner, it resets parameter status
			// -- and parameter values
			edgeGame = (Game) Global.getObjectOrDie(
							 subGameName,
							 Global.GAME);

 			edgeGame = generateNewEdgeGame(edgeGame);

		    } catch (Exception e) {
			Global.handleError(e, "Unable to generate polymatrix game (subgame "+subGameName +")");
		    }	
		    
		    
		    int[] dimensions = new int[2];
		    
		    // Dimensions of matrix for player 2
		    dimensions[0] = getNumActions(edge.getDest());
		    dimensions[1] = getNumActions(edge.getSource());
		    DoubleTensor payMatrixDest = new DoubleTensor(dimensions);
		    
		    // Dimensions of matrix for player 1
		    dimensions[0] = getNumActions(edge.getSource());
		    dimensions[1] = getNumActions(edge.getDest());
		    DoubleTensor payMatrixSource = 
			new DoubleTensor(dimensions);
	
		    Outcome outcome=new Outcome(2, dimensions);
		    outcome.reset();
		    
		    int[] next = new int[2];
		    int[] reverseNext = new int[2];
		    
		    while (outcome.hasMoreOutcomes()) {
			next = outcome.getOutcome();
			outcome.nextOutcome();
			
			reverseNext[0] = next[1];
			reverseNext[1] = next[0];
			
			payMatrixSource.setValue
			    (edgeGame.getPayoff(next, 0), next);
			payMatrixDest.setValue
			    (edgeGame.getPayoff(reverseNext, 1), 
			     reverseNext);
		    }
		    
		    // Now set the data for the edges on the graph 
		    try {
			edge.setData(payMatrixSource);
			reverse.setData(payMatrixDest); 
		    } catch (Exception e) {
			Global.handleError(e, "Error setting edge data");
		    }
 		}
	    }
	}
    }
}


