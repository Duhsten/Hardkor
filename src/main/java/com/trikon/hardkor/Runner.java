package com.trikon.hardkor;

public class Runner implements Runnable {
	 
    // We are creating anew class that implements the Runnable interface,
    // so we need to override and implement it's only method, run().
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        StatTrack.getInstance().updatePlayers();
       Hardkor.getInstance().trackPlayersHealth();
    }
}