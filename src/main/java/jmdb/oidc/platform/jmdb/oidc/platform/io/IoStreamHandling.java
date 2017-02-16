package jmdb.oidc.platform.jmdb.oidc.platform.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class IoStreamHandling {
    public IoStreamHandling() {
    }

    public static void closeQuietly(OutputStream out) {
        if(out != null) {
            try {
                out.close();
            } catch (IOException var2) {
                throw new RuntimeException("Could not close stream (See Cause)", var2);
            }
        }
    }

    public static void closeQuietly(Writer writer) {
        if(writer != null) {
            try {
                writer.close();
            } catch (IOException var2) {
                throw new RuntimeException("Could not close writer (See Cause)", var2);
            }
        }
    }

    public static void closeQuietly(Reader reader) {
        if(reader != null) {
            try {
                reader.close();
            } catch (IOException var2) {
                throw new RuntimeException("Could not close stream (See Cause)", var2);
            }
        }
    }

    public static void closeQuietly(InputStream in) {
        if(in != null) {
            try {
                in.close();
            } catch (IOException var2) {
                throw new RuntimeException("Could not close stream (See Cause)", var2);
            }
        }
    }

    public static InputStream toInputStream(String input, String charset) {
        try {
            return new ByteArrayInputStream(input.getBytes(charset));
        } catch (UnsupportedEncodingException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static String readFileAsString(File file, String charsetName) {
        try {
            return readFully(new FileInputStream(file), charsetName);
        } catch (FileNotFoundException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static String readFully(InputStream inputStream, String charsetName) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charsetName));
            char[] e = new char[512];
            boolean bytesRead = true;

            int bytesRead1;
            while((bytesRead1 = bufferedReader.read(e)) > 0) {
                stringBuilder.append(e, 0, bytesRead1);
            }
        } catch (IOException var9) {
            throw new RuntimeException(var9);
        } finally {
            closeQuietly((Reader)bufferedReader);
        }

        return stringBuilder.toString();
    }

    public static void copyStream(InputStream in, OutputStream out) {
        byte[] buffy = new byte[1024];

        try {
            boolean e = false;

            int e1;
            while((e1 = in.read(buffy)) != -1) {
                out.write(buffy, 0, e1);
            }

        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }

    public static void writeToFile(String content, File f) {
        writeToFile(content, f, "UTF-8");
    }

    public static void writeToFile(String content, File f, String charsetName) {
        BufferedWriter out = null;

        try {
            if(f.isDirectory()) {
                throw new RuntimeException(String.format("File [%s] is a directory! Cannot write here", new Object[]{f.getAbsolutePath()}));
            }

            if(!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), charsetName));
            out.write(content);
        } catch (IOException var8) {
            throw new RuntimeException(var8);
        } finally {
            closeQuietly((Writer)out);
        }

    }
}
