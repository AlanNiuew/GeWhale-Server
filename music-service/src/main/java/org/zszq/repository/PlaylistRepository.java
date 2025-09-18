package org.zszq.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zszq.entity.Playlist;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Page<Playlist> findByCreatorId(Long creatorId, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Playlist> searchPublicPlaylists(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.playCount DESC")
    List<Playlist> findTopPublicPlaylists(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    List<Playlist> findLatestPublicPlaylists(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.creatorId = :userId AND p.type = :type")
    Optional<Playlist> findByCreatorIdAndType(@Param("userId") Long userId, 
                                             @Param("type") Playlist.PlaylistType type);

    @Query("SELECT COUNT(p) FROM Playlist p WHERE p.creatorId = :userId")
    long countByCreatorId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Playlist p SET p.playCount = p.playCount + 1 WHERE p.id = :playlistId")
    void incrementPlayCount(@Param("playlistId") Long playlistId);

    @Modifying
    @Query("UPDATE Playlist p SET p.likeCount = p.likeCount + 1 WHERE p.id = :playlistId")
    void incrementLikeCount(@Param("playlistId") Long playlistId);

    @Modifying
    @Query("UPDATE Playlist p SET p.likeCount = p.likeCount - 1 WHERE p.id = :playlistId AND p.likeCount > 0")
    void decrementLikeCount(@Param("playlistId") Long playlistId);

    @Modifying
    @Query("UPDATE Playlist p SET p.musicCount = :count WHERE p.id = :playlistId")
    void updateMusicCount(@Param("playlistId") Long playlistId, @Param("count") Integer count);
}