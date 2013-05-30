import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import javax.swing.JOptionPane;

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit;
import java.util.*;
import java.lang.Double;
import java.lang.StringBuffer;
import javax.swing.border.BevelBorder;
import java.awt.Dimension;
import javax.swing.event.*;


/**
 * A very simple toy application with an intuitive UI.
 * We start with a simple class which will contain main,
 * create and launch the gui and serve as a container 
 * for the other classes specific to this project.
 * And lets use easily remembered variables names like:
 */
class HappyHackingConverter
{
	//Global variables:
	private static String minus = "-";
	private static String blank = "";
	
	
	/**
	 * I would describe the default widget colors used as soul numbingly gray.
	 * So instead we are going to use this variable as the background color throughout our toy app.
	 */
	private final static Color white = new Color(255,255,255);
	
	/**
	 * All classes in this toy app will be contained in this class.
	 * As they are specific to this projet and should have access to shared variables like the background color.
	 * And if we do decide to share them with other projects we can always refactor them out.
	 */

	 
	  /*
	 A single instance of this class will be shared 
	 between all units of any one type. 
	 Thus centemeters, meters, kilometers, etc share one intance.
	 */
	 class ConvertibleValue
	 {
	 public ConvertibleValue()
	 {
	 value = 1; //Default 
	 }
	 public double getValue()
	 {
	 return value;
	 }
	 public void setValue(double v)
	 {
	 value = v;
	 }
	 protected double value;
	 }
	 
	
	 interface Converter
	 {
		public double timesValue(double value);
		public double divValue(double value);
		public double getFactor();
	 }
	 
	 /*
	 An enum to handle conversion between distance units.
	 */
	 enum DistanceMultipliers implements Converter
	 {
		 METERS(1),
		 MILIMETERS(1000),
		 CENTIMETERS(100),
		 KILOMETERS(0.001),
		 MILES(0.00062137273),
		 YARDS(1.09361),
		 FEET(3.28084),
		 INCHES(39.3701);
		 private final double factor;
		 private DistanceMultipliers(double f)
		 {
			factor = f;
		 }
		 public double timesValue(double value)
		 {
		 return (value * factor);
		 }
		 public double divValue(double value)
		 {
		 return (value / factor);
		 }
		 public double getFactor()
		 {
		 return factor;
		 }
	 }
	 
	 enum TemperatureScales
	 {
		CENTIGRADE, FAHRENHEIT
	 }
	 
	 class TemperatureFactors implements Converter
	 {
		TemperatureScales scale;
		public TemperatureFactors(TemperatureScales s)
		{
			scale = s;
		}
		public double timesValue(double value)
		{
			switch(scale)
			{
				case CENTIGRADE: return value;
				case FAHRENHEIT: return ((value * (9.0/5.0)) + 32.0);
				default: return value;
			}
		}
		
		public double divValue(double value)
		 {
			switch(scale)
			{
				case CENTIGRADE: return value;
				case FAHRENHEIT: return ((value - 32.0) * (5.0/9.0));
				default: return value;
			}
		 }
		 
		 public double getFactor()
		 {
			//FIXME: We should probably assert here.
			return 0; //Not an actual multiplilcative factor.
		 }
	 }
	 
	 
	 /*
     * A frame class to contain all the visual components.
	 * This class also provides a way for force the redrawing
	 * of everthing inside it. As the user modifies any unit
	 * all other units will be updated and forced to redraw.
     */
	class CFrame extends JFrame 
	{
		public CFrame() 
		{
			super("Converter between Imperial and Metric.");
			
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			JMenu menu = new JMenu("Application");
			menu.setBackground(white);
			menu.setMnemonic(KeyEvent.VK_A);
			
			//quit item
			JMenuItem item = new JMenuItem("Quit");
			item.setBackground(white);
			item.setMnemonic(KeyEvent.VK_Q);
			item.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					System.out.println("Quit request");
					System.gc();
					System.exit(0);
				}
			}
			);
			menu.add(item);

			JMenuBar menuBar = new JMenuBar();
			menuBar.setBackground(white);
			menuBar.add(menu);
			setJMenuBar(menuBar);
			
			setVisible(true);
		}//end of CFrame constructor
		
		/*
		* Called to force redrawing of everything.
		*/
		public void forceRedraw()
		{
			getContentPane().validate();
			getContentPane().repaint();
		}
		
	}//end of CFrame class.
	 
	 
	 
	/**
	 * We will want to prevent input of invalid values.
	 * A validator will allow us to correct invalid input when the widget is about to lose focus.
	 * How ever what we want is to prevent the appearence of invalid input at all.
	 * This is why we need a customized document filter.
	 * This class also takes a refernce to the shared convertible value instance.
	 * A multiplier instance to do the actual conversions.
	 * And a pointer to the parent frame so that this instance can force the redraw of
	 * everything contained in the frame.
	 * And with this class we are  going to start using the leading C (as in Convert) convention in class naming. 
	 */
	 class CDocumentTemperatureFilter extends DocumentFilter
	 {		
		public CDocumentTemperatureFilter(ConvertibleValue cv, Converter m, CFrame frame)
		{
			super();
			updateValue = true;
			cValue = cv;
			multiplier = m;
			topContainer = frame;
			System.out.println("Constructed CDocumentTemperatureFilter");
		}
		protected ConvertibleValue cValue;
		protected Converter multiplier;
		protected CFrame topContainer;
		
		public Converter getConverter()
		{
		return multiplier;
		}
		
		//This function inserts or replaces the user's input in a string buffer, to create the text which would be on screen if we allowed it.
		//We declare to throw bad location due to doc.getText, but since the paramters are coming from the farmework, that should never happen.... in theory.
		//If insert is true we insert, if false we replace. 
		//Length parameter only used when replacing.
		private StringBuffer getTextPrototype(boolean insert, FilterBypass fb, int offs,
                             String str, int length) throws BadLocationException
				{
					Document doc = fb.getDocument();
					String text = doc.getText(0, doc.getLength());
					StringBuffer sb = new StringBuffer(text);
					if(insert) 
					{ 
						sb.insert(offs, str); 
					}
					else
					{
						sb.replace(offs, offs+length, str);
					}
					return sb;
				}//end of getTextPrototype
		
		//This function attempts to convert the text to a double.
		//It returns a valid double if successful, NaN otherwise.
		protected double getDouble(boolean insert, FilterBypass fb, int offs,
                             String str, int length) throws BadLocationException
				{
					StringBuffer sb = getTextPrototype(insert, fb, offs, str, length);
					if(sb.toString().isEmpty())
					{
					return 0; //We treat an empty string as 0.
					}
					try
					{
						return Double.parseDouble(sb.toString()); 
					}//try
					catch(NumberFormatException ex)
					{
						Toolkit.getDefaultToolkit().beep();//FIXME: remvoe this.
						return Double.NaN; //String was not a number.
					}
				}//End of get double
				
		//This is supposed to check if the resulting text would be a valid temperature. 
		//For simplicity's sake that just means any valid double. 
		private boolean isValidTemperature(boolean insert, FilterBypass fb, int offs,
                             String str, int length) throws BadLocationException
				{
					//Allow staring a temperature with a - (minus), or deleting everything and ending up with an empty field.
					StringBuffer sb = getTextPrototype(insert, fb, offs, str, length);
					if(minus.contentEquals(sb) || blank.contentEquals(sb))
					{
						return true; //We allow "-" as a valid temperature despite the fact that it is not a valid double.
					}
					else
					{
						double d = getDouble(insert, fb, offs, str, length);
						return !Double.isNaN(d);//If it is NOT a NaN, then it is a valid temperature.
					}
				}//End of check if valid temperature
				
		//The shared value instanc is update only when this is true.
		public void setUpdateValue(boolean b)
		{
			updateValue = b;
		}
		protected boolean updateValue;
		
		// Updates the shared value instance if updateValue is true.
		// Always forces the redraw of everything.
		protected void doValueUpdate(FilterBypass fb)
		throws BadLocationException
		{
			Document doc = fb.getDocument();
			String text = doc.getText(0, doc.getLength());
			if(text.isEmpty())
			{
				text = "0";
			}
			
			if(updateValue == true)
				{
					try
					{
					System.out.printf("doValueUpdate text coming is is: %s \n", text);
					Double value = new Double(text);
					double newValue = multiplier.divValue(value.doubleValue());
					cValue.setValue(newValue);
					System.out.printf("doValueUpdate new string value = %s \n", Double.toString(newValue));
					}
					catch (NumberFormatException e)
					{
					//do nothing, since we allow '-'
					}
				}
				else
				{
					System.out.printf("NotDoValueUpdate new string value = %s \n", text);
				}
				topContainer.forceRedraw();
		}
		
		public void insertString(FilterBypass fb, int offs,
                             String str, AttributeSet a)
        throws BadLocationException 
		{
			System.out.printf("in DocumentSizeFilter's insertString method %s \n", str);
			if(isValidTemperature(true, fb, offs, str, 0))
			{
				fb.insertString(offs, str, a);
				doValueUpdate(fb);
			}
		}//insertString
    
		public void replace(FilterBypass fb, int offs,
							int length, 
							String str, AttributeSet a)
			throws BadLocationException 
		{
			System.out.printf("in DocumentSizeFilter's replace method %s \n", str);
			if(isValidTemperature(false, fb, offs, str, length))
			{
				fb.replace(offs, length, str, a);
				doValueUpdate(fb);
			}
		}//replace
		
		public void remove(DocumentFilter.FilterBypass fb,
                   int offs,
                   int length)
            throws BadLocationException	
		{
			System.out.println("in DocumentSizeFilter's remove method.");
			//Insertion is just replacement of a 0 lenght string.
			//Delete is a kind of insertion. 
			//It's just replacement of non-zero lenght string with a 0 lenght string.
			//So we will insert a 0 length text.
			String str ="";
			//And we pass in false for insertion which means replacement.
			if(isValidTemperature(false, fb, offs, str, length))
			{
				fb.remove(offs, length);
				doValueUpdate(fb);
				System.out.printf("Removed from %d to %d \n", offs, length);
			}
		}//End of remove
	 }//End of temperature filter. 
	
	 /*
	 * This class will be used for weights and distances.
	 * The math for negative weight and distances works just fine.
	 * But we will not allow the user to enter negative weigths and distances.
	 * So this filter is a stricter version of the temperature filter.
	 */
	 class CDocumentPositiveNumberFilter extends CDocumentTemperatureFilter
	 {
		public CDocumentPositiveNumberFilter(ConvertibleValue cv, Converter m, CFrame frame)
		{
			super(cv, m, frame);
			System.out.printf("Constructed CDocumentPositiveNumberFilter with multiplier %f \n", m.getFactor());
		}
		
		private boolean isValidPostiveNumber(boolean insert, FilterBypass fb, int offs,
                             String str, int length) throws BadLocationException
		{
			double d = getDouble(insert, fb, offs, str, length);
			//In order evaluation, AND should test for NaN first.
			if (!Double.isNaN(d) && d >= 0)
			{
				return true;
			}
			return false;
		}//End of isValidPostiveNumber
		
		public void remove(DocumentFilter.FilterBypass fb,
                   int offs,
                   int length)
            throws BadLocationException
		{
		System.out.println("In CDocumentPositiveNumberFilter remove.");
			String str ="";
			if(isValidPostiveNumber(false, fb, offs, str, length))
			{
				fb.remove(offs, length);
				doValueUpdate(fb);
				System.out.printf("Removed from %d to %d \n", offs, length);
			}
		}//End of remove
		
		public void insertString(DocumentFilter.FilterBypass fb,
                         int offs,
                         String str,
                         AttributeSet a)
                  throws BadLocationException
		{
			if(isValidPostiveNumber(true, fb, offs, str, 0))
			{
				fb.insertString(offs, str, a);
				doValueUpdate(fb);
			}
		}//End of insert
		
		public void replace(DocumentFilter.FilterBypass fb,
                    int offs,
                    int length,
                    String str,
                    AttributeSet a)
             throws BadLocationException
		{
			if(isValidPostiveNumber(false, fb, offs, str, length))
			{
				fb.replace(offs, length, str, a);
				doValueUpdate(fb);
			}
		}//End of replace
	 }//end of positive number filter
	 

	 /*
	 We will customize JTextPane so that it takes references
	 to a shared convertible value instance and to a converter instance.
	 */
	 class CTalkativeTextPane extends JTextPane 
	 {
		public CTalkativeTextPane(ConvertibleValue V, CDocumentTemperatureFilter F)
		{
		super();
		value = V; //Shared value instance
		setPreferredSize(new Dimension(100,25));
		setBorder(new BevelBorder(BevelBorder.RAISED));
		filter = F;
		((AbstractDocument)getStyledDocument()).setDocumentFilter(filter);;
		multiplier = filter.getConverter();
		}
		protected CDocumentTemperatureFilter filter;
		
		/*
		Gets the displayed value, and uses the converter to convert the
		shared value to units this instance is supposed to represent.
		If the displayed value and shared value are different,
		then we turn off our document filter and update our value
		to match the shared value. We re-enable to document filter afterwards.
		*/
		@Override
        protected void paintComponent(Graphics g) 
		{
			super.paintComponent(g);
			try
			{
				StyledDocument doc = getStyledDocument();
				System.out.printf("The fucking multiplier is: %f \n", multiplier.getFactor());
				double actualValue = (multiplier.timesValue(value.getValue()));
				String actualValueAsString = Double.toString(actualValue);
				
				String displayedValue;
				try
				{
				displayedValue = doc.getText(0, doc.getLength());
				System.out.printf("Paint cdisplayedValuee = %f \n", actualValue);
				}
				catch(BadLocationException e)
				{
				displayedValue = "";
				//Beep?
				}
				if(displayedValue.isEmpty())
				{
				displayedValue = "0";
				}
				double displayedValueAsDouble = Double.parseDouble(displayedValue);
				if(actualValue != displayedValueAsDouble)
				{
				System.out.println("Repaint required");
					try 
					{ 
						filter.setUpdateValue(false);
						doc.remove(0, doc.getLength());
						System.out.printf("XXXX Called remove from %d to %d \n", 0, doc.getLength());
						doc.insertString(0, actualValueAsString, null); 
						System.out.printf("Paint component actualValue = %s \n", actualValueAsString);
				
					}
					catch(BadLocationException e) 
					{
						//Beep?
					}
					finally
					{
					filter.setUpdateValue(true);
					}
				}
			}			
			catch (NumberFormatException e)
			{
			System.out.println(e);
			}
						
        }

        @Override
        public void repaint(long tm, int x, int y, int width, int height) {
            // This forces repaint to repaint the entire TextPane.
            super.repaint(tm, 0, 0, getWidth(), getHeight());
        }
		
		protected Converter multiplier;
		protected ConvertibleValue value;
		

	 }
	 
	/*
	 * This inner class we will use will be a slightly customized JPanel.
	 */
	 abstract class CPanel extends JPanel 
	 {
		//Names for each text pane:
		/*
			Normaly inner classes can't have static members
			because you can't initialize them bofore initializing
			the outer class which is not static. 
			But I think it works for string litteral because
			they are created at compile time. 
			I am a bit uncomfortable with how this works,
			an alternative would be to put the initalization of the
			strings in the constructor beause they are not needed before that.
		*/
		protected static final String centegrade = "Centegrade";
		protected static final String fahrenheit = "Fahrenheit";
		protected static final String litre = "Liter";
		protected static final String galon = "Galon";
		protected static final String gram = "Gram";
		protected static final String ounce = "Ounce";
		protected static final String kilogram = "Kilogram";
		protected static final String pound = "Pound";
		protected static final String millimeter = "Millimeter(s)";
		protected static final String centimeter = "Centimeter(s)";
		protected static final String inch = "Inch(es)";
		protected static final String meter = "Meter(s)";
		protected static final String foot = "ft."; //FIXME: Add doucment listener to use Foot and Feet depending on the number.
		protected static final String yard = "Yard(s)";
		protected static final String kilometer = "Kilometer(s)";
		protected static final String mile = "Mile(s)";
		
		protected CFrame frame;
		protected ConvertibleValue cValue;  //reference to shared instance.
	 
		protected void newTextPane(GridBagConstraints c, GridBagLayout gridbag, CDocumentTemperatureFilter filter, String name)
		{
			JPanel panel = new JPanel();
			panel.setBackground(white);
			panel.setLayout(new FlowLayout(FlowLayout.LEADING));
			panel.add(new CTalkativeTextPane(cValue, filter));
			panel.add(new JLabel(name, JLabel.LEFT));
			gridbag.setConstraints(panel, c);
			add(panel);
			panel = null;
		}
		
		protected GridBagConstraints createContraints(int x, int y)
		{
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = x;
			c.gridy = y;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.ipadx = 1;
			c.weightx = 1; 
			c.weighty = 1;
			c.anchor = GridBagConstraints.LINE_START;
			//c.insets = new Insets(0,1,0,0);
			return c;
		}
		
		
	
		public CPanel(CFrame F, ConvertibleValue CV)
		{
			super(new GridBagLayout());//call to super must be first statement in constructor
			frame = F;
			cValue = CV;
			
			
			setBackground(white);
			
			
			
		}//constructor
		public abstract void layoutConverters();
	 }//end of class CPanel
	 
	 class MetricDistancesPanel extends CPanel
	 {
		public MetricDistancesPanel(CFrame F, ConvertibleValue CV)
		{
			super(F, CV);
			layoutConverters();
		}
	 
		public void layoutConverters()
		{
			GridBagLayout gridbag = (GridBagLayout)getLayout();
			int x = 0;
			int y = 0;
			
			newTextPane(createContraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.MILIMETERS, frame), millimeter);
			
			newTextPane(createContraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.CENTIMETERS, frame), centimeter);
			
			newTextPane(createContraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.METERS, frame), meter);
			
			newTextPane(createContraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.KILOMETERS, frame), kilometer);
		}
	 
	 }//end of class DistancesPanel
	 
	 class ImperialDistancesPanel extends CPanel
	 {
		public ImperialDistancesPanel(CFrame F, ConvertibleValue CV)
		{
			super(F, CV);
			layoutConverters();
		}
			
		public void layoutConverters()
		{
			GridBagLayout gridbag = (GridBagLayout)getLayout();
			int x = 0;
			int y = 0;
			
			newTextPane(createContraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.INCHES, frame), inch);
			
			newTextPane(createContraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.FEET, frame), foot);
			
			newTextPane(createContraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.YARDS, frame), yard);
			
			newTextPane(createContraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.MILES, frame), mile);
		}
	 
	 }//End of ImperialDistancesPanel
	 
	 class TemperaturePanel extends CPanel
	 {
		protected TemperatureScales scale;
		public TemperaturePanel(CFrame F, ConvertibleValue CV, TemperatureScales s)
		{
			super(F, CV);
			scale = s;
			layoutConverters();
		}
		
		public void layoutConverters()
		{
			GridBagLayout gridbag = (GridBagLayout)getLayout();
			String scaleString = centegrade;
			if(scale == TemperatureScales.FAHRENHEIT)
			{
				scaleString = fahrenheit;
			}
			newTextPane(createContraints(0,0), gridbag, new CDocumentTemperatureFilter(cValue, new TemperatureFactors(scale), frame), scaleString);
			
		}
	 }
	 
	/**
     * We are gonig to put our cutomized panels in a custom split pane. 
     */
	class CSplit extends JSplitPane
	{
		public CSplit(int newOrientation, boolean newContinuousLayout, Component newLeftComponent, Component newRightComponent) 
		{
			super(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent);
		}
	}//end of CSPlit class.


	
	//Constructor
	public HappyHackingConverter()
	{
		//Make sure things compile.
		System.out.println("Hello World!");
		System.out.println(DistanceMultipliers.KILOMETERS.timesValue(1));
		
		CFrame f = new CFrame();
		f.setBackground(white);
		
		ConvertibleValue temperatures = new ConvertibleValue();
		ConvertibleValue distances = new ConvertibleValue();
		
		GridBagLayout leftSideLayout = new GridBagLayout();
		JPanel leftSide = new JPanel(leftSideLayout);
		leftSide.setBackground(white);
		
		GridBagConstraints metricTemperatureConstraints = new GridBagConstraints();
		metricTemperatureConstraints.gridx = 0;
		metricTemperatureConstraints.gridy = 0;
		metricTemperatureConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		JPanel metricTemperaturePanel = new TemperaturePanel(f, temperatures, TemperatureScales.CENTIGRADE);
		leftSideLayout.setConstraints(metricTemperaturePanel, metricTemperatureConstraints);
		leftSide.add(metricTemperaturePanel);
		
		GridBagConstraints metricDistancesPanelConstraints = new GridBagConstraints();
		metricDistancesPanelConstraints.gridx = 0;
		metricDistancesPanelConstraints.gridy = 1;
		metricDistancesPanelConstraints.anchor = GridBagConstraints.LINE_START;
		JPanel metricDistancesPanel = new MetricDistancesPanel(f, distances);
		leftSideLayout.setConstraints(metricDistancesPanel, metricDistancesPanelConstraints);
		leftSide.add(metricDistancesPanel);
		
		GridBagLayout rightSideLayout = new GridBagLayout();
		JPanel rightSide = new JPanel(rightSideLayout);
		rightSide.setBackground(white);
		
		GridBagConstraints fahrenheitConstraints = new GridBagConstraints();
		fahrenheitConstraints.gridx = 0;
		fahrenheitConstraints.gridy = 0;
		fahrenheitConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		JPanel fahrenheitTemperaturePanel = new TemperaturePanel(f, temperatures, TemperatureScales.FAHRENHEIT);
		rightSideLayout.setConstraints(fahrenheitTemperaturePanel, fahrenheitConstraints);
		rightSide.add(fahrenheitTemperaturePanel);
		
		
		GridBagConstraints imperialDistancesPanelConstraints = new GridBagConstraints();
		imperialDistancesPanelConstraints.gridx = 0;
		imperialDistancesPanelConstraints.gridy = 1;
		imperialDistancesPanelConstraints.anchor = GridBagConstraints.LINE_START;
		JPanel imperialDistancesPanel = new ImperialDistancesPanel(f, distances);
		rightSideLayout.setConstraints(imperialDistancesPanel, imperialDistancesPanelConstraints);
		rightSide.add(imperialDistancesPanel);
		
		
		
		CSplit split = new CSplit(JSplitPane.HORIZONTAL_SPLIT, true, leftSide, rightSide);
		Container contentPane = f.getContentPane();
		contentPane.add(split);
		split.setDividerLocation(0.5);
		f.validate();
		f.pack();
		f.setSize(300,300);
	}//end of HappyHackingConverter constructor

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() 
	{
        HappyHackingConverter conv = new HappyHackingConverter();
    }

    public static void main(String[] args) 
	{
		System.gc();
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }//end of main
	
}//end of HappyHackingConverter