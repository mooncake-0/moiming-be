package com.peoplein.moiming;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootApplication
public class MoimingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoimingApplication.class, args);
    }

    /*
     앱 시작시 Firebase 연동 (수명주기중 한 번 수행)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initFirebaseApp() throws IOException {

        FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/fcm/moiming-b2ae3-firebase-adminsdk-21zjr-11c77c69f7.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        System.out.println("FIREBASE INITIALIZE COMPLETE");
    }

}
