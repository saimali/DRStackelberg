"""
This python code generates Figure A.2 in the appendix of the paper, use this in same folder as funcFile_arxiv.py
"""
# First run funcFile_arxiv.py to load the functions
# run funcFile_arxiv.py

"""
Synthetic Data with Random Utilities
"""

#%%
"""
Figure A2a- Runtime vs Number of Leader Actions
"""
# dictionary of runtimes for each simulation, keys are simulation index and 
# values are runtimes for each simulation.
runTimeDictMIP = {}
runTimeDictLP = {}
runTimeDictBayes = {}

# dictionary of optimal values
optValDictMIP = {}
optValDictLP = {}
optValDictBayes = {}

# number of simulations
numSim = 10

# run each simulation
for num in range(numSim):
    
    iterList = list(range(2,1003,100))# list of iterables values for the paramater
    totalNum= len(iterList) # total number of parameter data points
    # initialize
    runTimesMIP = np.zeros(totalNum)
    runTimesLP = np.zeros(totalNum)
    runTimesBayes = np.zeros(totalNum)
    
    optValMIP = np.zeros(totalNum)
    optValLP = np.zeros(totalNum)
    optValBayes = np.zeros(totalNum)
    
    index = 0 # for each loop
    stopLP = 0 # variable to decide stop running the LP
    LPindex = 0 # number of iterations for which we run the LP
    # vary n, the number of leader actions
    for sizLeaderAction in iterList: 
        
        # Size of follower action space m 
        sizFollAction = 12 #Size of follower action space, m
        sizFollFunc = 4 # Size of follower utility function space, k
        
        # Wasserstein parameters
        tWass = 2.0 # Wasserstein exponent t
        theta = 0.1 # Wasserstein radius theta
    
        # Optimization parameters
        M = 1 
        timeLim = 1000 # set a threshold
        
        # keep track of where we are
        print('This is simulation %d and leader action n is %d' % (num+1,sizLeaderAction))
        
        # Generate random [0,1] utility functions 
        utilLeader,utilFollower,nu = utilAndnu(sizLeaderAction,sizFollAction,sizFollFunc)
        
        # run the DR MIP
        optValMIP[index],runTimesMIP[index],timeTerminateMIP = runMIP2(sizLeaderAction,sizFollAction,sizFollFunc,
                                               tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
        
        # run the enumeration LP if we haven't hit the threshold yet
        if stopLP < timeLim:
            
            t0 = time.time()
            # run the enumeration LPs
            optValLP[index],_,timeTerminateLP = runLP2(sizLeaderAction,sizFollAction,sizFollFunc,
                                                tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
            runTimesLP[index] = time.time()-t0 # runtime of the LP

            stopLP = runTimesLP[index]
            LPindex += 1
            
        # run the Bayesian Stackelberg MIP
        optValBayes[index],runTimesBayes[index],timeTerminateBayes = runBayesian(sizLeaderAction,sizFollAction,sizFollFunc,
                                               tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
       
        index += 1

    # store the outputs in the dict, with key= simulation index
    runTimeDictMIP[num] = runTimesMIP   
    optValDictMIP[num] = optValMIP
    
    runTimeDictLP[num] = runTimesLP
    optValDictLP[num] = optValLP
    
    runTimeDictBayes[num] = runTimesBayes
    optValDictBayes[num] = optValBayes

# Compute mean and standard deviation of outputs
runTimesMIP = np.zeros(totalNum) # average run time
SDrunTimesMIP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrMIP = np.zeros(numSim)
    for num in range(numSim):
        runArrMIP[num] = runTimeDictMIP[num][j] # for j, store all runtimes among simulations
    
    runTimesMIP[j] = np.average(runArrMIP) # average runtimes
    SDrunTimesMIP[j] = np.std(runArrMIP) # Standard Deviation of runtimes

# Compute mean and standard deviation of outputs
runTimesLP = np.zeros(totalNum) # average run time
SDrunTimesLP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrLP = np.zeros(numSim)
    for num in range(numSim):
        runArrLP[num] = runTimeDictLP[num][j] # for j, store all runtimes among simulations
    
    runTimesLP[j] = np.average(runArrLP) # average runtimes
    SDrunTimesLP[j] = np.std(runArrLP) # Standard Deviation of runtimes
    
# Compute mean and standard deviation of outputs
runTimesBayes = np.zeros(totalNum) # average run time
SDrunTimesBayes = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrBayes = np.zeros(numSim)
    for num in range(numSim):
        runArrBayes[num] = runTimeDictBayes[num][j] # for j, store all runtimes among simulations
    
    runTimesBayes[j] = np.average(runArrBayes) # average runtimes
    SDrunTimesBayes[j] = np.std(runArrBayes) # Standard Deviation of runtimes

"Plot"
f1 = plt.figure()
ax = plt.subplot(1,1,1)
d = iterList
# plot SD bars for each data point
eb1=plt.errorbar(d,runTimesMIP,SDrunTimesMIP, color='black',label='DR MIP',
             ecolor='gray', marker='s',capsize=3)
eb1[-1][0].set_linestyle('--')

# if we don't run the enumeration LP for all iterable values
if LPindex < totalNum:
    runTimesLP[LPindex-1] = timeLim # set the threshold
    
# # plot SD bars for each data point
eb2=plt.errorbar(d[:LPindex],runTimesLP[:LPindex],SDrunTimesLP[:LPindex], 
                 marker='v',label='Enumeration LPs',color='purple',ecolor='blue',capsize=3)
eb2[-1][0].set_linestyle('--')

# plot SD bars for each data point
eb3=plt.errorbar(d,runTimesBayes,SDrunTimesBayes, marker='^',label='Bayesian Stackelberg',
                 color='red',ecolor='pink',capsize=3)
eb3[-1][0].set_linestyle('--')

plt.xlabel('Number of Leader Actions')
plt.ylabel('Run time (s)')
plt.legend(loc='upper left', bbox_to_anchor=(0,1),fontsize='large',shadow=True, fancybox=True)
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
ax.spines['right'].set_visible(False)
ax.spines['top'].set_visible(False)

plt.show()
# save the PDF
f1.savefig('10sim_RandomMatrix_varyn.pdf',bbox_inches='tight') 

#%%
"""
Figure A2b- Runtime vs Number of Follower Actions
"""
# dictionary of runtimes for each simulation, keys are simulation index and 
# values are runtimes for each simulation.
runTimeDictMIP = {}
runTimeDictLP = {}
runTimeDictBayes = {}

# dictionary of optimal values
optValDictMIP = {}
optValDictLP = {}
optValDictBayes = {}

# number of simulations
numSim = 10

# run each simulation
for num in range(numSim):
    
    iterList = [2,4,8,16,24,32,40,48] # list of iterables values for the paramater
    totalNum= len(iterList) # total number of parameter data points
    # initialize
    runTimesMIP = np.zeros(totalNum)
    runTimesLP = np.zeros(totalNum)
    runTimesBayes = np.zeros(totalNum)
    
    optValMIP = np.zeros(totalNum)
    optValLP = np.zeros(totalNum)
    optValBayes = np.zeros(totalNum)
    
    index = 0 # for each loop
    stopLP = 0 # variable to decide stop running the LP
    LPindex = 0 # number of iterations for which we run the LP
    # vary m, the number of follower actions
    for sizFollAction in iterList: 
        
        # Size of leader action space n
        sizLeaderAction = 50
        sizFollFunc = 4 # Size of follower utility function space, k
        
        # Wasserstein parameters
        tWass = 2.0 # Wasserstein exponent t
        theta = 0.1 # Wasserstein radius theta
    
        # Optimization parameters
        M = 1 
        timeLim = 1000 # set a threshold
        
        # keep track of where we are
        print('This is simulation %d and follower action m is %d' % (num+1,sizFollAction))
        
        # Generate random [0,1] utility functions 
        utilLeader,utilFollower,nu = utilAndnu(sizLeaderAction,sizFollAction,sizFollFunc)
        
        # run the DR MIP
        optValMIP[index],runTimesMIP[index],timeTerminateMIP = runMIP2(sizLeaderAction,sizFollAction,sizFollFunc,
                                               tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
        
        # run the enumeration LP if we haven't hit the threshold yet
        if stopLP < timeLim:
            
            t0 = time.time()
            # run the enumeration LPs
            optValLP[index],_,timeTerminateLP = runLP2(sizLeaderAction,sizFollAction,sizFollFunc,
                                                tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
            runTimesLP[index] = time.time()-t0 # runtime of the LP

            stopLP = runTimesLP[index]
            LPindex += 1
            
        # run the Bayesian Stackelberg MIP
        optValBayes[index],runTimesBayes[index],timeTerminateBayes = runBayesian(sizLeaderAction,sizFollAction,sizFollFunc,
                                               tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
       
        index += 1

    # store the outputs in the dict, with key= simulation index
    runTimeDictMIP[num] = runTimesMIP   
    optValDictMIP[num] = optValMIP
    
    runTimeDictLP[num] = runTimesLP
    optValDictLP[num] = optValLP
    
    runTimeDictBayes[num] = runTimesBayes
    optValDictBayes[num] = optValBayes

# Compute mean and standard deviation of outputs
runTimesMIP = np.zeros(totalNum) # average run time
SDrunTimesMIP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrMIP = np.zeros(numSim)
    for num in range(numSim):
        runArrMIP[num] = runTimeDictMIP[num][j] # for j, store all runtimes among simulations
    
    runTimesMIP[j] = np.average(runArrMIP) # average runtimes
    SDrunTimesMIP[j] = np.std(runArrMIP) # Standard Deviation of runtimes

# Compute mean and standard deviation of outputs
runTimesLP = np.zeros(totalNum) # average run time
SDrunTimesLP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrLP = np.zeros(numSim)
    for num in range(numSim):
        runArrLP[num] = runTimeDictLP[num][j] # for j, store all runtimes among simulations
    
    runTimesLP[j] = np.average(runArrLP) # average runtimes
    SDrunTimesLP[j] = np.std(runArrLP) # Standard Deviation of runtimes
    
# Compute mean and standard deviation of outputs
runTimesBayes = np.zeros(totalNum) # average run time
SDrunTimesBayes = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrBayes = np.zeros(numSim)
    for num in range(numSim):
        runArrBayes[num] = runTimeDictBayes[num][j] # for j, store all runtimes among simulations
    
    runTimesBayes[j] = np.average(runArrBayes) # average runtimes
    SDrunTimesBayes[j] = np.std(runArrBayes) # Standard Deviation of runtimes

"Plot"
f2 = plt.figure()
ax = plt.subplot(1,1,1)
d = iterList
# plot SD bars for each data point
eb1=plt.errorbar(d,runTimesMIP,SDrunTimesMIP, color='black',label='DR MIP',
             ecolor='gray', marker='s',capsize=3)
eb1[-1][0].set_linestyle('--')

# if we don't run the enumeration LP for all iterable values
if LPindex < totalNum:
    runTimesLP[LPindex-1] = timeLim # set the threshold
    
# # plot SD bars for each data point
eb2=plt.errorbar(d[:LPindex],runTimesLP[:LPindex],SDrunTimesLP[:LPindex], 
                 marker='v',label='Enumeration LPs',color='purple',ecolor='blue',capsize=3)
eb2[-1][0].set_linestyle('--')

# plot SD bars for each data point
eb3=plt.errorbar(d,runTimesBayes,SDrunTimesBayes, marker='^',label='Bayesian Stackelberg',
                 color='red',ecolor='pink',capsize=3)
eb3[-1][0].set_linestyle('--')

plt.xlabel('Number of Follower Actions')
plt.ylabel('Run time (s)')
plt.legend(loc='upper left', bbox_to_anchor=(0.2,1.1),fontsize='large',shadow=True, fancybox=True)
plt.ylim(ymin = 0)   
     
SMALL_SIZE = 8
MEDIUM_SIZE = 12
BIGGER_SIZE = 18

plt.rc('font', size=MEDIUM_SIZE)          # controls default text sizes
plt.rc('axes', titlesize=BIGGER_SIZE)     # fontsize of the axes title
plt.rc('axes', labelsize=BIGGER_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('legend', fontsize=MEDIUM_SIZE)    # legend fontsize
plt.rc('figure', titlesize=MEDIUM_SIZE)  # fontsize of the figure title
ax.spines['right'].set_visible(False)
ax.spines['top'].set_visible(False)

plt.show()
# save the PDF
f2.savefig('10sim_RandomMatrix_varym.pdf',bbox_inches='tight') 

#%%
"""
Figure A2c- Runtime vs Number of Follower Utility Functions
"""
# dictionary of runtimes for each simulation, keys are simulation index and 
# values are runtimes for each simulation.
runTimeDictMIP = {}
runTimeDictLP = {}
runTimeDictBayes = {}

# dictionary of optimal values
optValDictMIP = {}
optValDictLP = {}
optValDictBayes = {}

# number of simulations
numSim = 10

# run each simulation
for num in range(numSim):
    
    iterList = list(range(2,32,2)) # list of iterables values for the paramater
    totalNum= len(iterList) # total number of parameter data points
    # initialize
    runTimesMIP = np.zeros(totalNum)
    runTimesLP = np.zeros(totalNum)
    runTimesBayes = np.zeros(totalNum)
    
    optValMIP = np.zeros(totalNum)
    optValLP = np.zeros(totalNum)
    optValBayes = np.zeros(totalNum)
    
    index = 0 # for each loop
    stopLP = 0 # variable to decide stop running the LP
    LPindex = 0 # number of iterations for which we run the LP
    
    # vary k, the number of follower utility functions
    for sizFollFunc in iterList: 
        
        # Size of leader action space n
        sizLeaderAction = 8
        sizFollFunc = 4 # Size of follower action space k
        
        # Wasserstein parameters
        tWass = 2.0 # Wasserstein exponent t
        theta = 0.1 # Wasserstein radius theta
    
        # Optimization parameters
        M = 1 
        timeLim = 1000 # set a threshold
        
        # keep track of where we are
        print('This is simulation %d and follower function size k is %d' % (num+1,sizFollFunc))
        
        # Generate random [0,1] utility functions
        utilLeader,utilFollower,nu = utilAndnu(sizLeaderAction,sizFollAction,sizFollFunc)
        
        # run the DR MIP
        optValMIP[index],runTimesMIP[index],timeTerminateMIP = runMIP2(sizLeaderAction,sizFollAction,sizFollFunc,
                                               tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
        
        # run the enumeration LP if we haven't hit the threshold yet
        if stopLP < timeLim:
            
            t0 = time.time()
            # run the enumeration LPs
            optValLP[index],_,timeTerminateLP = runLP2(sizLeaderAction,sizFollAction,sizFollFunc,
                                                tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
            runTimesLP[index] = time.time()-t0 # runtime of the LP

            stopLP = runTimesLP[index]
            LPindex += 1
            
        # run the Bayesian Stackelberg MIP
        optValBayes[index],runTimesBayes[index],timeTerminateBayes = runBayesian(sizLeaderAction,sizFollAction,sizFollFunc,
                                               tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
       
        index += 1    

    # store the outputs in the dict, with key= simulation index
    runTimeDictMIP[num] = runTimesMIP   
    optValDictMIP[num] = optValMIP
    
    runTimeDictLP[num] = runTimesLP
    optValDictLP[num] = optValLP
    
    runTimeDictBayes[num] = runTimesBayes
    optValDictBayes[num] = optValBayes

# Compute mean and standard deviation of outputs
runTimesMIP = np.zeros(totalNum) # average run time
SDrunTimesMIP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrMIP = np.zeros(numSim)
    for num in range(numSim):
        runArrMIP[num] = runTimeDictMIP[num][j] # for j, store all runtimes among simulations
    
    runTimesMIP[j] = np.average(runArrMIP) # average runtimes
    SDrunTimesMIP[j] = np.std(runArrMIP) # Standard Deviation of runtimes

# Compute mean and standard deviation of outputs
runTimesLP = np.zeros(totalNum) # average run time
SDrunTimesLP = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrLP = np.zeros(numSim)
    for num in range(numSim):
        runArrLP[num] = runTimeDictLP[num][j] # for j, store all runtimes among simulations
    
    runTimesLP[j] = np.average(runArrLP) # average runtimes
    SDrunTimesLP[j] = np.std(runArrLP) # Standard Deviation of runtimes
    
# Compute mean and standard deviation of outputs
runTimesBayes = np.zeros(totalNum) # average run time
SDrunTimesBayes = np.zeros(totalNum) # SD run time
for j in range(totalNum):
    runArrBayes = np.zeros(numSim)
    for num in range(numSim):
        runArrBayes[num] = runTimeDictBayes[num][j] # for j, store all runtimes among simulations
    
    runTimesBayes[j] = np.average(runArrBayes) # average runtimes
    SDrunTimesBayes[j] = np.std(runArrBayes) # Standard Deviation of runtimes

"Plot"
f3 = plt.figure()
ax = plt.subplot(1,1,1)
d = iterList
# plot SD bars for each data point
eb1=plt.errorbar(d,runTimesMIP,SDrunTimesMIP, color='black',label='DR MIP',
             ecolor='gray', marker='s',capsize=3)
eb1[-1][0].set_linestyle('--')

# if we don't run the enumeration LP for all iterable values
if LPindex < totalNum:
    runTimesLP[LPindex-1] = timeLim # set the threshold
    
# # plot SD bars for each data point
eb2=plt.errorbar(d[:LPindex],runTimesLP[:LPindex],SDrunTimesLP[:LPindex], 
                 marker='v',label='Enumeration LPs',color='purple',ecolor='blue',capsize=3)
eb2[-1][0].set_linestyle('--')

# plot SD bars for each data point
eb3=plt.errorbar(d,runTimesBayes,SDrunTimesBayes, marker='^',label='Bayesian Stackelberg',
                 color='red',ecolor='pink',capsize=3)
eb3[-1][0].set_linestyle('--')

plt.xlabel('Number of Follower Utility Functions')
plt.ylabel('Run time (s)')
plt.legend(loc='upper right', bbox_to_anchor=(1,1),fontsize='large',shadow=True, fancybox=True)
plt.ylim(ymin = 0)   
plt.ylim(ymax = 1010)
plt.xlim(xmax=32)
plt.xticks(range(2,32,4))
     
SMALL_SIZE = 8
MEDIUM_SIZE = 12
BIGGER_SIZE = 18

plt.rc('font', size=MEDIUM_SIZE)          # controls default text sizes
plt.rc('axes', titlesize=BIGGER_SIZE)     # fontsize of the axes title
plt.rc('axes', labelsize=BIGGER_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('legend', fontsize=MEDIUM_SIZE)    # legend fontsize
plt.rc('figure', titlesize=MEDIUM_SIZE)  # fontsize of the figure title
ax.spines['right'].set_visible(False)
ax.spines['top'].set_visible(False)

plt.show()
# save the PDF
f3.savefig('10sim_Cournot_varyk.pdf',bbox_inches='tight') 

#%%
"""
Figure A2d- Runtime vs Wasserstein Radius
"""
# dictionary of runtimes for each simulation, keys are simulation index and 
# values are runtimes for each simulation.
runTimeDictMIP = {}

# dictionary of optimal values
optValDictMIP = {}

# number of simulations
numSim = 10

# run each simulation
for num in range(numSim):
    
    iterList = np.linspace(1e-2,2+1e-2,11) # list of iterables values for the paramater
    totalNum= len(iterList) # total number of parameter data points
    # initialize
    runTimesMIP = np.zeros(totalNum)
    optValMIP = np.zeros(totalNum)
    
    sizFollAction = sizLeaderAction = 10 #Size of follower action space, m
    sizFollFunc = 12 # Size of follower utility function space, k
    utilLeader,utilFollower,nu = utilAndnu(sizLeaderAction,sizFollAction,sizFollFunc)   

    tWass = 2
    theta=0
    M=1
    timeLim=1000
    
    index = 0 # for each loop

    # vary theta, Wasserstein radius
    for theta in iterList: 
        
        # keep track of where we are
        print('This is simulation %d and theta is %d' % (num+1,theta))
        
        # run the DR MIP
        optValMIP[index],runTimesMIP[index],timeTerminateMIP = runMIP2(sizLeaderAction,sizFollAction,sizFollFunc,
                                               tWass,theta,M,timeLim,utilLeader,utilFollower,nu)
        
        index += 1    

    # store the outputs in the dict, with key= simulation index
    runTimeDictMIP[num] = runTimesMIP   
    optValDictMIP[num] = optValMIP
    
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
f4 = plt.figure()
ax = plt.subplot(1,1,1)
d = iterList
# plot SD bars for each data point
eb1=plt.errorbar(d,runTimesMIP,SDrunTimesMIP, color='black',label='DR MIP',
             ecolor='gray', marker='s',capsize=3)
eb1[-1][0].set_linestyle('--')

plt.xlabel('Wasserstein radius')
plt.ylabel('Run time (s)')
plt.legend(loc='upper right', bbox_to_anchor=(1,1),fontsize='large',shadow=True, fancybox=True)
plt.ylim(ymin = 0)   
plt.xticks([0.01,0.41,0.81,1.21,1.61,2.01],[0,0.4,0.8,1.2,1.6,2])
     
SMALL_SIZE = 8
MEDIUM_SIZE = 12
BIGGER_SIZE = 18

plt.rc('font', size=MEDIUM_SIZE)          # controls default text sizes
plt.rc('axes', titlesize=BIGGER_SIZE)     # fontsize of the axes title
plt.rc('axes', labelsize=BIGGER_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=BIGGER_SIZE)    # fontsize of the tick labels
plt.rc('legend', fontsize=MEDIUM_SIZE)    # legend fontsize
plt.rc('figure', titlesize=MEDIUM_SIZE)  # fontsize of the figure title
ax.spines['right'].set_visible(False)
ax.spines['top'].set_visible(False)

plt.show()
# save the PDF
f4.savefig('10sim_RandomMatrix_varyWasserstein.pdf',bbox_inches='tight') 


