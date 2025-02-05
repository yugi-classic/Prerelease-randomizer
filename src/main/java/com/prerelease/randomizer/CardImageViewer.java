/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.prerelease.randomizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class CardImageViewer {
    private static final String IMAGE_PATH = "images\\OnePiece";
    private JFrame frame;
    private JPanel imagePanel;
    private JTextArea decklistArea;
    private List<String> currentDeck;
    private JLabel cardCountLabel;
    private JComboBox<String> setSelectionBox;
    private String selectedSet;
    private AtomicInteger totalCards = new AtomicInteger(0);
    private JButton loadButton = new JButton("Laden");
    private JProgressBar progressBar;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CardImageViewer().startViewer();
        });
    }

    public void startViewer() {
        try {
            this.currentDeck = new ArrayList<>();
            this.createAndShowGUI();
            this.loadDraftPools();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAndShowGUI() {
        this.frame = new JFrame("Pre-Release Randomizer");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLayout(new BorderLayout());

        JPanel imageHeaderPanel = new JPanel();
        imageHeaderPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        String imagePath = "images\\random.png";
        ImageIcon icon = new ImageIcon(imagePath);
        Image headerIcon = icon.getImage();
        this.frame.setIconImage(headerIcon);

        this.imagePanel = new JPanel();
        this.imagePanel.setLayout(new GridLayout(0, 4, 10, 10));
        JScrollPane imageScrollPane = new JScrollPane(this.imagePanel);
        imageScrollPane.setPreferredSize(new Dimension(600, 600));

        this.decklistArea = new JTextArea(20, 30);
        this.decklistArea.setEditable(false);
        JScrollPane decklistScrollPane = new JScrollPane(this.decklistArea);
        decklistScrollPane.setPreferredSize(new Dimension(200, 600));

        this.cardCountLabel = new JLabel("Karten im Deck: 0");
        this.cardCountLabel.setFont(new Font("Arial", Font.BOLD, 20));

        String[] sets = this.loadSetsFromDirectory("data/json");
        if (sets.length == 0) {
            sets = new String[] {
                    "Keine Sets gefunden"
            };
        }

        this.setSelectionBox = new JComboBox<>(sets);
        this.setSelectionBox.setSelectedItem(this.selectedSet);
        this.setSelectionBox.addActionListener(e -> this.selectedSet = (String) this.setSelectionBox.getSelectedItem());

        this.loadButton.addActionListener(e -> {
            try {
                this.regenerateDraftPools();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        topPanel.add(new JLabel("Set Auswahl:"));
        topPanel.add(this.setSelectionBox);
        topPanel.add(this.loadButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageScrollPane, decklistScrollPane);
        splitPane.setDividerLocation(1300);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(this.cardCountLabel, BorderLayout.SOUTH);

        this.frame.add(northPanel, BorderLayout.NORTH);
        this.frame.add(splitPane, BorderLayout.CENTER);
        this.frame.add(controlPanel, BorderLayout.SOUTH);

        this.frame.setSize(1980, 1080);
        this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.frame.setVisible(true);
    }

    private void loadDraftPools() throws IOException {
        if (this.selectedSet != null) {
            String setPath = "data/json/OnePiece-" + this.selectedSet + ".json";
            PrereleaseRandomizer randomizer = new PrereleaseRandomizer();
            final List<List<String>> draftPools = randomizer.randomize(setPath);

            this.imagePanel.removeAll();
            this.imagePanel.setLayout(new BoxLayout(this.imagePanel, BoxLayout.Y_AXIS));

            this.progressBar = new JProgressBar(0, draftPools.size() * 6 * 2);
            this.progressBar.setStringPainted(true);
            this.progressBar.setIndeterminate(false);
            this.progressBar.setValue(0);
            this.totalCards.set(0);

            this.imagePanel.add(this.progressBar);
            this.imagePanel.add(Box.createRigidArea(new Dimension(0, 10)));
            this.imagePanel.revalidate();
            this.imagePanel.repaint();

            this.setSelectionBox.setEnabled(false);
            this.loadButton.setEnabled(false);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    for (List<String> pool : draftPools) {
                        JPanel packPanel = new JPanel();
                        packPanel.setLayout(new GridLayout(0, 4, 5, 5));
                        Border outerBorder = BorderFactory.createLineBorder(Color.BLUE, 7);
                        Border titledBorder = BorderFactory.createTitledBorder(outerBorder, "Pack", TitledBorder.CENTER,
                                TitledBorder.BOTTOM, new Font("Arial", Font.BOLD, 50), Color.BLACK);
                        packPanel.setBorder(titledBorder);

                        for (String cardNumber : pool) {
                            ImageIcon icon = null;
                            try {
                                icon = DropboxAuth.auth(selectedSet, cardNumber);
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            String[] parts = cardNumber.split("_");
                            String number = parts[0];
                            JLabel cardLabel = new JLabel(icon);
                            cardLabel.setPreferredSize(new Dimension(150, 210));
                            cardLabel.setToolTipText(cardNumber);
                            cardLabel.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseReleased(MouseEvent e) {
                                    if (e.getButton() == MouseEvent.BUTTON3) {
                                        removeCardFromDeck(number);
                                    }
                                    else if (e.getButton() == MouseEvent.BUTTON1) {
                                        addCardToDeck(number);
                                    }
                                }
                            });
                            addCardToDeck(number);

                            packPanel.add(cardLabel);
                            packPanel.revalidate();
                            packPanel.repaint();
                            imagePanel.add(packPanel);
                            progressBar.setValue(totalCards.incrementAndGet());
                            progressBar.repaint();
                        }
                    }

                    SwingUtilities.invokeLater(() -> {
                        loadButton.setEnabled(true);
                        setSelectionBox.setEnabled(true);
                    });

                    return null;
                }
            }.execute();
        }
    }

    private void regenerateDraftPools() throws IOException {
        currentDeck.clear();
        decklistArea.setText("");
        updateCardCount();
        loadDraftPools();
    }

    private void removeCardFromDeck(String cardNumber) {
        for (int i = 0; i < currentDeck.size(); ++i) {
            String deckCard = currentDeck.get(i);
            if (deckCard.contains(cardNumber)) {
                int count = Integer.parseInt(deckCard.split("x")[0].trim());
                if (count > 1) {
                    currentDeck.set(i, (count - 1) + "x" + cardNumber);
                }
                else {
                    currentDeck.remove(i);
                }
                break;
            }
        }

        updateDecklist();
        updateCardCount();
    }

    private void addCardToDeck(String cardNumber) {
        boolean cardFound = false;

        for (int i = 0; i < currentDeck.size(); ++i) {
            String deckCard = currentDeck.get(i);
            if (deckCard.contains(cardNumber)) {
                int count = Integer.parseInt(deckCard.split("x")[0].trim());
                currentDeck.set(i, (count + 1) + "x" + cardNumber);
                cardFound = true;
                break;
            }
        }

        if (!cardFound) {
            currentDeck.add("1x" + cardNumber);
        }

        updateDecklist();
        updateCardCount();
    }

    private void updateDecklist() {
        decklistArea.setText("");
        for (String card : currentDeck) {
            decklistArea.append(card + "\n");
        }
    }

    private void updateCardCount() {
        int cardCount = currentDeck.stream().mapToInt(card -> Integer.parseInt(card.split("x")[0].trim())).sum();
        cardCountLabel.setText("Karten im Deck: " + cardCount);
    }

    private String[] loadSetsFromDirectory(String directoryPath) {
        URL resourceUrl = this.getClass().getClassLoader().getResource(directoryPath);
        if (resourceUrl == null) {
            System.out.println("Ressource nicht gefunden: " + directoryPath);
            return new String[0];
        }

        if (resourceUrl.getProtocol().equals("jar")) {
            try {
                String path = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
                JarFile jar = new JarFile(path);
                Enumeration<JarEntry> entries = jar.entries();
                List<String> setsList = new ArrayList<>();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith(directoryPath) && entry.getName().endsWith(".json")) {
                        String fileName = entry.getName().substring(entry.getName().indexOf("-") + 1,
                                entry.getName().lastIndexOf('.'));
                        setsList.add(fileName);
                    }
                }

                jar.close();
                return setsList.toArray(new String[0]);
            }
            catch (IOException e) {
                e.printStackTrace();
                return new String[0];
            }
        }

        File directory = new File(resourceUrl.getFile());
        if (directory.exists() && directory.isDirectory()) {
            File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
            if (jsonFiles != null && jsonFiles.length != 0) {
                return Arrays.stream(jsonFiles).map(file -> file.getName().substring(9, 13)).toArray(String[]::new);
            }
            else {
                System.out.println("Keine JSON-Dateien gefunden im Verzeichnis: " + directoryPath);
                return new String[0];
            }
        }
        else {
            System.out.println("Verzeichnis nicht gefunden oder kein Verzeichnis: " + directoryPath);
            return new String[0];
        }
    }
}
