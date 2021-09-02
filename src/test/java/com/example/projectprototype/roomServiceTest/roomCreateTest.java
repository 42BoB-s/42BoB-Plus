package com.example.projectprototype.roomServiceTest;

import com.example.projectprototype.dto.RoomDto;
import com.example.projectprototype.entity.Room;
import com.example.projectprototype.entity.RoomMenu;
import com.example.projectprototype.entity.User;
import com.example.projectprototype.entity.enums.Location;
import com.example.projectprototype.entity.enums.RoomStatus;
import com.example.projectprototype.repository.RoomRepository;
import com.example.projectprototype.repository.UserRepository;
import com.example.projectprototype.service.RoomService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * roomCreat 장상 / 오류 케이스 테스트
 */
@SpringBootTest
@Transactional
public class roomCreateTest {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RoomService roomService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    void convertToRoomNormalTest() {
        String meetTime = "2021-08-28 11:30:00";
        User user = createTestUser("tjeong", "/img/tjeong", "1");
        userRepository.save(user);

        List<String> menuExpected = new ArrayList<>();
        menuExpected.add("중식");
        menuExpected.add("한식");
        menuExpected.add("커피");

        RoomDto roomDTO = new RoomDto();
        roomDTO.setOwner(user);
        roomDTO.setCapacity(4);
        roomDTO.setStatus("active");
        roomDTO.setTitle("hello_room");
        roomDTO.setLocation("서초");
        roomDTO.setMeetTime(meetTime);
        roomDTO.setAnnouncement("announcement");
        roomDTO.setMenus(menuExpected);

        // roomDTO 를 Room 으로 바꿔서 DB 에 저장.
        Room  room = roomService.convertToRoom(roomDTO, user.getId());
        Assertions.assertThat(room.getId()).isGreaterThan(0);

        // DB에 저장된 정보를 Room 객체로 만들어서 정보 확인
        Room room2 = roomRepository.findById(room.getId()).get();
        Assertions.assertThat(room2.getMeetTime().format(formatter))
                .isEqualTo(roomDTO.getMeetTime());

        List<String> menuActual = new ArrayList<>();
        for (RoomMenu roomMenu : room2.getRoomMenuList()) {
            menuActual.add(roomMenu.getMenu().getName().toString());
        }
        Assertions.assertThat(menuActual).isEqualTo(menuActual);
    }

    /**
     * -1 입력된 시간의 형태가 형식에 맞지 않음.
     * -2 입력된 시간 + 1시간 이내에 유저가 방에 참여하고 있음.
     * -3 userId 가 DB에 등록되지 않은 id 임.
     * -4 enum 형 string 들 (ex : menus, location) 이 형식에 맞지 않음.
     */
    @Test
    void createRoom_NonParsableTimeTest() {
        String meetTime = "11:30:00 28/8/2021";
        User user = createTestUser("tjeong", "/img/tjeong", "1");
        userRepository.save(user);

        List<String> menuExpected = new ArrayList<>();
        menuExpected.add("중식");

        RoomDto roomDTO = new RoomDto();
        roomDTO.setOwner(user);
        roomDTO.setCapacity(4);
        roomDTO.setStatus("active");
        roomDTO.setTitle("hello_room");
        roomDTO.setLocation("서초");
        roomDTO.setMeetTime(meetTime);
        roomDTO.setMenus(menuExpected);

        boolean exceptionCatch = false;
        try {
            long result = roomService.createRoom(roomDTO, user.getId());
        } catch (DateTimeParseException e) {
            exceptionCatch = true;
        }
        Assertions.assertThat(exceptionCatch).isEqualTo(true);
    }

    @Test
    void createRoom_DuplicateTimeTest() {
        String meetTime = "2021-08-28 11:30:00";
        User user = createTestUser("tjeong", "/img/tjeong", "1");
        userRepository.save(user);

        List<String> menuExpected = new ArrayList<>();
        menuExpected.add("중식");

        RoomDto roomDTO = new RoomDto();
        roomDTO.setOwner(user);
        roomDTO.setCapacity(4);
        roomDTO.setStatus("active");
        roomDTO.setTitle("hello_room");
        roomDTO.setLocation("서초");
        roomDTO.setMeetTime(meetTime);
        roomDTO.setAnnouncement("announcement");
        roomDTO.setMenus(menuExpected);

        long result = roomService.createRoom(roomDTO, user.getId());
        Assertions.assertThat(result).isGreaterThan(0);

        roomDTO.setMeetTime("2021-08-28 12:00:00");
        long result2 = roomService.createRoom(roomDTO, user.getId());
        Assertions.assertThat(result2).isEqualTo(-2L);
        }

    @Test
    void createRoom_NotValidUserIdTest() {
        String meetTime = "2021-08-28 11:30:00";
        User user = createTestUser("tjeong", "/img/tjeong", "1");
        userRepository.save(user);

        List<String> menuExpected = new ArrayList<>();
        menuExpected.add("중식");

        RoomDto roomDTO = new RoomDto();
        roomDTO.setOwner(user);
        roomDTO.setCapacity(4);
        roomDTO.setStatus("active");
        roomDTO.setTitle("hello_room");
        roomDTO.setLocation("서초");
        roomDTO.setMeetTime(meetTime);
        roomDTO.setMenus(menuExpected);

        long result = roomService.createRoom(roomDTO, "hello");
        Assertions.assertThat(result).isEqualTo(-3L);
    }

    @Test
    void createRoom_NotValidEnumFormatTest() {
        String meetTime = "2021-08-28 11:30:00";
        User user = createTestUser("tjeong", "/img/tjeong", "1");
        userRepository.save(user);

        // 메뉴이름
        List<String> menuExpected = new ArrayList<>();
        menuExpected.add("돈까스");

        RoomDto roomDTO = new RoomDto();
        roomDTO.setOwner(user);
        roomDTO.setCapacity(4);
        roomDTO.setStatus("active");
        roomDTO.setTitle("hello_room");
        roomDTO.setLocation("서초");
        roomDTO.setMeetTime(meetTime);
        roomDTO.setMenus(menuExpected);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> { roomService.createRoom(roomDTO, "tjeong");
        });


        // 지역
        menuExpected.add("중식");
        menuExpected.remove(0);

        RoomDto roomDTO2 = new RoomDto();
        roomDTO.setOwner(user);
        roomDTO.setCapacity(4);
        roomDTO.setStatus("active");
        roomDTO.setTitle("hello_room");
        roomDTO.setLocation("강남");
        roomDTO.setMeetTime(meetTime);
        roomDTO.setMenus(menuExpected);


        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> { roomService.createRoom(roomDTO2, "tjeong");
                });

    }

    static User createTestUser(String userId, String profile, String role) {
        User user = new User();
        user.setId(userId);
        user.setProfile(profile);
        user.setRole(role);
        return user;
    }

    static Room createTestRoom(String meetTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Room room = new Room();

        room.setTitle("hello");
        room.setLocation(Location.서초);
        room.setStatus(RoomStatus.active);
        room.setAnnouncement("sample");
        room.setMeetTime(LocalDateTime.parse(meetTime,formatter));
        room.setAnnouncement("hellohellohello~~");
        return room;
    }
}