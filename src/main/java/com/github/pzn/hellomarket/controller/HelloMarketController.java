package com.github.pzn.hellomarket.controller;

import com.github.pzn.hellomarket.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HelloMarketController {

  private AppUserService appUserService;

  @Autowired
  public HelloMarketController(AppUserService appUserService) {
    this.appUserService = appUserService;
  }

  @GetMapping
  public String index(Model model) {
    model.addAttribute("appUsers", appUserService.findAll());
    return "index";
  }
}
