package com.jacky.service;


import com.jacky.annotation.MetricTime;
import com.jacky.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author jacky
 * @time 2020-12-04 14:46
 * @discription 实现用户注册和登录
 */
@Component
@Transactional   //表示所有的public方法都具有事务支持
public class UserService {

    @Autowired
    private MailService mailService;

    public void setMailService(@Autowired MailService mailService) {
        this.mailService = mailService;
    }

    private List<User> users = new ArrayList<>(Arrays.asList( // users:
            new User(1, "bob@example.com", "password", "Bob"), // bob
            new User(2, "alice@example.com", "password", "Alice"), // alice
            new User(3, "tom@example.com", "password", "Tom"))); // tom

    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                mailService.sendLoginMail(user);
                return user;
            }
        }
        throw new RuntimeException("login failed.");
    }

    public User getUser(long id) {
        return this.users.stream().filter(user -> user.getId() == id).findFirst().get();
    }

    @Transactional   //声明式事务
    @MetricTime("register")
    public User register(String email, String password, String name) {
        users.forEach((user) -> {
            if (user.getEmail().equalsIgnoreCase(email)) {
                throw new RuntimeException("email exist.");
            }
        });
        User user = new User(users.stream().mapToLong(User::getId).max().getAsLong(), email, password, name);
        users.add(user);
        mailService.sendRegistrationMail(user);
        return user;
    }


    /**
     * AOP避坑指南
     */

    /**
     * 成员变量：在UserService$$EnhancerBySpringCGLIB中，并未执行，
     *  原因是，没必要初始化proxy的成员变量，因为proxy的目的是代理方法。
     *
     * 实际上，成员变量的初始化是在构造方法中完成的。
     * 然而，对于Spring通过CGLIB动态创建的UserService$$EnhancerBySpringCGLIB代理类，
     * 它的构造方法中并未调用super()，因此，从父类继承的成员变量，
     * 包括final类型的成员变量，统统都没有初始化。
     *
     * 自动加super()的功能时Java编译器实现的，它发现你没加，就自动加上，发现你加错了，就报编译错误。
     * 但实际上，如果直接构造字节码，一个类的构造方法中，不一定非要调用super()。
     * Spring使用CGLIB构造的Proxy类，是直接生成字节码，并没有源码-编译-字节码这个步骤，因此：
     *  $$ Spring通过CGLIB创建的代理类，不会初始化代理类自身继承的任何成员变量，包括final类型的成员变量。 $$
     */
    public final ZoneId zoneId = ZoneId.systemDefault();

    //构造方法
    public UserService(){
        System.out.println("UserService(): init ...");
        System.out.println("UserService(): zoneId = " + this.zoneId);
    }

    //public方法
    public ZoneId getZoneId(){
        return zoneId;
    }

    /**
     * public final方法
     * 代理类无法覆写final方法（这一点绕不过JVM的ClassLoader检查），该方法返回的是代理类的zoneId字段，即null。
     * @return
     */
    public final ZoneId getFinalZoneId(){
        return zoneId;
    }

    /**
     * JdbcTemplate
     * Spring提供的JdbcTemplate采用Template模式，提供了一系列以回调为特点的工具方法，目的是避免繁琐的try...catch语句。
     * 使用JdbcTemplate时，根据需要优先选择高级方法；
     * 任何JDBC操作都可以使用保底的execute(ConnectionCallback)方法。
     */
    @Autowired
    JdbcTemplate jdbcTemplate;

    public User getUserById(long id){
        // 注意传入的是ConnectionCallback。回调方法允许获取Connection，然后做任何基于Connection的操作。
        return jdbcTemplate.execute((Connection conn)->{
            // 可以直接使用conn实例，不要释放它，回调结束后JdbcTemplate自动释放:
            // 在内部手动创建的PreparedStatement、ResultSet必须用try(...)释放:
            try(PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?")){
                ps.setObject(1, id);
                try(ResultSet rs = ps.executeQuery()){
                    if (rs.next()) {
                        return new User( // new User object:
                                rs.getLong("id"), // id
                                rs.getString("email"), // email
                                rs.getString("password"), // password
                                rs.getString("name")); // name
                    }
                    throw new RuntimeException("user not found by id.");
                }
            }
        });
    }

    public User getUserByName(String name) {
        // 需要传入SQL语句，以及PreparedStatementCallback:
        return jdbcTemplate.execute("SELECT * FROM users WHERE name = ?", (PreparedStatement ps) -> {
            // PreparedStatement实例已经由JdbcTemplate创建，并在回调后自动释放:
            ps.setObject(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User( // new User object:
                            rs.getLong("id"), // id
                            rs.getString("email"), // email
                            rs.getString("password"), // password
                            rs.getString("name")); // name
                }
                throw new RuntimeException("user not found by id.");
            }
        });
    }

    /**
     *  在queryForObject()方法中，传入SQL以及SQL参数后，JdbcTemplate会自动创建PreparedStatement，
     *  自动执行查询并返回ResultSet，我们提供的RowMapper需要做的事情就是把ResultSet的当前行映射成一个JavaBean并返回。
     *  整个过程中，使用Connection、PreparedStatement和ResultSet都不需要我们手动管理。
     *
     *  RowMapper不一定返回JavaBean，实际上它可以返回任何Java对象。例如，使用SELECT COUNT(*)查询时，可以返回Long：
     */

    public User getUserByEmail(String email) {
        // 传入SQL，参数和RowMapper实例:
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email = ?", new Object[] { email },
                (ResultSet rs, int rowNum) -> {
                    // 将ResultSet的当前行映射为一个JavaBean:
                    return new User( // new User object:
                            rs.getLong("id"), // id
                            rs.getString("email"), // email
                            rs.getString("password"), // password
                            rs.getString("name")); // name
                });
    }

    /**
     * 如果我们期望返回多行记录，而不是一行，可以用query()方法。
     *
     * 直接使用Spring提供的BeanPropertyRowMapper。如果数据库表的结构恰好和JavaBean的属性名称一致，
     * 那么BeanPropertyRowMapper就可以直接把一行记录按列名转换为JavaBean。
     */
    public List<User> getUsers(int pageIndex) {
        int limit = 100;
        int offset = limit * (pageIndex - 1);
        return jdbcTemplate.query("SELECT * FROM users LIMIT ? OFFSET ?", new Object[] { limit, offset },
                new BeanPropertyRowMapper<>(User.class));
    }

    /**
     * 如果我们执行的不是查询，而是插入、更新和删除操作，那么需要使用update()方法：
     */
    public void updateUser(User user) {
        // 传入SQL，SQL参数，返回更新的行数:
        if (1 != jdbcTemplate.update("UPDATE user SET name = ? WHERE id=?", user.getName(), user.getId())) {
            throw new RuntimeException("User not found by id");
        }
    }

    public long getUsers() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", null, (ResultSet rs, int rowNum) -> {
            return rs.getLong(1);
        });
    }
}
