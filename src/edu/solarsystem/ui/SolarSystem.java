package edu.solarsystem.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.solarsystem.model.Movable;
import edu.solarsystem.model.MyTime;

import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

public class SolarSystem extends JFrame {

	private static final String XMLFILE = "/solarsystem.xml";

	private JPanel contentPane;
	private Space space;
	
	private JButton buttonResumeTime;
	private JButton btnPauseTime;
	private JLabel lblDistanceStatus;
	private JButton btnZoomMore;
	private JButton btnZoomLess;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SolarSystem frame = new SolarSystem();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public SolarSystem() {
		setTitle("The Solar System");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        pack();
		setResizable(false);
		setVisible(true);
		setLocationRelativeTo(null);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		SUNX = screenSize.width/2;
		SUNY = screenSize.height/2;
		
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panelNorth = new JPanel();
		panelNorth.setBorder(new EmptyBorder(5, 20, 0, 0));
		panelNorth.setBackground(Color.BLACK);
		contentPane.add(panelNorth, BorderLayout.NORTH);

		space = new Space();
		contentPane.add(space, BorderLayout.CENTER);
		
		lblDistanceStatus = new JLabel();
		panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
		lblDistanceStatus.setForeground(Color.LIGHT_GRAY);
		panelNorth.add(lblDistanceStatus);
		updateDistanceStatus();
		
		lblTimeStatus = new JLabel();
		lblTimeStatus.setForeground(Color.LIGHT_GRAY);
		panelNorth.add(lblTimeStatus);
		
		
		JPanel panelEast = new JPanel();
		panelEast.setBackground(Color.BLACK);
		contentPane.add(panelEast, BorderLayout.EAST);
		panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));
		
		btnPauseTime = new JButton("Pause time | |");
		btnPauseTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyTime.getInstance().pause();
				btnPauseTime.setEnabled(false);
				buttonResumeTime.setEnabled(true);
			}
		});
		panelEast.add(btnPauseTime);
		
		buttonResumeTime = new JButton("Resume time |>");
		buttonResumeTime.setEnabled(false);
		buttonResumeTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyTime.getInstance().resume();
				buttonResumeTime.setEnabled(false);		
				btnPauseTime.setEnabled(true);
			}
		});
		panelEast.add(buttonResumeTime);
		
		btnZoomMore = new JButton("Zoom +");
		btnZoomMore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (ZOOM + 1 <= ZOOM_MAX) {
					ZOOM++;
					DSCALE = 15 * ZOOM/AU ;
					buildSpace();
					updateDistanceStatus();
				}
			}
		});
		panelEast.add(btnZoomMore);
		
		btnZoomLess = new JButton("Zoom -");
		btnZoomLess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ZOOM - 1 >= ZOOM_MIN) {
					ZOOM--;
					DSCALE = 15 * ZOOM/AU ;
					buildSpace();
					updateDistanceStatus();
				}				
			}
		});
		panelEast.add(btnZoomLess);
		
		btnMoveUp = new JButton("^ Move up");
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SUNY -= 10 ;
				buildSpace();				
			}
		});
		panelEast.add(btnMoveUp);
		
		btnMoveRight = new JButton("> Move right");
		btnMoveRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SUNX += 10 ;
				buildSpace();				
			}
		});
		panelEast.add(btnMoveRight);
		
		button = new JButton("< Move left");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SUNX -= 10 ;
				buildSpace();				
			}
		});
		panelEast.add(button);
		
		btnMoveDown = new JButton("v Move down");
		btnMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SUNY += 10 ;
				buildSpace();				
			}
		});
		panelEast.add(btnMoveDown);
		
		buildSpace();
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				updateTimeStatus();
			}	
		}, 0, 1000);

	}
	
	private void updateDistanceStatus() {
		lblDistanceStatus.setText("Distance scale : " + DSCALE + " [sun and planets are not in real scale]");		
	}

	private void updateTimeStatus() {
		long starttime = MyTime.getInstance().getStarttime();
		long offset = MyTime.getInstance().getOffset();
		offset = (long)(offset / TSCALE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(starttime + offset);
		lblTimeStatus.setText("Time scale : " + TSCALE + ", Date : " + new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
	}
	private void buildSpace() {
		space.removeAll();
		contentPane.remove(space);
		contentPane.add(space, BorderLayout.CENTER);
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
		    Document document = builder.parse(SolarSystem.class.getResource(XMLFILE).openStream());
		    XPath xPath =  XPathFactory.newInstance().newXPath();
		    
		    String expression = "/solarsystem/planet";
		    NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
		    for (int i = 0; i < nodeList.getLength(); i++) {
		    	NamedNodeMap attributes = nodeList.item(i).getAttributes();	
		    	String name = attributes.getNamedItem("name").getNodeValue();
		    	double sma = Double.parseDouble(attributes.getNamedItem("sma").getNodeValue());
		    	sma = sma * AU;
		    	double v = Double.parseDouble(attributes.getNamedItem("v").getNodeValue());
		    	double w = 0;
		    	int rscaled = (int)(DSCALE * 7 * Math.pow(10, 10));
		    	if(sma != 0) { // the sun
		    		w = v/sma;
		    		rscaled = (int)(DSCALE * 5 * Math.pow(10, 10));
		    	}
		    	Movable planet = new Movable(SUNX, SUNY, sma * DSCALE, 0, w/TSCALE);
				MovableUI planetui = new MovableUI(planet, new ImageIcon(SolarSystem.resize(ImageIO.read(SolarSystem.class.getResource("/" + name + ".png").openStream()), rscaled, rscaled)));
				planet.move();
				space.add(planetui);
		    }
		    /**
		     * Example of an inclined elliptic movement below, just uncomment for testing
		     */
//	    	double a = 4, b = 2;
//	    	a = a * AU;
//	    	b = b * AU;
//	    	double rotangle = Math.PI / 4;
//	    	double v = 5000000;
//	    	double w = v/((a + b)/2);
//	    	int rscaled = (int)(DSCALE * 5 * Math.pow(10, 10));
//	    	Movable planet = new Movable(SUNX, SUNY, a * DSCALE, b * DSCALE, rotangle, 0, w/TSCALE);
//			MovableUI planetui = new MovableUI(planet, new ImageIcon(SolarSystem.resize(ImageIO.read(SolarSystem.class.getResource("/spaceship.png").openStream()), rscaled, rscaled)));
//			planet.move();
//			space.add(planetui);

		} catch (Exception e) {
			e.printStackTrace();
		}				
		space.repaint();
	}

	private static final double AU = 15*Math.pow(10, 10);
	private static double ZOOM_MIN = 1;
	private static double ZOOM_MAX = 6;
	private static double ZOOM = ZOOM_MIN;
	private static double DSCALE = 15 * ZOOM/AU ;
	private static final double TSCALE = (double)50/(365*24*3600) ;

	private int SUNX = 800, SUNY = 500;
	
	private int LABEL_SIZE = 100;
	private JLabel lblTimeStatus;
	private JButton btnMoveUp;
	private JButton btnMoveRight;
	private JButton button;
	private JButton btnMoveDown;
	public static BufferedImage resize(BufferedImage image, int width, int height) {
	    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
	    Graphics2D g2d = (Graphics2D) bi.createGraphics();
	    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    g2d.drawImage(image, 0, 0, width, height, null);
	    g2d.dispose();
	    return bi;
	}
	
	class Space extends JPanel {

		public Space() {
			super();
			setBackground(Color.BLACK);
			setLayout(null);
		}
		
		
		@Override
		public void paintComponent (Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;

			g2.setColor(Color.GRAY);
	        // g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8}, 0));
	        
			try {
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
			    Document document = builder.parse(SolarSystem.class.getResource(XMLFILE).openStream());
			    XPath xPath =  XPathFactory.newInstance().newXPath();
			    
			    String expression = "/solarsystem/planet";
			    NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			    for (int i = 0; i < nodeList.getLength(); i++) {
			    	NamedNodeMap attributes = nodeList.item(i).getAttributes();	
			    	double sma = Double.parseDouble(attributes.getNamedItem("sma").getNodeValue());
			    	sma = sma * AU;
			    	int r = (int)(sma * DSCALE);
					g2.drawOval(SUNX -r + LABEL_SIZE/2, SUNY - r + LABEL_SIZE/2, r * 2 , r * 2 );
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}		

		}		
	}
	
	class MovableUI extends JLabel implements Observer{
		private Movable movable;
		
		public MovableUI(Movable movable, ImageIcon imageicon) {
			super(imageicon);
			this.movable = movable;
			
			this.movable.addObserver(this);
		}

		@Override
		public void update(Observable arg0, Object arg1) {
			this.setBounds((int)movable.getX(),(int)movable.getY(), LABEL_SIZE, LABEL_SIZE);
					
			this.repaint();
		}
	}
}