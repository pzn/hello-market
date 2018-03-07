package com.github.pzn.hellomarket.repository;

import com.github.pzn.hellomarket.model.entity.AppUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, Long> {

  @Query("SELECT au "
      + "FROM AppUser au "
      + "  JOIN FETCH au.appOrg ao "
      + "WHERE au.marketIdentifier = :marketIdentifier "
      + "  AND ao.code = :appOrgCode")
  AppUser findByMarketIdentifierAndAppOrgCode(@Param("marketIdentifier") String marketIdentifier, @Param("appOrgCode") String appOrgCode);
}
