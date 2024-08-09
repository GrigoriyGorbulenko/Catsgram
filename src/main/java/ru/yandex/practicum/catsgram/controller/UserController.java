package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;

import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;


import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }
    @PostMapping
    public User create(@RequestBody User user) {
        // проверяем выполнение необходимых условий
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        users.values()
                .stream()
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .forEach(u -> {
            throw new DuplicatedDataException("Этот имейл уже используется");
        });
        // формируем дополнительные данные
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        // сохраняем новую публикацию в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {

            if (newUser.getEmail() == null || newUser.getUsername() == null ||
                    newUser.getPassword() == null) {
                throw new ConditionsNotMetException("данные должны быть указан");
            }
            users.values()
                    .stream()
                    .filter(u -> u.getEmail().equals(newUser.getEmail()))
                    .forEach(u -> {
                        throw new DuplicatedDataException("Этот имейл уже используется");
                    });

            User user = users.get(newUser.getId());
            user.setUsername(newUser.getUsername());
            user.setEmail(newUser.getEmail());
            user.setPassword(newUser.getPassword());

            return user;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
