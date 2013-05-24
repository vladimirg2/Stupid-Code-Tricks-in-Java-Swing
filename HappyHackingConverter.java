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
	 * I would describe the default widget colors used as soul numbing gray.
	 * So instead we are going to use this variable as the background color throughout our toy app.
	 */
	private final static Color white = new Color(255,255,255);
	
	/**
	 * All classes in this toy app will be contained in this class.
	 * As they are specific to this projet and should have access to shared variables like the background color.
	 * And if we do decide to share them with other projects we can always refactor them out.
	 */

	 
	  /*
	 Distance, weight and temperature.
	 */
	 class ConvertibleValue
	 {
	 public ConvertibleValue()
	 {
	 value = 1;
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
	 
	 enum DistanceMultipliers
	 {
		 METERS(1),
		 MILIMETERS(1000),
		 CENTIMETERS(100),
		 KILOMETERS(0.001),
		 MILES(0.00062137273),
		 YARDS(1.09361),
		 FEET(3.28084),
		 INCHES(39.3701);
		 private final double meters;
		 private DistanceMultipliers(double multiplier)
		 {
		 meters = multiplier;
		 }
		 public double timesValue(double value)
		 {
		 return (value * meters);
		 }
		 public double divValue(double value)
		 {
		 return (value / meters);
		 }
	 }
	 
	 
	/**
	 * We will want to prevent input of invalid values.
	 * A validator will allow us to correct invalid input when the widget is about to lose focus.
	 * How ever what we want is to prevent the appearence of invalid input at all.
	 * This is why we need a customized document filter.
	 * And with this class we are  going to start using the leading C (as in Convert) convention in class naming. 
	 */
	 class CDocumentTemperatureFilter extends DocumentFilter
	 {		
		public CDocumentTemperatureFilter(ConvertibleValue cv, DistanceMultipliers m)
		{
			super();
			updateValue = true;
			cValue = cv;
			multiplier = m;
			System.out.println("Constructed CDocumentTemperatureFilter");
		}
		protected ConvertibleValue cValue;
		protected DistanceMultipliers multiplier;
		
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
					try
					{
						return Double.parseDouble(sb.toString()); 
					}//try
					catch(NumberFormatException ex)
					{
						Toolkit.getDefaultToolkit().beep();
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
				
		public void setUpdateValue(boolean b)
		{
			updateValue = b;
		}
		protected boolean updateValue;
		
		protected void doValueUpdate(FilterBypass fb)
		throws BadLocationException
		{
			if(updateValue == true)
				{
					try
					{
					Document doc = fb.getDocument();
					String text = doc.getText(0, doc.getLength());
					Double value = new Double(text);
					double newValue = multiplier.divValue(value.doubleValue());
					cValue.setValue(newValue);
					//System.out.printf("doValueUpdate new string value = %s \n", newStringValue);
					}
					catch (NumberFormatException e)
					{
					//do nothing, since we allow '-'
					}
				}
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
		public CDocumentPositiveNumberFilter(ConvertibleValue cv, DistanceMultipliers m)
		{
			super(cv, m);
			System.out.println("Constructed CDocumentPositiveNumberFilter");
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
			String str ="";
			if(isValidPostiveNumber(false, fb, offs, str, length))
			{
				fb.remove(offs, length);
				doValueUpdate(fb);
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
	 class CWeight extends ConvertibleValue
	 {
		public void setKilograms(Double kilos)
		{
		value = kilos;
		}
		public Double getKilos()
		{
		return value;
		}
		public Double getTonnes()
		{
		return value / 1000;
		}
		public Double getGramms()
		{
		return value * 1000;
		}
		public Double getMiligrams()
		{
		return value * 1000000;
		}
		public Double getWaterLiters()
		{
		return value;
		}
		public Double getWaterGallons()
		{
		return value * 0.264172;
		}
		public Double getImperialTons()
		{
		return value /  1016.04691;
		}
		public Double getPounds()
		{
		return value * 2.20462262;
		}
		public Double getOunce()
		{
		return value * 35.2739619;
		}
		public Double getTroyOunce()
		{
		return value * 32.1507466;
		}
	 }
	 
	 class CTemperature extends ConvertibleValue
	 {
		 public void setCelcius(Double celcius)
		 {
		 value = celcius;
		 }
		 public Double getCelsius()
		 {
		 return value;
		 }
		 public Double getFahrenheit()
		 {
		 return (((value * 9) /  5) + 32);
		 }
		 public void setFahrenheit(Double f)
		 {
		 value = (((f - 32) * 5) / 9);
		 }
	 }
	*/ 
	 /*
	 We will customize JTextPane so that it can notify
	 instances registered with it. Thus any weight change,
	 for example kilograms, will also update all other weights,
	 grams, tonnes, etc.
	 */
	 class CTalkativeTextPane extends JTextPane 
	 {
		public CTalkativeTextPane(ConvertibleValue V, DistanceMultipliers M)
		{
		super();
		value = V;
		multiplier = M;
		}
		protected CDocumentTemperatureFilter filter;
		
		public void setDocumentFilter(CDocumentTemperatureFilter f)
		{
		filter = f;
		StyledDocument styledDoc = getStyledDocument();
			if (styledDoc instanceof AbstractDocument) 
			{
				AbstractDocument doc = (AbstractDocument)styledDoc;
				doc.setDocumentFilter(filter);
			} 
			else 
			{
				System.err.println("Text pane's document isn't an AbstractDocument!");
				System.exit(-1);
			}
		}

		@Override
        protected void paintComponent(Graphics g) 
		{
			super.paintComponent(g);
			
			try
			{
				StyledDocument doc = getStyledDocument();
				double actualValue = (multiplier.timesValue(value.getValue()));
				//System.out.printf("Paint component actualValue = %s \n", actualValue);
				
				String displayedValue;
				try
				{
				displayedValue = doc.getText(0, doc.getLength());
				//System.out.printf("Paint cdisplayedValuee = %s \n", actualValue);
				}
				catch(BadLocationException e)
				{
				displayedValue = "";
				//Beep?
				}
				double displayedValueAsDouble = Double.parseDouble(displayedValue);
				if(actualValue != displayedValueAsDouble)
				{
				System.out.println("Repaint required");
					try 
					{ 
						filter.setUpdateValue(false);
						doc.remove(0, doc.getLength());
						doc.insertString(0, Double.toString(actualValue), null); 
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
			//Do nothing since we allow things like '-'.
			}
						
        }

        @Override
        public void repaint(long tm, int x, int y, int width, int height) {
            // This forces repaints to repaint the entire TextPane.
            super.repaint(tm, 0, 0, getWidth(), getHeight());
        }
		
		protected DistanceMultipliers multiplier;
		protected ConvertibleValue value;
		
		
	 	public Dimension getPreferredSize()
		{
			return new Dimension(50,50);
		}
		
		/*public void insertUpdate(DocumentEvent e) 
		{
			try
			{
				Document doc = (Document)e.getDocument();
				Double newValue = new Double(doc.getText(0, doc.getLength()));
				value.setValue(multiplier.divValue(newValue.doubleValue()));
				 System.out.printf("Vaaaalue set to &f \n", value);
			}
			catch(BadLocationException ex)
			{
				//Since we are asking for something from 0 to doc.Length(), this shouldn't happen.
				Toolkit.getDefaultToolkit().beep();
			}
        }
        public void removeUpdate(DocumentEvent e) 
		{
            try
			{
				Document doc = (Document)e.getDocument();
				Double newValue = new Double(doc.getText(0, doc.getLength()));
				value.setValue(multiplier.divValue(newValue.doubleValue()));
				 System.out.printf("Vlue set to &f \n", value);
			}
			catch(BadLocationException ex)
			{
				//Since we are asking for something from 0 to doc.Length(), this shouldn't happen.
				Toolkit.getDefaultToolkit().beep();
			}
        }
        public void changedUpdate(DocumentEvent e) 
		{
            //Plain text components don't fire these events.
			System.out.println("Changed update event fired.");
			//getNewValueUpdatedOthers(e);
		}*/
	 }
	 
	/*
	 * This inner class we will use will be a slightly customized JPanel.
	 */
	 class CPanel extends JPanel 
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
		private static final String centegrade = "Centegrade";
		private static final String fahrenheit = "Fahrenheit";
		private static final String litre = "Liter";
		private static final String galon = "Galon";
		private static final String gram = "Gram";
		private static final String ounce = "Ounce";
		private static final String kilogram = "Kilogram";
		private static final String pound = "Pound";
		private static final String centimeter = "Centimeter";
		private static final String inch = "Inch";
		private static final String meter = "Meter";
		private static final String foot = "Foot";
		private static final String kilometer = "Kilometer";
		private static final String mile = "Mile";
		
		private void getNewValueUpdatedOthers(DocumentEvent e)
		{
			String prop = "name";
			Document doc = (Document)e.getDocument();
			String name=""; 
			name += doc.getProperty(prop);
			String text = "";
			try
			{
				text = doc.getText(0, doc.getLength());
				
				switch(name)
				{
					case centegrade:
					System.out.println("Update fahrenheit");
					break;
					case  fahrenheit:
					break;
					case  litre:
					System.out.println("Update galon");
					break;
					case  galon:
					break;
					case  gram:
					break;
					case  ounce:
					break;
					case  kilogram:
					break;
					case  pound:
					break;
					case  centimeter:
					break;
					case  inch:
					break;
					case  meter:
					break;
					case  foot:
					break;
					case  kilometer:
					break;
					case  mile:
					break;
				}//End of switch(text) 
				
				if(minus.contentEquals(text) || blank.contentEquals(text))
				{
					//Set the corresponding items to blank.
				}
				else
				{
					try
					{
						double value = Double.parseDouble(text); 
						System.out.println(value);
					}//try
					catch(NumberFormatException ex)
					{
						//This shouldn't happen thanks to the document filters.
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
			catch(BadLocationException ex)
			{
				//Since we are asking for something from 0 to doc.Length(), this shouldn't happen.
				Toolkit.getDefaultToolkit().beep();
			}
		}//End of get new value then update corresponding values
		
		public Dimension getPreferredSize()
		{
			return new Dimension(200,200);
		}
		
		protected ConvertibleValue cValue;
	 
		private void newTextPane(GridBagConstraints c, GridBagLayout gridbag, CDocumentTemperatureFilter filter, String name)
		{
			Dimension dim = new Dimension(100,25);
			CTalkativeTextPane textPane = new CTalkativeTextPane(cValue, DistanceMultipliers.METERS);
			textPane.getDocument().putProperty("name", name);
			//textPane.getDocument().addDocumentListener(this);
			textPane.setPreferredSize(dim);
			BevelBorder border = new BevelBorder(BevelBorder.RAISED);
			textPane.setBorder(border);
			textPane.setDocumentFilter(filter);
			gridbag.setConstraints(textPane, c);
			add(textPane);
		}
		public CPanel()
		{
			super(new GridBagLayout());//call to super must be first statement in constructor
			cValue = new ConvertibleValue();
			GridBagLayout gridbag = (GridBagLayout)getLayout();
			
			setBackground(white);
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.gridheight = 2;
			c.ipadx = 1;
			c.weightx = 1; 
			c.weighty = 1;
			CDocumentTemperatureFilter tempFilter = new CDocumentTemperatureFilter(cValue, DistanceMultipliers.METERS);
			newTextPane(c, gridbag, tempFilter, centegrade);
			
			/*c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 2;
			c.gridheight = 2;
			c.ipadx = 1;
			c.weightx = 2; 
			c.weighty = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			CDocumentPositiveNumberFilter posFilter = new CDocumentPositiveNumberFilter(cValue, DistanceMultipliers.METERS);
			newTextPane(c, gridbag, posFilter, litre);*/
		}//constructor
	 }//end of class CPanel
	 
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

	/**
     * And lastly the frame class to contain everything.
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
		
	}//end of CFrame class.
	
	//Constructor
	public HappyHackingConverter()
	{
		//Make sure things compile.
		System.out.println("Hello World!");
		System.out.println(DistanceMultipliers.KILOMETERS.timesValue(1));
		
		CFrame f = new CFrame();
		f.setBackground(white);
		 
		JPanel a = new CPanel();
		JPanel b = new CPanel();
		
		
		CSplit split = new CSplit(JSplitPane.HORIZONTAL_SPLIT, true, a, b);
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