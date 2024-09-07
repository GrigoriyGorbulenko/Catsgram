package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(User user) {
        // проверяем выполнение необходимых условий
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Email должен быть указан");
        }
        if (users.values()
                .stream()
                .anyMatch(item -> isEmailExists(item.getEmail()))) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        // сохраняем новую публикацию в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    public User update(User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (!oldUser.getEmail().equalsIgnoreCase(newUser.getEmail()) && isEmailExists(newUser.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            if (isValidUserName(newUser.getUsername())) {
                oldUser.setUsername(newUser.getUsername());
            }
            if (isValidEmail(newUser.getEmail())) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (isValidPassword(newUser.getPassword())) {
                oldUser.setPassword(newUser.getPassword());
            }
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    public User findUserByEmail(String email) {
        Optional<User> result = users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();

        if (result.isPresent()) {
            return result.get();
        } else {
            throw new NotFoundException("Пользователь с email = " + email + " не найден");
        }
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean isEmailExists(String email) {
        return users.values()
                .stream()
                .anyMatch(item -> item.getEmail().equalsIgnoreCase(email));
    }

    private boolean isValidUserName(String userName) {
        return !(userName == null || userName.isEmpty());
    }

    private boolean isValidEmail(String email) {
        return !(email == null || email.isEmpty());
    }

    private boolean isValidPassword(String password) {
        return !(password == null || password.isEmpty());
    }

}