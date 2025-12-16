package com.example.proto5.qr_scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    public static void unzip(File zip, File target)
            throws Exception {

        ZipInputStream zis =
                new ZipInputStream(
                        new FileInputStream(zip));

        ZipEntry entry;
        byte[] buffer = new byte[4096];

        while ((entry = zis.getNextEntry()) != null) {
            File out = new File(target, entry.getName());

            if (entry.isDirectory()) {
                out.mkdirs();
            } else {
                out.getParentFile().mkdirs();
                FileOutputStream fos =
                        new FileOutputStream(out);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zis.closeEntry();
        }
        zis.close();
    }
}
