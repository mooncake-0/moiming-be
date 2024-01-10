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
    @Profile({"local", "dev"})
    CommandLineRunner init(RoleRepository roleRepository, CategoryRepository categoryRepository) {
        return (args) -> {

            Role roleUser = new Role(1L, "유저 권한", RoleType.USER);
            Role roleAdmin = new Role(2L, "관리자 권한", RoleType.ADMIN);

            roleRepository.save(roleUser);
            roleRepository.save(roleAdmin);

            // Category 추가
            int i = 1;
            Category cDANCE = new Category((long) i++, CategoryName.DANCE, 1, null);
            Category cOUTDOOR = new Category((long) i++, CategoryName.OUTDOOR, 1, null);
            Category cEXERCISE = new Category((long) i++, CategoryName.EXERCISE, 1, null);
            Category cBOOK = new Category((long) i++, CategoryName.BOOK, 1, null);
            Category cJOB = new Category((long) i++, CategoryName.JOB, 1, null);
            Category cLANGUAGE = new Category((long) i++, CategoryName.LANGUAGE, 1, null);
            Category cCULTURAL = new Category((long) i++, CategoryName.CULTURAL, 1, null);
            Category cMUSIC = new Category((long) i++, CategoryName.MUSIC, 1, null);
            Category cCRAFTS = new Category((long) i++, CategoryName.CRAFTS, 1, null);
            Category cCOOK = new Category((long) i++, CategoryName.COOK, 1, null);
            Category cPET = new Category((long) i++, CategoryName.PET, 1, null);
            Category cAMITY = new Category((long) i++, CategoryName.AMITY, 1, null);
            Category cHOBBY = new Category((long) i++, CategoryName.HOBBY, 1, null);

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

            // OUTDOOR 2차
            categoryRepository.save(new Category((long) i++, CategoryName.HIKING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.WALKING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.CAMPING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.DOMESTIC, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.INTERNATIONAL, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.FISHING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.PARAGLIDING, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.DRIVE, 2, cOUTDOOR));
            categoryRepository.save(new Category((long) i++, CategoryName.PICNIC, 2, cOUTDOOR));

            // EXERCISE 2차
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

            // BOOK 2차
            categoryRepository.save(new Category((long) i++, CategoryName.READING, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.HUMANITIES, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.PSYCHOLOGY, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.PHILOSOPHY, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.HISTORY, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.ECONOMICS, 2, cBOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.WRITING, 2, cBOOK));

            // JOB 2차
            categoryRepository.save(new Category((long) i++, CategoryName.INVESTMENT, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.BRANDING, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.SIDE_PROJECT, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.BUSINESS, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.CAREER, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.STUDY, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.FREELANCE, 2, cJOB));
            categoryRepository.save(new Category((long) i++, CategoryName.N_JOB, 2, cJOB));

            // LANGUAGE 2차
            categoryRepository.save(new Category((long) i++, CategoryName.ENGLISH, 2, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.JAPANESE, 2, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.CHINESE, 2, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.FRENCH, 2, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.SPANISH, 2, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.RUSSIAN, 2, cLANGUAGE));
            categoryRepository.save(new Category((long) i++, CategoryName.OTHER_LANGUAGE, 2, cLANGUAGE));

            // CULTURAL 2차
            categoryRepository.save(new Category((long) i++, CategoryName.MUSICAL, 2, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.SHOW, 2, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.MOVIE, 2, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.EXHIBITION, 2, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.SHOW_PRODUCE, 2, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.CULTURAL_HERITAGE, 2, cCULTURAL));
            categoryRepository.save(new Category((long) i++, CategoryName.FESTIVAL, 2, cCULTURAL));

            // MUSIC 2차
            categoryRepository.save(new Category((long) i++, CategoryName.VOCAL, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.GUITAR, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.UKULELE, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.DRUM, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.PIANO, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.VIOLIN, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.FLUTE, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.OCARINA, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.BAND, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.COMPOSE, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.INDE_MUSIC, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.HIPHOP, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.CLASSIC, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.JAZZ, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.ROCK, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.ELECTRONIC, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_MUSIC, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.CCM, 2, cMUSIC));
            categoryRepository.save(new Category((long) i++, CategoryName.NEW_AGE, 2, cMUSIC));

            // CRAFT 2차
            categoryRepository.save(new Category((long) i++, CategoryName.PAINTING, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.CALLIGRAPHY, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.FLOWER, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.CANDLE, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.COSMETICS, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.PROP, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.LEATHER, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.FURNITURE, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.MATERIAL, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.CLAY, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.KNITTING, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.KIDULT, 2, cCRAFTS));
            categoryRepository.save(new Category((long) i++, CategoryName.MAKEUP, 2, cCRAFTS));

            // COOK 2차
            categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_FOOD, 2, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.WESTERN_FOOD, 2, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.JAPANESE_FOOD, 2, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.CHINESE_FOOD, 2, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.BAKING, 2, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.HAND_DRIP, 2, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.WINE, 2, cCOOK));
            categoryRepository.save(new Category((long) i++, CategoryName.LIQUOR_MAKE, 2, cCOOK));

            // PET 2차
            categoryRepository.save(new Category((long) i++, CategoryName.DOG, 2, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.CAT, 2, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.FISH, 2, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.REPTILE, 2, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.BIRD, 2, cPET));
            categoryRepository.save(new Category((long) i++, CategoryName.RODENT, 2, cPET));

            // AMITY 2차
            categoryRepository.save(new Category((long)i ++, CategoryName.MUST_GO_RESTAURANT, 2, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.CAFE, 2, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.LIQUOR, 2, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.DINING, 2, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.PEER, 2, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.NEIGHBORHOOD, 2, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.CONCERN, 2, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.PARENTING, 2, cAMITY));
            categoryRepository.save(new Category((long)i ++, CategoryName.SMALL_TALK, 2, cAMITY));

            // HOBBY 2차
            categoryRepository.save(new Category((long)i ++, CategoryName.CAR, 2, cHOBBY));
            categoryRepository.save(new Category((long)i ++, CategoryName.PHOTOGRAPHY, 2, cHOBBY));
            categoryRepository.save(new Category((long)i ++, CategoryName.GAME, 2, cHOBBY));
            categoryRepository.save(new Category((long)i ++, CategoryName.VOLUNTEER, 2, cHOBBY));
            categoryRepository.save(new Category((long)i ++, CategoryName.FLOGGING, 2, cHOBBY));
        };
    }
}
