package com.github.pzn.hellomarket.service;

import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppOrgService {

  private AppOrgRepository repository;

  @Autowired
  public AppOrgService(AppOrgRepository appOrgRepository) {
    this.repository = appOrgRepository;
  }

  public Iterable<AppOrg> findAll() {
    return repository.findAllFetchUsers();
  }
}
