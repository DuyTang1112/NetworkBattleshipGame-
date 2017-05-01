/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BattleShip;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 *
 * @author Duy Anh Tang
 */
public class ServerFrame extends JFrame {

    private final int SCREEN_WIDTH = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int SCREEN_HEIGHT = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
    private final int APP_WIDTH = SCREEN_WIDTH * 7 / 10;
    private final int APP_HEIGHT = SCREEN_HEIGHT * 7 / 10;
    private final int BATTLESHIP_PORT = 9090;
    private JButton[] buttonFriend, buttonFoe;
    private JButton startButt, randomizeButton;
    private List<Ship> shipList;
    private int[] checkerBox;
    private Timer time;
    private int friend_counter, foe_counter, clock_count;
    private JLabel timer, status;
    private JPanel panelFriend, panelFoe;
    private ActionListener gridClick;
    public boolean ready1, ready2, meTurn;
    private ServerSocket server;
    // private Socket socket;
    private GameServer gs;
    private String myAttackLoc;

    public ServerFrame() {
        initialize();
        //listenForConnection();
    }

    private void listenForConnection() {
        try {
            if (server == null || server.isClosed()) {
                server = new ServerSocket(9090, 10);
            }
            // System.out.println("Listening");
            //socket = server.accept();
            gs = new GameServer(server);
            gs.start();
            //System.out.println("Finished listening for Connection");

        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }
    }

    /**
     *
     * @param s
     * @return The translated index from the given String coordinate Example:
     * "A1" returns 0, "B1" returns 1, "A2" returns 10
     */
    public int decodeCoor(String s) {
        if (s.length() != 2) {
            if (s.length() == 3) {
                return (Integer.parseInt(s.substring(1)) - 1) * 10 + (int) (s.charAt(0) - 65);
            }
            return -1;
        }
        return (Integer.parseInt(s.charAt(1) + "") - 1) * 10 + (int) (s.charAt(0) - 65);
    }

    /**
     * Initialize the GUI components. Should only initialize once
     */
    public void initialize() {
        gridClick = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!ready1 || !ready2) {
                    return;
                }
                if (!meTurn) {
                    return;
                }
                myAttackLoc = ae.getActionCommand();
                JButton b = (JButton) ae.getSource();
                b.removeActionListener(gridClick);
                meTurn = false;
            }
        };
        setTitle("BattleShip/Server");
        setResizable(false);
        setSize(APP_WIDTH, APP_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        //set the player's board
        panelFriend = new JPanel(new GridLayout(10, 10));
        //panelFriend.setSize(APP_HEIGHT*6/10, APP_HEIGHT*6/10);
        buttonFriend = new JButton[100];
        for (int i = 0; i < buttonFriend.length; i++) {
            buttonFriend[i] = new JButton();
            buttonFriend[i].setBackground(Color.white);
            //buttonFriend[i].setEnabled(false);
            //buttonFriend[i].setIcon(new ImageIcon("X.png"));
            panelFriend.add(buttonFriend[i]);

        }
        panelFriend.setBounds((int) (APP_WIDTH * 0.05), (int) (APP_HEIGHT * 0.1), APP_HEIGHT * 6 / 10, APP_HEIGHT * 6 / 10);
        panelFriend.setVisible(true);

        //set enemy board
        panelFoe = new JPanel(new GridLayout(10, 10));
        buttonFoe = new JButton[100];
        for (int i = 0; i < buttonFoe.length; i++) {
            buttonFoe[i] = new JButton();
            buttonFoe[i].setBackground(Color.white);
            buttonFoe[i].setActionCommand(String.valueOf((char) (i % 10 + 65)) + "" + ((i / 10) + 1));
            //buttonFoe[i].setIcon(new ImageIcon("X.png"));
            //buttonFoe[i].addActionListener(gridClick);
            //System.out.println(buttonFoe[i].getActionCommand());
            panelFoe.add(buttonFoe[i]);
        }
        panelFoe.setBounds((int) (APP_WIDTH * 0.55), (int) (APP_HEIGHT * 0.1), APP_HEIGHT * 6 / 10, APP_HEIGHT * 6 / 10);
        panelFoe.setVisible(true);

        // set timer
        timer = new JLabel();
        timer.setBounds((int) (APP_WIDTH * 0.45), (int) (APP_HEIGHT * 0.1), 80, 10);
        clock_count = 0;
        time = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                timer.setText(String.format("%02d : %02d", (int) (clock_count / 60), clock_count % 60));
                clock_count++;
            }
        });
        status = new JLabel();
        status.setBounds((int) (APP_WIDTH * 0.43), (int) (APP_HEIGHT * 0.03), 150, 20);
        startButt = new JButton("Ready");
        startButt.setBounds((int) (APP_WIDTH * 0.40), (int) (APP_HEIGHT * 0.8), 150, 40);
        startButt.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                startButt.setText("Player1 is ready");
                startButt.setEnabled(false);
                randomizeButton.setEnabled(false);
                status.setText("Waiting to connect");
                ready1 = true;
                listenForConnection();
            }
        });
        randomizeButton = new JButton("Randomize");
        randomizeButton.setBounds((int) (APP_WIDTH * 0.05), (int) (APP_HEIGHT * 0.85), 100, 30);
        randomizeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                for (JButton i : buttonFriend) {
                    i.setBackground(Color.white);
                }
                generateBattleShips();
            }
        });
        add(panelFriend);
        add(panelFoe);
        add(timer);
        add(startButt);
        add(randomizeButton);
        add(status);
        //adding label
        for (int i = 0; i < 10; i++) {
            JLabel j = new JLabel((char) (i + 65) + "");
            j.setBounds((int) (APP_WIDTH * 0.06) + i * APP_HEIGHT * 6 / 100, (int) (APP_HEIGHT * 0.08), 15, 10);
            JLabel j2 = new JLabel((char) (i + 65) + "");
            j2.setBounds((int) (APP_WIDTH * 0.56) + i * APP_HEIGHT * 6 / 100, (int) (APP_HEIGHT * 0.08), 15, 10);
            JLabel j3 = new JLabel(i + 1 + "");
            j3.setHorizontalAlignment(SwingConstants.RIGHT);
            j3.setBounds((int) (APP_WIDTH * 0.03), (int) (APP_HEIGHT * 0.12) + i * APP_HEIGHT * 6 / 100, 15, 10);
            JLabel j4 = new JLabel(i + 1 + "");
            j4.setHorizontalAlignment(SwingConstants.RIGHT);
            j4.setBounds((int) (APP_WIDTH * 0.53), (int) (APP_HEIGHT * 0.12) + i * APP_HEIGHT * 6 / 100, 15, 10);
            add(j);
            add(j2);
            add(j3);
            add(j4);
        }
        generateBattleShips();
    }

    /**
     * Generates 1 ship size 5
     */
    public void generate5() {

        //generate 1 ship size 5
        Ship s = new Ship();
        Random r = new Random();
        if (r.nextFloat() < .5) { //generate horizontally
            int place = r.nextInt(100);
            while (true) {
                if (place % 10 > 5) {
                    place = r.nextInt(100);
                } else {
                    break;
                }
            }
            if (place - 1 >= 0 && place % 10 != 0) {
                checkerBox[place - 1] = 0;
                // buttonFriend[place - 1].setBackground(Color.black);
            }
            if (place + 5 < 100 && (place + 5) % 10 != 0) {
                checkerBox[place + 5] = 0;
                // buttonFriend[place + 5].setBackground(Color.black);
            }
            System.out.println(place);
            for (int i = place; i < place + 5; i++) {
                checkerBox[i] = 1;
                buttonFriend[i].setBackground(Color.yellow);
                s.addLocation(i);
            }
            if (place - 10 >= 0) {
                for (int i = place - 11; i < place - 11 + 7; i++) {
                    if (i < 0 || i >= 100) {
                        continue;
                    }
                    if (place % 10 == 0 && i % 10 == 9) {
                        continue;
                    }
                    if (place % 10 == 5 && i % 10 == 0) {
                        continue;
                    }
                    checkerBox[i] = 0;
                    // buttonFriend[i].setBackground(Color.black);
                }

            }
            if (place + 10 < 100) {
                for (int i = place + 9; i < place + 9 + 7; i++) {
                    if (i < 0 || i >= 100) {
                        continue;
                    }
                    if (place % 10 == 0 && i % 10 == 9) {
                        continue;
                    }
                    if (place % 10 == 5 && i % 10 == 0) {
                        continue;
                    }
                    checkerBox[i] = 0;
                    //buttonFriend[i].setBackground(Color.black);
                }

            }
        } else {//generate vertically
            int place = r.nextInt(100);
            while (true) {
                if (place / 10 > 5) {
                    place = r.nextInt(100);
                } else {
                    break;
                }
            }

            System.out.println("Vertically" + place);
            if (place - 10 >= 0) {
                checkerBox[place - 10] = 0;
                //buttonFriend[place - 10].setBackground(Color.black);
            }
            if (place + 10 * 5 < 100) {
                checkerBox[place + 50] = 0;
                //buttonFriend[place + 50].setBackground(Color.black);
            }
            for (int i = place; i < place + 10 * 5; i += 10) {
                checkerBox[i] = 1;
                buttonFriend[i].setBackground(Color.yellow);
                s.addLocation(i);
            }
            if (place - 1 >= 0 && (place - 1) % 10 != 9) {
                for (int i = place - 11; i < place - 11 + 7 * 10; i += 10) {
                    if (i < 0 || i >= 100) {
                        continue;
                    }
                    checkerBox[i] = 0;
                    //buttonFriend[i].setBackground(Color.black);
                }
            }
            if (place + 1 < 100 && (place + 1) % 10 != 0) {
                for (int i = place - 9; i < place - 9 + 7 * 10; i += 10) {
                    if (i < 0 || i >= 100) {
                        continue;
                    }
                    checkerBox[i] = 0;
                    //buttonFriend[i].setBackground(Color.black);
                }

            }
        }
        shipList.add(s);
    }

    /**
     * Generates 1 ship size 4
     */
    public void generate4() {
        for (int k = 0; k < 1; k++) {
            Random r = new Random();
            Ship s = new Ship();
            if (r.nextFloat() < 0.5) {//horizontally
                int place = r.nextInt(100);
                while (true) {
                    if (place % 10 > 6) {
                        place = r.nextInt(100);
                        continue;
                    } else {
                        if (checkerBox[place] >= 0 || checkerBox[place + 1] >= 0 || checkerBox[place + 2] >= 0 || checkerBox[place + 3] >= 0) {
                            place = r.nextInt(100);
                            continue;
                        } else {
                            break;
                        }
                    }
                }
                System.out.println("4Horizontal:" + place);
                if (place - 1 >= 0 && place % 10 != 0) {
                    checkerBox[place - 1] = 0;
                    //buttonFriend[place - 1].setBackground(Color.black);
                }
                if ((place + 4) % 10 != 0) {
                    checkerBox[place + 4] = 0;
                    //buttonFriend[place + 4].setBackground(Color.black);
                }
                for (int i = place; i < place + 4; i++) {
                    checkerBox[i] = 1;
                    buttonFriend[i].setBackground(Color.GREEN);
                    s.addLocation(i);
                }
                if (place - 10 >= 0) {
                    for (int i = place - 11; i < place - 11 + 6; i++) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        if (place % 10 == 0 && i % 10 == 9) {
                            continue;
                        }
                        if (place % 10 == 6 && i % 10 == 0) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }

                }
                if (place + 10 < 100) {
                    for (int i = place + 9; i < place + 9 + 6; i++) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        if (place % 10 == 0 && i % 10 == 9) {
                            continue;
                        }
                        if (place % 10 == 6 && i % 10 == 0) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }

                }
            } else {//vertically
                int place = r.nextInt(100);
                while (true) {
                    if (place / 10 > 6) {
                        place = r.nextInt(100);
                        continue;
                    } else {
                        if (checkerBox[place] >= 0 || checkerBox[place + 10] >= 0 || checkerBox[place + 20] >= 0 || checkerBox[place + 30] >= 0) {
                            place = r.nextInt(100);
                            continue;
                        } else {
                            break;
                        }
                    }
                }

                System.out.println("4Vertically" + place);
                if (place - 10 >= 0) {
                    checkerBox[place - 10] = 0;
                    // buttonFriend[place - 10].setBackground(Color.black);
                }
                if (place + 10 * 4 < 100) {
                    checkerBox[place + 40] = 0;
                    //buttonFriend[place + 40].setBackground(Color.black);
                }
                for (int i = place; i < place + 10 * 4; i += 10) {
                    checkerBox[i] = 1;
                    buttonFriend[i].setBackground(Color.GREEN);
                    s.addLocation(i);
                }
                if (place - 1 >= 0 && (place - 1) % 10 != 9) {
                    for (int i = place - 11; i < place - 11 + 6 * 10; i += 10) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }

                }
                if (place + 1 < 100 && (place + 1) % 10 != 0) {
                    for (int i = place - 9; i < place - 9 + 6 * 10; i += 10) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }
                }
            }
            shipList.add(s);
        }
    }

    /**
     * Generates 2 ship size 3
     */
    public void generate3() {
        for (int k = 0; k < 2; k++) {
            Ship s = new Ship();
            Random r = new Random();
            if (r.nextFloat() < 0.5) {//horizontally
                int place = r.nextInt(100);
                while (true) {
                    if (place % 10 > 7) {
                        place = r.nextInt(100);
                        continue;
                    } else {
                        if (checkerBox[place] >= 0 || checkerBox[place + 1] >= 0 || checkerBox[place + 2] >= 0) {
                            place = r.nextInt(100);
                            continue;
                        } else {
                            break;
                        }
                    }
                }
                System.out.println("3Horizontal:" + place);
                if (place - 1 >= 0 && place % 10 != 0) {
                    checkerBox[place - 1] = 0;
                    // buttonFriend[place - 1].setBackground(Color.black);
                }
                if ((place + 3) % 10 != 0) {
                    checkerBox[place + 3] = 0;
                    // buttonFriend[place + 3].setBackground(Color.black);
                }
                for (int i = place; i < place + 3; i++) {
                    checkerBox[i] = 1;
                    buttonFriend[i].setBackground(Color.MAGENTA);
                    s.addLocation(i);
                }
                if (place - 10 >= 0) {
                    for (int i = place - 11; i < place - 11 + 5; i++) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        if (place % 10 == 0 && i % 10 == 9) {
                            continue;
                        }
                        if (place % 10 == 7 && i % 10 == 0) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }

                }
                if (place + 10 < 100) {
                    for (int i = place + 9; i < place + 9 + 5; i++) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        if (place % 10 == 0 && i % 10 == 9) {
                            continue;
                        }
                        if (place % 10 == 7 && i % 10 == 0) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }

                }
            } else {//vertically
                int place = r.nextInt(100);
                while (true) {
                    if (place / 10 > 7) {
                        place = r.nextInt(100);
                        continue;
                    } else {
                        if (checkerBox[place] >= 0 || checkerBox[place + 10] >= 0 || checkerBox[place + 20] >= 0) {
                            place = r.nextInt(100);
                            continue;
                        } else {
                            break;
                        }
                    }
                }

                System.out.println("3Vertically" + place);
                if (place - 10 >= 0) {
                    checkerBox[place - 10] = 0;
                    //buttonFriend[place - 10].setBackground(Color.black);
                }
                if (place + 10 * 3 < 100) {
                    checkerBox[place + 30] = 0;
                    // buttonFriend[place + 30].setBackground(Color.black);
                }
                for (int i = place; i < place + 10 * 3; i += 10) {
                    checkerBox[i] = 1;
                    buttonFriend[i].setBackground(Color.MAGENTA);
                    s.addLocation(i);
                }
                if (place - 1 >= 0 && (place - 1) % 10 != 9) {
                    for (int i = place - 11; i < place - 11 + 5 * 10; i += 10) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }

                }
                if (place + 1 < 100 && (place + 1) % 10 != 0) {
                    for (int i = place - 9; i < place - 9 + 5 * 10; i += 10) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }

                }
            }
            shipList.add(s);
        }
    }

    /**
     * Generates 1 ship size 2
     */
    public void generate2() {
        for (int k = 0; k < 1; k++) {
            Ship s = new Ship();
            Random r = new Random();
            if (r.nextFloat() < 0.5) {//horizontally
                int place = r.nextInt(100);
                while (true) {
                    if (place % 10 > 8) {
                        place = r.nextInt(100);
                        continue;
                    } else {
                        if (checkerBox[place] >= 0 || checkerBox[place + 1] >= 0) {
                            place = r.nextInt(100);
                            continue;
                        } else {
                            break;
                        }
                    }
                }
                System.out.println("2Horizontal:" + place);
                if (place - 1 >= 0 && place % 10 != 0) {
                    checkerBox[place - 1] = 0;
                    // buttonFriend[place - 1].setBackground(Color.black);
                }
                if ((place + 2) % 10 != 0) {
                    checkerBox[place + 2] = 0;
                    // buttonFriend[place + 2].setBackground(Color.black);
                }
                for (int i = place; i < place + 2; i++) {
                    checkerBox[i] = 1;
                    buttonFriend[i].setBackground(Color.blue);
                    s.addLocation(i);
                }

                if (place - 10 >= 0) {
                    for (int i = place - 11; i < place - 11 + 4; i++) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        if (place % 10 == 0 && i % 10 == 9) {
                            continue;
                        }
                        if (place % 10 == 8 && i % 10 == 0) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        // buttonFriend[i].setBackground(Color.black);
                    }

                }
                if (place + 10 < 100) {
                    for (int i = place + 9; i < place + 9 + 4; i++) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        if (place % 10 == 0 && i % 10 == 9) {
                            continue;
                        }
                        if (place % 10 == 8 && i % 10 == 0) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        // buttonFriend[i].setBackground(Color.black);
                    }

                }

            } else {//vertically
                int place = r.nextInt(100);
                while (true) {
                    if (place / 10 > 8) {
                        place = r.nextInt(100);
                        continue;
                    } else {
                        if (checkerBox[place] >= 0 || checkerBox[place + 10] >= 0) {
                            place = r.nextInt(100);
                            continue;
                        } else {
                            break;
                        }
                    }
                }

                System.out.println("2Vertically" + place);
                if (place - 10 >= 0) {
                    checkerBox[place - 10] = 0;
                    //buttonFriend[place - 10].setBackground(Color.black);
                }
                if (place + 10 * 2 < 100) {
                    checkerBox[place + 20] = 0;
                    //buttonFriend[place + 20].setBackground(Color.black);
                }
                for (int i = place; i < place + 10 * 2; i += 10) {
                    checkerBox[i] = 1;
                    buttonFriend[i].setBackground(Color.blue);
                    s.addLocation(i);
                }
                if (place - 1 >= 0 && (place - 1) % 10 != 9) {
                    for (int i = place - 11; i < place - 11 + 4 * 10; i += 10) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        //buttonFriend[i].setBackground(Color.black);
                    }

                }
                if (place + 1 < 100 && (place + 1) % 10 != 0) {
                    for (int i = place - 9; i < place - 9 + 4 * 10; i += 10) {
                        if (i < 0 || i >= 100) {
                            continue;
                        }
                        checkerBox[i] = 0;
                        // buttonFriend[i].setBackground(Color.black);
                    }

                }
            }
            shipList.add(s);
        }
    }

    /**
     * Resets and generates battleships and resets the statistics
     */
    public void generateBattleShips() {
        for (JButton i : buttonFoe) {
            i.removeActionListener(gridClick);
            i.addActionListener(gridClick);
            i.setIcon(null);
        }
        for (JButton i : buttonFriend) {
            i.setIcon(null);
            i.setBackground(Color.white);
        }
        ready1 = ready2 = meTurn = false;

        clock_count = 0;
        checkerBox = new int[buttonFoe.length];
        for (int i = 0; i < checkerBox.length; i++) {
            checkerBox[i] = -1;
        }
        shipList = new Vector<>();
        generate5();
        generate4();
        generate3();
        generate2();
        for (int i = 0; i < shipList.size(); i++) {
            System.out.println(shipList.get(i));
        }
        friend_counter = foe_counter = shipList.size();
    }

    /**
     *
     * @param loc: location the opponent will hit
     * @return 1 if it hits, 0 if it does not, 2 if it hits and sinks the ship
     */
    public int opponentAttack(int loc) {
        for (int i = 0; i < shipList.size(); i++) {
            if (shipList.get(i).hit(loc)) {
                if (shipList.get(i).isExploded()) {
                    return 2;
                } else {
                    return 1;
                }
            }
        }
        return 0;
    }

    class GameServer extends Thread {

        private Socket socket;
        private ServerSocket sv;
        private DataInputStream in;
        private DataOutputStream out;

        public GameServer(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                System.err.println(ex.toString());
            }
        }

        public GameServer(ServerSocket sv) {
            this.sv = sv;
        }

        @Override
        public void run() {
            System.out.println("Listening for connection...");
            try {
                this.socket = sv.accept();
                System.out.println("Connected " + socket.toString());
                try {
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                } catch (IOException ex) {
                    System.err.println(ex.toString());
                }
                //generateBattleShips();
                out.writeUTF("READY");
                ready1 = true;
                if (in.readUTF().equals("READY")) {
                    time.start();
                    status.setText("CONNECTED");
                    ready2 = true;
                }
                while (friend_counter != 0 && foe_counter != 0) { //condition for game is not finished
                    if (!meTurn) // if it is the other player's turn, player 2 is attacking
                    {
                        status.setText("PLAYER 2's TURN");
                        status.setForeground(Color.red);
                        int loc = decodeCoor(in.readUTF());// storing location
                        int result = opponentAttack(loc); // result of opponent attacking
                        if (result >= 1) {
                            buttonFriend[loc].setIcon(new ImageIcon("X.png"));
                            if (result == 2) {
                                friend_counter--;
                                out.writeUTF("SINK");
                            } else {
                                out.writeUTF("HIT");
                            }
                        } else {
                            buttonFriend[loc].setIcon(new ImageIcon("miss.png"));
                            out.writeUTF("MISS");
                        }

                        meTurn = true;
                    } else { //server is attacking
                        status.setText("PLAYER 1's TURN");
                        status.setForeground(Color.green);
                        try {
                            while (meTurn) {
                                continue;
                            }
                            out.writeUTF(myAttackLoc);
                            switch (in.readUTF()) {
                                case "HIT":
                                    buttonFoe[decodeCoor(myAttackLoc)].setIcon(new ImageIcon("X.png"));
                                    break;
                                case "SINK":
                                    buttonFoe[decodeCoor(myAttackLoc)].setIcon(new ImageIcon("sank.png"));
                                    foe_counter--;
                                    break;
                                case "MISS":
                                    buttonFoe[decodeCoor(myAttackLoc)].setIcon(new ImageIcon("miss.png"));
                                    break;
                            }
                        } catch (Exception ex) {
                            System.out.println(ex.toString());
                        }
                    }
                }
                if (foe_counter == 0) {
                    status.setText("YOU WON");
                    status.setForeground(Color.green);
                } else {
                    status.setText("YOU LOST");
                    status.setForeground(Color.red);
                }
                time.stop();
                randomizeButton.setEnabled(true);
                startButt.setEnabled(true);
                startButt.setText("Ready");
                socket.close();
                //sv.close();
                generateBattleShips();
            } catch (IOException ex) {
                System.err.println(ex.toString());
            }

        }

    }

    public static void main(String[] args) {
        Object[] options = {"Server",
            "Client"};
        int n = JOptionPane.showOptionDialog(null,
                "Would you like to run as server or client?",
                "Select options",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, //do not use a custom Icon
                options, //the titles of buttons
                options[0]); //default button title
        if (n == JOptionPane.YES_OPTION) {
            new ServerFrame().setVisible(true);
        }
        else if (n==JOptionPane.NO_OPTION){
           new ClientFrame().setVisible(true);
        }
        //System.out.println(s.decodeCoor("A2"));
    }

}
