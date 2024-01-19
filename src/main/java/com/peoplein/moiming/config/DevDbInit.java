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
    @Profile("local")
    CommandLineRunner init(RoleRepository roleRepository, CategoryRepository categoryRepository) {
        return (args) -> {

            Role roleUser = new Role(1L, "유저 권한", RoleType.USER);
            Role roleAdmin = new Role(2L, "관리자 권한", RoleType.ADMIN);

            roleRepository.save(roleUser);
            roleRepository.save(roleAdmin);

            // Category 추가
            int i = 1;
            Category cDANCE = new Category((long) i++, CategoryName.DANCE, 0, null);
            Category cOUTDOOR = new Category((long) i++, CategoryName.OUTDOOR, 0, null);
            Category cEXERCISE = new Category((long) i++, CategoryName.EXERCISE, 0, null);
            Category cBOOK = new Category((long) i++, CategoryName.BOOK, 0, null);
            Category cJOB = new Category((long) i++, CategoryName.JOB, 0, null);
            Category cLANGUAGE = new Category((long) i++, CategoryName.LANGUAGE, 0, null);
            Category cCULTURAL = new Category((long) i++, CategoryName.CULTURAL, 0, null);
            Category cMUSIC = new Category((long) i++, CategoryName.MUSIC, 0, null);
            Category cCRAFTS = new Category((long) i++, CategoryName.CRAFTS, 0, null);
            Category cCOOK = new Category((long) i++, CategoryName.COOK, 0, null);
            Category cPET = new Category((long) i++, CategoryName.PET, 0, null);
            Category cAMITY = new Category((long) i++, CategoryName.AMITY, 0, null);
            Category cHOBBY = new Category((long) i++, CategoryName.HOBBY, 0, null);

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

            // DANCE 2차
            categoryRepository.save(new Category((long) i++, CategoryName.LATIN_DANCE, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.SOCIAL_DANCE, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.BROADCAST, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.STREET_DANCE, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.BALLET, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.JAZZ_DANCE, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_DANCE, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.BELLY_DANCE, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.CONTEMPORARY_DANCE, 1, cDANCE));
            categoryRepository.save(new Category((long) i++, CategoryName.SWING_DANCE, 1, cDANCE));

            // OUTDOOR 2차
            categoryRepository.save(new Category((long) i++, CategoryName.HIKING, 1, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.WALKING, 1, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.CAMPING, 1, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.DOMESTIC, 1, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.INTERNATIONAL, 1, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.FISHING, 1, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.PARAGLIDING, 1, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.DRIVE, 1, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.PICNIC, 1, cOUTDOOR));

            // EXERCISE 2차
            categoryRepository.save(new Category((long) i++, CategoryName.CYCLING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BADMINTON, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BOWLING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.TENNIS, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.SKI, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.GOLF, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.CLIMBING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.DIET, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.FITNESS, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.YOGA, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.TABLE_TENNIS, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BILLIARDS, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.RUNNING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.SWIMMING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.SEA, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.FOOTBALL, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BASKETBALL, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BASEBALL, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.VOLLEYBALL, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.HORSEBACK_RIDING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.FENCING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.BOXING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.TAEKWONDO, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.KENDO, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.MARTIAL_ARTS, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.SKATING, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.CRUISER, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.FOOT_VOLLEY, 1, cEXERCISE));
            categoryRepository.save(new Category((long) i++, CategoryName.ARCHERY, 1, cEXERCISE));

            // BOOK 2차
            categoryRepository.save(new Category((long) i++, CategoryName.READING, 1, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.HUMANITIES, 1, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.PSYCHOLOGY, 1, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.PHILOSOPHY, 1, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.HISTORY, 1, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.ECONOMICS, 1, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.WRITING, 1, cBOOK));

            // JOB 2차
            categoryRepository.save(new Category((long) i++, CategoryName.INVESTMENT, 1, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.BRANDING, 1, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.SIDE_PROJECT, 1, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.BUSINESS, 1, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.CAREER, 1, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.STUDY, 1, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.FREELANCE, 1, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.N_JOB, 1, cJOB));

            // LANGUAGE 2차
            categoryRepository.save(new Category((long) i++, CategoryName.ENGLISH, 1, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.JAPANESE, 1, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.CHINESE, 1, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.FRENCH, 1, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.SPANISH, 1, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.RUSSIAN, 1, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.OTHER_LANGUAGE, 1, cLANGUAGE));

            // CULTURAL 2차
            categoryRepository.save(new Category((long) i++, CategoryName.MUSICAL, 1, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.SHOW, 1, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.MOVIE, 1, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.EXHIBITION, 1, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.SHOW_PRODUCE, 1, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.CULTURAL_HERITAGE, 1, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.FESTIVAL, 1, cCULTURAL));

            // MUSIC 2차
            categoryRepository.save(new Category((long) i++, CategoryName.VOCAL, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.GUITAR, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.UKULELE, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.DRUM, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.PIANO, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.VIOLIN, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.FLUTE, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.OCARINA, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.BAND, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.COMPOSE, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.INDE_MUSIC, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.HIPHOP, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.CLASSIC, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.JAZZ, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.ROCK, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.ELECTRONIC, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_MUSIC, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.CCM, 1, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.NEW_AGE, 1, cMUSIC));

            // CRAFT 2차
            categoryRepository.save(new Category((long) i++, CategoryName.PAINTING, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.CALLIGRAPHY, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.FLOWER, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.CANDLE, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.COSMETICS, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.PROP, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.LEATHER, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.FURNITURE, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.MATERIAL, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.CLAY, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.KNITTING, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.KIDULT, 1, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.MAKEUP, 1, cCRAFTS));

            // COOK 2차
            categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_FOOD, 1, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.WESTERN_FOOD, 1, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.JAPANESE_FOOD, 1, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.CHINESE_FOOD, 1, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.BAKING, 1, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.HAND_DRIP, 1, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.WINE, 1, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.LIQUOR_MAKE, 1, cCOOK));

            // PET 2차
            categoryRepository.save(new Category((long) i++, CategoryName.DOG, 1, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.CAT, 1, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.FISH, 1, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.REPTILE, 1, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.BIRD, 1, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.RODENT, 1, cPET));

            // AMITY 2차
            categoryRepository.save(new Category((long)i ++, CategoryName.MUST_GO_RESTAURANT, 1, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.CAFE, 1, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.LIQUOR, 1, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.DINING, 1, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.PEER, 1, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.NEIGHBORHOOD, 1, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.CONCERN, 1, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.PARENTING, 1, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.SMALL_TALK, 1, cAMITY));

            // HOBBY 2차
            categoryRepository.save(new Category((long)i ++, CategoryName.CAR, 1, cHOBBY));
            categoryRepository.save(new Category((long)i ++, CategoryName.PHOTOGRAPHY, 1, cHOBBY));
            categoryRepository.save(new Category((long)i ++, CategoryName.GAME, 1, cHOBBY));
            categoryRepository.save(new Category((long)i ++, CategoryName.VOLUNTEER, 1, cHOBBY));
            categoryRepository.save(new Category((long)i ++, CategoryName.FLOGGING, 1, cHOBBY));
        };
    }
}
