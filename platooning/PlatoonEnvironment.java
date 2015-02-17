package eass.platooning;

import java.util.ArrayList;
import java.util.Random;

import ail.mas.ActionScheduler;
import ail.mas.NActionScheduler;
import ail.platoon.Vehicle;
import ail.syntax.Action;
import ail.syntax.Literal;
import ail.syntax.Message;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.PredicatewAnnotation;
import ail.syntax.StringTerm;
import ail.syntax.StringTermImpl;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.syntax.VarTerm;
import ail.util.AILexception;
import ajpf.util.AJPFLogger;
import ajpf.util.VerifyMap;
import eass.cruise_control.MotorWayEnv.Car;
import eass.mas.DefaultEASSEnvironment;


//import eass.mas.matlab.EASSMatLabEnvironment;


public class PlatoonEnvironment extends DefaultEASSEnvironment{
	String logname = "eass.platooning.PlatoonEnvironment";

	
	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	
//	Vehicle v1 = new Vehicle(1);
//	Vehicle v2 = new Vehicle(2);
	Vehicle v3 = new Vehicle(3);
	
	/**
	 * Constructor.
	 *
	 */
	public PlatoonEnvironment() {
		super();
		NActionScheduler s = new NActionScheduler(100);
		s.addJobber(this);
		setScheduler(s);
		addPerceptListener(s);
	}
		
	
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#initialise()
	 */
	public void initialise() {
		super.initialise();
		vehicles.add(v3);
		// If not connected to matlab make the scheduler more verification friendly.
//		if (!connectedtomatlab) {
//			NActionScheduler s = (NActionScheduler) getScheduler();
//			removePerceptListener(s);
//			ActionScheduler s1 = new ActionScheduler();
//			s1.addJobber(this);
//			setScheduler(s1);
//			addPerceptListener(s1);
//		}
	}

	public void eachrun() {
		
		for (Vehicle v: vehicles) {
			v.update();
			v.execute("");

			Literal distance = new Literal("distance");
			distance.addTerm(new NumberTermImpl(v.getDistance()));
			addUniquePercept("abstraction_follower"+ v.getID(), distance);		

			Literal precedingPlatoonID = new Literal("precedingPID");
//			precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
			precedingPlatoonID.addTerm(new NumberTermImpl(v.getprecedingPID()));
			addPercept("abstraction_follower"+ v.getID(), precedingPlatoonID);
			
			Literal egoPlatoonID = new Literal("egoPID");
//			egoPlatoonId.addTerm(new NumberTermImpl(v.getegoPID());
			egoPlatoonID.addTerm(new NumberTermImpl(v.getegoPID()));
			addPercept("abstraction_follower"+ v.getID(), egoPlatoonID);

		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#printvalues(ail.syntax.Literal)
	 */
	public void printvalues(Literal pred) {
		if (pred.getFunctor().equals("distance")) {
			AJPFLogger.fine("eass.platooning", pred.toString());
		} 

	}

	public boolean done() {
		   return false;
		}

	public Unifier executeAction(String agName, Action act) throws AILexception {
		   
		Unifier u = new Unifier();
		boolean printed = false;
		 
	   if (act.getFunctor().equals("run")) {
			   int agentnum = Integer.parseInt(((String) agName).substring(14));
			   Predicate predlist = (Predicate) act.getTerm(0);
			   Predicate predargs = (Predicate) act.getTerm(1);
			   int num_args = predargs.getTermsSize();
			   String num_arg_s = "" + num_args;
			   int num_name_comps = predlist.getTermsSize();
			   String predname = "";

			   // to extract predname as String
			   for (int i=0; i<num_name_comps; i++) {
				   // Lots of extra work to get the string correct.
				   Term nb = predlist.getTerm(i);
				   String s = nb.toString();
				   if (nb instanceof VarTerm) {
					   VarTerm v = (VarTerm) nb;
					   nb = v.getValue();
				   }
				   if (nb instanceof NumberTerm) {
					   NumberTerm num = (NumberTerm) nb;
					   Double number = num.solve();
					   int num_as_int = number.intValue();
					   s = ((Integer) num_as_int).toString();
				   }
				   if (nb instanceof StringTermImpl) {
					   StringTermImpl string = (StringTermImpl) nb;
					   s = string.getString();
				   }
				   predname += s;
			   }
			   System.out.println("real call for performing an action for "+ predname);

			   // send the predicate for vehicle to convert it to binary
				for (Vehicle v: vehicles) {
					if (agName.equals("abstraction_follower"+v.getID()))
						v.execute(predname);
				}
	   }  else {
     		 u = super.executeAction(agName, act);
    		 printed = true;
    	}
	   
	   if (!printed) {
		   AJPFLogger.info("eass.mas.DefaultEASSEnvironment", agName + " done " + printAction(act));
	   }
	   return u;
	  }	  		
}
