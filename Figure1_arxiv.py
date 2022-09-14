"""
This python code generates Figure 1 in the paper, use this in same folder as funcFile_arxiv.py
"""

# First run funcFile_arxiv.py to load the functions
# run funcFile_arxiv

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

#%%
"""
Arxiv-2022
This is written to work for any E^f, commented since we do not use it for Figure 1
"""

# sizLeaderAction = 4 # n
# sizFollAction = 2 # m
# sizNominal = 4 # k
# tWass=2 # t
# theta=0.1 
# M=2
# maxIter = 50
# timeLim = 1000 # time limit in seconds
# utilLeader= np.random.rand(sizLeaderAction,sizFollAction)
# utilNomFollower = {}
# for j in range(sizNominal):
#     utilNomFollower[j] = np.random.rand(sizLeaderAction,sizFollAction)
# # uniform nominal distribution
# nu = [1/sizNominal for i in range(sizNominal)]

# xStar,lambdaStar,wStar,deltStar,optStar, runTimeAlg1,totalTau, storeGamma, storeSizes = Algorithm1(sizLeaderAction,sizFollAction,
#                                                           sizNominal,tWass,theta,M,timeLim,utilLeader,utilNomFollower,nu,maxIter)


#%%
"""
Simple Inspection Game
"""

"""
Figure 1a- Runtime vs Number of Leader Actions
"""
# dictionary of runtimes for each simulation, keys are simulation index and 
# values are runtimes for each simulation.
runTimeDictMIP = {}

# dictionary of optimal values
optValDictMIP = {}

# dictionary of number of iterations
totalTauDictMIP = {}

# number of simulations
numSim = 10

# run each simulation
for num in range(numSim):
    
    s = 7 # set size in Simple Inspection Game
    totalNum= s-2 # total number of iterables
    
    # initialize
    runTimesMIP = np.zeros(totalNum)
    
    optValMIP = np.zeros(totalNum)
    
    totalTau = np.zeros(totalNum)
    
    k = 4 # Size of nominal follower utility function, k
    sizNominal = k
    q = 2 # maximum size of follower set
    # number of follower actions
    sizFollAction = InspectionActionSize(s,q)
    # Wasserstein parameters
    tWass = 2.0 # Wasserstein exponent t
    theta = 0.1 # Wasserstein radius theta
    
    M=2 # set big M
    maxIter = 200 # maximum number of iterations
    timeLim = 1000 # threshold
    
    index = 0 # for each loop
    
    # vary n, the number of leader actions by varying maximum size of leader set
    for p in range(2,s): 
        
        # size of leader action
        sizLeaderAction = InspectionActionSize(s,p)
        # keep track of where we are- simulation, which p 
        print('This is simulation %d and p %d of %d' % (num+1,p-1,s-2))
        
        # nominal utility functions
        gameLossCoeffs_Nominal = np.random.uniform(0.3,0.6,k)
        gameWinCoeffs_Nominal = np.random.uniform(0.7,1,k)
        
        # Generate utility functions for the Simple Inspection Game
        utilLeader,utilFollower = InspectionUtilities2022(s,p,q,k,gameLossCoeffs_Nominal,gameWinCoeffs_Nominal)
        # nominal distribution, random probability vector
        utilNomFollower = {}
        # make nominal utilities random probability vectors
        for j in range(k):
            utilNomFollower[j] = utilFollower[:,:,j]
        
        # nu is uniformly distributed on the nominal functions
        nu = [1/k for i in range(k)]
        # computes Algorithm 1 for the inspection game
        xStar,lambdaStar,wStar,deltStar,optValMIP[index],runTimesMIP[index],totalTau[index], storeGamma, storeSizes = Algorithm1Inspection(sizLeaderAction,sizFollAction,
                                                          sizNominal,tWass,theta,M,timeLim,utilLeader,utilNomFollower,nu,maxIter)
        # update index 
        index += 1

    # store the outputs in the dict, with key= simulation index
    runTimeDictMIP[num] = runTimesMIP   
    optValDictMIP[num] = optValMIP
    totalTauDictMIP[num] = totalTau
    

# Compute mean and standard deviation of outputs
runTimesMIP = np.zeros(totalNum) # average run time
SDrunTimesMIP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrMIP = np.zeros(numSim)
    for num in range(numSim):
        runArrMIP[num] = runTimeDictMIP[num][j] # for j, store all runtimes among simulations
    
    runTimesMIP[j] = np.average(runArrMIP) # average runtimes
    SDrunTimesMIP[j] = np.std(runArrMIP) # Standard Deviation of runtimes
  
"Plot"
arxiv_runTimesfiga,arxiv_SDrunTimesfiga = runTimesMIP,SDrunTimesMIP

f1 = plt.figure()
ax = plt.subplot(1,1,1)
d = [InspectionActionSize(s,j) for j in range(2,s)]
# plot SD bars for each data point
eb1=plt.errorbar(d,arxiv_runTimesfiga,arxiv_SDrunTimesfiga, color='black',label='Algorithm 1',
             ecolor='gray', marker='s',capsize=3)
eb1[-1][0].set_linestyle('--')

plt.xlabel('Number of Leader Actions')
plt.ylabel('Run time (s)')
#plt.legend(loc='upper right', bbox_to_anchor=(1,1),fontsize='large',shadow=True, fancybox=True)
plt.ylim(ymin = 0)   
     
SMALL_SIZE = 8
MEDIUM_SIZE = 12
BIGGER_SIZE = 22

plt.rc('font', size=MEDIUM_SIZE)          # controls default text sizes
plt.rc('axes', titlesize=BIGGER_SIZE)     # fontsize of the axes title
plt.rc('axes', labelsize=BIGGER_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('legend', fontsize=MEDIUM_SIZE)    # legend fontsize
plt.rc('figure', titlesize=MEDIUM_SIZE)  # fontsize of the figure title
ax.spines['right'].set_visible(False)    # remove right spine
ax.spines['top'].set_visible(False)      # remove top spine

plt.show()
# save the PDF of figure in the folder
f1.savefig('arxiv_2022_Inspection_Figa.pdf',bbox_inches='tight') 


#%%
"""
Figure 1b- Runtime vs Number of Follower Actions
"""
# dictionary of runtimes for each simulation, keys are simulation index and 
# values are runtimes for each simulation.
runTimeDictMIP = {}

# dictionary of optimal values
optValDictMIP = {}

# dictionary of number of iterations
totalTauDictMIP = {}

# number of simulations
numSim = 10

# run each simulation
for num in range(numSim):
    
    s = 7 # set size in Simple Inspection Game
    totalNum= s-2 # total number of iterables
    
    # initialize
    runTimesMIP = np.zeros(totalNum)
    
    optValMIP = np.zeros(totalNum)
    
    totalTau = np.zeros(totalNum)
    
    k = 2 # Size of nominal follower utility function, k
    sizNominal = k
    p = 5 # maximum size of follower set
    # number of follower actions
    sizLeaderAction = InspectionActionSize(s,p)
    # Wasserstein parameters
    tWass = 2.0 # Wasserstein exponent t
    theta = 0.1 # Wasserstein radius theta
    
    M=2
    maxIter = 200
    timeLim = 1000 # threshold
    
    index = 0 # for each loop
    
    # vary m, the number of follower actions by varying maximum size of leader set
    for q in range(2,s): 
        
        sizFollAction = InspectionActionSize(s,q)
        # keep track of where we are
        print('This is simulation %d and p %d of %d' % (num+1,q-1,s-2))
        # nominal utility functions
        gameLossCoeffs_Nominal = np.random.uniform(0.3,0.6,k)
        gameWinCoeffs_Nominal = np.random.uniform(0.7,1,k)
        
        # Generate utility functions for the Simple Inspection Game
        utilLeader,utilFollower = InspectionUtilities2022(s,p,q,k,gameLossCoeffs_Nominal,gameWinCoeffs_Nominal)
        # nominal distribution, random probability vector
        utilNomFollower = {}
        for j in range(k):
            utilNomFollower[j] = utilFollower[:,:,j]
        
        # nu is uniformly distributed on the nominal functions
        nu = [1/k for i in range(k)]
        
        xStar,lambdaStar,wStar,deltStar,optValMIP[index],runTimesMIP[index],totalTau[index], storeGamma, storeSizes = Algorithm1Inspection(sizLeaderAction,sizFollAction,
                                                          sizNominal,tWass,theta,M,timeLim,utilLeader,utilNomFollower,nu,maxIter)

        
        index += 1

    # store the outputs in the dict, with key= simulation index
    runTimeDictMIP[num] = runTimesMIP   
    optValDictMIP[num] = optValMIP
    totalTauDictMIP[num] = totalTau
    

# Compute mean and standard deviation of outputs
runTimesMIP = np.zeros(totalNum) # average run time
SDrunTimesMIP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrMIP = np.zeros(numSim)
    for num in range(numSim):
        runArrMIP[num] = runTimeDictMIP[num][j] # for j, store all runtimes among simulations
    
    runTimesMIP[j] = np.average(runArrMIP) # average runtimes
    SDrunTimesMIP[j] = np.std(runArrMIP) # Standard Deviation of runtimes
  
"Plot"
arxiv_runTimesfigb,arxiv_SDrunTimesfigb = runTimesMIP,SDrunTimesMIP

f2 = plt.figure()
ax = plt.subplot(1,1,1)
d = [InspectionActionSize(s,j) for j in range(2,s)]
# plot SD bars for each data point
eb1=plt.errorbar(d,arxiv_runTimesfigb,arxiv_SDrunTimesfigb, color='black',label='Algorithm 1',
             ecolor='gray', marker='s',capsize=3)
eb1[-1][0].set_linestyle('--')

plt.xlabel('Number of Follower Actions')
plt.ylabel('Run time (s)')
#plt.legend(loc='upper left', bbox_to_anchor=(0,1),fontsize='large',shadow=True, fancybox=True)
plt.ylim(ymin = 0)   
     
SMALL_SIZE = 8
MEDIUM_SIZE = 12
BIGGER_SIZE = 22

plt.rc('font', size=MEDIUM_SIZE)          # controls default text sizes
plt.rc('axes', titlesize=BIGGER_SIZE)     # fontsize of the axes title
plt.rc('axes', labelsize=BIGGER_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('legend', fontsize=MEDIUM_SIZE)    # legend fontsize
plt.rc('figure', titlesize=MEDIUM_SIZE)  # fontsize of the figure title
ax.spines['right'].set_visible(False)    # remove right spine
ax.spines['top'].set_visible(False)      # remove top spine



# save the PDF
f2.savefig('arxiv_2022_Inspection_Figb.pdf',bbox_inches='tight') 

#%%

"""
Figure 1c- Runtime vs Number of Nominal Follower Utility Functions
"""

# dictionary of runtimes for each simulation, keys are simulation index and 
# values are runtimes for each simulation.
runTimeDictMIP = {}

# dictionary of optimal values
optValDictMIP = {}

# dictionary of optimal values
totalTauDictMIP = {}

# number of simulations
numSim = 10

# run each simulation
for num in range(numSim):
    
    s = 7 # set size in Simple Inspection Game
    iterList = [2,3,4,5,6] # iterate over this
    totalNum= len(iterList) # total number of iterables
    
    # initialize
    runTimesMIP = np.zeros(totalNum)
    
    optValMIP = np.zeros(totalNum)
    
    totalTau = np.zeros(totalNum)
    
    p = 2 # maximum size of leader set
    # number of leader actions
    sizLeaderAction = InspectionActionSize(s,p)
    q = 2 # maximum size of follower set
    # number of follower actions
    sizFollAction = InspectionActionSize(s,q)
    # Wasserstein parameters
    tWass = 2.0 # Wasserstein exponent t
    theta = 0.1 # Wasserstein radius theta
    
    M=2
    maxIter = 200
    timeLim = 1000 # threshold
    
    index = 0 # for each loop
    
    # vary m, the number of follower actions by varying maximum size of leader set
    for k in iterList: 
        
        print('This is simulation %d and k is %d' % (num+1,k))
        # nominal utility functions
        gameLossCoeffs_Nominal = np.random.uniform(0.3,0.6,k)
        gameWinCoeffs_Nominal = np.random.uniform(0.7,1,k)
        
        # Generate utility functions for the Simple Inspection Game
        utilLeader,utilFollower = InspectionUtilities2022(s,p,q,k,gameLossCoeffs_Nominal,gameWinCoeffs_Nominal)
        # nominal distribution, random probability vector
        utilNomFollower = {}
        for j in range(k):
            utilNomFollower[j] = utilFollower[:,:,j]
        
        # nu is uniformly distributed on the nominal functions
        nu = [1/k for i in range(k)]
        
        xStar,lambdaStar,wStar,deltStar,optValMIP[index],runTimesMIP[index],totalTau[index], storeGamma, storeSizes = Algorithm1Inspection(sizLeaderAction,sizFollAction,
                                                          k,tWass,theta,M,timeLim,utilLeader,utilNomFollower,nu,maxIter)

        
        index += 1

    # store the outputs in the dict, with key= simulation index
    runTimeDictMIP[num] = runTimesMIP   
    optValDictMIP[num] = optValMIP
    totalTauDictMIP[num] = totalTau
    

# Compute mean and standard deviation of outputs
runTimesMIP = np.zeros(totalNum) # average run time
SDrunTimesMIP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrMIP = np.zeros(numSim)
    for num in range(numSim):
        runArrMIP[num] = runTimeDictMIP[num][j] # for j, store all runtimes among simulations
    
    runTimesMIP[j] = np.average(runArrMIP) # average runtimes
    SDrunTimesMIP[j] = np.std(runArrMIP) # Standard Deviation of runtimes
  
"Plot"
arxiv_runTimesfigc,arxiv_SDrunTimesfigc = runTimesMIP,SDrunTimesMIP

f3 = plt.figure()
ax = plt.subplot(1,1,1)
d = iterList
# plot SD bars for each data point
eb3=plt.errorbar(d,arxiv_runTimesfigc,arxiv_SDrunTimesfigc, color='black',label='Algorithm 1',
             ecolor='gray', marker='s',capsize=3)
eb3[-1][0].set_linestyle('--')

plt.xlabel('Number of Nominal Follower Utilities')
plt.ylabel('Run time (s)')
# plt.legend(loc='upper left', bbox_to_anchor=(0,1),fontsize='large',shadow=True, fancybox=True)
plt.ylim(ymin = 0)   
     
SMALL_SIZE = 8
MEDIUM_SIZE = 12
BIGGER_SIZE = 22

plt.rc('font', size=MEDIUM_SIZE)          # controls default text sizes
#plt.rc('xaxis', titlesize=BIGGER_SIZE)     # fontsize of the axes title
plt.rc('axes', labelsize=BIGGER_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=18)    # fontsize of the tick labels
plt.rc('legend', fontsize=MEDIUM_SIZE)    # legend fontsize
plt.rc('figure', titlesize=MEDIUM_SIZE)  # fontsize of the figure title
ax.spines['right'].set_visible(False)    # remove right spine
ax.spines['top'].set_visible(False)      # remove top spine

plt.show()
# save the PDF of figure in the folder
f3.savefig('arxiv_2022_Inspection_Figc.pdf',bbox_inches='tight') 


#%%
"""
Figure 1d- Runtime vs Wasserstein Radius
"""
# dictionary of runtimes for each simulation, keys are simulation index and 
# values are runtimes for each simulation.
runTimeDictMIP = {}
# dictionary of optimal values
optValDictMIP = {}
# dictionary of number of total iterations
totalTauDictMIP = {}


# number of simulations
numSim = 5

# run each simulation
for num in range(numSim):
    
    s = 7 # set size in Simple Inspection Game
    
    iterList = np.linspace(1e-2,2+1e-2,11) # list of iterables values for the paramater
    totalNum= len(iterList) # total number of parameter data points
    
    # initialize
    runTimesMIP = np.zeros(totalNum)
    optValMIP = np.zeros(totalNum)
    totalTau = np.zeros(totalNum)
    
    k = 4 # Size of follower utility function space, k
    siz = 4
    p = 4 #2 # maximum size of leader set
    # number of leader actions
    sizLeaderAction = InspectionActionSize(s,p)
    q = 4 #2 # maximum size of follower set
    # number of follower actions
    sizFollAction = InspectionActionSize(s,q)
    # Wasserstein parameters
    tWass = 2.0 # Wasserstein exponent t
    theta = 0.1 # Wasserstein radius theta
    M=2
    maxIter = 200
    timeLim = 1000 # threshold
    
    gameLossCoeffs_Nominal = np.random.uniform(0.3,0.6,k)
    gameWinCoeffs_Nominal = np.random.uniform(0.7,1,k)
    # Generate utility functions for the Simple Inspection Game
    utilLeader,utilFollower = InspectionUtilities2022(s,p,q,k,gameLossCoeffs_Nominal,gameWinCoeffs_Nominal)
    # nu is uniformly distributed on the nominal functions
    nu = [1/k for i in range(k)]  
    
    index = 0 # for each loop
    
    # vary theta, Wasserstein radius
    for theta in iterList: 
        
        # keep track of where we are
        print('This is simulation %d and theta is %d' % (num+1,theta))
        # nominal utility functions
        gameLossCoeffs_Nominal = np.random.uniform(0.3,0.6,k)
        gameWinCoeffs_Nominal = np.random.uniform(0.7,1,k)
        
        # Generate utility functions for the Simple Inspection Game
        utilLeader,utilFollower = InspectionUtilities2022(s,p,q,k,gameLossCoeffs_Nominal,gameWinCoeffs_Nominal)
        # nominal distribution, random probability vector
        utilNomFollower = {}
        for j in range(k):
            utilNomFollower[j] = utilFollower[:,:,j]
        
        
        nu = [1/k for i in range(k)]
        # run the Algorithm 1
        xStar,lambdaStar,wStar,deltStar,optValMIP[index],runTimesMIP[index],totalTau[index], storeGamma, storeSizes = Algorithm1Inspection(sizLeaderAction,sizFollAction,k,tWass,
                                                                                                                                           theta,M,timeLim,utilLeader,utilNomFollower,nu,maxIter)

        index += 1

    # store the outputs in the dict, with key= simulation index
    runTimeDictMIP[num] = runTimesMIP   
    

# Compute mean and standard deviation of outputs
runTimesMIP = np.zeros(totalNum) # average run time
SDrunTimesMIP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrMIP = np.zeros(numSim)
    for num in range(numSim):
        runArrMIP[num] = runTimeDictMIP[num][j] # for j, store all runtimes among simulations
    
    runTimesMIP[j] = np.average(runArrMIP) # average runtimes
    SDrunTimesMIP[j] = np.std(runArrMIP) # Standard Deviation of runtimes

"Plot"
arxiv_runTimesfigd,arxiv_SDrunTimesfigd = runTimesMIP,SDrunTimesMIP

f4 = plt.figure()
ax = plt.subplot(1,1,1)
d = iterList
# plot SD bars for each data point
eb4=plt.errorbar(d,arxiv_runTimesfigd,arxiv_SDrunTimesfigd, color='black',label='Algorithm 1',
             ecolor='gray', marker='s',capsize=3)
eb4[-1][0].set_linestyle('--')


plt.xlabel('Wasserstein Radius')
plt.ylabel('Run time (s)')
# plt.legend(loc='upper right', bbox_to_anchor=(1,1),fontsize='large',shadow=True, fancybox=True)
plt.ylim(ymin = 0)   
plt.xticks([0.01,0.41,0.81,1.21,1.61,2.01],[0,0.4,0.8,1.2,1.6,2])
     
SMALL_SIZE = 8
MEDIUM_SIZE = 12
BIGGER_SIZE = 22

plt.rc('font', size=MEDIUM_SIZE)          # controls default text sizes
plt.rc('axes', titlesize=BIGGER_SIZE)     # fontsize of the axes title
plt.rc('axes', labelsize=BIGGER_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('legend', fontsize=MEDIUM_SIZE)    # legend fontsize
plt.rc('figure', titlesize=MEDIUM_SIZE)  # fontsize of the figure title
ax.spines['right'].set_visible(False)    # remove right spine
ax.spines['top'].set_visible(False)      # remove top spine

plt.show()
# save the PDF of figure in the folder
f1.savefig('arxiv_2022_Inspection_Figd.pdf',bbox_inches='tight') 










