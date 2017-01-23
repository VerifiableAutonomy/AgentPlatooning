package eass.platooning.util;

public class VehicleData {
	int uniqueID;
	int platoonID;
	
	/* Onboard (CAN) Data */
	double timestamp;
	double distance;
	double speed;
	double acceleration;
	double lateralAcceleration;
	double yawRate;
	double heading;
	
	/* Sensor data */
	double range;
	double azimuth;
	double lateralPosition;
	double headingError;
	double roadCurvature;
}