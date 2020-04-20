package com.intel.soak.plugin.mapred.encryption;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/23/13
 * Time: 3:58 AM
 * To change this template use File | Settings | File Templates.
 */
public enum DynamicJavaLibPathLoader {

    INSTANCE;

    /**
     * Because of the <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4280189">bug4280189</a>,
     * we could not add a java library path in runtime using
     * <code>System.setProperty("java.library.path", "$your_lib_path");</code>
     * The java.library.path would be executed only once while the JVM starting.
     * <p/>
     * The method helps us to modify the java.library.path in runtime with {@link ClassLoader}.
     * But it has one fatal limitation: The JVM must use the field 'user_paths' to
     * cache the java.library.path. In other words, the code is associate with the JVM implementation.
     *
     * @param libPath
     * @throws IOException
     */
    @Deprecated
    public static void addLibPath(String libPath) throws IOException {
        try {
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (int i = 0; i < paths.length; i++) {
                if (libPath.equals(paths[i])) {
                    return;
                }
            }
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = libPath;
            field.set(null, tmp);
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to get permission to set library path");
        } catch (NoSuchFieldException e) {
            throw new IOException("Failed to get filed handler to set library path");
        }
    }

}
