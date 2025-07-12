package com.authbase.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class for generating BCrypt encoded passwords.
 * This is useful for creating test data and initial passwords.
 */
public class PasswordGenerator {

  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  /**
   * Generate a BCrypt encoded password from plain text.
   * 
   * @param plainPassword the plain text password
   * @return the BCrypt encoded password
   */
  public static String encodePassword(String plainPassword) {
    return encoder.encode(plainPassword);
  }

  /**
   * Check if a plain text password matches a BCrypt encoded password.
   * 
   * @param plainPassword   the plain text password
   * @param encodedPassword the BCrypt encoded password
   * @return true if passwords match, false otherwise
   */
  public static boolean matches(String plainPassword, String encodedPassword) {
    return encoder.matches(plainPassword, encodedPassword);
  }

  /**
   * Main method for generating encoded passwords from command line.
   * Usage: java PasswordGenerator <plain_password>
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Usage: java PasswordGenerator <plain_password>");
      System.out.println("Example: java PasswordGenerator admin123");
      return;
    }

    String plainPassword = args[0];
    String encodedPassword = encodePassword(plainPassword);

    System.out.println("Plain password: " + plainPassword);
    System.out.println("BCrypt encoded: " + encodedPassword);
    System.out.println();
    System.out.println("You can use this encoded password in your data.sql file.");
    System.out.println("Example SQL:");
    System.out.println("INSERT INTO users (email, username, password) VALUES ('admin@example.com', 'admin', '"
        + encodedPassword + "');");
  }
}