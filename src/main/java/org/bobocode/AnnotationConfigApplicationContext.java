package org.bobocode;

import lombok.SneakyThrows;
import org.bobocode.annotation.Bean;
import org.bobocode.annotation.Inject;
import org.bobocode.exceptions.NoSuchBeanException;
import org.bobocode.exceptions.NoUniqueBeanException;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ApplicationContext implementation - build the context based on the annotations in the source code
 *
 */
public class AnnotationConfigApplicationContext implements ApplicationContext {
    private final String packageName;
    private final Map<String, Object> context;

    public AnnotationConfigApplicationContext(String packageName) {
        Objects.requireNonNull(packageName);
        this.context = new ConcurrentHashMap<>();
        this.packageName = packageName;
        scan();
        initialize();
    }

    /**
     * Searching a classes with @Bean annotation in the package
     */
    private void scan() {
        var reflections = new Reflections(packageName);
        var beanTypes = reflections.getTypesAnnotatedWith(Bean.class);
        beanTypes.forEach(beanType -> storeBean(getBeanName(beanType),createInstance(beanType)));
    }

    /**
     * Initializing beans in context
     */
    private void initialize(){
        Objects.requireNonNull(context);
        context.values().forEach(this::initializeBean);
    }

    /** Store bean in the context
     *
     * @param beanName The name of bean
     * @param bean The instance of bean
     * @param <T> The generic type of instance
     */
    private <T> void storeBean(String beanName, T bean){
        Objects.requireNonNull(beanName);
        Objects.requireNonNull(bean);
        System.out.println("Bring Framework: Creating bean >>> "+beanName+": "+bean+":"+bean.getClass().getName());
        context.put(beanName, bean);
    }

    /**
     * Retrieve a bean name by value at the @Bean annotation or the Class simple name with lowercase first letter
     * @param beanType The Class of type
     * @return The name of bean
     * @param <T> The type of instance
     */
    private <T> String getBeanName(Class<T> beanType) {
        Objects.requireNonNull(beanType);
        String beanName = Arrays.stream(beanType.getAnnotations())
                .findFirst()
                .map(beanAnnotation -> ((Bean) beanAnnotation).value())
                .orElseGet(beanType::getSimpleName);
        if (beanName.isBlank()){
            return beanType.getSimpleName().substring(0,1).toLowerCase()+beanType.getSimpleName().substring(1);
        }
        return beanName;
    }

    private <T> void initializeBean(T bean){
        Objects.requireNonNull(bean);
        injectBean(bean);
    }

    private <T> void injectBean(T bean){
        Field[] fields = bean.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field-> injectField(field, field.getType(), bean));
    }
    @SneakyThrows
    private <T, R> void injectField(Field field, Class<R> beanType, T bean){
        R value = getBean(beanType);
        field.setAccessible(true);
        field.set(bean,value);
        System.out.println("Bring Framework: Inject field >>> Bean: "+bean+". Field name: "+field.getName()+". Value: "+value);
    }

    private <T> Predicate<Map.Entry<String, Object>> filterByBeanType(Class<T> beanType) {
        return (entry) -> entry.getValue().getClass().isAssignableFrom(beanType);
    }
    @SneakyThrows
    private <T> T createInstance(Class<T> beanType) {
        var constructor = Arrays.stream(beanType.getConstructors())
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        return beanType.cast(constructor.newInstance());
    }


    @Override
    public <T> T getBean(Class<T> beanType) throws NoSuchBeanException, NoUniqueBeanException {
        Map<String, T> beans = getAllBeans(beanType);
        if (beans.size() > 1) {
            throw new NoUniqueBeanException();
        }
        return beans.values().stream()
                .findFirst()
                .orElseThrow(NoSuchBeanException::new);
    }

    @Override
    public <T> T getBean(String name, Class<T> beanType) throws NoSuchBeanException {
        Objects.requireNonNull(name);
        return getAllBeans(beanType).entrySet().stream()
                .filter(kv -> kv.getKey().equals(name))
                .map(Map.Entry::getValue)
                .findAny()
                .orElseThrow(NoSuchBeanException::new);
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return context.entrySet().stream()
                .filter(filterByBeanType(beanType))
                .collect(Collectors.toMap(Map.Entry::getKey, kv -> beanType.cast(kv.getValue())));
    }


}
