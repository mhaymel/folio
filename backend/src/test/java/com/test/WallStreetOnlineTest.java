package com.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

final class WallStreetOnlineTest {

    @Test
    void test() {
        // given
        WallStreetOnline wallStreetOnline = new WallStreetOnline();
        //when
        double quote = wallStreetOnline.qouteFor("https://www.wallstreet-online.de/aktien/general-mills-aktie");
        System.out.println(quote);

        assertThat(quote).isGreaterThan(0);
    }

}