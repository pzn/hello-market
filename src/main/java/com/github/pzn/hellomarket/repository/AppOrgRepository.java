package com.github.pzn.hellomarket.repository;

import com.github.pzn.hellomarket.model.entity.AppOrg;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AppOrgRepository extends CrudRepository<AppOrg, Long> {

  AppOrg findByMarketIdentifier(String marketIdentifier);

  AppOrg findByCode(String code);

  @Query("SELECT ao FROM AppOrg ao LEFT JOIN FETCH ao.appUsers")
  Set<AppOrg> findAllFetchUsers();

  @Query("SELECT ao FROM AppOrg ao LEFT JOIN FETCH ao.appUsers WHERE ao.code = :code")
  AppOrg findByCodeFetchUsers(@Param("code") String code);

  @Transactional
  @Modifying
  @Query("UPDATE AppOrg SET active = :isActive WHERE id = :id")
  void changeActiveStatus(@Param("id") Long id, @Param("isActive") boolean isActive);
}
