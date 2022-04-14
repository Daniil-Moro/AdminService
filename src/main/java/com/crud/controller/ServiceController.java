package com.crud.controller;


import com.crud.model.ResponseToken;
import com.crud.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
public class ServiceController {
    private static final String AUTHENTICATION_URL = "http://localhost:3001/authenticate";
    private static final String LIST_USERS_URL = "http://localhost:3001/api/users";

    @Autowired
    public ServiceController() {

    }

    @Autowired
    RestTemplate restTemplate;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String invoke(@RequestParam String username, @RequestParam String password) {
        String response = null;
        try {
            User authenticationUser = getAuthenticationUser(username, password);
            String authenticationBody = getBody(authenticationUser);
            HttpHeaders authenticationHeaders = getHeaders();
            HttpEntity<String> authenticationEntity = new HttpEntity<String>(authenticationBody, authenticationHeaders);

            ResponseEntity<ResponseToken> authenticationResponse = restTemplate.exchange(AUTHENTICATION_URL, HttpMethod.POST, authenticationEntity, ResponseToken.class);
            if (authenticationResponse.getStatusCode().equals(HttpStatus.OK)) {
                String token = "Bearer " + authenticationResponse.getBody().getToken();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", token);
                HttpEntity<String> jwtEntity = new HttpEntity<String>(headers);
                ResponseEntity<String> usersResponse = restTemplate.exchange(LIST_USERS_URL, HttpMethod.GET, jwtEntity, String.class);
                if (usersResponse.getStatusCode().equals(HttpStatus.OK)) {
                    response = usersResponse.getBody();
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
            return "User was not authenticated";
        }
        return response;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private User getAuthenticationUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    private String getBody(final User user) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(user);
    }

}
