/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.prerelease.randomizer;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class S3Auth {
    private static final String BUCKET_NAME = "onepiece-db";
    private static final Map<String, ImageIcon> imageCache = new HashMap();
    private static final String ACCESSKEY = "";
    private static final String SECRETKEY = "";

    public static ImageIcon auth(String selectedSet, String cardNumber) throws IOException {
        String cacheKey = selectedSet + "/" + cardNumber + ".png";
        if (imageCache.containsKey(cacheKey)) {
            return (ImageIcon) imageCache.get(cacheKey);
        }
        else {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(ACCESSKEY, SECRETKEY);
            AmazonS3 s3Client = (AmazonS3) ((AmazonS3ClientBuilder) ((AmazonS3ClientBuilder) AmazonS3ClientBuilder
                    .standard().withRegion("eu-north-1"))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))).build();
            String imagePath = selectedSet + "/" + cardNumber + ".png";

            try {
                S3Object s3Object = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, imagePath));
                S3ObjectInputStream inputStream = s3Object.getObjectContent();
                BufferedImage image = ImageIO.read(inputStream);
                if (image != null) {
                    Image scaledImage = image.getScaledInstance(150, 210, 4);
                    ImageIcon icon = new ImageIcon(scaledImage);
                    imageCache.put(cacheKey, icon);
                    return icon;
                }

                System.out.println("Das Bild konnte nicht dekodiert werden.");
            }
            catch (Exception var11) {
                System.out.println("Fehler beim Abrufen des Bildes von S3: " + var11.getMessage());
                var11.printStackTrace();
            }

            return null;
        }
    }
}