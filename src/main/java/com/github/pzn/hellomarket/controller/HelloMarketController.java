package com.github.pzn.hellomarket.controller;

import com.github.pzn.hellomarket.service.AppOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HelloMarketController {

  private AppOrgService appOrgService;

  @Autowired
  public HelloMarketController(AppOrgService appOrgService) {
    this.appOrgService = appOrgService;
  }

  @GetMapping
  public String index(Model model) {
    model.addAttribute("organizations", appOrgService.findAll());
    return "index";
  }
}
