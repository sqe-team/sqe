/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbheaven.sqe.core.maven.utils;

import java.io.File;

/**
 * proxy to maven's FileUtilities class
 * @author mkleint
 */
public class FileUtilities {

    public static File resolveFilePath(File toFile, String configLocation) {
        return org.netbeans.modules.maven.api.FileUtilities.resolveFilePath(toFile, configLocation);
    }


}
