package com.authbase.repository;

import com.authbase.entity.RefreshToken;
import com.authbase.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity operations.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * Find refresh token by token string.
   * 
   * @param token the token string
   * @return Optional containing the refresh token if found
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * Find refresh tokens by user.
   * 
   * @param user the user
   * @return list of refresh tokens for the user
   */
  List<RefreshToken> findByUser(User user);

  /**
   * Find refresh tokens by user ID.
   * 
   * @param userId the user ID
   * @return list of refresh tokens for the user
   */
  List<RefreshToken> findByUserId(Long userId);

  /**
   * Find valid refresh tokens for a user (not expired).
   * 
   * @param user the user
   * @return list of valid refresh tokens
   */
  @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.expiryDate > :now")
  List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

  /**
   * Find expired refresh tokens.
   * 
   * @param now current time
   * @return list of expired refresh tokens
   */
  @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate <= :now")
  List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

  /**
   * Delete refresh tokens by user.
   * 
   * @param user the user
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
  void deleteByUser(@Param("user") User user);

  /**
   * Delete refresh tokens by user ID.
   * 
   * @param userId the user ID
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
  void deleteByUserId(@Param("userId") Long userId);

  /**
   * Delete expired refresh tokens.
   * 
   * @param now current time
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate <= :now")
  void deleteExpiredTokens(@Param("now") LocalDateTime now);

  /**
   * Delete refresh tokens by expiry date before specified time.
   * 
   * @param expiryDate the expiry date threshold
   * @return number of deleted tokens
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :expiryDate")
  long deleteByExpiryDateBefore(@Param("expiryDate") LocalDateTime expiryDate);

  /**
   * Count active refresh tokens for a user.
   * 
   * @param userId     the user ID
   * @param expiryDate the expiry date threshold
   * @return count of active tokens
   */
  @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiryDate > :expiryDate")
  long countByUserIdAndExpiryDateAfter(@Param("userId") Long userId, @Param("expiryDate") LocalDateTime expiryDate);
}