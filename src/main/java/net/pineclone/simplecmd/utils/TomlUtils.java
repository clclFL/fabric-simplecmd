package net.pineclone.simplecmd.utils;

import com.moandjiezana.toml.Toml;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TomlUtils {

    public static Toml modToml = new Toml();

    public static void ensureToml() throws IOException {
        File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "simplecmd.toml");
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (success) {
                try (InputStream is = TomlUtils.class.getClassLoader().getResourceAsStream("simplecmd.toml");
                     InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                     BufferedReader br = new BufferedReader(isr);
                     FileOutputStream fos = new FileOutputStream(file);
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                     BufferedWriter bw = new BufferedWriter(osw);
                ) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        bw.write(line);
                        bw.newLine();
                        bw.flush();
                    }
                }
            }
        }
        modToml.read(file);
    }
}
