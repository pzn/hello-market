package com.github.pzn.hellomarket.repository;

import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, Long> {

  AppUser findByCode(String code);

  AppUser findByMarketAccountIdentifier(String marketAccountIdentifier);

  @Transactional
  @Modifying
  @Query("UPDATE AppUser SET subscriptionType = :subscriptionType WHERE id = :id")
  void changeSubscriptionType(@Param("id") Long id, @Param("subscriptionType") SubscriptionType subscriptionType);

  @Transactional
  @Modifying
  @Query("UPDATE AppUser SET active = :active WHERE id = :id")
  void changeAppUserStatus(@Param("id") Long id, @Param("active") boolean active);
}
