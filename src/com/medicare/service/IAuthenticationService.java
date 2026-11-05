package com.medicare.service;

import com.medicare.exception.AuthenticationException;
import com.medicare.exception.InvalidInputException;
import com.medicare.model.Patient;
import com.medicare.model.User;

public interface IAuthenticationService {
    User authenticate(String usernameOrEmail, String password) throws AuthenticationException;
    boolean registerPatient(Patient patient) throws InvalidInputException;
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
}