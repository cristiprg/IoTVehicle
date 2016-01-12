package app;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

import com.google.gson.Gson;

import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.OptionSet;

import java.io.IOException;
import java.net.ResponseCache;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import javax.jmdns.*;

/**
 * Vehicle model. Not using JmDNS 3.4.1 because it has some bugs, apparently, just use 3.4.0.
 * 
 *
 */
public class Vehicle{
	
	private String brokerAddress = "";
	private String licensePlate = "BUTTSEXXXXXXXXXXXXXXXXXX";
	private int parkingDuration = 0;
	CoapClient coapClient = null;
	
	private void log(String message){
		System.out.println("Vehicle "  + licensePlate + ": " + message);
	}
	
	public Vehicle() throws IOException {

		log("PART 1. discvering broker ...");
		
		int nrTries = 3;
		while (brokerAddress.equals("") && --nrTries >= 0){
			brokerAddress = disoverBroker();
		}
		
		if (brokerAddress.equals("")){
			log("could not find broker!");		
			
			// it dies
			return;
		}
	
		log("found broker at " + brokerAddress);

		// it's good, now connect to the coap server
		CoapClient coapClient = new CoapClient("coap://" + brokerAddress + ":5683");
		
		
	/*	Request request = new Request(Code.GET);
		OptionSet optionSet = new OptionSet();
		optionSet.addURIPath("/rd/XedGlUtyUN/6/0/0");
		request.setOptions(optionSet);
		CoapResponse coapResponse = coapClient.advanced(request);
		log("response: " + coapResponse.getResponseText());
		*/

		// TODO: change here to POST - register the vehicle
		Request request = new Request(Code.POST);
		OptionSet optionSet = new OptionSet();
		optionSet.addURIPath("Register");
		optionSet.addURIQuery("DriverID=" + licensePlate);
		request.setOptions(optionSet);

		log("PART 2. sending the registration request ...");
		CoapResponse coapResponse = coapClient.advanced(request);

		if (!ResponseCode.isSuccess(coapResponse.getCode())){
			log("could not register to broker!");
			
			// it dies
			return;
		}

		log("successufully registered to broker.");		
		
		log("PART 3. waiting for user input");
				
		// http://stackoverflow.com/questions/5287538/how-can-i-get-the-user-input-in-java
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.println("Enter desired parking duration in hours (Integer): ");
		parkingDuration = reader.nextInt(); // Scans the next token of the input as an int.
		
		
		//send int in post request named..?
		log("PART 4. sending request for list of parking spots ...");
		
		// prepare the request - GET /FreeParkingSpots?Duration=<duration>
		request = new Request(Code.GET);
		optionSet = new OptionSet();
		optionSet.addURIPath("FreeParkingSpots");
		optionSet.addURIQuery("Duration="+parkingDuration);
		request.setOptions(optionSet);
		coapResponse = coapClient.advanced(request);
		
		if (!ResponseCode.isSuccess(coapResponse.getCode())){
			log("could not retrieve list of parking spots from broker!");
			
			// it dies
			// TODO: just retry?
			return;
		}
		
		String receivedMessage = new String( coapResponse.getPayload(), StandardCharsets.UTF_8 );
		
		Gson gson = new Gson();
		ArrayList<String> freeParkingSpots = gson.fromJson(receivedMessage, ArrayList.class); 
				
		// NOT as the protocol - without the first "NumFreeSpots":INT
		log("received : " + freeParkingSpots);
	}
	
	private String disoverBroker() {
		String brokerServiceType = "_coap._udp.local.";
		String brokerAddress = "";
		JmDNS brokerService = null;
		try {
			// create instance of service
			brokerService = JmDNS.create();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}

		// listen for a while
		brokerService.addServiceListener(brokerServiceType, new SampleListener());
		
		// TODO: check out this thing
		// if there are more brokers, consider just the first
		ServiceInfo[] serviceInfos = brokerService.list(brokerServiceType);
		if (serviceInfos.length > 0)
			brokerAddress = serviceInfos[0].getAddress().getHostAddress();
			
		try {
			brokerService.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return brokerAddress;
	}

	/**
	 * TODO: 
	 * http://stackoverflow.com/questions/9276487/samples-with-jmdns/9286640#9286640
	 */
	static class SampleListener implements ServiceListener {
	        @Override
	        public void serviceAdded(ServiceEvent event) {
	            System.out.println("Service added   : " + event.getName() + "." + event.getType());
	        }

	        @Override
	        public void serviceRemoved(ServiceEvent event) {
	            System.out.println("Service removed : " + event.getName() + "." + event.getType());
	        }

	        @Override
	        public void serviceResolved(ServiceEvent event) {
	            System.out.println("Service resolved: " + event.getInfo());
	        }
	    }
	
	public static void main(String[] args) throws IOException{
		
		new Vehicle();		
		
		/*
		CoapClient client = new CoapClient("localhost:5683");
		
		//String text = client.get("/.well-knwon/core").getResponseText();
		Request request = new Request(Code.POST);
		request.setURI("/Register?ID=ABCD");
		CoapResponse response = client.advanced(request);

		System.out.println("Text = " + response.getCode());*/
				
    }

    
}
