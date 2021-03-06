/**
 * ***********************************
 * Filename: EmailClient.java 
 * Names: Haoxuan WANG,Yuan GAO
 * Student-IDs: 201219597, 201218960
 * Date: 21/Oct. 2016
 * ***********************************
 **/
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFileChooser;
import java.util.ArrayList;

public class EmailClient extends Frame {

    /* The stuff for the GUI. */
    private Button btSend = new Button("Send");
    private Button btClear = new Button("Clear");
    private Button btQuit = new Button("Quit");
    private Label serverLabel = new Label("Local mailserver:");
    private TextField serverField = new TextField("mail.csc.liv.ac.uk", 40);
    private Label fromLabel = new Label("From:");
    private TextField fromField = new TextField("", 40);
    private Label toLabel = new Label("To:");
    private TextField toField = new TextField("", 40);
    private Label ccLabel = new Label("Cc:");  /* [Add] CC Fields */
    private TextField ccField = new TextField("", 40);
    private Label subjectLabel = new Label("Subject:");
    private TextField subjectField = new TextField("", 40);
    private Label messageLabel = new Label("Message:");
    private TextArea messageText = new TextArea(30, 80);
    private Label urlLabel = new Label("HTTP://");
    private TextField urlField = new TextField("cgi.csc.liv.ac.uk/~gairing/test.txt", 40);
    private Button btGet = new Button("Get");
    /* [Add] Attachment buttons */
    private Label attachmentLabel = new Label("Attachment:");
    private TextField attachmentField = new TextField("", 100);
    private Button btAddAttach = new Button("Add an attachment");
    private Button btClearAttach = new Button("Clear all attachments");
    /* [Add] File Chooser to provide access to some files */
    private JFileChooser chooser = new JFileChooser();
    /* [Add] Temporary File Storage in a List */
    private final ArrayList<File> ATTACHES = new ArrayList<>();
    /* [Add] After encoding, the File object in ATTACHES list will be in SUBBODIES as SubEmailMessage objects*/
    private final ArrayList<SubEmailMessage> SUBBODIES = new ArrayList<>();
    /* [Add] SubEmailMessage object variable for user entered characters or fetched characters from HTTP connection*/
    private SubEmailMessage mainText = null;
    /* [Add] Fields for mainText*/
    private static String contentType = MessageType.TXT.toString();  
    private static String ContentEncoding = EncodingType.ASCII_7.toString();

    public static void setContentType(String contentType) {
        EmailClient.contentType = contentType;
    }

    public static void setContentEncoding(String ContentEncoding) {
        EmailClient.ContentEncoding = ContentEncoding;
    }

    /**
     * Create a new EmailClient window with fields for entering all the relevant
     * information (From, To, Subject, and message).
     */
    public EmailClient() {
        super("Java Emailclient");

        /* Create panels for holding the fields. To make it look nice,
	   create an extra panel for holding all the child panels. */
        Panel serverPanel = new Panel(new BorderLayout());
        Panel fromPanel = new Panel(new BorderLayout());
        Panel toPanel = new Panel(new BorderLayout());
        Panel ccPanel = new Panel(new BorderLayout());
        Panel subjectPanel = new Panel(new BorderLayout());
        Panel messagePanel = new Panel(new BorderLayout());
        Panel attechmentPanel = new Panel(new FlowLayout());

        serverPanel.add(serverLabel, BorderLayout.WEST);
        serverPanel.add(serverField, BorderLayout.CENTER);
        fromPanel.add(fromLabel, BorderLayout.WEST);
        fromPanel.add(fromField, BorderLayout.CENTER);
        toPanel.add(toLabel, BorderLayout.WEST);
        toPanel.add(toField, BorderLayout.CENTER);
        ccPanel.add(ccLabel, BorderLayout.WEST);
        ccPanel.add(ccField, BorderLayout.CENTER);
        subjectPanel.add(subjectLabel, BorderLayout.WEST);
        subjectPanel.add(subjectField, BorderLayout.CENTER);
        messagePanel.add(messageLabel, BorderLayout.NORTH);
        messagePanel.add(messageText, BorderLayout.CENTER);

        Panel fieldPanel = new Panel(new GridLayout(0, 1));
        fieldPanel.add(serverPanel);
        fieldPanel.add(fromPanel);
        fieldPanel.add(toPanel);
        fieldPanel.add(ccPanel);
        fieldPanel.add(subjectPanel);

        /* Create a panel for the URL field and add listener to the GET 
	   button. */
        Panel urlPanel = new Panel(new BorderLayout());
        urlPanel.add(urlLabel, BorderLayout.WEST);
        urlPanel.add(urlField, BorderLayout.CENTER);
        urlPanel.add(btGet, BorderLayout.EAST);
        fieldPanel.add(urlPanel);
        btGet.addActionListener(new GetListener());
        /* Create a panel for the buttons and add listeners to the attachment
	   buttons. */
        attachmentField.setEditable(false);
        attechmentPanel.add(attachmentLabel, FlowLayout.LEFT);
        attechmentPanel.add(attachmentField, FlowLayout.CENTER);
        attechmentPanel.add(btClearAttach, FlowLayout.RIGHT);
        attechmentPanel.add(btAddAttach, FlowLayout.RIGHT);
        fieldPanel.add(attechmentPanel);
        btClearAttach.addActionListener(new ClearAttachListener());
        btAddAttach.addActionListener(new AddListener());

        /* Create a panel for the buttons and add listeners to the
	   buttons. */
        Panel buttonPanel = new Panel(new GridLayout(1, 0));
        btSend.addActionListener(new SendListener());
        btClear.addActionListener(new ClearListener());
        btQuit.addActionListener(new QuitListener());
        buttonPanel.add(btSend);
        buttonPanel.add(btClear);
        buttonPanel.add(btQuit);

        /* Add, pack, and show. */
        add(fieldPanel, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);

        /* [Add]Verify sender address*/
        String host = "";
        try {
            host = System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ex) {
        }

        fromField.setText(host);

    }

    static public void main(String argv[]) {
        new EmailClient();
    }

    /* Handler for the Send-button. */
    class SendListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            System.out.println("Sending mail");

            /* Check that we have the local mailserver */
            if ((serverField.getText()).equals("")) {
                System.out.println("Need name of local mailserver!");
                return;
            }

            /* Check that we have the sender and recipient. */
            if ((fromField.getText()).equals("")) {
                System.out.println("Need sender!");
                return;
            }
            if ((toField.getText()).equals("")) {
                System.out.println("Need recipient!");
                return;
            }
            /*[Add] Clear SUBBODIES list to avoid duplicated attachment problem when clicking the send button more than once*/
            SUBBODIES.clear();
            
            /*[Add] Now transfer file to SubEmailMessage objects (Extract infomation and encoding), then place them into the list*/
            if (!attachmentField.getText().equals("")) {
                for (File file : ATTACHES) {
                    SUBBODIES.add(new SubEmailMessage(file));
                }
            }
            /* Create the message */
            /*[Add] Create the SubEmailMessage object for what user inputted or fetch from the Internet*/
            mainText = new SubEmailMessage(messageText.getText(), contentType, ContentEncoding);
            EmailMessage mailMessage;
            try {
                mailMessage = new EmailMessage(fromField.getText(), toField.getText(), ccField.getText(), subjectField.getText(), mainText, SUBBODIES, serverField.getText());
            } catch (UnknownHostException e) {
                /* If there is an error, do not go further */
                return;
            }

            /* Check that the message is valid, i.e., sender and
	       recipient addresses look ok. */
            if (!mailMessage.isValid()) {
                return;
            }
            try {
                System.out.println(mailMessage.getHeaders());
                SMTPConnect connection = new SMTPConnect(mailMessage);
                connection.send(mailMessage);
                connection.close();
            } catch (IOException error) {
                System.out.println("Sending failed: " + error);
                return;
            }
            System.out.println("Email sent succesfully!");
        }
    }

    /* Get URL if specified. */
    class GetListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            String receivedText;

            /* Check if URL field is empty. */
            if ((urlField.getText()).equals("")) {
                System.out.println("Need URL!");
                return;
            }
            /* Pass string from URL field to HTTPGet (trimmed);
			   returned string is either requested object 
			   or some error message. */
            /**
             * ******************************************
             * Uncomment this for part 2
             * ******************************************
             */
            HttpInteract request
                    = new HttpInteract(urlField, urlField.getText().trim());

            // Send http request. Returned String holds object 
            try {
                receivedText = request.send();
            } catch (IOException error) {
                messageText.setText("Downloading File failed.\r\nIOException: "
                        + error);
                return;
            } // Change message text
            messageText.setText(receivedText);
        }
    }

    /* Clear the fields on the GUI. */
    class ClearListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Clearing fields");
            fromField.setText("");
            toField.setText("");
            ccField.setText("");
            subjectField.setText("");
            messageText.setText("");
            attachmentField.setText("");
            contentType = MessageType.TXT.toString(); //Set to default value
            ContentEncoding = EncodingType.ASCII_7.toString(); //Set to default value
            mainText = null;
            ATTACHES.clear();
            SUBBODIES.clear();
        }
    }

    /* Quit. */
    class QuitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    /* Add an attachment. */
    class AddListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            chooser.setBounds(400, 200, 800, 800);
            chooser.showOpenDialog(chooser);
            chooser.setVisible(true);
            ATTACHES.add(chooser.getSelectedFile());
            String temp = "";
            for (File a : ATTACHES) {
                temp += (a.getName() + "\t\t");
            }
            attachmentField.setText(temp);
        }
    }
    /* Clear all the attachment. */
    class ClearAttachListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            SUBBODIES.clear();
            ATTACHES.clear();
            attachmentField.setText("");
        }

    }
}
    /* enum for regulate the possible encoding type */
enum EncodingType {
    BASE64("base64"), QP("quoted-printable"), ASCII_8("8BIT"), ASCII_7("7BIT"), BINARY("binary");
    private final String typeName;

    @Override
    public String toString() {
        return typeName;
    }

    private EncodingType(String typeName) {
        this.typeName = typeName;
    }
}
/* enum for regulate the possible message type */
enum MessageType {
    TXT("text/plain"), HTML("application/html"), XHTML("application/xhtml+xml"), GIF("image/gif"), JPG("image/jpeg"),
    PNG("image/png"), MPEG("video/mpeg"), GENER("application/octet-stream"), PDF("application/pdf"), WORD("application/msword"), RFC("message/rfc822"),
    MUTI("multipart/mixed"), MUTA("multipart/alternative");
    private final String typeName;

    @Override
    public String toString() {
        return typeName;
    }

    private MessageType(String typeName) {
        this.typeName = typeName;
    }
}
