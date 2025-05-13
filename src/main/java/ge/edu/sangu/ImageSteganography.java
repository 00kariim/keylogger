package ge.edu.sangu;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ImageSteganography {

    public static void embedMessage(String message, String inputImagePath, String outputImagePath) throws Exception {
        BufferedImage image = loadImage(inputImagePath);
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        int msgLength = msgBytes.length;

        if (!canEmbedMessage(msgLength, image)) {
            throw new IllegalArgumentException("Message trop long pour cette image.");
        }

        embedLength(msgLength, image);
        embedData(msgBytes, image);
        saveImage(image, outputImagePath);
    }

    public static String extractMessage(String imagePath) throws Exception {
        BufferedImage image = loadImage(imagePath);
        int msgLength = extractLength(image);
        return extractData(image, msgLength);
    }

    private static BufferedImage loadImage(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) throw new IllegalArgumentException("Image introuvable : " + path);
        return ImageIO.read(file);
    }

    private static void saveImage(BufferedImage image, String outputPath) throws Exception {
        File outputFile = new File(outputPath);
        if (!ImageIO.write(image, "png", outputFile)) {
            throw new IllegalArgumentException("Erreur d’enregistrement de l’image.");
        }
    }

    private static boolean canEmbedMessage(int msgLength, BufferedImage image) {
        int capacity = image.getWidth() * image.getHeight() - 32;
        return msgLength * 8 <= capacity;
    }

    private static void embedLength(int length, BufferedImage image) {
        for (int i = 0; i < 32; i++) {
            int bit = (length >> i) & 1;
            int x = i % image.getWidth();
            int y = i / image.getWidth();
            setLSB(image, x, y, bit);
        }
    }

    private static void embedData(byte[] data, BufferedImage image) {
        int offset = 32;
        int totalBits = data.length * 8;
        int width = image.getWidth();
        int height = image.getHeight();
        int capacity = width * height;

        Random rand = new Random(1337);
        List<Integer> indices = new ArrayList<>();
        for (int i = offset; i < capacity; i++) indices.add(i);
        Collections.shuffle(indices, rand);

        for (int i = 0; i < totalBits; i++) {
            int bit = (data[i / 8] >> (i % 8)) & 1;
            int pixelIndex = indices.get(i);
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            setLSB(image, x, y, bit);
        }
    }

    private static int extractLength(BufferedImage image) {
        int length = 0;
        for (int i = 0; i < 32; i++) {
            int x = i % image.getWidth();
            int y = i / image.getWidth();
            int bit = getLSB(image, x, y);
            length |= (bit << i);
        }
        return length;
    }

    private static String extractData(BufferedImage image, int msgLength) {
        int totalBits = msgLength * 8;
        int offset = 32;
        int width = image.getWidth();
        int height = image.getHeight();
        int capacity = width * height;

        Random rand = new Random(1337);
        List<Integer> indices = new ArrayList<>();
        for (int i = offset; i < capacity; i++) indices.add(i);
        Collections.shuffle(indices, rand);

        byte[] data = new byte[msgLength];
        for (int i = 0; i < totalBits; i++) {
            int pixelIndex = indices.get(i);
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            int lsb = getLSB(image, x, y);
            data[i / 8] |= (lsb << (i % 8));
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    private static void setLSB(BufferedImage image, int x, int y, int bit) {
        int rgb = image.getRGB(x, y);
        image.setRGB(x, y, (rgb & 0xFFFFFFFE) | bit);
    }

    private static int getLSB(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        return rgb & 1;
    }
}
