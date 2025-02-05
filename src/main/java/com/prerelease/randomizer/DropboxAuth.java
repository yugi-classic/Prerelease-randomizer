/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.prerelease.randomizer;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class DropboxAuth {
    private static final String ACCESS_TOKEN = "sl.u.AFhRzg9uwAimEhIdJVidLTNJAWfiQ_Tbt5KfAv0L2QZvu1fjQekSv7YeRfxkM_2MbhO03qz4a0_HKpWcIgXZAic-6WF_2oXCayrGscwtSlLfImD8wA1VXkQ3_MXOvGfD-qC_rF5ZzCDhFmylejz5-XuWttcdUzm8kudEig0IanomhQx0YJDQ5TtmUnpgaLyRhBp6vNKOHnMGjQftlHRfxfhk6esNLBjHqQgyaJ1Iy9pM6uUJblliwTPuaL9zTx6P94yhBjZm1QnwwSto_-gvd_xS0mJdydlJqrtvEh1AGMLiqMdifanO67doOCk4oje91AB4GFSIWI1A3LMRSZomADDrS9zsBKfhxyBsjXE2n5IyCKTF3zHSvNoa9EW1ETvfROHq9zrjUQ8n3tpeLOHzvXIXYpZ_7EH61Vrlp74Nn5I7fNgyhMy1-dvqGZFKBdgSSR575Jtc9yVQ19uNxROX3RJCsvYCgr8DcU4hVyMt-t6LM3PnwvJxy7vb0PAXbTHy5wtYXrxZHnwgJCwiDWmYGWx_oTGzprknZKCChwPIO4GvdI34i-zm8mBWbh3s6G0UHCn_gR8qfVBcJv7eh2qE04CJrolcqt4lao3J51NOvj5qN020UyQNYlHZsP07JPcOyHM0xX-YUD7GyLwcT3N9bFeTQnLWe9pxDIWPajgU79mvO-qcRvr_KgHFeAO1sWODsc-VAnsbCaQV8Ye9tQ5FLQN5Mdo-3CxU04phi5pj6SaRDDs521j0wuxkc7GaBlw5eO2pAjP7Oo3dmK3yYJ_qwHinVxSPygS9soy6RyCQrhdvZCxkkFlYfOkb_D0jkt5RkwA3Vs3SHzwg0SmihIesLoRULtmtsVVEM890oZ-NCh_B4OOP1meT91Yb0w_BJiqqLawBhLM2ceK1WzdGgnT3bfxsTOBP8Q9P8yP3QViT3XCC4DZkECvxYLYphEO2hhVMKBzFDX9_CadSJYT_YNdzs-9gmcfFIqxPlOr0QbOXTZtId-motRjSNXDSzaSGhAlpwqPq5DK5yuzeFPpzWj3OTC4IUJf8yosmO9hW7muVZZSQ0VuZ5H7fGAy4LWnaquIKfjTExSpiGyqR5zCHK9MPFEyhJgpWX2i1hZUJrUobdlLJ2h_wJg5PAfOrQifszFeQySnqjSF_mwzANNeJjbxq76sqXsRKbTTG7ZoqxX6ypV-Fcce7JU7r6RQPayjghgKJktfbA0xCBXihhIN1Ut-twHIJhC9BverLgQFoius6RCSSE9MVZSsBqphRR1xolaSICRDxz5aHJG7-MOloTHDHHgzz";
    private static final Map<String, ImageIcon> imageCache = new HashMap();

    public static ImageIcon auth(String selectedSet, String cardNumber) throws IOException {
        String cacheKey = selectedSet + "/" + cardNumber + ".png";
        if (imageCache.containsKey(cacheKey)) {
            return (ImageIcon) imageCache.get(cacheKey);
        }
        else {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("OnePiece_DB").build();
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
            String filePath = "/" + selectedSet + "/" + cardNumber + ".png";

            try {
                byte[] fileData = client.files().download(filePath).getInputStream().readAllBytes();
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileData));
                if (image != null) {
                    Image scaledImage = image.getScaledInstance(150, 210, 4);
                    ImageIcon icon = new ImageIcon(scaledImage);
                    imageCache.put(cacheKey, icon);
                    return icon;
                }

                System.out.println("Das Bild konnte nicht dekodiert werden.");
            }
            catch (Exception var10) {
                System.out.println("Fehler beim Abrufen des Bildes von Dropbox: " + var10.getMessage());
                var10.printStackTrace();
            }

            return null;
        }
    }
}