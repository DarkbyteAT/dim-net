package com.darkbyteat.net;

import java.io.IOException;

public interface NetworkListener {
	//Runs when a packet is received
	void onReceive(Packet packet) throws IOException;
}