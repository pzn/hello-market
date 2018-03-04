package com.github.pzn.hellomarket.service;

import com.github.pzn.hellomarket.repository.AppUserRepository;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {

  private AppUserRepository repository;

  @Autowired
  public AppUserService(AppUserRepository appUserRepository) {
    this.repository = appUserRepository;
  }

  public Iterable<AppUser> findAll() {
    return repository.findAll();
  }

  public AppUser findById(Long id) {
    return repository.findOne(id);
  }

  public AppUser findByCode(String code) {
    return repository.findByCode(code);
  }

  public AppUser findByMarketAccountIdentifier(String marketAccountIdentifier) {
    return repository.findByMarketAccountIdentifier(marketAccountIdentifier);
  }

  public AppUser save(String marketAccountIdentifier, SubscriptionType subscriptionType) {
    AppUser appUser = AppUser.builder()
        .code(UUID.randomUUID().toString())
        .marketAccountIdentifier(marketAccountIdentifier)
        .subscriptionType(subscriptionType)
        .active(true)
        .build();
    return repository.save(appUser);
  }

  public void changeSubscriptionType(Long id, SubscriptionType newSubscriptionType) {
    repository.changeSubscriptionType(id, newSubscriptionType);
  }

  public void changeStatus(Long id, boolean isActive) {
    repository.changeAppUserStatus(id, isActive);
  }
}
