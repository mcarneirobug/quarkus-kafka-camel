package com.poc.utils;

import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {
    private static final Logger LOG = Logger.getLogger(CsvUtils.class);

    /**
     * Extrai uma string de um array de forma segura
     */
    public static String getStringField(String[] parts, int index, String defaultValue) {
        if (parts != null && index >= 0 && index < parts.length && parts[index] != null) {
            String value = parts[index].trim();
            return value.isEmpty() ? defaultValue : value;
        }
        return defaultValue;
    }

    /**
     * Extrai um BigDecimal de um array de forma segura
     */
    public static BigDecimal getBigDecimalField(String[] parts, int index, BigDecimal defaultValue) {
        if (parts != null && index >= 0 && index < parts.length && parts[index] != null) {
            String value = parts[index].trim();
            if (!value.isEmpty()) {
                try {
                    return new BigDecimal(value);
                } catch (NumberFormatException e) {
                    LOG.warnf("Erro ao converter valor para BigDecimal: %s", e.getMessage());
                }
            }
        }
        return defaultValue;
    }

    /**
     * Divide uma string CSV em um array de linhas, lidando com diferentes quebras de linha
     */
    public static List<String> splitCsvLines(String content) {
        List<String> lines = new ArrayList<>();
        if (content != null && !content.trim().isEmpty()) {
            // Normaliza quebras de linha
            String normalized = content.replace("\r\n", "\n").replace("\r", "\n");
            String[] linesArray = normalized.split("\n");

            for (String line : linesArray) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        }
        return lines;
    }

    /**
     * Divide uma linha CSV em campos usando um separador específico
     */
    public static String[] splitCsvFields(String line, String separator) {
        if (line != null && !line.trim().isEmpty()) {
            return line.split(separator);
        }
        return new String[0];
    }
}
