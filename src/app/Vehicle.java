package app;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Code;

import java.io.IOException;

import javax.jmdns.*;

/**
 * Vehicle model. Not using JmDNS 3.4.1 because it has some bugs, apparently, just use 3.4.0.
 * 
 *
 */
public class Vehicle{
	
	private String brokerAddress = "";
	private String licensePlate = "BUTTSEX";
	
	public Vehicle() throws IOException {

		System.out.println("Vehicle "  + licensePlate + ": discovering broker ...");
		brokerAddress = disoverBroker();
		
		if (brokerAddress.equals(""))
			System.out.println("Vehicle "  + licensePlate + ": could not find broker!");
		else
			System.out.println("Vehicle "  + licensePlate + ": found broker at " + brokerAddress);
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
