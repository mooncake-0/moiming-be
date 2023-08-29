package com.peoplein.moiming.config;

import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.repository.CategoryRepository;
import com.peoplein.moiming.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DevDbInit {

    @Bean
    @Profile("dev")
    CommandLineRunner init(RoleRepository roleRepository, CategoryRepository categoryRepository) {
        return (args) -> {
            Role roleUser = new Role(1L, "유저 권한", RoleType.USER);
            Role roleAdmin = new Role(2L, "관리자 권한", RoleType.ADMIN);

            roleRepository.save(roleUser);
            roleRepository.save(roleAdmin);

            // Category 추가
            int i = 1;
            Category cDANCE = new Category((long) i++, CategoryName.DANCE, 2, null);
            Category cOUTDOOR = new Category((long) i++, CategoryName.OUTDOOR, 2, null);
            Category cEXERCISE = new Category((long) i++, CategoryName.EXERCISE, 2, null);
            Category cBOOK = new Category((long) i++, CategoryName.BOOK, 2, null);
            Category cJOB = new Category((long) i++, CategoryName.JOB, 2, null);
            Category cLANGUAGE = new Category((long) i++, CategoryName.LANGUAGE, 2, null);
            Category cCULTURAL = new Category((long) i++, CategoryName.CULTURAL, 2, null);
            Category cMUSIC = new Category((long) i++, CategoryName.MUSIC, 2, null);
            Category cCRAFTS = new Category((long) i++, CategoryName.CRAFTS, 2, null);
            Category cCOOK = new Category((long) i++, CategoryName.COOK, 2, null);
            Category cPET = new Category((long) i++, CategoryName.PET, 2, null);
            Category cAMITY = new Category((long) i++, CategoryName.AMITY, 2, null);
            Category cHOBBY = new Category((long) i++, CategoryName.HOBBY, 2, null);

            categoryRepository.save(cDANCE);
            categoryRepository.save(cOUTDOOR);
            categoryRepository.save(cEXERCISE);
            categoryRepository.save(cBOOK);
            categoryRepository.save(cJOB);
            categoryRepository.save(cLANGUAGE);
            categoryRepository.save(cCULTURAL);
            categoryRepository.save(cMUSIC);
            categoryRepository.save(cCRAFTS);
            categoryRepository.save(cCOOK);
            categoryRepository.save(cPET);
            categoryRepository.save(cAMITY);
            categoryRepository.save(cHOBBY);


            categoryRepository.save(new Category((long) i++, CategoryName.LATIN_DANCE, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.SOCIAL_DANCE, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.BROADCAST, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.STREET_DANCE, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.BALLET, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.JAZZ_DANCE, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_DANCE, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.BELLY_DANCE, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.CONTEMPORARY_DANCE, 2, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.SWING_DANCE, 2, cDANCE));


            categoryRepository.save(new Category((long) i++, CategoryName.HIKING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.WALKING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.CAMPING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.DOMESTIC, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.INTERNATIONAL, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.FISHING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.PARAGLIDING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.DRIVE, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.PICNIC, 2, cOUTDOOR));


            categoryRepository.save(new Category((long) i++, CategoryName.CYCLING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BADMINTON, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BOWLING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.TENNIS, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.SKI, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.GOLF, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.CLIMBING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.DIET, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.FITNESS, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.YOGA, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.TABLE_TENNIS, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BILLIARDS, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.RUNNING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.SWIMMING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.SEA, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.FOOTBALL, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BASKETBALL, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BASEBALL, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.VOLLEYBALL, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.HORSEBACK_RIDING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.FENCING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BOXING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.TAEKWONDO, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.KENDO, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.MARTIAL_ARTS, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.SKATING, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.CRUISER, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.FOOT_VOLLEY, 2, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.ARCHERY, 2, cEXERCISE));


            categoryRepository.save(new Category((long) i++, CategoryName.READING, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.HUMANITIES, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.PSYCHOLOGY, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.PHILOSOPHY, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.HISTORY, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.ECONOMICS, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.WRITING, 2, cBOOK));


            categoryRepository.save(new Category((long) i++, CategoryName.INVESTMENT, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.BRANDING, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.SIDE_PROJECT, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.BUSINESS, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.CAREER, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.STUDY, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.FREELANCE, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.N_JOB, 2, cJOB));
        };
    }
}
