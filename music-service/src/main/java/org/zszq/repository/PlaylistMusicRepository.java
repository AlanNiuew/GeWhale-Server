package org.zszq.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zszq.entity.PlaylistMusic;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistMusicRepository extends JpaRepository<PlaylistMusic, Long> {

    @Query("SELECT pm FROM PlaylistMusic pm JOIN FETCH pm.music WHERE pm.playlist.id = :playlistId ORDER BY pm.sortOrder ASC")
    Page<PlaylistMusic> findByPlaylistIdOrderBySortOrder(@Param("playlistId") Long playlistId, Pageable pageable);

    @Query("SELECT pm FROM PlaylistMusic pm WHERE pm.playlist.id = :playlistId AND pm.music.id = :musicId")
    Optional<PlaylistMusic> findByPlaylistIdAndMusicId(@Param("playlistId") Long playlistId, 
                                                       @Param("musicId") Long musicId);

    @Query("SELECT MAX(pm.sortOrder) FROM PlaylistMusic pm WHERE pm.playlist.id = :playlistId")
    Optional<Integer> findMaxSortOrderByPlaylistId(@Param("playlistId") Long playlistId);

    @Query("SELECT COUNT(pm) FROM PlaylistMusic pm WHERE pm.playlist.id = :playlistId")
    int countByPlaylistId(@Param("playlistId") Long playlistId);

    @Modifying
    @Query("DELETE FROM PlaylistMusic pm WHERE pm.playlist.id = :playlistId AND pm.music.id = :musicId")
    void deleteByPlaylistIdAndMusicId(@Param("playlistId") Long playlistId, @Param("musicId") Long musicId);

    @Modifying
    @Query("UPDATE PlaylistMusic pm SET pm.sortOrder = pm.sortOrder - 1 " +
           "WHERE pm.playlist.id = :playlistId AND pm.sortOrder > :sortOrder")
    void updateSortOrderAfterRemoval(@Param("playlistId") Long playlistId, @Param("sortOrder") Integer sortOrder);

    @Query("SELECT pm FROM PlaylistMusic pm WHERE pm.addedById = :userId ORDER BY pm.addedAt DESC")
    Page<PlaylistMusic> findByAddedByIdOrderByAddedAtDesc(@Param("userId") Long userId, Pageable pageable);
}