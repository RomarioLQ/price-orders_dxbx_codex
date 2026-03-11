package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Пользователь системы (сотрудник клиента).
 */
@Getter
@Setter
@Entity
@Table(name = "system_users")
public class SystemUser {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "phone")
  private String phone;

  /**
   * Telegram user id (после миграции тип varchar(50)).
   */
  @Column(name = "tg_user_id", nullable = false, length = 50)
  private String tgUserId;
}
