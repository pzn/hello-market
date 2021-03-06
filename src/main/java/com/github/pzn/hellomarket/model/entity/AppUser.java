package com.github.pzn.hellomarket.model.entity;

import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appuser")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

  @Id
  @GeneratedValue(strategy = SEQUENCE, generator = "appuser_id_seq")
  @SequenceGenerator(name = "appuser_id_seq", sequenceName = "appuser_id_seq")
  private Long id;
  private String code;
  private String marketIdentifier;

  private String firstName;
  private String lastName;
  private String openId;

  @ManyToOne
  @JoinColumn(name = "apporg_id")
  private AppOrg appOrg;
}
