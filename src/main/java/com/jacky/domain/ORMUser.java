package com.jacky.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author jacky
 * @time 2020-12-23 10:33
 * @discription
 */

@Entity
@Table(name = "users")
public class ORMUser extends AbstractEntity {
    private String email;
    private String password;
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(nullable = false, length = 100)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(nullable = false, length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, email=%s, name=%s, password=%s, createdAt=%s, createdDateTime=%s]", getId(),
                getEmail(), getName(), getPassword(), getCreatedAt(), getCreatedDateTime());
    }
}
