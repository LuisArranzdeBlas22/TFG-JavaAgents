package com.luis.tfg.ipfilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Clase para gestionar listas de IPs de whitelist y blacklist.
 * Permite añadir, eliminar y persistir IPs en un archivo de configuración.
 */
public class AgentConfig {
    private Set<String> whitelist;
    private Set<String> blacklist;
    private String configFilePath;

    /**
     * Constructor que inicializa las listas y carga la configuración desde el archivo.
     *
     * @param configFilePath ruta del archivo de configuración.
     */
    public AgentConfig(String configFilePath) {
        this.configFilePath = configFilePath;
        whitelist = new HashSet<>();
        blacklist = new HashSet<>();
        createConfigFileIfNotExists();
        loadConfig();
    }

    /**
     * Crea el archivo de configuración si no existe y define secciones vacías.
     */
    private void createConfigFileIfNotExists() {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write("whitelist:\n");
                writer.write("blacklist:\n");
            } catch (IOException e) {
                System.err.println("Error creating config file: " + e.getMessage());
            }
        }
    }

    /**
     * Carga las IPs de las listas desde el archivo de configuración.
     */
    private void loadConfig() {
        whitelist.clear();
        blacklist.clear();
        try {
            List<String> lines = Files.readAllLines(Paths.get(configFilePath));
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("whitelist:") && line.length() > 10) {
                    whitelist.add(line.substring(10).trim());
                } else if (line.startsWith("blacklist:") && line.length() > 10) {
                    blacklist.add(line.substring(10).trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading config file: " + e.getMessage());
        }
    }

    /**
     * Añade una IP a la whitelist y guarda los cambios.
     *
     * @param ip IP a añadir a la whitelist.
     */
    public void addToWhitelist(String ip) {
        if (whitelist.add(ip)) {
            saveConfig();
        }
    }

    /**
     * Añade una IP a la blacklist y guarda los cambios.
     *
     * @param ip IP a añadir a la blacklist.
     */
    public void addToBlacklist(String ip) {
        if (blacklist.add(ip)) {
            saveConfig();
        }
    }

    /**
     * Elimina una IP de la whitelist y reescribe el archivo.
     *
     * @param ip IP a eliminar de la whitelist.
     */
    public void removeFromWhitelist(String ip) {
        if (whitelist.remove(ip)) {
            rewriteConfig();
        }
    }

    /**
     * Elimina una IP de la blacklist y reescribe el archivo.
     *
     * @param ip IP a eliminar de la blacklist.
     */
    public void removeFromBlacklist(String ip) {
        if (blacklist.remove(ip)) {
            rewriteConfig();
        }
    }

    /**
     * Reescribe el archivo de configuración, omitiendo secciones vacías.
     */
    private void rewriteConfig() {
        try (FileWriter writer = new FileWriter(configFilePath)) {
            for (String ip : whitelist) {
                writer.write("whitelist: " + ip + "\n");
            }
            for (String ip : blacklist) {
                writer.write("blacklist: " + ip + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error rewriting config file: " + e.getMessage());
        }
    }

    /**
     * Guarda las listas actualizadas en el archivo de configuración.
     */
    private void saveConfig() {
        try (FileWriter writer = new FileWriter(configFilePath)) {
            if (!whitelist.isEmpty()) {
                for (String ip : whitelist) {
                    writer.write("whitelist: " + ip + "\n");
                }
            }

            if (!blacklist.isEmpty()) {
                for (String ip : blacklist) {
                    writer.write("blacklist: " + ip + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving config file: " + e.getMessage());
        }
    }

    /**
     * Obtiene la whitelist actual.
     *
     * @return conjunto de IPs en la whitelist.
     */
    public Set<String> getWhitelist() {
        return whitelist;
    }

    /**
     * Obtiene la blacklist actual.
     *
     * @return conjunto de IPs en la blacklist.
     */
    public Set<String> getBlacklist() {
        return blacklist;
    }
}
