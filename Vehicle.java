package ail.platoon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Random;

import ail.syntax.Unifier;
import ail.util.AILSocketServer;

public class Vehicle {
	/* Base port numbers - vehicle ID will be added to these */
	private int BASE_INBOUND_PORT = 7000;
	private int BASE_OUTBOUND_PORT = 8000;
	
	/* Inbound and outbound sockets, both are needed as Simulink can't handle bidirectional communication on a single socket */
	private AILSocketServer inbound;
	private AILSocketServer outbound;
	
	/* Data containers for ego (own vehicle), leader (via V2V) and preceding (via V2V) */
	public VehicleData ego = new VehicleData();
	public VehicleData leader = new VehicleData();
	public VehicleData preceding = new VehicleData();

	public String[] command_list = {"", 
									"send_message_too_close", 
									"send_message_too_far",
									"breaking_platoon",
									"set_distance_parameter_for_latency(0)",
									"set_distance_parameter_for_latency(1)",
									"set_distance_parameter_for_latency(2)",
									"set_distance_parameter_for_latency(3)",
									"request_to_join",
									"assign_platoon_id",
									"switch_cruise_to_platoon",
									"switch_platoon_to_cruise"};
	/* Set up our sockets */
	public Vehicle(int id) {
		ego.uniqueID = id;
		/* 
		 * IMPORTANT: Because these calls block on 'accept', we need to force Simulink to connect to the sockets in the right order.
		 * Use an atomic (or function-call) sub-system to encapsulate TCP Send/Receive blocks then use block priorities to force
		 * sorted execution order
		 */
		System.out.print("Vehicle " + id + " waiting for connection on port " + (BASE_INBOUND_PORT + id) + "... ");
		inbound = new AILSocketServer(BASE_INBOUND_PORT + id);
		System.out.println("Connected!");
		System.out.print("Vehicle " + id + " waiting for connection on port " + (BASE_OUTBOUND_PORT + id) + "... ");
		outbound = new AILSocketServer(BASE_OUTBOUND_PORT + id);
		System.out.println("Connected!");
	}

	public void execute(String command) {
		int command_number = 0;
		
		for(int i=0; i< command_list.length; i++){
			if(command.equals(command_list[i]))
				command_number= i;
		}
		
		/* Create buffer to transmit our command */
		ByteBuffer buf2 = ByteBuffer.allocate(4);
		buf2.order(ByteOrder.LITTLE_ENDIAN);
		buf2.putInt(command_number);
		
		/* Write to outbound socket */
		outbound.write(buf2);
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
		leader.timestamp = buf.getDouble();
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
		preceding.timestamp = buf.getDouble();
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
	
	public int getegoPID(){
		return ego.uniqueID;
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