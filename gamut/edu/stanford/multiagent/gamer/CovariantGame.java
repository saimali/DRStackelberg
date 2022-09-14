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

import Jama.*;

/**
 * Return a game, where payoffs for players are drawn from
 * a multi-normal with covariance r.
 */

public class CovariantGame extends MatrixGame
{
  // -- Parameters: 
  // number of players and the number of actions
  // covariance r
  private static Parameters.ParamInfo[] cvgParam;
  
  private static Parameters.ParamInfo rParam;

  static {

    rParam = new Parameters.ParamInfo("r", Parameters.ParamInfo.DOUBLE_PARAM, new Double(-1), new Double(1), "covariance of any two player's payoffs in the same action profile. Must be between -1/(#players-1) and 1.", false, new Double(0.5));

    cvgParam = new Parameters.ParamInfo[] {Game.players, Game.actions, rParam};
    Global.registerParams(CovariantGame.class, cvgParam);
  }
  
  // ----------------------------------------------
  
  /**
   * Construct a new game
   */
  
  public CovariantGame()
    throws Exception
  {
    super();
  }
  
  public void initialize()
    throws Exception
  {
    super.initialize();
    
    parsePlayersActions();
    
    initMatrix();
  }
  
  
  protected void checkParameters() throws Exception
  {
    int n = (int)getLongParameter(Game.players.name);
    double r = getDoubleParameter(rParam.name);
    if(r < -1.0/ (double)(n-1))
      throw new Exception("Correlation must be >= -1/(n-1)");
  }
  

  public void randomizeParameters()  {
    int n = (int)getLongParameter(Game.players.name);
    rParam.low = new Double(-1.0/(double)(n-1));
    parameters.randomizeParameter(rParam.name);
  }
  
  /** 
   * Returns the help screen
   */  
  protected String getGameHelp()
  {
    return "Creates a game with the given number of players " +
      "with payoffs distributed normally(0,1) with covariance r." +
      getRangeHelp();
  }
  
  /** 
   * Fills in the payoffs
   */
  public void doGenerate()
  {
    setDescription("A Game With Normal Covariant Random Payoffs\n" 
		   + getDescription());
    setName("Random Normal-Covariant Matrix Game");


    double r = getDoubleParameter(rParam.name);
    //    double a = Math.sqrt(r);
    //    double b = Math.sqrt(1-r);

    double[][] a = new double[getNumPlayers()][getNumPlayers()];

    for(int i=0; i<getNumPlayers(); i++)
      for(int j=0; j<getNumPlayers(); j++)
	a[i][j]= (i==j ? 1 : r);

    Matrix sigma=new Matrix(a, getNumPlayers(), getNumPlayers());
    CholeskyDecomposition chol = sigma.chol();
    if(!chol.isSPD())
      System.err.println("WARNING: SIGMA is not SPD!");

    Matrix lMat = chol.getL();
    double[][] z=new double[getNumPlayers()][1];
    
    
    Outcome outcome=new Outcome(getNumPlayers(), getNumActions());
    
    for(outcome.reset(); outcome.hasMoreOutcomes(); outcome.nextOutcome())
      {

//  	double x=Global.rand.nextGaussian();

//  	for(int i=0; i<getNumPlayers(); i++)
//  	  {
//  	    double y = Global.rand.nextGaussian();

//  	    setPayoff(outcome.getOutcome(), i, a*x+b*y);
//  	  }

	for(int i=0; i<z.length; i++)
	  z[i][0]=Global.rand.nextGaussian();

	Matrix m = new Matrix(z, z.length, 1);

	Matrix x = lMat.times(m);

	for(int i=0; i<getNumPlayers(); i++)
	  setPayoff(outcome.getOutcome(), i, x.get(i,0));
	
      }
  }
}
