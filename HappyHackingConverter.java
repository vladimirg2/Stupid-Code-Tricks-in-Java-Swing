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
import java.lang.Double;
import java.lang.StringBuffer;


/**
 * A very simple toy application with an intuitive UI.
 * We start with a simple class which will contain main,
 * create and launch the gui and serve as a container 
 * for the other classes specific to this project.
 * And lets use easily remembered variables names like:
 */
class HappyHackingConverter
{
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

	/**
	 * We will want to prevent input of invalid values.
	 * A validator will allow us to correct invalid input when the widget is about to lose focus.
	 * How ever what we want is to prevent the appearence of invalid input at all.
	 * This is why we need a customized document filter.
	 * And with this class we are  going to start using the leading C (as in Convert) convention in class naming. 
	 */
	 class CDocumentTemperatureFilter extends DocumentFilter
	 {		
		public CDocumentTemperatureFilter()
		{
			System.out.println("Constructed CDocumentTemperatureFilter");
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
		private double getDouble(boolean insert, FilterBypass fb, int offs,
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
					String minus = "-";
					String blank = "";
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
		
		public void insertString(FilterBypass fb, int offs,
                             String str, AttributeSet a)
        throws BadLocationException 
		{
			System.out.println("in DocumentSizeFilter's insertString method");
			if(isValidTemperature(true, fb, offs, str, 0))
			{
				super.insertString(fb, offs, str, a);
			}
		}//insertString
    
		public void replace(FilterBypass fb, int offs,
							int length, 
							String str, AttributeSet a)
			throws BadLocationException 
		{
			System.out.println("in DocumentSizeFilter's replace method");
			if(isValidTemperature(false, fb, offs, str, length))
			{
				super.replace(fb, offs, length, str, a);
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
				super.remove(fb, offs, length);
			}
		}//End of remove
	 }//End of temperature filter. 
	 
	/*
	 * This inner class we will use will be a slightly customized JPanel.
	 */
	 class CPanel extends JPanel
	 {
		public CPanel()
		{
			super(new GridBagLayout());//call to super must be first statement in constructor
			GridBagLayout gridbag = (GridBagLayout)getLayout();
			
			setBackground(white);
			
			JTextPane textPane = new JTextPane();
			StyledDocument styledDoc = textPane.getStyledDocument();
			if (styledDoc instanceof AbstractDocument) 
			{
				AbstractDocument doc = (AbstractDocument)styledDoc;
				doc.setDocumentFilter(new CDocumentTemperatureFilter());
			} 
			else 
			{
				System.err.println("Text pane's document isn't an AbstractDocument!");
				System.exit(-1);
			}
			
			GridBagConstraints c = new GridBagConstraints();
			gridbag.setConstraints(textPane, c);
			add(textPane);
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
			
			setSize(new Dimension(200, 200));
			
			setVisible(true);
		}//end of CFrame constructor
		
	}//end of CFrame class.
	
	//Constructor
	public HappyHackingConverter()
	{
		//Make sure things compile.
		System.out.println("Hello World!");
		
		CFrame f = new CFrame();
		f.setBackground(white);
		 
		JPanel a = new CPanel();
		JPanel b = new CPanel();
		
		
		CSplit split = new CSplit(JSplitPane.HORIZONTAL_SPLIT, true, a, b);
		
		f.getContentPane().add(split);
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