/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.prerelease.randomizer;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PrereleaseRandomizer {
    private static final int DRAFT_PICKS = 6;

    public List<List<String>> randomize(String setpath) {
        try {
            Map<String, String> cardData = extractCardData(setpath);
            return generateCardPools(cardData, new Random());
        }
        catch (IOException var3) {
            System.err.println("Ein Fehler ist aufgetreten: " + var3.getMessage());
            return Collections.emptyList();
        }
    }

    private static Map<String, String> extractCardData(String pathToJson) throws IOException {
        Map<String, String> cardData = new HashMap();
        InputStream inputStream = loadResourceAsStream(pathToJson);
        if (inputStream == null) {
            throw new IOException("Datei konnte nicht geladen werden: " + pathToJson);
        }
        else {
            byte[] jsonBytes = inputStream.readAllBytes();
            String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
            Any rootNode = JsonIterator.deserialize(jsonString);
            Iterator var6 = rootNode.iterator();

            while (var6.hasNext()) {
                Any card = (Any) var6.next();
                String cardNumber = card.get("Card-Number").toString();
                String rarity = card.get("Rarity").toString();
                if (cardNumber != null && rarity != null) {
                    cardData.put(cardNumber, rarity);
                }
            }

            return cardData;
        }
    }

    private static InputStream loadResourceAsStream(String resourcePath) {
        InputStream inputStream = PrereleaseRandomizer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            try {
                String fullPath = "src/main/resources/" + resourcePath;
                inputStream = new FileInputStream(fullPath);
            }
            catch (FileNotFoundException var3) {
                var3.printStackTrace();
            }
        }

        return (InputStream) inputStream;
    }

    private static List<List<String>> generateCardPools(Map<String, String> cardData, Random random) {
        List<List<String>> pools = new ArrayList();
        List<String> commons = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.contains("Common");
        });
        List<String> rares = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.matches("(^|_)Rare($|_)");
        });
        List<String> uncommons = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.contains("Uncommon");
        });
        List<String> superRares = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.contains("Super-Rare");
        });
        List<String> secretRares = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.contains("Secret-Rare");
        });
        List<String> alternateArt = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.contains("Alternate-Art");
        });
        List<String> specialCard = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.contains("Special-Card");
        });
        List<String> leader = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.contains("Leader");
        });
        List<String> mangaRare = filterCardsByRarity(cardData, (rarity) -> {
            return rarity.contains("Manga-Art");
        });

        for (int i = 0; i < 6; ++i) {
            List<String> pool = new ArrayList();
            pool.addAll(pickRandomCards(commons, 8, random, cardData, "Common"));
            if (random.nextDouble() > 0.3D) {
                if (random.nextDouble() > 0.3D) {
                    pool.addAll(pickRandomCards(rares, 2, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
                else {
                    pool.addAll(pickRandomCards(leader, 1, random, cardData, "Leader"));
                    pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                    pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
                }
            }
            else if (random.nextDouble() <= 0.2D) {
                pool.addAll(pickRandomCards(alternateArt, 1, random, cardData, "Alternate-Art"));
                pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
            }
            else if (random.nextDouble() <= 0.1D) {
                pool.addAll(pickRandomCards(secretRares, 1, random, cardData, "Secret-Rare"));
                pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
            }
            else if (random.nextDouble() <= 0.05D) {
                pool.addAll(pickRandomCards(specialCard, 1, random, cardData, "Special-Card"));
                pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
            }
            else if (random.nextDouble() <= 0.01D) {
                pool.addAll(pickRandomCards(mangaRare, 1, random, cardData, "Manga-Art"));
                pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
            }
            else {
                pool.addAll(pickRandomCards(superRares, 1, random, cardData, "Super-Rare"));
                pool.addAll(pickRandomCards(rares, 1, random, cardData, "Rare"));
                pool.addAll(pickRandomCards(uncommons, 2, random, cardData, "Uncommon"));
            }

            pools.add(pool);
        }

        return pools;
    }

    private static List<String> pickRandomCards(List<String> cardList, int count, Random random,
            Map<String, String> cardData, String selectedRarity) {
        List<String> pickedCards = new ArrayList();

        for (int i = 0; i < count && !cardList.isEmpty(); ++i) {
            int index = random.nextInt(cardList.size());
            String cardNumber = (String) cardList.get(index);
            String rarity = (String) cardData.get(cardNumber);
            String[] parts = rarity.split("_");
            String finalRarity = getFinalRarity(parts, selectedRarity);
            if (finalRarity != null) {
                pickedCards.add(cardNumber + "_" + finalRarity);
            }
        }

        return pickedCards;
    }

    private static List<String> filterCardsByRarity(Map<String, String> cardData, Predicate<String> rarityFilter) {
        return (List) cardData.entrySet().stream().filter((entry) -> {
            return rarityFilter.test((String) entry.getValue());
        }).map(Entry::getKey).collect(Collectors.toList());
    }

    private static String getFinalRarity(String[] parts, String selectedRarity) {
        byte var3 = -1;
        switch (selectedRarity.hashCode()) {
        case -2022887127:
            if (selectedRarity.equals("Leader")) {
                var3 = 8;
            }
            break;
        case -1147883548:
            if (selectedRarity.equals("Special-Card")) {
                var3 = 6;
            }
            break;
        case -414914145:
            if (selectedRarity.equals("Secret-Rare")) {
                var3 = 4;
            }
            break;
        case -403667484:
            if (selectedRarity.equals("Uncommon")) {
                var3 = 2;
            }
            break;
        case 2539714:
            if (selectedRarity.equals("Rare")) {
                var3 = 1;
            }
            break;
        case 228115444:
            if (selectedRarity.equals("Super-Rare")) {
                var3 = 3;
            }
            break;
        case 652155472:
            if (selectedRarity.equals("Alternate-Art")) {
                var3 = 5;
            }
            break;
        case 1694785674:
            if (selectedRarity.equals("Manga-Art")) {
                var3 = 7;
            }
            break;
        case 2024019467:
            if (selectedRarity.equals("Common")) {
                var3 = 0;
            }
        }

        switch (var3) {
        case 0:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Common");
            }).findFirst().orElse("Common");
        case 1:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Rare");
            }).findFirst().orElse("Rare");
        case 2:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Uncommon");
            }).findFirst().orElse("Uncommon");
        case 3:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Super-Rare");
            }).findFirst().orElse("Super-Rare");
        case 4:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Secret-Rare");
            }).findFirst().orElse("Secret-Rare");
        case 5:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Alternate-Art");
            }).findFirst().orElse("Alternate-Art");
        case 6:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Special-Card");
            }).findFirst().orElse("Special-Card");
        case 7:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Manga-Art");
            }).findFirst().orElse("Manga-Art");
        case 8:
            return (String) Arrays.stream(parts).filter((part) -> {
                return part.equals("Leader");
            }).findFirst().orElse("Leader");
        default:
            return "Unknown";
        }
    }
}
