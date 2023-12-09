package com.peoplein.moiming.domain.test;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;

@Profile("test")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Wheel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wheel_id")
    private Long id;

    private String wheelName;

    @JoinColumn(name = "car_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Car car;

    public Wheel(String wheelName, Car car) {
        this.wheelName = wheelName;
        this.car = car;
    }
}
