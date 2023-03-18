package com.agorro.subtitledownloader.utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Compress
{
    public static void decompressRAR(File fileCompressed, File fileDecompressed) throws IOException, RarException
    {
        try (Archive archive = new Archive(fileCompressed))
        {
            if (archive.isEncrypted()) throw new IOException("Archive rar is encrypted");
            FileHeader fileHeader = archive.nextFileHeader();
            if (null == fileHeader) throw new IOException("There is no file header in rar");
            if (fileHeader.isEncrypted()) throw new IOException("File " + fileHeader.getFileName() + " is encrypted");
            if (fileHeader.isDirectory()) throw new IOException("File " + fileHeader.getFileName() + " is directory");

            fileDecompressed.createNewFile();
            try (OutputStream stream = new FileOutputStream(fileDecompressed))
            {
                archive.extractFile(fileHeader, stream);
            }
        }
    }

    public static void decompressZIP(File fileCompressed, File fileDecompressed) throws IOException
    {
        try (InputStream is =  new FileInputStream(fileCompressed);
             ZipInputStream zis = new ZipInputStream(is))
        {
            ZipEntry zipEntry = zis.getNextEntry();
            if (null == zipEntry) throw new IOException("There is no zip entry in zip");
            if (zipEntry.isDirectory()) throw new IOException("File " + zipEntry.getName() + " is directory");

            fileDecompressed.createNewFile();
            try (FileOutputStream out = new FileOutputStream(fileDecompressed))
            {
                IO.copy(zis, out);
            }
        }
    }
}
