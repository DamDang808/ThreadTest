package org.example.demo;

public class DaemonFolder extends Thread {

    public DaemonFolder() {
        setDaemon(true);
        start();
    }

}
