package com.smartpark.util;

/**
 * Contract for entities serializable to and from standard comma-separated text.
 */
public interface CsvConvertible {
    String toCsvRow();
}
