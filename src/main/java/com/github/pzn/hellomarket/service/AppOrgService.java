package com.github.pzn.hellomarket.service;

import static javax.persistence.FetchType.EAGER;

import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import javax.persistence.FetchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppOrgService {

  private AppOrgRepository repository;

  @Autowired
  public AppOrgService(AppOrgRepository appOrgRepository) {
    this.repository = repository;
  }

  public Iterable<AppOrg> findAll(FetchType fetchType) {

    if (fetchType.equals(EAGER)) {
      return repository.findAllFetchUsers();
    }
    return repository.findAll();
  }
}
