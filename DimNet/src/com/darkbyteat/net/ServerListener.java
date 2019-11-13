package com.darkbyteat.net;

public interface ServerListener extends NetworkListener {
	//Runs when a client joins
	void onJoin(String id);
}