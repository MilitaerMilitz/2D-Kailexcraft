package com.github.kailex.api.util;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 *  This class splits a type into fields, constructors and methods and offers functionality to call them.
 *
 * @author Alexander Ley
 * @version 1.0
 *
 * @param <T> Type that should split up into fields, constructors and methods.
 */
public class Reflector<T> {

    private final HashMap<String, Field> fieldMap = new HashMap<>();
    private final HashMap<String, Constructor<?>> constructorMap = new HashMap<>();
    private final HashMap<String, Method> methodMap = new HashMap<>();

    /**
     * @param clazz Class that should split up into fields, constructors and methods.
     */
    public Reflector(Class<T> clazz) {
        //Fields
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            fieldMap.put(field.getName(), field);
        });

        //Constructors
        Arrays.stream(clazz.getDeclaredConstructors()).forEach(constructor -> {
            constructor.setAccessible(true);

            final long count = getConstructorCount(constructor.getName());
            constructorMap.put(constructor.getName() + "_" + count, constructor);
        });

        //Methods
        Arrays.stream(clazz.getDeclaredMethods()).forEach(method -> {
            method.setAccessible(true);

            final long count = getConstructorCount(method.getName());
            methodMap.put(method.getName() + "_" + count, method);
        });
    }

    public boolean containsField(String name){
        return fieldMap.containsKey(name);
    }

    public boolean containsConstructor(String name){
        return constructorMap.keySet().stream()
                .map(s -> s.substring(0, s.length() - 2))
                .collect(Collectors.toList())
                .contains(name);
    }

    public boolean containsMethod(String name){
        return methodMap.keySet().stream()
                .map(s -> s.substring(0, s.length() - 2))
                .collect(Collectors.toList())
                .contains(name);
    }

    public Field getField(String name){
        return fieldMap.get(name);
    }

    /**
     * @param skip Amount of constructors which should skipped.
     *             (e.g. If several constructors are available and you want to get the second one, skip have to be 1).
     * @return Returns constructor.
     */
    public Constructor<?> getConstructor(String name, int skip){
        return constructorMap.get(name + "_" + skip);
    }

    /**
     * @return Returns first located constructor.
     */
    public Constructor<?> getConstructor(String name){
        return getConstructor(name, 0);
    }

    /**
     * @param skip Amount of methods which should skipped.
     *             (e.g. If several methods are available and you want to get the second one, skip have to be 1).
     * @return Returns method.
     */
    public Method getMethod(String name, int skip){
        return methodMap.get(name + "_" + skip);
    }

    /**
     * @return Returns first located method.
     */
    public Method getMethod(String name){
        return getMethod(name, 0);
    }

    public long getFieldCount(){
        return fieldMap.size();
    }

    /**
     * @return Returns count of constructors with given name.
     */
    public long getConstructorCount(String name){
        return constructorMap.keySet().stream()
                .map(s -> s.substring(0, s.length() - 2))
                .filter(s -> s.equals(name)).count();
    }

    /**
     * @return Returns count of methods with given name.
     */
    public long getMethodCount(String name){
        return methodMap.keySet().stream()
                .map(s -> s.substring(0, s.length() - 2))
                .filter(s -> s.equals(name)).count();
    }

    /**
     * @param obj Object where the field content should be read.
     * @param name Name of field.
     * @return Returns field content.
     * @throws IllegalAccessException if this field object is enforcing Java language access control and the underlying field is inaccessible.
     */
    public Object accessField(Object obj, String name) throws IllegalAccessException {
        if (!containsField(name)) throw new IllegalArgumentException("Cannot find field: " + name + " in " + obj);

        return fieldMap.get(name).get(obj);
    }

    /**
     * @param obj Object where field content should be read.
     * @return Returns field content of static final declared fields.
     * @throws NoSuchFieldException if a field with the specified name is not found.
     * @throws IllegalAccessException if this Field object is enforcing Java language access control and the underlying field is inaccessible;
     *                                          or if this Field object has no write access.
     */
    public static Object accessStaticFinalField(Object obj, Field field) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        return field.get(obj);
    }

    /**
     * Updates field content of an object to newValue.
     * @param obj Object where field content should be overridden.
     * @param name Name of field.
     * @throws IllegalAccessException if this Field object is enforcing Java language access control and the underlying field is inaccessible or final;
     *                                          or if this Field object has no write access.
     */
    public void setFieldContent(Object obj, String name, Object newValue) throws IllegalAccessException {
        if (!containsField(name)) throw new IllegalArgumentException("Cannot find field: " + name + " in " + obj);

        fieldMap.get(name).set(obj, newValue);
    }

    /**
     * Updates static final declared field content.
     * @throws NoSuchFieldException if a field with the specified name is not found.
     * @throws IllegalAccessException if this Field object is enforcing Java language access control and the underlying field is inaccessible;
     *                                          or if this Field object has no write access.
     */
    public static void setStaticFinalField(Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    /**
     * @param skip Amount of constructors which should skipped.
     *             (e.g. If several constructors are available and you want to get the second one, skip have to be 1).
     * @return Returns a new instance of <T>.
     * @throws InvocationTargetException if the underlying constructor throws an exception.
     * @throws InstantiationException if the class that declares the underlying constructor represents an abstract class.
     * @throws IllegalAccessException if this Constructor object is enforcing Java language access control and the underlying constructor is inaccessible.
     */
    public Object invokeConstructor(String name, int skip, Object... parameter) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!constructorMap.containsKey(name + "_" + skip)) throw new IllegalArgumentException("Cannot find constructor: " + name + "_" + skip);

        return constructorMap.get(name + "_" + skip).newInstance(parameter);
    }

    /**
     * @return Returns a new instance of <T> and invokes the first located constructor.
     * @throws InvocationTargetException if the underlying constructor throws an exception.
     * @throws InstantiationException if the class that declares the underlying constructor represents an abstract class.
     * @throws IllegalAccessException if this Constructor object is enforcing Java language access control and the underlying constructor is inaccessible.
     */
    public Object invokeConstructor(String name, Object... parameter) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return invokeConstructor(name, 0, parameter);
    }

    /**
     * @param obj Object where the method should be invoked.
     * @param skip Amount of methods which should skipped.
     *             (e.g. If several methods are available and you want to get the second one, skip have to be 1).
     * @return Returns content that have the invoked methods returned.
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws IllegalAccessException if this Method object is enforcing Java language access control and the underlying method is inaccessible.
     */
    public Object invokeMethod(Object obj, String name, int skip, Object... parameter) throws InvocationTargetException, IllegalAccessException {
        if (!methodMap.containsKey(name + "_" + skip)) throw new IllegalArgumentException("Cannot find method: " + name + "_" + skip + " in " + obj);

        return methodMap.get(name + "_" + skip).invoke(obj, parameter);
    }

    /**
     * @param obj Object where the first located method should be invoked.
     * @return Returns content that have the first located invoked methods returned.
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws IllegalAccessException if this Method object is enforcing Java language access control and the underlying method is inaccessible.
     */
    public Object invokeMethod(Object obj, String name, Object... parameter) throws InvocationTargetException, IllegalAccessException {
        return invokeMethod(obj, name, 0, parameter);
    }

    /**
     * Invokes static declared method.
     * @param skip Amount of methods which should skipped.
     *             (e.g. If several methods are available and you want to get the second one, skip have to be 1).
     * @return Returns content that have the invoked methods returned.
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws IllegalAccessException if this Method object is enforcing Java language access control and the underlying method is inaccessible.
     */
    public static <T> Object invokeStaticMethod(Class<T> clazz, String name, int skip, Object... parameter) throws InvocationTargetException, IllegalAccessException {
        return new Reflector<T>(clazz).invokeMethod(null, name, skip, parameter);
    }

    /**
     * Invokes static declared method.
     * @return Returns content that have the first located invoked methods returned.
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws IllegalAccessException if this Method object is enforcing Java language access control and the underlying method is inaccessible.
     */
    public static <T> Object invokeStaticMethod(Class<T> clazz, String name, Object... parameter) throws InvocationTargetException, IllegalAccessException {
        return invokeStaticMethod(clazz, name, 0, parameter);
    }
}
