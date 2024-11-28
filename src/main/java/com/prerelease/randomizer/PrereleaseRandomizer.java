/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.prerelease.randomizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PrereleaseRandomizer {

    private static final String PATH_TO_JSON = "Data\\json\\OnePiece-OP09.json";
    private static final String OUTPUT_FILE = "Decklist.txt";
    private static final int MAX_COPIES = 4;
    private static final int MAX_SUPER_RARE_CARDS = 6;
    private static final int DECK_SIZE = 41;

    public void randomize() {
        try {
            Map<String, String> cardData = extractCardData(PATH_TO_JSON);
            List<String> decklist = generateDecklist(cardData);
            writeDecklistToFile(decklist, OUTPUT_FILE);
            CardImageViewer.openViewer();
        }
        catch (IOException e) {
            System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
        }
    }

    private static Map<String, String> extractCardData(String pathToJson) throws IOException {
        Map<String, String> cardData = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.readTree(Paths.get(pathToJson).toFile());
        rootNode.forEach(card -> {
            JsonNode cardNumberNode = card.get("Card-Number");
            JsonNode rarityNode = card.get("Rarity");
            if (cardNumberNode != null && rarityNode != null) {
                cardData.put(cardNumberNode.asText(), rarityNode.asText());
            }
        });

        return cardData;
    }

    private static List<String> generateDecklist(Map<String, String> cardData) {
        Random random = new Random();
        List<String> decklist = new ArrayList<>();
        List<String> cardNumbers = new ArrayList<>(cardData.keySet());
        List<String> rarities = new ArrayList<>(cardData.values());

        int totalCards = 0;
        int rareCardCount = 0;
        boolean leaderCardAdded = false;

        while (!cardNumbers.isEmpty() && totalCards < DECK_SIZE) {
            int randomIndex = random.nextInt(cardNumbers.size());
            String card = cardNumbers.get(randomIndex);
            String rarity = rarities.get(randomIndex);

            if (isRareCard(rarity)) {
                double value = random.nextDouble();
                if (value > 0.2) {
                    cardNumbers.remove(randomIndex);
                    rarities.remove(randomIndex);
                    continue;
                }
            }

            if (rarity.contains("Leader/Alternate-Art")) {
                card = "P-999";
                if (leaderCardAdded) {
                    continue;
                }
                decklist.add("1x" + card);
                totalCards++;
                leaderCardAdded = true;

                cardNumbers.remove(randomIndex);
                rarities.remove(randomIndex);
                continue;
            }

            if (isRareCard(rarity) && rareCardCount >= MAX_SUPER_RARE_CARDS) {
                continue;
            }

            int maxCopies = Math.min(MAX_COPIES, DECK_SIZE - totalCards);
            int copies = generateWeightedCopies(maxCopies, random, rarity, rareCardCount);

            if (copies > 0) {
                decklist.add(copies + "x" + card);
                totalCards += copies;

                if (isRareCard(rarity)) {
                    rareCardCount++;
                }

                cardNumbers.remove(randomIndex);
                rarities.remove(randomIndex);
            }
        }

        return decklist;
    }

    private static int generateWeightedCopies(int maxCopies, Random random, String rarity, int superRareCount) {
        int[] weights;

        if (rarity.contains("Super-Rare") || rarity.contains("Secret-Rare")) {
            weights = new int[] {
                    30, 20, 10, 5
            };
        }
        else if (rarity.contains("Special") || rarity.contains("Alternate-Art") || rarity.contains("Manga-Art")) {
            weights = new int[] {
                    50, 35, 10, 5
            };
        }
        else {
            weights = new int[] {
                    50, 30, 15, 5
            };
        }

        if (superRareCount >= MAX_SUPER_RARE_CARDS
                && (rarity.contains("Super-Rare") || rarity.contains("Secret-Rare"))) {
            return 0;
        }

        int[] adjustedWeights = Arrays.copyOf(weights, maxCopies);
        int totalWeight = Arrays.stream(adjustedWeights).sum();
        int randomValue = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (int i = 0; i < adjustedWeights.length; i++) {
            cumulativeWeight += adjustedWeights[i];
            if (randomValue < cumulativeWeight) {
                return i + 1;
            }
        }

        return 1;
    }

    private static boolean isRareCard(String rarity) {
        return rarity.contains("Super-Rare") || rarity.contains("Secret-Rare") || rarity.contains("Special")
                || rarity.contains("Alternate-Art") || rarity.contains("Manga-Art");
    }

    private static void writeDecklistToFile(List<String> decklist, String outputPath) throws IOException {
        Files.write(Paths.get(outputPath), decklist);
    }

}