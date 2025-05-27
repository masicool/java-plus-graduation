package ru.practicum.ewm.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.type.NotFoundException;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        return modelMapper.map(userRepository.save(modelMapper.map(newUserRequest, User.class)), UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findUsersByRequest(List<Long> userIds, int from, int size) {
        PageRequest page = PageRequest.of(from, size);
        if (userIds == null || userIds.isEmpty()) {
            return userRepository.findAll(page).stream()
                    .map(o -> modelMapper.map(o, UserDto.class))
                    .toList();
        }
        return userRepository.findByIdIn(userIds, page).stream()
                .map(o -> modelMapper.map(o, UserDto.class))
                .toList();
    }

    @Override
    @Transactional
    public void deleteUserById(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserShortDto findUserById(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        return modelMapper.map(user, UserShortDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserShortDto> findUsersByIds(List<Long> userIds) {
        return userRepository.findByIdIn(userIds).stream()
                .map(o -> modelMapper.map(o, UserShortDto.class))
                .toList();
    }
}
