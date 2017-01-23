package eass.platooning.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import ail.util.AILSocketChannelServer;

public class Vehicle {
	/* Base port numbers - vehicle ID will be added to these */
	private int BASE_INBOUND_PORT = 17000;
	private int BASE_OUTBOUND_PORT = 18000;
	
	/* Inbound and outbound sockets, both are needed as Simulink can't handle bidirectional communication on a single socket */
	private AILSocketChannelServer inbound;
	private AILSocketChannelServer outbound;
	
	/* Data containers for ego (own vehicle), leader (via V2V) and preceding (via V2V) */
	public VehicleData ego = new VehicleData();
	public VehicleData leader = new VehicleData();
	public VehicleData preceding = new VehicleData();

	ArrayList<ExecutableAction> commandList = new ArrayList<ExecutableAction>();
	
//	public String[] command_list = {"", 
//									"send_message_too_close",
//									"send_message_too_far",
//									"breaking_platoon",
//									"set_distance_parameter_for_latency(0)",
//									"set_distance_parameter_for_latency(1)",
//									"set_distance_parameter_for_latency(2)",
//									"set_distance_parameter_for_latency(3)",
//									"assign_platoon_id",
//									"speed_controller_enabled",
//									"speed_controller_disabled",
//									"steering_controller_enabled",
//									"steering_controller_disabled",
//									"set_spacing"};
	
	
	/* Set up our sockets */
	public Vehicle(int id) {
		ego.uniqueID = id;
		ego.platoonID = 0;
		/* 
		 * IMPORTANT: Because these calls block on 'accept', we need to force Simulink to connect to the sockets in the right order.
		 * Use an atomic (or function-call) sub-system to encapsulate TCP Send/Receive blocks then use block priorities to force
		 * sorted execution order
		 */
		System.out.print("Vehicle " + id + " waiting for connection on port " + (BASE_INBOUND_PORT + id) + "... ");
		inbound = new AILSocketChannelServer(BASE_INBOUND_PORT + id);
		System.out.println("Connected!");
		System.out.print("Vehicle " + id + " waiting for connection on port " + (BASE_OUTBOUND_PORT + id) + "... ");
		outbound = new AILSocketChannelServer(BASE_OUTBOUND_PORT + id);
		System.out.println("Connected!");

		commandList.add(new ExecutableAction("speed_controller", 1, 1));
		commandList.add(new ExecutableAction("steering_controller", 2, 1));
		commandList.add(new ExecutableAction("set_spacing", 3, 1));
		commandList.add(new ExecutableAction("stop", 4, 0));
		commandList.add(new ExecutableAction("join_ok", 5, 1));
		commandList.add(new ExecutableAction("join_position", 6, 1));
		
		
	}

	// command composed of three attributes: commandID (int), number of arguments (int) and the value of arguments (double)
	// if commandID == 0, there is no equivalent command in command_list, 
	public void execute(String command, int num_args, double[] cmd_args) {
		int command_number = 0;
		
		if(command.equals("assign_platoon_id")){
			ego.platoonID = (int) cmd_args[0];
		}else{
		
			for (ExecutableAction ea : commandList) {
				if(ea.actionName.equals(command))	
					command_number = ea.actionID;
			}
		
			if(command_number != 0){
				/* Create buffer to transmit our command */
				ByteBuffer buf2 = ByteBuffer.allocate(16);
				buf2.order(ByteOrder.LITTLE_ENDIAN);
				buf2.putInt(command_number);
				buf2.putInt(num_args);
				if(num_args != 0){
					for(int i=0; i< num_args; i++)			
						buf2.putDouble(cmd_args[i]);
				}
				/* Write to outbound socket */
				outbound.write(buf2);
			}
		}
//		System.out.println("\n command_number is "+ command_number+ " and it is "+ command);		
	}
	
	
	/* Update our data and send a command */
	public boolean update() {
		
		/* Set up a receive buffer */
		ByteBuffer buf = ByteBuffer.allocate(33*8);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		/* Read from the inbound socket, if there's no data then we fail out */
		if (inbound.read(buf) <= 0) {
			return false;
		}
		
		/* Decode the data on the buffer */
		/* Ego data */
		ego.timestamp = buf.getDouble();
		ego.distance = buf.getDouble();
		ego.speed = buf.getDouble();
		ego.acceleration = buf.getDouble();
		ego.lateralAcceleration = buf.getDouble();
		ego.yawRate = buf.getDouble();
		ego.heading = buf.getDouble();
		
		ego.range = buf.getDouble();
		ego.azimuth = buf.getDouble();
		ego.lateralPosition = buf.getDouble();
		ego.headingError = buf.getDouble();
		ego.roadCurvature = buf.getDouble();
		
		/* Leader data */
		leader.timestamp = buf.getDouble(); // presenting leader latency
		leader.distance = buf.getDouble();
		leader.speed = buf.getDouble();
		leader.acceleration = buf.getDouble();
		leader.lateralAcceleration = buf.getDouble();
		leader.yawRate = buf.getDouble();
		leader.heading = buf.getDouble();
		
		leader.lateralPosition = buf.getDouble();
		leader.headingError = buf.getDouble();
		leader.roadCurvature = buf.getDouble();
		
		/* Preceding data */
		preceding.platoonID = (int)buf.getDouble();
		preceding.timestamp = buf.getDouble(); // presenting preceding latency
		preceding.distance = buf.getDouble();
		preceding.speed = buf.getDouble();
		preceding.acceleration = buf.getDouble();
		preceding.lateralAcceleration = buf.getDouble();
		preceding.yawRate = buf.getDouble();
		preceding.heading = buf.getDouble();
		
		preceding.range = buf.getDouble();
		preceding.azimuth = buf.getDouble();
		preceding.lateralPosition = buf.getDouble();
		
		return true;
	}
	
	public double getDistance(){
		return ego.range;
	}

	public int getID(){
		return ego.uniqueID;
	}
	
	public int getprecedingPID(){
		return preceding.platoonID;
	}
	
	public int getegoPID(){ // IT SHOULD BE MODIFIED
		return ego.platoonID;
	}
	
	public double getprecedingSpeed(){
		return ego.speed;
	}
	
	public double getSpeed(){
		return preceding.speed;
	}
	
	public double getAzimuth(){
		return (Math.abs(ego.azimuth)*100);
	}
	
	public int getTimeStamp(){
		return (int) ego.timestamp;
	}
	
	public double getLeaderLatency(){
		double latency = leader.timestamp;
		return latency;
	}
	
	public double getPrecedingLatency(){
		double latency = preceding.timestamp;
		return latency;
	}
	
	public double getLateralPosition(){
		double lateral = ego.lateralPosition;
	//	System.out.println(Math.abs(lateral));
		return Math.abs(lateral);
	}
	
	// PRIVATE INNER CLASS 
	private class ExecutableAction {
		String actionName;
		int actionID;
		int actionArgumentNumber;
		
		public ExecutableAction(String actionName, int actionID, int actionArgumentNumber){
			this.actionName = actionName;
			this.actionID = actionID;
			this.actionArgumentNumber = actionArgumentNumber;
		}
		
	}
	
	

//	
//	/* Quick example usage */
//	public static void main(String [] args) {
//		/*
//		 * Vehicle instantiation must be in the same order that the vehicles are initiated in Simulink.
//		 * This is because the service.accept() call blocks until the relevent Simulink block is connected
//		 */
//		Vehicle v = new Vehicle(3);
////		Vehicle v2 = new Vehicle(5);
//		
//		/* Loop forever and update from Simulink, read will block so the processes naturally sync */
//		while (true) {
//			
//			/*
//			 * Call update with a random integer (as we have no commands to send yet!)
//			 * These calls also need to be in the order than Simulink executes the blocks
//			 * (check sorted executed order)
//			 */
//			if (v.update((new Random()).nextInt(10))) { //) && v2.update((new Random()).nextInt(10))) {
//				/* Print out some information */
//				System.out.print(String.format("%.1f",v.ego.timestamp) + 
//						"s: Vehicle " + v.ego.uniqueID + " is travelling at " +
//						String.format("%.1f", v.ego.speed) + "m/s and is ");
//				if (v.ego.range < 100) {
//					System.out.print(String.format("%.2f", v.ego.range) + "m");
//				} else {
//					System.out.print("not");
//				}
//				System.out.println(" behind another vehicle");
///*				System.out.println(String.format("%.1f",v2.ego.timestamp) + 
//						"s: Vehicle " + v2.ego.uniqueID + " is travelling at " +
//						String.format("%.1f", v2.ego.speed) + "m/s and is " +
//						String.format("%.2f", v2.ego.range) + "m behind another vehicle");*/
//			} else{
//				return;
//			}
//		}
//	}

	
}