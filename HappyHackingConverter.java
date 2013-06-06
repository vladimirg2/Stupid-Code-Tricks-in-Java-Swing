import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.border.*;
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
import java.text.DecimalFormat;



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
		public double getDisplayValue(double value);
		public double getValueToBeSet(double value);
		public double getFactor();
	 }

	 
	 /*
	 An enum to handle conversion between distance units.
	 */
	 enum DistanceMultipliers implements Converter
	 {
		 MILIMETERS(1000),
		 CENTIMETERS(100),
		 METERS(1),
		 KILOMETERS(0.001),
		 INCHES(39.3701),
		 FEET(3.28084),
		 YARDS(1.09361),
		 MILES(0.00062137273);
		 
		 
		 
		 private final double factor;
		 private DistanceMultipliers(double f)
		 {
			factor = f;
		 }
		 public double getDisplayValue(double value)
		 {
		 return (value * factor);
		 }
		 public double getValueToBeSet(double value)
		 {
		 return (value / factor);
		 }
		 public double getFactor()
		 {
		 return factor;
		 }
	 }
	 
	 enum WeightMultipliers implements Converter
	 {
		MILLIGRAM(1000000),
		GRAM(1000),
		KILOGRAM(1),
		METRIC_TON(0.001), //It's almost as if the metric units could share the factors.
		LITER(1), //Water, option to add gasoline later.
		MILLILITER(1000),
		OUNZE(35.274),
		TORY_OUNCE(32.1507466),
		POUND(2.20462262185),
		UK_GALON(0.219969),
		US_GALON(0.264172),
		LONG_TON(0.000984207), //United Kingdom
		SHORT_TON(0.00110231); //United States
		private final double factor;
		private WeightMultipliers(double f)
		{
			factor = f;
		}
		/*
		Since all Java enums implicitly extend java.lang.Enum,
		and thus cannont extend anything else. 
		There is some code duplication with the distance
		enum here. Each enum could get an intace of a class which
		implements the Converter API and takes
		the factor in its constructor, but then
		all the calls to that instance would be duplicated. 
		*/
		public double getDisplayValue(double value)
		 {
		 return (value * factor);
		 }
		 public double getValueToBeSet(double value)
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
		public double getDisplayValue(double value)
		{
			switch(scale)
			{
				case CENTIGRADE: return value;
				case FAHRENHEIT: return ((value * (9.0/5.0)) + 32.0);
				default: return value;
			}
		}
		
		public double getValueToBeSet(double value)
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
			//FIXME: We should probably throw here.
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
					double newValue = multiplier.getValueToBeSet(value.doubleValue());
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
		df = new DecimalFormat("#.###");
		}
		protected CDocumentTemperatureFilter filter;
		protected DecimalFormat df;
		
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
				//System.out.printf("The fucking multiplier is: %f \n", multiplier.getFactor());
				double actualValue = (multiplier.getDisplayValue(value.getValue()));
				String actualValueAsString = df.format(actualValue);//Double.toString(actualValue);
				
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
				if(!actualValueAsString.equals(displayedValue) && displayedValueAsDouble != actualValue) //Allow user to enter trailing zeroes.
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
	 
	 
	class LabelPair<Singular, Plural> 
	{
		protected Singular s;
		protected Plural p;
		public LabelPair(Singular S, Plural P)
		{
			s = S;
			p = P;
		}
		public Singular getSingular() {return s;}
		public Plural getPlural() {return p;}
	}
	 
	class DisplayPanel extends JPanel implements DocumentListener 
	{
		protected LabelPair<String, String> labelPair;
		protected JLabel label;
		public DisplayPanel()
		{
			super(new GridBagLayout());
			setBackground(white);
		}
		public void addForDisplay(CTalkativeTextPane pane, LabelPair<String, String> lp)
		{
			GridBagLayout gridbag = (GridBagLayout)getLayout();
			labelPair = lp;
			label = new JLabel(labelPair.getSingular(), JLabel.LEFT);
			pane.getDocument().addDocumentListener(this);
			
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			//c.ipadx = 1;
			c.weightx = 0.1; 
			c.weighty = 0.1;
			//c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_START;
			c.insets = new Insets(0,20,0,00);
			gridbag.setConstraints(pane, c);
			add(pane);
			
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			//c.ipadx = 1;
			c.weightx = 0.1; 
			c.weighty = 0.1;
			c.anchor = GridBagConstraints.LINE_START;
			c.insets = new Insets(0,0,0,20);
			gridbag.setConstraints(label, c);
			add(label);
			
			JPanel filler = new JPanel();
			filler.setBackground(white);
			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			//c.ipadx = 1;
			c.weightx = 0.8; 
			c.weighty = 0.8;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.LINE_END;
			gridbag.setConstraints(filler, c);
			add(filler);
			
			c = null;
		}
		
		public void changedUpdate(DocumentEvent e) { updateTheLabel(e); }
		public void insertUpdate(DocumentEvent e) { updateTheLabel(e); }
		public void removeUpdate(DocumentEvent e) { updateTheLabel(e); }
		
		void updateTheLabel(DocumentEvent e)
		{
			Document doc = (Document)e.getDocument();
			String text = null;
			try 
			{
				text = doc.getText(0, doc.getLength());
			}
			catch(BadLocationException ex)
			{
				text = null;
			}
			if(text != null)
			{
				try
				{
					double number = Double.parseDouble(text);
					if(number > 1)
					{
						label.setText(labelPair.getPlural());
					}
					else
					{
						label.setText(labelPair.getSingular());
					}
				}
				catch (NumberFormatException ex)
				{
				//Do nothing
			}
			}
		}
	}
	 
	/*
	 * This inner class we will use will be a slightly customized JPanel.
	 */
	 abstract class CPanel extends JPanel 
	 {
		//Names for each text pane:
		/*
			Normaly inner classes can't have static members
			because you can't initialize them before initializing
			the outer class which is not static. 
			But I think it works for string litteral because
			they are created at compile time. 
			I am a bit uncomfortable with how this works,
			an alternative would be to put the initalization of the
			strings in the constructor beause they are not needed before that.
		*/
		
		
		
		
		
		
		/** Returns an ImageIcon, or null if the path was invalid. */
		protected ImageIcon createImageIcon(String path, String description) 
		{
			java.net.URL imgURL = getClass().getResource(path);
			if (imgURL != null) 
			{
				return new ImageIcon(imgURL, description);
			} 
			else 
			{
				System.err.println("Couldn't find file: " + path);
				return null;
			}
		}
				
		protected CFrame frame;
		protected ConvertibleValue cValue;  //reference to shared instance.
		
		public Dimension getPreferredSize()
		{
			return preferedSize;
		}
	 
		protected void newTextPane(GridBagConstraints c, GridBagLayout gridbag, CDocumentTemperatureFilter filter, LabelPair names)
		{
			DisplayPanel panel = new DisplayPanel();
			panel.addForDisplay(new CTalkativeTextPane(cValue, filter), names);
			gridbag.setConstraints(panel, c);
			add(panel);
			panel = null;
		}
		
		protected GridBagConstraints createConstraints(int x, int y)
		{
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = x;
			c.gridy = y;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.ipadx = 1;
			c.weightx = 0.5; 
			c.weighty = 0.5;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.CENTER;
			//c.insets = new Insets(0,1,0,0);
			return c;
		}
		
		protected Dimension preferedSize;
	
		public CPanel(CFrame F, ConvertibleValue CV)
		{
			super(new GridBagLayout());//call to super must be first statement in constructor
			setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.blue));
			frame = F;
			cValue = CV;
			preferedSize = new Dimension(100, 40);
			
			setBackground(white);
			
			
			
		}//constructor
		public abstract void layoutConverters();
	 }//end of class CPanel
	 
	 class MetricDistancesPanel extends CPanel
	 {
		protected static final String millimeter = "s)";
		protected static final String centimeter = "(s)";
		protected static final String meter = "(s)";
		protected static final String kilometer = "(s)";
		
		protected final LabelPair[] labels = { new LabelPair("Millimeter","Millimeters"), new LabelPair("Centimeter", "Centimeters"), new LabelPair("Meter","Meters"), new LabelPair("Kilometer","Kilometers") };
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
			
			GridBagConstraints c = createConstraints(x,y++);
			ImageIcon icon = createImageIcon("images/90km.png", "EU");
			JLabel label = new JLabel(icon);
			gridbag.setConstraints(label, c);
			add(label);
			
			int just_metric = 0;
			for(DistanceMultipliers dm : DistanceMultipliers.values())
			{
				if(just_metric > 3)
				{
					return;
				}
				newTextPane(createConstraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, dm, frame), labels[just_metric]);
				just_metric++;
			}

		}
	 
	 }//end of class DistancesPanel
	 
	 class ImperialDistancesPanel extends CPanel
	 {
		protected final LabelPair[] labels = { new LabelPair("Inch","Inches"), new LabelPair("Foot","Feet"), new LabelPair("Yard","Yards"), new LabelPair("Mile","Miles") };
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
			
			GridBagConstraints c = createConstraints(x,y++);
			ImageIcon icon = createImageIcon("images/55.png", "EU");
			JLabel label = new JLabel(icon);
			gridbag.setConstraints(label, c);
			add(label);
			
			int ignore_metric = 0;
			for(DistanceMultipliers dm : DistanceMultipliers.values())
			{
				if(ignore_metric > 3)
				{
					newTextPane(createConstraints(x,y++), gridbag, new CDocumentPositiveNumberFilter(cValue, dm, frame), labels[ignore_metric-4]);
				}
				ignore_metric++;
			}

		}
	 
	 }//End of ImperialDistancesPanel
	 
	 class TemperaturePanel extends CPanel
	 {
		protected static final String centegrade = "Centegrade";
		protected static final String fahrenheit = "Fahrenheit";
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
			
			ImageIcon sunIcon = createImageIcon("images/Sun.png", "Visit the Mediterranean.");
			ImageIcon cloudIcon = createImageIcon("images/Cloud.png", "UK weather.");
			JLabel label;
			
			LabelPair scalePair = null; 
			if(scale == TemperatureScales.FAHRENHEIT)
			{
				scalePair = new LabelPair(fahrenheit, fahrenheit);
				label = new JLabel(cloudIcon);
			}
			else
			{
				scalePair = new LabelPair(centegrade, centegrade);
				label = new JLabel(sunIcon);
			}
			GridBagConstraints c = createConstraints(0,0);
			gridbag.setConstraints(label, c);
			add(label);
			newTextPane(createConstraints(0,1), gridbag, new CDocumentTemperatureFilter(cValue, new TemperatureFactors(scale), frame), scalePair);
			
		}

	 }
	 
	 class MetricWeightsPanel extends CPanel
	 {
		protected final LabelPair[] labels = { new LabelPair("Milligram","Milligrams"), new LabelPair("Gram","Grams"), new LabelPair("Kilogram","Kilograms"), new LabelPair("Tonne","Tonnes"), new LabelPair("Liter (Water)","Liters (Water)"), new LabelPair("Milliliter","Milliliters") };
		public MetricWeightsPanel(CFrame F, ConvertibleValue CV)
		{
			super(F, CV);
			layoutConverters();
		}
		public void layoutConverters()
		{
			GridBagLayout gridbag = (GridBagLayout)getLayout();
			int x = 0;
			int y = 0;
			
			int just_metric = 0;
			for(WeightMultipliers wm : WeightMultipliers.values())
			{
				if(just_metric > 5) 
				{
					return;
				}
				
				newTextPane(createConstraints(x, y++), gridbag, new CDocumentPositiveNumberFilter(cValue, wm, frame), labels[just_metric]);
				just_metric++;
			}
			
		}
	 }
	 
	 
	class ImperialWeightsPanel extends CPanel
	{
		//The labels must match the enum delcaration order.
		protected final LabelPair[] labels = { new LabelPair("Ounce","Ounces"), new LabelPair("Troy ounce", "Troy ounces"), new LabelPair("Pound", "Pounds"), new LabelPair("Imperial gallon (UK)", "Imperial gallons (UK)"), new LabelPair("US gallon", "US gallons"), new LabelPair("Long ton (UK)", "Long tons (UK)"), new LabelPair("Short ton (US)","Short tons (US)")};
		public ImperialWeightsPanel(CFrame F, ConvertibleValue CV)
		{
			super(F, CV);
			layoutConverters();
		}
		public void layoutConverters()
		{
			GridBagLayout gridbag = (GridBagLayout)getLayout();
			int x = 0;
			int y = 0;
			
			int ignore_metric = 0;
			for(WeightMultipliers wm : WeightMultipliers.values())
			{
				/*
				values returns all enum values in order of declartion but we 
				want to ingore the first 6 (metric) values.
				*/
				if(ignore_metric > 5)
				{
					newTextPane(createConstraints(x, y++), gridbag, new CDocumentPositiveNumberFilter(cValue, wm, frame), labels[ignore_metric-6]);
				}
				ignore_metric++;
				
			}
			//Is there a cleaner way to iterate over a chunk of what values() returns?
	
			
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
		System.out.println(DistanceMultipliers.KILOMETERS.getDisplayValue(1));
		
		CFrame frame = new CFrame();
		frame.setBackground(white);
		
		ConvertibleValue temperatures = new ConvertibleValue();
		ConvertibleValue distances = new ConvertibleValue();
		ConvertibleValue weights = new ConvertibleValue();
		
		GridBagLayout leftSideLayout = new GridBagLayout();
		JPanel leftSide = new JPanel(leftSideLayout);
		leftSide.setBackground(white);
		
		GridBagLayout rightSideLayout = new GridBagLayout();
		JPanel rightSide = new JPanel(rightSideLayout);
		rightSide.setBackground(white);
		
		GridBagConstraints metricTemperatureConstraints = new GridBagConstraints();
		metricTemperatureConstraints.gridx = 0;
		metricTemperatureConstraints.gridy = 0;
		metricTemperatureConstraints.weighty = 0.1;
		metricTemperatureConstraints.weightx = 1;
		metricTemperatureConstraints.fill = GridBagConstraints.BOTH;
		metricTemperatureConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		JPanel metricTemperaturePanel = new TemperaturePanel(frame, temperatures, TemperatureScales.CENTIGRADE);
		leftSideLayout.setConstraints(metricTemperaturePanel, metricTemperatureConstraints);
		leftSide.add(metricTemperaturePanel);
		
		GridBagConstraints metricDistancesPanelConstraints = new GridBagConstraints();
		metricDistancesPanelConstraints.gridx = 0;
		metricDistancesPanelConstraints.gridy = 1;
		metricDistancesPanelConstraints.weighty = 0.3;
		metricDistancesPanelConstraints.weightx = 1;
		metricDistancesPanelConstraints.fill = GridBagConstraints.BOTH;
		metricDistancesPanelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		JPanel metricDistancesPanel = new MetricDistancesPanel(frame, distances);
		leftSideLayout.setConstraints(metricDistancesPanel, metricDistancesPanelConstraints);
		leftSide.add(metricDistancesPanel);
		
		
		
		GridBagConstraints fahrenheitConstraints = new GridBagConstraints();
		fahrenheitConstraints.gridx = 0;
		fahrenheitConstraints.gridy = 0;
		fahrenheitConstraints.weighty = 0.1;
		fahrenheitConstraints.weightx = 1;
		fahrenheitConstraints.fill = GridBagConstraints.BOTH;
		fahrenheitConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		JPanel fahrenheitTemperaturePanel = new TemperaturePanel(frame, temperatures, TemperatureScales.FAHRENHEIT);
		rightSideLayout.setConstraints(fahrenheitTemperaturePanel, fahrenheitConstraints);
		rightSide.add(fahrenheitTemperaturePanel);
		
		
		GridBagConstraints imperialDistancesPanelConstraints = new GridBagConstraints();
		imperialDistancesPanelConstraints.gridx = 0;
		imperialDistancesPanelConstraints.gridy = 1;
		imperialDistancesPanelConstraints.weighty = 0.3;
		imperialDistancesPanelConstraints.weightx = 1;
		imperialDistancesPanelConstraints.fill = GridBagConstraints.BOTH;
		imperialDistancesPanelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		JPanel imperialDistancesPanel = new ImperialDistancesPanel(frame, distances);
		rightSideLayout.setConstraints(imperialDistancesPanel, imperialDistancesPanelConstraints);
		rightSide.add(imperialDistancesPanel);
		
		GridBagConstraints metricWeightsConstraints = new GridBagConstraints();
		metricWeightsConstraints.gridx = 0;
		metricWeightsConstraints.gridy = 2;
		metricWeightsConstraints.weighty = 0.6;
		metricWeightsConstraints.weightx = 1;
		metricWeightsConstraints.fill = GridBagConstraints.BOTH;
		metricWeightsConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		MetricWeightsPanel metricWeightsPanel = new MetricWeightsPanel(frame, weights);
		leftSideLayout.setConstraints(metricWeightsPanel, metricWeightsConstraints);
		leftSide.add(metricWeightsPanel);
		
		GridBagConstraints imperialWeightsConstraints = new GridBagConstraints();
		imperialWeightsConstraints.gridx = 0;
		imperialWeightsConstraints.gridy = 2;
		imperialWeightsConstraints.weighty = 0.6;
		imperialWeightsConstraints.weightx = 1;
		imperialWeightsConstraints.fill = GridBagConstraints.BOTH;
		imperialWeightsConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		ImperialWeightsPanel imperialWeightsPanel = new ImperialWeightsPanel(frame, weights);
		rightSideLayout.setConstraints(imperialWeightsPanel, imperialWeightsConstraints);
		rightSide.add(imperialWeightsPanel);
		
		
		CSplit split = new CSplit(JSplitPane.HORIZONTAL_SPLIT, true, leftSide, rightSide);
		Container contentPane = frame.getContentPane();
		contentPane.add(split);
		frame.validate();
		frame.pack();
		frame.setSize(600,800);
		split.setResizeWeight(0.5);
		split.setDividerLocation(0.5);
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