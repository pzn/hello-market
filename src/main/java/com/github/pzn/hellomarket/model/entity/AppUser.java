package com.github.pzn.hellomarket.model.entity;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

  @Id
  @GeneratedValue(strategy = SEQUENCE, generator = "app_user_id_seq")
  @SequenceGenerator(name = "app_user_id_seq", sequenceName = "app_user_id_seq")
  private Long id;
  private String code;
  private String marketAccountIdentifier;
  private boolean active;
  @Enumerated(STRING)
  private SubscriptionType subscriptionType;
}
