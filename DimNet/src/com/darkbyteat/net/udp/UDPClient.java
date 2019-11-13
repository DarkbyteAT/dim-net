package com.darkbyteat.net.udp;

import java.io.IOException;
import java.net.DatagramSocket;

import com.darkbyteat.net.NetworkListener;

public class UDPClient extends UDPConnection {

	//Constructor for the client, connects to the IP and port specified
	//Creates a TCP connection using these parameters
	public UDPClient(NetworkListener listener, final String ip, final int port) throws IOException {
		super(new DatagramSocket(), listener, ip, port);
	}
}