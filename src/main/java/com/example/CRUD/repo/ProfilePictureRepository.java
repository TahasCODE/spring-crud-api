package com.example.CRUD.repo;

import com.example.CRUD.Entity.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

    Optional<ProfilePicture> findByPersonId(Long personId);

    // frees the large object in pg_largeobject; a plain DELETE leaves it behind as an orphan
    @Query(value = "SELECT lo_unlink(content) FROM profile_pictures WHERE person_id = :personId", nativeQuery = true)
    List<Integer> unlinkContentByPersonId(@Param("personId") Long personId);

    @Modifying
    @Query("DELETE FROM ProfilePicture p WHERE p.personId = :personId")
    int deleteByPersonId(@Param("personId") Long personId);
}