package eass.platooning;

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
import eass.mas.DefaultEASSEnvironment;

//import eass.mas.matlab.EASSMatLabEnvironment;


public class PlatoonEnvironment extends DefaultEASSEnvironment{
	String logname = "eass.platooning.PlatoonEnvironment";

	
//	VerifyMap<String, Vehicle> vehicles = new VerifyMap<String, Vehicle>();
	
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
		
//		v3.update((new Random()).nextInt(10));		
		v3.update();		
		v3.execute("");
		Literal distance = new Literal("distance");
		distance.addTerm(new NumberTermImpl(v3.getDistance()));
		addUniquePercept("abstraction_follower", distance);		
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
		 
	   if (act.getFunctor().equals("assert_shared")) {
		   addSharedBelief(agName, new Literal(true, new PredicatewAnnotation((Predicate) act.getTerm(0))));
		   printed = true;
	   } else  if (act.getFunctor().equals("remove_shared")) {
		   removeSharedBelief(agName, new Literal(true, new PredicatewAnnotation((Predicate) act.getTerm(0))));
		   printed = true;
	   } else  if (act.getFunctor().equals("remove_shared_unifies")) {
		   removeUnifiesShared(agName, new Literal(true, new PredicatewAnnotation((Predicate) act.getTerm(0))));
	   } else if (act.getFunctor().equals("run")) {
		   if (connectedtomatlab) {
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
			   v3.execute(predname);
		   }  			   
	   } else if (act.getFunctor().equals("perf")) {
		   Predicate run = (Predicate) act.getTerm(0);
		   Message m = new Message(2, agName, "abstraction", run);
		   String abs = abstractionengines.get(agName);
		   addMessage(abs, m);
	   } else if (act.getFunctor().equals("query")) {
		   Predicate query = (Predicate) act.getTerm(0);
		   Message m = new Message(2, agName, "abstraction", query);
		   String abs = abstractionengines.get(agName);
		   addMessage(abs, m);
	   }  else	if (act.getFunctor().equals("append_string_pred")) {
    		StringTerm x = (StringTerm) act.getTerm(0);
    		String y =  act.getTerm(1).toString();
    		String append = x.getString() + y;
    		VarTerm result = (VarTerm) act.getTerm(2);
    		StringTermImpl z = new StringTermImpl(append);
    		u.unifies(result, z);
    		printed = true;
    	} else {
     		 u = super.executeAction(agName, act);
    		 printed = true;
    	}
	   
	   if (!printed) {
		   AJPFLogger.info("eass.mas.DefaultEASSEnvironment", agName + " done " + printAction(act));
	   }
	   return u;
	  }	  		
}

