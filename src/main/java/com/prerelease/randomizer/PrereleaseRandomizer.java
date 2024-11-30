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
        List<String> superRares = filterCardsByRarity(cardData,
                rarity -> (rarity.contains("Super-Rare") || rarity.contains("Secret-Rare"))
                        || rarity.contains("Alternate-Art") && !rarity.matches("(^|_)Rare($|_)"));

        List<String> specialRare = filterCardsByRarity(cardData, rarity -> (rarity.contains("Special-Card")));

        List<String> mangaRare = filterCardsByRarity(cardData, rarity -> (rarity.contains("Manga-Art")));

        for (int i = 0; i < DRAFT_PICKS; i++) {
            List<String> pool = new ArrayList<>();
            pool.addAll(pickRandomCards(commons, 8, random, cardData));

            if (random.nextDouble() > 0.3) {
                pool.addAll(pickRandomCards(rares, 2, random, cardData));
                pool.addAll(pickRandomCards(uncommons, 2, random, cardData));
            }
            else {

                if (random.nextDouble() <= 0.05) {
                    pool.addAll(pickRandomCards(specialRare, 1, random, cardData));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData));
                }
                else if (random.nextDouble() <= 0.01) {
                    pool.addAll(pickRandomCards(mangaRare, 1, random, cardData));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData));
                }
                else {
                    pool.addAll(pickRandomCards(superRares, 1, random, cardData));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData));
                }
            }

            pools.add(pool);
            System.out.println(pool.size());
        }

        return pools;
    }

    private static List<String> pickRandomCards(List<String> cardList, int count, Random random,
            Map<String, String> cardData) {
        List<String> pickedCards = new ArrayList<>();
        for (int i = 0; i < count && !cardList.isEmpty(); i++) {
            int index = random.nextInt(cardList.size());
            String cardNumber = cardList.get(index);
            String rarity = cardData.get(cardNumber);
            String[] parts = rarity.split("/");

            if (parts.length == 1) {
                rarity = parts[0];
            }
            else if (parts.length == 2) {
                rarity = parts[random.nextInt(2)];
            }
            else if (parts.length == 3) {
                rarity = parts[random.nextInt(3)];
            }
            else if (parts.length == 4) {
                rarity = parts[random.nextInt(4)];
            }
            pickedCards.add(cardNumber + "_" + rarity);
        }
        return pickedCards;
    }

    private static List<String> filterCardsByRarity(Map<String, String> cardData, Predicate<String> rarityFilter) {
        return cardData.entrySet().stream().filter(entry -> rarityFilter.test(entry.getValue())).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}