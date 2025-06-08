package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserSaveDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto addUser(UserSaveDto userSaveDto) {
        User user = userMapper.mapToUser(userSaveDto);
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicatedDataException(String.format("Email %s is already in use", user.getEmail()));
        }
        return userMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids);
        } else {
            users = userRepository.findUsersLimited(from, size);
        }

        return users.stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
        userRepository.deleteById(userId);
    }
}
