package org.example;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

public class Main {
    private static final int QR_CODE_SIZE = 500; // Размер QR-кода
    private static final String PNG_DIRECTORY = "generated_images";
    private static final Random random = new Random();
    private static JLabel qrCodeLabel;

    public static void main(String[] args) {
        // Создаем директорию для PNG-файлов, если она не существует
        createPngDirectory();

        // Создаем и настраиваем GUI
        JFrame frame = new JFrame("QR Code Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 600); // Увеличен размер окна
        frame.setLayout(new BorderLayout());

        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(JLabel.CENTER);

        // Добавляем обработчик нажатия мыши для генерации нового QR-кода
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    generateAndDisplayQRCode();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Добавляем обработчик нажатия мыши для самого QR-кода
        qrCodeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    generateAndDisplayQRCode();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.add(qrCodeLabel, BorderLayout.CENTER);
        frame.setVisible(true);

        // Генерируем первый QR-код при запуске
        try {
            generateAndDisplayQRCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createPngDirectory() {
        File directory = new File(PNG_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private static void generateAndDisplayQRCode() throws WriterException, IOException {
        // Генерируем уникальный идентификатор для QR-кода
        String uniqueId = generateUniqueId();

        // Получаем байтовое представление QR-кода
        byte[] qrCodeBytes = generateQRCodeBytes(uniqueId);

        // Преобразуем байты обратно в изображение для отображения
        BufferedImage bufferedImage = ImageIO.read(new java.io.ByteArrayInputStream(qrCodeBytes));
        ImageIcon qrCodeIcon = new ImageIcon(bufferedImage);

        // Обновляем GUI в потоке EDT
        SwingUtilities.invokeLater(() -> {
            qrCodeLabel.setIcon(qrCodeIcon);
        });

        // Сохраняем изображение в файл
        savePngFile(bufferedImage, uniqueId);
    }

    // Новая функция для генерации QR-кода в виде массива байтов
    private static byte[] generateQRCodeBytes(String content) throws WriterException, IOException {
        BitMatrix bitMatrix = generateQRCodeBitMatrix(content);
        return convertQRCodeToBytes(bitMatrix);
    }

    private static String generateUniqueId() {
        // Генерируем уникальный идентификатор на основе текущего времени и случайного числа
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomSuffix = String.format("%04d", random.nextInt(10000));
        return "qrcode_" + timestamp + "_" + randomSuffix;
    }

    private static void savePngFile(BufferedImage image, String uniqueId) throws IOException {
        String pngFileName = PNG_DIRECTORY + "/" + uniqueId + ".png";
        Path pngPath = Paths.get(pngFileName);
        ImageIO.write(image, "PNG", pngPath.toFile());
    }

    private static byte[] convertQRCodeToBytes(BitMatrix bitMatrix) throws IOException {
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        return baos.toByteArray();
    }

    private static BitMatrix generateQRCodeBitMatrix(String content) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);
    }
}
