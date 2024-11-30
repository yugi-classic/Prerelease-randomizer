/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.prerelease.randomizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PrereleaseRandomizer {

    private static final String PATH_TO_JSON = "Data\\json\\OnePiece-OP09.json";
    private static final int DRAFT_PICKS = 6;

    public List<List<String>> randomize() {
        try {
            Map<String, String> cardData = extractCardData(PATH_TO_JSON);
            return generateCardPools(cardData, new Random());
        }
        catch (IOException e) {
            System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
            return Collections.emptyList();
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

    private static List<List<String>> generateCardPools(Map<String, String> cardData, Random random) {
        List<List<String>> pools = new ArrayList<>();
        List<String> commons = filterCardsByRarity(cardData, rarity -> rarity.contains("Common"));
        List<String> rares = filterCardsByRarity(cardData, rarity -> rarity.matches("(^|_)Rare($|_)"));
        List<String> uncommons = filterCardsByRarity(cardData, rarity -> rarity.contains("Uncommon"));

        List<String> superRares = filterCardsByRarity(cardData, rarity -> (rarity.contains("Super-Rare")));
        List<String> secretRares = filterCardsByRarity(cardData, rarity -> (rarity.contains("Secret-Rare")));
        List<String> alternateArt = filterCardsByRarity(cardData, rarity -> (rarity.contains("Alternate-Art")));
        List<String> specialCard = filterCardsByRarity(cardData, rarity -> (rarity.contains("Special-Card")));
        List<String> leader = filterCardsByRarity(cardData, rarity -> (rarity.contains("Leader")));

        List<String> mangaRare = filterCardsByRarity(cardData, rarity -> (rarity.contains("Manga-Art")));

        for (int i = 0; i < DRAFT_PICKS; i++) {
            List<String> pool = new ArrayList<>();
            pool.addAll(pickRandomCards(commons, 8, random, cardData, "Common"));

            if (random.nextDouble() > 0.3) {
                if (random.nextDouble() > 0.3) {
                    pool.addAll(pickRandomCards(rares, 2, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
                else {
                    pool.addAll(pickRandomCards(leader, 1, random, cardData, "Leader"));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
            }
            else {
                if (random.nextDouble() <= 0.2) {
                    pool.addAll(pickRandomCards(alternateArt, 1, random, cardData, "Alternate-Art"));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
                else if (random.nextDouble() <= 0.1) {
                    pool.addAll(pickRandomCards(secretRares, 1, random, cardData, "Secret-Rare"));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
                else if (random.nextDouble() <= 0.05) {
                    pool.addAll(pickRandomCards(specialCard, 1, random, cardData, "Special-Card"));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
                else if (random.nextDouble() <= 0.01) {
                    pool.addAll(pickRandomCards(mangaRare, 1, random, cardData, "Manga-Art"));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
                else {
                    pool.addAll(pickRandomCards(superRares, 1, random, cardData, "Super-Rare"));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
            }

            pools.add(pool);
            System.out.println(pool.size());
        }

        return pools;
    }

    private static List<String> pickRandomCards(List<String> cardList, int count, Random random,
            Map<String, String> cardData, String selectedRarity) {
        List<String> pickedCards = new ArrayList<>();

        for (int i = 0; i < count && !cardList.isEmpty(); i++) {
            int index = random.nextInt(cardList.size());
            String cardNumber = cardList.get(index);
            String rarity = cardData.get(cardNumber);
            String[] parts = rarity.split("_");

            String finalRarity = getFinalRarity(parts, selectedRarity);

            if (finalRarity != null) {
                pickedCards.add(cardNumber + "_" + finalRarity);
            }

        }

        return pickedCards;
    }

    private static List<String> filterCardsByRarity(Map<String, String> cardData, Predicate<String> rarityFilter) {
        return cardData.entrySet().stream().filter(entry -> rarityFilter.test(entry.getValue())).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static String getFinalRarity(String[] parts, String selectedRarity) {
        switch (selectedRarity) {
        case "Common":
            return Arrays.stream(parts).filter(part -> part.equals("Common")).findFirst().orElse("Common");
        case "Rare":
            return Arrays.stream(parts).filter(part -> part.equals("Rare")).findFirst().orElse("Rare");
        case "Uncommon":
            return Arrays.stream(parts).filter(part -> part.equals("Uncommon")).findFirst().orElse("Uncommon");
        case "Super-Rare":
            return Arrays.stream(parts).filter(part -> part.equals("Super-Rare")).findFirst().orElse("Super-Rare");
        case "Secret-Rare":
            return Arrays.stream(parts).filter(part -> part.equals("Secret-Rare")).findFirst().orElse("Secret-Rare");
        case "Alternate-Art":
            return Arrays.stream(parts).filter(part -> part.equals("Alternate-Art")).findFirst()
                    .orElse("Alternate-Art");
        case "Special-Card":
            return Arrays.stream(parts).filter(part -> part.equals("Special-Card")).findFirst().orElse("Special-Card");
        case "Manga-Art":
            return Arrays.stream(parts).filter(part -> part.equals("Manga-Art")).findFirst().orElse("Manga-Art");
        case "Leader":
            return Arrays.stream(parts).filter(part -> part.equals("Leader")).findFirst().orElse("Leader");
        default:
            return "Unknown"; // Standardwert für nicht erkannte Raritäten
        }
    }

}