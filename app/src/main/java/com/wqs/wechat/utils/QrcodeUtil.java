package com.wqs.wechat.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class QrcodeUtil {
    public static Bitmap encode(String text,int qwidth,int qheight) {
        if (!text.equals("")) {
            try {
                BitMatrix result = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, qwidth, qheight);
                int width = result.getWidth();
                int height = result.getHeight();
                int[] pixels = new int[width * height];
                for (int y = 0; y < height; y++) {
                    int offset = y * width;
                    for (int x = 0; x < width; x++) {
                        pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                    }
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                return bitmap;
            } catch (Exception iae) {
                // Unsupported format
            }
        }
        return null;
    }
}
