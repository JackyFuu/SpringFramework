package com.jacky.service;

import com.jacky.domain.MybatisUser;
import com.jacky.mapper.MybatisUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jacky
 * @time 2020-12-23 09:57
 * @discription
 */

@Component
@Transactional
public class MybatisUserService {

    @Autowired
    MybatisUserMapper userMapper;

    public MybatisUser getUserById(long id) {
        MybatisUser user = userMapper.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found by id.");
        }
        return user;
    }

    public MybatisUser fetchUserByEmail(String email) {
        return userMapper.getByEmail(email);
    }

    public MybatisUser getUserByEmail(String email) {
        MybatisUser user = fetchUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found by email.");
        }
        return user;
    }

    public List<MybatisUser> getUsers(int pageIndex) {
        int pageSize = 100;
        return userMapper.getAll((pageIndex - 1) * pageSize, pageSize);
    }

    public MybatisUser login(String email, String password) {
        MybatisUser user = userMapper.getByEmail(email);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        throw new RuntimeException("login failed.");
    }

    public MybatisUser register(String email, String password, String name) {
        MybatisUser user = new MybatisUser();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setCreatedAt(System.currentTimeMillis());
        userMapper.insert(user);
        return user;
    }

    public void updateUser(Long id, String name) {
        MybatisUser user = getUserById(id);
        user.setName(name);
        user.setCreatedAt(System.currentTimeMillis());
        userMapper.update(user);
    }

    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
}
