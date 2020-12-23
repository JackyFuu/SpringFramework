package com.jacky.service;

import com.jacky.domain.ORMUser;
import com.jacky.orm.DbTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jacky
 * @time 2020-12-23 10:36
 * @discription
 */

@Transactional
@Component
public class ORMUserService {
    @Autowired
    DbTemplate db;

    public ORMUser getUserById(long id) {
        return db.get(ORMUser.class, id);
    }

    public ORMUser fetchUserByEmail(String email) {
        return db.from(ORMUser.class).where("email = ?", email).first();
    }

    public ORMUser getUserByEmail(String email) {
        return db.from(ORMUser.class).where("email = ?", email).unique();
    }

    public String getNameByEmail(String email) {
        ORMUser user = db.select("name").from(ORMUser.class).where("email = ?", email).unique();
        return user.getName();
    }

    public List<ORMUser> getUsers(int pageIndex) {
        int pageSize = 100;
        return db.from(ORMUser.class).orderBy("id").limit((pageIndex - 1) * pageSize, pageSize).list();
    }

    public ORMUser login(String email, String password) {
        ORMUser user = fetchUserByEmail(email);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        throw new RuntimeException("login failed.");
    }

    public ORMUser register(String email, String password, String name) {
        ORMUser user = new ORMUser();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setCreatedAt(System.currentTimeMillis());
        db.insert(user);
        return user;
    }

    public void updateUser(Long id, String name) {
        ORMUser user = getUserById(id);
        user.setName(name);
        user.setCreatedAt(System.currentTimeMillis());
        db.update(user);
    }

    public void deleteUser(Long id) {
        db.delete(ORMUser.class, id);
    }
}
