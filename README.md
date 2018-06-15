# AgentPlatooning
BDI agent codes for autonomous vehicle platooning

This repository contains:

1- guideline for running our hybrid agent-based platooning model using TORCS simulation
2- guideline for regenerating the verification results for platooning
3- guideline for regenerating of untimed automaton from agent model
4- links to Uppaal model of platooning
5- agent code used on Jaquar Rovers
6- links to real hardware test demo
7- links to simulation demo


====== running the hybrid agent-based platooning model using TORCS simulation ==========

1. Checkout the following repository first 

	git clone https://github.com/VerifiableAutonomy/mcapl

2. Checkout this repository 

	git clone https://github.com/VerifiableAutonomy/AgentPlatooning
	cd AgentForSimulationScenario1-6/platooning

3. Add the AgentForSimulationScenario1-6/platooning folder in the mcapl/src/examples/eass folder

4. Checkout the following repository which contains Simulink Robot for TORCS
	and guideline for how to use it
	
	git clone https://github.com/TORCSLink
	
5. Run the Simulink and TORCS by following "Usage" guideline that is provided with Simulink Roboto for TORCS repository

6. Checkout the following repository for Simulink control system

	git clone https://github.com/VerifiableAutonomy/AutonomousPlatooning/tree/AgentImplementation 

6. Run MCAPL (run-AIL) with scenario6.ail or any *.ail file for different scenarios in platooning

======= regenerating the verification of platooning using AJPF ===========

1. Checkout the following repository for MCAPL first
	
	git clone https://github.com/VerifiableAutonomy/mcapl

2. Checkout this repository
	
	git clone https://gitbub.com/VerifiableAutonomy/AgentPlatooning

3. Add the AgentVerification folder in the mcapl/src/examples/verification/eass folder

4. Run MCAPL (run-JPF) with platoon.jpf argument 

======= regenerating the untimed automaton from agent model ===========

1. Follow the same guideline for regenerating the verification of platooning using AJPF 
	to generate the states perceptions and state transition of the agent code
	
2. Run AgentToAutomatonTranslation/TranslatorAgentToAutomaton.java from
	repository https://github.com/VerifiableAutonomy/AgentPlatooning

	Input files:
	a) perception list is the list of all the perceptions (perception_list.txt)
	b) perception type indicates if a perception is an input/output belief
	c) states perception is the generated file with the AJPF that shows the percpetion of 
		each state
	d) state transitions is the generated file with the AJPF that shows the transitions between
		states 
	Output files:
	a) a list of translated/abstracted states 
	b) a list of translated/abstracted transitions
	c) a DOT file for generating the translated automaton in Graphviz

======= Uppaal model for platooning =============

1. Checkout this repository 

	git clone https://github.com/VerifiableAutonomy/AgentPlatooning

2. Open the Uppaal model from UppaalPlatooningModel folder

======= UDP model of the agent code for real hardware test =============

1. Checkout this repository

	git clone https://github.com/VerifiableAutonomy/AgentPlatooning

======= links to realhardware test demo =========

1. http://wordpress.csc.liv.ac.uk/va/2017/01/18/real-hardware-testing-of-autonomous-vehicle-platooning-demo/


====== links to simulation demo ===========

1. http://wordpress.csc.liv.ac.uk/va/2016/05/18/autonomous-vehicle-platooning-demo/

