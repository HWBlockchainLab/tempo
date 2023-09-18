package org.hyperledger.tempo.external.chaincode.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class JavaClassLoader {
    @SuppressWarnings("unchecked")
    public void invokeClassMethod(String classBinName, String methodName, Integer iterationNumber, String chaincodeID) {
        try {
            // Create a new JavaClassLoader
            ClassLoader classLoader = this.getClass().getClassLoader();

            // Load the target class using its binary name
            Class loadedMyClass = classLoader.loadClass(classBinName);

            System.out.println("Loaded class name: " + loadedMyClass.getName());

            // Create a new instance from the loaded class
            Constructor constructor = loadedMyClass.getConstructor();
            Object myClassObject = constructor.newInstance();

            // Getting the target method from the loaded class and invoke it using its name
            Method method = loadedMyClass.getMethod(methodName, Integer.class, String.class);
            System.out.println("Invoked method name: " + method.getName());
            method.invoke(myClassObject, iterationNumber, chaincodeID);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
