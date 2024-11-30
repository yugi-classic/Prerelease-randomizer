/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.prerelease.randomizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * @author Marti
 */
public class CardImageViewer {

    private static final String IMAGE_PATH = "Images\\OnePiece\\OP09";

    private JFrame frame;
    private JPanel imagePanel;
    private JTextArea decklistArea;
    private List<String> currentDeck;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CardImageViewer().startViewer());
    }

    public void startViewer() {
        try {
            currentDeck = new ArrayList<>();
            createAndShowGUI();
            loadDraftPools();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAndShowGUI() {
        frame = new JFrame("Card Draft Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(0, 4, 10, 10));

        JScrollPane imageScrollPane = new JScrollPane(imagePanel);
        imageScrollPane.setPreferredSize(new Dimension(800, 800));

        decklistArea = new JTextArea(20, 30);
        decklistArea.setEditable(false);

        JScrollPane decklistScrollPane = new JScrollPane(decklistArea);
        decklistScrollPane.setPreferredSize(new Dimension(300, 800));

        JButton regenerateButton = new JButton("Neu generieren");
        regenerateButton.addActionListener(e -> {
            try {
                regenerateDraftPools();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(regenerateButton, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageScrollPane, decklistScrollPane);
        splitPane.setDividerLocation(1300);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setSize(1980, 1080);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private void loadDraftPools() throws IOException {
        PrereleaseRandomizer randomizer = new PrereleaseRandomizer();
        List<List<String>> draftPools = randomizer.randomize();

        imagePanel.removeAll();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));

        for (List<String> pool : draftPools) {
            JPanel packPanel = new JPanel();
            packPanel.setLayout(new GridLayout(0, 4, 10, 10));

            Border outerBorder = BorderFactory.createLineBorder(Color.BLUE, 7);
            Border titledBorder = BorderFactory.createTitledBorder(outerBorder, "Pack", TitledBorder.CENTER,
                    TitledBorder.CENTER, new Font("Arial", Font.BOLD, 50), Color.BLACK);
            packPanel.setBorder(titledBorder);

            for (String cardNumber : pool) {
                File imageFile = new File(IMAGE_PATH + "\\" + cardNumber + ".png");
                if (imageFile.exists()) {
                    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                    Image img = icon.getImage();
                    Image scaledImg = img.getScaledInstance(300, 419, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaledImg);

                    String[] parts = cardNumber.split("_");
                    String number = parts[0];

                    JLabel cardLabel = new JLabel(icon);
                    cardLabel.setPreferredSize(new Dimension(300, 419));
                    cardLabel.setToolTipText(cardNumber);

                    cardLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            addCardToDeck(number);
                        }
                    });

                    packPanel.add(cardLabel);
                }
            }

            imagePanel.add(packPanel);
            imagePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private void regenerateDraftPools() throws IOException {
        currentDeck.clear();
        decklistArea.setText("");
        loadDraftPools();
    }

    private void addCardToDeck(String cardNumber) {
        boolean cardFound = false;

        for (int i = 0; i < currentDeck.size(); i++) {
            String deckCard = currentDeck.get(i);
            if (deckCard.contains(cardNumber)) {
                int count = Integer.parseInt(deckCard.split("x")[0].trim());
                count++;
                currentDeck.set(i, count + "x" + cardNumber);
                cardFound = true;
                break;
            }
        }

        if (!cardFound) {
            currentDeck.add("1x" + cardNumber);
        }

        updateDecklist();
    }

    private void updateDecklist() {
        decklistArea.setText("");
        for (String card : currentDeck) {
            decklistArea.append(card + "\n");
        }
    }
}