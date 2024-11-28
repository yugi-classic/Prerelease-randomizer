/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.prerelease.randomizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * @author Marti
 */
public class CardImageViewer {

    private static final String IMAGE_PATH = "Images\\OnePiece\\OP09";
    private static final String DECKLIST_PATH = "Decklist.txt";

    private static CardImageViewer instance;
    private JFrame frame;
    private JPanel imagePanel;
    private JTextArea decklistArea;

    public static void openViewer() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (instance == null) {
                    instance = new CardImageViewer();
                    instance.createAndShowGUI();
                }
                else {
                    instance.updateDeckContent();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void createAndShowGUI() throws IOException {
        frame = new JFrame("Card Image Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JButton randomizeButton = new JButton("Randomize Deck");
        randomizeButton.addActionListener(e -> randomizeDeck());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(randomizeButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(10, 10));

        JScrollPane imageScrollPane = new JScrollPane(imagePanel);
        imageScrollPane.setPreferredSize(new Dimension(800, 800));

        decklistArea = new JTextArea(20, 30);
        decklistArea.setEditable(false);

        JScrollPane decklistScrollPane = new JScrollPane(decklistArea);
        decklistScrollPane.setPreferredSize(new Dimension(300, 800));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageScrollPane, decklistScrollPane);
        splitPane.setDividerLocation(800);
        frame.add(splitPane, BorderLayout.CENTER);

        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        updateDeckContent();
    }

    private void randomizeDeck() {
        try {
            new PrereleaseRandomizer().randomize();
            updateDeckContent();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDeckContent() throws IOException {
        Set<String> cardNumbersInDecklist = readCardNumbersFromDecklist(DECKLIST_PATH);

        imagePanel.removeAll();

        File imageDirectory = new File(IMAGE_PATH);
        File[] imageFiles = imageDirectory.listFiles((dir, name) -> {
            String cardNumberFromName = name.split("_")[0];
            return name.endsWith(".png") && cardNumbersInDecklist.contains(cardNumberFromName);
        });

        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                String imageName = imageFile.getName();
                String cardNumber = imageName.split("_")[0];

                if (cardNumbersInDecklist.contains(cardNumber)) {
                    ImageIcon imageIcon = new ImageIcon(imageFile.getAbsolutePath());
                    Image img = imageIcon.getImage();
                    Image scaledImg = img.getScaledInstance(200, 280, Image.SCALE_SMOOTH);
                    imageIcon = new ImageIcon(scaledImg);

                    JLabel imageLabel = new JLabel(imageIcon);
                    imageLabel.setPreferredSize(new Dimension(200, 280));

                    imagePanel.add(imageLabel);
                }
            }
        }

        if (imagePanel.getComponentCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Es wurden keine passenden Bilder für die Kartennummern gefunden.",
                    "Keine Bilder gefunden", JOptionPane.INFORMATION_MESSAGE);
        }

        decklistArea.setText(readDecklistText(DECKLIST_PATH));

        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private Set<String> readCardNumbersFromDecklist(String decklistPath) throws IOException {
        Set<String> cardNumbers = new HashSet<>();
        List<String> lines = Files.readAllLines(Paths.get(decklistPath));

        for (String line : lines) {
            if (line.contains("x")) {
                String cardNumber = line.split("x")[1].trim();
                cardNumbers.add(cardNumber);
            }
        }

        return cardNumbers;
    }

    private String readDecklistText(String decklistPath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(decklistPath));
        return String.join("\n", lines);
    }
}