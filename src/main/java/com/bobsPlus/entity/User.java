package com.bobsPlus.entity;

import com.bobsPlus.dto.MealHistoryDto;
import com.bobsPlus.dto.StatDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@NamedNativeQuery(
        name = "searchStatQuery",
        query = "WITH UserSucceedRoom AS(" +
                "    select room_id, location" +
                "    from (select room_id from participant where user_id = :id)p" +
                "    left join room on p.room_id = room.id where status = 'succeed'" +
                "),NotUserSucceedRoom AS(" +
                "    select room_id, location, user_id" +
                "    from (select room_id, user_id from participant where user_id != :id )p" +
                "    left join room on p.room_id = room.id where status = 'succeed')" +
                "   select (select count(*) from UserSucceedRoom) as succeedStat, (select location from UserSucceedRoom group by location order by count(*) desc LIMIT 1) as locationStat," +
                "       (select name from UserSucceedRoom left join room_menu on UserSucceedRoom.room_id = room_menu.room_id left join menu m on room_menu.menu_id = m.id group by menu_id order by count(*) desc LIMIT 1) as menusStat," +
                "       (select NotUserSucceedRoom.user_id from UserSucceedRoom" +
                "            join NotUserSucceedRoom on UserSucceedRoom.room_id = NotUserSucceedRoom.room_id" +
                "       group by user_id order by count(*) desc limit 1)as name",
        resultSetMapping = "StatDtoMapping"
)
@SqlResultSetMapping(
        name = "StatDtoMapping",
        classes = @ConstructorResult(
                targetClass = StatDto.class,
                columns = {
                        @ColumnResult(name = "succeedStat",type = long.class),
                        @ColumnResult(name = "locationStat",type = String.class),
                        @ColumnResult(name = "menusStat",type = String.class),
                        @ColumnResult(name = "name",type = String.class)
                }
        )
)
@NamedNativeQuery(
        name = "searchHistoryQuery",
        query = "select user_id as name,date_format(meet_time,'%Y-%m-%d %H:%m')as meetTime" +
                " from (select room_id from participant where user_id = :id) p1" +
                    " join (select * from participant where user_id != :id) p2 on p1.room_id = p2.room_id" +
                    " join room on p1.room_id = room.id"+
                " where TIMESTAMPDIFF(DAY ,meet_time,now()) < 31 and room.status = \"succeed\"",
        resultSetMapping = "MealHistoryDtoMapping"
)
@SqlResultSetMapping(
        name = "MealHistoryDtoMapping",
        classes = @ConstructorResult(
                targetClass = MealHistoryDto.class,
                columns = {
                        @ColumnResult(name = "name",type = String.class),
                        @ColumnResult(name = "meetTime",type = String.class)
                }
        )
)
@Table(name = "user")
public class User extends TimeEntity {
    @Id
    @Column(length = 45)
    private String id;

    @Column(length = 45)
    private String role;
    
    @Lob
    private String profile;

    @OneToMany(mappedBy = "src")
    private List<Ban> banSrcList = new ArrayList<>();

    @OneToMany(mappedBy = "dest")
    private List<Ban> banDestList = new ArrayList<>();

    // room.owner 와의 연관관계 설정
    @OneToMany(mappedBy = "owner")
    private List<Room> ownerList = new ArrayList<>();

    // room 에 참여한 정보
    @OneToMany(mappedBy = "user")
    private List<Participant> participantList = new ArrayList<>();

    public void addBanSrc(Ban ban) {
        this.banSrcList.add(ban);
        if (ban.getSrc() != this) {
            ban.setSrc(this);
        }
    }

    public void addBanDest(Ban ban) {
        this.banDestList.add(ban);
        if (ban.getDest() != this) {
            ban.setDest(this);
        }
    }

    public void addOwner(Room room) {
        this.ownerList.add(room);
        if (room.getOwner() != this) {
            room.setOwner(this);
        }
    }

    public void addParticipant(Participant participant) {
        this.participantList.add(participant);
        if (participant.getUser() != this) {
            participant.setUser(this);
        }
    }
}
