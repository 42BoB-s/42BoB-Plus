package com.example.projectprototype.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "ban")
@Getter @Setter
public class Ban extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User src;

    @ManyToOne
    private User dest;

    public void setSrc(User srcUser) {
        if (this.src != null) {
            this.src.getBanSrcList().remove(this);
        }
        this.src = srcUser;
        srcUser.getBanSrcList().add(this);
    }

    public void setDest(User destUser) {
        if (this.dest != null) {
            this.dest.getBanDestList().remove(this);
        }
        this.dest = destUser;
        destUser.getBanDestList().add(this);
    }
}
