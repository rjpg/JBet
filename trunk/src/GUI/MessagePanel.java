package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleConstants.ColorConstants;

public class MessagePanel extends JPanel {
	
	private JTextComponent msgTextArea = null;
	private JScrollPane jScrollPane = null;

	private static final int MAX_TEXT_MSG_LENGHT = 50000;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
	
	
	public MessagePanel() {
		setLayout(new BorderLayout());
		add(getTextPanelConsole(), BorderLayout.CENTER);
	}

	public JScrollPane getTextPanelConsole() {

		jScrollPane = new JScrollPane();
		msgTextArea = new JTextPane();
		msgTextArea.setEditable(false);
		// msgTextArea.setFont(new java.awt.Font("DialogInput",
		// java.awt.Font.PLAIN, 12));

		jScrollPane.setViewportView(msgTextArea);
		// jScrollPane.setBounds(new java.awt.Rectangle(8,107,196,124));
		this.add(jScrollPane, BorderLayout.CENTER);
		jScrollPane.setVisible(true);

		return jScrollPane;

	}

	public void writeMessageText(final String message, final Color type) {
		
		 Runnable  runnable = new Runnable() {
				
				@Override
				public void run() {
		
		//msgTextArea.setText("[" + getTimeStamp() + "]: "+msgTextArea.getText()+message+"\n");
		
		// getMsgTextArea().append(message);
		// getMsgTextArea().setCaretPosition(getMsgTextArea().getText().length());
		SimpleAttributeSet attrTS = new SimpleAttributeSet();
		attrTS.addAttribute(ColorConstants.Foreground, Color.DARK_GRAY);
		attrTS.addAttribute(StyleConstants.Bold, true);
		SimpleAttributeSet attr = new SimpleAttributeSet();
		attr.addAttribute(ColorConstants.Foreground, type);
		Document doc = msgTextArea.getDocument();
		// msgTextArea.setText(msgTextArea.getText()+message+"\n");
		try {
			doc.insertString(doc.getLength(), "[" + getTimeStamp() + "]: ",
					attrTS);
			doc.insertString(doc.getLength(), message + "\n", attr);
			int docLength = doc.getLength();
			if (docLength > MAX_TEXT_MSG_LENGHT)
				doc.remove(0, docLength - MAX_TEXT_MSG_LENGHT);
			// System.err.println("Doc. lenght " + doc.getLength());
			msgTextArea.setCaretPosition(doc.getLength());
		} catch (Exception e) {
		}
		
				}
			};

		SwingUtilities.invokeLater(runnable);	 
	
		
	}

	public String getTimeStamp() {
		return dateFormat.format(new Date(System.currentTimeMillis()));
	}
	
}
