package org.bobocode.demo;

import org.bobocode.annotation.Bean;
import org.bobocode.annotation.Inject;

@Bean
public class FirstBean {

    @Inject
    private SecondBean bean;
    public FirstBean() {
    }

    public SecondBean getBean() {
        return bean;
    }

}
