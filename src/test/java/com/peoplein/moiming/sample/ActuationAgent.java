package com.peoplein.moiming.sample;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class ActuationAgent {

    public static void main(String[] args) {
        ZoneController zoneController = new ZoneController();
        // 1. 현재 ZoneInfo 를 가져온다
        ZoneInfo curZone = zoneController.getCurZoneInfo();

        // 2. Human 여부 기준으로 판단한다
        if (curZone.isHuman()) { // 존재한다
            int judged = curZone.judgeHumanTemp();
            if (judged != 0) {
                ActuationPlan actuationPlan = zoneController.generateActuation();
                ActuationType type;
                if (judged > 0) {
                    type = ActuationType.HEAT;
                } else {
                    type = ActuationType.COOL;
                }
                zoneController.runActuator(type, actuationPlan);
            }
        }

        // 3. 필요시 ActuationPlan 을 생성한다

        // 4. 적절한 Actuator 에게 전달한다
    }
}

class ZoneController implements ActuatorController {

    private List<Actuator> actuators;

    @Override
    public ZoneInfo getCurZoneInfo() {
        return null;
    }

    @Override
    public ActuationPlan generateActuation() {
        return null;
    }

    @Override
    public boolean runActuator(ActuationType type, ActuationPlan plan) {
        return false;
    }
}

@Getter
class ZoneInfo {
    private boolean isHuman;
    private int curTemp;
    private int curAirQuality;
    private int setHumanTemp;
    private int setNonHumanTemp;
    private int setHumanAirQuality;
    private int setNonHumanAirQuality;

    public int judgeHumanTemp() { // 양수면 올려야 함 // 음수면 내려야 함
        return setHumanTemp - curTemp;
    }
}

enum ActuationType {
    COOL, HEAT, VENTILATE
}

class ActuationPlan {
    private ActuationType type;
    private int deltaValue;
    private LocalDateTime createdAt;
}

interface ActuatorController {
    ZoneInfo getCurZoneInfo();

    ActuationPlan generateActuation();

    boolean runActuator(ActuationType type, ActuationPlan plan);
}

abstract class Actuator {

}

class CoolerActuator extends Actuator {

}