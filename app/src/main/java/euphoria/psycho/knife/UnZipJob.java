package euphoria.psycho.knife;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import euphoria.psycho.common.Log;
import euphoria.psycho.common.StreamUtil;
import euphoria.psycho.common.StringUtils;

public class UnZipJob {

    private UnZipListener mUnZipListener;

    public UnZipJob(UnZipListener listener) {
        mUnZipListener = listener;
    }


    private void unTarGz(File srcFile, File outputDirectory) throws Exception {
        if (outputDirectory == null) {
            outputDirectory = new File(srcFile.getParentFile(), StringUtils.substringBeforeLast(srcFile.getName(), ".tar.gz"));
            if (!outputDirectory.isDirectory())
                outputDirectory.mkdir();
        }
        byte[] data = readAllBytes(srcFile);


        ByteArrayInputStream bai = new ByteArrayInputStream(data);
        GzipCompressorInputStream gci = new GzipCompressorInputStream(bai);
        TarArchiveInputStream tai = new TarArchiveInputStream(gci);

        try {
            byte[] buffer = new byte[StreamUtil.DEFAULT_BUFFER_SIZE];
            TarArchiveEntry entry = tai.getNextTarEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    File dir = new File(outputDirectory, entry.getName());
                    dir.mkdirs();
                } else {

                    File targetFile = new File(outputDirectory, entry.getName());
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    while (tai.read(buffer) != -1) {
                        outputStream.write(buffer);
                    }
                    outputStream.close();


                }


                // readTarRecursively(entry);
//                readByte = tai.read(buffer);
//                while (readByte != -1) {
//
//
//                }
                entry = tai.getNextTarEntry();
            }
        } finally {
            tai.close();
            gci.close();
            bai.close();
        }

    }

    public void unzip(String srcPath) {
        if (srcPath.endsWith(".tar.gz")) {
            try {
                unTarGz(new File(srcPath), null);
            } catch (Exception e) {
                if (mUnZipListener != null) {
                    mUnZipListener.onError(e);
                }
            }
        } else if (srcPath.endsWith(".zip") || srcPath.endsWith(".epub")) {
            File srcFile = new File(srcPath);
            File outdir = new File(srcFile.getParentFile(), StringUtils.substringAfterLast(srcFile.getName(), "."));
            outdir.mkdirs();
            extract(new File(srcPath), outdir);
        }
    }

    private static String dirpart(String name) {
        int s = name.lastIndexOf(File.separatorChar);
        return s == -1 ? null : name.substring(0, s);
    }

    public static void extract(File zipfile, File outdir) {
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile));
            ZipEntry entry;
            String name, dir;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                if (entry.isDirectory()) {
                    mkdirs(outdir, name);
                    continue;
                }
                /* this part is necessary because file entry can come before
                 * directory entry where is file located
                 * i.e.:
                 *   /foo/foo.txt
                 *   /foo/
                 */
                dir = dirpart(name);
                if (dir != null)
                    mkdirs(outdir, dir);

                extractFile(zin, outdir, name);
            }
            zin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractFile(ZipInputStream in, File outdir, String name) throws IOException {
        byte[] buffer = new byte[StreamUtil.DEFAULT_BUFFER_SIZE];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)));
        int count = -1;
        while ((count = in.read(buffer)) != -1)
            out.write(buffer, 0, count);
        out.close();
    }

    private static void mkdirs(File outdir, String path) {
        File d = new File(outdir, path);
        if (!d.exists())
            d.mkdirs();
    }

    public static byte[] readAllBytes(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] result = null;
        try {
            byte[] fetchByte = new byte[1024];
            int readByte = fis.read(fetchByte);
            while (readByte != -1) {
                baos.write(fetchByte, 0, readByte);
                readByte = fis.read(fetchByte);
            }
            result = baos.toByteArray();
        } catch (Exception e) {
        } finally {
            baos.close();
            fis.close();
        }
        return result;
    }


    public interface UnZipListener {
        void onError(Exception exception);
    }
}
