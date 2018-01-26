package com.itheima.miaosha.service;
import com.itheima.miaosha.dao.UserDao;
import com.itheima.miaosha.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class UserService {
    @Autowired
    UserDao userDao;
    public User getById(int id){
        User user = userDao.getById(id);
        return user;
    }
}
