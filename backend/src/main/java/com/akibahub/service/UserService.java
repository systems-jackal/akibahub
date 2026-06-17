package com.akibahub.service;

import com.akibahub.dto.request.CreateUserRequest;
import com.akibahub.model.User;
import com.akibahub.model.Wallet;
import com.akibahub.model.WalletType;
import com.akibahub.repository.UserRepository;
import com.akibahub.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UserService(UserRepository userRepository,
                        WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public User createUser(CreateUserRequest request) {

        // 1. Create user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        // generate member code
        user.setMemberCode(generateMemberCode());

        User savedUser = userRepository.save(user);

        // 2. Create PERSONAL wallet
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setWalletType(WalletType.PERSONAL);
        wallet.setBalance(java.math.BigDecimal.ZERO);

        walletRepository.save(wallet);

        return savedUser;
    }

    private String generateMemberCode() {
        return "AKB-" + (1000 + new Random().nextInt(9000));
    }
}