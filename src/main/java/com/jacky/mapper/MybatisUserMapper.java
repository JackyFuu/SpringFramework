package com.jacky.mapper;

import com.jacky.domain.MybatisUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author jacky
 * @time 2020-12-23 09:44
 * @discription
 */
public interface MybatisUserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    MybatisUser getById(@Param("id") long id);

    @Select("SELECT * FROM users WHERE email = #{email}")
    MybatisUser getByEmail(@Param("email") String email);

    @Select("SELECT * FROM users LIMIT #{offset}, #{maxResults}")
    List<MybatisUser> getAll(@Param("offset") int offset, @Param("maxResults") int maxResults);

    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO users (email, password, name, createdAt) VALUES (#{user.email}, #{user.password}, #{user.name}, #{user.createdAt})")
    void insert(@Param("user") MybatisUser user);

    @Update("UPDATE users SET name = #{user.name}, createdAt = #{user.createdAt} WHERE id = #{user.id}")
    void update(@Param("user") MybatisUser user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(@Param("id") long id);
}
