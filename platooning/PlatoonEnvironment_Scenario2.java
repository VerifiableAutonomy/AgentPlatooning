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



public class PlatoonEnvironment_Scenario2 extends EASSMatLabEnvironment {
	String logname = "eass.platooning.PlatoonEnvironment";

	
//	VerifyMap<String, Vehicle> vehicles = new VerifyMap<String, Vehicle>();
	
//	Vehicle v = new Vehicle(2);
	int Y = 1;
	int precedingPID=2;
	int egoPID=0;
	int egoID=3;
	boolean agreement=false;
	
	Random r = new Random();

//	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	
	
	/**
	 * Constructor.
	 *
	 */
	public PlatoonEnvironment_Scenario2() {
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
		if (!connectedtomatlab) {
			NActionScheduler s = (NActionScheduler) getScheduler();
			removePerceptListener(s);
			ActionScheduler s1 = new ActionScheduler();
			s1.addJobber(this);
			setScheduler(s1);
			addPerceptListener(s1);
		}
	}

	public void eachrun() {

		if (Y == 10){
			Y=1;
			agreement= r.nextBoolean();
	//		egoPID = r.nextInt(2);
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
		if(agreement){
			   Literal LeaderAgreement = new Literal("leaderAgreement");
//				egoPlatoonId.addTerm(new NumberTermImpl(v.getegoPID());
			   int pid= 3;
			   LeaderAgreement.addTerm(new NumberTermImpl(egoID));
			   LeaderAgreement.addTerm(new NumberTermImpl(pid));
			   addPercept("abstraction_follower1",LeaderAgreement);
			   agreement= false;
		}
		
		
		Literal distance = new Literal("distance");
//		distance.addTerm(new NumberTermImpl(v.getDistance()));
		distance.addTerm(new NumberTermImpl(Y));
		addUniquePercept("abstraction_follower1", distance);		
		
		Literal precedingPlatoonID = new Literal("precedingPID");
//		precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
		precedingPlatoonID.addTerm(new NumberTermImpl(precedingPID));
		addPercept("abstraction_follower1",precedingPlatoonID);
		
		Literal egoPlatoonID = new Literal("egoPID");
//		egoPlatoonId.addTerm(new NumberTermImpl(v.getegoPID());
		egoPlatoonID.addTerm(new NumberTermImpl(egoPID));
		addPercept("abstraction_follower1",egoPlatoonID);
		
		Y++;
		
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
	
//	public void noconnection_run(String agname, Action act) {
//		if (act.getFunctor().equals("run")) {
//			   Predicate predlist = (Predicate) act.getTerm(0);
//			   Predicate predargs = (Predicate) act.getTerm(1);
//			   int num_name_comps = predlist.getTermsSize();
//			   String predname = "";
//			   for (int i=0; i<num_name_comps; i++) {
//				   // Lots of extra work to get the string correct.
//				   Term nb = predlist.getTerm(i);
//				   String s = nb.toString();
//				   if (nb instanceof VarTerm) {
//					   VarTerm v = (VarTerm) nb;
//					   nb = v.getValue();
//				   }
//				   if (nb instanceof NumberTerm) {
//					   NumberTerm num = (NumberTerm) nb;
//					   Double number = num.solve();
//					   int num_as_int = number.intValue();
//					   s = ((Integer) num_as_int).toString();
//				   }
//				   if (nb instanceof StringTermImpl) {
//					   StringTermImpl string = (StringTermImpl) nb;
//					   s = string.getString();
//				   }
//				   predname += s;
//			   }
//			   
//			   Term arg = predargs.getTerm(0);
////			   String s = arg.toString();
//			   
////			   if(predname.equals("request_to_join")) {
////				    agreement= true;	
////					Literal req_join = new Literal("request_to_join");
//////					egoPlatoonId.addTerm(new NumberTermImpl(v.getegoPID());
////					addPercept("abstraction_follower",req_join);
////
////			   }
//			
//			   
//			}
//			System.err.println("No Matlab Connection: " + act.toString());		
//		}
	


}

