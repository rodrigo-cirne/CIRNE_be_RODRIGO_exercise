package com.ecore.roles.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecore.roles.client.UsersClient;
import com.ecore.roles.client.model.User;
import com.ecore.roles.service.UsersService;

@Service
public class UsersServiceImpl implements UsersService {

    private final UsersClient usersClient;

    // removed superfluous @Autowired annotation
    public UsersServiceImpl(UsersClient usersClient) {
        this.usersClient = usersClient;
    }

    public User getUser(UUID id) {
        return usersClient.getUser(id).getBody();
    }

    public List<User> getUsers() {
        return usersClient.getUsers().getBody();
    }
}
