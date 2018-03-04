package com.github.pzn.hellomarket.model.entity;

import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "apporg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppOrg {

  @Id
  @GeneratedValue(strategy = SEQUENCE, generator = "apporg_id_seq")
  @SequenceGenerator(name = "apporg_id_seq", sequenceName = "apporg_id_seq")
  private Long id;
  private String code;
  private String marketIdentifier;
  private Boolean active;
  private Long maxUsers;

  private String name;
  private String country;

  @OneToMany(fetch = LAZY, cascade = REMOVE, mappedBy = "appOrg")
  private Set<AppUser> appUsers;
}
