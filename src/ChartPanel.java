import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.*;

public class ChartPanel extends JPanel {
	private static final int NUM_POINTS = 100;
	private static final int LEFT_PADDING = 120;
	private static final int RIGHT_PADDING = 50;
	private static final int TOP_PADDING = 25;
	private static final int BOTTOM_PADDING = 50;
	private static final int POINT_WIDTH = 3;
	
	private final Map<String, Color> websiteColors;
	
	private int maxLatency;
	private Map<String, LinkedList<Integer>> websiteLatencies;
	
	public ChartPanel(Map<String, Color> websiteColors, Map<String, LinkedList<Integer>> websiteLatencies) {
	    this.websiteColors = websiteColors;
        this.websiteLatencies = websiteLatencies;
        
        maxLatency = 300;
        for(Map.Entry<String, LinkedList<Integer>> entry : websiteLatencies.entrySet()) {
            LinkedList<Integer> latencies = entry.getValue();
            if (latencies.size() > 0) {
                int max = Collections.max(latencies);
                if (max > maxLatency) {
                    maxLatency = ((max + 100)/100)*100 + 100; // Round up to nearest hundred and add some buffer too
                } 
            }            
        }
	}
	
    public void paintComponent(Graphics gl) {
        Graphics2D g = (Graphics2D) gl;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(222, 222, 222));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(new Color(250, 0, 0));
        
        g.drawLine(LEFT_PADDING, TOP_PADDING, LEFT_PADDING, this.getHeight()-BOTTOM_PADDING);
        g.drawLine(LEFT_PADDING, this.getHeight()-BOTTOM_PADDING, this.getWidth()-RIGHT_PADDING, this.getHeight()-BOTTOM_PADDING);
        
        g.drawString("Latency (ms)", 10, (this.getHeight() - TOP_PADDING - BOTTOM_PADDING)/2 + TOP_PADDING);
        g.drawString("Time", (this.getWidth() - LEFT_PADDING - RIGHT_PADDING)/2 + LEFT_PADDING - 10, this.getHeight() - 25);
        
        drawTicks(g);
        
        double xScale = ((double) (getWidth() - LEFT_PADDING - RIGHT_PADDING) / (NUM_POINTS - 1));
        double yScale = ((double) (getHeight() - TOP_PADDING - BOTTOM_PADDING) / (maxLatency - 1));
        
        // For each website, draw it's corresponding line graph
        for (Map.Entry<String, Color> entry : websiteColors.entrySet()) {
            String website = entry.getKey();
            Color color = entry.getValue();
            LinkedList<Integer> latencies = websiteLatencies.get(website);
            
            // Convert the latencies into a more meaningful form like points on a graph 
            ArrayList<Point> graphPoints = new ArrayList<Point>();
            for (int i=0; i<latencies.size(); i++) {
                int x1 = (int) (i*xScale) + LEFT_PADDING;
                int y1 = (int) ((maxLatency - latencies.get(latencies.size()-i-1))*yScale) + TOP_PADDING; // Read latest
                graphPoints.add(new Point(x1, y1));
            }
            
            drawLines(g, color, graphPoints);
        }        
    }
    
    /**
     * Draw and label the ticks on the Y axis
     * 
     * @param g
     */
    private void drawTicks(Graphics2D g) {
        // Create ticks and labels for Y Axis
        for (int i=0; i<=10; i++) {
            int x0 = LEFT_PADDING;
            int y0 = getHeight() - BOTTOM_PADDING - ((i*(getHeight() - TOP_PADDING - BOTTOM_PADDING))/10);
            int x1 = POINT_WIDTH + LEFT_PADDING;
            int y1 = y0;
            g.drawLine(x0, y0, x1, y1);
            g.drawString(String.valueOf(maxLatency*i/10), LEFT_PADDING - 30, y0 + 5);
        }
    }
    
    /**
     * Draw the line on the graph
     * 
     * @param g
     * @param color
     * @param graphPoints
     */
    private void drawLines(Graphics2D g, Color color, ArrayList<Point> graphPoints) {
        g.setColor(color);
        for (int j=0; j<graphPoints.size()-1; j++) {
            int x1 = graphPoints.get(j).x;
            int y1 = graphPoints.get(j).y;
            int x2 = graphPoints.get(j + 1).x;
            int y2 = graphPoints.get(j + 1).y;
            g.drawLine(x1, y1, x2, y2);
        }
    }
}
