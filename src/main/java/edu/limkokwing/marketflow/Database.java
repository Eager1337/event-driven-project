package edu.limkokwing.marketflow;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** One short-lived JDBC connection per operation; configuration never contains embedded credentials. */
public final class Database {
    private final String url, user, password;
    public Database(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }
    public static Database configured() throws IOException {
        Properties p = new Properties();
        Path path = Path.of("config.properties");
        if (Files.exists(path)) try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { p.load(reader); }
        String url = setting("DB_URL", "db.url", p, "jdbc:mysql://127.0.0.1:3307/marketflow?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000");
        String user = setting("DB_USER", "db.user", p, "marketflow");
        String password = setting("DB_PASSWORD", "db.password", p, "");
        if (password.isBlank()) throw new IOException("Set DB_PASSWORD or db.password in config.properties. See README setup steps.");
        return new Database(url, user, password);
    }
    private static String setting(String env, String key, Properties p, String fallback) {
        String value = System.getenv(env); return value == null ? p.getProperty(key, fallback) : value;
    }
    public Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }
    public void ping() throws SQLException { try (Connection c = open(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM products")) { s.executeQuery().close(); } }
}