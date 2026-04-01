package com.futureprograms.MyIkea.Interfaces;

import org.springframework.security.core.userdetails.UserDetailsService;
import com.futureprograms.MyIkea.Models.Auth.User;

import java.util.List;

public interface UserInterface extends UserDetailsService {
    public List<User> list();

    public User detail(int id);

    public void create(User role) throws Exception;

    public void edit(int id, User role);

    public void delete(int id);
}
