package project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.DAO.UserDAO;
import project.models.User;
import org.springframework.http.HttpStatus;
import project.utils.Response;

import java.util.ArrayList;
import java.util.List;


@ResponseBody
@RestController
@RequestMapping("/api/user")
public class UserController {

    private UserDAO userDAO;


    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @RequestMapping(path = "/{nickname}/create", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createUser(@RequestBody User body, @PathVariable("nickname") String nickname) { //TODO rewrite
        body.setNickname(nickname);
            Response<User> res = userDAO.createUser(body);
            return ResponseEntity.status(res.getStatus()).body(res.getBody());

    }

}