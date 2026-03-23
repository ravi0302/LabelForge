package com.labelforge.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Generates a PNG byte array for an EAN-13 barcode using ZXing.
 * Used by the /barcode endpoint so the React frontend (or label printers)
 * can download a raster image.
 */
@Service
public class BarcodeImageService {

    private static final int DEFAULT_WIDTH  = 300;
    private static final int DEFAULT_HEIGHT = 120;

    /**
     * Renders the given EAN-13 string as a PNG image.
     *
     * @param ean    validated 13-digit EAN string
     * @param width  image width in pixels (default 300)
     * @param height image height in pixels (default 120)
     * @return PNG bytes
     */
    public byte[] generatePng(String ean, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.MARGIN, 10   // quiet zone around barcode
            );

            BitMatrix matrix = new MultiFormatWriter()
                .encode(ean, BarcodeFormat.EAN_13, width, height, hints);

            MatrixToImageConfig config = new MatrixToImageConfig(
                0xFF000000,   // bar color  – black
                0xFFFFFFFF    // background – white
            );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out, config);
            return out.toByteArray();

        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Failed to generate barcode image for EAN: " + ean, e);
        }
    }

    public byte[] generatePng(String ean) {
        return generatePng(ean, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
