import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

public class LatencyTest {
    private static final int NUM_POINTS = 100;
    
    private final JFrame frame;
    private final Map<String, Color> websiteColors;
    private final int maxTimeout = 10000;
    
    private volatile Map<String, LinkedList<Integer>> websiteLatencies;
    
    private ChartPanel chartPanel;
    
    LatencyTest() {
        frame = new JFrame("Latency Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(1000, 500);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        
        websiteColors = new HashMap<String, Color>();
        websiteColors.put("http://google.com", new Color(0, 150, 0));
        websiteColors.put("http://facebook.com", new Color(0, 0, 250));
        //websiteColors.put("yoursitehere", new Color(0, 100, 100));
        
        websiteLatencies = new HashMap<String, LinkedList<Integer>>();
        for (Map.Entry<String, Color> entry : websiteColors.entrySet()) {
            websiteLatencies.put(entry.getKey(), new LinkedList<Integer>());
        }
    }

    public void run() {
        JLabel titleLabel = new JLabel("facebook.com vs google.com Latency");
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        
        frame.getContentPane().add(titleLabel);
        
        for (Map.Entry<String, Color> entry : websiteColors.entrySet()) {
            String website = entry.getKey();
            Color color = entry.getValue();
            
            JLabel websiteLabel = new JLabel();
            websiteLabel.setText(website);
            websiteLabel.setForeground(color);
            
            frame.getContentPane().add(websiteLabel);
        }
        
        chartPanel = new ChartPanel(websiteColors, websiteLatencies);
        frame.getContentPane().add(chartPanel);
        
        frame.setVisible(true);
        
        Timer timer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // Use ExecutorService to manage threads
                ExecutorService threadPool = Executors.newCachedThreadPool();
                
                for (Map.Entry<String, Color> entry : websiteColors.entrySet()) {
                    threadPool.submit(new MyPing(entry.getKey(), maxTimeout));
                }
                
                threadPool.shutdown();
            }
        });
        timer.setInitialDelay(0); // Just start right away
        timer.start();
    }

    public static void main(String args[]) {
    	LatencyTest latencyTest = new LatencyTest();
    	latencyTest.run();
    }
    
    /*
     * Creates a worker thread that establishes a network connection to read response times
     * and update the GUI later
     */
    public class MyPing implements Runnable {
    	private final String website;
    	private final int maxTimeout;
    	
    	MyPing(String website, int maxTimeout) {
    		this.website = website;
    		this.maxTimeout = maxTimeout;
    	}

		@Override
        public void run() {
			long elapsed = 0;
			
			try {
				// Set up the connection and get a response
				HttpURLConnection connection = (HttpURLConnection) new URL(website).openConnection();
		        connection.setConnectTimeout(maxTimeout/2);
		        connection.setReadTimeout(maxTimeout/2); // maxTimeout = ConnectTimeout + ReadTimeout
		        connection.setRequestMethod("HEAD"); // Don't need the response body
		        
		        long start = System.nanoTime();
		        connection.getResponseCode();
		        elapsed = System.nanoTime() - start;
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			elapsed = elapsed/1000000; // Convert the nanoseconds to milliseconds
			
			LinkedList<Integer> latencies = websiteLatencies.get(website);
            latencies.add((int) elapsed);
            if (latencies.size() > NUM_POINTS) {
                latencies.remove(); // Implements a list that keeps only the latest NUM_POINTS elements 
            }
            
            websiteLatencies.put(website, latencies);
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (chartPanel != null) {
                        frame.getContentPane().remove(chartPanel); // Remove previous graph if there is one
                    }
                    chartPanel = new ChartPanel(websiteColors, websiteLatencies);
                    frame.getContentPane().add(chartPanel); // Add new graph
                    chartPanel.revalidate();
                }
            });
        }
    }
}
