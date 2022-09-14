"""
This file contains functions used to generate figures for the arxiv-22 submission. Run this first.
"""

#%%
"""
Import libraries
"""
from matplotlib import pyplot as plt
import pandas as pd
import numpy as np
import random
from collections import Counter
import itertools
import gurobipy as gp
from gurobipy import *
from gurobipy import GRB
import csv
import os
import time
import math

###############################################################################
"""
Algebraic functions
"""

"""
This function makes a zero matrix.
Parameters:
n_rows: number of rows
n_columns: number of columns  
output: matrix of zeros
"""
def make_zeros(n_rows: int, n_columns: int):
    # define empty matrix
    matrix = []
    for i in range(n_rows):
        matrix.append([0.0] * n_columns)
    return matrix

"""
This function returns n choose r, in the combinatorial sense.
Parameters:
n: first argument in nCr
r: second argument in nCr
output: n Choose r, number of ways of choosing r objects out of n objects
"""
def nChoosek(n,r):
    # define factorial operator
    f = math.factorial 
    return f(n) / f(r) / f(n-r)

"""
This function returns the number of actions of the player given the set size 
and subset size in the Simple Inspection Game.
Parameters:
s: size of the big set 
v: maximum size of the subset (leader or follower) for the player
output: size of the action space, equal to sC1 + sC2 + ... sCv
"""
def InspectionActionSize(s,v):
    out = 0
    j = 1
    while j<v+1:
        out += nChoosek(s,j)
        j += 1
        
    return int(out)

"""
This function computes the utility function for a mixed strategy, given one
pure strategy/action for the other player.
Parameters:
utilityFunc: utility function under consideration (Leader or Follower)
mixStrategy: mixed strategy vector (simplex) for one player
pureAction: pure strategy/action for other player
leadOrFoll: Is this for leader (L) or follower (F)
output: mixed strategy utility function value
"""
def MixedStrategyUtilityOneAction(utilityFunc,mixStrategy,pureAction,leadOrFoll):
  
    # If we are computing leader utility
    if (leadOrFoll=='L'):
        # take column corresponding to pureAction and multiply by mix strategy
        utilMix = np.matmul(np.transpose(utilityFunc[:,pureAction]),mixStrategy)
    # if we are computing follower utility    
    else: 
        # take row corresponding to pureAction and multiply by mix strategy
        utilMix = np.matmul(utilityFunc[:,pureAction],mixStrategy)
        
    return utilMix

"""
This function computes the utility function for a mixed strategy, given all
pure strategies/actions for the other player.
Parameters:
utilityFunc: utility function under consideration (Leader or Follower)
mixStrategy: mixed strategy vector (simplex) for one player
leadOrFoll: Is this for leader (L) or follower (F)
output: mixed strategy utility function value for all actions of other player
"""
def MixedStrategyUtilityAllActions(utilityFunc,mixStrategy,leadOrFoll):
  
    # If we are computing leader utility
    if (leadOrFoll=='L'):
        #output will be a row vector
        utilMix = np.matmul(np.transpose(mixStrategy),utilityFunc)
        # make it into column vector
        utilMix = np.transpose(utilMix) 
    # if we are computing follower utility
    else: 
        # take row corr to pureAction and multiply by mix strategy
        utilMix = np.matmul(utilityFunc,mixStrategy)
        
    return utilMix


"""
This function generates all Frobenius matrix differences, given k matrices
Parameters:
utilFollower: a three dimensional matrix of k follower utility functions
k: number of follower utility functions
output: k x k matrix where each (i,j) entry is frobenius norm difference of
the ith and jth follower utility function
"""
def FrobDiffMatrix(utilFollower,k):
    out = np.zeros((k,k))
    for i in range(k):
        for j in range(k):
            # difference of matrix
            matDiff = np.subtract(utilFollower[:,:,i],utilFollower[:,:,j])
            # frobenius norm
            out[i][j] = np.linalg.norm(matDiff, ord='fro')
            
    return out

"""
This function generates all Frobenius matrix differences, given nominal utility functions
and follower utility functions
Parameters:
utilFollowerSets: k sets of follower utility functions
utilNomFollower: the k hat{u}_fj nominal follower utilities
sizNominal: number of nominal utility functions
sizFollFunc: number of utlity functions in current iteration
EfTaujSizes: sizes of each EfTauj set of follower utility functions
output: sizFollFunc x sizNominal matrix where each (i,j) entry is frobenius norm difference of
the ith follower utility function and jth nominal distribution
"""
def FrobDiffNomMatrix(utilFollowerSets,utilNomFollower,sizNominal,sizFollFunc,EfTaujSizes):
    out = np.zeros((sizFollFunc,sizNominal))
    
    # for each uhat(i)
    for i in range(sizNominal):
    
        somCount = 0 # counter
        for j in range(sizNominal): # for each j=1,...,k
            for j1 in range(EfTaujSizes[j]): # for each uf in Ef(tau,j)
    
                # difference of matrix
                matDiff = np.subtract(utilFollowerSets[j][j1],utilNomFollower[i])
                # frobenius norm
                out[somCount][i] = np.linalg.norm(matDiff, ord='fro')
                # update counter
                somCount += 1
            
    return out

"""
This function computes total number of follower utility functions, given
the follower utlity sets E^f_{tau,j}. 
Parameters:
utilFollower: a three dimensional matrix of k follower utility function sets
k: number of nominal functions
output: integer, cardinality of E^f_{tau}
"""
def TotalSizFollUtil(utilFollower,k):
    out = 0
    # for each j
    for j in range(k):
        # number of utility functions in E^f_{tau,j}
        out += len(utilFollower[j])
            
    return out

"""
Post processing MIP
This function extracts the optimal variables from the Gurobi solution of MIP
Parameters:
optVariab: Gurobi optimal variable formatted as list of tuples (variable name, value)
sizLeaderAction: number of leader actions
sizFollAction: number of follower actions
sizFollFunc: total number of follower functions at this iteration
sizNominal: number of nominal functions
output: x*, w*, lambda*, delta*
"""
def PostProcessMIP(optVariab,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal):
    # counter
    somCount = 0
    
    # output optimal x
    xOpt = []
    # extract optimal x from optVariab
    for i in range(sizLeaderAction):
        xOpt.append(optVariab[i][1])
        somCount += 1
    # extract optimal lambda
    lambdaOpt = optVariab[somCount][1]
    somCount += 1
    
    # extract optimal w
    wOpt = []
    for i in range(sizNominal):
        wOpt.append(optVariab[somCount][1])
        somCount += 1
    
    # extract optimal delta   
    deltOpt = []
    for i in range(sizFollAction*sizFollFunc):
        deltOpt.append(optVariab[somCount][1])
        somCount += 1
        
    return xOpt,lambdaOpt,wOpt,deltOpt

"""
Post processing subproblem
This function extracts the optimal variables from the Gurobi solution of subproblem
Parameters:
optVariab: Gurobi optimal variable formatted as list of tuples (variable name, value)
sizLeaderAction: number of leader actions
sizFollAction: number of follower actions
sizFollFunc: total number of follower functions at this iteration
sizNominal: number of nominal functions
output: uf*
"""
def PostProcessSubProblem(optVariab,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal):
    
    uf = make_zeros(sizLeaderAction,sizFollAction)
    
    somCount = 1 # counter
    # for each a_l
    for j1 in range(sizLeaderAction):
        # for each a_f
        for j2 in range(sizFollAction):
            # add uf(al,af) variable to the model
            uf[j1][j2] = optVariab[somCount][1]
            somCount += 1
    
    return uf


###############################################################################
"""
Functions for different games- Cournot, Inspection and synthetic data 
"""

"""
This function generates utility functions for the Cournot Duopoly game
Parameters:
n: number of leader actions
m: number of follower actions
k: number of follower utility functions
output: leader and follower utility functions for the Cournot Duopoly game
"""    
def CournotUtilities(n,m,k):

    # move to the GAMUT folder
    os.chdir('gamut/edu/stanford/multiagent/gamer/')

    # set the number of actions
    str_act = '-actions '+str(n)+' '
    
    # Input inverse demand function function coefficients
    P0 = int(75)  # constant term
    P1 = -np.random.randint(1,10) # linear coefficient
    # inverse demand function is P0 - P1(y1 + y2)
    str_Pfunc = '-p_func PolyFunction -p_params [-degree 1 -coefs '+str(P0)+' '+str(P1)+'] '
    
    # Make Player 1 cost function coefficients- C1
    C1_0 = np.random.randint(10,40)  # constant term
    C1_1 = np.random.randint(10,20) # linear coefficient
    # inverse demand function is C1_0 + C1_1*y1
    str_cost1 = '-cost_func1 IncreasingPoly -cost_params1 [-degree 1 -coefs '+str(C1_0)+' '+str(C1_1)+'] '
    
    # other strings to run the game
    # define the game
    str_gamedef = 'java -jar gamut.jar -g CournotDuopoly ' 
    # normalize the game
    str_normalize = '-normalize -min_payoff 0 -max_payoff 1 '
    # store the output
    str_out = '-f CDuo.game '
    
    # initialize utilities
    utilLeader = np.zeros((n,m))
    utilFollower = np.zeros((n,m,k))
    
    for index in range(k):
        
        os.remove('CDuo.game')
        # Make Player 2 cost function coefficients- C2
        C2_0 = np.random.randint(2,20)  # constant term
        C2_1 = np.random.randint(1,15) # linear coefficient
        # inverse demand function is C2_0 + C2_1*y2
        str_cost2 = '-cost_func2 IncreasingPoly -cost_params2 [-degree 1 -coefs '+str(C2_0)+' '+str(C2_1)+'] '

        # Run the game in GAMUT
        os.system(str_gamedef+str_normalize+str_out+str_act+str_Pfunc+str_cost1+str_cost2)

        # Using readlines() 
        file1 = open('CDuo.game', 'r') 
        Lines = file1.readlines() 
        
        # read the file generated by Cournot and extract utilities
        linecount = 14
        i = 0
        j = 0 
        for j in range(m):
            for i in range(n):
            
                line = Lines[linecount]
                
                remStr = line.partition('[ ')[2] 
                remStr = remStr.rpartition(']')[0] 
                
                remList = remStr.split(' ')
                
                utilLeader[i,j] = float(remList[0])
                utilFollower[i,j,index] = float(remList[1])
                
                linecount += 1
         
    # come back to main folder
    os.chdir('..')
    os.chdir('..')
    os.chdir('..')
    os.chdir('..')
    os.chdir('..')
                
    return utilLeader, utilFollower


"""
This function generates utility functions for the Simple Inspection Game
Parameters:
s: size of set S in the game
p: maximum size of leader set
q: maximum size of follower set
k: number of nominal follower utility functions
gameLossCoeffs_Nominal: loss payoff for each nominal
gameWinCoeffs_Nominal: win payoff for each nominal
output: leader and follower utility functions for the Inspection game
""" 
def InspectionUtilities2022(s,p,q,k,gameLossCoeffs_Nominal,gameWinCoeffs_Nominal):

    # move to the GAMUT folder
    os.chdir('gamut/edu/stanford/multiagent/gamer/')

    # set the number of actions
    str_params = '-set_size '+str(s)+' -max_r '+str(p)+' -max_b '+str(q)
    
    # other strings to run the game in GAMUT
    # define the game
    str_gamedef = 'java -jar gamut.jar -g SimpleInspectionGame ' 
    # normalize the game
    str_normalize = '-normalize -min_payoff 0 -max_payoff 1 '
    # store the output
    str_out = '-f SIG.game '
    
    # size of leader and follower action spaces
    n = InspectionActionSize(s,p)
    m = InspectionActionSize(s,q)
    
    # initialize output utilities
    utilLeader = np.zeros((n,m))
    utilFollower = np.zeros((n,m,k))
    
    # alpha and beta for the inspection game
    lowPayoffArr = gameLossCoeffs_Nominal.tolist()
    highPayoffArr = gameWinCoeffs_Nominal.tolist()
    
    for index in range(k):
        
        # Run the game in GAMUT
        os.system(str_gamedef+str_normalize+str_out+str_params)

        # Using readlines() 
        file1 = open('SIG.game', 'r') 
        Lines = file1.readlines() 
        
        # read the GAMUT output
        linecount = 10
        i = 0
        j = 0 
        for j in range(m):
            for i in range(n):
            
                line = Lines[linecount]
                
                remStr = line.partition('[ ')[2] 
                remStr = remStr.rpartition(']')[0] 
                
                remList = remStr.split(' ')
                
                utilLeader[i,j] = float(remList[0])
                
                follPayoff = float(remList[1])

                if follPayoff == 0.5:
                    follPayoff = lowPayoffArr[index]
                
                elif follPayoff == 1:
                    follPayoff = highPayoffArr[index]
                    
                utilFollower[i,j,index] = follPayoff
                
                
                linecount += 1
                
        os.remove('SIG.game')
     
    # come back to main folder          
    os.chdir('..')
    os.chdir('..')
    os.chdir('..')
    os.chdir('..')
    os.chdir('..')
                
    return utilLeader, utilFollower



"""
This function generates (synthetic) random utility functions (values in [0,1]) 
and a random nominal distribution, given n,m and k.
Parameters:
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizFollFunc: k, number of follower utility functions
output: random leader and follower utilities, and a random nominal distribution
"""
def utilAndnu(sizLeaderAction,sizFollAction,sizFollFunc):

    # Generating random leader utility
    utilLeader = np.random.rand(sizLeaderAction,sizFollAction) 
    # Random follower utilities, third dimension is for each of the k functions
    utilFollower = np.random.rand(sizLeaderAction,sizFollAction,sizFollFunc) 
    
    # nominal distribution, random probability vector
    nu = np.random.random(sizFollFunc)
    nu /= nu.sum()
    
    return utilLeader,utilFollower,nu

"""
This function finds the subset of follower actions B_x(af) for the
subproblem constraint
sizFollAction: m, number of follower actions
utilLeader: leader utility function
somex: some x 
someFollAction: foll action a for which we want to find B_x(af)
output: optVal is the optimal value of the Bayesian Stackelberg, out is the run time,
and time_terminate is an indicator if the MIP timed out at the threshold
"""
def FindBxaf(utilLeader,sizFollAction,somex,someFollAction):
    
    # store output, subset of follower actions
    out = []
    # store ul(x,a)
    rhs = MixedStrategyUtilityOneAction(utilLeader,somex,someFollAction,'L')
    
    # for each af' in Af
    for i in range(sizFollAction):

        lhs = MixedStrategyUtilityOneAction(utilLeader,somex,i,'L')
        
        # if strict inequality
        if lhs > rhs:
            # add the index to output
            out += [i]
            
    return out


###############################################################################
"""
Functions that run math programs
"""

"""
arxiv-22 Main Body Functions
"""
"""
This function runs DR MIP (12) in the main body for any Ef
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizFollFunc: total number of follower utility functions in current iteration
sizNominal: k, number of nominal follower function sets, aka E^f(tau,j)
tau: iteration number
tWass: Wasserstein exponent t
theta: Wasserstein radius theta
M: big M in the MIP
timeLim: threshold for the MIP run time
utilLeader: leader utility function
utilFollowerSets: k sets of follower utility functions
utilNomFollower: the k hat{u}_fj nominal follower utilities
nu: nominal distribution
output: optVal is the optimal value of the MIP, out is the run time of the MIP
and time_terminate is an indicator if the MIP timed out at the threshold
"""
def runMIP3(sizLeaderAction,sizFollAction,sizFollFunc,sizNominal,EfTaujSizes,tau,tWass,theta,M,timeLim,utilLeader,utilFollowerSets,utilNomFollower,nu):

    # Create a new Gurobi MIP model
    m = gp.Model("mip1")
    
    # set time limit
    m.setParam(GRB.Param.TimeLimit, timeLim)
    
    # Create variables
    # the variable x, continuous
    x = [0 for i in range(sizLeaderAction)] 
    for i in range(sizLeaderAction):
        x[i] = m.addVar(vtype=GRB.CONTINUOUS,lb=0,ub=1,name="x_%g" % i)
        
    # the variable lambda, continuous   
    lambd = m.addVar(vtype=GRB.CONTINUOUS,lb=0,name="lambda")
    
    # the variable w, continuous
    w = [0 for i in range(sizNominal)]
    for i in range(sizNominal):
        w[i] = m.addVar(vtype=GRB.CONTINUOUS,lb=-GRB.INFINITY,name="w_%d" % i)
                
    # the variable delt, binary
    delt= make_zeros(sizFollAction,sizFollFunc)
        
    # for each action af
    for i in range(sizFollAction):
        # counter
        somCount = 0
        # for each j=1,...,k
        for j in range(sizNominal):
            
            # for each j in E^f(tau,j)
            for j2 in range(EfTaujSizes[j]):
            
                delt[i][somCount] = m.addVar(vtype=GRB.BINARY,name="delt_%d%d%d" % (i,j,j2))
                somCount += 1 # update counter
    
    # Set objective
    # One term in the objective, theta^t
    expTerm = theta**tWass 
    # nu^T * w
    expr = LinExpr(nu,w) 
    expr2 = [nu[j]*w[j] for j in range(sizNominal)]
    expr2 = sum(expr2)
    # objective
    obj = lambd*expTerm - expr 
    m.setObjective(obj, GRB.MINIMIZE)
    
    # Add constraint: u_f(x,a_f) >= u_f(x,a_f') + M(delt_{a_f,u_f}-1)
    somCount = 0 # counter
    for j in range(sizNominal): # for each j=1,...,k
        for j1 in range(EfTaujSizes[j]): # for each uf in Ef(tau,j)
        
            someUfFunction = utilFollowerSets[j][j1]
            
            for j2 in range(sizFollAction): # for each a_f
                for j3 in range(sizFollAction): # for each a_f'
                    # u_f(x,a_f)
                    lhsTerm = MixedStrategyUtilityOneAction(someUfFunction,x,j2,'F')
                    # u_f(x,a_f') 
                    rhsTerm1 = MixedStrategyUtilityOneAction(someUfFunction,x,j3,'F')
                    # M(delt_{a_f,u_f}-1)
                    rhsTerm2 = M*(delt[j2][somCount]-1)
                    
                    # add first constraint
                    m.addConstr(lhsTerm >= rhsTerm1 + rhsTerm2)
            somCount += 1 # update counter       
    # compute matrix of frobenius norm differences of follower utilities
    frobMat = FrobDiffNomMatrix(utilFollowerSets,utilNomFollower,sizNominal,sizFollFunc,EfTaujSizes)
    
    # Add constraint: w_j <= M(1-delt_{a_f,u_f}) + lambd*d^t(u_f,hat{u}_fj) + u_l(x,a_f)
    somCount = 0 # counter
    for j in range(sizNominal): # for each j=1,...,k
        # u_f(x,a_f)
        lhsTerm = w[j]
        for j2 in range(EfTaujSizes[j]): # for each u_f in EfTauj
            
            for j3 in range(sizFollAction): # for each a_f
                
                # M(1-delt_{a_f,u_f})
                rhsTerm1 = M*(1-delt[j3][somCount])
                # lambd*d^t(u_f,u_fj)
                rhsTerm2 = lambd*((frobMat[somCount][j])**tWass)
                # u_l(x,a_f') 
                rhsTerm3 = MixedStrategyUtilityOneAction(utilLeader,x,j3,'L')
                # add second constraint
                m.addConstr(lhsTerm <= rhsTerm1 + rhsTerm2 + rhsTerm3, "c2")
                
            somCount += 1 
            
            
    # Add constraint: sum_{a_f} y(a_f,u_f) = 1
    for j in range(sizFollFunc):
        m.addConstr(np.sum([delt[j2][j] for j2 in range(sizFollAction)])==1.0,"c3")
    
    # Add constraint: x is a probability vector, its elements sum to 1
    m.addConstr(np.sum(x) == 1.0, "c4")    
    
    # initialize output variables
    out = 0 # output, the runtime
    time_terminate = 0 # indicator for exceeding time threshold
    optVal = 0 # optimal value
    optVariab = [] # optimal variables
    
    # Optimize model
    m.optimize()
    
    # run time
    out = m.RunTime
    # if we have an optimal solution
    if m.status == GRB.OPTIMAL:
        # record the optimal solution
        optVal = m.objVal
        # store optimal variables
        optVariab = [(v.varName, v.X) for v in m.getVars()]
    
    # if the given time limit is exceeded
    if m.status==GRB.TIME_LIMIT:
        # set indicator variable to 1
        time_terminate = 1
         
    
    return optVariab,optVal,out,time_terminate

"""
This function runs subproblem in Algorithm 1 for any Ef
whichNom: which nominal distribution index j we want to do subproblem for
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizFollFunc: total number of follower utility functions in current iteration
sizNominal: k, number of nominal follower function sets, aka E^f(tau,j)
tau: iteration number
tWass: Wasserstein exponent t
timeLim: threshold for the MIP run time
utilLeader: leader utility function
utilFollowerSets: k sets of follower utility functions
utilNomFollower: the k hat{u}_fj nominal follower utilities
xTau: x_tau
lambdaTau: lambda_tau
wTau: w_tau
output: optVal is the optimal value of the MIP, out is the run time of the MIP
and time_terminate is an indicator if the MIP timed out at the threshold
"""
def runSubProblem(whichNom,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal,tau,tWass,timeLim,utilLeader,
            utilFollowerSets,utilNomFollower,xTau,lambdaTau,wTau):
    
    # initialize output variables
    out = 0 # output, the runtime
    optVal = 0 # optimal value    
    optUf = make_zeros(sizLeaderAction,sizFollAction) # optimal Uf
    
    optVariab = np.zeros((sizLeaderAction,sizFollAction,sizFollAction)) # optimal Uf for each af
    
    eachInf = [10e2]*sizFollAction # store all the inner inf problem solutions aka oracle
    
    # for each af in Af
    for i in range(sizFollAction):
        
        # hatuf_i
        hatufi = utilNomFollower[whichNom]
        # Create a new Gurobi MIP model
        m = gp.Model("qp")
        
        # set time limit
        m.setParam(GRB.Param.TimeLimit, timeLim)
        
        # now add the variable, uf matrix
        uf = make_zeros(sizLeaderAction,sizFollAction)
        
        # aux variable ot calculate frobenius norm
        nx = make_zeros(sizLeaderAction,sizFollAction)
        
        dtTerm = m.addVar(vtype=GRB.CONTINUOUS,lb=0,name="dtTerm")
        
        # for each a_l
        for j1 in range(sizLeaderAction):
            # for each a_f
            for j2 in range(sizFollAction):
                # add uf(al,af) variable to the model
                uf[j1][j2] = m.addVar(vtype=GRB.CONTINUOUS,lb=0,ub=1,name="uf_%g%g" % (j1,j2))
                
                # for each a_l
        for j1 in range(sizLeaderAction):
            # for each a_f
            for j2 in range(sizFollAction):
                # aux variables, one for each uf(j1,j2)
                nx[j1][j2] = m.addVar(vtype=GRB.CONTINUOUS,lb=0,ub=1,name="nx_%g%g" % (j1,j2))

                m.addConstr(nx[j1][j2] == (uf[j1][j2] - hatufi[j1][j2]) ,"aux frobenius constraint")
                
                dtTerm += (nx[j1][j2])**2

        
        tWass2 = tWass/2
        """
        rhs1 = lambdaTau*((dtTerm)**tWass2)
        We have assumed tWass = 2 here.
        """
        rhs1 = lambdaTau*(dtTerm)
        # u_l(x,a_f) 
        rhs2 = MixedStrategyUtilityOneAction(utilLeader,xTau,i,'L')
        # objective function
        obj = rhs1 + rhs2
        
        m.setObjective(obj, GRB.MINIMIZE)
        
     
        # Add constraint: u_f(x,a_f) >= u_f(x,a_f')
        # u_f(x,a_f)
        ufCol = [uf[j4][i] for j4 in range(sizLeaderAction)]
        lhsTerm = LinExpr(xTau,ufCol)
        
        # Find B_x(af) 
        bxafSet = FindBxaf(utilLeader,sizFollAction,xTau,i)    
        
        for j3 in range(sizFollAction): # for each a_f'
            # u_f(x,a_f') 
            ufColj3 = [uf[j4][j3] for j4 in range(sizLeaderAction)]
            rhsTerm = LinExpr(xTau,ufColj3)
            # if action is in Bxaf
            if j3 in bxafSet:
                
                # add BR constraint, strict
                m.addConstr((lhsTerm)  >= (rhsTerm) +1e-5 , "1nd constraint strong SSE %d %d" % (i,j3))    
            
            else:
                # add BR constraint, strict
                m.addConstr((lhsTerm)  >= (rhsTerm) , "Regular BR constraint %d %d" % (i,j3))
            
        
        # initialize output variables
        out2 = 0 # output, the runtime
        time_terminate2 = 0 # indicator for exceeding time threshold
        optVariab2 = [] # optimal variables
        
        # Optimize model
        m.write("subproblem.lp")
        m.optimize()
        
        # run time
        out2 = m.RunTime
        # if we have an optimal solution
        if m.status == GRB.OPTIMAL:
            # record the optimal solution
            eachInf[i] = m.objVal
            # store optimal variables
            optVariab2 = [(v.varName, v.X) for v in m.getVars()]
        
        # if the given time limit is exceeded
        if m.status==GRB.TIME_LIMIT:
            # set indicator variable to 1
            time_terminate2 = 1
                    
        # store all the optimizing uf for each inner inf problem
        optVariab[:,:,i] = PostProcessSubProblem(optVariab2,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal)
     
        # add to runtime
        out += out2
        
    # this is Gamma(tau,whichNom)
    optVal = min(eachInf)
    # some af so that min happens
    someIndex = eachInf.index(optVal)
    
    # get the uf that minimizes
    optUf = optVariab[:,:,someIndex]
    
    return optVal,optUf,out



"""
This function runs Algorithm 1 
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizNominal: k, number of nominal follower function sets, aka E^f(tau,j)
tWass: Wasserstein exponent t
theta: Wasserstein constsnt 
M: big M constant
timeLim: threshold for the MIP run time
utilLeader: leader utility function
utilNomFollower: the k hat{u}_fj nominal follower utilities
nu: nominal distribution
maxIter: upper limit on number of iterations

output: optVal is the optimal value of Algorithm 1, out is the run time
and time_terminate is an indicator if the MIP timed out at the threshold
"""
def Algorithm1(sizLeaderAction,sizFollAction,sizNominal,tWass,theta,M,timeLim,utilLeader,utilNomFollower,nu,maxIter):
    
    #initialize iteration
    tau = 1
    
    EfTaujSizes = [1]*sizNominal
    sizFollFunc = sum(EfTaujSizes)
    # initialize Et_tauj sets
    utilFollowerSets = {}
    for j in range(sizNominal):
        utilFollowerSets[j] = {}
        for j2 in range(EfTaujSizes[j]):
            utilFollowerSets[j][j2] = utilNomFollower[j]
    
            
    # termination criterion
    Gamma = -0.1 # initialize to some value less than zero
    
    # total run time of this algo
    runTimeAlg1 = 0
    
    storeGamma = []
    storeSizes = {}
    # as long as gamma is sufficiently negative, and max iterations is not reached
    while (Gamma < -1e-2 and tau<maxIter):
        Gamma = 0
        
        # total number of follower utilities under consideration
        sizFollFunc = sum(EfTaujSizes)

        # Run the MIP
        optVariabMIPx,optValMIPx,runTimesMIPx,timeTerminateMIPx = runMIP3(sizLeaderAction,sizFollAction,
                                sizFollFunc,sizNominal,EfTaujSizes,tau,tWass,theta,M,timeLim,utilLeader,
                                utilFollowerSets,utilNomFollower,nu)
        
        # post process the MIP
        xOpt,lambdaOpt,wOpt,deltOpt = PostProcessMIP(optVariabMIPx,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal)

        # run the subproblem
        xTau,lambdaTau,wTau = xOpt,lambdaOpt,wOpt
        allGammas = [0]*sizNominal # store all gammas
        
        for j in range(sizNominal):
            # compute optimal values of subproblem
            optVal,optUf,out = runSubProblem(j,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal,tau,tWass,timeLim,utilLeader,
                                     utilFollowerSets,utilNomFollower,xTau,lambdaTau,wTau)
            # if optimal value < wj
            if optVal < wTau[j]:
                # update Ef_{tau} set size
                currEfTauj = EfTaujSizes[j]
                utilFollowerSets[j][currEfTauj] = optUf
                EfTaujSizes[j] += 1
            # store all gamma-wj
            allGammas[j] = optVal - wTau[j]
        # min value of gamma-wj
        Gamma = min(allGammas)
        
        # store all the gamma values
        storeGamma += [Gamma]
        # store number of iterations
        storeSizes[tau] = EfTaujSizes
        # update runtime
        runTimeAlg1 += runTimesMIPx + out
        
        tau += 1
        
    # store output 
    xStar,lambdaStar,wStar,deltStar, optStar = xOpt,lambdaOpt,wOpt,deltOpt,optValMIPx
    
    return xStar,lambdaStar,wStar,deltStar,optStar, runTimeAlg1,tau, storeGamma, storeSizes
    

"""
This function runs Algorithm 1 for the Inspection game
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizNominal: k, number of nominal follower function sets, aka E^f(tau,j)
tWass: Wasserstein exponent t
theta: Wasserstein constsnt 
M: big M constant
timeLim: threshold for the MIP run time
utilLeader: leader utility function
utilNomFollower: the k hat{u}_fj nominal follower utilities
nu: nominal distribution
maxIter: upper limit on number of iterations

output: optVal is the optimal value of Algorithm 1, out is the run time
and time_terminate is an indicator if the MIP timed out at the threshold
"""
def Algorithm1Inspection(sizLeaderAction,sizFollAction,sizNominal,tWass,theta,M,timeLim,utilLeader,utilNomFollower,nu,maxIter):
    
    #initialize iteration
    tau = 1
    
    EfTaujSizes = [1]*sizNominal
    sizFollFunc = sum(EfTaujSizes)
    # initialize Et_tauj sets
    utilFollowerSets = {}
    for j in range(sizNominal):
        utilFollowerSets[j] = {}
        # initialize Ef_{tau} to be just the nominal distribution
        for j2 in range(EfTaujSizes[j]):
            utilFollowerSets[j][j2] = utilNomFollower[j]
    
    # termination criterion
    Gamma = -0.1 # initialize to some value less than zero
    
    # total run time of this algo
    runTimeAlg1 = 0
    
    storeGamma = []
    storeSizes = {}

    while (Gamma < -1e-2 and tau<maxIter):
        Gamma = 0
        
        # total number of follower utilities under consideration
        sizFollFunc = sum(EfTaujSizes)

        # Run the MIP
        optVariabMIPx,optValMIPx,runTimesMIPx,timeTerminateMIPx = runMIP3(sizLeaderAction,sizFollAction,
                                sizFollFunc,sizNominal,EfTaujSizes,tau,tWass,theta,M,timeLim,utilLeader,
                                utilFollowerSets,utilNomFollower,nu)
        
        # post process the MIP
        xOpt,lambdaOpt,wOpt,deltOpt = PostProcessMIP(optVariabMIPx,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal)

        # run the subproblem
        xTau,lambdaTau,wTau = xOpt,lambdaOpt,wOpt
        allGammas = [0]*sizNominal # store all gammas
        #subproblem
        for j in range(sizNominal):
            optVal,optUf,out = runSubProblemInspection(j,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal,tau,tWass,timeLim,utilLeader,
                                     utilFollowerSets,utilNomFollower,xTau,lambdaTau,wTau)
            # if optval<wj
            if optVal < wTau[j]:
                currEfTauj = EfTaujSizes[j]
                utilFollowerSets[j][currEfTauj] = optUf
                EfTaujSizes[j] += 1
            # store gamma-wj 
            allGammas[j] = optVal - wTau[j]
                
        Gamma = min(allGammas)
        
        storeGamma += [Gamma]
        storeSizes[tau] = EfTaujSizes
        runTimeAlg1 += runTimesMIPx + out
        # update iteration
        tau += 1
    
    # store optimal value            
    xStar,lambdaStar,wStar,deltStar, optStar = xOpt,lambdaOpt,wOpt,deltOpt,optValMIPx
    
    return xStar,lambdaStar,wStar,deltStar,optStar, runTimeAlg1,tau, storeGamma, storeSizes


"""
This function runs subproblem in Algorithm 1 for Inspection game
whichNom: which nominal distribution index j we want to do subproblem for
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizFollFunc: total number of follower utility functions in current iteration
sizNominal: k, number of nominal follower function sets, aka E^f(tau,j)
tau: iteration number
tWass: Wasserstein exponent t
timeLim: threshold for the MIP run time
utilLeader: leader utility function
utilFollowerSets: k sets of follower utility functions
utilNomFollower: the k hat{u}_fj nominal follower utilities
xTau: x_tau
lambdaTau: lambda_tau
wTau: w_tau
output: optVal is the optimal value of the MIP, out is the run time of the MIP
and time_terminate is an indicator if the MIP timed out at the threshold
"""
def runSubProblemInspection(whichNom,sizLeaderAction,sizFollAction,sizFollFunc,sizNominal,tau,tWass,timeLim,utilLeader,
            utilFollowerSets,utilNomFollower,xTau,lambdaTau,wTau):
    
    # initialize output variables
    out = 0 # output, the runtime
    optVal = 0 # optimal value    
    optUf = make_zeros(sizLeaderAction,sizFollAction) # optimal Uf
    
    optVariab = np.zeros((sizLeaderAction,sizFollAction,sizFollAction)) # optimal Uf for each af
    
    eachInf = [10e2]*sizFollAction # store all the inner inf problem solutions aka oracle
    
    # for each af in Af
    for i in range(sizFollAction):
        
        # hatuf_i
        hatufi = utilNomFollower[whichNom]
        
        low_hatufi = hatufi[0][0]
        high_hatufi = hatufi[0][1]
        # Create a new Gurobi MIP model
        m = gp.Model("qp")
        
        # set time limit
        m.setParam(GRB.Param.TimeLimit, timeLim)
        
        # number of entries of the matrix with value beta and alpha 
        nnzCount = np.count_nonzero(hatufi-low_hatufi)
        zCount = sizLeaderAction*sizFollAction - nnzCount
                
        tWass2 = tWass/2
        
        """
        Special uf constraint for Inspection Game
        """
        skeleton = utilNomFollower[0]
        lowVal = skeleton[0,0]
        
        ar = (skeleton - lowVal)
        someVal = ar[0,1]
        
        #this is the skeleton for the Inspection game, 1=highvalue, 0=lowvalue
        skeleton2 = ar / someVal
        
        lowPayoff = m.addVar(vtype=GRB.CONTINUOUS,lb=0.3,ub=0.6,name="lowPayoff")
        highPayoff = m.addVar(vtype=GRB.CONTINUOUS,lb=0.7,ub=1,name="highPayoff")

        rhs1 = lambdaTau*(zCount*(lowPayoff-low_hatufi)**2 + nnzCount*(highPayoff-high_hatufi)**2)
        # u_l(x,a_f) 
        rhs2 = MixedStrategyUtilityOneAction(utilLeader,xTau,i,'L')
        
        obj = rhs1 + rhs2
        
        m.setObjective(obj, GRB.MINIMIZE)
    
        # Add constraint: u_f(x,a_f) >= u_f(x,a_f')
        # u_f(x,a_f)
        temp1 = (xTau)@(skeleton2[:,i])
        lhsTerm = lowPayoff*sum(xTau) + (highPayoff-lowPayoff)*temp1
        
        # Find B_x(af) 
        bxafSet = FindBxaf(utilLeader,sizFollAction,xTau,i)    
        
        for j3 in range(sizFollAction): # for each a_f'
            # u_f(x,a_f') 
            temp2 = (xTau)@(skeleton2[:,j3])
            
            rhsTerm = lowPayoff*sum(xTau) + (highPayoff-lowPayoff)*temp2
            
            if j3 in bxafSet:
            
                # add BR constraint
                m.addConstr((lhsTerm)  >= (rhsTerm) + 1e-3 , "1nd constraint strong SSE %d %d" % (i,j3))
                
            else:
                    
                # add BR constraint
                m.addConstr((lhsTerm)  >= (rhsTerm) , "Regular BR constraint %d %d" % (i,j3))
         
             
        # initialize output variables
        out2 = 0 # output, the runtime
        time_terminate2 = 0 # indicator for exceeding time threshold
        optVariab2 = [] # optimal variables
        
        # Optimize model
        m.write("subproblem.lp")
        m.optimize()
        
        # run time
        out2 = m.RunTime
        # if we have an optimal solution
        if m.status == GRB.OPTIMAL:
            # record the optimal solution
            eachInf[i] = m.objVal
            # store optimal variables
            optVariab2 = [(v.varName, v.X) for v in m.getVars()]
            
            # store all the optimizing uf for each inner inf problem
            optVariab[:,:,i] = PostProcessSubProblemInspection(optVariab2,sizLeaderAction,sizFollAction,skeleton2)
     
        
        # if the given time limit is exceeded
        if m.status==GRB.TIME_LIMIT:
            # set indicator variable to 1
            time_terminate2 = 1
        
        # add to runtime
        out += out2
        
    # this is Gamma(tau,whichNom)
    optVal = min(eachInf)
    # some af so that min happens
    someIndex = eachInf.index(optVal)
    
    # get the uf that minimizes
    optUf = optVariab[:,:,someIndex]
    
    return optVal,optUf,out

"""
Post processing for the subproblem in Inspection Game
This function extracts the optimal variables from the Gurobi solution of subproblem
Parameters:
optVariab: Gurobi optimal variable formatted as list of tuples (variable name, value)
sizLeaderAction: number of leader actions
sizFollAction: number of follower actions
sizFollFunc: total number of follower functions at this iteration
sizNominal: number of nominal functions
output: uf*
"""

def PostProcessSubProblemInspection(optVariab,sizLeaderAction,sizFollAction,skeleton2):
    
    uf = make_zeros(sizLeaderAction,sizFollAction)
        
    low = optVariab[0][1]
    high = optVariab[1][1]
    # for each a_l
    for j1 in range(sizLeaderAction):
        # for each a_f
        for j2 in range(sizFollAction):
            # add uf(al,af) variable to the model
        
            uf[j1][j2] = ((skeleton2[j1][j2])*(high-low) + low)
    
    return uf

"""
Functions needed for Appendix graphs
"""

"""
This function runs the DR MIP (A.5) in the appendix for the finite utility case
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizFollFunc: k, number of follwoer functions
tWass: Wasserstein exponent t
theta: Wasserstein radius theta
M: big M in the MIP
timeLim: threshold for the MIP run time
utilLeader: leader utility function
utilFollower: k follower utility functions
nu: nominal distribution
output: optVal is the optimal value of the MIP, out is the run time of the MIP
and time_terminate is an indicator if the MIP timed out at the threshold
"""
def runMIP2(sizLeaderAction,sizFollAction,sizFollFunc,tWass,theta,M,timeLim,utilLeader,utilFollower,nu):

    # Create a new Gurobi MIP model
    m = gp.Model("mip1")
    
    # set time limit
    m.setParam(GRB.Param.TimeLimit, timeLim)

    
    # Create variables
    # the variable x, continuous
    x = [0 for i in range(sizLeaderAction)] 
    for i in range(sizLeaderAction):
        x[i] = m.addVar(vtype=GRB.CONTINUOUS,lb=0,ub=1,name="x_%g" % i)
        
    # the variable lambda, continuous   
    lambd = m.addVar(vtype=GRB.CONTINUOUS,lb=0,name="lambda")
    
    # the variable w, continuous
    w = [0 for i in range(sizFollFunc)]
    for i in range(sizFollFunc):
        w[i] = m.addVar(vtype=GRB.CONTINUOUS,lb=-GRB.INFINITY,name="w_%d" % i)
        
    # the variable y, binary
    y= make_zeros(sizFollAction, sizFollFunc)
    for i in range(sizFollAction):
        for j in range(sizFollFunc):
            y[i][j] = m.addVar(vtype=GRB.BINARY,name="y_%d%d" % (i,j))
    
    # Set objective
    # One term in the objective, theta^t
    expTerm = theta**tWass 
    # nu^T * w
    expr = LinExpr(nu,w) 
    # objective
    obj = lambd*expTerm - expr 
    m.setObjective(obj, GRB.MINIMIZE)
    
    # Add constraint: u_f(x,a_f) >= u_f(x,a_f') + M(y_{a_f,u_f}-1)
    for j1 in range(sizFollFunc): # for each u_f
        for j2 in range(sizFollAction): # for each a_f
            for j3 in range(sizFollAction): # for each a_f'
                # u_f(x,a_f)
                lhsTerm = MixedStrategyUtilityOneAction(utilFollower[:,:,j1],x,j2,'F')
                # u_f(x,a_f') 
                rhsTerm1 = MixedStrategyUtilityOneAction(utilFollower[:,:,j1],x,j3,'F')
                # M(y_{a_f,u_f}-1)
                rhsTerm2 = M*(y[j2][j1]-1)
                # add first constraint
                m.addConstr(lhsTerm >= rhsTerm1 + rhsTerm2, "c1")
    
    # compute matrix of frobenius norm differences of follower utilities
    frobMat = FrobDiffMatrix(utilFollower,sizFollFunc)
    
    # Add constraint: w_j <= M(1-y_{a_f,u_f}) + lambd*d^t(u_f,u_fj) + u_l(x,a_f)
    for j1 in range(sizFollFunc): # for each j, as in u_fj
        for j2 in range(sizFollFunc): # for each u_f
            for j3 in range(sizFollAction): # for each a_f
                # u_f(x,a_f)
                lhsTerm = w[j1]
                # M(1-y_{a_f,u_f})
                rhsTerm1 = M*(1-y[j3][j2])
                # lambd*d^t(u_f,u_fj)
                rhsTerm2 = lambd*((frobMat[j1][j2])**tWass)
                # u_l(x,a_f') 
                rhsTerm3 = MixedStrategyUtilityOneAction(utilLeader,x,j3,'L')
                # add second constraint
                m.addConstr(lhsTerm <= rhsTerm1 + rhsTerm2 + rhsTerm3, "c2")
                
    # Add constraint: sum_{a_f} y(a_f,u_f) = 1
    for j in range(sizFollFunc):
        m.addConstr(np.sum([y[j2][j] for j2 in range(sizFollAction)])==1,"c3")
    
    # Add constraint: x is a probability vector, its elements sum to 1
    m.addConstr(np.sum(x) == 1, "c4")    
    
    # initialize output variables
    out = 0 # output, the runtime
    time_terminate = 0 # indicator for exceeding time threshold
    optVal = 0 # optimal value
    
    # Optimize model
    m.optimize()
    
    # run time
    out = m.RunTime
    # if we have an optimal solution
    if m.status == GRB.OPTIMAL:
        # record the optimal solution
        optVal = m.objVal
    
    # if the given time limit is exceeded
    if m.status==GRB.TIME_LIMIT:
        # set indicator variable to 1
        time_terminate = 1
    
    return optVal,out,time_terminate


"""
This function runs the Baseline LP (A.6) in the appendix, given a value of y (or z)
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizFollFunc: k, number of follwoer functions
tWass: Wasserstein exponent t
theta: Wasserstein radius theta
M: big M in the MIP
timeLim: threshold for the Baseline LP run time
utilLeader: leader utility function
utilFollower: k follower utility functions
nu: nominal distribution
y: Given a y 
frobMat: matrix of frobenius norm differences of follower utilities
output: optVal is the optimal value of the Baseline LP, out is the run time,
and time_terminate is an indicator if the Baseline LP timed out at the threshold
"""
def BaselineLP(sizLeaderAction,sizFollAction,sizFollFunc,tWass,theta,M,timeLim,utilLeader,utilFollower,nu,y,frobMat):
    
    # Create a new Gurobi model
    m = gp.Model()
    
    # set time limit
    m.setParam(GRB.Param.TimeLimit, timeLim)
    
    # Create variables
    # the variable x, continuous
    x = [0 for i in range(sizLeaderAction)] 
    for i in range(sizLeaderAction):
        x[i] = m.addVar(vtype=GRB.CONTINUOUS,lb=0,ub=1,name="x_%g" % i)
        
    # the variable lambda, continuous   
    lambd = m.addVar(vtype=GRB.CONTINUOUS,lb=0,name="lambda")

    # the variable w, continuous
    w = [0 for i in range(sizFollFunc)]
    for i in range(sizFollFunc):
        w[i] = m.addVar(vtype=GRB.CONTINUOUS,lb=-GRB.INFINITY,name="w_%d" % i)
        
    #updste the model      
    m.update()                
    
    # Set objective
    # One term in the objective, theta^t
    expTerm = theta**tWass 
    # nu^T * w
    expr = LinExpr(nu,w) 
    # objective
    obj = lambd*expTerm - expr 
    m.setObjective(obj, GRB.MINIMIZE)
    
    # Add constraint: u_f(x,a_f) >= u_f(x,a_f') + M(y_{a_f,u_f}-1)
    for j1 in range(sizFollFunc): # for each u_f
        for j2 in range(sizFollAction): # for each a_f
            for j3 in range(sizFollAction): # for each a_f'
                # u_f(x,a_f)
                lhsTerm = MixedStrategyUtilityOneAction(utilFollower[:,:,j1],x,j2,'F')
                # u_f(x,a_f') 
                rhsTerm1 = MixedStrategyUtilityOneAction(utilFollower[:,:,j1],x,j3,'F')
                # M(y_{a_f,u_f}-1)
                rhsTerm2 = M*(y[j2][j1]-1)
                # first constraint added
                m.addConstr(lhsTerm >= rhsTerm1 + rhsTerm2, "c1")
    
    # Add constraint: w_j <= M(1-y_{a_f,u_f}) + lambd*d^t(u_f,u_fj) + u_l(x,a_f)
    for j1 in range(sizFollFunc): # for each j, as in u_fj
        for j2 in range(sizFollFunc): # for each u_f
            for j3 in range(sizFollAction): # for each a_f
                # u_f(x,a_f)
                lhsTerm = w[j1]
                # M(1-y_{a_f,u_f})
                rhsTerm1 = M*(1-y[j3][j2])
                # lambd*d^t(u_f,u_fj)
                rhsTerm2 = lambd*((frobMat[j1][j2])**tWass)
                # u_l(x,a_f') 
                rhsTerm3 = MixedStrategyUtilityOneAction(utilLeader,x,j3,'L')
                # second constraint added
                m.addConstr(lhsTerm <= rhsTerm1 + rhsTerm2 + rhsTerm3, "c2")
            
    
    # Add constraint: x is a probability vector, its elements sum to 1
    m.addConstr(np.sum(x) == 1, "c4")    
    
    m.update()
    
    # initialize output variables
    out = 0 # output, the runtime
    time_terminate = 0 # indicator for exceeding time threshold
    optVal = 0 # optimal value

    # Optimize model
    m.optimize()
    
    # run time
    out = m.RunTime
    
    # if we have an optimal solution
    if m.status == GRB.OPTIMAL:
        # record the optimal solution
        optVal = m.objVal
    
    # if the given time limit is exceeded
    if m.status==GRB.TIME_LIMIT:
        # set indicator variable to 1
        time_terminate = 1
    
    return optVal,out,time_terminate

"""
This function runs the enumeration LP approach in the appendix
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizFollFunc: k, number of follwoer functions
tWass: Wasserstein exponent t
theta: Wasserstein radius theta
M: big M in the MIP
timeLim: threshold for the enumeration run time
utilLeader: leader utility function
utilFollower: k follower utility functions
nu: nominal distribution
output: optVal is the optimal value of the enumeration approach, out is the run time,
and time_terminate is an indicator if the enumeration timed out at the threshold
"""
def runLP2(sizLeaderAction,sizFollAction,sizFollFunc,tWass,theta,M,timeLim,utilLeader,utilFollower,nu):
    
    # initialize output optimum
    maxOpt = 0
    # initialize y
    y= make_zeros(sizFollAction, sizFollFunc)
    # total run time of the enumeration
    totalTime = 0
    
    # indicator for threshold
    maxTime = 0
    
    m, k = sizFollAction, sizFollFunc
    rows = np.vstack(np.identity(m, dtype=np.int))
    index = np.indices([len(rows)] * k).reshape(k, -1).T
    # all possible y's
    all_y = rows[index]   

    # compute matrix of frobenius norm differences of follower utilities
    frobMat = FrobDiffMatrix(utilFollower,sizFollFunc)
    
    # for each possible y
    for y in all_y:
    
        # run the baseline LP
        optVal,out,time_terminate = BaselineLP(sizLeaderAction,sizFollAction,sizFollFunc,tWass,theta,M,timeLim,utilLeader,utilFollower,nu,y.T,frobMat)
        
        # cumulative total run time
        totalTime += out
        
        # if this Baseline LP has higher optimal value than current max
        if out>0 and maxOpt > optVal:
            # store output
            maxOpt = optVal
            maxTime = time_terminate
            max_y = y
                        
    return maxOpt,totalTime,maxTime

"""
This function runs the Bayesian Stackelberg MIP (A.7) in the appendix for 
the finite utility case
sizLeaderAction: n, number of leader actions
sizFollAction: m, number of follower actions
sizFollFunc: k, number of follwoer functions
tWass: Wasserstein exponent t
theta: Wasserstein radius theta
M: big M in the MIP
timeLim: threshold for the enumeration run time
utilLeader: leader utility function
utilFollower: k follower utility functions
nu: nominal distribution
output: optVal is the optimal value of the Bayesian Stackelberg, out is the run time,
and time_terminate is an indicator if the MIP timed out at the threshold
"""
def runBayesian(sizLeaderAction,sizFollAction,sizFollFunc,tWass,theta,M,timeLim,utilLeader,utilFollower,nu):
    
    # Create a new Gurobi MIP model
    m = gp.Model("mip1")
    
    # set time limit
    m.setParam(GRB.Param.TimeLimit, timeLim)

    
    # Create variables
    # the variable x, continuous
    x = [0 for i in range(sizLeaderAction)] 
    for i in range(sizLeaderAction):
        x[i] = m.addVar(vtype=GRB.CONTINUOUS,lb=0,ub=1,name="x_%g" % i)
        
    # the variable w, continuous
    w = [0 for i in range(sizFollFunc)]
    for i in range(sizFollFunc):
        w[i] = m.addVar(vtype=GRB.CONTINUOUS,lb=-GRB.INFINITY,name="w_%d" % i)
        
    # the variable y, binary
    y= make_zeros(sizFollAction, sizFollFunc)
    for i in range(sizFollAction):
        for j in range(sizFollFunc):
            y[i][j] = m.addVar(vtype=GRB.BINARY,name="y_%d%d" % (i,j))
    
    # Set objective
    expr = LinExpr(nu,w) # nu^T w
    obj = expr # objective
    # Set objective
    m.setObjective(obj, GRB.MAXIMIZE)
    
    # Add constraint: u_f(x,a_f) >= u_f(x,a_f') + M(y_{a_f,u_f}-1)
    for j1 in range(sizFollFunc): # for each u_f
        for j2 in range(sizFollAction): # for each a_f
            for j3 in range(sizFollAction): # for each a_f'
                # u_f(x,a_f)
                lhsTerm = MixedStrategyUtilityOneAction(utilFollower[:,:,j1],x,j2,'F')
                # u_f(x,a_f') 
                rhsTerm1 = MixedStrategyUtilityOneAction(utilFollower[:,:,j1],x,j3,'F')
                # M(y_{a_f,u_f}-1)
                rhsTerm2 = M*(y[j2][j1]-1)
                # first constraint added
                m.addConstr(lhsTerm >= rhsTerm1 + rhsTerm2, "c1")
    
    # Add constraint: w_j <= M(1-y_{a_f,u_f}) + u_l(x,a_f)
    for j1 in range(sizFollFunc): # for each j, as in u_fj
        for j2 in range(sizFollFunc): # for each u_f
            for j3 in range(sizFollAction): # for each a_f
                # u_f(x,a_f)
                lhsTerm = w[j1]
                # M(1-y_{a_f,u_f})
                rhsTerm1 = M*(1-y[j3][j2])
                # u_l(x,a_f') 
                rhsTerm3 = MixedStrategyUtilityOneAction(utilLeader,x,j3,'L')
                # second constraint added
                m.addConstr(lhsTerm <= rhsTerm1 + rhsTerm3, "c2")
                
    # Add constraint: sum_{a_f} y(a_f,u_f) = 1
    for j in range(sizFollFunc):
        m.addConstr(np.sum([y[j2][j] for j2 in range(sizFollAction)])==1,"c3")
    
    # Add constraint: x is a probability vector, its elements sum to 1
    m.addConstr(np.sum(x) == 1, "c4")    
    
    # initialize output variables
    out = 0 # output, the runtime
    time_terminate = 0 # indicator for exceeding time threshold
    optVal = 0 # optimal value
    
    # Optimize model
    m.optimize()
    
    # run time
    out = m.RunTime
    # if we have an optimal solution
    if m.status == GRB.OPTIMAL:
        # record the optimal solution
        optVal = m.objVal
    
    # if the given time limit is exceeded
    if m.status==GRB.TIME_LIMIT:
        # set indicator variable to 1
        time_terminate = 1
    
    return optVal,out,time_terminate


