package Main;

import View.Canvas;
import Model.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Whiteboard  extends JFrame {
    private Color pickedColor;
    private Canvas canvas;

    //GUI declarations
    private JPanel westPanel, addMenu, scriptMenu, otherMenu, saveMenu, networkMenu;
    private JButton addRect, addOval, addLine, addText, colorPicker, moveFront, moveBack, removeShape,
            saveCanvas, openCanvas, saveImage, serverBtn, clientBtn;
    private JLabel modeLbl;
    private JTextField scriptTxtField;
    private JComboBox scriptComboBox;
    private String[] fonts;
    private JTable dataTable;
    private JTableModel tableModel;

    public JTextField getScriptTxtField() {
        return this.scriptTxtField;
    }

    public JComboBox getScriptComboBox(){ return this.scriptComboBox; }

    public JTable getDataTable() { return this.dataTable; }

    public JTableModel getTableModel() { return this.tableModel; }

    //networking declarations
    private ServerSocket serverSocket;
    private final int defaultPort = 39587;
    private ArrayList<ObjectOutputStream> clientOutputStreams;
    private static final int SERVER_MODE = 0;
    private static final int CLIENT_MODE = 1;
    private int currentMode = -1; // 0- Server, 1- Client, -1: default

    public boolean isServer(){ return currentMode == SERVER_MODE;}

    public boolean isClient(){ return currentMode == CLIENT_MODE;}

    public Whiteboard(){
        initWhiteboard();
    }

    public void initWhiteboard(){
        setTitle("Whiteboard");
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(840, 400));

        canvas = new Canvas(this);
        this.add(westPanel(), BorderLayout.WEST);
        this.add(canvas, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JPanel westPanel(){
    /** big container **/
        westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setPreferredSize(new Dimension(440, 400));

    /** small containers **/
    //addMenu
        addMenu = new JPanel();
        addMenu.setLayout(new BoxLayout(addMenu, BoxLayout.X_AXIS));
        addMenu.setPreferredSize(new Dimension(400, 50));

        JLabel addLabel = new JLabel("Add: ");
        addRect = new JButton("Rect");
        addRect.addActionListener(e ->{
            // add a Rectangle
            canvas.addShape(new DRectModel());
        });
        addOval = new JButton("Oval");
        addOval.addActionListener(e -> {
            // add an Oval
            canvas.addShape(new DOvalModel());
        });
        addLine = new JButton("Line");
        addLine.addActionListener(e -> {
            // add a Line
            canvas.addShape(new DLineModel());
        });
        addText = new JButton("Text");
        addText.addActionListener(e ->{
           // add a Text
            canvas.addShape(new DTextModel());
        });
        addMenu.add(addLabel);
        addMenu.add(addRect);
        addMenu.add(addOval);
        addMenu.add(addLine);
        addMenu.add(addText);

    // colorPicker
        colorPicker = new JButton("Set color");
        colorPicker.addActionListener(e-> {
            pickedColor = JColorChooser.showDialog(colorPicker, "Set a color", Color.GRAY);
            if(pickedColor != null || pickedColor != Color.GRAY)
                canvas.setColor(pickedColor);
        });

    // scriptMenu
        scriptMenu = new JPanel();
        scriptMenu.setLayout(new BoxLayout(scriptMenu, BoxLayout.X_AXIS));
        scriptMenu.setPreferredSize(new Dimension(400,50));

        scriptTxtField = new JTextField("");
        scriptTxtField.setEnabled(false);
        scriptTxtField.setMaximumSize(new Dimension(200, 30));
        scriptTxtField.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 canvas.updateScriptText(scriptTxtField.getText());
             }

             @Override
             public void removeUpdate(DocumentEvent e) {
                canvas.updateScriptText(scriptTxtField.getText());
             }

             @Override
             public void changedUpdate(DocumentEvent e) {

             }
        });
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fonts = ge.getAvailableFontFamilyNames();
        scriptComboBox = new JComboBox(fonts);
        scriptComboBox.setSelectedIndex(-1);
        scriptComboBox.setEnabled(false);
        scriptComboBox.setMaximumSize(new Dimension(200,50));
        scriptComboBox.addItemListener(e ->{
            if(scriptComboBox.getSelectedIndex() != -1)
                canvas.setFont(scriptComboBox.getSelectedItem().toString());
        });

        scriptMenu.add(scriptTxtField);
        scriptMenu.add(scriptComboBox);

    // otherMenu: Move-to-Front, ... removeShape
        otherMenu = new JPanel();
        otherMenu.setLayout(new BoxLayout(otherMenu, BoxLayout.X_AXIS));
        otherMenu.setPreferredSize(new Dimension(400,50));

        moveFront = new JButton("Move Front");
        moveFront.addActionListener(e -> {
            // move a selected shape to front
            canvas.moveTo(true);
        });
        moveBack = new JButton("Move Back");
        moveBack.addActionListener(e ->{
            // move a selected shape to back
            canvas.moveTo(false);
        });
        removeShape = new JButton("Remove shape");
        removeShape.addActionListener(e ->{
            // remove a selected shape
            canvas.removeShape();
        });
        otherMenu.add(moveFront);
        otherMenu.add(moveBack);
        otherMenu.add(removeShape);

    // saveMenu
        saveMenu = new JPanel();
        saveMenu.setLayout(new BoxLayout(saveMenu, BoxLayout.X_AXIS));
        saveMenu.setPreferredSize(new Dimension(400,50));

        saveCanvas = new JButton("Save");
        saveCanvas.addActionListener(e ->{
            //save canvas
            saveFile(false);
        });
        openCanvas = new JButton("Open");
        openCanvas.addActionListener(e ->{
            //open canvas
            openFile();
        });
        saveImage = new JButton("Save as PNG");
        saveImage.addActionListener(e ->{
            //save image
            saveFile(true);
        });

        saveMenu.add(saveCanvas);
        saveMenu.add(openCanvas);
        saveMenu.add(saveImage);

    // network buttons
        networkMenu = new JPanel();
        networkMenu.setLayout(new BoxLayout(networkMenu, BoxLayout.X_AXIS));
        networkMenu.setPreferredSize(new Dimension(400,50));

        serverBtn = new JButton("Server Mode");
        serverBtn.addActionListener(e ->{
            String input = JOptionPane.showInputDialog("Default port:" + defaultPort, defaultPort);
            if(input != null) {
                currentMode = SERVER_MODE;
                modeLbl.setText("Server mode, port: " + input);
                serverBtn.setEnabled(false);
                clientBtn.setEnabled(false);
                //don't load file while communicating sockets
                openCanvas.setEnabled(false);
                Thread server = new Thread(new Server(Integer.parseInt(input)));
                server.start();
            }
        });

        clientBtn = new JButton("Client Mode");
        clientBtn.addActionListener(e ->{
            String input = JOptionPane.showInputDialog("Default 127.0.0.1:" + defaultPort,
                    "127.0.0.1:" + defaultPort);
            if (input != null) {
                String[] tokens = input.split(":");
                String address = tokens[0].trim();
                int port = Integer.parseInt(tokens[1].trim());
                whiteboardMode(true);
                currentMode = CLIENT_MODE;
                modeLbl.setText("Client mode, port: " + port);
                Thread client = new Thread(new Client(address, port));
                client.start();
            }
        });

        modeLbl = new JLabel("");
        modeLbl.setFont(new Font("Arial", Font.ITALIC, 12));

        networkMenu.add(serverBtn);
        networkMenu.add(clientBtn);
        networkMenu.add(modeLbl);

    // dataTable: x, y, width, height
        tableModel = new JTableModel();
        dataTable = new JTable(tableModel);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setMaximumSize(new Dimension(440, 230));

        westPanel.add(addMenu);
        westPanel.add(colorPicker);
        westPanel.add(scriptMenu);
        westPanel.add(otherMenu);
        westPanel.add(saveMenu);
        westPanel.add(networkMenu);
        westPanel.add(scrollPane);

        for(Component component: westPanel.getComponents()){
            ((JComponent)component).setAlignmentX(Box.LEFT_ALIGNMENT);
        }
        return westPanel;
    }

    private void saveFile(boolean isAsPNG){
        JFileChooser fc = new JFileChooser();
        int input = fc.showSaveDialog(this);
        if(input == JFileChooser.APPROVE_OPTION){
            try {
                if(isAsPNG){
                    Dimension canvasSize = canvas.getSize();
                    BufferedImage image = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = image.createGraphics();
                    //don't draw knobs
                    canvas.setSelectedShape(null);
                    canvas.paintAll(g2);
                    g2.dispose();
                    String path = fc.getSelectedFile().getPath();
                    path = path.substring(0, path.lastIndexOf(System.getProperty("file.separator")) + 1);
                    ImageIO.write(image, "png", new File(path + "art-" + fc.getSelectedFile().getName() + ".png"));
                    JOptionPane.showMessageDialog(null, "Canvas is saved as PNG");
                    return;
                }
                XMLEncoder saveXml = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(fc.getSelectedFile() + ".ser")));
                DShapeModel[] shapeModels = canvas.getShapeModels();
                saveXml.writeObject(shapeModels);
                saveXml.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openFile(){
        JFileChooser fc = new JFileChooser();
        int input = fc.showOpenDialog(this);
        if(input == JFileChooser.APPROVE_OPTION){
            try {
                XMLDecoder openXml = new XMLDecoder(new BufferedInputStream(new FileInputStream(fc.getSelectedFile())));
                //reload table
                for(DShapeModel model: canvas.getShapeModels()){
                    tableModel.removeRow(model);
                }
                DShapeModel[] models = (DShapeModel[]) openXml.readObject();
                //delete all existing shapes in canvas to load a file
                canvas.createNewShapes();
                for(DShapeModel model: models) {
                    canvas.addShape(model);
                }
                openXml.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Client extends Thread {

        private String addr;
        private int port;

        public Client(String addr, int port) {
            this.addr = addr;
            this.port = port;
        }

        public void run() {
            //connect client
            try {
                SocketAddress socketAddress = new InetSocketAddress(addr, port);
                Socket client = new Socket();
                //will throws connectException if client can't connect, timeoutException after 5s
                client.connect(socketAddress, 5000);
                //else remove all current shapes from client
                for(DShapeModel model: canvas.getShapeModels()){
                    canvas.setSelectedShape(canvas.getShapeFromModelID(model.getID()));
                    canvas.removeShape();
                }
                //then fetching data from server
                ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                while (true) {
                    //decode xmlString to get DShapeModel object
                    String[] cmd = (String[]) objectInputStream.readObject();
                    XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(cmd[1].getBytes()));
                    DShapeModel model = (DShapeModel) decoder.readObject();

                    clientReaction(cmd[0], model);
                }
            } catch (ConnectException ex) {
                //switch back to normal mode
                JOptionPane.showMessageDialog(null, "Connection error: No server is running");
                whiteboardMode(false);
            } catch (SocketTimeoutException timeOutEx) {
                //timeout connection, print message
                JOptionPane.showMessageDialog(null, "SocketTimeout: Client can't connect to server");
                whiteboardMode(false);
            } catch (IOException ioEx) {
                //socket exception
                JOptionPane.showMessageDialog(null, ioEx.toString());
                whiteboardMode(false);
            } catch (ClassNotFoundException cnfEx) {
                //objectInputStream exceptions
                JOptionPane.showMessageDialog(null, "ObjectOutputStream error: " + cnfEx.toString());
                whiteboardMode(false);
            }
        }
    }

    private void clientReaction(String command, DShapeModel model){
        if(!command.equals("add")) {
            canvas.setSelectedShape(canvas.getShapeFromModelID(model.getID()));
        }
        switch(command) {
            case "add":
                canvas.addShape(model);
                break;
            case "remove":
                canvas.removeShape();
                break;
            case "front":
                canvas.moveTo(true);
                break;
            case "back":
                canvas.moveTo(false);
                break;
            case "change":
                canvas.getSelectedShape().getModel().mimic(model);
                break;
            default:
                break;
        }
    }

    private class Server extends Thread{
        private int port;

        public Server(int port) {
            this.port = port;
        }

        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                clientOutputStreams = new ArrayList<>();
                //while loop to keep accept client
                while(true) {
                    //waiting if there is no client connection
                    Socket client = serverSocket.accept();
                    //outputStream is a channel to communicate between server and client
                    OutputStream outputStream = client.getOutputStream();
                    //objectOutputStream send objects from server to client
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    //client connected, add to list of connecting clients
                    clientOutputStreams.add(objectOutputStream);
                    //inform server number of client connections
                    modeLbl.setText("Server mode: " + clientOutputStreams.size() + " connection(s)");
                    //add current shapes on server to client if any
                    if(canvas.getShapes().size() > 0){
                        for(DShapeModel model: canvas.getShapeModels()){
                            String[] cmd = new String[2]; // action command and equivalent model
                            //encode model object to xml string
                            cmd[0] = "add";
                            cmd[1] = getModelXmlString(model);
                            objectOutputStream.writeObject(cmd);
                            objectOutputStream.flush();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String getModelXmlString(DShapeModel model){
        OutputStream modelStream = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(modelStream);
        encoder.writeObject(model);
        encoder.close();
        return modelStream.toString();
    }

    public synchronized void send(String cmd, DShapeModel model){
        if(model != null) {
            String[] cmds = {cmd, getModelXmlString(model)};
            for (ObjectOutputStream clientOutputStream : clientOutputStreams) {
                try {
                    clientOutputStream.writeObject(cmds);
                    clientOutputStream.flush();
                } catch (IOException ioEx) {
                    JOptionPane.showMessageDialog(null, "connection failure");
                    System.exit(0);
                }
            }
        }
    }

    private void whiteboardMode(boolean isClientMode){
        //In client mode, disable all edit buttons
        for(Component component: westPanel.getComponents()){
            if(component instanceof  JPanel){
                for(Component comp: ((JPanel)component).getComponents()) {
                    comp.setEnabled(!isClientMode);
                }
            } else {
                component.setEnabled(!isClientMode);
            }
        }
        if(!isClientMode) {
            modeLbl.setText("");
            currentMode = -1;
            scriptTxtField.setEnabled(isClientMode);
            scriptComboBox.setEnabled(isClientMode);
        }
    }

    public static void main(String[] args){
        Whiteboard whiteboard = new Whiteboard();
        whiteboard.setVisible(true);

        /*Whiteboard whiteboard1 = new Whiteboard();
        whiteboard1.setTitle("Tester Whiteboard");
        whiteboard1.setLocation(0,0);
        whiteboard1.setVisible(true);

        Whiteboard whiteboard2 = new Whiteboard();
        whiteboard2.setTitle("Tester2 Whiteboard");
        whiteboard2.setLocation(600,0);
        whiteboard2.setVisible(true);*/
    }
}
