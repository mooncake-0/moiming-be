package com.peoplein.moiming.external;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Profile(value = "production")
public class FirebaseService {

    @Value("${app_files.fcm_path}")
    private String fcmFilePath;

    /*
     앱 시작시 Firebase 연동 (수명주기중 한 번 수행)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initFirebaseApp() throws IOException {

        FileInputStream serviceAccount =
                new FileInputStream(fcmFilePath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
            // For Shutdown Hook.
            Runtime.getRuntime().addShutdownHook(new Thread(new FirebaseShutDownRunnable(firebaseApp)));
            log.info("FIREBASE INITIALIZE COMPLETE");
        }
    }

    private static class FirebaseShutDownRunnable implements Runnable {

        private final FirebaseApp firebaseApp;

        public FirebaseShutDownRunnable(FirebaseApp firebaseApp) {
            this.firebaseApp = firebaseApp;
        }

        @Override
        public void run() {
            firebaseApp.delete();
        }
    }

}
