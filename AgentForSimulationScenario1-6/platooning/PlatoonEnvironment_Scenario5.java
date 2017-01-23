package eass.platooning;

import java.util.ArrayList;
import java.util.Random;

import ail.mas.ActionScheduler;
import ail.mas.NActionScheduler;
import ail.platoon.Vehicle;
import ail.syntax.Action;
import ail.syntax.Literal;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.StringTermImpl;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.syntax.VarTerm;
import ail.util.AILexception;
import ajpf.util.AJPFLogger;
import ajpf.util.VerifyMap;
import eass.cruise_control.MotorWayEnv.Car;
import eass.mas.DefaultEASSEnvironment;
import eass.mas.matlab.EASSMatLabEnvironment;



public class PlatoonEnvironment_Scenario5 extends DefaultEASSEnvironment {
	String logname = "eass.platooning.PlatoonEnvironment";

	
//	VerifyMap<String, Vehicle> vehicles = new VerifyMap<String, Vehicle>();
	
//	Vehicle v = new Vehicle(2);
	int Y = 1;
	int precedingPID=2;
	int egoPID=3;
	int egoID=3;
	boolean agreement=false;
	
	Random r = new Random();

//	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	
	
	/**
	 * Constructor.
	 *
	 */
	public PlatoonEnvironment_Scenario5() {
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
		// If not connected to matlab make the scheduler more verification friendly.
			NActionScheduler s = (NActionScheduler) getScheduler();
			removePerceptListener(s);
			ActionScheduler s1 = new ActionScheduler();
			s1.addJobber(this);
			setScheduler(s1);
			addPerceptListener(s1);
			
//			Literal latency = new Literal("latency");
////			precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
//			latency.addTerm(new NumberTermImpl(0));
//			addPercept("abstraction_follower1",latency);
			
		
	}

	public void eachrun() {

		if (Y == 10){
			Y=1;
			if(egoPID == 0)
				egoPID = 3;
		}
		
/*		for (Vehicle v: vehicles) {
			if(v.getEgoID()== 3){
				Literal precedingPlatoonID = new Literal("precedingPID");
				precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
				addUniquePercept("precedingPID",precedingPlatoonID);
				
				Literal egoPlatoonID = new Literal("egoPID");
				egoPlatoonID.addTerm(new NumberTermImpl(v.getEgoPID()));
				addUniquePercept("egoPID",egoPlatoonID);
				
			}
		}
*/

		precedingPID = r.nextInt(4);

		Literal distance = new Literal("distance");

//		distance.addTerm(new NumberTermImpl(v.getDistance()));
		distance.addTerm(new NumberTermImpl(Y));
		addUniquePercept("abstraction_follower1", "distance", distance);
		
		Literal precedingPlatoonID = new Literal("precedingPID");
//		precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
		precedingPlatoonID.addTerm(new NumberTermImpl(precedingPID));
		addUniquePercept("abstraction_follower1", "precedingPID", precedingPlatoonID);
		
		Literal egoPlatoonID = new Literal("egoPID");
//		egoPlatoonId.addTerm(new NumberTermImpl(v.getegoPID());
		egoPlatoonID.addTerm(new NumberTermImpl(egoPID));
		addUniquePercept("abstraction_follower1", "egoPID", egoPlatoonID);

		
		Literal precedingSpeed = new Literal("precedingSpeed");
//		egoPlatoonId.addTerm(new NumberTermImpl(v.getegoPID());
		precedingSpeed.addTerm(new NumberTermImpl(new Random().nextInt(150)));
		addUniquePercept("abstraction_follower1", "precedingSpeed", precedingSpeed);

		Literal speed = new Literal("speed");
//		egoPlatoonId.addTerm(new NumberTermImpl(v.getegoPID());
		speed.addTerm(new NumberTermImpl(new Random().nextInt(150)));
		addUniquePercept("abstraction_follower1", "speed", speed);
		Y++;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#printvalues(ail.syntax.Literal)
	 */
	public void printvalues(Literal pred) {
		if (pred.getFunctor().equals("precedingPID")) {
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
//			   int agentnum = Integer.parseInt(((String) agName).substring(14));
			   Predicate predlist = (Predicate) act.getTerm(0);
//			   Predicate predargs = (Predicate) act.getTerm(1);
//			   int num_args = predargs.getTermsSize();
//			   String num_arg_s = "" + num_args;
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

				if (predname.equals("switch_from_platoon_to_manual"))
						egoPID = 0;
						System.out.println("set switch from platoon to manual by setting egoPID = 0 ");
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

