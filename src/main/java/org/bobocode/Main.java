package org.bobocode;

import org.bobocode.demo.FirstBean;
import org.bobocode.demo.SecondBean;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("org.bobocode");
        //Testing...
        // Bean not found exception
        try {
            context.getBean(String.class);
        }catch (Exception ex){
            System.out.println(ex.getClass().getName());
        }

        FirstBean firstBean = context.getBean(FirstBean.class);
        System.out.println("Get First Bean from context: "+firstBean);
        SecondBean secondBean = context.getBean(SecondBean.class);
        System.out.println("Get Second Bean from context: "+secondBean);
        FirstBean firstBeanByName = context.getBean("firstBean", FirstBean.class);
        System.out.println("Get First Bean by Name from context: "+firstBeanByName);
        System.out.println("Firsts bean field: "+firstBeanByName.getBean());
    }
}